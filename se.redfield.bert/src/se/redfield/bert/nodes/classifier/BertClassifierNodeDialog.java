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
package se.redfield.bert.nodes.classifier;

import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;

import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.port.PortObjectSpec;

import se.redfield.bert.setting.BertClassifierSettings;
import se.redfield.bert.setting.ui.InputSettingsEditor;

/**
 * 
 * Dialog for the {@link BertClassifierNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertClassifierNodeDialog extends NodeDialogPane {

	private final BertClassifierSettings settings;

	private InputSettingsEditor inputSettings;
	private DialogComponentColumnNameSelection classColumn;

	/**
	 * Creates new instance
	 */
	public BertClassifierNodeDialog() {
		settings = new BertClassifierSettings();

		addTab("Settings", createSettingsPanel());
	}

	@SuppressWarnings("unchecked")
	private JComponent createSettingsPanel() {
		inputSettings = new InputSettingsEditor(settings.getInputSettings(), BertClassifierNodeModel.PORT_DATA_TABLE);

		classColumn = new DialogComponentColumnNameSelection(settings.getClassColumnModel(), "Class column",
				BertClassifierNodeModel.PORT_DATA_TABLE, StringValue.class);
		classColumn.getComponentPanel().setLayout(new FlowLayout(FlowLayout.LEFT));

		Box box = new Box(BoxLayout.Y_AXIS);
		box.add(inputSettings);
		box.add(classColumn.getComponentPanel());
		box.add(Box.createVerticalGlue());
		return box;
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
		try {
			this.settings.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			// ignore
		}

		inputSettings.loadSettings(settings, specs);
		classColumn.loadSettingsFrom(settings, specs);
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		this.settings.saveSettingsTo(settings);
	}

}
