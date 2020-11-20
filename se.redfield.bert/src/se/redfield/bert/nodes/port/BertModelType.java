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

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.config.ConfigRO;
import org.knime.core.node.config.ConfigWO;

/**
 * Enum representing different types of supported BERT model.
 * 
 * @author Alexander Bondaletov
 *
 */
public enum BertModelType {
	/**
	 * Model from Tensorflow hub.
	 */
	TFHUB("load_bert_layer", "BertClassifier"),
	/**
	 * Model provided by Hugging Face.
	 */
	HUGGING_FACE("load_hf_bert_layer", "HFBertClassifier");

	private String loadMethod;
	private String classifierClass;

	private BertModelType(String loadMothod, String classifierClass) {
		this.loadMethod = loadMothod;
		this.classifierClass = classifierClass;
	}

	/**
	 * @return The python load method name.
	 */
	public String getLoadMethod() {
		return loadMethod;
	}

	/**
	 * @return The python classifier class
	 */
	public String getClassifierClass() {
		return classifierClass;
	}

	/**
	 * Saves the type into the given {@link ConfigWO} under the provided settings
	 * key.
	 * 
	 * @param config The config object.
	 * @param key    The config key.
	 */
	public void save(ConfigWO config, String key) {
		config.addString(key, name());
	}

	/**
	 * Loads the type from the given {@link ConfigRO} by the given key.
	 * 
	 * @param config The config object.
	 * @param key    The config key.
	 * @return The loaded {@link BertModelType}, or the <code>TFHUB</code> (default
	 *         value) in case config does not contain provided key.
	 * @throws InvalidSettingsException
	 */
	public static BertModelType load(ConfigRO config, String key) throws InvalidSettingsException {
		if (config.containsKey(key)) {
			String name = config.getString(key);
			try {
				return BertModelType.valueOf(name);
			} catch (IllegalArgumentException e) {
				throw new InvalidSettingsException("Invalid model type: " + name, e);
			}
		}
		return TFHUB;
	}

}
