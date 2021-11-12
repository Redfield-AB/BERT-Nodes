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

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import se.redfield.bert.setting.BertTokenizerSettings;
import se.redfield.bert.setting.ui.InputSettingsEditor;
import se.redfield.bert.setting.ui.PythonNodeDialog;

/**
 * 
 * Settings dialog for the {@link BertTokenizerNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertTokenizerNodeDialog extends PythonNodeDialog<BertTokenizerSettings> {

	private final InputSettingsEditor inputSettingsEditor;

	/**
	 * Creates new instance.
	 */
	public BertTokenizerNodeDialog() {
		super(new BertTokenizerSettings());
		inputSettingsEditor = new InputSettingsEditor(settings.getInputSettings(),
				BertTokenizerNodeModel.PORT_INPUT_TABLE);

		addTab("Settings", inputSettingsEditor.getComponentGroupPanel());
		addPythonTab();
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
		super.loadSettingsFrom(settings, specs);
		inputSettingsEditor.loadSettings(settings, specs);
	}

}
