package io.cucumber.gherkin.pretty.test;

import io.cucumber.gherkin.GherkinParser;
import io.cucumber.gherkin.pretty.Pretty;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.GherkinDocument;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.CREATE;
import static org.junit.jupiter.api.Assertions.assertEquals;


class PrettyAcceptanceTest {

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
    class GherkinPretty {

        static List<TestCase> acceptance() throws IOException {
            var actualDirectory = Paths.get("..", "testdata", "gherkin-pretty", "src");
            var expectedDirectory = Paths.get("..", "testdata", "gherkin-pretty", "expected");
            return findFeatures(actualDirectory)
                    .stream()
                    .map(actual -> new TestCase(actual, expectedDirectory, new Pretty()))
                    .toList();
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        @DisabledIfEnvironmentVariable(named = "UPDATE_EXPECTED_FILES", matches = "true")
        void test(TestCase testCase) throws Exception {
            assertEquals(testCase.getExpected(), testCase.pretty.format(testCase.getActual()));
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        @EnabledIfEnvironmentVariable(named = "UPDATE_EXPECTED_FILES", matches = "true")
        void update(TestCase testCase) throws Exception {
            var formatted = testCase.pretty.format(testCase.getActual());
            Files.writeString(testCase.expectedPath, formatted, CREATE);
        }
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

        static Set<String> unsupported = Set.of("commentsBeforeAndAfterKeywords");

        @ParameterizedTest
        @MethodSource("acceptance")
        @DisabledIfEnvironmentVariable(named = "UPDATE_EXPECTED_FILES", matches = "true")
        void test(TestCase testCase) throws Exception {
            if (unsupported.contains(testCase.name)) {
                return;
            }
            assertEquals(testCase.getExpected(), testCase.pretty.format(testCase.getActual()));
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        @EnabledIfEnvironmentVariable(named = "UPDATE_EXPECTED_FILES", matches = "true")
        void update(TestCase testCase) throws Exception {
            if (unsupported.contains(testCase.name)) {
                return;
            }
            var formatted = testCase.pretty.format(testCase.getActual());
            Files.writeString(testCase.expectedPath, formatted, CREATE);
        }
    }

    @Nested
    class CompatibilityKit {

        static List<TestCase> acceptance() throws IOException {
            // Tests a round trip
            var expectedAndActual = Paths.get("..", "testdata", "compatibility-kit", "src");
            return findFeatures(expectedAndActual)
                    .stream()
                    .map(actual -> new TestCase(actual, expectedAndActual, new Pretty()))
                    .toList();
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        void test(TestCase testCase) throws Exception {
            assertEquals(testCase.getExpected(), testCase.pretty.format(testCase.getActual()));
        }

    }

    static class TestCase {

        private static final GherkinParser parser = GherkinParser.builder()
                .includePickles(false)
                .build();

        private final Pretty pretty;
        private final String name;
        private final Path actualPath;
        private final Path expectedPath;

        TestCase(Path actualPath, Path expectedDirectory, Pretty pretty) {
            this.pretty = pretty;
            String fileName = actualPath.getFileName().toString();
            this.name = fileName.substring(0, fileName.lastIndexOf("."));
            this.actualPath = actualPath;
            this.expectedPath = expectedDirectory.resolve(fileName);
        }

        GherkinDocument getActual() throws IOException {
            return parseGherkinDocument(actualPath);
        }

        String getExpected() throws IOException {
            return Files.readString(expectedPath);
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
    }
}
