package io.cucumber.gherkin.pretty;

import io.cucumber.messages.types.GherkinDocument;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

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

        private String indentation = "  ";

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

        public Pretty build() {
            return new Pretty(new Configuration(indentation));
        }

    }
}
