package io.cucumber.gherkin.pretty;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PrettyTest {

    @Test
    void test(){
        Assertions.assertDoesNotThrow(Pretty::new);
    }
}
