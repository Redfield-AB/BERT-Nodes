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
package se.redfield.bert.core;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.dl.core.DLInvalidEnvironmentException;
import org.knime.dl.python.prefs.DLPythonPreferences;
import org.knime.dl.python.util.DLPythonSourceCodeBuilder;
import org.knime.dl.python.util.DLPythonUtils;
import org.knime.dl.util.DLUtils;
import org.knime.python2.PythonCommand;
import org.knime.python2.kernel.PythonCancelable;
import org.knime.python2.kernel.PythonCanceledExecutionException;
import org.knime.python2.kernel.PythonExecutionMonitorCancelable;
import org.knime.python2.kernel.PythonIOException;
import org.knime.python2.kernel.PythonKernel;
import org.knime.python2.kernel.PythonKernelOptions;
import org.knime.python2.kernel.PythonKernelQueue;

import com.google.common.base.Strings;

public class BertTokenizer {

	private static final String SCRIPT_FILE_PATH = "py/BertTokenizer.py";

	private static final String INPUT_TABLE_NAME = "input_table";
	private static final String OUTPUT_TABLE_NAME = "output_table";
	private static final String IDS_COLUMN_NAME = "ids";
	private static final String MASKS_COLUMN_NAME = "masks";
	private static final String SEGMENTS_COLUMN_NAME = "segments";

	private static final String IDS_COLUMN_VAR = "IDS_COLUMN";
	private static final String MASKS_COLUMN_VAR = "MASKS_COLUMN";
	private static final String SEGMENTS_COLUMN_VAR = "SEGMENTS_COLUMN";

	public DataTableSpec createSpec(DataTableSpec inTableSpec) {
		DataColumnSpec ids = new DataColumnSpecCreator(IDS_COLUMN_NAME, ListCell.getCollectionType(IntCell.TYPE))
				.createSpec();
		DataColumnSpec masks = new DataColumnSpecCreator(MASKS_COLUMN_NAME, ListCell.getCollectionType(IntCell.TYPE))
				.createSpec();
		DataColumnSpec segments = new DataColumnSpecCreator(SEGMENTS_COLUMN_NAME,
				ListCell.getCollectionType(IntCell.TYPE)).createSpec();

		return new DataTableSpec(inTableSpec, new DataTableSpec(ids, masks, segments));
	}

	public BufferedDataTable tokenize(BufferedDataTable inTable, ExecutionContext exec)
			throws DLInvalidEnvironmentException, CanceledExecutionException, IOException {
		PythonCancelable cancelable = new PythonExecutionMonitorCancelable(exec);

		try (PythonKernel kernel = createKernel()) {
			kernel.putDataTable(INPUT_TABLE_NAME, inTable, exec);
			kernel.execute(setupVariables(), cancelable);
			kernel.execute(readScriptFile(), cancelable);
			return kernel.getDataTable(OUTPUT_TABLE_NAME, exec, exec);
		}
	}

	private static String setupVariables() {
		DLPythonSourceCodeBuilder b = DLPythonUtils.createSourceCodeBuilder();
		b.a(IDS_COLUMN_VAR).a(" = ").as(IDS_COLUMN_NAME).n();
		b.a(MASKS_COLUMN_VAR).a(" = ").as(MASKS_COLUMN_NAME).n();
		b.a(SEGMENTS_COLUMN_VAR).a(" = ").as(SEGMENTS_COLUMN_NAME).n();
		return b.toString();
	}

	private String readScriptFile() throws IOException {
		File script = DLUtils.Files.getFileFromSameBundle(this, SCRIPT_FILE_PATH);
		return DLUtils.Files.readAllUTF8(script);
	}

	private static PythonKernel createKernel() throws DLInvalidEnvironmentException {
		PythonKernelOptions options = getKernelOptions();
		PythonCommand command = getCommand();
		try {
			return PythonKernelQueue.getNextKernel(command, Collections.emptySet(), Collections.emptySet(), options,
					PythonCancelable.NOT_CANCELABLE);
		} catch (PythonIOException e) {
			final String msg = !Strings.isNullOrEmpty(e.getMessage())
					? "An error occurred while trying to launch Python: " + e.getMessage()
					: "An unknown error occurred while trying to launch Python. See log for details.";
			throw new DLInvalidEnvironmentException(msg, e);
		} catch (PythonCanceledExecutionException e) {
			throw new IllegalStateException("Implementation error", e);
		}
	}

	private static PythonKernelOptions getKernelOptions() {
		// TODO add bert as required module
		return new PythonKernelOptions();
	}

	private static PythonCommand getCommand() {
		return DLPythonPreferences.getPythonTF2CommandPreference();
	}
}
