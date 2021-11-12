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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.knime.core.data.StringValue;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ColumnSelectionPanel;
import org.knime.dl.base.nodes.AbstractGridBagDialogComponentGroup;

import se.redfield.bert.setting.BertClassifierSettings;
import se.redfield.bert.setting.ui.OptimizerSettingsEditor;
import se.redfield.bert.setting.ui.PythonNodeDialog;

/**
 * 
 * Dialog for the {@link BertClassifierNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertClassifierNodeDialog extends PythonNodeDialog<BertClassifierSettings> {

	private DialogComponentColumnNameSelection sentenceColumn;
	private DialogComponentColumnNameSelection classColumn;
	private OptimizerSettingsEditor optimizer;

	/**
	 * Creates new instance
	 */
	@SuppressWarnings("unchecked")
	public BertClassifierNodeDialog() {
		super(new BertClassifierSettings());

		sentenceColumn = new DialogComponentColumnNameSelection(settings.getSentenceColumnModel(), "Sentence column",
				BertClassifierNodeModel.PORT_DATA_TABLE, StringValue.class);
		classColumn = new DialogComponentColumnNameSelection(settings.getClassColumnModel(), "Class column",
				BertClassifierNodeModel.PORT_DATA_TABLE, StringValue.class);

		addTab("Settings", new SettingsTabGroup().getComponentGroupPanel());
		addTab("Advanced", createAdvancedSettingsTab());
		addPythonTab();
	}

	private JComponent createAdvancedSettingsTab() {
		optimizer = new OptimizerSettingsEditor(settings.getOptimizerSettings());
		optimizer.setBorder(BorderFactory.createTitledBorder("Optimizer"));

		Box box = new Box((BoxLayout.Y_AXIS));
		box.add(new TrainingSettingsGroup().getComponentGroupPanel());
		box.add(optimizer);
		return box;
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
		super.loadSettingsFrom(settings, specs);

		sentenceColumn.loadSettingsFrom(settings, specs);
		classColumn.loadSettingsFrom(settings, specs);
		optimizer.settingsLoaded();

		this.settings.getValidationBatchSizeModel()
				.setEnabled(specs[BertClassifierNodeModel.PORT_VALIDATION_TABLE] != null);
	}

	private class SettingsTabGroup extends AbstractGridBagDialogComponentGroup {
		public SettingsTabGroup() {
			addDoubleColumnRow(new JLabel("Sentence column"),
					getFirstComponent(sentenceColumn, ColumnSelectionPanel.class));
			addDoubleColumnRow(new JLabel("Class column"), getFirstComponent(classColumn, ColumnSelectionPanel.class));
			addNumberSpinnerRowComponent(settings.getMaxSeqLengthModel(), "Max sequence length", 1);
		}
	}

	private class TrainingSettingsGroup extends AbstractGridBagDialogComponentGroup {
		public TrainingSettingsGroup() {
			addNumberSpinnerRowComponent(settings.getEpochsModel(), "Number of epochs", 1);
			addNumberSpinnerRowComponent(settings.getBatchSizeModel(), "Batch size", 1);
			addNumberSpinnerRowComponent(settings.getValidationBatchSizeModel(), "Validation batch size", 1);
			addCheckboxRow(settings.getFineTuneBertModel(), "Fine tune BERT", true);
			getComponentGroupPanel().setBorder(BorderFactory.createTitledBorder("Training settings"));
		}
	}
}
