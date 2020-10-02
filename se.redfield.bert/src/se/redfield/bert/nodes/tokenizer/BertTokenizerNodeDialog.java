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
package se.redfield.bert.nodes.tokenizer;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import se.redfield.bert.setting.BertTokenizerSettings;
import se.redfield.bert.setting.ui.InputSettingsEditor;

/**
 * 
 * Settings dialog for the {@link BertTokenizerNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertTokenizerNodeDialog extends NodeDialogPane {

	private final BertTokenizerSettings settings;
	private final InputSettingsEditor inputSettingsEditor;

	/**
	 * Creates new instance.
	 */
	public BertTokenizerNodeDialog() {
		settings = new BertTokenizerSettings();
		inputSettingsEditor = new InputSettingsEditor(settings.getInputSettings(),
				BertTokenizerNodeModel.PORT_INPUT_TABLE);

		addTab("Settings", inputSettingsEditor.getComponentGroupPanel());
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
		try {
			this.settings.loadSettings(settings);
		} catch (InvalidSettingsException e) {
			// ignore
		}
		inputSettingsEditor.loadSettings(settings, specs);
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		this.settings.saveSettingsTo(settings);
	}

}
