package io.cucumber.gherkin.pretty;

import io.cucumber.messages.types.DataTable;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.TableCell;
import io.cucumber.messages.types.TableRow;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

final class DataTableFormatter {

    private static final Pattern IS_NUMBER_LIKE = Pattern.compile(
            // Sign
            "[-+]?" +
                    // Significant
                    "(?:\\d+(?:[,.]\\d+)*(?:[,.]\\d+)?|[,.]\\d+)" +
                    // Exponent
                    "(?:E[-+]?\\d+)?");

    private Map<TableRow, String[]> current = Collections.emptyMap();
    private int[] longestCellLengthInColumn = new int[0];

    DataTableFormatter() {
    }

    void setCurrent(DataTable dataTable) {
        var rendered = new HashMap<TableRow, String[]>();
        dataTable.getRows().forEach(tableRow -> rendered.put(tableRow, render(tableRow)));
        setCurrent(rendered);
    }

    void setCurrent(Examples examples) {
        var rendered = new HashMap<TableRow, String[]>();
        examples.getTableHeader().ifPresent(tableRow -> rendered.put(tableRow, render(tableRow)));
        examples.getTableBody().forEach(tableRow -> rendered.put(tableRow, render(tableRow)));
        setCurrent(rendered);
    }

    void setCurrent(Map<TableRow, String[]> current) {
        this.current = current;
        this.longestCellLengthInColumn = findLongestCellLengthInColumn(current.values());
    }

    public void clearCurrent() {
        this.current = Collections.emptyMap();
        this.longestCellLengthInColumn = new int[0];
    }

    private int[] findLongestCellLengthInColumn(Collection<String[]> rows) {
        // datatables are always square and non-sparse.
        if (rows.isEmpty()) {
            return new int[0];
        }
        int width = rows.iterator().next().length;
        int[] longestCellInColumnLength = new int[width];
        for (String[] row : rows) {
            findLongestCellInColumn(longestCellInColumnLength, row);
        }
        return longestCellInColumnLength;
    }

    private void findLongestCellInColumn(int[] longestCellInColumnLength, String[] row) {
        int width = row.length;
        for (int colIndex = 0; colIndex < width; colIndex++) {
            int current = longestCellInColumnLength[colIndex];
            int candidate = cellLength(row[colIndex]);
            longestCellInColumnLength[colIndex] = Math.max(current, candidate);
        }
    }

    public void formatTableRowTo(TableRow tableRow, Consumer<String> lineBuilder) {
        String[] cells = requireNonNull(current.get(tableRow));
        lineBuilder.accept(renderTableRowWithPadding(cells, longestCellLengthInColumn));
    }

    private String renderTableRowWithPadding(String[] cells, int[] longestCellLengthInColumn) {
        var builder = new StringBuilder("|");
        int length = cells.length;
        for (int i = 0; i < length; i++) {
            var cellText = cells[i];
            // TODO: cellLength is calculated twice
            int padding = longestCellLengthInColumn[i] - cellLength(cellText);
            builder.append(renderCellWithPadding(cellText, padding));
            builder.append("|");
        }
        return builder.toString();
    }

    @SuppressWarnings("UnnecessaryParentheses")
    private int cellLength(String rendered) {
        return rendered.codePoints() //
                .map(codepoint -> {
                    // CJK
                    if ('\u3000' <= codepoint && codepoint <= '\u9fff') {
                        return 2;
                    }
                    // Korean
                    if ('\uac00' <= codepoint && codepoint <= '\ud7af') {
                        return 2;
                    }
                    // Full width characters
                    if ('\uff01' <= codepoint && codepoint <= '\uff60') {
                        return 2;
                    }
                    return 1;
                })
                .sum();
    }


    private String[] render(TableRow tableRow) {
        return tableRow.getCells().stream().map(this::render).toArray(String[]::new);
    }

    private String render(TableCell cell) {
        var value = cell.getValue();
        int length = value.length();
        var builder = new StringBuilder(length);
        for (int i = 0; i < length; ++i) {
            char c = value.charAt(i);
            builder.append(switch (c) {
                case '\\' -> "\\\\";
                case '\n' -> "\\n";
                case '|' -> "\\|";
                default -> c;
            });
        }
        return builder.toString();
    }

    private static String renderCellWithPadding(String cellText, int n) {
        var padding = " ".repeat(n);
        if (isNumberLike(cellText)) {
            return " " + padding + cellText + " ";
        }
        return " " + cellText + padding + " ";
    }

    /**
     * Matches numbers, currency, IP addresses and hopefully not much else.
     */
    private static boolean isNumberLike(String cellText) {
        return IS_NUMBER_LIKE.matcher(cellText).matches();
    }

}
