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

import java.util.List;
import java.util.stream.Collectors;

public final class Pretty {

    public Pretty(){

    }

    public String format(GherkinDocument gherkinDocument){
        var pretty = new PrettyHandler();
        new GherkinDocumentWalker().walkGherkinDocument(gherkinDocument, pretty);
        return pretty.toString();
    }

    private static final class PrettyHandler implements Handler {

        private static final String DOCSTRING_SEPARATOR = "\"\"\"";
        private static final String ESCAPED_DOCSTRING_SEPARATOR = "\\\\\"\\\\\"\\\\\"";
        private static final String DOCSTRING_ALTERNATIVE_SEPARATOR = "```";
        private static final String ESCAPED_DOCSTRING_ALTERNATIVE_SEPARATOR = "\\\\`\\\\`\\\\`";
        private static final String INDENT = "  ";

        private final DataTableFormatter tableFormatter = new DataTableFormatter();
        private final StringBuilder builder = new StringBuilder();

        private int consecutiveNewLinesWritten = 1; // empty document

        @Override
        public void startFeature(int level, Feature feature) {
            ensureEmptyLine();
        }

        @Override
        public void handleFeature(int level, Feature feature) {
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
        public void startDescription(int level, String description) {
            // TODO: Make spacing configurable
            ensureEmptyLine();
        }

        @Override
        public void handleDescription(int level, String description) {
            description.stripTrailing().stripIndent().lines()
                    .map(String::stripTrailing)
                    .forEach(s -> writeLine(level, s));
        }

        @Override
        public void finishDescription(int level, String description) {
            ensureEmptyLine();
        }

        @Override
        public void startRule(int level, Rule rule) {
            ensureEmptyLine();
        }

        @Override
        public void handleRule(int level, Rule rule) {
            writeLine(level, formatTitle(rule.getKeyword(), rule.getName()));
        }

        @Override
        public void startBackground(int level, Background background) {
            ensureEmptyLine();
        }

        @Override
        public void handleBackground(int level, Background background) {
            writeLine(level, formatTitle(background.getKeyword(), background.getName()));
        }

        @Override
        public void startScenario(int level, Scenario scenario) {
            ensureEmptyLine();
        }

        @Override
        public void handleScenario(int level, Scenario scenario) {
            writeLine(level, formatTitle(scenario.getKeyword(), scenario.getName()));
        }

        @Override
        public void handleStep(int level, Step step) {
            writeLine(level, formatStep(step.getKeyword(), step.getText()));
        }

        @Override
        public void handleDocString(int level, DocString docString) {
            writeLine(level, formatDocStringCommentOpen(docString));
            docString.getContent()
                    .lines()
                    .map(docStringLine -> formatDocStringLine(docString, docStringLine))
                    .forEach(line -> writeLine(level, line));
            writeLine(level, formatDocStringCommentClose(docString));
        }


        private String formatDocStringLine(DocString docString, String docStringLine) {
            if (docString.getDelimiter().equals(DOCSTRING_SEPARATOR)) {
                return docStringLine.replaceAll(DOCSTRING_SEPARATOR, ESCAPED_DOCSTRING_SEPARATOR);
            } else {
                return docStringLine.replaceAll(DOCSTRING_ALTERNATIVE_SEPARATOR, ESCAPED_DOCSTRING_ALTERNATIVE_SEPARATOR);
            }

        }

        @Override
        public void handleDataTable(int level, DataTable dataTable) {
            tableFormatter.setCurrentTable(dataTable);
        }

        @Override
        public void startExample(int level, Examples scenario) {
            ensureEmptyLine();
        }

        @Override
        public void handleExamples(int level, Examples examples) {
            writeLine(level, formatTitle(examples.getKeyword(), examples.getName()));
            tableFormatter.setCurrentTable(examples);
        }

        @Override
        public void handleTableRow(int level, TableRow tableRow) {
            tableFormatter.formatTableRowTo(tableRow, line -> writeLine(level, line));
        }

        @Override
        public void handleTagLine(int level, List<Tag> tags) {
            writeLine(level, tags.stream().map(Tag::getName).collect(Collectors.joining(" ")));
        }

        @Override
        public void handleComment(int level, Comment comment) {
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

        private void ensureEmptyLine() {
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
