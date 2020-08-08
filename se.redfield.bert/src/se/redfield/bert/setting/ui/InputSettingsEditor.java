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

import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.knime.core.data.StringValue;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.port.PortObjectSpec;

import se.redfield.bert.setting.InputSettings;

/**
 * 
 * Editor component for the {@link InputSettings}.
 * 
 * @author Alexander Bondaletov
 *
 */
public class InputSettingsEditor extends JPanel {
	private static final long serialVersionUID = 1L;

	private final InputSettings settings;
	private final int specIndex;

	private DialogComponentColumnNameSelection firstSentenceColumn;
	private DialogComponentColumnNameSelection secondSentenceColumn;

	/**
	 * Creates new instance.
	 * 
	 * @param settings  The settings object.
	 * @param specIndex Input data table spec index.
	 */
	public InputSettingsEditor(InputSettings settings, int specIndex) {
		this.settings = settings;
		this.specIndex = specIndex;
		iniUI();
	}

	@SuppressWarnings("unchecked")
	private void iniUI() {
		firstSentenceColumn = new DialogComponentColumnNameSelection(settings.getSentenceColumnModel(),
				"Sentence column", specIndex, true, StringValue.class);
		secondSentenceColumn = new DialogComponentColumnNameSelection(settings.getSecondSentenceColumnModel(),
				"Second sentence column", specIndex, StringValue.class);
		DialogComponentBoolean twoSentenceMode = new DialogComponentBoolean(settings.getTwoSentenceModeModel(),
				"Two-sentence mode");
		DialogComponentNumber maxSeqLenght = new DialogComponentNumber(settings.getMaxSeqLengthModel(),
				"Max sequence length", 1);

		firstSentenceColumn.getComponentPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
		secondSentenceColumn.getComponentPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
		twoSentenceMode.getComponentPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
		maxSeqLenght.getComponentPanel().setLayout(new FlowLayout(FlowLayout.LEFT));

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(firstSentenceColumn.getComponentPanel());
		add(twoSentenceMode.getComponentPanel());
		add(secondSentenceColumn.getComponentPanel());
		add(maxSeqLenght.getComponentPanel());
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
