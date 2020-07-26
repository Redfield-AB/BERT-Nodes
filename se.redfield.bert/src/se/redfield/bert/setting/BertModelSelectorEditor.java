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
package se.redfield.bert.setting;

import java.awt.FlowLayout;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentString;

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
		initRadioButtons();

		DialogComponentString remoteUrl = new DialogComponentString(settings.getRemoteUrlModel(), "URL:", false, 50);
		DialogComponentFileChooser localPath = new DialogComponentFileChooser(settings.getLocalPathModel(),
				"knime.bert-model", JFileChooser.OPEN_DIALOG, true);

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(buttons.get(BertModelSelectionMode.TF_HUB));
		add(createTFHubInput());
		add(buttons.get(BertModelSelectionMode.REMOTE_URL));
		add(remoteUrl.getComponentPanel());
		add(buttons.get(BertModelSelectionMode.LOCAL_PATH));
		add(localPath.getComponentPanel());
	}

	private void initRadioButtons() {
		buttons = new EnumMap<>(BertModelSelectionMode.class);
		ButtonGroup group = new ButtonGroup();
		for (BertModelSelectionMode mode : BertModelSelectionMode.values()) {
			createModeRb(mode, group);
		}
	}

	private JRadioButton createModeRb(BertModelSelectionMode mode, ButtonGroup group) {
		JRadioButton rb = new JRadioButton(mode.getTitle());
		rb.addActionListener(e -> onModeChanged(mode));

		group.add(rb);
		buttons.put(mode, rb);

		return rb;
	}

	private JComponent createTFHubInput() {
		hubModelCombo = new JComboBox<>(TFHubModel.values().toArray(new TFHubModel[] {}));
		hubModelCombo.addActionListener(e -> {
			settings.setTfModel((TFHubModel) hubModelCombo.getSelectedItem());
		});

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Select model:"));
		panel.add(hubModelCombo);
		return panel;
	}

	private void onModeChanged(BertModelSelectionMode mode) {
		settings.setMode(mode);
		hubModelCombo.setEnabled(mode == BertModelSelectionMode.TF_HUB);
	}

	/**
	 * Method intended to be called by the parent dialog after settings was loaded.
	 */
	public void onSettingsLoaded() {
		BertModelSelectionMode mode = settings.getMode();
		buttons.get(mode).setSelected(true);

		hubModelCombo.setSelectedItem(settings.getTfModel());
		hubModelCombo.setEnabled(mode == BertModelSelectionMode.TF_HUB);
	}
}
