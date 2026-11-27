package io.cucumber.gherkin.formatter;

import io.cucumber.messages.LocationComparator;
import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.DataTable;
import io.cucumber.messages.types.DocString;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.Tag;

import java.util.stream.Stream;

final class GherkinDocumentUtil {

    static Location startOf(Feature element) {
        var location = Stream.of(element.getLocation());
        var tagLocations = element.getTags().stream().map(Tag::getLocation);
        return Stream.concat(tagLocations, location).min(new LocationComparator()).orElseThrow();
    }

    static Location startOf(Rule element) {
        var location = Stream.of(element.getLocation());
        var tagLocations = element.getTags().stream().map(Tag::getLocation);
        return Stream.concat(tagLocations, location).min(new LocationComparator()).orElseThrow();
    }

    static Location startOf(Background element) {
        return element.getLocation();
    }

    static Location startOf(Scenario element) {
        var location = Stream.of(element.getLocation());
        var tagLocations = element.getTags().stream().map(Tag::getLocation);
        return Stream.concat(tagLocations, location).min(new LocationComparator()).orElseThrow();
    }

    static Location startOf(Examples element) {
        var location = Stream.of(element.getLocation());
        var tagLocations = element.getTags().stream().map(Tag::getLocation);
        return Stream.concat(tagLocations, location).min(new LocationComparator()).orElseThrow();
    }

    static Location startOf(Step element) {
        return element.getLocation();
    }

    static Location startOf(DataTable element) {
        return element.getLocation();
    }

    static Location startOf(DocString element) {
        return element.getLocation();
    }

    private GherkinDocumentUtil() {
        /* no-op */
    }
}
