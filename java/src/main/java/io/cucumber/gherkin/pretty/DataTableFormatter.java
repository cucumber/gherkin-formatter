package io.cucumber.gherkin.pretty;

import io.cucumber.messages.types.DataTable;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.TableCell;
import io.cucumber.messages.types.TableRow;

import java.util.List;
import java.util.function.Consumer;

final class DataTableFormatter {

    private int[] longestCellLengthInColumn = new int[0];


    DataTableFormatter() {
    }

    void setCurrentTable(DataTable dataTable) {
        longestCellLengthInColumn = findLongestCellLengthInColumn(dataTable);
    }


    void setCurrentTable(Examples examples) {
        longestCellLengthInColumn = findLongestCellLengthInColumn(examples);
    }

    private int[] findLongestCellLengthInColumn(DataTable table) {
        // datatables are always square and non-sparse.
        var rows = table.getRows();
        if (rows.isEmpty()) {
            return new int[0];
        }
        int width = rows.get(0).getCells().size();
        int[] longestCellInColumnLength = new int[width];
        for (TableRow row : rows) {
            findLongestCellInColumn(longestCellInColumnLength, row);
        }
        return longestCellInColumnLength;
    }

    private int[] findLongestCellLengthInColumn(Examples table) {
        // datatables are always square and non-sparse.
        return table.getTableHeader().map(header -> {
            List<TableCell> headerCells = header.getCells();
            var width = headerCells.size();
            int[] longestCellInColumnLength = new int[width];
            findLongestCellInColumn(longestCellInColumnLength, header);
            for (TableRow row : table.getTableBody()) {
                findLongestCellInColumn(longestCellInColumnLength, row);
            }

            return longestCellInColumnLength;
        }).orElse(new int[0]);
    }

    private void findLongestCellInColumn(int[] longestCellInColumnLength, TableRow row) {
        int width = longestCellInColumnLength.length;
        for (int colIndex = 0; colIndex < width; colIndex++) {
            int current = longestCellInColumnLength[colIndex];
            TableCell cell = row.getCells().get(colIndex);
            int candidate = cellLength(renderCell(cell));
            longestCellInColumnLength[colIndex] = Math.max(current, candidate);
        }
    }

    public void formatTableRowTo(TableRow tableRow, Consumer<String> lineBuilder) {
        lineBuilder.accept(renderTableRowWithPadding(tableRow, longestCellLengthInColumn));
    }

    private String renderTableRowWithPadding(TableRow tableRow, int[] longestCellLengthInColumn) {
        var builder = new StringBuilder("|");
        var cells = tableRow.getCells();
        for (int i = 0; i < cells.size(); i++) {
            var cellText = renderCell(cells.get(i));
            int padding = longestCellLengthInColumn[i] - cellLength(cellText);
            builder.append(renderCellWithPadding(cellText, padding));
            builder.append("|");
        }
        return builder.toString();
    }

    private int cellLength(String rendered) {
        return rendered.codePoints() //
                .map(codepoint ->
                        // CJK
                        ('\u3000' <= codepoint && codepoint <= '\u9fff')
                                // Korean
                                || ('\uac00' <= codepoint && codepoint <= '\ud7af')
                                // Full width characters
                                || ('\uff01' <= codepoint && codepoint <= '\uff60') //
                                ? 2 : 1)
                .sum();
    }

    private String renderCell(TableCell cell) {
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

    private static String renderCellWithPadding(String cellText, int padding) {
        StringBuilder result = new StringBuilder();
        result.append(" ");
        result.append(cellText);
        padSpace(result, padding);
        result.append(" ");
        return result.toString();
    }

    private static void padSpace(StringBuilder result, int padding) {
        result.append(" ".repeat(padding));
    }

}
