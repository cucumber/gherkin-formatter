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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

class GherkinDocumentWalker {

    interface Handler {

        default void start(int level, Feature feature) {
        }

        default void start(int level, Description description) {
        }

        default void start(int level, Rule rule) {
        }

        default void start(int level, Background background) {
        }

        default void start(int level, Scenario scenario) {
        }

        default void start(int level, Examples scenario) {
        }

        default void finish(int level, Feature feature) {
        }

        default void finish(int level, Description description) {
        }

        default void finish(int level, Rule rule) {
        }

        default void finish(int level, Background background) {
        }

        default void finish(int level, Scenario scenario) {
        }

        default void finish(int level, Examples scenario) {
        }

        default void handle(int level, Feature rule) {
        }

        default void handle(int level, Description description) {
        }

        default void handle(int level, Rule rule) {
        }

        default void handle(int level, Background background) {
        }

        default void handle(int level, Scenario scenario) {
        }

        default void handle(int level, Step step) {
        }

        default void handle(int level, DataTable dataTable) {
        }

        default void handle(int level, DocString docString) {
        }

        default void handle(int level, Examples examples) {
        }

        default void handle(int level, TableRow tableRow) {
        }

        default void handle(int level, TagLine tagLine) {
        }


        default void handle(int level, Comment comment) {
        }

    }

    /**
     * Synthetic representation of a group of tags.
     *
     * <p>Tags are not logically grouped in a GherkinDocument
     * but in practice act like a logical group.
     */
    record TagLine(Location location, List<Tag> tags) {

    }

    /**
     * Synthetic representation of a description.
     */
    record Description(String body) {

    }

    public void walkGherkinDocument(GherkinDocument gherkinDocument, Handler handler) {
        int level = 0;
        var remainingComments = new ArrayList<>(gherkinDocument.getComments());
        gherkinDocument.getFeature().ifPresent(feature -> {
            walkFeature(level, feature, remainingComments, handler);
        });
        remainingComments.forEach(comment -> walkComment(level, comment, handler));
    }

    private void walkFeature(int level, Feature feature, List<Comment> remainingComments, Handler handler) {
        handler.start(level, feature);
        walkFeatureTags(level, feature, remainingComments, handler);
        walkCommentsUpto(level, remainingComments, feature.getLocation(), handler);
        handler.handle(level, feature);
        walkDescription(level, feature.getDescription(), handler);
        walkFeatureChildren(level, feature, remainingComments, handler);
        handler.finish(level, feature);
    }

    private void walkDescription(int level, String description, Handler handler) {
        if (description.isBlank()) {
            return;
        }

        var wrapper = new Description(description);
        handler.start(level + 1, wrapper);
        handler.handle(level + 1, wrapper);
        handler.finish(level, wrapper);
    }

    private void walkFeatureTags(int level, Feature feature, List<Comment> remainingComments, Handler handler) {
        walkTags(level, feature.getTags(), remainingComments, handler);
    }

    private void walkFeatureChildren(int level, Feature feature, List<Comment> remainingComments, Handler handler) {
        feature.getChildren().forEach(featureChild -> walkFeatureChild(level + 1, featureChild, remainingComments, handler));
    }

    private void walkFeatureChild(int level, FeatureChild child, List<Comment> remainingComments, Handler handler) {
        child.getRule().ifPresent(rule -> {
            handler.start(level, rule);
            walkTagRuleTags(level, rule, remainingComments, handler);
            walkCommentsUpto(level, remainingComments, rule.getLocation(), handler);
            handler.handle(level, rule);
            walkDescription(level, rule.getDescription(), handler);
            walkRuleChildren(level, rule, remainingComments, handler);
            handler.finish(level, rule);
        });

        child.getBackground().ifPresent(background -> {
            handler.start(level, background);
            walkBackground(level, background, remainingComments, handler);
            handler.finish(level, background);
        });

        child.getScenario().ifPresent(scenario -> {
            handler.start(level, scenario);
            walkScenario(level, scenario, remainingComments, handler);
            handler.finish(level, scenario);
        });
    }

    private void walkTagRuleTags(int level, Rule rule, List<Comment> remainingComments, Handler handler) {
        walkTags(level, rule.getTags(), remainingComments, handler);
    }

    private void walkRuleChildren(int level, Rule rule, List<Comment> remainingComments, Handler handler) {
        rule.getChildren().forEach(featureChild -> walkRuleChild(level + 1, featureChild, remainingComments, handler));
    }

    private void walkRuleChild(int level, RuleChild child, List<Comment> remainingComments, Handler handler) {
        child.getBackground().ifPresent(background -> {
            walkBackground(level, background, remainingComments, handler);
        });

        child.getScenario().ifPresent(scenario -> {
            walkScenario(level, scenario, remainingComments, handler);
        });
    }

    private void walkBackground(int level, Background background, List<Comment> remainingComments, Handler handler) {
        handler.start(level, background);
        walkCommentsUpto(level, remainingComments, background.getLocation(), handler);
        handler.handle(level, background);
        walkDescription(level, background.getDescription(), handler);
        walkSteps(level, background, remainingComments, handler);
        handler.finish(level, background);
    }

