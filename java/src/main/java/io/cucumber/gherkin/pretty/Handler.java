package io.cucumber.gherkin.pretty;

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

import java.util.List;

interface Handler {

    default void startFeature(int level, Feature feature) {
    }

    default void startDescription(int level, String description) {
    }

    default void startRule(int level, Rule rule) {
    }

    default void startBackground(int level, Background background) {
    }

    default void startScenario(int level, Scenario scenario) {
    }

    default void startExamples(int level, Examples examples) {
    }

    default void startDataTable(int level, DataTable dataTable) {
    }

    default void startTags(int level, List<Tag> tags){
    }

    default void finishFeature(int level, Feature feature) {
    }

    default void finishDescription(int level, String description) {
    }

    default void finishRule(int level, Rule rule) {
    }

    default void finishBackground(int level, Background background) {
    }

    default void finishScenario(int level, Scenario scenario) {
    }

    default void finishExamples(int level, Examples examples) {
    }

    default void finishDataTable(int level, DataTable dataTable) {
    }

    default void finishTags(int level, List<Tag> tags){
    }

    default void handleFeature(int level, Feature rule) {
    }

    default void handleDescription(int level, String description) {
    }

    default void handleRule(int level, Rule rule) {
    }

    default void handleBackground(int level, Background background) {
    }

    default void handleScenario(int level, Scenario scenario) {
    }

    default void handleStep(int level, Step step) {
    }

    default void handleDataTable(int level, DataTable dataTable) {
    }

    default void handleDocString(int level, DocString docString) {
    }

    default void handleExamples(int level, Examples examples) {
    }

    default void handleTableRow(int level, TableRow tableRow) {
    }

    default void handleTagLine(int level, List<Tag> tagLine) {
    }

    default void handleComment(int level, Comment comment) {
    }

}
