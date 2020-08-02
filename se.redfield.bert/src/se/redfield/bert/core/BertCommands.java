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

import java.util.Collections;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.dl.core.DLInvalidEnvironmentException;
import org.knime.dl.python.prefs.DLPythonPreferences;
import org.knime.dl.python.util.DLPythonSourceCodeBuilder;
import org.knime.dl.python.util.DLPythonUtils;
import org.knime.python2.PythonCommand;
import org.knime.python2.kernel.PythonCancelable;
import org.knime.python2.kernel.PythonCanceledExecutionException;
import org.knime.python2.kernel.PythonExecutionMonitorCancelable;
import org.knime.python2.kernel.PythonIOException;
import org.knime.python2.kernel.PythonKernel;
import org.knime.python2.kernel.PythonKernelCleanupException;
import org.knime.python2.kernel.PythonKernelOptions;
import org.knime.python2.kernel.PythonKernelQueue;

import com.google.common.base.Strings;

import se.redfield.bert.setting.InputSettings;

public class BertCommands implements AutoCloseable {

	public static final String VAR_INPUT_TABLE = "input_table";
	public static final String VAR_OUTPUT_TABLE = "output_table";
	public static final String VAR_BERT_LAYER = "bert_layer";
	public static final String VAR_TOKENIZER = "tokenizer";

	private PythonKernel kernel;

	public BertCommands() throws DLInvalidEnvironmentException {
		kernel = createKernel();
	}

	public static PythonKernel createKernel() throws DLInvalidEnvironmentException {
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

	public void putDataTable(String name, BufferedDataTable table, ExecutionMonitor exec)
			throws PythonIOException, CanceledExecutionException {
		kernel.putDataTable(name, table, exec);
	}

	public void putDataTable(BufferedDataTable table, ExecutionMonitor exec)
			throws PythonIOException, CanceledExecutionException {
		putDataTable(VAR_INPUT_TABLE, table, exec);
	}

	public BufferedDataTable getDataTable(String name, ExecutionContext exec, ExecutionMonitor monitor)
			throws PythonIOException, CanceledExecutionException {
		return kernel.getDataTable(name, exec, monitor);
	}

	public void executeInKernel(String code, ExecutionMonitor exec)
			throws PythonIOException, CanceledExecutionException {
		kernel.execute(code, new PythonExecutionMonitorCancelable(exec));
	}

	public void loadBertModel(String modelHandle, ExecutionMonitor exec)
			throws PythonIOException, CanceledExecutionException {
		DLPythonSourceCodeBuilder b = DLPythonUtils.createSourceCodeBuilder();
		b.a("import tensorflow_hub as hub").n();
		b.a(VAR_BERT_LAYER).a(" = hub.KerasLayer(").as(modelHandle).a(", trainable=True)").n();

		executeInKernel(b.toString(), exec);
	}

	public void createTokenizer(InputSettings inputSettings, ExecutionMonitor exec)
			throws PythonIOException, CanceledExecutionException {
		DLPythonSourceCodeBuilder b = DLPythonUtils.createSourceCodeBuilder("from BertTokenizer import BertTokenizer");
		b.a(VAR_TOKENIZER).a(" = BertTokenizer(").a(VAR_BERT_LAYER).a(", ").a(inputSettings.getMaxSeqLength()).a(", ")
				.as(inputSettings.getSentenceColumn());

		if (inputSettings.getTwoSentenceMode()) {
			b.a(", ").as(inputSettings.getSecondSentenceColumn());
		}

		b.a(")").n();

		executeInKernel(b.toString(), exec);
	}

	@Override
	public void close() throws PythonKernelCleanupException {
		kernel.close();
	}

}
