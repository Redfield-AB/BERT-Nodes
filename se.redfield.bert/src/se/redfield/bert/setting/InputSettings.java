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

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class InputSettings {
	private static final String KEY_SENTENCE_COLUMN = "sentenceColumn";
	private static final String KEY_SECOND_SENTENCE_COLUMN = "secondSentenceColumn";
	private static final String KEY_TWO_SENTENCE_MODE = "twoSentenceMode";
	private static final String KEY_MAX_SEQ_LENGTH = "maxSeqLength";

	private final SettingsModelString sentenceColumn;
	private final SettingsModelString secondSentenceColumn;
	private final SettingsModelBoolean twoSentenceMode;
	private final SettingsModelIntegerBounded maxSeqLength;

	public InputSettings() {
		sentenceColumn = new SettingsModelString(KEY_SENTENCE_COLUMN, "");
		secondSentenceColumn = new SettingsModelString(KEY_SECOND_SENTENCE_COLUMN, "");
		twoSentenceMode = new SettingsModelBoolean(KEY_TWO_SENTENCE_MODE, false);
		maxSeqLength = new SettingsModelIntegerBounded(KEY_MAX_SEQ_LENGTH, 128, 3, 512);

		secondSentenceColumn.setEnabled(false);
		twoSentenceMode.addChangeListener(e -> {
			secondSentenceColumn.setEnabled(twoSentenceMode.getBooleanValue());
		});
	}

	public void saveSettingsTo(NodeSettingsWO settings) {
		sentenceColumn.saveSettingsTo(settings);
		secondSentenceColumn.saveSettingsTo(settings);
		twoSentenceMode.saveSettingsTo(settings);
		maxSeqLength.saveSettingsTo(settings);
	}

	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		sentenceColumn.validateSettings(settings);
		secondSentenceColumn.validateSettings(settings);
		twoSentenceMode.validateSettings(settings);
		maxSeqLength.validateSettings(settings);

		InputSettings temp = new InputSettings();
		temp.loadSettingsFrom(settings);
		temp.validate();
	}

	public void validate() throws InvalidSettingsException {
		if (sentenceColumn.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Sentence column is not selected");
		}
		if (twoSentenceMode.getBooleanValue() && secondSentenceColumn.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Second sentence column is not selected");
		}
	}

	public void loadSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		sentenceColumn.loadSettingsFrom(settings);
		secondSentenceColumn.loadSettingsFrom(settings);
		twoSentenceMode.loadSettingsFrom(settings);
		maxSeqLength.loadSettingsFrom(settings);
	}

	public SettingsModelString getSentenceColumnModel() {
		return sentenceColumn;
	}

	public String getSentenceColumn() {
		return sentenceColumn.getStringValue();
	}

	public SettingsModelString getSecondSentenceColumnModel() {
		return secondSentenceColumn;
	}

	public String getSecondSentenceColumn() {
		return secondSentenceColumn.getStringValue();
	}

	public SettingsModelBoolean getTwoSentenceModeModel() {
		return twoSentenceMode;
	}

	public boolean getTwoSentenceMode() {
		return twoSentenceMode.getBooleanValue();
	}

	public SettingsModelIntegerBounded getMaxSeqLengthModel() {
		return maxSeqLength;
	}

	public int getMaxSeqLength() {
		return maxSeqLength.getIntValue();
	}
}
