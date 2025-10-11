import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	public static final Pattern LINE_BREAK_PATTERN = Pattern.compile("[\\r\\n]+");

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

	public static void run(final String task, final Callable<Object> callable) throws Exception {
		final long startTime = System.currentTimeMillis();
		final Object result = callable.call();
		IO.println("%s: %s (Run time %d ms)".formatted(task, result, System.currentTimeMillis() - startTime));
	}

	public static void expect(final Object o, final Object toBe) {
		if (!o.equals(toBe)) {
			throw new AssertionError("Expected %s, got %s".formatted(toBe, o));
		}
	}
}