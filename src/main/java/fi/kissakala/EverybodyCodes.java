package fi.kissakala;

/**
 * <a href="https://everybody.codes/home">Everybody codes</a>
 */
public class EverybodyCodes {
	static void main() {
		Utils.run("Utils tests", Utils::testAll);
		IO.println();

		IO.println("=== The Kingdom of Algorithmia ===");
		Utils.run("The Kingdom of Algorithmia", TheKingdomOfAlgorithmia::solve);
	}
}