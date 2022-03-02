/*
 * InvestBook
 * Copyright (C) 2022  Vitalii Ananev <spacious-team@ya.ru>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ru.investbook.parser.tinkoff;

import lombok.RequiredArgsConstructor;
import org.spacious_team.broker.report_parser.api.BrokerReport;
import org.spacious_team.broker.report_parser.api.ReportTables;
import org.spacious_team.broker.report_parser.api.ReportTablesFactory;
import org.springframework.stereotype.Component;
import ru.investbook.parser.TransactionValueAndFeeParser;

@Component
@RequiredArgsConstructor
public class TinkoffReportTablesFactory implements ReportTablesFactory {
    private final TransactionValueAndFeeParser transactionValueAndFeeParser;

    @Override
    public boolean canCreate(BrokerReport brokerReport) {
        return brokerReport instanceof TinkoffBrokerReport;
    }

    @Override
    public ReportTables create(BrokerReport brokerReport) {
        return new TinkoffReportTables((TinkoffBrokerReport) brokerReport, transactionValueAndFeeParser);
    }
}
