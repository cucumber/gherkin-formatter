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

        private int depth = -1;
        private int consecutiveNewLinesWritten = 1; // empty document
        private int enqueuedEmptyLines = 0;

        @Override
        public void startFeature(Feature feature) {
            depth++;
            ensureEmptyLine();
        }

        @Override
        public void handleFeature(Feature feature) {
            writeLine(formatTitle(feature.getKeyword(), feature.getName()));
        }

        @Override
        public void finishFeature(Feature feature) {
            depth--;
        }

        @Override
        public void handleLanguage(String language) {
            if ("en".equals(language)) {
                return;
            }
            writeLine("# language: %s".formatted(language));
        }

        @Override
        public void startDescription(String description) {
            depth++;
            // TODO: Make spacing configurable
            ensureEmptyLine();
        }

        @Override
        public void handleDescription(String description) {
            description.stripTrailing().stripIndent().lines()
                    .map(String::stripTrailing)
                    .forEach(this::writeLine);
        }

        @Override
        public void finishDescription(String description) {
            depth--;
            ensureEmptyLine();
        }

        @Override
        public void startRule(Rule rule) {
            ensureEmptyLine();
            depth++;
        }

        @Override
        public void handleRule(Rule rule) {
            writeLine(formatTitle(rule.getKeyword(), rule.getName()));
        }

        @Override
        public void finishRule(Rule rule) {
            depth--;
        }

        @Override
        public void startBackground(Background background) {
            depth++;
            ensureEmptyLine();
        }

        @Override
        public void handleBackground(Background background) {
            writeLine(formatTitle(background.getKeyword(), background.getName()));
        }

        @Override
        public void finishBackground(Background background) {
            depth--;
        }

        @Override
        public void startScenario(Scenario scenario) {
            depth++;
            ensureEmptyLine();
        }

        @Override
        public void handleScenario(Scenario scenario) {
            writeLine(formatTitle(scenario.getKeyword(), scenario.getName()));
        }

        @Override
        public void finishScenario(Scenario scenario) {
            depth--;
        }

        @Override
        public void startStep(Step step) {
            depth++;
        }

        @Override
        public void handleStep(Step step) {
            writeLine(formatStep(step.getKeyword(), step.getText()));
        }

        @Override
        public void finishStep(Step step) {
            depth--;
        }

        @Override
        public void startDocString(DocString docString) {
            depth++;
        }

        @Override
        public void handleDocString(DocString docString) {
            docStringFormatter.formatTo(docString, this::writeLine);
        }

        @Override
        public void finishDocString(DocString docString) {
            depth--;
        }

        @Override
        public void startDataTable(DataTable dataTable) {
            depth++;
            tableFormatter.setCurrent(dataTable);
        }

        @Override
        public void finishDataTable(DataTable dataTable) {
            depth--;
            tableFormatter.clearCurrent();
        }

        @Override
        public void handleDataTableRow(TableRow tableRow) {
            tableFormatter.formatTableRowTo(tableRow, this::writeLine);
        }

        @Override
        public void startExamples(Examples examples) {
            depth++;
            ensureEmptyLine();
            tableFormatter.setCurrent(examples);
        }

        @Override
        public void handleExamples(Examples examples) {
            writeLine(formatTitle(examples.getKeyword(), examples.getName()));
        }

        @Override
        public void finishExamples(Examples examples) {
            depth--;
            tableFormatter.clearCurrent();
        }

        @Override
        public void startExamplesTableRow(TableRow tableRow) {
            depth++;
        }

        @Override
        public void handleExamplesTableRow(TableRow tableRow) {
            tableFormatter.formatTableRowTo(tableRow, this::writeLine);
        }

        @Override
        public void finishExamplesTableRow(TableRow tableRow) {
            depth--;
        }

        @Override
        public void handleTags(List<Tag> tags) {
            writeLine(tags.stream().map(Tag::getName).collect(Collectors.joining(" ")));
        }

        @Override
        public void handleComments(List<Comment> comments) {
            commentFormatter.formatCommentBlockTo(comments, this::writeLine);
        }

        @Override
        public void startDetachedComments(List<Comment> comments) {
            depth++;
            ensureEmptyLine();
        }

        @Override
        public void handleDetachedComments(List<Comment> comments) {
            commentFormatter.formatCommentBlockTo(comments, this::writeLine);
        }

        @Override
        public void finishDetachedComment(List<Comment> comments) {
            depth--;
            enqueueEmptyLine();
        }

        private void writeLine(String line) {
            if (line.isBlank()) {
                writeLine();
                return;
            }
            if (enqueuedEmptyLines > 0) {
                writeLine();
            }
            out.append(INDENT.repeat(depth));
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
