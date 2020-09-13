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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.dl.base.nodes.AbstractGridBagDialogComponentGroup;
import org.knime.dl.base.nodes.IDialogComponentGroup;
import org.knime.dl.python.util.DLPythonUtils;

/**
 * 
 * Settings class for learning optimizer.
 * 
 * @author Alexander Bondaletov
 *
 */
public class OptimizerSettings {

	private static final String KEY_TYPE = "type";

	private final String key;
	private Optimizer optimizer;

	/**
	 * @param key the settings key
	 */
	public OptimizerSettings(String key) {
		this.key = key;
		optimizer = OptimizerType.getDefault().createInstance();
	}

	/**
	 * Saves the settings into the given {@link NodeSettingsWO}.
	 * 
	 * @param settings the settings
	 */
	public void saveSettingsTo(NodeSettingsWO settings) {
		NodeSettingsWO cfg = settings.addNodeSettings(key);

		optimizer.saveSettingsTo(cfg);
	}

	/**
	 * Loads the settings from the given {@link NodeSettingsRO}.
	 * 
	 * @param settings the settings.
	 * @throws InvalidSettingsException
	 */
	public void loadSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		NodeSettingsRO cfg = settings.getNodeSettings(key);

		OptimizerType type = OptimizerType.valueOf(cfg.getString(KEY_TYPE));
		optimizer = type.createInstance();
		optimizer.loadSettingsFrom(cfg);
	}

	/**
	 * @return the {@link Optimizer} object instance.
	 */
	public Optimizer getOptimizer() {
		return optimizer;
	}

	/**
	 * @param optimizer the optimizer.
	 */
	public void setOptimizer(Optimizer optimizer) {
		this.optimizer = optimizer;
	}

	/**
	 * Abstract class representing Keras optimizer
	 * 
	 * @author Alexander Bondaletov
	 *
	 */
	public abstract static class Optimizer {

		private final OptimizerType type;
		private IDialogComponentGroup editor;

		/**
		 * @param type Optimizer type.
		 */
		public Optimizer(OptimizerType type) {
			this.type = type;
		}

		/**
		 * Saves the settings into the given {@link NodeSettingsWO}.
		 * 
		 * @param settings
		 */
		public void saveSettingsTo(NodeSettingsWO settings) {
			settings.addString(KEY_TYPE, getType().name());
			for (SettingsModel sm : getSettings()) {
				sm.saveSettingsTo(settings);
			}
		}

		/**
		 * Loads the settings from the given {@link NodeSettingsRO}.
		 * 
		 * @param settings
		 * @throws InvalidSettingsException
		 */
		public void loadSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
			for (SettingsModel sm : getSettings()) {
				sm.loadSettingsFrom(settings);
			}
		}

		/**
		 * @return The optimizer type.
		 */
		public OptimizerType getType() {
			return type;
		}

		/**
		 * @return The editor {@link IDialogComponentGroup}.
		 */
		public IDialogComponentGroup getEditor() {
			if (editor == null) {
				editor = createEditor();
			}
			return editor;
		}

		/**
		 * @return The Python representation of the optimizer.
		 */
		public String getBackendRepresentation() {
			Map<String, String> params = new HashMap<>();
			populateParams(params);
			String paramsString = params.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
					.collect(Collectors.joining(", "));
			return type.getIdentifier() + "(" + paramsString + ")";
		}

		protected abstract Collection<SettingsModel> getSettings();

		protected abstract IDialogComponentGroup createEditor();

		protected abstract void populateParams(Map<String, String> params);
	}

	private static class AdamOptimizer extends Optimizer {
		private static final String KEY_LEARNING_RATE = "learning_rate";
		private static final String KEY_BETA_1 = "beta_1";
		private static final String KEY_BETA_2 = "beta_2";
		private static final String KEY_EPSILON = "epsilon";
		private static final String KEY_AMSGRAD = "amsgrad";

		private final SettingsModelDoubleBounded learningRate;
		private final SettingsModelDoubleBounded beta1;
		private final SettingsModelDoubleBounded beta2;
		private final SettingsModelDoubleBounded epsilon;
		private final SettingsModelBoolean amsgrad;

		private AdamOptimizer() {
			super(OptimizerType.ADAM);
			learningRate = new SettingsModelDoubleBounded(KEY_LEARNING_RATE, 0.001, 0, Double.MAX_VALUE);
			beta1 = new SettingsModelDoubleBounded(KEY_BETA_1, 0.9, Math.nextUp(0), Math.nextDown(1));
			beta2 = new SettingsModelDoubleBounded(KEY_BETA_2, 0.999, Math.nextUp(0), Math.nextDown(1));
			epsilon = new SettingsModelDoubleBounded(KEY_EPSILON, 1e-7, 0, Double.MAX_VALUE);
			amsgrad = new SettingsModelBoolean(KEY_AMSGRAD, false);
		}

		@Override
		protected Collection<SettingsModel> getSettings() {
			return Arrays.asList(learningRate, beta1, beta2, epsilon, amsgrad);
		}

		@Override
		protected void populateParams(Map<String, String> params) {
			params.put("learning_rate", DLPythonUtils.toPython(learningRate.getDoubleValue()));
			params.put("beta_1", DLPythonUtils.toPython(beta1.getDoubleValue()));
			params.put("beta_2", DLPythonUtils.toPython(beta2.getDoubleValue()));
			params.put("epsilon", DLPythonUtils.toPython(epsilon.getDoubleValue()));
			params.put("amsgrad", DLPythonUtils.toPython(amsgrad.getBooleanValue()));
		}

		@Override
		public IDialogComponentGroup createEditor() {
			return new AdamDialogGroup();
		}

		private class AdamDialogGroup extends AbstractGridBagDialogComponentGroup {
			public AdamDialogGroup() {
				addNumberEditRowComponent(learningRate, "Learning rate");
				addNumberEditRowComponent(beta1, "Beta 1");
				addNumberEditRowComponent(beta2, "Beta 2");
				addNumberEditRowComponent(epsilon, "Epsilon");
				addCheckboxRow(amsgrad, "Apply AMSGrad variant of this algorithm", true);
			}
		}
	}

	private static class SGDOptimizer extends Optimizer {
		private static final String KEY_LEARNING_RATE = "learning_rate";
		private static final String KEY_MOMENTUM = "momentum";
		private static final String KEY_NESTEROV = "nesterov";

		private final SettingsModelDoubleBounded learningRate;
		private final SettingsModelDoubleBounded momentum;
		private final SettingsModelBoolean nesterov;

		private SGDOptimizer() {
			super(OptimizerType.SGD);
			learningRate = new SettingsModelDoubleBounded(KEY_LEARNING_RATE, 0.01, 0, Double.MAX_VALUE);
			momentum = new SettingsModelDoubleBounded(KEY_MOMENTUM, 0, 0, Double.MAX_VALUE);
			nesterov = new SettingsModelBoolean(KEY_NESTEROV, false);
		}

		@Override
		protected Collection<SettingsModel> getSettings() {
			return Arrays.asList(learningRate, momentum, nesterov);
		}

		@Override
		protected void populateParams(Map<String, String> params) {
			params.put("learning_rate", DLPythonUtils.toPython(learningRate.getDoubleValue()));
			params.put("momentum", DLPythonUtils.toPython(momentum.getDoubleValue()));
			params.put("nesterov", DLPythonUtils.toPython(nesterov.getBooleanValue()));
		}

		@Override
		public IDialogComponentGroup createEditor() {
			return new SGDDialogGroup();
		}

		private class SGDDialogGroup extends AbstractGridBagDialogComponentGroup {
			public SGDDialogGroup() {
				addNumberEditRowComponent(learningRate, "Learning rate");
				addNumberEditRowComponent(momentum, "Momentum");
				addCheckboxRow(nesterov, "Apply Nesterov momentum", true);
			}
		}
	}

	/**
	 * Enum representing available Keras optimizers.
	 * 
	 * @author Alexander Bondaletov
	 *
	 */
	public enum OptimizerType {
		/**
		 * Adam optimizer
		 */
		ADAM("Adam", "tf.keras.optimizers.Adam", AdamOptimizer::new),
		/**
		 * SGD optimizer
		 */
		SGD("SGD", "tf.keras.optimizers.SGD", SGDOptimizer::new);

		private String title;
		private String identifier;
		private Supplier<Optimizer> constructor;

		private OptimizerType(String title, String identifier, Supplier<Optimizer> constructor) {
			this.title = title;
			this.identifier = identifier;
			this.constructor = constructor;
		}

		/**
		 * @return the Python identifier
		 */
		public String getIdentifier() {
			return identifier;
		}

		/**
		 * Creates new {@link Optimizer} instance of the selected type.
		 * 
		 * @return the {@link Optimizer} instance.
		 */
		public Optimizer createInstance() {
			return constructor.get();
		}

		@Override
		public String toString() {
			return title;
		}

		/**
		 * @return The default optimizer type.
		 */
		public static OptimizerType getDefault() {
			return ADAM;
		}
	}
}
