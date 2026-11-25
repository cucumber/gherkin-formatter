package io.cucumber.gherkin.pretty.test;

import io.cucumber.gherkin.GherkinParser;
import io.cucumber.gherkin.pretty.Pretty;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.GherkinDocument;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class TestCase {

    private static final GherkinParser parser = GherkinParser.builder()
            .includePickles(false)
            .build();

    private final io.cucumber.gherkin.pretty.Pretty pretty;
    private final String testCaseName;
    private final String name;
    private final Path actualPath;
    private final Path expectedPath;


    TestCase(Path actualPath, Path expectedDirectory, io.cucumber.gherkin.pretty.Pretty pretty) {
        this(
                "",
                actualPath,
                expectedDirectory,
                pretty

        );
    }

    TestCase(String testCaseName, Path actualPath, Path expectedDirectory, io.cucumber.gherkin.pretty.Pretty pretty) {
        this.name = getName(actualPath);
        this.testCaseName = testCaseName;
        this.pretty = pretty;
        this.actualPath = actualPath;
        this.expectedPath = expectedDirectory.resolve(actualPath.getFileName());
    }

    private static String getName(Path actualPath) {
        String fileName = actualPath.getFileName().toString();
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    Pretty pretty() {
        return pretty;
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
