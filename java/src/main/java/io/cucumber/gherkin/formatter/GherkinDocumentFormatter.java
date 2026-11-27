package io.cucumber.gherkin.formatter;

import io.cucumber.messages.types.Comment;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Location;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.cucumber.gherkin.formatter.Configuration.DEFAULT_INDENTATION;
import static io.cucumber.gherkin.formatter.Configuration.DEFAULT_MARGINS;
import static io.cucumber.gherkin.formatter.Configuration.DEFAULT_TABLE_RIGHT_ALIGNED_VALUES_PATTERN;
import static io.cucumber.gherkin.formatter.Configuration.DEFAULT_TAG_ORDER;
import static io.cucumber.gherkin.formatter.Configuration.DEFAULT_TAG_STYLE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

/**
 * Format Gherkin documents.
 */
public final class GherkinDocumentFormatter {

    private final Configuration configuration;

    private GherkinDocumentFormatter(Configuration configuration) {
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

        var handler = new Pretty(configuration);
        var documentWalker = new GherkinDocumentWalker<>(handler);

        try (var printWriter = new PrintWriter(new OutputStreamWriter(out, UTF_8))) {
            documentWalker.walkGherkinDocument(gherkinDocument, printWriter);
        }
    }

    public static final class Builder {

        private final Map<Element, Margin> margins = new EnumMap<>(DEFAULT_MARGINS);
        private String indentation = DEFAULT_INDENTATION;
        private @Nullable Pattern tableRightAlignedValuesPattern = DEFAULT_TABLE_RIGHT_ALIGNED_VALUES_PATTERN;
        private TagLayout tagLayout = DEFAULT_TAG_STYLE;
        private TagOrder tagOrder = DEFAULT_TAG_ORDER;

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
        public Builder margins(Element element, Margin margin) {
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

        /**
         * Configure the layout of the tags in a Gherkin Document.
         */
        public Builder tagStyle(TagLayout tagLayout) {
            requireNonNull(tagLayout);
            this.tagLayout = tagLayout;
            return this;
        }

        /**
         * Configure the order of the tags in a Gherkin document.
         */
        public Builder tagOrder(TagOrder tagOrder) {
            requireNonNull(tagOrder);
            this.tagOrder = tagOrder;
            return this;
        }

        public GherkinDocumentFormatter build() {
            return new GherkinDocumentFormatter(new Configuration(
                    indentation,
                    Map.copyOf(margins),
                    tableRightAlignedValuesPattern,
                    tagLayout,
                    tagOrder
            ));
        }

    }

    /**
     * Configures how tags in a Gherkin document are laid out.
     */
    public enum TagLayout {
        /**
         * Tags are kept on the line they're defined.
         *
         * <p>When used in combination with {@link TagOrder#SORTED}, only tags on
         * the same line are sorted.
         */
        AS_IS,

        /**
         * Each tag is written on its own line.
         */
        ONE_PER_LINE,

        /**
         * All tags are written on the same line.
         */
        ONE_LINE
    }

    /**
     * Configures the order in tags in a Gherkin document are laid out.
     */
    public enum TagOrder {
        /**
         * Tags are kept in order they're found in the Gherkin Document.
         */
        USER_DEFINED_ORDER,
        /**
         * Tags are sorted lexicographically.
         *
         * <p>When used in combination with the default {@link TagLayout#AS_IS},
         * only tags on the same line are sorted.
         */
        SORTED
    }

    public enum Margin {
        NONE, BEFORE, AFTER, BOTH
    }

    /**
     * Represent an element in a feature file.
     */
    public enum Element {
        DOCUMENT,
        LANGUAGE,
        FEATURE,
        DESCRIPTION,
        RULE,
        BACKGROUND,
        SCENARIO,
        STEP,
        DOC_STRING,
        DATA_TABLE,
        DATA_TABLE_ROW,
        EXAMPLES,
        EXAMPLES_TABLE_ROW,
        TAGS,
        COMMENTS,
        DETACHED_COMMENTS
    }

    record CommentBlock(Location start, Location finish, List<Comment> comments) {

        static List<CommentBlock> groupByCommentBlock(List<Comment> comments) {
           var blocks = GherkinDocumentUtil.groupByBlock(comments, Comment::getLocation);
            return blocks.stream() //
                    .map(block -> {
                        var start = block.get(0).getLocation();
                        var end = block.get(block.size() - 1).getLocation();
                        return new CommentBlock(start, end, block);
                    }) //
                    .collect(Collectors.toList());
        }
    }
}
