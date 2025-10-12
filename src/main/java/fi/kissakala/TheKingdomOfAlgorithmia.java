package fi.kissakala;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static fi.kissakala.Utils.*;

/**
 * <a href="https://everybody.codes/event/2024/quests">The Kingdom of Algorithmia</a>
 */
public class TheKingdomOfAlgorithmia {
	private static final Pattern QUEST_2_PATTERN = Pattern.compile("^WORDS:([A-Z,]+)[\\r\\n]+([A-Z\\s,.]+)$");

	static void main() throws Exception {
		testAll();

		IO.println("=== Quest 1 ===");
		run("Part 1", () -> calculatePotionsForEnemies(readInputAsCharArray("TheKingdomOfAlgorithmia/Quest1Part1.txt")));
		run("Part 2", () -> calculatePotionsForGroups(readInput("TheKingdomOfAlgorithmia/Quest1Part2.txt"), 2));
		run("Part 3", () -> calculatePotionsForGroups(readInput("TheKingdomOfAlgorithmia/Quest1Part3.txt"), 3));

		IO.println("=== Quest 2 ===");
		run("Part 1", () -> countRunicWordsAndSymbols(readInput("TheKingdomOfAlgorithmia/Quest2Part1.txt"), false).wordCount());
		run("Part 2", () -> countRunicWordsAndSymbols(readInput("TheKingdomOfAlgorithmia/Quest2Part2.txt"), true).symbolsCount());

		IO.println("=== Quest 3 ===");
		run("Part 1", () -> slopeCalculator(readInput("TheKingdomOfAlgorithmia/Quest3Part1.txt"), false));
		run("Part 2", () -> slopeCalculator(readInput("TheKingdomOfAlgorithmia/Quest3Part2.txt"), false));
		run("Part 3", () -> slopeCalculator(readInput("TheKingdomOfAlgorithmia/Quest3Part3.txt"), true));

		IO.println("=== Quest 4 ===");
		run("Part 1", () -> countMiniumHammerStrikes(readInputAsRows("TheKingdomOfAlgorithmia/Quest4Part1.txt", Integer::parseInt)));
		run("Part 2", () -> countMiniumHammerStrikes(readInputAsRows("TheKingdomOfAlgorithmia/Quest4Part2.txt", Integer::parseInt)));
		run("Part 3", () -> countMiniumHammerStrikesForPartThree(readInputAsRows("TheKingdomOfAlgorithmia/Quest4Part3.txt", Integer::parseInt)));
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
				result += currentGroup.stream().mapToInt(TheKingdomOfAlgorithmia::calculatePotionsForEnemies).sum();

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
		final Matcher matcher = matchInput(input, QUEST_2_PATTERN);
		final char[][] grid = Arrays.stream(LINE_BREAK_PATTERN.split(matcher.group(2)))
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

	private static int slopeCalculator(final String input, final boolean diagonal) {
		final Integer[][] grid = pad2DArray(stringAs2DArray(input, s -> s.equals("#") ? 1 : 0, Integer.class), 1, 0);

		final List<XY> toBeDug = new ArrayList<>();
		do {
			toBeDug.clear();

			for (int y = 0; y < grid.length; y++) {
				for (int x = 0; x < grid[y].length; x++) {
					if (grid[y][x] == 0) continue;

					if (Objects.equals(grid[y][x - 1], grid[y][x]) // left
						&& Objects.equals(grid[y - 1][x], grid[y][x]) // up
						&& Objects.equals(grid[y][x + 1], grid[y][x]) // right
						&& Objects.equals(grid[y + 1][x], grid[y][x]) // down
						&& (!diagonal || Objects.equals(grid[y - 1][x - 1], grid[y][x]))
						&& (!diagonal || Objects.equals(grid[y - 1][x + 1], grid[y][x]))
						&& (!diagonal || Objects.equals(grid[y + 1][x - 1], grid[y][x]))
						&& (!diagonal || Objects.equals(grid[y + 1][x + 1], grid[y][x]))
					) { toBeDug.add(new XY(x, y)); }
				}
			}

			toBeDug.forEach(xy -> grid[xy.y()][xy.x()]++);
		} while (!toBeDug.isEmpty());

		return Arrays.stream(grid)
			.mapToInt(row -> Arrays.stream(row).reduce(0, Integer::sum))
			.sum();
	}

	private static int countMiniumHammerStrikes(final List<Integer> input) {
		input.sort(Integer::compareTo);
		final int shortest = input.getFirst();
		return input.stream()
			.skip(1L)
			.mapToInt(i -> i - shortest)
			.sum();
	}
	private static long countMiniumHammerStrikesForPartThree(final List<Integer> input) {
		input.sort(Integer::compareTo);
		final int median = input.get(input.size() / 2);

		long result = 0;
		for (final Integer nail : input) {
			result += Math.abs(nail - median);
		}
		return result;
	}

	private static void testAll() {
		expect(calculatePotionsForEnemies("ABBAC".toCharArray()), 5);
		expect(calculatePotionsForGroups("AxBCDDCAxD", 2), 28);
		expect(calculatePotionsForGroups("xBxAAABCDxCC", 3), 30);

		Map.of(
			"AWAKEN THE POWER ADORNED WITH THE FLAMES BRIGHT IRE", 4,
			"THE FLAME SHIELDED THE HEART OF THE KINGS", 3,
			"POWE PO WER P OWE R", 2,
			"THERE IS THE END", 3
		).forEach((inscription, count) -> expect(countRunicWordsAndSymbols("WORDS:THE,OWE,MES,ROD,HER\n\n" + inscription, false).wordCount(), count));
		expect(
			countRunicWordsAndSymbols("WORDS:THE,OWE,MES,ROD,HER,QAQ\n\n" + String.join("\n", List.of(
				"AWAKEN THE POWE ADORNED WITH THE FLAMES BRIGHT IRE",
				"THE FLAME SHIELDED THE HEART OF THE KINGS",
				"POWE PO WER P OWE R",
				"THERE IS THE END",
				"QAQAQ"
			)), true).symbolsCount(),
			42L
		);

		Map.of(false, 35, true, 29).forEach((diagonal, expectedResult) -> expect(slopeCalculator("""
			..........
			..###.##..
			...####...
			..######..
			..######..
			...####...
			..........""", diagonal), expectedResult));

		expect(countMiniumHammerStrikes(new ArrayList<>(List.of(3, 4, 7, 8))), 10);
		expect(countMiniumHammerStrikesForPartThree(new ArrayList<>(List.of(2, 4, 5, 6, 8))), 8L);
	}

	private record RunicWordsAndSymbolsCount(int wordCount, long symbolsCount) {}
}