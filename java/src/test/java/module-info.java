module io.cucumber.gherkin.pretty.test {
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;

    requires io.cucumber.gherkin;
    requires io.cucumber.gherkin.pretty;
    requires io.cucumber.messages;
    requires io.cucumber.messages.ndjson;

    opens io.cucumber.gherkin.pretty.test;
}
