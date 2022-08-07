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
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

import org.knime.core.node.NodeLogger;
import org.knime.python3.PythonSourceDirectoryLocator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import se.redfield.bert.nodes.port.BertModelFeature;

/**
 * The class representing information about available Hugging Face models.
 * 
 * @author Alexander Bondaletov
 *
 */
public class HuggingFaceModel {
	private static final NodeLogger LOGGER = NodeLogger.getLogger(HuggingFaceModel.class);
	private static final String MODELS_FILE = "config/hf_bert_models.json";

	private static List<HuggingFaceModel> values;
	private static EnumMap<BertModelFeature, List<HuggingFaceModel>> byFeature;

	private String handle;
	private List<BertModelFeature> features;

	/**
	 * @return The list of available Hugging Face models.
	 */
	public static List<HuggingFaceModel> values() {
		if (values == null) {
			try {
				values = readModels();
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
				values = Collections.emptyList();
			}

			byFeature = new EnumMap<>(BertModelFeature.class);
			for (HuggingFaceModel model : values) {
				for (BertModelFeature feature : model.getFeatures()) {
					byFeature.computeIfAbsent(feature, k -> new ArrayList<>()).add(model);
				}
			}

		}
		return values;
	}

	private static List<HuggingFaceModel> readModels() throws IOException {
		Gson gson = new GsonBuilder().create();
		Path path = PythonSourceDirectoryLocator.getPathFor(HuggingFaceModel.class, MODELS_FILE);
		try (Reader reader = Files.newBufferedReader(path)) {
			return List.of(gson.fromJson(reader, HuggingFaceModel[].class));
		}
	}

	/**
	 * @param handle The model identifier.
	 * @return The optional with the {@link HuggingFaceModel} object corresponding
	 *         to the given handler, or an empty optional in case none is found.
	 */
	public static Optional<HuggingFaceModel> forHandle(String handle) {
		return values().stream().filter(m -> m.handle.equals(handle)).findAny();
	}

	/**
	 * @param feature The BERT model feature.
	 * @return The list of models that supports given feature. Returns all of the
	 *         available models if passed feature is <code>null</code>.
	 */
	public static List<HuggingFaceModel> forFeature(BertModelFeature feature) {
		if (feature == null) {
			return values();
		} else {
			return byFeature.get(feature);
		}
	}

	/**
	 * @return The model handle.
	 */
	public String getHandle() {
		return handle;
	}

	/**
	 * @return The features supported by the model.
	 */
	public List<BertModelFeature> getFeatures() {
		return features;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HuggingFaceModel) {
			HuggingFaceModel other = (HuggingFaceModel) obj;
			return handle.equals(other.handle);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return handle.hashCode();
	}

	@Override
	public String toString() {
		return handle;
	}
}
