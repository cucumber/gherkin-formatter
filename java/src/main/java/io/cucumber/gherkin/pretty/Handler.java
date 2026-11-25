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

    default void handleTags(List<Tag> tagLine) {
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
