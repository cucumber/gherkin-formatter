package io.cucumber.gherkin.pretty.test;

import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.GherkinDocument;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

class TestCase {

    private static final GherkinParser parser = GherkinParser.builder()
            .includePickles(false)
            .build();

    private final BiConsumer<GherkinDocument, OutputStream> pretty;
    private final String testCaseName;
    private final String name;
    private final Path actualPath;
    private final Path expectedPath;


    TestCase(Path actualPath, Path expectedDirectory, BiConsumer<GherkinDocument, OutputStream> pretty) {
        this(
                "",
                actualPath,
                expectedDirectory,
                pretty

        );
    }

    TestCase(String testCaseName, Path actualPath, Path expectedDirectory, BiConsumer<GherkinDocument, OutputStream> pretty) {
        this.name = getName(actualPath);
        this.testCaseName = testCaseName;
        this.pretty = pretty;
        this.actualPath = actualPath;
        this.expectedPath = expectedDirectory.resolve(getExpectedName(name));
    }

    private  String getExpectedName(String name) {
        if (testCaseName.isEmpty()) {
            return name + ".feature";
        }
        return name + "." + testCaseName + ".feature";
    }

    private static String getName(Path actualPath) {
        String fileName = actualPath.getFileName().toString();
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    static List<Path> findFeatures(Path dir) throws IOException {
        List<Path> result;
        try (Stream<Path> paths = Files.list(dir)) {
            result = paths
                    .filter(path -> path.getFileName().toString().endsWith(".feature"))
                    .toList();
        }
        return result;
    }

    String pretty(GherkinDocument document) {
        var out = new ByteArrayOutputStream();
        pretty.accept(document, out);
        return out.toString(StandardCharsets.UTF_8);
    }

    void prettyTo(GherkinDocument document, OutputStream out) {
        pretty.accept(document, out);
    }

    GherkinDocument actual() throws IOException {
        return parseGherkinDocument(actualPath);
    }

    String expected() throws IOException {
        return Files.readString(expectedPath);
    }

    Path expectedPath() {
        return expectedPath;
    }

    String name() {
        return name;
    }

    @Override
    public String toString() {
        if (testCaseName.isEmpty()) {
            return name;
        }
        return name + " -> " + testCaseName;
    }

    private static GherkinDocument parseGherkinDocument(Path path) throws IOException {
        return parser.parse(path)
                .filter(envelope -> envelope.getGherkinDocument().isPresent())
                .findFirst()
                .flatMap(Envelope::getGherkinDocument)
                .orElseThrow();
    }
}
