package fi.kissakala;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Utils {
	public static final Pattern LINE_BREAK_PATTERN = Pattern.compile("[\\r\\n]+");
	public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

	public static String readInput(final String filename) throws IOException, URISyntaxException {
		final URL url = Utils.class.getClassLoader().getResource(filename);
		if (url == null) {
			throw new FileNotFoundException(filename);
		}

		return Files.readString(Path.of(url.toURI()), StandardCharsets.UTF_8);
	}

	public static char[] readInputAsCharArray(final String filename) throws IOException, URISyntaxException {
		return readInput(filename).toCharArray();
	}

	public static String[] readInputAsRows(final String filename) throws IOException, URISyntaxException {
		return LINE_BREAK_PATTERN.split(readInput(filename));
	}

	public static <T> List<T> readInputAsRows(final String filename, final Function<String, T> mapper) throws IOException, URISyntaxException {
		return Arrays.stream(readInputAsRows(filename)).map(mapper).collect(Collectors.toCollection(ArrayList::new));
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

	public static <T> Predicate<T> distinctByKey(final Function<? super T, ?> keyExtractor) {
		final Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(keyExtractor.apply(t));
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

		Tree.test();
	}

	public record XY(int x, int y) {}
	public record BouncerResult(int index, DIRECTION direction) {}
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