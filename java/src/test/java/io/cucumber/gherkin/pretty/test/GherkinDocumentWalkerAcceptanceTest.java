package io.cucumber.gherkin.pretty.test;

import io.cucumber.gherkin.pretty.GherkinDocumentWalker;
import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.Comment;
import io.cucumber.messages.types.DataTable;
import io.cucumber.messages.types.DocString;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.TableRow;
import io.cucumber.messages.types.Tag;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.cucumber.gherkin.pretty.test.TestCase.findFeatures;
import static org.assertj.core.api.Assertions.assertThat;

class GherkinDocumentWalkerAcceptanceTest {

    @Nested
    class CompatibilityKit {

        static List<TestCase> acceptance() throws IOException {
            var actualDirectory = Path.of("../testdata/compatibility-kit/src");
            var expectedDirectory = Path.of("../testdata/compatibility-kit/expected");
            return findFeatures(actualDirectory)
                    .stream()
                    .map(actual -> new TestCase(actual, expectedDirectory, CompatibilityKit::formatTo))
                    .toList();
        }

        private static void formatTo(GherkinDocument document, OutputStream outputStream) {
            var handler = new LoggingHandler();
            new GherkinDocumentWalker().walkGherkinDocument(document, handler);
            try (var out = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)){
                out.write(handler.print());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        @DisabledIfEnvironmentVariable(named = "UPDATE_EXPECTED_FILES", matches = "true")
        void test(TestCase testCase) throws Exception {
            var formatted = testCase.pretty(testCase.actual());
            assertThat(formatted).isEqualToIgnoringNewLines(testCase.expected());
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        @EnabledIfEnvironmentVariable(named = "UPDATE_EXPECTED_FILES", matches = "true")
        void updateExpectedFiles(TestCase testCase) throws Exception {
            try (OutputStream out = Files.newOutputStream(testCase.expectedPath())) {
                testCase.prettyTo(testCase.actual(), out);
                Files.copy(testCase.expectedPath(), System.out);
            }
        }
    }

    private static class LoggingHandler implements GherkinDocumentWalker.Handler {
        private final List<String> calls = new ArrayList<>();
        private int depth = 0;

        private void log() {
            var caller = new Exception().getStackTrace()[1].getMethodName();
            if (caller.startsWith("finish")) {
                depth--;
            }

            calls.add(" ".repeat(depth) + caller);

            if (caller.startsWith("start")) {
                depth++;
            }

        }

        String print(){
            return String.join("\n", calls);
        }

        @Override
        public void startGherkinDocument(GherkinDocument gherkinDocument) {
            log();
        }

        @Override
        public void finishGherkinDocument(GherkinDocument gherkinDocument) {
            log();
        }

        @Override
        public void handleGherkinDocument(GherkinDocument gherkinDocument) {
            log();
        }

        @Override
        public void startFeature(Feature feature) {
            log();
        }

        @Override
        public void handleFeature(Feature feature) {
            log();
        }

        @Override
        public void finishFeature(Feature feature) {
            log();
        }

        @Override
        public void startLanguage(String language) {
            log();
        }

        @Override
        public void handleLanguage(String language) {
            log();
        }

        @Override
        public void finishLanguage(String language) {
            log();
        }

        @Override
        public void startDescription(String description) {
            log();
        }

        @Override
        public void handleDescription(String description) {
            log();
        }

        @Override
        public void finishDescription(String description) {
            log();
        }

        @Override
        public void startRule(Rule rule) {
            log();
        }

        @Override
        public void handleRule(Rule rule) {
            log();
        }

        @Override
        public void finishRule(Rule rule) {
            log();
        }

        @Override
        public void startBackground(Background background) {
            log();
        }

        @Override
        public void handleBackground(Background background) {
            log();
        }

        @Override
        public void finishBackground(Background background) {
            log();
        }

        @Override
        public void startScenario(Scenario scenario) {
            log();
        }

        @Override
        public void handleScenario(Scenario scenario) {
            log();
        }

        @Override
        public void finishScenario(Scenario scenario) {
            log();
        }

        @Override
        public void startStep(Step step) {
            log();
        }

        @Override
        public void handleStep(Step step) {
            log();
        }

        @Override
        public void finishStep(Step step) {
            log();
        }

        @Override
        public void startExamples(Examples examples) {
            log();
        }

        @Override
        public void handleExamples(Examples examples) {
            log();
        }

        @Override
        public void finishExamples(Examples examples) {
            log();
        }

        @Override
        public void startExamplesTableRow(TableRow tableRow) {
            log();
        }

        @Override
        public void handleExamplesTableRow(TableRow tableRow) {
            log();
        }

        @Override
        public void finishExamplesTableRow(TableRow tableRow) {
            log();
        }

        @Override
        public void startDataTable(DataTable dataTable) {
            log();
        }

        @Override
        public void handleDataTable(DataTable dataTable) {
            log();
        }

        @Override
        public void finishDataTable(DataTable dataTable) {
            log();
        }

        @Override
        public void startDataTableRow(TableRow tableRow) {
            log();
        }

        @Override
        public void handleDataTableRow(TableRow tableRow) {
            log();
        }

        @Override
        public void finishDataTableRow(TableRow tableRow) {
            log();
        }

        @Override
        public void startDocString(DocString docString) {
            log();
        }

        @Override
        public void handleDocString(DocString docString) {
            log();
        }

        @Override
        public void finishDocString(DocString docString) {
            log();
        }

        @Override
        public void startTags(List<Tag> tags) {
            log();
        }

        @Override
        public void handleTags(List<Tag> tags) {
            log();
        }

        @Override
        public void finishTags(List<Tag> tags) {
            log();
        }

        @Override
        public void startDetachedComments(List<Comment> comments) {
            log();
        }

        @Override
        public void handleDetachedComments(List<Comment> comments) {
            log();
        }

        @Override
        public void finishDetachedComment(List<Comment> comments) {
            log();
        }

        @Override
        public void startComments(List<Comment> comments) {
            log();
        }

        @Override
        public void handleComments(List<Comment> comments) {
            log();
        }

        @Override
        public void finishComments(List<Comment> comments) {
            log();
        }
    }
}
