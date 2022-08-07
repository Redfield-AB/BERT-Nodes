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
package se.redfield.bert.nodes.port;

/**
 * Enum representing different features supported by the specific BERT model.
 * 
 * @author Alexander Bondaletov
 *
 */
public enum BertModelFeature {
	/**
	 * BERT Core
	 */
	CORE("BERT Core"),
	/**
	 * Zero-shot text classification
	 */
	ZSTC("Zero-shot classification");

	private String name;

	private BertModelFeature(String name) {
		this.name = name;
	}

	/**
	 * @return The name.
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
