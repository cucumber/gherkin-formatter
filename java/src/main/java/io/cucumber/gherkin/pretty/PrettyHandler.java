package io.cucumber.gherkin.pretty;

import io.cucumber.gherkin.pretty.GherkinDocumentWalker.Handler;
import io.cucumber.gherkin.pretty.PrettyHandler.Context;
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

import java.io.PrintWriter;
import java.util.List;
import java.util.function.Consumer;

import static io.cucumber.gherkin.pretty.GherkinPrettyFormatter.Element.BACKGROUND;
import static io.cucumber.gherkin.pretty.GherkinPrettyFormatter.Element.COMMENTS;
import static io.cucumber.gherkin.pretty.GherkinPrettyFormatter.Element.DATA_TABLE;
import static io.cucumber.gherkin.pretty.GherkinPrettyFormatter.Element.DATA_TABLE_ROW;
import static io.cucumber.gherkin.pretty.GherkinPrettyFormatter.Element.DESCRIPTION;
import static io.cucumber.gherkin.pretty.GherkinPrettyFormatter.Element.DETACHED_COMMENTS;
import static io.cucumber.gherkin.pretty.GherkinPrettyFormatter.Element.DOCUMENT;
import static io.cucumber.gherkin.pretty.GherkinPrettyFormatter.Element.DOC_STRING;
import static io.cucumber.gherkin.pretty.GherkinPrettyFormatter.Element.EXAMPLES;
import static io.cucumber.gherkin.pretty.GherkinPrettyFormatter.Element.EXAMPLES_TABLE_ROW;
import static io.cucumber.gherkin.pretty.GherkinPrettyFormatter.Element.FEATURE;
import static io.cucumber.gherkin.pretty.GherkinPrettyFormatter.Element.LANGUAGE;
import static io.cucumber.gherkin.pretty.GherkinPrettyFormatter.Element.RULE;
import static io.cucumber.gherkin.pretty.GherkinPrettyFormatter.Element.SCENARIO;
import static io.cucumber.gherkin.pretty.GherkinPrettyFormatter.Element.STEP;
import static io.cucumber.gherkin.pretty.GherkinPrettyFormatter.Element.TAGS;
import static io.cucumber.gherkin.pretty.GherkinPrettyFormatter.Margin.BEFORE;
import static io.cucumber.gherkin.pretty.GherkinPrettyFormatter.Margin.BOTH;
import static io.cucumber.gherkin.pretty.GherkinPrettyFormatter.Margin.NONE;
import static java.util.stream.Collectors.joining;

final class PrettyHandler implements Handler<Context, PrintWriter> {

    private final Configuration configuration;

