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
import java.io.IOException;
import java.nio.file.Files;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.dl.util.DLUtils;

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
	private static final NodeLogger LOGGER = NodeLogger.getLogger(BertModelSelectorEditor.class);
	private static final String HUGGING_FACE_MODELS_FILE = "config/hf_bert_models.txt";

	private BertModelSelectorSettings settings;

	private JPanel cards;
	private Map<BertModelSelectionMode, JRadioButton> buttons;
	private JComboBox<TFHubModel> hubModelCombo;
	private JComboBox<String> hfModelCombo;
	private JComponent cacheDirPanel;

	/**
	 * @param settings The settings.
	 */
	public BertModelSelectorEditor(BertModelSelectorSettings settings) {
		this.settings = settings;
		initUI();

		settings.getAdvancedModeEnabledModel().addChangeListener(e -> updateModesVisibility());
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
		c.gridy += 1;
		add(createCacheDirPanel(), c);

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
		cards.add(createHuggingFaceInput(), BertModelSelectionMode.HUGGING_FACE.name());
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

	private JComponent createHuggingFaceInput() {
		hfModelCombo = new JComboBox<>(getHuggingFaceModels());
		hfModelCombo.addActionListener(e -> {
			settings.getHfModel().setStringValue((String) hfModelCombo.getSelectedItem());
		});

		hfModelCombo.setEditable(true);
		ComboBoxEditor editor = hfModelCombo.getEditor();
		((JTextField) editor.getEditorComponent()).getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				onUpdated();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				onUpdated();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				onUpdated();
			}

			private void onUpdated() {
				hfModelCombo.setSelectedItem(editor.getItem());
			}
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
		panel.add(hfModelCombo, c);
		return panel;
	}

	private String[] getHuggingFaceModels() {
		try {
			return Files.readAllLines(DLUtils.Files.getFileFromSameBundle(this, HUGGING_FACE_MODELS_FILE).toPath())
					.toArray(new String[] {});
		} catch (IllegalArgumentException | IOException e) {
			LOGGER.warn("Unable to load the list of Hugging Face models", e);
		}
		return new String[] {};
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

	private JComponent createCacheDirPanel() {
		DialogComponentFileChooser cacheDir = new DialogComponentFileChooser(settings.getCacheDirModel(),
				"knime.tf-cache-dir", JFileChooser.OPEN_DIALOG, true);
		cacheDir.setBorderTitle("Cache dir");
		cacheDirPanel = cacheDir.getComponentPanel();
		return cacheDirPanel;
	}

	private void onModeChanged(BertModelSelectionMode mode) {
		settings.setMode(mode);
		((CardLayout) cards.getLayout()).show(cards, mode.name());
		cacheDirPanel.setVisible(mode != BertModelSelectionMode.LOCAL_PATH);
	}

	/**
	 * Method intended to be called by the parent dialog after settings was loaded.
	 */
	public void onSettingsLoaded() {
		BertModelSelectionMode mode = settings.getMode();
		buttons.get(mode).setSelected(true);
		hubModelCombo.setSelectedItem(settings.getTfModel());
		hfModelCombo.setSelectedItem(settings.getHfModel().getStringValue());
		onModeChanged(mode);
		updateModesVisibility();
	}

	private void updateModesVisibility() {
		boolean advanced = settings.isAdvancedModeEnabled();
		buttons.get(BertModelSelectionMode.REMOTE_URL).setVisible(advanced);
		buttons.get(BertModelSelectionMode.LOCAL_PATH).setVisible(advanced);

		BertModelSelectionMode mode = settings.getMode();
		if (!advanced && (mode == BertModelSelectionMode.LOCAL_PATH || mode == BertModelSelectionMode.REMOTE_URL)) {
			buttons.get(BertModelSelectionMode.TF_HUB).doClick();
		}
	}
}
