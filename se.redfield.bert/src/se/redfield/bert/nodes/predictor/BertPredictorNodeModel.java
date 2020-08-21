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
package se.redfield.bert.nodes.predictor;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.filestore.FileStore;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.dl.core.DLInvalidEnvironmentException;
import org.knime.dl.python.util.DLPythonSourceCodeBuilder;
import org.knime.dl.python.util.DLPythonUtils;
import org.knime.python2.kernel.PythonIOException;
import org.knime.python2.kernel.PythonKernelCleanupException;

import se.redfield.bert.core.BertCommands;
import se.redfield.bert.nodes.port.BertClassifierPortObject;
import se.redfield.bert.setting.BertPredictorSettings;

/**
 * BERT Predictor node. Takes trained {@link BertClassifierPortObject} and the
 * data table and computes predictions using provided classifier model.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertPredictorNodeModel extends NodeModel {

	/**
	 * {@link BertClassifierPortObject} input port index.
	 */
	public static final int PORT_BERT_CLASSIFIER = 0;
	/**
	 * Data table inpt port index.
	 */
	public static final int PORT_DATA_TABLE = 1;

	private final BertPredictorSettings settings = new BertPredictorSettings();

	protected BertPredictorNodeModel() {
		super(new PortType[] { BertClassifierPortObject.TYPE, BufferedDataTable.TYPE },
				new PortType[] { BufferedDataTable.TYPE });
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		BertClassifierPortObject classifier = (BertClassifierPortObject) inObjects[PORT_BERT_CLASSIFIER];
		return new PortObject[] {
				runPredict(classifier.getFileStore(), (BufferedDataTable) inObjects[PORT_DATA_TABLE], exec) };
	}

	private BufferedDataTable runPredict(FileStore fileStore, BufferedDataTable inTable, ExecutionContext exec)
			throws PythonKernelCleanupException, DLInvalidEnvironmentException, PythonIOException,
			CanceledExecutionException {
		try (BertCommands commands = new BertCommands()) {
			commands.putDataTable(inTable, exec.createSubProgress(0.1));
			commands.executeInKernel(getPredictScript(fileStore.getFile().getAbsolutePath()),
					exec.createSubProgress(0.8));
			return commands.getDataTable(BertCommands.VAR_OUTPUT_TABLE, exec, exec.createSubProgress(0.1));
		}
	}

	private String getPredictScript(String fileStore) {
		DLPythonSourceCodeBuilder b = DLPythonUtils
				.createSourceCodeBuilder("from BertClassifier import BertClassifier");
		b.a(BertCommands.VAR_OUTPUT_TABLE).a(" = BertClassifier.run_predict(").n();

		BertCommands.putInputTableArgs(b);
		BertCommands.putArgs(b, settings.getInputSettings());
		BertCommands.putFileStoreArgs(b, fileStore);
		BertCommands.putBatchSizeArgs(b, settings.getBatchSize());

		b.a(")").n();

		return b.toString();
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		return new PortObjectSpec[] { null };
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// no internals
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// no internals
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		this.settings.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		this.settings.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		this.settings.loadSettingsFrom(settings);
	}

	@Override
	protected void reset() {
		// nothing to reset
	}

}
