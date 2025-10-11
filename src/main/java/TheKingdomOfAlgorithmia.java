/**
 * <a href="https://everybody.codes/event/2024/quests">The Kingdom of Algorithmia</a>
 */
void main() throws Exception {
	testAll();

	Utils.run("Quest 1 Part 1", () -> calculatePotionsForEnemies(Utils.readInputAsCharArray("Quest1Part1.txt")));
	Utils.run("Quest 1 Part 2", () -> calculatePotionsForGroups(Utils.readInput("Quest1Part2.txt"), 2));
	Utils.run("Quest 1 Part 3", () -> calculatePotionsForGroups(Utils.readInput("Quest1Part3.txt"), 3));
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

private static void testAll() {
	Utils.expect(calculatePotionsForEnemies("ABBAC".toCharArray()), 5);
	Utils.expect(calculatePotionsForGroups("AxBCDDCAxD", 2), 28);
	Utils.expect(calculatePotionsForGroups("xBxAAABCDxCC", 3), 30);
}