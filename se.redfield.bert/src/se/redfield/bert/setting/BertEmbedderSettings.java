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

import se.redfield.bert.nodes.embedder.BertEmbedderNodeModel;

/**
 * Settings for the {@link BertEmbedderNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertEmbedderSettings {
	private static final String KEY_INPUT_SETTINGS = "input";

	private final InputSettings inputSettings;

	/**
	 * Creates new instance
	 */
	public BertEmbedderSettings() {
		inputSettings = new InputSettings();
	}

	/**
	 * Saves curent settings into the given {@link NodeSettingsWO}.
	 * 
	 * @param settings
	 */
	public void saveSettingsTo(NodeSettingsWO settings) {
		inputSettings.saveSettingsTo(settings.addNodeSettings(KEY_INPUT_SETTINGS));
	}

	/**
	 * Validates settings in the provided {@link NodeSettingsRO}.
	 * 
	 * @param settings
	 * @throws InvalidSettingsException
	 */
	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		inputSettings.validateSettings(settings.getNodeSettings(KEY_INPUT_SETTINGS));

		BertEmbedderSettings temp = new BertEmbedderSettings();
		temp.loadSettingsFrom(settings);
		temp.validate();
	}

	/**
	 * Validates internal consistency of the current settings.
	 * 
	 * @throws InvalidSettingsException
	 */
	public void validate() throws InvalidSettingsException {
		inputSettings.validate();
	}

	/**
	 * Validates the settings against input table spec.
	 * 
	 * @param spec Input table spec.
	 * @throws InvalidSettingsException
	 */
	public void validate(DataTableSpec spec) throws InvalidSettingsException {
		inputSettings.validate(spec);
	}

	/**
	 * Loads settings from the given {@link NodeSettingsRO} object.
	 * 
	 * @param settings
	 * @throws InvalidSettingsException
	 */
	public void loadSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		inputSettings.loadSettingsFrom(settings.getNodeSettings(KEY_INPUT_SETTINGS));
	}

	/**
	 * @return The input settings.
	 */
	public InputSettings getInputSettings() {
		return inputSettings;
	}
}
