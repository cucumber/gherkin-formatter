package io.cucumber.gherkin.pretty;

import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.Comment;
import io.cucumber.messages.types.DataTable;
import io.cucumber.messages.types.DocString;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.FeatureChild;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.RuleChild;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.TableRow;
import io.cucumber.messages.types.Tag;

import java.util.LinkedHashMap;
import java.util.List;

import static io.cucumber.gherkin.pretty.CommentBlock.groupByCommentBlock;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Utility to walk a Gherkin document.
 *
 * <p>The matching methods of the {@link Handler} are called as the walker
 * traverses the document.
 *
 * <p>In addition to the elements of the {@link GherkinDocument} the walker
 * distinguishes attached and detached comment blocks. Attached comment blocks
 * are processed within the scope of the element they are attached to.
 *
 * <p>If the line immediately after a comment block is not empty the comment
 * block is considered attached to that line. Otherwise, it is detached.
 */
class GherkinDocumentWalker {

    void walkGherkinDocument(GherkinDocument gherkinDocument, Handler handler) {
        var remainingComments = groupByCommentBlock(gherkinDocument.getComments());
        handler.startGherkinDocument(gherkinDocument);
        handler.handleGherkinDocument(gherkinDocument);
        gherkinDocument.getFeature().ifPresent(feature -> {
            walkFeature(feature, remainingComments, handler);
        });
        remainingComments.forEach(comment -> walkDetachedComment(comment, handler));
        handler.finishGherkinDocument(gherkinDocument);
    }

    private void walkFeature(Feature feature, List<CommentBlock> remainingComments, Handler handler) {
        walkDetachedCommentsUpTo(remainingComments, GherkinDocumentUtil.startOf(feature), handler);
        handler.startFeature(feature);
        walkFeatureLanguage(feature, handler);
        walkFeatureTags(feature, remainingComments, handler);
        walkCommentsUpto(remainingComments, feature.getLocation(), handler);
        handler.handleFeature(feature);
        walkDescription(feature.getDescription(), handler);
        walkFeatureChildren(feature, remainingComments, handler);
        handler.finishFeature(feature);
    }

    private void walkFeatureLanguage(Feature feature, Handler handler) {
        var language = feature.getLanguage();
        handler.startLanguage(language);
        handler.handleLanguage(language);
        handler.finishLanguage(language);
    }

    private void walkDescription(String description, Handler handler) {
        if (description.isBlank()) {
            return;
        }
        handler.startDescription(description);
        handler.handleDescription(description);
        handler.finishDescription(description);
    }

    private void walkFeatureTags(Feature feature, List<CommentBlock> remainingComments, Handler handler) {
        walkTags(feature.getTags(), remainingComments, handler);
    }

    private void walkFeatureChildren(Feature feature, List<CommentBlock> remainingComments, Handler handler) {
        feature.getChildren().forEach(featureChild -> walkFeatureChild(featureChild, remainingComments, handler));
    }

    private void walkFeatureChild(FeatureChild child, List<CommentBlock> remainingComments, Handler handler) {
        child.getRule().ifPresent(rule -> {
            walkRule(rule, remainingComments, handler);
        });
        child.getBackground().ifPresent(background -> {
            walkBackground(background, remainingComments, handler);
        });
        child.getScenario().ifPresent(scenario -> {
            walkScenario(scenario, remainingComments, handler);
        });
    }

    private void walkRule(Rule rule, List<CommentBlock> remainingComments, Handler handler) {
        walkDetachedCommentsUpTo(remainingComments, GherkinDocumentUtil.startOf(rule), handler);
        handler.startRule(rule);
        walkTagRuleTags(rule, remainingComments, handler);
        walkCommentsUpto(remainingComments, rule.getLocation(), handler);
        handler.handleRule(rule);
        walkDescription(rule.getDescription(), handler);
        walkRuleChildren(rule, remainingComments, handler);
        handler.finishRule(rule);
    }

    private void walkTagRuleTags(Rule rule, List<CommentBlock> remainingComments, Handler handler) {
        walkTags(rule.getTags(), remainingComments, handler);
    }

    private void walkRuleChildren(Rule rule, List<CommentBlock> remainingComments, Handler handler) {
        rule.getChildren().forEach(featureChild -> walkRuleChild(featureChild, remainingComments, handler));
    }

    private void walkRuleChild(RuleChild child, List<CommentBlock> remainingComments, Handler handler) {
        child.getBackground().ifPresent(background -> {
            walkBackground(background, remainingComments, handler);
        });
        child.getScenario().ifPresent(scenario -> {
            walkScenario(scenario, remainingComments, handler);
        });
    }

    private void walkBackground(Background background, List<CommentBlock> remainingComments, Handler handler) {
        walkDetachedCommentsUpTo(remainingComments, GherkinDocumentUtil.startOf(background), handler);
        handler.startBackground(background);
        walkCommentsUpto(remainingComments, background.getLocation(), handler);
        handler.handleBackground(background);
        walkDescription(background.getDescription(), handler);
        walkSteps(background, remainingComments, handler);
        handler.finishBackground(background);
    }

