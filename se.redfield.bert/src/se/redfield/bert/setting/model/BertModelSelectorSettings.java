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
package se.redfield.bert.setting.model;

import java.util.Collections;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import se.redfield.bert.nodes.port.BertModelFeature;
import se.redfield.bert.nodes.port.BertModelType;
import se.redfield.bert.nodes.selector.BertModelSelectorNodeModel;
import se.redfield.bert.setting.PythonNodeSettings;

/**
 * Settings for the {@link BertModelSelectorNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertModelSelectorSettings extends PythonNodeSettings {
	private static final String KEY_MODE = "mode";
	private static final String KEY_TFHUB_MODEL = "tfHubModel";
	private static final String KEY_HUGGING_FACE_MODEL = "huggingFaceModel";
	private static final String KEY_REMOTE_URL = "remoteUrl";
	private static final String KEY_LOCAL_PATH = "localPath";
	private static final String KEY_CACHE_DIR = "cacheDir";
	private static final String KEY_ADVANCED_MODE_ENABLED = "advancedMode";

	private BertModelSelectionMode mode;
	private TFHubModel tfModel;
	private final SettingsModelString hfModel;
	private final SettingsModelString remoteUrl;
	private final SettingsModelString localPath;
	private final SettingsModelString cacheDir;
	private final SettingsModelBoolean advancedModeEnabled;

	/**
	 * Creates new instance
	 */
	public BertModelSelectorSettings() {
		mode = BertModelSelectionMode.getDefault();
		tfModel = TFHubModel.getDefault();
		hfModel = new SettingsModelString(KEY_HUGGING_FACE_MODEL, "");
		remoteUrl = new SettingsModelString(KEY_REMOTE_URL, "");
		localPath = new SettingsModelString(KEY_LOCAL_PATH, "");
		cacheDir = new SettingsModelString(KEY_CACHE_DIR, "");
		advancedModeEnabled = new SettingsModelBoolean(KEY_ADVANCED_MODE_ENABLED, false);
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
	 * @return the hfModel settings model.
	 */
	public SettingsModelString getHfModel() {
		return hfModel;
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
		case HUGGING_FACE:
			return hfModel.getStringValue();
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
	 * @return the advancedModeEnabled model.
	 */
	public SettingsModelBoolean getAdvancedModeEnabledModel() {
		return advancedModeEnabled;
	}

	/**
	 * @return Whether the advanced mode is enabled.
	 */
	public boolean isAdvancedModeEnabled() {
		return advancedModeEnabled.getBooleanValue();
	}

	/**
	 * @return The model type.
	 */
	public BertModelType getType() {
		if (getMode() == BertModelSelectionMode.HUGGING_FACE) {
			return BertModelType.HUGGING_FACE;
		}
		return BertModelType.TFHUB;
	}

	/**
	 * @return The list of features supported by the selected model. The empty list
	 *         is returned in cases when this information is not available.
	 */
	public List<BertModelFeature> getModelFeatures() {
		switch (mode) {
		case TF_HUB:
			return List.of(BertModelFeature.CORE);
		case HUGGING_FACE:
			return HuggingFaceModel.forHandle(hfModel.getStringValue())//
					.map(HuggingFaceModel::getFeatures)//
					.orElse(Collections.emptyList());
		default:
			return Collections.emptyList();
		}
	}

	/**
	 * Saves the settings into the given {@link NodeSettingsWO} object.
	 * 
	 * @param settings
	 */
	@Override
	public void saveSettingsTo(NodeSettingsWO settings) {
		super.saveSettingsTo(settings);
		settings.addString(KEY_MODE, mode.name());
		settings.addString(KEY_TFHUB_MODEL, tfModel.getName());
		hfModel.saveSettingsTo(settings);
		remoteUrl.saveSettingsTo(settings);
		localPath.saveSettingsTo(settings);
		cacheDir.saveSettingsTo(settings);
		advancedModeEnabled.saveSettingsTo(settings);
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

		if (settings.containsKey(KEY_HUGGING_FACE_MODEL)) {
			hfModel.validateSettings(settings);
		}

		if (settings.containsKey(KEY_ADVANCED_MODE_ENABLED)) {
			advancedModeEnabled.validateSettings(settings);
		}

		BertModelSelectorSettings temp = new BertModelSelectorSettings();
		temp.loadSettingsFrom(settings);
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

		if (mode == BertModelSelectionMode.HUGGING_FACE && hfModel.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Hugging Face model is not selected");
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
	@Override
	public void loadSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		super.loadSettingsFrom(settings);
		mode = BertModelSelectionMode.valueOf(settings.getString(KEY_MODE, BertModelSelectionMode.getDefault().name()));
		tfModel = TFHubModel.getByName(settings.getString(KEY_TFHUB_MODEL));
		remoteUrl.loadSettingsFrom(settings);
		localPath.loadSettingsFrom(settings);
		cacheDir.loadSettingsFrom(settings);

		if (settings.containsKey(KEY_HUGGING_FACE_MODEL)) {
			hfModel.loadSettingsFrom(settings);
		}

		if (settings.containsKey(KEY_ADVANCED_MODE_ENABLED)) {
			advancedModeEnabled.loadSettingsFrom(settings);
		}
	}
}
