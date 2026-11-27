module io.cucumber.gherkin.formatter.test {
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;

    requires io.cucumber.gherkin;
    requires io.cucumber.gherkin.formatter;
    requires io.cucumber.messages;
    requires org.assertj.core;
    requires io.cucumber.gherkin.utils;

    opens io.cucumber.gherkin.formatter.test;
}
