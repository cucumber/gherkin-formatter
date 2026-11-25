package io.cucumber.gherkin.pretty;

import io.cucumber.messages.types.DocString;

import java.util.function.Consumer;

final class DocStringFormatter {
    private static final String DOCSTRING_SEPARATOR = "\"\"\"";
    private static final String ESCAPED_DOCSTRING_SEPARATOR = "\\\\\"\\\\\"\\\\\"";
    private static final String DOCSTRING_ALTERNATIVE_SEPARATOR = "```";
    private static final String ESCAPED_DOCSTRING_ALTERNATIVE_SEPARATOR = "\\\\`\\\\`\\\\`";

    void formatTo(DocString docString, Consumer<String> lineBuilder) {
        lineBuilder.accept(formatDocStringCommentOpen(docString));
        docString.getContent()
                .lines()
                .map(docStringLine -> formatDocStringLine(docString, docStringLine))
                .forEach(lineBuilder);
        lineBuilder.accept(formatDocStringCommentClose(docString));
    }

    private String formatDocStringLine(DocString docString, String docStringLine) {
        if (docString.getDelimiter().equals(DOCSTRING_SEPARATOR)) {
            return docStringLine.replaceAll(DOCSTRING_SEPARATOR, ESCAPED_DOCSTRING_SEPARATOR);
        } else {
            return docStringLine.replaceAll(DOCSTRING_ALTERNATIVE_SEPARATOR, ESCAPED_DOCSTRING_ALTERNATIVE_SEPARATOR);
        }
    }

    private String formatDocStringCommentOpen(DocString docString) {
        return "%s%s".formatted(docString.getDelimiter(), docString.getMediaType().orElse(""));
    }

    private String formatDocStringCommentClose(DocString docString) {
        return docString.getDelimiter();
    }
}
