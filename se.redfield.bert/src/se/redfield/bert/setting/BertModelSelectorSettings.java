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
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.dl.util.DLUtils;

public class BertModelSelectorSettings {

	private static final String KEY_MODE = "mode";
	private static final String KEY_REMOVE_URL = "remoteUrl";
	private static final String KEY_LOCAL_PATH = "localPath";

	private BertModelSelectionMode mode;
	private TFHubModel tfModel;
	private final SettingsModelString remoteUrl;
	private final SettingsModelString localPath;

	public BertModelSelectorSettings() {
		mode = BertModelSelectionMode.getDefault();
		tfModel = TFHubModel.getDefault();
		remoteUrl = new SettingsModelString(KEY_REMOVE_URL, "");
		localPath = new SettingsModelString(KEY_LOCAL_PATH, "");
	}

	public BertModelSelectionMode getMode() {
		return mode;
	}

	public void setMode(BertModelSelectionMode mode) {
		this.mode = mode;
	}

	public TFHubModel getTfModel() {
		return tfModel;
	}

	public void setTfModel(TFHubModel tfModel) {
		this.tfModel = tfModel;
	}

	public SettingsModelString getRemoteUrlModel() {
		return remoteUrl;
	}

	public SettingsModelString getLocalPathModel() {
		return localPath;
	}

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

	public void saveSettingsTo(NodeSettingsWO settings) {
		settings.addString(KEY_MODE, mode.name());
		remoteUrl.saveSettingsTo(settings);
		localPath.saveSettingsTo(settings);
	}

	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		remoteUrl.validateSettings(settings);
		localPath.validateSettings(settings);

		BertModelSelectorSettings temp = new BertModelSelectorSettings();

	}

	public void validate() {
		// TODO
	}

	public void loadSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		mode = BertModelSelectionMode.valueOf(settings.getString(KEY_MODE, BertModelSelectionMode.getDefault().name()));
		remoteUrl.loadSettingsFrom(settings);
		localPath.loadSettingsFrom(settings);
	}

	public enum BertModelSelectionMode {
		TF_HUB("TensorFlow Hub"), //
		REMOTE_URL("Remote URL"), //
		LOCAL_PATH("Local folder");

		private String title;

		private BertModelSelectionMode(String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}

		static BertModelSelectionMode getDefault() {
			return TF_HUB;
		}
	}

	public static class TFHubModel {
		private static final String MODELS_FILE = "config/tf_bert_models.csv";
		private static List<TFHubModel> values;

		private final String name;
		private final String url;

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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public static TFHubModel getDefault() {
			return values().get(0);
		}

		private TFHubModel(String name, String url) {
			this.name = name;
			this.url = url;
		}

		public String getName() {
			return name;
		}

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
