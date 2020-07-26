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

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

/**
 * 
 * Port object containing a path to the BERT model.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertModelPortObject extends AbstractSimplePortObject {

	/**
	 * Serializer for the {@link BertModelPortObject}.
	 */
	public static final class Serializer extends AbstractSimplePortObjectSerializer<BertModelPortObject> {
	}

	/**
	 * The type of this port.
	 */
	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(BertModelPortObject.class);

	private BertModelPortObjectSpec spec;

	/**
	 * Creates new instance
	 */
	public BertModelPortObject() {
		this(null);
	}

	/**
	 * @param spec The spec object.
	 */
	public BertModelPortObject(BertModelPortObjectSpec spec) {
		this.spec = spec;
	}

	@Override
	public String getSummary() {
		return spec.toString();
	}

	@Override
	public PortObjectSpec getSpec() {
		return spec;
	}

	@Override
	protected void save(ModelContentWO model, ExecutionMonitor exec) throws CanceledExecutionException {
		// nothing to save
	}

	@Override
	protected void load(ModelContentRO model, PortObjectSpec spec, ExecutionMonitor exec)
			throws InvalidSettingsException, CanceledExecutionException {
		this.spec = (BertModelPortObjectSpec) spec;
	}

	/**
	 * @return The bert model handle
	 */
	public String getHandle() {
		return spec.getHandle();
	}
}
