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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        private final Path source;
        private final Pretty pretty;
        private final String name;
        private @Nullable List<Envelope> messages;

        TestCase(Path source, Pretty pretty) {
            this.source = source;
            this.pretty = pretty;
            String fileName = source.getFileName().toString();
            this.name = fileName.substring(0, fileName.lastIndexOf(".ndjson"));
        }

        GherkinDocument getGherkinDocument() throws IOException {
            return findMessages().stream() //
                    .map(Envelope::getGherkinDocument) //
                    .filter(Optional::isPresent) //
                    .findFirst() //
                    .filter(Optional::isPresent) //
                    .map(Optional::get) //
                    .orElseThrow();
        }

        Source getSource() throws IOException {
            return findMessages().stream() //
                    .map(Envelope::getSource) //
                    .filter(Optional::isPresent) //
                    .findFirst() //
                    .filter(Optional::isPresent) //
                    .map(Optional::get) //
                    .orElseThrow();
        }

        private List<Envelope> findMessages() throws IOException {
            if (messages != null) {
                return messages;
            }

            try (var in = Files.newInputStream(source)) {
                try (var reader = new NdjsonToMessageReader(in, deserializer)) {
                    messages = reader.lines() //
                            .filter(envelope ->
                                    envelope.getGherkinDocument().isPresent() //
                                            || envelope.getSource().isPresent()) //
                            .toList();
                }
            }
            return messages;
        }

        @Override
        public String toString() {
            return name;
        }

    }
}
