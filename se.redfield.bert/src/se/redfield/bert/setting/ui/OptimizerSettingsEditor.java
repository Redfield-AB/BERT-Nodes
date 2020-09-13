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
import java.util.EnumMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import se.redfield.bert.setting.OptimizerSettings;
import se.redfield.bert.setting.OptimizerSettings.Optimizer;
import se.redfield.bert.setting.OptimizerSettings.OptimizerType;

/**
 * Editor component for the {@link OptimizerSettings}.
 * 
 * @author Alexander Bondaletov
 *
 */
public class OptimizerSettingsEditor extends JPanel {
	private static final long serialVersionUID = 1L;

	private final OptimizerSettings settings;

	private Map<OptimizerType, Optimizer> optimizers;
	private JComboBox<OptimizerType> cbOptimizerType;
	private JPanel cards;

	/**
	 * @param settings the optimizer settings
	 */
	public OptimizerSettingsEditor(OptimizerSettings settings) {
		this.settings = settings;
		initUI();
	}

	private void initUI() {
		cbOptimizerType = new JComboBox<>(OptimizerType.values());
		cbOptimizerType.addActionListener(e -> onOptimizerTypeChanged());
		cards = new JPanel(new CardLayout());

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(cbOptimizerType);
		add(cards);
	}

	private void onOptimizerTypeChanged() {
		OptimizerType type = (OptimizerType) cbOptimizerType.getSelectedItem();
		settings.setOptimizer(optimizers.get(type));

		((CardLayout) cards.getLayout()).show(cards, type.name());
	}

	/**
	 * Method intended to be called by the parent dialog after settings are loaded.
	 */
	public void settingsLoaded() {
		optimizers = new EnumMap<>(OptimizerType.class);
		Optimizer selected = settings.getOptimizer();
		for (OptimizerType type : OptimizerType.values()) {
			Optimizer opt = type == selected.getType() ? selected : type.createInstance();

			optimizers.put(type, opt);
			cards.add(opt.getEditor().getComponentGroupPanel(), type.name());
		}

		cbOptimizerType.setSelectedItem(selected.getType());
	}
}
