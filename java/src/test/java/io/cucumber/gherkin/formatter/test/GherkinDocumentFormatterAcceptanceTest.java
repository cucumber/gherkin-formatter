package io.cucumber.gherkin.formatter.test;

import io.cucumber.gherkin.formatter.GherkinDocumentFormatter;
import io.cucumber.gherkin.formatter.GherkinDocumentFormatter.Margin;
import io.cucumber.gherkin.formatter.GherkinDocumentFormatter.TagLayout;
import io.cucumber.gherkin.formatter.GherkinDocumentFormatter.TagOrder;
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

import static io.cucumber.gherkin.formatter.test.TestCase.findFeatures;
import static org.assertj.core.api.Assertions.assertThat;


class GherkinDocumentFormatterAcceptanceTest {

    @Nested
    class Configuration {
        static List<TestCase> acceptance() {

            var pretties = new LinkedHashMap<String, GherkinDocumentFormatter>();
            pretties.put("tab-indentation", GherkinDocumentFormatter.builder().indentation("\t").build());
            pretties.put("none-margins", setMargins(GherkinDocumentFormatter.builder(), Margin.NONE).build());
            pretties.put("both-margins", setMargins(GherkinDocumentFormatter.builder(), Margin.BOTH).build());
            pretties.put("swap-alignment", GherkinDocumentFormatter.builder().tableRightAlignedValuesPattern(Pattern.compile("[a-z]+")).build());
            pretties.put("no-alignment", GherkinDocumentFormatter.builder().tableRightAlignedValuesPattern(null).build());
            pretties.put("tags-one-line", GherkinDocumentFormatter.builder().tagStyle(TagLayout.ONE_LINE).build());
            pretties.put("tags-one-per-line", GherkinDocumentFormatter.builder().tagStyle(TagLayout.ONE_PER_LINE).build());
            pretties.put("tags-sorted", GherkinDocumentFormatter.builder().tagOrder(TagOrder.SORTED).build());
            pretties.put("tags-sorted-one-line", GherkinDocumentFormatter.builder().tagOrder(TagOrder.SORTED).tagStyle(TagLayout.ONE_LINE).build());
            pretties.put("tags-sorted-one-per-line", GherkinDocumentFormatter.builder().tagOrder(TagOrder.SORTED).tagStyle(TagLayout.ONE_PER_LINE).build());

            var paths = List.of(
                    Path.of("../testdata/compatibility-kit/src/minimal.feature"),
                    Path.of("../testdata/compatibility-kit/src/examples-tables.feature"),
                    Path.of("../testdata/configuration/src/tags.feature")
            );

            var expectedDirectory = Path.of("../testdata/configuration/expected");
            var testCases = new ArrayList<TestCase>();
            pretties.forEach((name, formatter) -> {
                paths.forEach(path -> {
                    testCases.add(new TestCase(name, path, expectedDirectory, formatter::formatTo));
                });
            });

            return testCases;
        }

        private static GherkinDocumentFormatter.Builder setMargins(GherkinDocumentFormatter.Builder builder, Margin none) {
            for (GherkinDocumentFormatter.Element element : GherkinDocumentFormatter.Element.values()) {
                builder.margins(element, none);
            }
            return builder;
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        @DisabledIfEnvironmentVariable(named = "UPDATE_EXPECTED_FILES", matches = "true")
        void test(TestCase testCase) throws Exception {
            var formatted = testCase.format(testCase.actual());
            assertThat(formatted).isEqualToIgnoringNewLines(testCase.expected());
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        @EnabledIfEnvironmentVariable(named = "UPDATE_EXPECTED_FILES", matches = "true")
        void updateExpectedFiles(TestCase testCase) throws Exception {
            try (OutputStream out = Files.newOutputStream(testCase.expectedPath())) {
                testCase.formatTo(testCase.actual(), out);
                Files.copy(testCase.expectedPath(), System.out);
            }
        }
    }


    @Nested
    class GherkinGherkinDocumentFormatter {

        static List<TestCase> acceptance() throws IOException {
            var actualDirectory = Path.of("../testdata/gherkin-formatter/src");
            var expectedDirectory = Path.of("../testdata/gherkin-formatter/expected");
            var formatter = GherkinDocumentFormatter.builder().build();
            return findFeatures(actualDirectory)
                    .stream()
                    .map(actual -> new TestCase(actual, expectedDirectory, formatter::formatTo))
                    .toList();
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        @DisabledIfEnvironmentVariable(named = "UPDATE_EXPECTED_FILES", matches = "true")
        void test(TestCase testCase) throws Exception {
            var formatted = testCase.format(testCase.actual());
            assertThat(formatted).isEqualToIgnoringNewLines(testCase.expected());
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        @EnabledIfEnvironmentVariable(named = "UPDATE_EXPECTED_FILES", matches = "true")
        void updateExpectedFiles(TestCase testCase) throws Exception {
            try (OutputStream out = Files.newOutputStream(testCase.expectedPath())) {
                testCase.formatTo(testCase.actual(), out);
                Files.copy(testCase.expectedPath(), System.out);
            }
        }
    }

    @Nested
    class GherkinUtils {

        static List<TestCase> acceptance() throws IOException {
            var actualDirectory = Path.of("../testdata/gherkin-utils/src");
            var expectedDirectory = Path.of("../testdata/gherkin-utils/expected");
            var formatter = GherkinDocumentFormatter.builder().build();
            return findFeatures(actualDirectory)
                    .stream()
                    .map(actual -> new TestCase(actual, expectedDirectory, formatter::formatTo))
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
            var formatted = testCase.format(testCase.actual());
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
                testCase.formatTo(testCase.actual(), out);
                Files.copy(testCase.expectedPath(), System.out);
            }
        }
    }

    @Nested
    class CompatibilityKit {

        static List<TestCase> acceptance() throws IOException {
            // Tests a round trip
            var expectedAndActual = Path.of("../testdata/compatibility-kit/src");
            var formatter = GherkinDocumentFormatter.builder().build();
            return findFeatures(expectedAndActual)
                    .stream()
                    .map(actual -> new TestCase(actual, expectedAndActual, formatter::formatTo))
                    .toList();
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        void test(TestCase testCase) throws Exception {
            var formatted = testCase.format(testCase.actual());
            assertThat(formatted).isEqualToIgnoringNewLines(testCase.expected());
        }
    }
}
