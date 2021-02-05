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

import java.io.IOException;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

import se.redfield.bert.nodes.port.BertPortObjectSpecBase.BertPortObjectType;

public interface BertPortObjectBase extends PortObject {
	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(BertPortObjectBase.class);

	/**
	 * Only purpose is to make this interface class available to the
	 * {@link PortTypeRegistry} via the PorType extension point.
	 */
	public static final class DummySerializer extends PortObjectSerializer<BertPortObjectBase> {
		@Override
		public void savePortObject(final BertPortObjectBase portObject, final PortObjectZipOutputStream out,
				final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
			throw new UnsupportedOperationException("Don't use this serializer");
		}

		@Override
		public BertPortObjectBase loadPortObject(final PortObjectZipInputStream in, final PortObjectSpec spec,
				final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
			throw new UnsupportedOperationException("Don't use this serializer");
		}
	}

	@Override
	BertPortObjectSpecBase getSpec();

	/**
	 * @return The port object type
	 */
	default BertPortObjectType getType() {
		return getSpec().getType();
	}
}
