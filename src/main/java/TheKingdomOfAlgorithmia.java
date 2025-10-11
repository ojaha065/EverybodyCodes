/**
 * <a href="https://everybody.codes/event/2024/quests">The Kingdom of Algorithmia</a>
 */
void main() throws Exception {
	testAll();

	IO.println("=== Quest 1 ===");
	Utils.run("Part 1", () -> calculatePotionsForEnemies(Utils.readInputAsCharArray("Quest1Part1.txt")));
	Utils.run("Part 2", () -> calculatePotionsForGroups(Utils.readInput("Quest1Part2.txt"), 2));
	Utils.run("Part 3", () -> calculatePotionsForGroups(Utils.readInput("Quest1Part3.txt"), 3));

	IO.println("=== Quest 2 ===");
	Utils.run("Part 1", () -> countRunicWordsAndSymbols(Utils.readInput("Quest2Part1.txt"), false).wordCount());
	Utils.run("Part 2", () -> countRunicWordsAndSymbols(Utils.readInput("Quest2Part2.txt"), true).symbolsCount());
}

private static int calculatePotionsForEnemies(final char... input) {
	int result = 0;
	for (final char c : input) {
		result += switch (c) {
			case 'B' -> 1;
			case 'C' -> 3;
			case 'D' -> 5; // Parts 2 and 3 only
			default -> 0;
		};
	}
	return result;
}
private static int calculatePotionsForGroups(final String input, final int groupSize) {
	final List<Character> currentGroup = new ArrayList<>();
	int result = 0;

	for (final char c : input.toCharArray()) {
		currentGroup.add(c);
		if (currentGroup.size() == groupSize) {
			result += currentGroup.stream().mapToInt(thisC -> calculatePotionsForEnemies(thisC)).sum();

			final long enemyCount = currentGroup.stream().filter(thisC -> thisC.equals('A') || thisC.equals('B') || thisC.equals('C') || thisC.equals('D')).count();
			if (enemyCount == 3L) {
				result += 6;
			}
			else if (enemyCount == 2L) {
				result += 2;
			}

			currentGroup.clear();
		}
	}

	return result;
}

private static RunicWordsAndSymbolsCount countRunicWordsAndSymbols(final String input, final boolean doReverseSearch) {
	final Matcher matcher = Utils.matchInput(input, Pattern.compile("^WORDS:([A-Z,]+)[\\r\\n]+([A-Z\\s,.]+)$"));
	final char[][] grid = Arrays.stream(Utils.LINE_BREAK_PATTERN.split(matcher.group(2)))
		.map(String::toCharArray)
		.toArray(char[][]::new);

	final List<String> words = new ArrayList<>(Arrays.asList(matcher.group(1).split(",")));
	if (doReverseSearch) {
		words.addAll(words.stream().map(Utils::reverse).toList());
	}

	int wordCount = 0;
	for (int y = 0; y < grid.length; y++) {
		final int finalY = y;
		final String inscription = new String(grid[y]);

		for (final String word : words) {
			int index = 0;
			while ((index = inscription.indexOf(word, index)) != -1) {
				wordCount++;
				IntStream.range(index, index + word.length()).forEach(i -> grid[finalY][i] = Character.toLowerCase(grid[finalY][i]));
				index++;
			}
		}
	}

	return new RunicWordsAndSymbolsCount(
		wordCount,
		Arrays.stream(grid)
			.mapToLong(row -> IntStream.range(0, row.length).filter(i -> Character.isLowerCase(row[i])).count())
			.sum()
	);
}

private static void testAll() {
	Utils.expect(calculatePotionsForEnemies("ABBAC".toCharArray()), 5);
	Utils.expect(calculatePotionsForGroups("AxBCDDCAxD", 2), 28);
	Utils.expect(calculatePotionsForGroups("xBxAAABCDxCC", 3), 30);

	Map.of(
		"AWAKEN THE POWER ADORNED WITH THE FLAMES BRIGHT IRE", 4,
		"THE FLAME SHIELDED THE HEART OF THE KINGS", 3,
		"POWE PO WER P OWE R", 2,
		"THERE IS THE END", 3
	).forEach((inscription, count) -> Utils.expect(countRunicWordsAndSymbols("WORDS:THE,OWE,MES,ROD,HER\n\n" + inscription, false).wordCount(), count));
	Utils.expect(
		countRunicWordsAndSymbols("WORDS:THE,OWE,MES,ROD,HER,QAQ\n\n" + String.join("\n", List.of(
			"AWAKEN THE POWE ADORNED WITH THE FLAMES BRIGHT IRE",
			"THE FLAME SHIELDED THE HEART OF THE KINGS",
			"POWE PO WER P OWE R",
			"THERE IS THE END",
			"QAQAQ"
		)), true).symbolsCount(),
		42L
	);
}

private record RunicWordsAndSymbolsCount(int wordCount, long symbolsCount) {}