    private void walkScenario(int level, Scenario scenario, List<Comment> remainingComments, Handler handler) {
        handler.start(level, scenario);
        walkScenarioTags(level, scenario, remainingComments, handler);
        walkCommentsUpto(level, remainingComments, scenario.getLocation(), handler);
        handler.handle(level, scenario);
        walkDescription(level, scenario.getDescription(), handler);
        walkSteps(level, scenario, remainingComments, handler);
        walkExamples(level, scenario, remainingComments, handler);
        handler.finish(level, scenario);
    }

    private void walkScenarioTags(int level, Scenario scenario, List<Comment> remainingComments, Handler handler) {
        walkTags(level, scenario.getTags(), remainingComments, handler);
    }

    private void walkSteps(int level, Background background, List<Comment> remainingComments, Handler handler) {
        background.getSteps().forEach(step -> walkStep(level + 1, step, remainingComments, handler));
    }

    private void walkSteps(int level, Scenario scenario, List<Comment> remainingComments, Handler handler) {
        scenario.getSteps().forEach(step -> walkStep(level + 1, step, remainingComments, handler));
    }

    private void walkStep(int level, Step step, List<Comment> remainingComments, Handler handler) {
        walkCommentsUpto(level, remainingComments, step.getLocation(), handler);
        handler.handle(level, step);
        walkStepArguments(level, step, remainingComments, handler);
    }

    private void walkStepArguments(int level, Step step, List<Comment> remainingComments, Handler handler) {
        var dataTable = step.getDataTable();
        var docString = step.getDocString();

        var dataTableLocation = dataTable.map(DataTable::getLocation).map(Location::getLine).orElse(-1);
        var docStringLocation = docString.map(DocString::getLocation).map(Location::getLine).orElse(-1);

        if (dataTableLocation < docStringLocation) {
            dataTable.ifPresent(it -> walkDataTable(level + 1, it, remainingComments, handler));
            docString.ifPresent(it -> walkDocString(level + 1, it, remainingComments, handler));
        } else {
            docString.ifPresent(it -> walkDocString(level + 1, it, remainingComments, handler));
            dataTable.ifPresent(it -> walkDataTable(level + 1, it, remainingComments, handler));
        }
    }

    private void walkDataTable(int level, DataTable dataTable, List<Comment> remainingComments, Handler handler) {
        walkCommentsUpto(level, remainingComments, dataTable.getLocation(), handler);
        handler.handle(level, dataTable);
        walkDataTableRows(level, dataTable, remainingComments, handler);
    }

    private void walkDataTableRows(int level, DataTable dataTable, List<Comment> remainingComments, Handler handler) {
        dataTable.getRows().forEach(tableRow -> walkDatTableRow(level, tableRow, remainingComments, handler));
    }

    private void walkDatTableRow(int level, TableRow tableRow, List<Comment> remainingComments, Handler handler) {
        walkCommentsUpto(level, remainingComments, tableRow.getLocation(), handler);
        handler.handle(level, tableRow);
    }

    private void walkDocString(int level, DocString docString, List<Comment> remainingComments, Handler handler) {
        walkCommentsUpto(level, remainingComments, docString.getLocation(), handler);
        handler.handle(level, docString);
    }

    private void walkExamples(int level, Scenario scenario, List<Comment> remainingComments, Handler handler) {
        scenario.getExamples().forEach(examples -> walkExamples(level + 1, examples, remainingComments, handler));
    }

    private void walkExamples(int level, Examples examples, List<Comment> remainingComments, Handler handler) {
        handler.start(level, examples);
        walkExamplesTags(level, examples, remainingComments, handler);
        walkCommentsUpto(level, remainingComments, examples.getLocation(), handler);
        handler.handle(level, examples);
        walkDescription(level, examples.getDescription(), handler);
        examples.getTableHeader().ifPresent(tableRow -> walkExampleTableRow(level + 1, tableRow, remainingComments, handler));
        examples.getTableBody().forEach(tableRow -> walkExampleTableRow(level + 1, tableRow, remainingComments, handler));
        handler.finish(level, examples);
    }

    private void walkExamplesTags(int level, Examples examples, List<Comment> remainingComments, Handler handler) {
        walkTags(level, examples.getTags(), remainingComments, handler);
    }

    private void walkExampleTableRow(int level, TableRow tableRow, List<Comment> remainingComments, Handler handler) {
        walkCommentsUpto(level, remainingComments, tableRow.getLocation(), handler);
        handler.handle(level, tableRow);
    }

    private void walkTags(int level, List<Tag> tags, List<Comment> remainingComments, Handler handler) {
        if (tags.isEmpty()) {
            return;
        }
        var tagsByLocation = tags.stream() //
                .collect(groupingBy(tag -> tag.getLocation().getLine(), LinkedHashMap::new, toList()));
        tagsByLocation.forEach((integer, tagOnSameLine) -> {
            var location = tagOnSameLine.get(0).getLocation();
            walkCommentsUpto(level, remainingComments, location, handler);
            handler.handle(level, new TagLine(location, tagOnSameLine));
        });
    }

    private void walkCommentsUpto(int level, List<Comment> remainingComments, Location currentLocation, Handler handler) {
        int currentLine = currentLocation.getLine();
        var iterator = remainingComments.iterator();
        while (iterator.hasNext()) {
            var comment = iterator.next();
            if (comment.getLocation().getLine() < currentLine) {
                iterator.remove();
                walkComment(level, comment, handler);
            } else {
                return;
            }
        }
    }

    private void walkComment(int level, Comment comment, Handler handler) {
        handler.handle(level, comment);
    }


}
