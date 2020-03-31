/*
 * Portfolio
 * Copyright (C) 2020  Vitalii Ananev <an-vitek@ya.ru>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ru.portfolio.portfolio.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Collections.emptyList;

@Slf4j
@ToString(of = {"tableName"})
public class ExcelTable implements Iterable<Row> {
    @Getter
    private final Sheet sheet;
    private final String tableName;
    @Getter
    private final CellRangeAddress tableRange;
    private final Map<TableColumn, Integer> columnIndices;
    @Getter
    private final boolean empty;
    /**
     * Offset of first data row. First table row is a header. Default is 2.
     */
    @Setter
    private int dataRowOffset = 2;
    /**
     * Set to true if last table row contains total information. Default is false.
     */
    @Setter
    private boolean isLastTableRowContainsTotalData = false;

    public static ExcelTable of(Sheet sheet, String tableName, String tableFooterString,
                         Class<? extends TableColumnDescription> headerDescription) {
        ExcelTable table = new ExcelTable(sheet, tableName,
                ExcelTableHelper.getTableCellRange(sheet, tableName, tableFooterString),
                headerDescription);
        table.setLastTableRowContainsTotalData(true);
        return table;
    }

    public static ExcelTable of(Sheet sheet, String tableName,
                         Class<? extends TableColumnDescription> headerDescription) {
        ExcelTable table = new ExcelTable(sheet, tableName,
                ExcelTableHelper.getTableCellRange(sheet, tableName),
                headerDescription);
        table.setLastTableRowContainsTotalData(false);
        return table;
    }

    private ExcelTable(Sheet sheet, String tableName, CellRangeAddress tableRange, Class<? extends TableColumnDescription> headerDescription) {
        this.sheet = sheet;
        this.tableName = tableName;
        this.tableRange = tableRange;
        this.empty = this.tableRange.equals(ExcelTableHelper.EMTPY_RANGE);
        this.columnIndices = empty ?
                Collections.emptyMap() :
                getColumnIndices(sheet, this.tableRange, headerDescription);
    }

    private  Map<TableColumn, Integer> getColumnIndices(Sheet sheet, CellRangeAddress tableRange,
                                                        Class<? extends TableColumnDescription> headerDescription) {
        Map<TableColumn, Integer> columnIndices = new HashMap<>();
        Row header = sheet.getRow(tableRange.getFirstRow() + 1);
        TableColumn[] columns = Arrays.stream(headerDescription.getEnumConstants())
                .map(TableColumnDescription::getColumn)
                .toArray(TableColumn[]::new);
        for (TableColumn column : columns) {
            columnIndices.put(column, column.getColumnIndex(header));
        }
        return columnIndices;
    }

    public <T> List<T> getData(Path file, BiFunction<ExcelTable, Row, T> rowExtractor) {
        return getDataCollection(file, (table, row) ->
                        Optional.ofNullable(rowExtractor.apply(table, row))
                                .map(Collections::singletonList)
                                .orElse(emptyList()));
    }

    public <T> List<T> getDataCollection(Path file, BiFunction<ExcelTable, Row, Collection<T>> rowExtractor) {
        return getDataCollection(file, rowExtractor, e -> Arrays.asList(e, e));
    }

    public <T> List<T> getDataCollection(Path file, BiFunction<ExcelTable, Row, Collection<T>> rowExtractor,
                                         Function<T, Collection<T>> mergeDuplicates) {
        List<T> data = new ArrayList<>();
        for (Row row : this) {
            if (row != null) {
                try {
                    Collection<T> result = rowExtractor.apply(this, row);
                    if (result != null) {
                        for (T r : result) {
                            if (data.contains(r)) {
                                data.remove(r);
                                data.addAll(mergeDuplicates.apply(r));
                            } else {
                                data.add(r);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Не могу распарсить таблицу '{}' в файле {}, строка {}", tableName, file.getFileName(), row.getRowNum(), e);
                }
            }
        }
        return data;
    }

    public Cell getCell(Row row, TableColumnDescription columnDescription) {
        return row.getCell(columnIndices.get(columnDescription.getColumn()));
    }

    public Cell getCell(CellAddress address) {
        return sheet.getRow(address.getRow()).getCell(address.getColumn());
    }

    public int getIntCellValue(Row row, TableColumnDescription columnDescription) {
        return (int) getLongCellValue(row, columnDescription);
    }

    public long getIntCellValue(CellAddress address) {
        return (int) getLongCellValue(address);
    }

    public long getLongCellValue(Row row, TableColumnDescription columnDescription) {
        return getLongCellValue(getCell(row, columnDescription));
    }

    public long getLongCellValue(CellAddress address) {
        return getLongCellValue(getCell(address));
    }

    public static long getLongCellValue(Cell cell) {
        CellType type = cell.getCellType();
        if (type == CellType.NUMERIC) {
            return Double.valueOf(cell.getNumericCellValue()).longValue();
        } else {
            return Long.parseLong(cell.getStringCellValue());
        }
    }

    public BigDecimal getCurrencyCellValue(Row row, TableColumnDescription columnDescription) {
        return getCurrencyCellValue(getCell(row, columnDescription));
    }

    public BigDecimal getCurrencyCellValue(CellAddress address) {
        return getCurrencyCellValue(getCell(address));
    }

    public static BigDecimal getCurrencyCellValue(Cell cell) {
        double cellValue = cell.getNumericCellValue();
        return (cellValue - 0.01d < 0) ? BigDecimal.ZERO : BigDecimal.valueOf(cellValue);
    }

    public String getStringCellValue(Row row, TableColumnDescription columnDescription) {
        return getStringCellValue(getCell(row, columnDescription));
    }

    public String getStringCellValue(CellAddress address) {
        return getStringCellValue(getCell(address));
    }

    public static String getStringCellValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return "";
        }
        return cell.getStringCellValue();
    }

    @Override
    public Iterator<Row> iterator() {
        return new ExelTableIterator();
    }

    class ExelTableIterator implements Iterator<Row> {
        private int cnt = 0;

        @Override
        public boolean hasNext() {
            int dataRowsCount = tableRange.getLastRow() - tableRange.getFirstRow()
                    - dataRowOffset
                    + (isLastTableRowContainsTotalData ? 0 : 1);
            return cnt < dataRowsCount;
        }

        @Override
        public Row next() {
            Row row;
            do {
                row = sheet.getRow(tableRange.getFirstRow() + dataRowOffset + (cnt++));
            } while (row == null && hasNext());
            return row;
        }
    }
}
