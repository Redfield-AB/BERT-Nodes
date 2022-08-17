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

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import se.redfield.bert.nodes.zstc.ZeroShotTextClassifierNodeModel;

/**
 * Settings for {@link ZeroShotTextClassifierNodeModel} node.
 * 
 * @author Abderrahim Alakouche.
 * 
 */

public class ZeroShotTextClassifierSettings extends BertPredictorSettings {
	private static final String CANDIDATE_LABELS = "candidateLabels";
	private static final String KEY_USE_CUSTOM_HYPOTHESIS = "useCustomHypothesis";
	private static final String KEY_HYPOTHESIS = "hypothesis";
	private static final String KEY_MULTILABEL_CLASSIFICATION = "multilabelClassification";
	private static final String DEFAULT_HYPOTHESIS = "This example is {}";

	private final SettingsModelString candidateLabels;
	private final SettingsModelBoolean useCustomHypothesis;
	private final SettingsModelString hypothesis;
	private final SettingsModelBoolean multilabelClassification;

	/**
	 * Create a new instance.
	 */
	public ZeroShotTextClassifierSettings() {
		candidateLabels = new SettingsModelString(CANDIDATE_LABELS, "");
		useCustomHypothesis = new SettingsModelBoolean(KEY_USE_CUSTOM_HYPOTHESIS, false);
		hypothesis = new SettingsModelString(KEY_HYPOTHESIS, DEFAULT_HYPOTHESIS);
		multilabelClassification = new SettingsModelBoolean(KEY_MULTILABEL_CLASSIFICATION, false);

		hypothesis.setEnabled(false);

		useCustomHypothesis.addChangeListener(e -> hypothesis.setEnabled(useCustomHypothesis.getBooleanValue()));

		multilabelClassification.addChangeListener(e -> {
			boolean multiLabel = multilabelClassification.getBooleanValue();

			if (!multiLabel) {
				fixNumberOfClasses.setBooleanValue(false);
				useCustomThreshould.setBooleanValue(false);
			}

			fixNumberOfClasses.setEnabled(multiLabel);
			useCustomThreshould.setEnabled(multiLabel);
		});

		fixNumberOfClasses.setEnabled(false);
		useCustomThreshould.setEnabled(false);
	}

	/**
	 * Save current settings into the given {@link NodeSettingsWO}
	 * 
	 * @param settings
	 */

	@Override
	public void saveSettingsTo(NodeSettingsWO settings) {
		super.saveSettingsTo(settings);
		candidateLabels.saveSettingsTo(settings);
		useCustomHypothesis.saveSettingsTo(settings);
		hypothesis.saveSettingsTo(settings);
		multilabelClassification.saveSettingsTo(settings);
	}

	/**
	 * Validates settings in the provided {@link NodeSettingsRO}
	 * 
	 * @param settings
	 * @throws InvalidSettingsException
	 */

	@Override
	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		ZeroShotTextClassifierSettings temp = new ZeroShotTextClassifierSettings();
		temp.loadSettingsFrom(settings);
		temp.validate();

	}

	@Override
	public void validate() throws InvalidSettingsException {
		super.validate();
		// invalid candidate labels
		if (candidateLabels.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("At least one candidate label should be provided");
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
		candidateLabels.loadSettingsFrom(settings);
		useCustomHypothesis.loadSettingsFrom(settings);
		hypothesis.loadSettingsFrom(settings);
		multilabelClassification.loadSettingsFrom(settings);
	}

	/**
	 * @return the batch size model
	 */
	public SettingsModelString getCandidateLabelsModel() {
		return candidateLabels;
	}

	/**
	 * @return The candidate labels.
	 */
	public String[] getCandidateLabels() {
		return Stream.of(candidateLabels.getStringValue().split(getClassSeparator()))//
				.map(String::trim)//
				.filter(s -> !s.isEmpty())//
				.collect(Collectors.toList()).toArray(new String[] {});
	}

	/**
	 * @return The useCustomHypotheses model.
	 */
	public SettingsModelBoolean getUseCustomHypothesisModel() {
		return useCustomHypothesis;
	}

	public boolean getUseCustomHypothesis() {
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

	public SettingsModelBoolean getMultilabelClassification() {
		return multilabelClassification;
	}

	public Boolean isMultilabelClassification() {
		return multilabelClassification.getBooleanValue();
	}

}
