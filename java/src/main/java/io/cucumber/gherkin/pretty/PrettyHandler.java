package io.cucumber.gherkin.pretty;

import io.cucumber.gherkin.pretty.GherkinDocumentWalker.Handler;
import io.cucumber.gherkin.pretty.PrettyHandler.Context;
import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.Comment;
import io.cucumber.messages.types.DataTable;
import io.cucumber.messages.types.DocString;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.TableRow;
import io.cucumber.messages.types.Tag;

import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

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
    public void startFeature(Context context, Feature feature) {
        context.depth++;
        context.ensureEmptyLine();
    }

    @Override
    public void handleFeature(Context context, Feature feature) {
        context.writeLine(formatTitle(feature.getKeyword(), feature.getName()));
    }

    @Override
    public void finishFeature(Context context, Feature feature) {
        context.depth--;
    }

    @Override
    public void handleLanguage(Context context, String language) {
        if ("en".equals(language)) {
            return;
        }
        context.writeLine("# language: %s".formatted(language));
    }

    @Override
    public void startDescription(Context context, String description) {
        context.depth++;
        // TODO: Make spacing configurable
        context.ensureEmptyLine();
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
        context.depth--;
        context.ensureEmptyLine();
    }

    @Override
    public void startRule(Context context, Rule rule) {
        context.ensureEmptyLine();
        context.depth++;
    }

    @Override
    public void handleRule(Context context, Rule rule) {
        context.writeLine(formatTitle(rule.getKeyword(), rule.getName()));
    }

    @Override
    public void finishRule(Context context, Rule rule) {
        context.depth--;
    }

    @Override
    public void startBackground(Context context, Background background) {
        context.depth++;
        context.ensureEmptyLine();
    }

    @Override
    public void handleBackground(Context context, Background background) {
        context.writeLine(formatTitle(background.getKeyword(), background.getName()));
    }

    @Override
    public void finishBackground(Context context, Background background) {
        context.depth--;
    }

    @Override
    public void startScenario(Context context, Scenario scenario) {
        context.depth++;
        context.ensureEmptyLine();
    }

    @Override
    public void handleScenario(Context context, Scenario scenario) {
        context.writeLine(formatTitle(scenario.getKeyword(), scenario.getName()));
    }

    @Override
    public void finishScenario(Context context, Scenario scenario) {
        context.depth--;
    }

    @Override
    public void startStep(Context context, Step step) {
        context.depth++;
    }

    @Override
    public void handleStep(Context context, Step step) {
        context.writeLine(formatStep(step.getKeyword(), step.getText()));
    }

    @Override
    public void finishStep(Context context, Step step) {
        context.depth--;
    }

    @Override
    public void startDocString(Context context, DocString docString) {
        context.depth++;
    }

    @Override
    public void handleDocString(Context context, DocString docString) {
        context.docStringFormatter.formatTo(docString, context::writeLine);
    }

    @Override
    public void finishDocString(Context context, DocString docString) {
        context.depth--;
    }

    @Override
    public void startDataTable(Context context, DataTable dataTable) {
        context.depth++;
        context.tableFormatter.setCurrent(dataTable);
    }

    @Override
    public void finishDataTable(Context context, DataTable dataTable) {
        context.depth--;
        context.tableFormatter.clearCurrent();
    }

    @Override
    public void handleDataTableRow(Context context, TableRow tableRow) {
        context.tableFormatter.formatTableRowTo(tableRow, context::writeLine);
    }

    @Override
    public void startExamples(Context context, Examples examples) {
        context.depth++;
        context.ensureEmptyLine();
        context.tableFormatter.setCurrent(examples);
    }

    @Override
    public void handleExamples(Context context, Examples examples) {
        context.writeLine(formatTitle(examples.getKeyword(), examples.getName()));
    }

    @Override
    public void finishExamples(Context context, Examples examples) {
        context.depth--;
        context.tableFormatter.clearCurrent();
    }

    @Override
    public void startExamplesTableRow(Context context, TableRow tableRow) {
        context.depth++;
    }

    @Override
    public void handleExamplesTableRow(Context context, TableRow tableRow) {
        context.tableFormatter.formatTableRowTo(tableRow, context::writeLine);
    }

    @Override
    public void finishExamplesTableRow(Context context, TableRow tableRow) {
        context.depth--;
    }

    @Override
    public void handleTags(Context context, List<Tag> tags) {
        // TODO: Add option to put each tag on a single line
        // TODO: Add option to sort tags
        context.writeLine(tags.stream().map(Tag::getName).collect(Collectors.joining(" ")));
    }

    @Override
    public void handleComments(Context context, List<Comment> comments) {
        context.commentFormatter.formatCommentBlockTo(comments, context::writeLine);
    }

    @Override
    public void startDetachedComments(Context context, List<Comment> comments) {
        context.depth++;
        context.ensureEmptyLine();
    }

    @Override
    public void handleDetachedComments(Context context, List<Comment> comments) {
        context.commentFormatter.formatCommentBlockTo(comments, context::writeLine);
    }

    @Override
    public void finishDetachedComment(Context context, List<Comment> comments) {
        context.depth--;
        context.enqueueEmptyLine();
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

        private final DataTableFormatter tableFormatter = new DataTableFormatter();
        private final DocStringFormatter docStringFormatter = new DocStringFormatter();
        private final CommentFormatter commentFormatter = new CommentFormatter();

        private final PrintWriter writer;

        private int depth = -1;
        private int consecutiveNewLinesWritten = 1; // empty document
        private int enqueuedEmptyLines = 0;

        Context(PrintWriter writer) {
            this.writer = writer;
        }

        private void writeLine(String line) {
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
