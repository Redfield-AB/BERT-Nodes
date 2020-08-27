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

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import se.redfield.bert.core.BertTokenizer;
import se.redfield.bert.nodes.port.BertModelPortObject;
import se.redfield.bert.setting.BertTokenizerSettings;

/**
 * BERT Tokenizer node. Takes an input table and performs tokenization computing
 * ids, masks and segments columns that could be used as inputs for the BERT
 * model.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertTokenizerNodeModel extends NodeModel {

	/**
	 * {@link BertModelPortObject} input port index.
	 */
	public static final int PORT_BERT_MODEL = 0;
	/**
	 * Data table input port index.
	 */
	public static final int PORT_INPUT_TABLE = 1;

	private final BertTokenizerSettings settings;
	private final BertTokenizer tokenizer;

	protected BertTokenizerNodeModel() {
		super(new PortType[] { BertModelPortObject.TYPE, BufferedDataTable.TYPE },
				new PortType[] { BufferedDataTable.TYPE });

		settings = new BertTokenizerSettings();
		tokenizer = new BertTokenizer(settings);
	}

	@Override
	protected PortObject[] execute(PortObject[] inData, ExecutionContext exec) throws Exception {
		BertModelPortObject model = (BertModelPortObject) inData[PORT_BERT_MODEL];
		return new PortObject[] {
				tokenizer.tokenize(model.getModel(), (BufferedDataTable) inData[PORT_INPUT_TABLE], exec) };
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		settings.validate((DataTableSpec) inSpecs[PORT_INPUT_TABLE]);
		return new PortObjectSpec[] { tokenizer.createSpec((DataTableSpec) inSpecs[PORT_INPUT_TABLE]) };
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// no internals

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// no internals

	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		this.settings.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		this.settings.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		this.settings.loadSettings(settings);
	}

	@Override
	protected void reset() {
		// nothing to reset

	}

}
