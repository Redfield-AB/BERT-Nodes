/*
 * Copyright (c) 2020 Redfield AB.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 *  
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package se.redfield.bert.util;

import java.util.Optional;
import java.util.function.Function;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 * Provides utilities to prepare the input table for Python.
 * 
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public final class InputUtils {

	private InputUtils() {

	}

	/**
	 * Converts the provided columns to string columns if they aren't already string
	 * columns and removes all other columns from the provided table.
	 * 
	 * @param inTable The table to convert.
	 * @param exec    Execution context.
	 * @param columns The columns to keep and convert to strings.
	 * @return The converted table containing only selected columns as a string
	 *         columns.
	 * @throws CanceledExecutionException
	 */
	public static BufferedDataTable toStringColumnsTable(BufferedDataTable inTable, ExecutionContext exec,
			String... columns) throws CanceledExecutionException {
		var rearranger = new ColumnRearranger(inTable.getDataTableSpec());
		InputUtils.convertColumnsToString(rearranger, columns);
		rearranger.keepOnly(columns);
		return exec.createColumnRearrangeTable(inTable, rearranger, exec);
	}

	/**
	 * Converts the provided columns to string columns if they aren't already string
	 * columns.
	 * 
	 * @param rearranger the rearranger to modify
	 * @param columns    name of the columns to convert
	 */
	public static void convertColumnsToString(final ColumnRearranger rearranger, final String... columns) {
		var spec = rearranger.createSpec();
		for (var column : columns) {
			var colIdx = spec.findColumnIndex(column);
			var colSpec = spec.getColumnSpec(colIdx);
			getConverter(colSpec.getType())//
					.ifPresent(c -> rearranger.replace(new ToStringCellFactory(colIdx, colSpec.getName(), c), colIdx));
		}
	}

	private static Optional<Function<DataCell, String>> getConverter(DataType type) {
		if (type.isCompatible(DocumentValue.class)) {
			return Optional.of(InputUtils::documentValueToString);
		} else if (!type.equals(StringCell.TYPE)) {
			return Optional.of(InputUtils::stringValueToString);
		} else {
			return Optional.empty();
		}
	}

	private static String documentValueToString(final DataCell cell) {
		var document = ((DocumentValue) cell).getDocument();
		return document.getDocumentBodyText();
	}

	private static String stringValueToString(final DataCell cell) {
		return ((StringValue) cell).getStringValue();
	}

	/**
	 * Turns any column into a String column.
	 * 
	 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
	 */
	private static final class ToStringCellFactory extends SingleCellFactory {

		private final int m_colIdx;

		private final Function<DataCell, String> m_converter;

		ToStringCellFactory(int colIdx, String name, final Function<DataCell, String> converter) {
			super(new DataColumnSpecCreator(name, StringCell.TYPE).createSpec());
			m_colIdx = colIdx;
			m_converter = converter;
		}

		@Override
		public DataCell getCell(DataRow row) {
			var cell = row.getCell(m_colIdx);
			if (cell.isMissing()) {
				return cell;
			} else {
				return new StringCell(m_converter.apply(cell));
			}
		}

	}

}
