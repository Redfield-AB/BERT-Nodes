/*
 * Copyright (c) 2022 Redfield AB.
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.knime.core.node.defaultnodesettings.SettingsModelString;

import se.redfield.bert.nodes.port.BertModelFeature;
import se.redfield.bert.setting.model.HuggingFaceModel;

/**
 * Editor component for selecting a model from the Hugging Face repository.
 * 
 * @author Alexander Bondaletov
 *
 */
public class HuggingFaceModelInput extends JPanel {
	private static final long serialVersionUID = 1L;

	private final SettingsModelString settingsModel;
	private HFModelComboBoxModel hfComboBoxModel;

	/**
	 * @param settingsModel The settings model holding selected model handle.
	 */
	public HuggingFaceModelInput(SettingsModelString settingsModel) {
		this.settingsModel = settingsModel;
		hfComboBoxModel = new HFModelComboBoxModel();
		settingsModel.addChangeListener(e -> hfComboBoxModel.setSelectedItem(settingsModel.getStringValue()));

		JComboBox<FeatureComboItem> filterCombo = createFilterCombo();
		JComboBox<HuggingFaceModel> modelCombo = createModelCombo();

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(5, 10, 5, 5);
		add(new JLabel("Display models:"), c);

		c.gridy += 1;
		add(new JLabel("Select model:"), c);

		c.gridy = 0;
		c.gridx += 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(5, 5, 5, 10);
		add(filterCombo, c);

		c.gridy += 1;
		add(modelCombo, c);
	}

	private JComboBox<FeatureComboItem> createFilterCombo() {
		List<FeatureComboItem> items = new ArrayList<>();
		items.add(new FeatureComboItem(null));
		for (BertModelFeature feature : BertModelFeature.values()) {
			items.add(new FeatureComboItem(feature));
		}

		JComboBox<FeatureComboItem> combo = new JComboBox<>(items.toArray(new FeatureComboItem[] {}));
		combo.addActionListener(
				e -> hfComboBoxModel.onFilter(((FeatureComboItem) combo.getSelectedItem()).getFeature()));
		return combo;
	}

	private JComboBox<HuggingFaceModel> createModelCombo() {
		JComboBox<HuggingFaceModel> modelCombo = new JComboBox<>(hfComboBoxModel);
		modelCombo.addActionListener(e -> settingsModel.setStringValue(hfComboBoxModel.getSelectedAsString()));

		modelCombo.setEditable(true);
		ComboBoxEditor editor = modelCombo.getEditor();
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
				modelCombo.setSelectedItem(editor.getItem());
			}
		});
		return modelCombo;
	}

	private class HFModelComboBoxModel extends DefaultComboBoxModel<HuggingFaceModel> {
		private static final long serialVersionUID = 1L;

		public HFModelComboBoxModel() {
			super(HuggingFaceModel.values().toArray(new HuggingFaceModel[] {}));
		}

		@Override
		public void setSelectedItem(Object anObject) {
			if (anObject instanceof String) {
				Optional<HuggingFaceModel> optional = HuggingFaceModel.forHandle((String) anObject);
				if (optional.isPresent()) {
					anObject = optional.get();
				}
			}
			super.setSelectedItem(anObject);
		}

		public String getSelectedAsString() {
			Object item = getSelectedItem();
			if (item instanceof HuggingFaceModel) {
				return ((HuggingFaceModel) item).getHandle();
			} else if (item instanceof String) {
				return (String) item;
			} else {
				return "";
			}
		}

		public void onFilter(BertModelFeature feature) {
			setItems(HuggingFaceModel.forFeature(feature));
		}

		private void setItems(List<HuggingFaceModel> items) {
			removeAllElements();
			addAll(items);
		}
	}

	private class FeatureComboItem {
		private final BertModelFeature feature;

		public FeatureComboItem(BertModelFeature feature) {
			this.feature = feature;
		}

		public BertModelFeature getFeature() {
			return feature;
		}

		@Override
		public String toString() {
			if (feature == null) {
				return "All";
			} else {
				return feature.toString();
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof FeatureComboItem) {
				FeatureComboItem other = (FeatureComboItem) obj;
				return feature == other.feature;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(feature);
		}
	}
}
