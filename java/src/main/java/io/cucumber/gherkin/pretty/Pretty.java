package io.cucumber.gherkin.pretty;

import io.cucumber.messages.types.GherkinDocument;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

import static io.cucumber.gherkin.pretty.Configuration.DEFAULT_INDENTATION;
import static io.cucumber.gherkin.pretty.Configuration.DEFAULT_MARGINS;
import static io.cucumber.gherkin.pretty.Configuration.DEFAULT_TABLE_RIGHT_ALIGNED_VALUES_PATTERN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

/**
 * Pretty format Gherkin documents.
 */
public final class Pretty {

    private final Configuration configuration;

    private Pretty(Configuration configuration) {
        this.configuration = configuration;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String format(GherkinDocument gherkinDocument) {
        requireNonNull(gherkinDocument);
        var out = new ByteArrayOutputStream();
        formatTo(gherkinDocument, out);
        return out.toString(UTF_8);
    }

    public void formatTo(GherkinDocument gherkinDocument, OutputStream out) {
        requireNonNull(gherkinDocument);
        requireNonNull(out);

        var handler = new PrettyHandler(configuration);
        var documentWalker = new GherkinDocumentWalker<>(handler);

        try (var printWriter = new PrintWriter(new OutputStreamWriter(out, UTF_8))) {
            documentWalker.walkGherkinDocument(gherkinDocument, printWriter);
        }
    }

    public static final class Builder {

        private final Map<Element, Margin> margins = new EnumMap<>(DEFAULT_MARGINS);
        private String indentation = DEFAULT_INDENTATION;
        private @Nullable Pattern tableRightAlignedValuesPattern = DEFAULT_TABLE_RIGHT_ALIGNED_VALUES_PATTERN;

        private Builder() {
        }

        /**
         * Configure the indentation step. A number of spaces or tabs recommended.
         *
         * <p>Defaults to two spaces.
         */
        public Builder indentation(String indentation) {
            requireNonNull(indentation);
            if (indentation.isEmpty()) {
                throw new IllegalArgumentException("indentation should not be empty");
            }
            this.indentation = indentation;
            return this;
        }

        /**
         * Configure the margins around an element.
         *
         * <p>Note: Comments attached to the element are considered to be
         * contained by the element. Detached comments are not.
         */
        public Builder margin(Element element, Margin margin) {
            requireNonNull(element);
            requireNonNull(margin);
            margins.put(element, margin);
            return this;
        }

        /**
         * Configure the pattern for values that should be right aligned in data
         * and example tables. Or null if all values should be left aligned.
         */
        public Builder tableRightAlignedValuesPattern(@Nullable Pattern pattern) {
            this.tableRightAlignedValuesPattern = pattern;
            return this;
        }

        public Pretty build() {
            return new Pretty(new Configuration(indentation, Map.copyOf(margins), tableRightAlignedValuesPattern));
        }

    }
}
