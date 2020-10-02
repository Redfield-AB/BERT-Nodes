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
package se.redfield.bert.setting.ui;

import javax.swing.JLabel;

import org.knime.core.data.StringValue;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ColumnSelectionPanel;
import org.knime.dl.base.nodes.AbstractGridBagDialogComponentGroup;

import se.redfield.bert.setting.InputSettings;

/**
 * 
 * Editor component for the {@link InputSettings}.
 * 
 * @author Alexander Bondaletov
 *
 */
public class InputSettingsEditor extends AbstractGridBagDialogComponentGroup {

	private DialogComponentColumnNameSelection firstSentenceColumn;
	private DialogComponentColumnNameSelection secondSentenceColumn;

	/**
	 * Creates new instance.
	 * 
	 * @param settings  The settings object.
	 * @param specIndex Input data table spec index.
	 */
	@SuppressWarnings("unchecked")
	public InputSettingsEditor(InputSettings settings, int specIndex) {
		firstSentenceColumn = new DialogComponentColumnNameSelection(settings.getSentenceColumnModel(), "", specIndex,
				true, StringValue.class);
		secondSentenceColumn = new DialogComponentColumnNameSelection(settings.getSecondSentenceColumnModel(), "",
				specIndex, StringValue.class);

		addDoubleColumnRow(new JLabel("Sentence column"),
				getFirstComponent(firstSentenceColumn, ColumnSelectionPanel.class));
		addCheckboxRow(settings.getTwoSentenceModeModel(), "Two-sencence mode", true);
		addDoubleColumnRow(new JLabel("Second sentence column"),
				getFirstComponent(secondSentenceColumn, ColumnSelectionPanel.class));
		addNumberSpinnerRowComponent(settings.getMaxSeqLengthModel(), "Max sequence length", 1);
	}

	/**
	 * Initializes {@link DialogComponentColumnNameSelection} components with the
	 * input table spec
	 * 
	 * @param settings The settings object.
	 * @param specs    The input specs
	 * @throws NotConfigurableException
	 */
	public void loadSettings(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
		firstSentenceColumn.loadSettingsFrom(settings, specs);
		secondSentenceColumn.loadSettingsFrom(settings, specs);
	}
}