    private void walkScenario(Scenario scenario, List<CommentBlock> remainingComments, Handler handler) {
        walkDetachedCommentsUpTo(remainingComments, GherkinDocumentUtil.startOf(scenario), handler);
        handler.startScenario(scenario);
        walkScenarioTags(scenario, remainingComments, handler);
        walkCommentsUpto(remainingComments, scenario.getLocation(), handler);
        handler.handleScenario(scenario);
        walkDescription(scenario.getDescription(), handler);
        walkSteps(scenario, remainingComments, handler);
        walkExamples(scenario, remainingComments, handler);
        handler.finishScenario(scenario);
    }

    private void walkScenarioTags(Scenario scenario, List<CommentBlock> remainingComments, Handler handler) {
        walkTags(scenario.getTags(), remainingComments, handler);
    }

    private void walkSteps(Background background, List<CommentBlock> remainingComments, Handler handler) {
        walkSteps(background.getSteps(), remainingComments, handler);
    }

    private void walkSteps(Scenario scenario, List<CommentBlock> remainingComments, Handler handler) {
        walkSteps(scenario.getSteps(), remainingComments, handler);
    }

    private void walkSteps(List<Step> steps, List<CommentBlock> remainingComments, Handler handler) {
        steps.forEach(step -> walkStep(step, remainingComments, handler));
    }

    private void walkStep(Step step, List<CommentBlock> remainingComments, Handler handler) {
        walkDetachedCommentsUpTo(remainingComments, GherkinDocumentUtil.startOf(step), handler);
        handler.startStep(step);
        walkCommentsUpto(remainingComments, step.getLocation(), handler);
        handler.handleStep(step);
        walkStepArguments(step, remainingComments, handler);
        handler.finishStep(step);
    }

    private void walkStepArguments(Step step, List<CommentBlock> remainingComments, Handler handler) {
        var dataTable = step.getDataTable();
        var docString = step.getDocString();

        var dataTableLocation = dataTable.map(DataTable::getLocation).map(Location::getLine).orElse(-1);
        var docStringLocation = docString.map(DocString::getLocation).map(Location::getLine).orElse(-1);

        if (dataTableLocation < docStringLocation) {
            dataTable.ifPresent(it -> walkDataTable(it, remainingComments, handler));
            docString.ifPresent(it -> walkDocString(it, remainingComments, handler));
        } else {
            docString.ifPresent(it -> walkDocString(it, remainingComments, handler));
            dataTable.ifPresent(it -> walkDataTable(it, remainingComments, handler));
        }
    }

    private void walkDataTable(DataTable dataTable, List<CommentBlock> remainingComments, Handler handler) {
        walkDetachedCommentsUpTo(remainingComments, GherkinDocumentUtil.startOf(dataTable), handler);
        handler.startDataTable(dataTable);
        walkCommentsUpto(remainingComments, dataTable.getLocation(), handler);
        handler.handleDataTable(dataTable);
        walkDataTableRows(dataTable, remainingComments, handler);
        handler.finishDataTable(dataTable);
    }

    private void walkDataTableRows(DataTable dataTable, List<CommentBlock> remainingComments, Handler handler) {
        dataTable.getRows().forEach(tableRow -> walkDatTableRow(tableRow, remainingComments, handler));
    }

    private void walkDatTableRow(TableRow tableRow, List<CommentBlock> remainingComments, Handler handler) {
        walkCommentsUpto(remainingComments, tableRow.getLocation(), handler);
        handler.startDataTableRow(tableRow);
        handler.handleDataTableRow(tableRow);
        handler.finishDataTableRow(tableRow);
    }

    private void walkDocString(DocString docString, List<CommentBlock> remainingComments, Handler handler) {
        walkDetachedCommentsUpTo(remainingComments, GherkinDocumentUtil.startOf(docString), handler);
        handler.startDocString(docString);
        walkCommentsUpto(remainingComments, docString.getLocation(), handler);
        handler.handleDocString(docString);
        handler.finishDocString(docString);
    }

    private void walkExamples(Scenario scenario, List<CommentBlock> remainingComments, Handler handler) {
        scenario.getExamples().forEach(examples -> walkExamples(examples, remainingComments, handler));
    }

    private void walkExamples(Examples examples, List<CommentBlock> remainingComments, Handler handler) {
        walkDetachedCommentsUpTo(remainingComments, GherkinDocumentUtil.startOf(examples), handler);
        handler.startExamples(examples);
        walkExamplesTags(examples, remainingComments, handler);
        walkCommentsUpto(remainingComments, examples.getLocation(), handler);
        handler.handleExamples(examples);
        walkDescription(examples.getDescription(), handler);
        examples.getTableHeader().ifPresent(headerRow -> {
            walkExampleTableRow(headerRow, remainingComments, handler);
            examples.getTableBody().forEach(bodyRow -> walkExampleTableRow(bodyRow, remainingComments, handler));
        });
        handler.finishExamples(examples);
    }

    private void walkExamplesTags(Examples examples, List<CommentBlock> remainingComments, Handler handler) {
        walkTags(examples.getTags(), remainingComments, handler);
    }

