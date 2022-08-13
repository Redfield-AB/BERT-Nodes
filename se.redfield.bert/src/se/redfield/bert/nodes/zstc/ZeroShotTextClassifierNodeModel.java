/*
 * Copyright (c) 2022 Redfield AB.
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

package se.redfield.bert.nodes.zstc;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.knime.core.data.DataTableSpec;
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
import se.redfield.bert.core.PredictionTableBuilder;
import se.redfield.bert.nodes.port.BertModelConfig;
import se.redfield.bert.nodes.port.BertModelFeature;
import se.redfield.bert.nodes.port.BertModelPortObject;
import se.redfield.bert.nodes.port.BertModelPortObjectSpec;
import se.redfield.bert.setting.ZeroShotTextClassifierSettings;
import se.redfield.bert.util.InputUtils;

/**
 * Zero shot Text Classifier
 * 
 * @author Abderrahim Alakouche
 */
public class ZeroShotTextClassifierNodeModel extends NodeModel {

	/**
	 * BERT model input port index.
	 */
	public static final int PORT_BERT_MODEL = 0;

	/**
	 * Data table input port index
	 */
	public static final int PORT_DATA_TABLE = 1;

	private final ZeroShotTextClassifierSettings settings = new ZeroShotTextClassifierSettings();

	private PredictionTableBuilder outputBuilder;

	protected ZeroShotTextClassifierNodeModel() {
		super(new PortType[] { BertModelPortObject.TYPE, BufferedDataTable.TYPE },
				new PortType[] { BufferedDataTable.TYPE });
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {

		BertModelPortObject zstcModel = (BertModelPortObject) inObjects[PORT_BERT_MODEL];

		BufferedDataTable inTable = (BufferedDataTable) inObjects[PORT_DATA_TABLE];

		BufferedDataTable probabilitiesTable = runZeroShotTextClassifier(zstcModel.getModel(), inTable,
				exec.createSubExecutionContext(0.9));
		BufferedDataTable outputTable = outputBuilder.buildPredictionTable(inTable, probabilitiesTable,
				exec.createSubExecutionContext(0.1));

		return new PortObject[] { outputTable };

	}

	private BufferedDataTable runZeroShotTextClassifier(BertModelConfig zstcModel, BufferedDataTable inTable,
			ExecutionContext exec) throws PythonKernelCleanupException, DLInvalidEnvironmentException,
			PythonIOException, CanceledExecutionException {
		var preprocessedTable = InputUtils.toStringColumnsTable(inTable, exec.createSubExecutionContext(0.05),
				settings.getSentenceColumn());

		try (BertCommands commands = new BertCommands(settings.getPythonCommand(), 1)) {
			commands.putDataTable(preprocessedTable, exec.createSubProgress(0.05));
			commands.executeInKernel(getZeroShotTextClassifierScript(zstcModel), exec.createSubProgress(0.8));

			return commands.getDataTable(exec, exec.createSubProgress(0.1));

		}

	}

	private String getZeroShotTextClassifierScript(BertModelConfig zstcModel) {
		DLPythonSourceCodeBuilder b = DLPythonUtils
				.createSourceCodeBuilder("from ZeroShotTextClassifier import ZeroShotTextClassifier");

		b.a("ZeroShotTextClassifier.run_zstc(").n();
		BertCommands.putInputTableArgs(b); // input_table
		BertCommands.putSentenceColumArg(b, settings.getSentenceColumn()); // sentence_column
		b.a("candidate_labels = ").as(settings.getCandidateLabels()).a(",").n(); // candidate_labels
		b.a("hypothesis = ").as(settings.getHypothesis()).a(",").n(); // hypothesis
		BertCommands.putBertModelArgs(b, zstcModel); // bert_model_handle, cach_dir
		b.a("multi_label = ").a(settings.isMultilabelClassification()).a(",").n();
		b.a(")").n();

		return b.toString();

	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		DataTableSpec inTableSpec = (DataTableSpec) inSpecs[PORT_DATA_TABLE];
		settings.validate(inTableSpec);
		validateModelFeatures((BertModelPortObjectSpec) inSpecs[PORT_BERT_MODEL]);

		outputBuilder = new PredictionTableBuilder(settings, settings.isMultilabelClassification(),
				settings.getCandidateLabels());
		return new PortObjectSpec[] { outputBuilder.createSpec(inTableSpec) };

	}

	private void validateModelFeatures(BertModelPortObjectSpec spec) throws InvalidSettingsException {
		List<BertModelFeature> features = spec.getModel().getFeatures();
		if (features.isEmpty()) {
			setWarningMessage("Unable to detect if the selected model supports Zero-shot classification.");
		} else if (!features.contains(BertModelFeature.ZSTC)) {
			throw new InvalidSettingsException("The selected model does not support Zero-shot classification");
		}
	}

	@Override
	protected void reset() {
		outputBuilder = null;
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
}
