package io.cucumber.gherkin.pretty.test;

import io.cucumber.gherkin.GherkinParser;
import io.cucumber.gherkin.pretty.Pretty;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.GherkinDocument;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


class PrettyAcceptanceTest {

    private static final GherkinParser parser = GherkinParser.builder()
            .includePickles(false)
            .build();

    private static List<Path> findFeatures(Path dir) throws IOException {
        List<Path> result;
        try (Stream<Path> paths = Files.list(dir)) {
            result = paths
                    .filter(path -> path.getFileName().toString().endsWith(".feature"))
                    .toList();
        }
        return result;
    }

    @Nested
    class GherkinUtils {

        static List<TestCase> acceptance() throws IOException {
            var actualDirectory = Paths.get("..", "testdata", "gherkin-utils", "src");
            var expectedDirectory = Paths.get("..", "testdata", "gherkin-utils", "expected");
            return findFeatures(actualDirectory)
                    .stream()
                    .map(actual -> new TestCase(actual, expectedDirectory, new Pretty()))
                    .toList();
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        void test(TestCase testCase) throws Exception {
            assertEquals(testCase.getExpected(), testCase.pretty.format(testCase.getActual()));
        }
    }

    @Nested
    class CompatibilityKit {

        static List<TestCase> acceptance() throws IOException {
            var testData = Paths.get("..", "testdata", "compatibility-kit", "src");
            return findFeatures(testData)
                    .stream()
                    // Tests a round trip
                    .map(actual -> new TestCase(actual, testData, new Pretty()))
                    .toList();
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        void test(TestCase testCase) throws Exception {
            assertEquals(testCase.getExpected(), testCase.pretty.format(testCase.getActual()));
        }

    }

    static class TestCase {
        private final Pretty pretty;
        private final String name;
        private final Path actual;
        private final Path expectedDirectory;

        TestCase(Path actual, Path expectedDirectory, Pretty pretty) {
            this.pretty = pretty;
            String fileName = actual.getFileName().toString();
            this.name = fileName.substring(0, fileName.lastIndexOf("."));
            this.actual = actual;
            this.expectedDirectory = expectedDirectory;
        }

        GherkinDocument getActual() throws IOException {
            return parseGherkinDocument(actual);
        }

        String getExpected() throws IOException {
            return readExpected(expectedDirectory, actual.getFileName());
        }

        @Override
        public String toString() {
            return name;
        }

        private static GherkinDocument parseGherkinDocument(Path path) throws IOException {
            return parser.parse(path)
                    .filter(envelope -> envelope.getGherkinDocument().isPresent())
                    .findFirst()
                    .flatMap(Envelope::getGherkinDocument)
                    .orElseThrow();
        }

        private static String readExpected(Path expected, Path fileName) throws IOException {
            return Files.readString(expected.resolve(fileName));
        }
    }
}
