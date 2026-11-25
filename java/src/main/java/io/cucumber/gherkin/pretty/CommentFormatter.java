package io.cucumber.gherkin.pretty;

import io.cucumber.messages.types.Comment;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class CommentFormatter {

    void formatCommentBlockTo(List<Comment> comments, Consumer<String> lineBuilder) {
        var lines = comments.stream()
                .map(Comment::getText)
                .map(String::stripLeading)
                .map(s -> s.substring(1))
                .map(String::stripTrailing)
                .collect(Collectors.joining("\n"))
                // Removes empty trailing lines
                .stripTrailing()
                .stripIndent()
                .lines()
                .toList();

        var prefixWithoutSpace = lines.stream().allMatch(this::canBePrefixedWithoutSpace);
        lines.stream()
                .filter(stripLeading())
                .map(trimmed -> prefix(prefixWithoutSpace, trimmed))
                .forEach(lineBuilder);
    }

    private boolean canBePrefixedWithoutSpace(String s) {
        if (s.isEmpty()) {
            return true;
        }
        if (s.startsWith("#")) {
            return true;
        }
        if (s.startsWith(" ") || s.startsWith("\t")) {
            return true;
        }
        return Character.isWhitespace(s.codePointAt(0));
    }

    private static Predicate<String> stripLeading() {
        boolean[] enabled = new boolean[]{true};
        return s -> {
            if (!enabled[0]) {
                return true;
            }
            if (s.isEmpty()) {
                return false;
            }
            // Disable on first non-blank line
            enabled[0] = false;
            return true;
        };
    }

    private static String prefix(boolean prefixWithoutSpace, String trimmed) {
        if (trimmed.isEmpty()) {
            return "#";
        }
        if (prefixWithoutSpace) {
            return "#" + trimmed;
        }
        return "# " + trimmed;
    }

}
