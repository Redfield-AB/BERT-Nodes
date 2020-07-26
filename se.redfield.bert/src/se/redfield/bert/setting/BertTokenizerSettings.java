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

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class BertTokenizerSettings {
	private static final String KEY_TARGET_COLUMN = "targetColumn";
	
	private final SettingsModelString targetColumnModel;
	
	public BertTokenizerSettings() {
		targetColumnModel = new SettingsModelString(KEY_TARGET_COLUMN, "");
	}
	
	public SettingsModelString getTargetColumnModel() {
		return targetColumnModel;
	}
	
	public String getTargetColumn() {
		return targetColumnModel.getStringValue();
	}
	
	public void saveSettingsTo(NodeSettingsWO settings) {
		targetColumnModel.saveSettingsTo(settings);
	}
	
	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		targetColumnModel.validateSettings(settings);
		
		BertTokenizerSettings temp = new BertTokenizerSettings();
		temp.loadSettings(settings);
		temp.validate();
	}
	
	public void validate() throws InvalidSettingsException{
		if(targetColumnModel.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Target column is not selected");
		}
	}
	
	public void loadSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		targetColumnModel.loadSettingsFrom(settings);
	}
}
