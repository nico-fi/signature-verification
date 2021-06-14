import static marvin.MarvinPluginCollection.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.stromberglabs.jopensurf.Surf;

import marvin.color.MarvinColorModelConverter;
import marvin.image.MarvinImage;
import marvin.image.MarvinSegment;
import marvin.io.MarvinImageIO;

class Signature {

	private MarvinImage image;

	private Signature(MarvinImage image) {
		this.image = image;
	}

	BufferedImage getImage() {
		return image.getBufferedImage();
	}

	static List<Signature> detectSignatures(String fileName) throws IOException {
		int dpi = 300;
		List<BufferedImage> images = PDFConverter.getImages(fileName, dpi);
		List<Signature> signatures = new ArrayList<>();
		int maxWhiteSpace = (int) (0.7 * dpi);
		int maxFontLineWidth = dpi;
		int minTextWidth = (int) (0.7 * dpi);
		int grayScaleThrsh = 127;
		int minHeight = (int) (0.25 * dpi);
		int startingPosition = 4 * dpi;
		for (BufferedImage i : images) {
			MarvinImage page = new MarvinImage(i);
			for (MarvinSegment s : findTextRegions(page, maxWhiteSpace, maxFontLineWidth, minTextWidth, grayScaleThrsh))
				if (s.height > minHeight && s.x1 > startingPosition)
					signatures.add(new Signature(page.subimage(s.x1, s.y1, s.width, s.height)));
		}
		return signatures;
	}

	static void preprocess(List<Signature> signatures) {
		signatures.forEach(s -> s.removeHorizontalLine());
		signatures.forEach(s -> s.removeMargins());
		signatures.forEach(s -> scale(s.image.clone(), s.image, 900));
		signatures.forEach(s -> s.image = MarvinColorModelConverter.rgbToBinary(s.image, 127));
	}

	private void removeHorizontalLine() {
		int BLACK = -16777216;
		for (int y = 0; y < image.getHeight(); y++) {
			int blacks = 0;
			for (int x = 0; x < image.getWidth(); x++)
				if (image.getIntColor(x, y) == BLACK)
					blacks++;
			if (blacks > image.getWidth() * 0.5)
				image.drawLine(0, y, image.getWidth() - 1, y, Color.WHITE);
		}
	}

	private void removeMargins() {
		int BLACK = -16777216;
		int x1 = 0;
		int x2 = image.getWidth() - 1;
		int y1 = 0;
		int y2 = image.getHeight() - 1;
		for (int y = 0; true; y++) {
			if (y == image.getHeight()) {
				x1++;
				y = 0;
			}
			if (image.getIntColor(x1, y) == BLACK)
				break;
		}
		for (int y = 0; true; y++) {
			if (y == image.getHeight()) {
				x2--;
				y = 0;
			}
			if (image.getIntColor(x2, y) == BLACK)
				break;
		}
		for (int x = 0; true; x++) {
			if (x == image.getWidth()) {
				y1++;
				x = 0;
			}
			if (image.getIntColor(x, y1) == BLACK)
				break;
		}
		for (int x = 0; true; x++) {
			if (x == image.getWidth()) {
				y2--;
				x = 0;
			}
			if (image.getIntColor(x, y2) == BLACK)
				break;
		}
		image = image.subimage(x1, y1, x2 - x1, y2 - y1);
	}

	static void saveAsImages(List<Signature> signatures, String path) throws IOException {
		File directory = new File(path);
		FileUtils.deleteDirectory(directory);
		directory.mkdirs();
		signatures.forEach(s -> MarvinImageIO.saveImage(s.image, path + "/" + (signatures.indexOf(s) + 1) + ".png"));
	}

	int[] bwRatioGrid() {
		int rows = 4;
		int columns = 10;
		int[] regions = new int[rows * columns];
		int width = image.getWidth() / columns;
		int height = image.getHeight() / rows;
		for (int i = 0; i < columns; i++)
			for (int j = 0; j < rows; j++) {
				for (int x = i * width; x < (i + 1) * width; x++)
					for (int y = j * height; y < (j + 1) * height; y++)
						if (image.getBinaryColor(x, y))
							regions[i * rows + j]++;
				regions[i * rows + j] = regions[i * rows + j] * 100 / (width * height);
			}
		return regions;
	}

	int[] upperContour() {
		int border[] = new int[image.getWidth()];
		for (int x = 0; x < image.getWidth(); x++)
			for (int y = 0; y < image.getHeight(); y++)
				if (image.getBinaryColor(x, y)) {
					border[x] = y;
					break;
				}
		return border;
	}

	int[] lowerContour() {
		int border[] = new int[image.getWidth()];
		for (int x = 0; x < image.getWidth(); x++)
			for (int y = image.getHeight() - 1; y >= 0; y--)
				if (image.getBinaryColor(x, y)) {
					border[x] = y;
					break;
				}
		return border;
	}

	int overlapMatchingPoints(Signature s) {
		MarvinImage sImage = MarvinColorModelConverter.binaryToRgb(s.image);
		scale(sImage.clone(), sImage, image.getWidth(), image.getHeight());
		sImage = MarvinColorModelConverter.rgbToBinary(sImage, 127);
		int matchingPoints = 0;
		for (int x = 0; x < image.getWidth(); x++)
			for (int y = 0; y < image.getHeight(); y++)
				if (image.getBinaryColor(x, y) && sImage.getBinaryColor(x, y))
					matchingPoints++;
		return matchingPoints;
	}

	int surfMatchingPoints(Signature s) {
		Surf surf = new Surf(this.getImage());
		return surf.getMatchingPoints(new Surf(s.getImage()), true).size();
	}

}
