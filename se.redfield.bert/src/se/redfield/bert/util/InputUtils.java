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

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.StringCell;

/**
 * Provides utilities to prepare the input table for Python.
 * 
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public final class InputUtils {
	
	private InputUtils() {
		
	}
	
	/**
	 * Converts the provided columns to string columns if they aren't already string columns.
	 * 
	 * @param rearranger the rearranger to modify
	 * @param columns name of the columns to convert
	 */
	public static void convertColumnsToString(final ColumnRearranger rearranger, final String ...columns) {
		var spec = rearranger.createSpec();
		for (var column : columns) {
			var colIdx = spec.findColumnIndex(column);
			var colSpec = spec.getColumnSpec(colIdx);
			if (!colSpec.getType().equals(StringCell.TYPE)) {
				rearranger.replace(new ToStringCellFactory(colIdx, colSpec.getName()), colIdx);
			}
		}
	}
	
	/**
	 * Turns any column into a String column.
	 * 
	 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
	 */
	private static class ToStringCellFactory extends SingleCellFactory {

		private final int m_colIdx;
		
		ToStringCellFactory(int colIdx, String name) {
			super(new DataColumnSpecCreator(name, StringCell.TYPE).createSpec());
			m_colIdx = colIdx;
		}

		@Override
		public DataCell getCell(DataRow row) {
			var cell = row.getCell(m_colIdx);
			if (cell.isMissing()) {
				return cell;
			} else {
				return new StringCell(cell.toString());
			}
		}
		
	}

}