    private void walkExampleTableRow(TableRow tableRow, List<CommentBlock> remainingComments, Handler handler) {
        walkDetachedCommentsUpTo(remainingComments, tableRow.getLocation(), handler);
        handler.startExamplesTableRow(tableRow);
        walkCommentsUpto(remainingComments, tableRow.getLocation(), handler);
        handler.handleExamplesTableRow(tableRow);
        handler.finishExamplesTableRow(tableRow);
    }

    private void walkTags(List<Tag> tags, List<CommentBlock> remainingComments, Handler handler) {
        if (tags.isEmpty()) {
            return;
        }
        handler.startTags(tags);
        var tagsByLocation = tags.stream() //
                .collect(groupingBy(tag -> tag.getLocation().getLine(), LinkedHashMap::new, toList()));
        tagsByLocation.forEach((integer, tagOnSameLine) -> {
            var location = tagOnSameLine.get(0).getLocation();
            walkCommentsUpto(remainingComments, location, handler);
            handler.handleTags(tagOnSameLine);
        });
        handler.finishTags(tags);
    }

    private void walkCommentsUpto(List<CommentBlock> remainingComments, Location upTo, Handler handler) {
        int upToLine = upTo.getLine();
        var iterator = remainingComments.iterator();
        while (iterator.hasNext()) {
            var commentBlock = iterator.next();
            if (commentBlock.start().getLine() < upToLine) {
                iterator.remove();
                walkComment(commentBlock, handler);
            } else {
                return;
            }
        }
    }

    private void walkComment(CommentBlock block, Handler handler) {
        var comments = block.comments();
        handler.startComments(comments);
        handler.handleComments(comments);
        handler.finishComments(comments);
    }

    private void walkDetachedCommentsUpTo(List<CommentBlock> remainingComments, Location upTo, Handler handler) {
        // A comment is detached if it is not above the current line.
        int upToLine = upTo.getLine() - 1;
        var iterator = remainingComments.iterator();
        while (iterator.hasNext()) {
            var commentBlock = iterator.next();
            if (commentBlock.end().getLine() < upToLine) {
                iterator.remove();
                walkDetachedComment(commentBlock, handler);
            } else {
                return;
            }
        }
    }

    private void walkDetachedComment(CommentBlock block, Handler handler) {
        var comments = block.comments();
        handler.startDetachedComments(comments);
        handler.handleDetachedComments(comments);
        handler.finishDetachedComment(comments);
    }

    interface Handler {

        default void startGherkinDocument(GherkinDocument gherkinDocument) {
        }

        default void finishGherkinDocument(GherkinDocument gherkinDocument) {
        }

        default void handleGherkinDocument(GherkinDocument gherkinDocument) {
        }


        default void startFeature(Feature feature) {
        }

        default void handleFeature(Feature feature) {
        }

        default void finishFeature(Feature feature) {
        }

        default void startLanguage(String language) {
        }

        default void handleLanguage(String language) {
        }

        default void finishLanguage(String language) {
        }

        default void startDescription(String description) {
        }

        default void handleDescription(String description) {
        }

        default void finishDescription(String description) {
        }


        default void startRule(Rule rule) {
        }

        default void handleRule(Rule rule) {
        }

        default void finishRule(Rule rule) {
        }


        default void startBackground(Background background) {
        }

        default void handleBackground(Background background) {
        }

        default void finishBackground(Background background) {
        }


        default void startScenario(Scenario scenario) {
        }

        default void handleScenario(Scenario scenario) {
        }

        default void finishScenario(Scenario scenario) {
        }


        default void startStep(Step step) {
        }

        default void handleStep(Step step) {
        }

        default void finishStep(Step step) {
        }


        default void startExamples(Examples examples) {
        }

        default void handleExamples(Examples examples) {
        }

        default void finishExamples(Examples examples) {
        }


        default void startExamplesTableRow(TableRow tableRow) {
        }

        default void handleExamplesTableRow(TableRow tableRow) {
        }

        default void finishExamplesTableRow(TableRow tableRow) {
        }


        default void startDataTable(DataTable dataTable) {
        }

        default void handleDataTable(DataTable dataTable) {
        }

        default void finishDataTable(DataTable dataTable) {
        }


        default void startDataTableRow(TableRow tableRow) {
        }

        default void handleDataTableRow(TableRow tableRow) {
        }

        default void finishDataTableRow(TableRow tableRow) {
        }


        default void startDocString(DocString docString) {
        }

        default void handleDocString(DocString docString) {
        }

        default void finishDocString(DocString docString) {
        }


        default void startTags(List<Tag> tags) {
        }

        default void handleTags(List<Tag> tags) {
        }

        default void finishTags(List<Tag> tags) {
        }


        default void startDetachedComments(List<Comment> comments) {
        }

        default void handleDetachedComments(List<Comment> comments) {

        }

        default void finishDetachedComment(List<Comment> comments) {
        }


        default void startComments(List<Comment> comments) {
        }

        default void handleComments(List<Comment> comments) {
        }

        default void finishComments(List<Comment> comments) {
        }

    }
}
