package fi.kissakala;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("unused")
public class Utils {
	public static final Pattern LINE_BREAK_PATTERN = Pattern.compile("\\R+");
	public static final Pattern SINGLE_LINE_BREAK_PATTERN = Pattern.compile("\\R");
	public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

	public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static String readInput(final String filename) throws IOException, URISyntaxException {
		final URL url = Utils.class.getClassLoader().getResource(filename);
		if (url == null) {
			throw new FileNotFoundException(filename);
		}

		return Files.readString(Path.of(url.toURI()), StandardCharsets.UTF_8);
	}

	public static int readInputAsInt(final String filename) throws IOException, URISyntaxException {
		return Integer.parseInt(readInput(filename));
	}

	public static char[] readInputAsCharArray(final String filename) throws IOException, URISyntaxException {
		return readInput(filename).toCharArray();
	}

	public static String[] readInputAsRows(final String filename) throws IOException, URISyntaxException {
		return LINE_BREAK_PATTERN.split(readInput(filename));
	}

	public static <T> List<T> readInputAsRows(final String filename, final Function<String, T> mapper) throws IOException, URISyntaxException {
		return readInputStringAsRows(readInput(filename), mapper);
	}
	public static <T> List<T> readInputStringAsRows(final String input, final Function<String, T> mapper) {
		return Arrays.stream(LINE_BREAK_PATTERN.split(input)).map(mapper).collect(Collectors.toCollection(ArrayList::new));
	}

	@SuppressWarnings("unchecked")
	public static <T> T[][] stringAs2DArray(final String string, final Pattern rowSplitter, final Function<String, T> mapper, final Class<T> clazz) {
		return Arrays.stream(LINE_BREAK_PATTERN.split(string))
			.map(row -> Arrays.stream(rowSplitter == null ? row.split("") : rowSplitter.split(row))
				.map(mapper)
				.toArray(len -> (T[]) Array.newInstance(clazz, len))
			)
			.toArray(size -> (T[][]) Array.newInstance(clazz, size, 0));
	}

