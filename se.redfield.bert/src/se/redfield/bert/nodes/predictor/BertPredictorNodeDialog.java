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
package se.redfield.bert.nodes.predictor;

import javax.swing.JLabel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ColumnSelectionPanel;
import org.knime.dl.base.nodes.AbstractGridBagDialogComponentGroup;

import se.redfield.bert.nodes.port.BertClassifierPortObjectSpec;
import se.redfield.bert.setting.BertPredictorSettings;
import se.redfield.bert.setting.ui.PythonNodeDialog;

/**
 * Dialog for the {@link BertPredictorNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertPredictorNodeDialog extends PythonNodeDialog<BertPredictorSettings> {

	private DialogComponentColumnNameSelection sentenceColumn;

	/**
	 * Creates new instance
	 */
	@SuppressWarnings("unchecked")
	public BertPredictorNodeDialog() {
		super(new BertPredictorSettings());

		sentenceColumn = new DialogComponentColumnNameSelection(settings.getSentenceColumnModel(), "Sentence column",
				BertPredictorNodeModel.PORT_DATA_TABLE, StringValue.class);

		addTab("Settings", new SettingsTabGroup().getComponentGroupPanel());
		addTab("Multi-label", new AdvancedTabGroup().getComponentGroupPanel());
		addPythonTab();
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
		super.loadSettingsFrom(settings, specs);
		try {
			this.settings.configure((DataTableSpec) specs[BertPredictorNodeModel.PORT_DATA_TABLE],
					(BertClassifierPortObjectSpec) specs[BertPredictorNodeModel.PORT_BERT_CLASSIFIER]);
		} catch (InvalidSettingsException e) {
			// ignore
		}

		sentenceColumn.loadSettingsFrom(settings, specs);
	}

	private class SettingsTabGroup extends AbstractGridBagDialogComponentGroup {
		public SettingsTabGroup() {
			addDoubleColumnRow(new JLabel("Sentence column"),
					getFirstComponent(sentenceColumn, ColumnSelectionPanel.class));
			addNumberSpinnerRowComponent(settings.getBatchSizeModel(), "Batch size", 1);
			addHorizontalSeparator();
			addCheckboxRow(settings.getChangePredictionColumnModel(), "Change prediction column name", true);
			addStringEditRowComponent(settings.getPredictionColumnModel(), "Prediction column name");
			addCheckboxRow(settings.getOutputProbabilitiesModel(), "Append individual class probabilities", true);
			addStringEditRowComponent(settings.getProbabilitiesColumnSuffixModel(), "Suffix for probability columns");
		}
	}

	private class AdvancedTabGroup extends AbstractGridBagDialogComponentGroup {
		public AdvancedTabGroup() {
			addCheckboxRow(settings.getUseCustomThreshouldModel(), "Use custom threshold for determining predictions",
					true);
			addNumberSpinnerRowComponent(settings.getPredictionThresholdModel(), "Probability threshold", 0.01);
			addCheckboxRow(settings.getFixNumberOfClassesModel(), "Fixed number of classes per prediction", true);
			addNumberSpinnerRowComponent(settings.getNumberOfClassesPerPredictionModel(),
					"Number of classes per prediction", 1);
			addHorizontalSeparator();
			addCheckboxRow(settings.getUseCustomClassSeparatorModel(), "Use custom class separator", true);
			addStringEditRowComponent(settings.getClassSeparatorModel(), "Class separator");
		}
	}
}
