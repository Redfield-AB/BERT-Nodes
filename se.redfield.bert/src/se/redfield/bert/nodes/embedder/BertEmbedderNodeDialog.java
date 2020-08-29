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
package se.redfield.bert.nodes.embedder;

import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.port.PortObjectSpec;

import se.redfield.bert.setting.BertEmbedderSettings;
import se.redfield.bert.setting.ui.InputSettingsEditor;

/**
 * 
 * Dialog for {@link BertEmbedderNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertEmbedderNodeDialog extends NodeDialogPane {
	private final BertEmbedderSettings settings;
	private InputSettingsEditor inputSettings;

	/**
	 * Creates new instance.
	 */
	public BertEmbedderNodeDialog() {
		settings = new BertEmbedderSettings();

		addTab("Settings", createSettingsPanel());
	}

	private JComponent createSettingsPanel() {
		DialogComponentNumber batchSize = new DialogComponentNumber(settings.getBatchSizeModel(), "Batch size", 1);
		batchSize.getComponentPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
		DialogComponentBoolean includeSeqEmbeddings = new DialogComponentBoolean(
				settings.getIncludeSeqEmbeddingsModel(), "Include sequence embeddings");
		includeSeqEmbeddings.getComponentPanel().setLayout(new FlowLayout(FlowLayout.LEFT));

		inputSettings = new InputSettingsEditor(settings.getInputSettings(), BertEmbedderNodeModel.PORT_DATA_TABLE);

		Box box = new Box(BoxLayout.Y_AXIS);
		box.add(inputSettings);
		box.add(batchSize.getComponentPanel());
		box.add(includeSeqEmbeddings.getComponentPanel());
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
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		this.settings.saveSettingsTo(settings);
	}
}
