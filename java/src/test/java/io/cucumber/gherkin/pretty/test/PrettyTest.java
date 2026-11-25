package io.cucumber.gherkin.pretty.test;

import io.cucumber.gherkin.pretty.Pretty;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrettyTest {

    @Test
    void indentationShouldNotBeEmpty(){
        var exception = assertThrows(IllegalArgumentException.class, () -> Pretty.builder().indentation(""));
        assertThat(exception).hasMessage("indentation should not be empty");
    }
}
