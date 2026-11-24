package io.cucumber.gherkin.pretty;

import io.cucumber.messages.types.DataTable;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.TableCell;
import io.cucumber.messages.types.TableRow;

import java.util.List;
import java.util.function.Consumer;

final class PickleTableFormatter {

    private int[] longestCellLengthInColumn = new int[0];


    PickleTableFormatter() {
    }

    void setCurrentTable(DataTable dataTable){
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
            int candidate = renderCell(row.getCells().get(colIndex)).length();
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
            int padding = longestCellLengthInColumn[i] - cellText.length();
            builder.append(renderCellWithPadding(cellText, padding));
            builder.append("|");
        }
        return builder.toString();
    }

    private String renderCell(TableCell cell) {
        // TODO: Handle escapes
        return cell.getValue();
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
