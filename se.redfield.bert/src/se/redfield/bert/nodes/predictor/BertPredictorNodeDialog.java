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

import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ColumnSelectionPanel;
import org.knime.dl.base.nodes.AbstractGridBagDialogComponentGroup;

import se.redfield.bert.setting.BertPredictorSettings;

/**
 * Dialog for the {@link BertPredictorNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertPredictorNodeDialog extends NodeDialogPane {

	private final BertPredictorSettings settings;

	private DialogComponentColumnNameSelection sentenceColumn;

	/**
	 * Creates new instance
	 */
	@SuppressWarnings("unchecked")
	public BertPredictorNodeDialog() {
		settings = new BertPredictorSettings();
		sentenceColumn = new DialogComponentColumnNameSelection(settings.getSentenceColumnModel(), "Sentence column",
				BertPredictorNodeModel.PORT_DATA_TABLE, StringValue.class);

		addTab("Settings", new SettingsTabGroup().getComponentGroupPanel());
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
		try {
			this.settings.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			// ignore
		}

		sentenceColumn.loadSettingsFrom(settings, specs);
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		this.settings.saveSettingsTo(settings);
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
}
