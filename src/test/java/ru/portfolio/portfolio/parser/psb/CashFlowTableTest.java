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

package ru.portfolio.portfolio.parser.psb;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.testng.Assert.assertEquals;

@Ignore
public class CashFlowTableTest {

    @DataProvider(name = "cash")
    Object[][] getData() {
        return new Object[][]{{"E:\\1.xlsx", BigDecimal.valueOf(400 - 760.77)},
                {"E:\\Налог.xlsx", BigDecimal.valueOf(-542.0)}};
    }

    @Test(dataProvider = "cash")
    void testIsin(String reportFile, BigDecimal expectedCashIn) throws IOException {
        PsbBrokerReport report = new PsbBrokerReport(reportFile);
        List<CashFlowTable.CashFlowTableRow> data = new CashFlowTable(report).getData();
        BigDecimal sum = BigDecimal.ZERO;
        for (CashFlowTable.CashFlowTableRow r : data) {
            sum = sum.add(r.getValue());
        }
        assertEquals(sum, expectedCashIn);
    }
}