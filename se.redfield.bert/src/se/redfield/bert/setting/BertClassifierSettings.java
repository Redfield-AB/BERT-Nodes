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
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import se.redfield.bert.nodes.classifier.BertClassifierNodeModel;

/**
 * 
 * Settings for the {@link BertClassifierNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertClassifierSettings {
	private static final String KEY_SENTENCE_COLUMN = "sentenceColumn";
	private static final String KEY_MAX_SEQ_LENGTH = "maxSeqLength";
	private static final String KEY_CLASS_COLUMN = "classColumn";
	private static final String KEY_EPOCHS = "epochs";
	private static final String KEY_BATCH_SIZE = "batchSize";
	private static final String KEY_FINE_TUNE_BERT = "fineTuneBert";

	private final SettingsModelString sentenceColumn;
	private final SettingsModelIntegerBounded maxSeqLength;
	private final SettingsModelString classColumn;
	private final SettingsModelIntegerBounded epochs;
	private final SettingsModelIntegerBounded batchSize;
	private final SettingsModelBoolean fineTuneBert;

	/**
	 * Creates new instance
	 */
	public BertClassifierSettings() {
		sentenceColumn = new SettingsModelString(KEY_SENTENCE_COLUMN, "");
		maxSeqLength = new SettingsModelIntegerBounded(KEY_MAX_SEQ_LENGTH, 128, 3, 512);
		classColumn = new SettingsModelString(KEY_CLASS_COLUMN, "");
		epochs = new SettingsModelIntegerBounded(KEY_EPOCHS, 1, 1, Integer.MAX_VALUE);
		batchSize = new SettingsModelIntegerBounded(KEY_BATCH_SIZE, 20, 1, Integer.MAX_VALUE);
		fineTuneBert = new SettingsModelBoolean(KEY_FINE_TUNE_BERT, false);
	}

	/**
	 * Saves current settings into the given {@link NodeSettingsWO}.
	 * 
	 * @param settings
	 */
	public void saveSettingsTo(NodeSettingsWO settings) {
		sentenceColumn.saveSettingsTo(settings);
		maxSeqLength.saveSettingsTo(settings);
		classColumn.saveSettingsTo(settings);
		epochs.saveSettingsTo(settings);
		batchSize.saveSettingsTo(settings);
		fineTuneBert.saveSettingsTo(settings);
	}

	/**
	 * Validates settings in the provided {@link NodeSettingsRO}.
	 * 
	 * @param settings
	 * @throws InvalidSettingsException
	 */
	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		sentenceColumn.validateSettings(settings);
		maxSeqLength.validateSettings(settings);
		classColumn.validateSettings(settings);
		epochs.validateSettings(settings);
		batchSize.validateSettings(settings);
		fineTuneBert.validateSettings(settings);

		BertClassifierSettings temp = new BertClassifierSettings();
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

		if (classColumn.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Class column is not selected");
		}
	}

	/**
	 * Validates the settings against input table spec.
	 * 
	 * @param spec Input table spec.
	 * @throws InvalidSettingsException
	 */
	public void validate(DataTableSpec spec) throws InvalidSettingsException {
		validate();

		String sc = sentenceColumn.getStringValue();
		if (!spec.containsName(sc)) {
			throw new InvalidSettingsException("Input table doesn't contain column: " + sc);
		}

		String cc = classColumn.getStringValue();
		if (!spec.containsName(cc)) {
			throw new InvalidSettingsException("Input table doesn't contain column: " + cc);
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
		maxSeqLength.loadSettingsFrom(settings);
		classColumn.loadSettingsFrom(settings);
		epochs.loadSettingsFrom(settings);
		batchSize.loadSettingsFrom(settings);
		fineTuneBert.loadSettingsFrom(settings);
	}

	/**
	 * @return the sentenceColumn model.
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
	 * @return the maxSeqLenght model.
	 */
	public SettingsModelIntegerBounded getMaxSeqLengthModel() {
		return maxSeqLength;
	}

	/**
	 * @return the max sequence length
	 */
	public int getMaxSeqLength() {
		return maxSeqLength.getIntValue();
	}

	/**
	 * @return the classColumn model
	 */
	public SettingsModelString getClassColumnModel() {
		return classColumn;
	}

	/**
	 * @return the class column
	 */
	public String getClassColumn() {
		return classColumn.getStringValue();
	}

	/**
	 * @return the epochs model
	 */
	public SettingsModelIntegerBounded getEpochsModel() {
		return epochs;
	}

	/**
	 * @return the number of training epochs
	 */
	public int getEpochs() {
		return epochs.getIntValue();
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
	 * @return the fineTuneBert model
	 */
	public SettingsModelBoolean getFineTuneBertModel() {
		return fineTuneBert;
	}

	/**
	 * @return the fine tune BERT option
	 */
	public boolean getFineTuneBert() {
		return fineTuneBert.getBooleanValue();
	}
}
