package io.cucumber.gherkin.formatter;

import io.cucumber.gherkin.formatter.GherkinDocumentFormatter.Element;
import io.cucumber.gherkin.formatter.GherkinDocumentFormatter.Margin;
import io.cucumber.gherkin.formatter.GherkinDocumentFormatter.TagLayout;
import io.cucumber.gherkin.formatter.GherkinDocumentFormatter.TagOrder;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.regex.Pattern;

import static io.cucumber.gherkin.formatter.GherkinDocumentFormatter.Margin.BEFORE;
import static io.cucumber.gherkin.formatter.GherkinDocumentFormatter.Margin.BOTH;

record Configuration(
        String indentation,
        Map<Element, Margin> margin,
        @Nullable Pattern tableRightAlignedValuesPattern,
        TagLayout tagLayout,
        TagOrder tagOrder
) {

    static final String DEFAULT_INDENTATION = "  ";

    static final Map<Element, Margin> DEFAULT_MARGINS = Map.of(
            Element.FEATURE, BEFORE,
            Element.DESCRIPTION, BOTH,
            Element.RULE, BOTH,
            Element.BACKGROUND, BEFORE,
            Element.SCENARIO, BEFORE,
            Element.EXAMPLES, BEFORE,
            Element.DETACHED_COMMENTS, BOTH
    );

    /**
     * Matches numbers, IPv4 addresses and hopefully not much else.
     */
    static final Pattern DEFAULT_TABLE_RIGHT_ALIGNED_VALUES_PATTERN = Pattern.compile(
            // Sign
            "[-+]?" +
                    // Significant
                    "(?:\\d+(?:[,.]\\d+)*(?:[,.]\\d+)?|[,.]\\d+)" +
                    // Exponent
                    "(?:E[-+]?\\d+)?");

    static final TagLayout DEFAULT_TAG_STYLE = TagLayout.AS_IS;
    static final TagOrder DEFAULT_TAG_ORDER = TagOrder.USER_DEFINED_ORDER;

}
