package io.cucumber.gherkin.pretty;

import io.cucumber.messages.NdjsonToMessageReader;
import io.cucumber.messages.ndjson.Json;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Source;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;


class PrettyAcceptanceTest {

    private static final NdjsonToMessageReader.Deserializer deserializer = Json.instance()
            .map(json -> json.deserializer(Envelope.class))
            .orElseThrow()::readValue;

    static List<TestCase> acceptance() throws IOException {
        List<Path> sources = getSources();
        List<TestCase> testCases = new ArrayList<>();
        sources.forEach(path -> testCases.add(new TestCase(path, new Pretty())));
        return testCases;
    }

    private static List<Path> getSources() throws IOException {
        try (Stream<Path> paths = Files.list(Paths.get("..", "testdata", "compatibility-kit", "src"))) {
            return paths
                    .filter(path -> path.getFileName().toString().endsWith(".ndjson"))
                    .collect(Collectors.toList());
        }
    }

    @ParameterizedTest
    @MethodSource("acceptance")
    void test(TestCase testCase) throws Exception {
        var gherkinDocument = testCase.getGherkinDocument();
        var pretty = testCase.pretty;
        assertEquals(testCase.getSource().getData(), pretty.format(gherkinDocument));
    }

    static class TestCase {
        private final Path path;
        private final Pretty pretty;
        private final String name;
        private @Nullable Source source;
        private @Nullable GherkinDocument gherkinDocument;

        TestCase(Path path, Pretty pretty) {
            this.path = path;
            this.pretty = pretty;
            String fileName = path.getFileName().toString();
            this.name = fileName.substring(0, fileName.lastIndexOf(".ndjson"));
        }

        GherkinDocument getGherkinDocument() throws IOException {
            if (gherkinDocument == null) {
                findMessages();
            }
            return requireNonNull(gherkinDocument);
        }

        Source getSource() throws IOException {
            if (source == null) {
                findMessages();
            }
            return requireNonNull(source);
        }

        private void findMessages() throws IOException {
            if (this.source != null && this.gherkinDocument != null) {
                return;
            }
            try (var in = Files.newInputStream(path)) {
                try (var reader = new NdjsonToMessageReader(in, deserializer)) {
                    Envelope line;
                    while ((line = reader.readLine()) != null) {
                        line.getSource().ifPresent(m -> this.source = m);
                        line.getGherkinDocument().ifPresent(m -> this.gherkinDocument = m);
                        // Don't read the whole file if not needed.
                        if (this.source != null && this.gherkinDocument != null) {
                            return;
                        }
                    }
                }
            }
            throw new IllegalStateException("Could not find source and gherkin document in " + path);
        }

        @Override
        public String toString() {
            return name;
        }

    }
}
