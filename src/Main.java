import java.util.List;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws Exception {
		int users = 8;
		System.out.print("Which user do you want to test [1-" + users + "]? ");
		Scanner sc = new Scanner(System.in);
		int userIndex = sc.nextInt();
		sc.close();
		if (userIndex < 1 || userIndex > users)
			throw new Exception("Invalid input");

		System.out.print("\nDetecting authentic signatures...");
		List<Signature> templates = Signature.detectSignatures("pdf/templates" + userIndex + ".pdf");
		if (templates.size() < 3)
			throw new Exception("Insufficient number of templates");
		System.out.println("\tCompleted");

		System.out.print("Detecting test signatures...");
		List<Signature> tests = Signature.detectSignatures("pdf/tests" + userIndex + ".pdf");
		System.out.println("\t\tCompleted");

		System.out.print("Processing images...");
		Signature.preprocess(templates);
		Signature.preprocess(tests);
		System.out.println("\t\t\tCompleted");

		System.out.print("Saving images...");
		Signature.saveAsImages(templates, "png/templates" + userIndex);
		Signature.saveAsImages(tests, "png/tests" + userIndex);
		System.out.println("\t\t\tCompleted");

		System.out.print("Verifying authenticity...");
		Comparator evaluation = new Comparator(templates, tests);
		System.out.println("\t\tCompleted\n\n");

		System.out.println(evaluation);
	}

}