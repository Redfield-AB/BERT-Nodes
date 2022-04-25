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

package se.redfield.bert.setting;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import se.redfield.bert.nodes.zstc.ZeroShotTextClassifierNodeModel;

/**
 * Settings for {@link ZeroShotTextClassifierNodeModel} node.
 * 
 * @author Abderrahim Alakouche.
 * 
 */

public class ZeroShotTextClassifierSettings extends PythonNodeSettings {

	private static final String KEY_SENTENCE_COLUMN = "sentenceColumn";
	private static final String CANDIDATE_LABELS = "candidateLabels";
	private static final String KEY_USE_CUSTOM_HYPOTHESIS = "useCustomHypothesis";
	private static final String KEY_HYPOTHESIS = "hypothesis";
	private static final String KEY_APPEND_PROBABILITIES = "appendProbabilities";
	private static final String KEY_MULTILABEL_CLASSIFICATION = "multilabelClassification";
	private static final String KEY_USE_CUSTOM_THRESHOLD = "useCustomThreshold";
	private static final String KEY_PREDICTION_THRESHOLD = "predictionThreshold";
	private static final double DEFAULT_PREDICTION_THRESHOLD = 0.5;
	private static final String DEFAULT_HYPOTHESIS = "This example is {}";

	private final SettingsModelString sentenceColumn;
	private final SettingsModelString candidateLabels;
	private final SettingsModelBoolean useCustomHypothesis;
	private final SettingsModelString hypothesis;
	private final SettingsModelBoolean appendProbabilities;
	private final SettingsModelBoolean multilabelClassification;
	private final SettingsModelBoolean useCustomThreshold;
	private final SettingsModelDoubleBounded predictionThreshold;

	/**
	 * Create a new instance.
	 */
	public ZeroShotTextClassifierSettings() {
		sentenceColumn = new SettingsModelString(KEY_SENTENCE_COLUMN, "");
		candidateLabels = new SettingsModelString(CANDIDATE_LABELS, "");
		useCustomHypothesis = new SettingsModelBoolean(KEY_USE_CUSTOM_HYPOTHESIS, false);
		hypothesis = new SettingsModelString(KEY_HYPOTHESIS, DEFAULT_HYPOTHESIS);
		appendProbabilities = new SettingsModelBoolean(KEY_APPEND_PROBABILITIES, false);
		multilabelClassification = new SettingsModelBoolean(KEY_MULTILABEL_CLASSIFICATION, false);
		useCustomThreshold = new SettingsModelBoolean(KEY_USE_CUSTOM_THRESHOLD, false);
		predictionThreshold = new SettingsModelDoubleBounded(KEY_PREDICTION_THRESHOLD, DEFAULT_PREDICTION_THRESHOLD, 0,
				1);

		predictionThreshold.setEnabled(false);
		useCustomThreshold.setEnabled(false);
		hypothesis.setEnabled(false);

		useCustomHypothesis.addChangeListener(e -> {
			boolean selected = useCustomHypothesis.getBooleanValue();

			hypothesis.setEnabled(selected);

		});

		multilabelClassification.addChangeListener(e -> {

			boolean selected = multilabelClassification.getBooleanValue();
			useCustomThreshold.setEnabled(selected);

		});

		useCustomThreshold.addChangeListener(e1 -> {
			boolean selected = useCustomThreshold.getBooleanValue();
			predictionThreshold.setEnabled(selected);
		});

	}

	/**
	 * Save current settings into the given {@link NodeSettingsWO}
	 * 
	 * @param settings
	 */

	public void saveSettingsTo(NodeSettingsWO settings) {
		super.saveSettingsTo(settings);
		sentenceColumn.saveSettingsTo(settings);
		candidateLabels.saveSettingsTo(settings);
		useCustomHypothesis.saveSettingsTo(settings);
		appendProbabilities.saveSettingsTo(settings);
		hypothesis.saveSettingsTo(settings);
		multilabelClassification.saveSettingsTo(settings);
		useCustomThreshold.saveSettingsTo(settings);
		predictionThreshold.saveSettingsTo(settings);

	}

