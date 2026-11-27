package io.cucumber.gherkin.pretty.test;

import io.cucumber.gherkin.pretty.GherkinDocumentFormatter;
import io.cucumber.messages.types.GherkinDocument;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GherkinDocumentFormatterTest {

    @Test
    void formatsEmptyDocument(){
        var pretty = GherkinDocumentFormatter.builder().build();
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
                () -> GherkinDocumentFormatter.builder().indentation("")
        );
        assertThat(exception).hasMessage("indentation should not be empty");
    }
}
