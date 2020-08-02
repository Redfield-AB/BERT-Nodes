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

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentString;

import se.redfield.bert.setting.BertModelSelectorSettings;
import se.redfield.bert.setting.BertModelSelectorSettings.BertModelSelectionMode;
import se.redfield.bert.setting.BertModelSelectorSettings.TFHubModel;

/**
 * Editor component for the {@link BertModelSelectorSettings}.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertModelSelectorEditor extends JPanel {
	private static final long serialVersionUID = 1L;

	private BertModelSelectorSettings settings;

	private JPanel cards;
	private Map<BertModelSelectionMode, JRadioButton> buttons;
	private JComboBox<TFHubModel> hubModelCombo;

	/**
	 * @param settings The settings.
	 */
	public BertModelSelectorEditor(BertModelSelectorSettings settings) {
		this.settings = settings;
		initUI();
	}

	private void initUI() {
		GridBagConstraints c = new GridBagConstraints();
		setLayout(new GridBagLayout());

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;

		add(createRadioButtonsPanel(), c);
		c.gridy += 1;
		add(createCardsPanel(), c);

		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		c.gridy += 1;
		add(Box.createVerticalGlue(), c);
	}

	private JComponent createRadioButtonsPanel() {
		buttons = new EnumMap<>(BertModelSelectionMode.class);
		ButtonGroup group = new ButtonGroup();
		JPanel panel = new JPanel();

		for (BertModelSelectionMode mode : BertModelSelectionMode.values()) {
			JRadioButton rb = createModeRb(mode, group);
			panel.add(rb);
		}
		return panel;
	}

	private JRadioButton createModeRb(BertModelSelectionMode mode, ButtonGroup group) {
		JRadioButton rb = new JRadioButton(mode.getTitle());
		rb.addActionListener(e -> onModeChanged(mode));

		group.add(rb);
		buttons.put(mode, rb);

		return rb;
	}

	private JComponent createCardsPanel() {
		cards = new JPanel(new CardLayout());
		cards.add(createTFHubInput(), BertModelSelectionMode.TF_HUB.name());
		cards.add(createRemoteUrlInput(), BertModelSelectionMode.REMOTE_URL.name());
		cards.add(createLocalPathInput(), BertModelSelectionMode.LOCAL_PATH.name());
		return cards;
	}

	private JComponent createTFHubInput() {
		hubModelCombo = new JComboBox<>(TFHubModel.values().toArray(new TFHubModel[] {}));
		hubModelCombo.addActionListener(e -> {
			settings.setTfModel((TFHubModel) hubModelCombo.getSelectedItem());
		});

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(5, 10, 5, 5);
		panel.add(new JLabel("Select model:"), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx += 1;
		c.insets = new Insets(5, 5, 5, 10);
		panel.add(hubModelCombo, c);
		return panel;
	}

	private JComponent createRemoteUrlInput() {
		DialogComponentString remoteUrl = new DialogComponentString(settings.getRemoteUrlModel(), "URL:", false, 50);
		return remoteUrl.getComponentPanel();
	}

	private JComponent createLocalPathInput() {
		DialogComponentFileChooser localPath = new DialogComponentFileChooser(settings.getLocalPathModel(),
				"knime.bert-model", JFileChooser.OPEN_DIALOG, true);
		return localPath.getComponentPanel();
	}

	private void onModeChanged(BertModelSelectionMode mode) {
		settings.setMode(mode);
		((CardLayout) cards.getLayout()).show(cards, mode.name());
	}

	/**
	 * Method intended to be called by the parent dialog after settings was loaded.
	 */
	public void onSettingsLoaded() {
		BertModelSelectionMode mode = settings.getMode();
		buttons.get(mode).setSelected(true);
		hubModelCombo.setSelectedItem(settings.getTfModel());
		onModeChanged(mode);
	}
}