	/**
	 * Validates settings in the provided {@link NodeSettingsRO}
	 * 
	 * @param settings
	 * @throws InvalidSettingsException
	 */

	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		sentenceColumn.validateSettings(settings);
		candidateLabels.validateSettings(settings);
		useCustomHypothesis.validateSettings(settings);
		hypothesis.validateSettings(settings);
		appendProbabilities.validateSettings(settings);
		multilabelClassification.validateSettings(settings);
		useCustomThreshold.validateSettings(settings);
		predictionThreshold.validateSettings(settings);

		ZeroShotTextClassifierSettings temp = new ZeroShotTextClassifierSettings();
		temp.loadSettingsFrom(settings);
		temp.validate();

	}

	public void validate() throws InvalidSettingsException {
		// Invalid sentence column
		if (sentenceColumn.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Sentnece column is not selected");
		}
		// invalid candidate labels
		if (candidateLabels.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("At least one candidate label should be provided");
		}

	}

	public void validate(DataTableSpec spec) throws InvalidSettingsException {
		validate();
		String sc = sentenceColumn.getStringValue();
		if (!spec.containsName(sc)) {
			throw new InvalidSettingsException("Input table doesn't contain column:" + sc);

		}
	}

	/**
	 * Loads settings from the provided {@link NodeSettingsRO}
	 * 
	 * @param settings
	 * @throws InvalidSettingsException
	 */
	@Override
	public void loadSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		super.loadSettingsFrom(settings);
		sentenceColumn.loadSettingsFrom(settings);
		candidateLabels.loadSettingsFrom(settings);
		useCustomHypothesis.loadSettingsFrom(settings);
		hypothesis.loadSettingsFrom(settings);
		appendProbabilities.loadSettingsFrom(settings);
		predictionThreshold.loadSettingsFrom(settings);
		multilabelClassification.loadSettingsFrom(settings);
		useCustomThreshold.loadSettingsFrom(settings);

	}

	/**
	 * @return the sentence column model.
	 */
	public SettingsModelString getSentenceColumnModel() {
		return sentenceColumn;
	}

	/**
	 * @return the sentence column
	 */
	public String getSentenceColumn() {
		return sentenceColumn.getStringValue();
	}

	/**
	 * @return the batch size model
	 */
	public SettingsModelString getCandidateLabelsModel() {
		return candidateLabels;
	}

	public String getCandidateLabels() {
		return candidateLabels.getStringValue();
	}

	public SettingsModelBoolean getUseCustomHypothesisModel() {
		return useCustomHypothesis;
	}

	public Boolean getUseCustomHypothesis() {
		return useCustomHypothesis.getBooleanValue();
	}

	public SettingsModelString getHypothesisModel() {
		return hypothesis;
	}

	public String getHypothesis() {
		if (getUseCustomHypothesis())
			return hypothesis.getStringValue();
		else
			return DEFAULT_HYPOTHESIS;

	}

	public SettingsModelBoolean getAppendProbabilitiesModel() {
		return appendProbabilities;

	}

	public Boolean getAppendProbabilities() {
		return appendProbabilities.getBooleanValue();
	}

	public SettingsModelBoolean getMultilabelClassification() {
		return multilabelClassification;
	}

	public Boolean isMultilabelClassification() {
		return multilabelClassification.getBooleanValue();
	}

	public SettingsModelBoolean getUseCustomThresholdModel() {
		return useCustomThreshold;
	}

	/**
	 * @return whether user-defined probability threshold value should be used to
	 *         compute predicted classes
	 */
	public boolean getUseCustomThreshold() {
		return useCustomThreshold.getBooleanValue();
	}

	/**
	 * @return the predictionThreshold model.
	 */
	public SettingsModelDoubleBounded getPredictionThresholdModel() {
		return predictionThreshold;
	}

	/**
	 * @return the probability threshold value used to determine predicted classes
	 */
	public double getPredictionThreshold() {
		if (getUseCustomThreshold()) {
			return predictionThreshold.getDoubleValue();
		} else {
			return DEFAULT_PREDICTION_THRESHOLD;
		}
	}

}
