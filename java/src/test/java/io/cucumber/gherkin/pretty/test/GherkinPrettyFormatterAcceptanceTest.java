package io.cucumber.gherkin.pretty.test;

import io.cucumber.gherkin.pretty.GherkinPrettyFormatter;
import io.cucumber.gherkin.pretty.GherkinPrettyFormatter.Margin;
import io.cucumber.gherkin.pretty.GherkinPrettyFormatter.TagLayout;
import io.cucumber.gherkin.pretty.GherkinPrettyFormatter.TagOrder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static io.cucumber.gherkin.pretty.test.TestCase.findFeatures;
import static org.assertj.core.api.Assertions.assertThat;


class GherkinPrettyFormatterAcceptanceTest {

    @Nested
    class Configuration {
        static List<TestCase> acceptance() {

            var pretties = new LinkedHashMap<String, GherkinPrettyFormatter>();
            pretties.put("tab-indentation", GherkinPrettyFormatter.builder().indentation("\t").build());
            pretties.put("none-margins", setMargins(GherkinPrettyFormatter.builder(), Margin.NONE).build());
            pretties.put("both-margins", setMargins(GherkinPrettyFormatter.builder(), Margin.BOTH).build());
            pretties.put("swap-alignment", GherkinPrettyFormatter.builder().tableRightAlignedValuesPattern(Pattern.compile("[a-z]+")).build());
            pretties.put("no-alignment", GherkinPrettyFormatter.builder().tableRightAlignedValuesPattern(null).build());
            pretties.put("tags-one-line", GherkinPrettyFormatter.builder().tagStyle(TagLayout.ONE_LINE).build());
            pretties.put("tags-one-per-line", GherkinPrettyFormatter.builder().tagStyle(TagLayout.ONE_PER_LINE).build());
            pretties.put("tags-sorted", GherkinPrettyFormatter.builder().tagOrder(TagOrder.SORTED).build());
            pretties.put("tags-sorted-one-line", GherkinPrettyFormatter.builder().tagOrder(TagOrder.SORTED).tagStyle(TagLayout.ONE_LINE).build());
            pretties.put("tags-sorted-one-per-line", GherkinPrettyFormatter.builder().tagOrder(TagOrder.SORTED).tagStyle(TagLayout.ONE_PER_LINE).build());

            var paths = List.of(
                    Path.of("../testdata/compatibility-kit/src/minimal.feature"),
                    Path.of("../testdata/compatibility-kit/src/examples-tables.feature"),
                    Path.of("../testdata/configuration/src/tags.feature")
            );

            var expectedDirectory = Path.of("../testdata/configuration/expected");
            var testCases = new ArrayList<TestCase>();
            pretties.forEach((name, pretty) -> {
                paths.forEach(path -> {
                    testCases.add(new TestCase(name, path, expectedDirectory, pretty::formatTo));
                });
            });

            return testCases;
        }

        private static GherkinPrettyFormatter.Builder setMargins(GherkinPrettyFormatter.Builder builder, Margin none) {
            for (GherkinPrettyFormatter.Element value : GherkinPrettyFormatter.Element.values()) {
                builder.margins(value, none);
            }
            return builder;
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        @DisabledIfEnvironmentVariable(named = "UPDATE_EXPECTED_FILES", matches = "true")
        void test(TestCase testCase) throws Exception {
            var formatted = testCase.pretty(testCase.actual());
            assertThat(formatted).isEqualToIgnoringNewLines(testCase.expected());
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        @EnabledIfEnvironmentVariable(named = "UPDATE_EXPECTED_FILES", matches = "true")
        void updateExpectedFiles(TestCase testCase) throws Exception {
            try (OutputStream out = Files.newOutputStream(testCase.expectedPath())) {
                testCase.prettyTo(testCase.actual(), out);
                Files.copy(testCase.expectedPath(), System.out);
            }
        }
    }


    @Nested
    class GherkinGherkinPrettyFormatter {

        static List<TestCase> acceptance() throws IOException {
            var actualDirectory = Path.of("../testdata/gherkin-pretty/src");
            var expectedDirectory = Path.of("../testdata/gherkin-pretty/expected");
            var pretty = GherkinPrettyFormatter.builder().build();
            return findFeatures(actualDirectory)
                    .stream()
                    .map(actual -> new TestCase(actual, expectedDirectory, pretty::formatTo))
                    .toList();
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        @DisabledIfEnvironmentVariable(named = "UPDATE_EXPECTED_FILES", matches = "true")
        void test(TestCase testCase) throws Exception {
            var formatted = testCase.pretty(testCase.actual());
            assertThat(formatted).isEqualToIgnoringNewLines(testCase.expected());
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        @EnabledIfEnvironmentVariable(named = "UPDATE_EXPECTED_FILES", matches = "true")
        void updateExpectedFiles(TestCase testCase) throws Exception {
            try (OutputStream out = Files.newOutputStream(testCase.expectedPath())) {
                testCase.prettyTo(testCase.actual(), out);
                Files.copy(testCase.expectedPath(), System.out);
            }
        }
    }

    @Nested
    class GherkinUtils {

        static List<TestCase> acceptance() throws IOException {
            var actualDirectory = Path.of("../testdata/gherkin-utils/src");
            var expectedDirectory = Path.of("../testdata/gherkin-utils/expected");
            var pretty = GherkinPrettyFormatter.builder().build();
            return findFeatures(actualDirectory)
                    .stream()
                    .map(actual -> new TestCase(actual, expectedDirectory, pretty::formatTo))
                    .toList();
        }

        static Set<String> unsupported = Set.of("commentsBeforeAndAfterKeywords");

        @ParameterizedTest
        @MethodSource("acceptance")
        @DisabledIfEnvironmentVariable(named = "UPDATE_EXPECTED_FILES", matches = "true")
        void test(TestCase testCase) throws Exception {
            if (unsupported.contains(testCase.name())) {
                return;
            }
            var formatted = testCase.pretty(testCase.actual());
            assertThat(formatted).isEqualToIgnoringNewLines(testCase.expected());
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        @EnabledIfEnvironmentVariable(named = "UPDATE_EXPECTED_FILES", matches = "true")
        void updateExpectedFiles(TestCase testCase) throws Exception {
            if (unsupported.contains(testCase.name())) {
                return;
            }
            try (OutputStream out = Files.newOutputStream(testCase.expectedPath())) {
                testCase.prettyTo(testCase.actual(), out);
                Files.copy(testCase.expectedPath(), System.out);
            }
        }
    }

    @Nested
    class CompatibilityKit {

        static List<TestCase> acceptance() throws IOException {
            // Tests a round trip
            var expectedAndActual = Path.of("../testdata/compatibility-kit/src");
            var pretty = GherkinPrettyFormatter.builder().build();
            return findFeatures(expectedAndActual)
                    .stream()
                    .map(actual -> new TestCase(actual, expectedAndActual, pretty::formatTo))
                    .toList();
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        void test(TestCase testCase) throws Exception {
            var formatted = testCase.pretty(testCase.actual());
            assertThat(formatted).isEqualToIgnoringNewLines(testCase.expected());
        }
    }
}