	public static Matcher matchInput(final String input, final Pattern pattern) {
		final Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			return matcher;
		}
		throw new RuntimeException("No match found!");
	}

	public static String reverse(final String string) {
		return new StringBuffer(string).reverse().toString();
	}

	@SuppressWarnings("unchecked")
	public static <T> T[][] pad2DArray(final T[][] array, final int padding, final T paddingValue) {
		final T[][] result = (T[][]) Array.newInstance(
			array.getClass().getComponentType().getComponentType(),
			array.length + (padding * 2), array[0].length + (padding * 2)
		);

		for (T[] row : result) {
			Arrays.fill(row, paddingValue);
		}
		for (int y = 0; y < array.length; y++) {
			System.arraycopy(array[y], 0, result[y + padding], padding, array[y].length);
		}

		return result;
	}

	public static BouncerResult bouncer(int length, int moves, DIRECTION startingDirection) {
		if (moves == 0) {
			return new BouncerResult(0, startingDirection);
		}
		if (length <= 1) {
			return new BouncerResult(0, (moves % 2 == 0) ? startingDirection : startingDirection.getOpposite());
		}

		final int bouncingPeriod = 2 * length;
		final int eff = moves % bouncingPeriod;

		if (eff == 0) {
			return new BouncerResult(0, startingDirection);
		}
		if (eff <= (length - 1)) {
			return new BouncerResult(eff, startingDirection);
		}
		if (eff == length) {
			return new BouncerResult(length - 1, startingDirection.getOpposite());
		}
		else {
			return new BouncerResult(2 * length - 1 - eff, startingDirection.getOpposite());
		}
	}

	public static <T> String getShortestPathIn2dGrid(final T[][] grid, final XY start, final XY end, final T wall) {
		final T[][] g = pad2DArray(grid, 1, wall);
		final StringBuilder builder = new StringBuilder();

		int x = start.x() + 1;
		int y = start.y() + 1;
		if (g[y][x] == wall) {
			throw new RuntimeException("Cannot start in a wall!");
		}

		final HashSet<XY> visited = new HashSet<>();
		while (builder.isEmpty() || x != end.x() + 1 || y != end.y() + 1) {
			builder.append(g[y][x]);

			if (g[y][x + 1] != wall && !visited.contains(new XY(x + 1, y))) x++;
			else if (g[y + 1][x] != wall && !visited.contains(new XY(x, y + 1))) y++;
			else if (g[y][x - 1] != wall && !visited.contains(new XY(x - 1, y))) x--;
			else if (g[y - 1][x] != wall && !visited.contains(new XY(x, y - 1))) y--;
			else throw new RuntimeException("No path found!");

			visited.add(new XY(x, y));
		}

		return builder.toString();
	}

	public static List<String> generatePermutations(final char firstChar, final int firstCount,
													final char secondChar, final int secondCount,
													final char thirdChar, final int thirdCount) {
		final List<String> result = new ArrayList<>();
		final char[] buffer = new char[firstCount + secondCount + thirdCount];
		backtrack(buffer, 0, firstCount, secondCount, thirdCount, firstChar, secondChar, thirdChar, result);
		return result;
	}
	private static void backtrack(final char[] buf, final int pos,
								  final int firstLeft, final int secondLeft, final int thirdLeft,
								  final char firstChar, final char secondChar, final char thirdChar,
								  final List<String> result) {
		if (pos == buf.length) {
			result.add(new String(buf));
			return;
		}

		if (firstLeft > 0) {
			buf[pos] = firstChar;
			backtrack(buf, pos + 1, firstLeft - 1, secondLeft, thirdLeft, firstChar, secondChar, thirdChar, result);
		}
		if (secondLeft > 0) {
			buf[pos] = secondChar;
			backtrack(buf, pos + 1, firstLeft, secondLeft - 1, thirdLeft, firstChar, secondChar, thirdChar, result);
		}
		if (thirdLeft > 0) {
			buf[pos] = thirdChar;
			backtrack(buf, pos + 1, firstLeft, secondLeft, thirdLeft - 1, firstChar, secondChar, thirdChar, result);
		}
	}

	public static <T> Predicate<T> distinctByKey(final Function<? super T, ?> keyExtractor) {
		final Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(keyExtractor.apply(t));
	}

	public static <T> int findIndex(final T[] array, final T value) {
		return IntStream.range(0, array.length)
			.filter(i -> Objects.equals(array[i], value))
			.findFirst()
			.orElse(-1);
	}

	public static <T> XY findFrom2dArray(final T[][] grid, final T value) {
		for (int y = 0; y < grid.length; y++) {
			for (int x = 0; x < grid[y].length; x++) {
				if (Objects.equals(grid[y][x], value)) {
					return new XY(x, y);
				}
			}
		}
		throw new RuntimeException(value + " not found from grid");
	}

	public static int gcd(int a, int b) {
		if (a == 0) {
			return b;
		}

		while (b != 0) {
			final int t = a % b;
			a = b;
			b = t;
		}

		return Math.abs(a);
	}

	/**
	 * Computes the dynamic programming table for the unbounded minimum–coin (coin-change) problem.
	 * @param target the maximum sum to compute minimum coin counts for; must be {@code >= 0}
	 * @param coins an array of coin denominations; each value must be positive.
	 * @return an integer array of size {@code target + 1}
	 */
	public static int[] computeUnboundedMinCoinDp(final int target, final Integer[] coins) {
		Arrays.sort(coins);

		final int[] dp = new int[target + 1];
		Arrays.fill(dp, Integer.MAX_VALUE);
		dp[0] = 0;

		for (int s = 1; s <= target; s++) {
			for (int stamp : coins) {
				if (stamp > s) {
					break;
				}
				dp[s] = Math.min(dp[s], dp[s - stamp] + 1);
			}
		}

		return dp;
	}

	public static void run(final String task, final Callable<Object> callable) throws Exception {
		final long startTime = System.currentTimeMillis();
		final Object result = callable.call();
		IO.println("%s: %s (Run time %d ms)".formatted(task, result, System.currentTimeMillis() - startTime));
	}
	public static void run(final String task, final Runnable runnable) {
		final long startTime = System.currentTimeMillis();
		runnable.run();
		IO.println("%s completed in %d ms".formatted(task, System.currentTimeMillis() - startTime));
	}

	public static void expect(final Object o, final Object toBe) {
		if (!o.equals(toBe)) {
			throw new AssertionError("Expected %s, got %s".formatted(toBe, o));
		}
	}

	public static void testAll() {
		List.of(
			new Triplet<>(1, 0, new BouncerResult(0, DIRECTION.DOWN)),
			new Triplet<>(1, 1, new BouncerResult(0, DIRECTION.UP)),
			new Triplet<>(1, 2, new BouncerResult(0, DIRECTION.DOWN)),
			new Triplet<>(1, 3, new BouncerResult(0, DIRECTION.UP)),
			new Triplet<>(2, 0, new BouncerResult(0, DIRECTION.DOWN)),
			new Triplet<>(2, 1, new BouncerResult(1, DIRECTION.DOWN)),
			new Triplet<>(2, 2, new BouncerResult(1, DIRECTION.UP)),
			new Triplet<>(2, 3, new BouncerResult(0, DIRECTION.UP)),
			new Triplet<>(2, 4, new BouncerResult(0, DIRECTION.DOWN)),
			new Triplet<>(3, 0, new BouncerResult(0, DIRECTION.DOWN)),
			new Triplet<>(3, 1, new BouncerResult(1, DIRECTION.DOWN)),
			new Triplet<>(3, 2, new BouncerResult(2, DIRECTION.DOWN)),
			new Triplet<>(3, 3, new BouncerResult(2, DIRECTION.UP)),
			new Triplet<>(3, 4, new BouncerResult(1, DIRECTION.UP)),
			new Triplet<>(3, 5, new BouncerResult(0, DIRECTION.UP)),
			new Triplet<>(3, 6, new BouncerResult(0, DIRECTION.DOWN)),
			new Triplet<>(4, 0, new BouncerResult(0, DIRECTION.DOWN)),
			new Triplet<>(4, 1, new BouncerResult(1, DIRECTION.DOWN)),
			new Triplet<>(4, 2, new BouncerResult(2, DIRECTION.DOWN)),
			new Triplet<>(4, 3, new BouncerResult(3, DIRECTION.DOWN)),
			new Triplet<>(4, 4, new BouncerResult(3, DIRECTION.UP)),
			new Triplet<>(4, 5, new BouncerResult(2, DIRECTION.UP)),
			new Triplet<>(4, 6, new BouncerResult(1, DIRECTION.UP)),
			new Triplet<>(4, 7, new BouncerResult(0, DIRECTION.UP)),
			new Triplet<>(4, 8, new BouncerResult(0, DIRECTION.DOWN)),
			new Triplet<>(5, 0, new BouncerResult(0, DIRECTION.DOWN)),
			new Triplet<>(5, 1, new BouncerResult(1, DIRECTION.DOWN)),
			new Triplet<>(5, 2, new BouncerResult(2, DIRECTION.DOWN)),
			new Triplet<>(5, 3, new BouncerResult(3, DIRECTION.DOWN)),
			new Triplet<>(5, 4, new BouncerResult(4, DIRECTION.DOWN)),
			new Triplet<>(5, 5, new BouncerResult(4, DIRECTION.UP)),
			new Triplet<>(5, 6, new BouncerResult(3, DIRECTION.UP)),
			new Triplet<>(5, 7, new BouncerResult(2, DIRECTION.UP)),
			new Triplet<>(5, 8, new BouncerResult(1, DIRECTION.UP)),
			new Triplet<>(5, 9, new BouncerResult(0, DIRECTION.UP)),
			new Triplet<>(5,10, new BouncerResult(0, DIRECTION.DOWN))
		).forEach(triplet -> expect(bouncer(triplet.first(), triplet.second(), DIRECTION.DOWN), triplet.third()));

		expect(getShortestPathIn2dGrid(stringAs2DArray("""
			S+===
			-   +
			=+=-+
			""", null, s -> s.charAt(0), Character.class), new XY(0, 0), new XY(0, 0), ' '), "S+===" + "+" + reverse("=+=-+") + "-");

		expect(new HashSet<>(generatePermutations('A', 5, 'B', 3, 'C', 3)).size(), 9240);

		Tree.test();
	}

	public record BouncerResult(int index, DIRECTION direction) {}

	public record XY(int x, int y) {}
	public record Pair<T, S>(T first, S second) {}
	public record Triplet<T, S, U>(T first, S second, U third) {}

	public enum DIRECTION {
		UP,
		DOWN,
		LEFT,
		RIGHT;

		public DIRECTION getOpposite() {
			return switch (this) {
				case UP -> DIRECTION.DOWN;
				case DOWN -> DIRECTION.UP;
				case LEFT -> DIRECTION.RIGHT;
				case RIGHT -> DIRECTION.LEFT;
			};
		}
	}
}