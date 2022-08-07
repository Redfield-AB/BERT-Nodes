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
package se.redfield.bert.setting.model;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.NodeLogger;
import org.knime.dl.util.DLUtils;

/**
 * Class representing a Tensorflow Hub model.
 * 
 * @author Alexander Bondaletov
 *
 */
public class TFHubModel {
	private static final NodeLogger LOGGER = NodeLogger.getLogger(TFHubModel.class);
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