    PrettyHandler(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Context createContext(PrintWriter writer) {
        return new Context(writer);
    }

    @Override
    public void startGherkinDocument(Context context, GherkinDocument gherkinDocument) {
        context.insertMarginBefore(DOCUMENT);
    }

    @Override
    public void finishGherkinDocument(Context context, GherkinDocument gherkinDocument) {
        context.insertMarginAfter(DOCUMENT);
    }

    @Override
    public void startFeature(Context context, Feature feature) {
        context.depth++;
        context.insertMarginBefore(FEATURE);
    }

    @Override
    public void handleFeature(Context context, Feature feature) {
        context.writeLine(formatTitle(feature.getKeyword(), feature.getName()));
    }

    @Override
    public void finishFeature(Context context, Feature feature) {
        context.depth--;
        context.insertMarginAfter(FEATURE);
    }

    @Override
    public void startLanguage(Context context, String language) {
        context.insertMarginBefore(LANGUAGE);
    }

    @Override
    public void handleLanguage(Context context, String language) {
        if ("en".equals(language)) {
            return;
        }
        context.writeLine("# language: %s".formatted(language));
    }

    @Override
    public void finishLanguage(Context context, String language) {
        context.insertMarginAfter(LANGUAGE);
    }


    @Override
    public void startDescription(Context context, String description) {
        if (description.isBlank()) {
            return;
        }
        context.depth++;
        context.insertMarginBefore(DESCRIPTION);
    }

    @Override
    public void handleDescription(Context context, String description) {
        // TODO: Description are multi line but don't have a location attached
        // or a line abstraction in Gherkin. This means comments around the
        // location are not accurately positioned.
        description.stripTrailing().stripIndent().lines()
                .map(String::stripTrailing)
                .forEach(context::writeLine);
    }

    @Override
    public void finishDescription(Context context, String description) {
        if (description.isBlank()) {
            return;
        }
        context.depth--;
        context.insertMarginAfter(DESCRIPTION);
    }

    @Override
    public void startRule(Context context, Rule rule) {
        context.depth++;
        context.insertMarginBefore(RULE);
    }

    @Override
    public void handleRule(Context context, Rule rule) {
        context.writeLine(formatTitle(rule.getKeyword(), rule.getName()));
    }

    @Override
    public void finishRule(Context context, Rule rule) {
        context.depth--;
        context.insertMarginAfter(RULE);
    }

    @Override
    public void startBackground(Context context, Background background) {
        context.depth++;
        context.insertMarginBefore(BACKGROUND);
    }

    @Override
    public void handleBackground(Context context, Background background) {
        context.writeLine(formatTitle(background.getKeyword(), background.getName()));
    }

    @Override
    public void finishBackground(Context context, Background background) {
        context.depth--;
        context.insertMarginAfter(BACKGROUND);
    }

    @Override
    public void startScenario(Context context, Scenario scenario) {
        context.depth++;
        context.insertMarginBefore(SCENARIO);
    }

    @Override
    public void handleScenario(Context context, Scenario scenario) {
        context.writeLine(formatTitle(scenario.getKeyword(), scenario.getName()));
    }

    @Override
    public void finishScenario(Context context, Scenario scenario) {
        context.depth--;
        context.insertMarginAfter(SCENARIO);
    }

    @Override
    public void startStep(Context context, Step step) {
        context.depth++;
        context.insertMarginBefore(STEP);
    }

    @Override
    public void handleStep(Context context, Step step) {
        context.writeLine(formatStep(step.getKeyword(), step.getText()));
    }

    @Override
    public void finishStep(Context context, Step step) {
        context.depth--;
        context.insertMarginAfter(STEP);
    }

    @Override
    public void startDocString(Context context, DocString docString) {
        context.depth++;
        context.insertMarginBefore(DOC_STRING);
    }

    @Override
    public void handleDocString(Context context, DocString docString) {
        context.docStringFormatter.formatTo(docString, context::writeLine);
    }

    @Override
    public void finishDocString(Context context, DocString docString) {
        context.depth--;
        context.insertMarginAfter(DOC_STRING);
    }

    @Override
    public void startDataTable(Context context, DataTable dataTable) {
        context.depth++;
        context.insertMarginBefore(DATA_TABLE);
        context.tableFormatter.setCurrent(dataTable);
    }

    @Override
    public void finishDataTable(Context context, DataTable dataTable) {
        context.depth--;
        context.insertMarginAfter(DATA_TABLE);
        context.tableFormatter.clearCurrent();
    }

    @Override
    public void startDataTableRow(Context context, TableRow tableRow) {
        context.insertMarginBefore(DATA_TABLE_ROW);
    }

    @Override
    public void handleDataTableRow(Context context, TableRow tableRow) {
        context.tableFormatter.formatTableRowTo(tableRow, context::writeLine);
    }

    @Override
    public void finishDataTableRow(Context context, TableRow tableRow) {
        context.insertMarginAfter(DATA_TABLE_ROW);
    }

    @Override
    public void startExamples(Context context, Examples examples) {
        context.depth++;
        context.insertMarginBefore(EXAMPLES);
        context.tableFormatter.setCurrent(examples);
    }

    @Override
    public void handleExamples(Context context, Examples examples) {
        context.writeLine(formatTitle(examples.getKeyword(), examples.getName()));
    }

    @Override
    public void finishExamples(Context context, Examples examples) {
        context.depth--;
        context.insertMarginAfter(EXAMPLES);
        context.tableFormatter.clearCurrent();
    }

    @Override
    public void startExamplesTableRow(Context context, TableRow tableRow) {
        context.depth++;
        context.insertMarginBefore(EXAMPLES_TABLE_ROW);
    }

    @Override
    public void handleExamplesTableRow(Context context, TableRow tableRow) {
        context.tableFormatter.formatTableRowTo(tableRow, context::writeLine);
    }

    @Override
    public void finishExamplesTableRow(Context context, TableRow tableRow) {
        context.depth--;
        context.insertMarginAfter(EXAMPLES_TABLE_ROW);
    }

    @Override
    public void startTags(Context context, List<Tag> tags) {
        context.insertMarginBefore(TAGS);
    }

    @Override
    public void handleTags(Context context, List<Tag> tags) {
        switch (configuration.tagLayout()) {
            case AS_IS -> context.writeLine(createTagLine(tags));
            case ONE_PER_LINE, ONE_LINE -> { /* no-op */ }
        }
    }

    @Override
    public void finishTags(Context context, List<Tag> tags) {
        switch (configuration.tagLayout()) {
            case AS_IS -> { /* no-op */ }
            case ONE_PER_LINE -> writeTagsTo(tags, context::writeLine);
            case ONE_LINE -> context.writeLine(createTagLine(tags));
        }
        context.insertMarginAfter(TAGS);
    }

    private String createTagLine(List<Tag> tags) {
        return switch (configuration.tagOrder()) {
            case USER_DEFINED_ORDER -> tags.stream().map(Tag::getName).collect(joining(" "));
            case SORTED -> tags.stream().map(Tag::getName).sorted().collect(joining(" "));
        };
    }

    private void writeTagsTo(List<Tag> tags, Consumer<String> writer) {
        switch (configuration.tagOrder()) {
            case USER_DEFINED_ORDER -> tags.stream().map(Tag::getName).forEach(writer);
            case SORTED -> tags.stream().map(Tag::getName).sorted().forEach(writer);
        }
    }

    @Override
    public void startComments(Context context, List<Comment> comments) {
        context.insertMarginBefore(COMMENTS);
    }

    @Override
    public void handleComments(Context context, List<Comment> comments) {
        context.commentFormatter.formatCommentBlockTo(comments, context::writeLine);
    }

    @Override
    public void finishComments(Context context, List<Comment> comments) {
        context.insertMarginAfter(COMMENTS);
    }

    @Override
    public void startDetachedComments(Context context, List<Comment> comments) {
        context.depth++;
        context.insertMarginBefore(DETACHED_COMMENTS);
    }

    @Override
    public void handleDetachedComments(Context context, List<Comment> comments) {
        context.commentFormatter.formatCommentBlockTo(comments, context::writeLine);
    }

    @Override
    public void finishDetachedComment(Context context, List<Comment> comments) {
        context.depth--;
        context.insertMarginAfter(DETACHED_COMMENTS);
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

    class Context {

        private final TableFormatter tableFormatter = new TableFormatter(configuration);
        private final DocStringFormatter docStringFormatter = new DocStringFormatter();
        private final CommentFormatter commentFormatter = new CommentFormatter();

        private final PrintWriter writer;

        private int depth = -1;
        private int consecutiveNewLinesWritten = 1; // empty document
        private int enqueuedEmptyLines = 0;

        Context(PrintWriter writer) {
            this.writer = writer;
        }

        void writeLine(String line) {
            if (line.isBlank()) {
                writeLine();
                return;
            }
            if (enqueuedEmptyLines > 0) {
                writeLine();
            }
            for (int i = 0; i < depth; i++) {
                writer.write(configuration.indentation());
            }
            writer.append(line);
            writeLine();
            consecutiveNewLinesWritten = 0;
        }

        void insertMarginBefore(GherkinPrettyFormatter.Element element) {
            var mode = configuration.margin().getOrDefault(element, NONE);
            if (mode == BEFORE || mode == BOTH) {
                ensureEmptyLine();
            }
        }

        void insertMarginAfter(GherkinPrettyFormatter.Element element) {
            var mode = configuration.margin().getOrDefault(element, NONE);
            if (mode == BEFORE || mode == BOTH) {
                enqueueEmptyLine();
            }
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
            writer.println();
            consecutiveNewLinesWritten++;
            enqueuedEmptyLines = 0;
        }
    }
}
