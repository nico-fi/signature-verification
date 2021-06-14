import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

class PDFConverter {

	static List<BufferedImage> getImages(String fileName, int dpi) throws IOException {
		PDDocument document = null;
		try {
			document = Loader.loadPDF(new File(fileName));
			PDFRenderer renderer = new PDFRenderer(document);
			List<BufferedImage> images = new ArrayList<>();
			for (int i = 0; i < document.getNumberOfPages(); i++)
				images.add(renderer.renderImageWithDPI(i, dpi, ImageType.BINARY));
			return images;
		} finally {
			if (document != null)
				document.close();
		}
	}

}