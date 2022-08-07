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
	 * Select Hugging Face model
	 */
	HUGGING_FACE("Hugging Face"),

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