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
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;

/**
 * Specs for the {@link BertModelPortObject}.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertModelPortObjectSpec extends AbstractSimplePortObjectSpec {
	/**
	 * The serializer for the {@link BertModelPortObjectSpec}
	 *
	 */
	public static final class Serializer extends AbstractSimplePortObjectSpecSerializer<BertModelPortObjectSpec> {
	}

	private BertModelConfig model;

	/**
	 * Creates new instance.
	 */
	public BertModelPortObjectSpec() {
		this(new BertModelConfig());
	}

	/**
	 * @param model The {@link BertModelConfig} object.
	 */
	public BertModelPortObjectSpec(BertModelConfig model) {
		this.model = model;
	}

	@Override
	protected void save(ModelContentWO model) {
		this.model.save(model);
	}

	@Override
	protected void load(ModelContentRO model) throws InvalidSettingsException {
		this.model.load(model);
	}

	/**
	 * @return the {@link BertModelConfig} object.
	 */
	public BertModelConfig getModel() {
		return model;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Selection mode: ").append(model.getMode()).append("\n");
		sb.append("Handle: ").append(model.getHandle()).append("\n");
		return sb.toString();
	}
}
