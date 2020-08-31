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
package se.redfield.bert.nodes.classifier;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.MissingValue;
import org.knime.core.data.MissingValueException;
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
import se.redfield.bert.nodes.port.BertClassifierPortObjectSpec;
import se.redfield.bert.nodes.port.BertModelConfig;
import se.redfield.bert.nodes.port.BertModelPortObject;
import se.redfield.bert.setting.BertClassifierSettings;

/**
 * BERT Classifier node. Takes BERT model and constructs classifier that then
 * trainined on the provided data table.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertClassifierNodeModel extends NodeModel {

	/**
	 * {@link BertModelPortObject} input port index.
	 */
	public static final int PORT_BERT_MODEL = 0;
	/**
	 * Data table input port index.
	 */
	public static final int PORT_DATA_TABLE = 1;

	private final BertClassifierSettings settings = new BertClassifierSettings();

	protected BertClassifierNodeModel() {
		super(new PortType[] { BertModelPortObject.TYPE, BufferedDataTable.TYPE },
				new PortType[] { BertClassifierPortObject.TYPE });
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		BertModelPortObject bertModel = (BertModelPortObject) inObjects[PORT_BERT_MODEL];
		FileStore fileStore = exec.createFileStore("model");

		runTrain(bertModel.getModel(), fileStore, (BufferedDataTable) inObjects[PORT_DATA_TABLE], exec);

		return new PortObject[] { new BertClassifierPortObject(createSpec(), fileStore, settings.getMaxSeqLength()) };
	}

	private void runTrain(BertModelConfig bertModel, FileStore fileStore, BufferedDataTable inTable,
			ExecutionContext exec) throws PythonKernelCleanupException, DLInvalidEnvironmentException,
			PythonIOException, CanceledExecutionException, InvalidSettingsException {
		try (BertCommands commands = new BertCommands()) {
			commands.putDataTable(inTable, exec.createSubProgress(0.1));
			commands.executeInKernel(
					getTrainScript(bertModel, fileStore.getFile().getAbsolutePath(), calcClassCout(inTable)),
					exec.createSubProgress(0.9));
		}
	}

	private int calcClassCout(BufferedDataTable inTable) throws InvalidSettingsException {
		Set<String> values = new HashSet<>();
		int idx = inTable.getSpec().findColumnIndex(settings.getClassColumn());

		for (DataRow row : inTable) {
			DataCell c = row.getCell(idx);

			if (c.isMissing()) {
				throw new MissingValueException((MissingValue) c, "Class column contains missing values");
			}

			values.add(c.toString());
		}

		int classCount = values.size();

		if (classCount < 2) {
			throw new InvalidSettingsException("The class column should contain at least 2 classes");
		}

		return classCount;
	}

	private String getTrainScript(BertModelConfig bertModel, String fileStore, int classCount) {
		DLPythonSourceCodeBuilder b = DLPythonUtils
				.createSourceCodeBuilder("from BertClassifier import BertClassifier");
		b.a("BertClassifier.run_train(").n();

		BertCommands.putInputTableArgs(b);
		BertCommands.putBertModelArgs(b, bertModel);
		BertCommands.putSentenceColumArg(b, settings.getSentenceColumn());
		BertCommands.putMaxSeqLengthArg(b, settings.getMaxSeqLength());
		BertCommands.putFileStoreArgs(b, fileStore);
		BertCommands.putBatchSizeArgs(b, settings.getBatchSize());

		b.a("class_column = ").as(settings.getClassColumn()).a(",").n();
		b.a("class_count = ").a(classCount).a(",").n();
		b.a("epochs = ").a(settings.getEpochs()).a(",").n();
		b.a("fine_tune_bert = ").a(settings.getFineTuneBert()).a(",").n();
		b.a(")").n();

		return b.toString();
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		settings.validate((DataTableSpec) inSpecs[PORT_DATA_TABLE]);
		return new PortObjectSpec[] { createSpec() };
	}

	private static BertClassifierPortObjectSpec createSpec() {
		return new BertClassifierPortObjectSpec();
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
