package io.cucumber.gherkin.pretty.test;

import io.cucumber.gherkin.pretty.GherkinPrettyFormatter;
import io.cucumber.messages.types.GherkinDocument;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GherkinPrettyFormatterTest {

    @Test
    void formatsEmptyDocument(){
        var pretty = GherkinPrettyFormatter.builder().build();
        GherkinDocument document = new GherkinDocument(
                "path/to/example.feature",
                null,
                Collections.emptyList()
        );
        var formatted = pretty.format(document);
        assertThat(formatted).isEmpty();
    }

    @Test
    void indentationShouldNotBeEmpty() {
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> GherkinPrettyFormatter.builder().indentation("")
        );
        assertThat(exception).hasMessage("indentation should not be empty");
    }
}
