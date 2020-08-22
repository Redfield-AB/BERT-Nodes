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
package se.redfield.bert.nodes.selector;

import java.io.File;
import java.io.IOException;

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
import org.knime.dl.core.DLInvalidEnvironmentException;
import org.knime.dl.python.util.DLPythonSourceCodeBuilder;
import org.knime.dl.python.util.DLPythonUtils;

import se.redfield.bert.core.BertCommands;
import se.redfield.bert.nodes.port.BertModelConfig;
import se.redfield.bert.nodes.port.BertModelPortObject;
import se.redfield.bert.nodes.port.BertModelPortObjectSpec;
import se.redfield.bert.setting.BertModelSelectorSettings;

public class BertModelSelectorNodeModel extends NodeModel {

	private final BertModelSelectorSettings settings = new BertModelSelectorSettings();

	protected BertModelSelectorNodeModel() {
		super(new PortType[] {}, new PortType[] { BertModelPortObject.TYPE });
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		BertModelPortObjectSpec spec = createSpec();
		downloadOrCheckModel(spec.getModel(), exec);
		return new PortObject[] { new BertModelPortObject(spec) };
	}

	private static void downloadOrCheckModel(BertModelConfig model, ExecutionContext exec)
			throws IOException, DLInvalidEnvironmentException, CanceledExecutionException {
		try (BertCommands commands = new BertCommands()) {
			commands.executeInKernel(getLoadModelScript(model), exec);
		}
	}

	private static String getLoadModelScript(BertModelConfig model) {
		DLPythonSourceCodeBuilder b = DLPythonUtils.createSourceCodeBuilder("from bert_utils import load_bert_layer");
		b.a("load_bert_layer(").n();
		BertCommands.putBertModelArgs(b, model);
		b.a(")").n();

		return b.toString();
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		return new PortObjectSpec[] { createSpec() };
	}

	private BertModelPortObjectSpec createSpec() {
		BertModelConfig model = new BertModelConfig(settings.getMode().name(), settings.getHandle(),
				settings.getCacheDir());
		return new BertModelPortObjectSpec(model);
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
		this.settings.loadSettingsFrom(settings);
	}

	@Override
	protected void reset() {
		// nothing to reset
	}

}
