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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;

import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.port.PortObjectSpec;

import se.redfield.bert.setting.BertClassifierSettings;
import se.redfield.bert.setting.ui.OptimizerSettingsEditor;

/**
 * 
 * Dialog for the {@link BertClassifierNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertClassifierNodeDialog extends NodeDialogPane {

	private final BertClassifierSettings settings;

	private DialogComponentColumnNameSelection sentenceColumn;
	private DialogComponentColumnNameSelection classColumn;
	private OptimizerSettingsEditor optimizer;

	/**
	 * Creates new instance
	 */
	public BertClassifierNodeDialog() {
		settings = new BertClassifierSettings();

		addTab("Settings", createSettingsTab());
		addTab("Advanced", createAdvancedSettingsTab());
	}

	private JComponent createSettingsTab() {
		return createInputSettingsPanel();
	}

	@SuppressWarnings("unchecked")
	private JComponent createInputSettingsPanel() {
		sentenceColumn = new DialogComponentColumnNameSelection(settings.getSentenceColumnModel(), "Sentence column",
				BertClassifierNodeModel.PORT_DATA_TABLE, StringValue.class);
		classColumn = new DialogComponentColumnNameSelection(settings.getClassColumnModel(), "Class column",
				BertClassifierNodeModel.PORT_DATA_TABLE, StringValue.class);
		DialogComponentNumber maxSeqLength = new DialogComponentNumber(settings.getMaxSeqLengthModel(),
				"Max sequence length", 1);

		sentenceColumn.getComponentPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
		classColumn.getComponentPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
		maxSeqLength.getComponentPanel().setLayout(new FlowLayout(FlowLayout.LEFT));

		Box box = new Box(BoxLayout.Y_AXIS);
		box.add(sentenceColumn.getComponentPanel());
		box.add(classColumn.getComponentPanel());
		box.add(maxSeqLength.getComponentPanel());
		return box;
	}

	private JComponent createAdvancedSettingsTab() {
		optimizer = new OptimizerSettingsEditor(settings.getOptimizerSettings());
		optimizer.setBorder(BorderFactory.createTitledBorder("Optimizer"));

		Box box = new Box((BoxLayout.Y_AXIS));
		box.add(createTrainingSettingsPanel());
		box.add(optimizer);
		return box;
	}

	private JComponent createTrainingSettingsPanel() {
		DialogComponentNumber epochs = new DialogComponentNumber(settings.getEpochsModel(), "Numbert of epochs", 1);
		DialogComponentNumber batchSize = new DialogComponentNumber(settings.getBatchSizeModel(), "Batch size", 1);
		DialogComponentBoolean fineTuneBert = new DialogComponentBoolean(settings.getFineTuneBertModel(),
				"Fine tune BERT");

		epochs.getComponentPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
		batchSize.getComponentPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
		fineTuneBert.getComponentPanel().setLayout(new FlowLayout(FlowLayout.LEFT));

		Box box = new Box(BoxLayout.Y_AXIS);
		box.add(epochs.getComponentPanel());
		box.add(batchSize.getComponentPanel());
		box.add(fineTuneBert.getComponentPanel());
		box.setBorder(BorderFactory.createTitledBorder("Training settings"));
		return box;
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
		try {
			this.settings.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			// ignore
		}

		sentenceColumn.loadSettingsFrom(settings, specs);
		classColumn.loadSettingsFrom(settings, specs);
		optimizer.settingsLoaded();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		this.settings.saveSettingsTo(settings);
	}

}
