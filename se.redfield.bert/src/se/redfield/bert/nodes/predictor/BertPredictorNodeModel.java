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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
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
import se.redfield.bert.core.ComputePredictionCellFactory;
import se.redfield.bert.nodes.port.BertClassifierPortObject;
import se.redfield.bert.nodes.port.BertClassifierPortObjectSpec;
import se.redfield.bert.setting.BertPredictorSettings;
import se.redfield.bert.util.InputUtils;

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
		BufferedDataTable inTable = (BufferedDataTable) inObjects[PORT_DATA_TABLE];

		BufferedDataTable probabilitiesTable = runPredict(classifier, inTable, exec);
		BufferedDataTable outputTable = composeOutputTable(inTable, probabilitiesTable, classifier, exec);

		return new PortObject[] { outputTable };
	}

	private BufferedDataTable runPredict(BertClassifierPortObject classifier, BufferedDataTable inTable,
			ExecutionContext exec) throws PythonKernelCleanupException, DLInvalidEnvironmentException,
			PythonIOException, CanceledExecutionException {
		var preprocessedTable = InputUtils.toStringColumnsTable(inTable, exec.createSubExecutionContext(0.05),
				settings.getSentenceColumn());
		try (BertCommands commands = new BertCommands(settings.getPythonCommand(), 1)) {
			commands.putDataTable(preprocessedTable, exec.createSubProgress(0.05));
			commands.executeInKernel(getPredictScript(classifier), exec.createSubProgress(0.8));
			return commands.getDataTable(exec, exec.createSubProgress(0.1));
		}
	}

	private String getPredictScript(BertClassifierPortObject classifier) {
		DLPythonSourceCodeBuilder b = DLPythonUtils
				.createSourceCodeBuilder("from BertClassifier import BertClassifier");
		b.a("BertClassifier.run_predict(").n();

		BertCommands.putInputTableArgs(b);
		BertCommands.putSentenceColumArg(b, settings.getSentenceColumn());
		BertCommands.putMaxSeqLengthArg(b, classifier.getMaxSeqLength());
		BertCommands.putFileStoreArgs(b, classifier.getFileStore());
		BertCommands.putModelTypeArg(b, classifier.getModelType());
		BertCommands.putBatchSizeArgs(b, settings.getBatchSize());

		b.a(")").n();

		return b.toString();
	}

	private BufferedDataTable composeOutputTable(BufferedDataTable inTable, BufferedDataTable probabilitiesTable,
			BertClassifierPortObject classifier, ExecutionContext exec) throws CanceledExecutionException {
		probabilitiesTable = renameProbabilitiesColumns(probabilitiesTable, classifier, exec);

		ColumnRearranger r = new ColumnRearranger(probabilitiesTable.getDataTableSpec());
		r.insertAt(0, ComputePredictionCellFactory.create(settings, classifier));

		if (!settings.getOutputProbabilities()) {
			r.keepOnly(0);
		}

		BufferedDataTable result = exec.createColumnRearrangeTable(probabilitiesTable, r, exec);

		return exec.createJoinedTable(inTable, result, exec);
	}

	private BufferedDataTable renameProbabilitiesColumns(BufferedDataTable probTable,
			BertClassifierPortObject classifier, ExecutionContext exec) {
		if (settings.getOutputProbabilities()) {
			return exec.createSpecReplacerTable(probTable, createProbabilitiesSpec(classifier));
		} else {
			return probTable;
		}
	}

	private DataTableSpec createProbabilitiesSpec(BertClassifierPortObject classifier) {
		List<DataColumnSpec> cols = Arrays.stream(classifier.getClasses())
				.map(c -> String.format("P (%s)%s", c, settings.getProbabilitiesColumnSuffix()))
				.map(name -> new DataColumnSpecCreator(name, DoubleCell.TYPE).createSpec())
				.collect(Collectors.toList());

		return new DataTableSpec(cols.toArray(new DataColumnSpec[] {}));
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		settings.configure((DataTableSpec) inSpecs[PORT_DATA_TABLE],
				(BertClassifierPortObjectSpec) inSpecs[PORT_BERT_CLASSIFIER]);
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
