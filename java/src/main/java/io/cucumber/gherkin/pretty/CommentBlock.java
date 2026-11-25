package io.cucumber.gherkin.pretty;

import io.cucumber.messages.types.Comment;
import io.cucumber.messages.types.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

record CommentBlock(Location start, Location end, List<Comment> comments) {

    static List<CommentBlock> groupByCommentBlock(List<Comment> comments) {
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }
        var blocks = new ArrayList<List<Comment>>();

        var currentBlockStartIndex = 0;
        int currentLine = comments.get(0).getLocation().getLine();
        int size = comments.size();
        for (int i = 0; i < size; i++) {
            int candidate = comments.get(i).getLocation().getLine();
            if (currentLine < candidate - 1) {
                blocks.add(comments.subList(currentBlockStartIndex, i));
                currentBlockStartIndex = i;
            }
            currentLine = candidate;
        }
        blocks.add(comments.subList(currentBlockStartIndex, comments.size()));

        return blocks.stream() //
                .map(block -> new CommentBlock(block.get(0).getLocation(), block.get(block.size() - 1).getLocation(), block)) //
                .collect(Collectors.toList());
    }
}
