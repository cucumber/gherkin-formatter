package io.cucumber.gherkin.pretty;

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

import java.util.stream.Collectors;

public final class Pretty {

    public Pretty(){

    }

    public String format(GherkinDocument gherkinDocument){
        var pretty = new PrettyHandler();
        new GherkinDocumentWalker().walkGherkinDocument(gherkinDocument, pretty);
        return pretty.toString();
    }

    private static class PrettyHandler implements GherkinDocumentWalker.Handler {

        private static final String INDENT = "  ";

        private final PickleTableFormatter tableFormatter = new PickleTableFormatter();
        private final StringBuilder builder = new StringBuilder();

        private int consecutiveNewLinesWritten = 1; // empty document

        @Override
        public void start(int level, Feature feature) {
            ensureSpacing();
        }

        @Override
        public void handle(int level, Feature feature) {
            handleLanguage(level, feature);
            writeLine(level, formatTitle(feature.getKeyword(), feature.getName()));
        }

        private void handleLanguage(int level, Feature feature) {
            // TODO: Pull up.
            var language = feature.getLanguage();
            if ("en".equals(language)) {
                return;
            }
            writeLine(level, "# language: %s".formatted(language));
        }

        @Override
        public void start(int level, GherkinDocumentWalker.Description description) {
            ensureSpacing();
        }

        @Override
        public void handle(int level, GherkinDocumentWalker.Description description) {
            description.body().stripTrailing().stripIndent().lines()
                    .map(String::stripTrailing)
                    .forEach(s -> writeLine(level, s));
        }

        @Override
        public void finish(int level, GherkinDocumentWalker.Description description) {
            ensureSpacing();
        }

        @Override
        public void start(int level, Rule rule) {
            ensureSpacing();
        }

        @Override
        public void handle(int level, Rule rule) {
            writeLine(level, formatTitle(rule.getKeyword(), rule.getName()));
        }

        @Override
        public void start(int level, Background background) {
            ensureSpacing();
        }

        @Override
        public void handle(int level, Background background) {
            writeLine(level, formatTitle(background.getKeyword(), background.getName()));
        }

        @Override
        public void start(int level, Scenario scenario) {
            ensureSpacing();
        }

        @Override
        public void handle(int level, Scenario scenario) {
            writeLine(level, formatTitle(scenario.getKeyword(), scenario.getName()));
        }

        @Override
        public void handle(int level, Step step) {
            writeLine(level, formatStep(step.getKeyword(), step.getText()));
        }

        @Override
        public void handle(int level, DocString docString) {
            writeLine(level, formatDocStringCommentOpen(docString));
            docString.getContent()
                    .lines().forEach(line -> writeLine(level, line));
            writeLine(level, formatDocStringCommentClose(docString));
        }

        @Override
        public void handle(int level, DataTable dataTable) {
            tableFormatter.setCurrentTable(dataTable);
        }

        @Override
        public void start(int level, Examples scenario) {
            ensureSpacing();
        }

        @Override
        public void handle(int level, Examples examples) {
            writeLine(level, formatTitle(examples.getKeyword(), examples.getName()));
            tableFormatter.setCurrentTable(examples);
        }

        @Override
        public void handle(int level, TableRow tableRow) {
            tableFormatter.formatTableRowTo(tableRow, line -> writeLine(level, line));
        }

        @Override
        public void handle(int level, GherkinDocumentWalker.TagLine tags) {
            writeLine(level, tags.tags().stream().map(Tag::getName).collect(Collectors.joining(" ")));
        }

        @Override
        public void handle(int level, Comment comment) {
            writeLine(level, formatComment(comment));
        }

        private void writeLine(int level, String line) {
            if (line.isBlank()) {
                writeLine();
            } else {
                builder.append(INDENT.repeat(level));
                builder.append(line);
                writeLine();
                consecutiveNewLinesWritten = 0;
            }
        }

        private void ensureSpacing() {
            if (consecutiveNewLinesWritten == 0) {
                writeLine();
            }
        }

        private void writeLine() {
            // TODO: Which newline?
            builder.append("\n");
            consecutiveNewLinesWritten++;
        }

        private static String formatComment(Comment comment) {
            var trimmed = comment.getText().trim().substring(1).trim();
            return "# " + trimmed;
        }

        private static String formatTitle(String keyword, String name) {
            if (name.isEmpty()) {
                return keyword + ":";
            }
            return "%s: %s".formatted(keyword, name);
        }

        private String formatStep(String keyword, String text) {
            return "%s%s".formatted(keyword, text);
        }

        private String formatDocStringCommentOpen(DocString docString) {
            return "%s%s".formatted(docString.getDelimiter(), docString.getMediaType().orElse(""));
        }

        private String formatDocStringCommentClose(DocString docString) {
            return docString.getDelimiter();
        }

        @Override
        public String toString() {
            return builder.toString();
        }
    }
}
