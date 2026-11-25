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

    public Pretty() {

    }

    public String format(GherkinDocument gherkinDocument) {
        var pretty = new PrettyHandler();
        new GherkinDocumentWalker().walkGherkinDocument(gherkinDocument, pretty);
        return pretty.toString();
    }

    private static final class PrettyHandler implements Handler {

        private static final String INDENT = "  ";

        private final DataTableFormatter tableFormatter = new DataTableFormatter();
        private final DocStringFormatter docStringFormatter = new DocStringFormatter();
        private final CommentFormatter commentFormatter = new CommentFormatter();
        private final StringBuilder out = new StringBuilder();

        private int consecutiveNewLinesWritten = 1; // empty document
        private int enqueuedEmptyLines = 0;

        @Override
        public void startFeature(int level, Feature feature) {
            ensureEmptyLine();
        }

        @Override
        public void handleFeature(int level, Feature feature) {
            writeLine(level, formatTitle(feature.getKeyword(), feature.getName()));
        }

        @Override
        public void handleLanguage(int level, String language) {
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
            docStringFormatter.formatTo(docString, line -> writeLine(level, line));
        }

        @Override
        public void startDataTable(int level, DataTable dataTable) {
            tableFormatter.setCurrent(dataTable);
        }

        @Override
        public void finishDataTable(int level, DataTable dataTable) {
            tableFormatter.clearCurrent();
        }

        @Override
        public void startExamples(int level, Examples examples) {
            ensureEmptyLine();
            tableFormatter.setCurrent(examples);
        }

        @Override
        public void handleExamples(int level, Examples examples) {
            writeLine(level, formatTitle(examples.getKeyword(), examples.getName()));
        }

        @Override
        public void finishExamples(int level, Examples examples) {
            tableFormatter.clearCurrent();
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
        public void handleCommentBlock(int level, List<Comment> comments) {
            commentFormatter.formatCommentBlockTo(comments, comment -> writeLine(level, comment));
        }

        @Override
        public void startDetachedCommentBlock(int level, List<Comment> comments) {
            ensureEmptyLine();
        }

        @Override
        public void handleDetachedCommentBlock(int level, List<Comment> comments) {
            commentFormatter.formatCommentBlockTo(comments, comment -> writeLine(level, comment));
        }

        @Override
        public void finishDetachedCommentBlock(int level, List<Comment> comments) {
            enqueueEmptyLine();
        }

        private void writeLine(int level, String line) {
            if (line.isBlank()) {
                writeLine();
                return;
            }
            if (enqueuedEmptyLines > 0) {
                writeLine();
            }
            out.append(INDENT.repeat(level));
            out.append(line);
            writeLine();
            consecutiveNewLinesWritten = 0;
        }

        private void ensureEmptyLine() {
            if (consecutiveNewLinesWritten == 0) {
                writeLine();
            }
        }

        private void enqueueEmptyLine() {
            enqueuedEmptyLines++;
        }

        private void writeLine() {
            // TODO: Which newline?
            out.append("\n");
            consecutiveNewLinesWritten++;
            enqueuedEmptyLines = 0;
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


        @Override
        public String toString() {
            return out.toString();
        }
    }
}
