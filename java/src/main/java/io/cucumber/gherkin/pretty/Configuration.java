package io.cucumber.gherkin.pretty;

import java.util.Map;

record Configuration(
        String indentation,
        Map<Element, Margin> margin
) {

    static final String DEFAULT_INDENTATION = "  ";
    static final Map<Element, Margin> DEFAULT_MARGINS = Map.of(
            Element.FEATURE, Margin.BEFORE,
            Element.DESCRIPTION, Margin.BOTH,
            Element.RULE, Margin.BOTH,
            Element.BACKGROUND, Margin.BEFORE,
            Element.SCENARIO, Margin.BEFORE,
            Element.EXAMPLES, Margin.BEFORE,
            Element.DETACHED_COMMENTS, Margin.BOTH
    );

}
