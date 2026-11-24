package io.cucumber.gherkin.pretty;

import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.NdjsonToMessageReader;
import io.cucumber.messages.ndjson.Json;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Source;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


class PrettyAcceptanceTest {


    @Nested
    class GherkinUtils {
        private static final GherkinParser parser = GherkinParser.builder()
                .includePickles(false)
                .build();

        static List<TestCase> acceptance() throws IOException {
            List<Path> sources = findSources();
            List<TestCase> testCases = new ArrayList<>();
            for (Path path : sources) {
                var testCase = new TestCase( //
                        path, //
                        parseGherkinDocument(path), //
                        readExpected(path), //
                        new Pretty()
                );
                testCases.add(testCase);
            }
            return testCases;
        }

        private static String readExpected(Path actual) throws IOException {
            var fileName = actual.getFileName().toString();
            var expected = Paths.get("..", "testdata", "gherkin-utils", "expected", fileName);
            return Files.readString(expected);
        }

        private static GherkinDocument parseGherkinDocument(Path path) throws IOException {
            return parser.parse(path)
                    .filter(envelope -> envelope.getGherkinDocument().isPresent())
                    .findFirst()
                    .flatMap(Envelope::getGherkinDocument)
                    .orElseThrow();
        }

        private static List<Path> findSources() throws IOException {
            List<Path> result;
            try (Stream<Path> paths = Files.list(Paths.get("..", "testdata", "gherkin-utils", "src"))) {
                result = paths
                        .filter(path -> path.getFileName().toString().endsWith(".feature"))
                        .toList();
            }
            return result;
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        void test(TestCase testCase) {
            assertEquals(testCase.expected, testCase.pretty.format(testCase.actual));
        }
    }


    @Nested
    class CompatibilityKit {


        private static final NdjsonToMessageReader.Deserializer deserializer = Json.instance()
                .map(json -> json.deserializer(Envelope.class))
                .orElseThrow()::readValue;

        private static final Set<String> unsupported = Set.of(
                // The Markdown feature is written in Markdown so it will never
                // have a matching pretty gherkin representation.
                "markdown"
        );

        @ParameterizedTest
        @MethodSource("acceptance")
        void test(TestCase testCase) {
            if (unsupported.contains(testCase.name)) {
                return;
            }
            assertEquals(testCase.expected, testCase.pretty.format(testCase.actual));
        }

        static List<TestCase> acceptance() throws IOException {
            List<Path> sources = findSources();
            List<TestCase> testCases = new ArrayList<>();
            sources.forEach(path -> {
                var documents = findSourceAndGherkinDocument(path);
                var testCase = new TestCase( //
                        path, //
                        documents.gherkinDocument, //
                        documents.source.getData(), //
                        new Pretty() //
                );
                testCases.add(testCase);
            });
            return testCases;
        }

        private static List<Path> findSources() throws IOException {
            List<Path> result;
            try (Stream<Path> paths = Files.list(Paths.get("..", "testdata", "compatibility-kit", "src"))) {
                result = paths
                        .filter(path1 -> path1.getFileName().toString().endsWith(".ndjson"))
                        .toList();
            }
            return result;
        }

        record Documents(Source source, GherkinDocument gherkinDocument) {

        }

        private static Documents findSourceAndGherkinDocument(Path path) {
            Source source = null;
            GherkinDocument gherkinDocument = null;
            try (var in = Files.newInputStream(path)) {
                try (var reader = new NdjsonToMessageReader(in, deserializer)) {
                    Envelope line;
                    while ((line = reader.readLine()) != null) {
                        if (line.getSource().isPresent()) {
                            source = line.getSource().get();
                        }

                        if (line.getGherkinDocument().isPresent()) {
                            gherkinDocument = line.getGherkinDocument().get();
                        }
                        if (source != null && gherkinDocument != null) {
                            return new Documents(source, gherkinDocument);
                        }
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            throw new IllegalStateException("Could not find source and gherkin document in " + path);
        }

    }

    static class TestCase {
        private final Pretty pretty;
        private final String name;
        private final GherkinDocument actual;
        private final String expected;

        TestCase(Path path, GherkinDocument source, String expected, Pretty pretty) {
            this.pretty = pretty;
            String fileName = path.getFileName().toString();
            this.name = fileName.substring(0, fileName.lastIndexOf("."));
            this.actual = source;
            this.expected = expected;
        }

        @Override
        public String toString() {
            return name;
        }

    }
}
