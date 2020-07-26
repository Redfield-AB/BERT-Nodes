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
package se.redfield.bert.nodes.tokenizer;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;

import se.redfield.bert.setting.BertTokenizerSettings;

/**
 * 
 * Settings dialog for the {@link BertTokenizerNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertTokenizerNodeDialog extends DefaultNodeSettingsPane {

	private BertTokenizerSettings settings = new BertTokenizerSettings();

	/**
	 * Creates new instance.
	 */
	@SuppressWarnings("unchecked")
	public BertTokenizerNodeDialog() {
		DialogComponentColumnNameSelection targetColumn = new DialogComponentColumnNameSelection(
				settings.getTargetColumnModel(), "Target Column", BertTokenizerNodeModel.PORT_INPUT_TABLE,
				StringValue.class);

		addDialogComponent(targetColumn);
	}

}
