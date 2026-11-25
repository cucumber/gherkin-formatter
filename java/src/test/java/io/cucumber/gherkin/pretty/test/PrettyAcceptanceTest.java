package io.cucumber.gherkin.pretty.test;

import io.cucumber.gherkin.pretty.Pretty;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.assertj.core.api.Assertions.assertThat;


class PrettyAcceptanceTest {

    private static List<Path> findFeatures(Path dir) throws IOException {
        List<Path> result;
        try (Stream<Path> paths = Files.list(dir)) {
            result = paths
                    .filter(path -> path.getFileName().toString().endsWith(".feature"))
                    .toList();
        }
        return result;
    }

    @Nested
    class Configuration {
        static List<TestCase> acceptance() {

            var pretties = new LinkedHashMap<String, Pretty>();
            pretties.put("tab-indentation", Pretty.builder().indentation("\t").build());


            var paths = List.of(
                    Path.of("../testdata/compatibility-kit/src/minimal.feature")
            );

            var expectedDirectory = Path.of("../testdata/configuration/expected");
            var testCases = new ArrayList<TestCase>();
            pretties.forEach((name, pretty) -> {
                paths.forEach(path -> {
                    testCases.add(new TestCase(name, path, expectedDirectory, pretty));
                });
            });

            return testCases;
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        @DisabledIfEnvironmentVariable(named = "UPDATE_EXPECTED_FILES", matches = "true")
        void test(TestCase testCase) throws Exception {
            var formatted = testCase.pretty().format(testCase.actual());
            assertThat(formatted).isEqualToIgnoringNewLines(testCase.expected());
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        @EnabledIfEnvironmentVariable(named = "UPDATE_EXPECTED_FILES", matches = "true")
        void update(TestCase testCase) throws Exception {
            var formatted = testCase.pretty().format(testCase.actual());
            Files.writeString(testCase.expectedPath(), formatted, TRUNCATE_EXISTING);
        }
    }


    @Nested
    class GherkinPretty {

        static List<TestCase> acceptance() throws IOException {
            var actualDirectory = Path.of("../testdata/gherkin-pretty/src");
            var expectedDirectory = Path.of("../testdata/gherkin-pretty/expected");
            var pretty = Pretty.builder().build();
            return findFeatures(actualDirectory)
                    .stream()
                    .map(actual -> new TestCase(actual, expectedDirectory, pretty))
                    .toList();
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        @DisabledIfEnvironmentVariable(named = "UPDATE_EXPECTED_FILES", matches = "true")
        void test(TestCase testCase) throws Exception {
            var formatted = testCase.pretty().format(testCase.actual());
            assertThat(formatted).isEqualToIgnoringNewLines(testCase.expected());
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        @EnabledIfEnvironmentVariable(named = "UPDATE_EXPECTED_FILES", matches = "true")
        void update(TestCase testCase) throws Exception {
            var formatted = testCase.pretty().format(testCase.actual());
            Files.writeString(testCase.expectedPath(), formatted, TRUNCATE_EXISTING);
        }
    }

    @Nested
    class GherkinUtils {

        static List<TestCase> acceptance() throws IOException {
            var actualDirectory = Path.of("../testdata/gherkin-utils/src");
            var expectedDirectory = Path.of("../testdata/gherkin-utils/expected");
            var pretty = Pretty.builder().build();
            return findFeatures(actualDirectory)
                    .stream()
                    .map(actual -> new TestCase(actual, expectedDirectory, pretty))
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
            var formatted = testCase.pretty().format(testCase.actual());
            assertThat(formatted).isEqualToIgnoringNewLines(testCase.expected());
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        @EnabledIfEnvironmentVariable(named = "UPDATE_EXPECTED_FILES", matches = "true")
        void update(TestCase testCase) throws Exception {
            if (unsupported.contains(testCase.name())) {
                return;
            }
            var formatted = testCase.pretty().format(testCase.actual());
            Files.writeString(testCase.expectedPath(), formatted, TRUNCATE_EXISTING);
        }
    }

    @Nested
    class CompatibilityKit {

        static List<TestCase> acceptance() throws IOException {
            // Tests a round trip
            var expectedAndActual = Path.of("../testdata/compatibility-kit/src");
            var pretty = Pretty.builder().build();
            return findFeatures(expectedAndActual)
                    .stream()
                    .map(actual -> new TestCase(actual, expectedAndActual, pretty))
                    .toList();
        }

        @ParameterizedTest
        @MethodSource("acceptance")
        void test(TestCase testCase) throws Exception {
            var formatted = testCase.pretty().format(testCase.actual());
            assertThat(formatted).isEqualToIgnoringNewLines(testCase.expected());
        }
    }
}
