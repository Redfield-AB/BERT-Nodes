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
 * The spec for the {@link BertClassifierPortObject}.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertClassifierPortObjectSpec extends AbstractSimplePortObjectSpec {

	/**
	 * The serializer for the {@link BertClassifierPortObjectSpec}
	 *
	 */
	public static final class Serializer extends AbstractSimplePortObjectSpecSerializer<BertClassifierPortObjectSpec> {
	}

	@Override
	protected void save(ModelContentWO model) {
		// no settings
	}

	@Override
	protected void load(ModelContentRO model) throws InvalidSettingsException {
		// no settings
	}

}
