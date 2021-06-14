import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntBiFunction;

class Comparator {

	private List<Signature> templates;

	private List<Signature> tests;

	private List<boolean[]> evaluations = new ArrayList<>();

	Comparator(List<Signature> templates, List<Signature> tests) {
		this.templates = templates;
		this.tests = tests;
		evaluations.add(compare(s -> s.bwRatioGrid()));
		evaluations.add(compare(s -> s.upperContour()));
		evaluations.add(compare(s -> s.lowerContour()));
		evaluations.add(compare((s, t) -> s.overlapMatchingPoints(t)));
		evaluations.add(compare((s, t) -> s.surfMatchingPoints(t)));
	}

	private boolean[] compare(Function<Signature, int[]> foo) {
		int match[] = new int[tests.size()];
		int feature[][] = new int[templates.size()][];
		HashMap<Set<Integer>, Double> comparison = new HashMap<>();
		for (Signature t : templates)
			feature[templates.indexOf(t)] = foo.apply(t);
		for (int i = 0; i < templates.size(); i++) {
			for (int j = i + 1; j < templates.size(); j++)
				comparison.put(Set.of(i, j), cosineSimilarity(feature[i], feature[j]));
			double threshold = 0;
			for (int j = 0; j < templates.size(); j++)
				threshold += i != j ? comparison.get(Set.of(i, j)) : 0;
			threshold /= templates.size() - 1;
			for (Signature s : tests)
				if (cosineSimilarity(foo.apply(s), feature[i]) >= threshold)
					match[tests.indexOf(s)]++;
		}
		return decide(match);
	}

	private boolean[] compare(ToIntBiFunction<Signature, Signature> foo) {
		int match[] = new int[tests.size()];
		HashMap<Set<Integer>, Integer> comparison = new HashMap<>();
		for (int i = 0; i < templates.size(); i++) {
			for (int j = i + 1; j < templates.size(); j++)
				comparison.put(Set.of(i, j), foo.applyAsInt(templates.get(i), templates.get(j)));
			double threshold = 0;
			for (int j = 0; j < templates.size(); j++)
				threshold += i != j ? comparison.get(Set.of(i, j)) : 0;
			threshold /= templates.size() - 1;
			for (Signature s : tests)
				if (foo.applyAsInt(s, templates.get(i)) >= threshold)
					match[tests.indexOf(s)]++;
		}
		return decide(match);
	}

	private boolean[] decide(int match[]) {
		boolean outcome[] = new boolean[match.length];
		for (int i = 0; i < match.length; i++)
			outcome[i] = match[i] >= templates.size() / 2 ? true : false;
		return outcome;
	}

	private double cosineSimilarity(int[] vectorA, int[] vectorB) {
		double dotProduct = 0;
		double normA = 0;
		double normB = 0;
		for (int i = 0; i < vectorA.length; i++) {
			dotProduct += vectorA[i] * vectorB[i];
			normA += Math.pow(vectorA[i], 2);
			normB += Math.pow(vectorB[i], 2);
		}
		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}

	public String toString() {
		String result = "Criterion\t\t\tTested signatures\n\n";
		result += "Black-White Ratio:\t\t" + arrayToString(evaluations.get(0));
		result += "Upper Contour:\t\t\t" + arrayToString(evaluations.get(1));
		result += "Lower Contour:\t\t\t" + arrayToString(evaluations.get(2));
		result += "Overlap Matching Points:\t" + arrayToString(evaluations.get(3));
		result += "Surf Matching Points:\t\t" + arrayToString(evaluations.get(4));
		result += "\nPredicted Authenticity:\t\t" + arrayToString(predictClass());
		result += "Actual Authenticity:\t\t" + arrayToString(new boolean[] { false, false, false, true, true, true });
		return result;
	}

	String arrayToString(boolean array[]) {
		String result = new String();
		for (boolean b : array)
			result += b + "\t";
		return result + "\n";
	}

	boolean[] predictClass() {
		boolean predictedClass[] = new boolean[tests.size()];
		for (int i = 0; i < tests.size(); i++) {
			int match = 0;
			for (boolean eval[] : evaluations)
				match += eval[i] ? 1 : 0;
			if (match > evaluations.size() / 2)
				predictedClass[i] = true;
		}
		return predictedClass;
	}

}
