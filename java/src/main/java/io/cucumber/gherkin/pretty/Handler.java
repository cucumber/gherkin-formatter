package io.cucumber.gherkin.pretty;

import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.Comment;
import io.cucumber.messages.types.DataTable;
import io.cucumber.messages.types.DocString;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.TableRow;
import io.cucumber.messages.types.Tag;

import java.util.List;

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
}
