package io.cucumber.gherkin.pretty;

import io.cucumber.messages.types.GherkinDocument;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;

public final class Pretty {

    public Pretty() {

    }

    public String format(GherkinDocument gherkinDocument) {
        requireNonNull(gherkinDocument);
        var out = new ByteArrayOutputStream();
        formatTo(gherkinDocument, out);
        return out.toString(StandardCharsets.UTF_8);
    }

    public void formatTo(GherkinDocument gherkinDocument, OutputStream out) {
        requireNonNull(gherkinDocument);
        requireNonNull(out);

        try (var printWriter = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            var handler = new PrettyHandler(printWriter);
            new GherkinDocumentWalker().walkGherkinDocument(gherkinDocument, handler);
        }
    }

}
