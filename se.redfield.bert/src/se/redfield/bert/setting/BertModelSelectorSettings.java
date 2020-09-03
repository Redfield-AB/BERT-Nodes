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

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.dl.util.DLUtils;

import se.redfield.bert.nodes.selector.BertModelSelectorNodeModel;

/**
 * Settings for the {@link BertModelSelectorNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertModelSelectorSettings {
	private static final NodeLogger LOGGER = NodeLogger.getLogger(BertModelSelectorSettings.class);

	private static final String KEY_MODE = "mode";
	private static final String KEY_TFHUB_MODEL = "tfHubModel";
	private static final String KEY_REMOTE_URL = "remoteUrl";
	private static final String KEY_LOCAL_PATH = "localPath";
	private static final String KEY_CACHE_DIR = "cacheDir";

	private BertModelSelectionMode mode;
	private TFHubModel tfModel;
	private final SettingsModelString remoteUrl;
	private final SettingsModelString localPath;
	private final SettingsModelString cacheDir;

	/**
	 * Creates new instance
	 */
	public BertModelSelectorSettings() {
		mode = BertModelSelectionMode.getDefault();
		tfModel = TFHubModel.getDefault();
		remoteUrl = new SettingsModelString(KEY_REMOTE_URL, "");
		localPath = new SettingsModelString(KEY_LOCAL_PATH, "");
		cacheDir = new SettingsModelString(KEY_CACHE_DIR, "");
	}

	/**
	 * @return the selection mode.
	 */
	public BertModelSelectionMode getMode() {
		return mode;
	}

	/**
	 * @param mode the selection mode.
	 */
	public void setMode(BertModelSelectionMode mode) {
		this.mode = mode;
	}

	/**
	 * @return selected TFHub model
	 */
	public TFHubModel getTfModel() {
		return tfModel;
	}

	/**
	 * @param tfModel The TFHub model.
	 */
	public void setTfModel(TFHubModel tfModel) {
		this.tfModel = tfModel;
	}

	/**
	 * @return the removeUrl model.
	 */
	public SettingsModelString getRemoteUrlModel() {
		return remoteUrl;
	}

	/**
	 * @return the localPathModel
	 */
	public SettingsModelString getLocalPathModel() {
		return localPath;
	}

	/**
	 * @return The BERT model handle in a form of remote URL or local path depending
	 *         on the current selection mode.
	 */
	public String getHandle() {
		switch (mode) {
		case TF_HUB:
			return tfModel.getUrl();
		case REMOTE_URL:
			return remoteUrl.getStringValue();
		case LOCAL_PATH:
			return localPath.getStringValue();
		}
		return null;
	}

	/**
	 * @return the cache dir model.
	 */
	public SettingsModelString getCacheDirModel() {
		return cacheDir;
	}

	/**
	 * @return the TFHub cache dir.
	 */
	public String getCacheDir() {
		return cacheDir.getStringValue();
	}

	/**
	 * Saves the settings into the given {@link NodeSettingsWO} object.
	 * 
	 * @param settings
	 */
	public void saveSettingsTo(NodeSettingsWO settings) {
		settings.addString(KEY_MODE, mode.name());
		settings.addString(KEY_TFHUB_MODEL, tfModel.getName());
		remoteUrl.saveSettingsTo(settings);
		localPath.saveSettingsTo(settings);
		cacheDir.saveSettingsTo(settings);
	}

	/**
	 * Validates the settings in the given {@link NodeSettingsRO} object.
	 * 
	 * @param settings
	 * @throws InvalidSettingsException
	 */
	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		remoteUrl.validateSettings(settings);
		localPath.validateSettings(settings);
		cacheDir.validateSettings(settings);

		BertModelSelectorSettings temp = new BertModelSelectorSettings();
		temp.validate();
	}

	/**
	 * Validates settings consistency.
	 * 
	 * @throws InvalidSettingsException
	 */
	public void validate() throws InvalidSettingsException {
		if (mode == BertModelSelectionMode.TF_HUB && tfModel == null) {
			throw new InvalidSettingsException("TensorFlow Hub model is not selected");
		}

		if (mode == BertModelSelectionMode.REMOTE_URL && remoteUrl.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Remote URL is not specified");
		}

		if (mode == BertModelSelectionMode.LOCAL_PATH && localPath.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Local path is not specified");
		}
	}

	/**
	 * Loads settings from the given {@link NodeSettingsRO} object.
	 * 
	 * @param settings
	 * @throws InvalidSettingsException
	 */
	public void loadSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		mode = BertModelSelectionMode.valueOf(settings.getString(KEY_MODE, BertModelSelectionMode.getDefault().name()));
		tfModel = TFHubModel.getByName(settings.getString(KEY_TFHUB_MODEL));
		remoteUrl.loadSettingsFrom(settings);
		localPath.loadSettingsFrom(settings);
		cacheDir.loadSettingsFrom(settings);
	}

	/**
	 * BERT model selection mode.
	 * 
	 * @author Alexander Bondaletov
	 *
	 */
	public enum BertModelSelectionMode {

		/**
		 * Select from provided TFHub models
		 */
		TF_HUB("TensorFlow Hub"),

		/**
		 * Enter remove URL of the model
		 */
		REMOTE_URL("Remote URL"),

		/**
		 * Load from the local directory
		 */
		LOCAL_PATH("Local folder");

		private String title;

		private BertModelSelectionMode(String title) {
			this.title = title;
		}

		/**
		 * @return the title
		 */
		public String getTitle() {
			return title;
		}

		static BertModelSelectionMode getDefault() {
			return TF_HUB;
		}
	}

	/**
	 * Class representing a Tensorflow Hub model.
	 * 
	 * @author Alexander Bondaletov
	 *
	 */
	public static class TFHubModel {
		private static final String MODELS_FILE = "config/tf_bert_models.csv";
		private static List<TFHubModel> values;

		private final String name;
		private final String url;

		/**
		 * @return List of availbale TFHub models.
		 */
		public static List<TFHubModel> values() {
			if (values == null) {
				readModels();
			}
			return values;
		}

		private static void readModels() {
			values = new ArrayList<>();
			try {
				List<String> lines = Files
						.readAllLines(DLUtils.Files.getFileFromSameBundle(TFHubModel.class, MODELS_FILE).toPath());

				for (String line : lines) {
					String[] parts = line.split(";");
					values.add(new TFHubModel(parts[0], parts[1]));
				}
			} catch (IllegalArgumentException | IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

		/**
		 * @return the default TFHub model.
		 */
		public static TFHubModel getDefault() {
			return values().get(0);
		}

		/**
		 * 
		 * @param name Model name.
		 * @return TFHub model.
		 */
		public static TFHubModel getByName(String name) {
			for (TFHubModel m : values()) {
				if (m.getName().equals(name)) {
					return m;
				}
			}
			return getDefault();
		}

		private TFHubModel(String name, String url) {
			this.name = name;
			this.url = url;
		}

		/**
		 * @return the model name.
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the model URL
		 */
		public String getUrl() {
			return url;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof TFHubModel)) {
				return false;
			}

			TFHubModel other = (TFHubModel) obj;
			return name.equals(other.name);
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
