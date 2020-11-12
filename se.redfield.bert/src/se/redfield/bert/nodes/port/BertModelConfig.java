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
package se.redfield.bert.nodes.port;

import java.net.MalformedURLException;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.FileUtil;

import se.redfield.bert.setting.BertModelSelectorSettings.BertModelSelectionMode;

/**
 * Data describing selected BERT model.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertModelConfig {
	private static final NodeLogger LOGGER = NodeLogger.getLogger(BertModelConfig.class);

	private static final String KEY_MODE = "mode";
	private static final String KEY_HANDLE = "handle";
	private static final String KEY_CACHE_DIR = "cacheDir";

	private String mode;
	private String handle;
	private String cacheDir;

	/**
	 * Creates new instance
	 */
	public BertModelConfig() {
		this("", "", "");
	}

	/**
	 * @param mode     Model selection mode.
	 * @param handle   BERT model handle.
	 * @param cacheDir TFHub cache dir.
	 */
	public BertModelConfig(String mode, String handle, String cacheDir) {
		this.mode = mode;
		this.handle = handle;
		this.cacheDir = cacheDir;
	}

	/**
	 * Saves the content into the given {@link ModelContentWO} object.
	 * 
	 * @param model
	 */
	public void save(ModelContentWO model) {
		model.addString(KEY_MODE, mode);
		model.addString(KEY_HANDLE, handle);
		model.addString(KEY_CACHE_DIR, cacheDir);
	}

	/**
	 * 
	 * Loads the content from the given {@link ModelContentRO} object.
	 * 
	 * @param model
	 * @throws InvalidSettingsException
	 */
	public void load(ModelContentRO model) throws InvalidSettingsException {
		mode = model.getString(KEY_MODE, "");
		handle = model.getString(KEY_HANDLE, "");
		cacheDir = model.getString(KEY_CACHE_DIR, "");
	}

	/**
	 * @return the BERT model selection mode
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * @return the BERT model handle as it is stored in the config
	 */
	public String getHandleRaw() {
		return handle;
	}

	/**
	 * Returns the model handle. Converts any <code>knime://</code> URI to local
	 * absolute path if necessary.
	 * 
	 * @return The processed handle.
	 */
	public String getHandle() {
		if (BertModelSelectionMode.LOCAL_PATH.name().equals(mode)) {
			try {
				return toAbsolutePath(handle);
			} catch (MalformedURLException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		return handle;
	}

	private static String toAbsolutePath(String path) throws MalformedURLException {
		return FileUtil.getFileFromURL(FileUtil.toURL(path)).getAbsolutePath();
	}

	/**
	 * Returns the TFHub cache directory path. Converts any <code>knime://</code>
	 * URI to local absolute path if necessary.
	 * 
	 * @return the TFHub cache directory
	 */
	public String getCacheDir() {
		try {
			return toAbsolutePath(cacheDir);
		} catch (MalformedURLException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return "";
	}
}
