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
package se.redfield.bert.setting;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import se.redfield.bert.nodes.port.BertClassifierPortObject;
import se.redfield.bert.nodes.port.BertClassifierPortObjectSpec;
import se.redfield.bert.nodes.predictor.BertPredictorNodeModel;

/**
 * Settings for the {@link BertPredictorNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertPredictorSettings {
	private static final String KEY_SENTENCE_COLUMN = "sentenceColumn";
	private static final String KEY_BATCH_SIZE = "batchSize";
	private static final String KEY_CHANGE_PREDICTION_COLUMN = "changePredictionColumn";
	private static final String KEY_PREDICTION_COLUMN = "predictionColumn";
	private static final String KEY_OUTPUT_PROBABILITIES = "outputProbabilities";
	private static final String KEY_PROBABILITIES_COLUMN_SUFFIX = "probabilitiesColumnSuffix";
	private static final String KEY_USE_CUSTOM_THRESHOLD = "useCustomThreshold";
	private static final String KEY_PREDICTION_THRESHOLD = "predictionThreshold";
	private static final String KEY_FIX_NUMBER_OF_CLASSES = "fixNumberOfClasses";
	private static final String KEY_NUMBER_OF_PREDICTED_CLASSES = "numberOfPredictedClasses";
	private static final String KEY_USE_CUSTOM_CLASS_SEPARATOR = "useCustomClassSeparator";
	private static final String KEY_CLASS_SEPARATOR = "classSeparator";

	private static final String DEFAULT_PRECICTION_COLUMN = "Prediction";
	private static final double DEFAULT_PREDICTION_THRESHOLD = 0.5;

	private final SettingsModelString sentenceColumn;
	private final SettingsModelIntegerBounded batchSize;
	private final SettingsModelBoolean changePredictionColumn;
	private final SettingsModelString predictionColumn;
	private final SettingsModelBoolean outputProbabilities;
	private final SettingsModelString probabilitiesColumnSuffix;
	private final SettingsModelBoolean useCustomThreshould;
	private final SettingsModelDoubleBounded predictionThreshold;
	private final SettingsModelBoolean fixNumberOfClasses;
	private final SettingsModelIntegerBounded numberOfClassesPerPrediction;
	private final SettingsModelBoolean useCustomClassSeparator;
	private final SettingsModelString classSeparator;

	/**
	 * Creates new instance.
	 */
	public BertPredictorSettings() {
		sentenceColumn = new SettingsModelString(KEY_SENTENCE_COLUMN, "");
		batchSize = new SettingsModelIntegerBounded(KEY_BATCH_SIZE, 20, 1, Integer.MAX_VALUE);
		changePredictionColumn = new SettingsModelBoolean(KEY_CHANGE_PREDICTION_COLUMN, false);
		predictionColumn = new SettingsModelString(KEY_PREDICTION_COLUMN, DEFAULT_PRECICTION_COLUMN);
		outputProbabilities = new SettingsModelBoolean(KEY_OUTPUT_PROBABILITIES, true);
		probabilitiesColumnSuffix = new SettingsModelString(KEY_PROBABILITIES_COLUMN_SUFFIX, "");
		useCustomThreshould = new SettingsModelBoolean(KEY_USE_CUSTOM_THRESHOLD, false);
		predictionThreshold = new SettingsModelDoubleBounded(KEY_PREDICTION_THRESHOLD, DEFAULT_PREDICTION_THRESHOLD, 0,
				1);
		fixNumberOfClasses = new SettingsModelBoolean(KEY_FIX_NUMBER_OF_CLASSES, false);
		numberOfClassesPerPrediction = new SettingsModelIntegerBounded(KEY_NUMBER_OF_PREDICTED_CLASSES, 2, 0,
				Integer.MAX_VALUE);
		useCustomClassSeparator = new SettingsModelBoolean(KEY_USE_CUSTOM_CLASS_SEPARATOR, false);
		classSeparator = new SettingsModelString(KEY_CLASS_SEPARATOR, BertClassifierSettings.DEFAULT_CLASS_SEPARATOR);

		predictionColumn.setEnabled(changePredictionColumn.getBooleanValue());
		probabilitiesColumnSuffix.setEnabled(outputProbabilities.getBooleanValue());
		predictionThreshold.setEnabled(useCustomThreshould.getBooleanValue());
		numberOfClassesPerPrediction.setEnabled(false);
		classSeparator.setEnabled(false);

		changePredictionColumn.addChangeListener(e -> {
			predictionColumn.setEnabled(changePredictionColumn.getBooleanValue());
		});
		outputProbabilities.addChangeListener(e -> {
			probabilitiesColumnSuffix.setEnabled(outputProbabilities.getBooleanValue());
		});

		useCustomThreshould.addChangeListener(e -> {
			boolean selected = useCustomThreshould.getBooleanValue();
			predictionThreshold.setEnabled(selected);
			if (selected) {
				fixNumberOfClasses.setBooleanValue(false);
			}
		});
		fixNumberOfClasses.addChangeListener(e -> {
			boolean selected = fixNumberOfClasses.getBooleanValue();
			numberOfClassesPerPrediction.setEnabled(selected);
			if (selected) {
				useCustomThreshould.setBooleanValue(false);
			}
		});

		useCustomClassSeparator.addChangeListener(e -> {
			classSeparator.setEnabled(useCustomClassSeparator.getBooleanValue());
		});
	}

	/**
	 * Saves current settings into the given {@link NodeSettingsWO}.
	 * 
	 * @param settings
	 */
	public void saveSettingsTo(NodeSettingsWO settings) {
		sentenceColumn.saveSettingsTo(settings);
		batchSize.saveSettingsTo(settings);
		changePredictionColumn.saveSettingsTo(settings);
		predictionColumn.saveSettingsTo(settings);
		outputProbabilities.saveSettingsTo(settings);
		probabilitiesColumnSuffix.saveSettingsTo(settings);
		useCustomThreshould.saveSettingsTo(settings);
		predictionThreshold.saveSettingsTo(settings);
		useCustomClassSeparator.saveSettingsTo(settings);
		classSeparator.saveSettingsTo(settings);
		fixNumberOfClasses.saveSettingsTo(settings);
		numberOfClassesPerPrediction.saveSettingsTo(settings);
	}

	/**
	 * Validates settings in the provided {@link NodeSettingsRO}.
	 * 
	 * @param settings
	 * @throws InvalidSettingsException
	 */
	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		sentenceColumn.validateSettings(settings);
		batchSize.validateSettings(settings);
		changePredictionColumn.validateSettings(settings);
		predictionColumn.validateSettings(settings);
		outputProbabilities.validateSettings(settings);
		probabilitiesColumnSuffix.validateSettings(settings);
		useCustomThreshould.validateSettings(settings);
		predictionThreshold.validateSettings(settings);
		fixNumberOfClasses.validateSettings(settings);
		numberOfClassesPerPrediction.validateSettings(settings);
		useCustomClassSeparator.validateSettings(settings);
		classSeparator.validateSettings(settings);

		BertPredictorSettings temp = new BertPredictorSettings();
		temp.loadSettingsFrom(settings);
		temp.validate();
	}

	/**
	 * Validates internal consistency of the current settings
	 * 
	 * @throws InvalidSettingsException
	 */
	public void validate() throws InvalidSettingsException {
		if (sentenceColumn.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Sentence column is not selected");
		}
		if (changePredictionColumn.getBooleanValue() && predictionColumn.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Prediction column name is empty");
		}
	}

	/**
	 * Configures and validates the settings against input table spec and
	 * {@link BertClassifierPortObject}.
	 * 
	 * @param spec       Input table spec.
	 * @param classifier Classifier object spec.
	 * @throws InvalidSettingsException
	 */
	public void configure(DataTableSpec spec, BertClassifierPortObjectSpec classifier) throws InvalidSettingsException {
		validate(spec);

		if (classifier.isMultiLabel() && !getUseCustomClassSeparator()) {
			classSeparator.setStringValue(classifier.getClassSeparator());
		}

		if (!classifier.isMultiLabel()) {
			fixNumberOfClasses.setBooleanValue(false);
			useCustomThreshould.setBooleanValue(false);
			useCustomClassSeparator.setBooleanValue(false);

			fixNumberOfClasses.setEnabled(false);
			useCustomThreshould.setEnabled(false);
			useCustomClassSeparator.setEnabled(false);
		}
	}

	private void validate(DataTableSpec spec) throws InvalidSettingsException {
		validate();

		String sc = sentenceColumn.getStringValue();
		if (!spec.containsName(sc)) {
			throw new InvalidSettingsException("Input table doesn't contain column: " + sc);
		}
	}

	/**
	 * Loads settings from the provided {@link NodeSettingsRO}
	 * 
	 * @param settings
	 * @throws InvalidSettingsException
	 */
	public void loadSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		sentenceColumn.loadSettingsFrom(settings);
		batchSize.loadSettingsFrom(settings);
		predictionColumn.loadSettingsFrom(settings);
		probabilitiesColumnSuffix.loadSettingsFrom(settings);
		changePredictionColumn.loadSettingsFrom(settings);
		outputProbabilities.loadSettingsFrom(settings);
		predictionThreshold.loadSettingsFrom(settings);
		useCustomThreshould.loadSettingsFrom(settings);
		numberOfClassesPerPrediction.loadSettingsFrom(settings);
		fixNumberOfClasses.loadSettingsFrom(settings);
		classSeparator.loadSettingsFrom(settings);
		useCustomClassSeparator.loadSettingsFrom(settings);
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
	public SettingsModelIntegerBounded getBatchSizeModel() {
		return batchSize;
	}

	/**
	 * @return the batch size
	 */
	public int getBatchSize() {
		return batchSize.getIntValue();
	}

	/**
	 * @return the changePredictionColumn model.
	 */
	public SettingsModelBoolean getChangePredictionColumnModel() {
		return changePredictionColumn;
	}

	/**
	 * @return <code>true</code> if the default prediction column name is supposed
	 *         to be changed, <code>false</code> otherwise.
	 */
	public boolean getChangePredictionColumn() {
		return changePredictionColumn.getBooleanValue();
	}

	/**
	 * @return the predictionColumn model.
	 */
	public SettingsModelString getPredictionColumnModel() {
		return predictionColumn;
	}

	/**
	 * @return the prediction column name (either default or entered by user
	 *         depending on the settings).
	 */
	public String getPredictionColumn() {
		if (getChangePredictionColumn()) {
			return predictionColumn.getStringValue();
		}
		return DEFAULT_PRECICTION_COLUMN;
	}

	/**
	 * @return the outputProbabilities model.
	 */
	public SettingsModelBoolean getOutputProbabilitiesModel() {
		return outputProbabilities;
	}

	/**
	 * @return <code>true</code> if probabilities columns should be added to output
	 *         table, <code>false</code> otherwise.
	 */
	public boolean getOutputProbabilities() {
		return outputProbabilities.getBooleanValue();
	}

	/**
	 * @return the probabilitiesColumnSuffix model.
	 */
	public SettingsModelString getProbabilitiesColumnSuffixModel() {
		return probabilitiesColumnSuffix;
	}

	/**
	 * @return the suffix to be added to the probabilities columns names.
	 */
	public String getProbabilitiesColumnSuffix() {
		return probabilitiesColumnSuffix.getStringValue();
	}

	/**
	 * @return the useCustomThreshold model.
	 */
	public SettingsModelBoolean getUseCustomThreshouldModel() {
		return useCustomThreshould;
	}

	/**
	 * @return whether user-defined probability threshold value should be used to
	 *         compute predicted classes
	 */
	public boolean getUseCustomThreshould() {
		return useCustomThreshould.getBooleanValue();
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
		if (getUseCustomThreshould()) {
			return predictionThreshold.getDoubleValue();
		} else {
			return DEFAULT_PREDICTION_THRESHOLD;
		}
	}

	/**
	 * @return the fixNumberOfClasses model.
	 */
	public SettingsModelBoolean getFixNumberOfClassesModel() {
		return fixNumberOfClasses;
	}

	/**
	 * @return whether each prediction should be consist of a fixed number of
	 *         predicted classes per row.
	 */
	public boolean getFixNumberOfClasses() {
		return fixNumberOfClasses.getBooleanValue();
	}

	/**
	 * @return the numberOfClassesPerPrediction model.
	 */
	public SettingsModelIntegerBounded getNumberOfClassesPerPredictionModel() {
		return numberOfClassesPerPrediction;
	}

	/**
	 * @return the number of classes per prediction in case 'fixed number of
	 *         classes' mode is used.
	 */
	public int getNumberOfClassesPerPrediction() {
		return numberOfClassesPerPrediction.getIntValue();
	}

	/**
	 * @return the customClassSeparator model.
	 */
	public SettingsModelBoolean getUseCustomClassSeparatorModel() {
		return useCustomClassSeparator;
	}

	/**
	 * @return whether the custom class separator character is used
	 */
	public boolean getUseCustomClassSeparator() {
		return useCustomClassSeparator.getBooleanValue();
	}

	/**
	 * @return the classSeparator model.
	 */
	public SettingsModelString getClassSeparatorModel() {
		return classSeparator;
	}

	/**
	 * @return the separator character to be used in predictions consists of
	 *         multiple classes.
	 */
	public String getClassSeparator() {
		return classSeparator.getStringValue();
	}
}
