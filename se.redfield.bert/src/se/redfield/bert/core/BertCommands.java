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

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.knime.core.data.filestore.FileStore;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.dl.core.DLInvalidEnvironmentException;
import org.knime.dl.python.util.DLPythonSourceCodeBuilder;
import org.knime.python2.PythonCommand;
import org.knime.python2.config.PythonCommandConfig;
import org.knime.python2.extensions.serializationlibrary.SerializationOptions;
import org.knime.python2.kernel.PythonCancelable;
import org.knime.python2.kernel.PythonCanceledExecutionException;
import org.knime.python2.kernel.PythonExecutionMonitorCancelable;
import org.knime.python2.kernel.PythonIOException;
import org.knime.python2.kernel.PythonKernel;
import org.knime.python2.kernel.PythonKernelCleanupException;
import org.knime.python2.kernel.PythonKernelOptions;
import org.knime.python2.kernel.PythonKernelQueue;
import org.knime.python2.kernel.PythonOutputListener;

import com.google.common.base.Strings;

import se.redfield.bert.nodes.port.BertModelConfig;
import se.redfield.bert.nodes.port.BertModelType;
import se.redfield.bert.setting.InputSettings;

public class BertCommands implements AutoCloseable {

	public static final String VAR_INPUT_TABLE = "input_table";
	public static final String VAR_OUTPUT_TABLE = "output_table";

	private static final int DEFAULT_TABLE_CHUNK_SIZE = 10000;

	private PythonKernel kernel;
	private ProgressListener progressListener;

	public BertCommands(PythonCommandConfig config) throws DLInvalidEnvironmentException {
		kernel = createKernel(config.getCommand());
		progressListener = new ProgressListener();
		kernel.addStdoutListener(progressListener);
	}

	public static PythonKernel createKernel(PythonCommand command) throws DLInvalidEnvironmentException {
		PythonKernelOptions options = getKernelOptions();
		try {
			PythonKernel kernel = PythonKernelQueue.getNextKernel(command, Collections.emptySet(),
					Collections.emptySet(), options, PythonCancelable.NOT_CANCELABLE);
			kernel.execute("import tensorflow as tf");
			return kernel;
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
		SerializationOptions serializationOpts = new SerializationOptions().forChunkSize(DEFAULT_TABLE_CHUNK_SIZE);

		return new PythonKernelOptions().forAddedAdditionalRequiredModuleNames(Arrays.asList("bert", "tensorflow_hub"))
				.forSerializationOptions(serializationOpts);
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
		progressListener.setMonitor(exec);
		kernel.execute(code, new PythonExecutionMonitorCancelable(exec));
		progressListener.setMonitor(null);
		exec.setProgress(1.0);
	}

	@Override
	public void close() throws PythonKernelCleanupException {
		kernel.close();
	}

	public static void putInputTableArgs(DLPythonSourceCodeBuilder b) {
		b.a(VAR_INPUT_TABLE).a(" = ").a(VAR_INPUT_TABLE).a(",").n();
	}

	public static void putBertModelArgs(DLPythonSourceCodeBuilder b, BertModelConfig model) {
		b.a("bert_model_handle = ").asr(model.getHandle()).a(", ").n();

		String cacheDir = model.getCacheDir();
		if (cacheDir != null && !cacheDir.isEmpty()) {
			b.a("cache_dir = ").asr(cacheDir).a(",").n();
		}

		putModelTypeArg(b, model.getType());
	}

	// CandidateLabels
	public static void putCandidateLabelsArg(DLPythonSourceCodeBuilder b, String candidateLabels) {
		b.a("candidate_labels = ").as(candidateLabels).a(",").n();
	}

	public static void putFileStoreArgs(DLPythonSourceCodeBuilder b, FileStore fileStore) {
		b.a("file_store = ").asr(fileStore.getFile().getAbsolutePath()).a(",").n();
	}

	public static void putBatchSizeArgs(DLPythonSourceCodeBuilder b, int batchSize) {
		b.a("batch_size = ").a(batchSize).a(",").n();
	}

	public static void putArgs(DLPythonSourceCodeBuilder b, InputSettings input) {
		putSentenceColumArg(b, input.getSentenceColumn());
		putMaxSeqLengthArg(b, input.getMaxSeqLength());

		if (input.getTwoSentenceMode()) {
			b.a("second_sentence_column = ").as(input.getSecondSentenceColumn()).a(",").n();
		}
	}

	public static void putSentenceColumArg(DLPythonSourceCodeBuilder b, String sentenceColumn) {
		b.a("sentence_column = ").as(sentenceColumn).a(",").n();
	}

	public static void putMaxSeqLengthArg(DLPythonSourceCodeBuilder b, int maxSeqLength) {
		b.a("max_seq_length = ").a(maxSeqLength).a(",").n();
	}

	public static void putModelTypeArg(DLPythonSourceCodeBuilder b, BertModelType type) {
		b.a("bert_model_type_key = ").as(type.getKey()).a(",").n();
	}

	private static class ProgressListener implements PythonOutputListener {
		private static final Pattern PATTERN = Pattern.compile("^progress: (\\d+)");

		private ExecutionMonitor monitor;
		private boolean disabled = false;

		public void setMonitor(ExecutionMonitor monitor) {
			this.monitor = monitor;
		}

		@Override
		public void setDisabled(boolean disabled) {
			this.disabled = disabled;
		}

		@Override
		public void messageReceived(String message, boolean isWarningMessage) {
			if (monitor != null && !disabled) {
				Matcher m = PATTERN.matcher(message);
				if (m.matches()) {
					try {
						int progress = Integer.parseInt(m.group(1));
						monitor.setProgress(progress / 100.0);
					} catch (Exception e) {
						// ignore
					}
				}
			}
		}

	}
}
