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
	private static final String KEY_INPUT_SETTINGS = "input";
	private static final String KEY_CLASS_COLUMN = "classColumn";

	private final InputSettings inputSettings;
	private final SettingsModelString classColumn;

	/**
	 * Creates new instance
	 */
	public BertClassifierSettings() {
		inputSettings = new InputSettings();
		classColumn = new SettingsModelString(KEY_CLASS_COLUMN, "");
	}

	/**
	 * Saves current settings into the given {@link NodeSettingsWO}.
	 * 
	 * @param settings
	 */
	public void saveSettingsTo(NodeSettingsWO settings) {
		inputSettings.saveSettingsTo(settings.addNodeSettings(KEY_INPUT_SETTINGS));
		classColumn.saveSettingsTo(settings);
	}

	/**
	 * Validates settings in the provided {@link NodeSettingsRO}.
	 * 
	 * @param settings
	 * @throws InvalidSettingsException
	 */
	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		inputSettings.validateSettings(settings.getNodeSettings(KEY_INPUT_SETTINGS));
		classColumn.validateSettings(settings);

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
		inputSettings.validate();

		if (classColumn.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Class column is not selected");
		}
	}

	/**
	 * Loads settings from the provided {@link NodeSettingsRO}
	 * 
	 * @param settings
	 * @throws InvalidSettingsException
	 */
	public void loadSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		inputSettings.loadSettingsFrom(settings.getNodeSettings(KEY_INPUT_SETTINGS));
		classColumn.loadSettingsFrom(settings);
	}

	/**
	 * @return the input settings
	 */
	public InputSettings getInputSettings() {
		return inputSettings;
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
}
