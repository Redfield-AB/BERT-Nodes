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

	private static final String KEY_MULTILABEL = "multiLabel";

	private boolean multiLabel;

	/**
	 * Creates new instance.
	 */
	public BertClassifierPortObjectSpec() {
		this(false);
	}

	/**
	 * @param multiLabel whenever the multilabel classification mode is used.
	 */
	public BertClassifierPortObjectSpec(boolean multiLabel) {
		this.multiLabel = multiLabel;
	}

	/**
	 * @return whenever the multi-label classification mode is used.
	 */
	public boolean isMultiLabel() {
		return multiLabel;
	}

	@Override
	protected void save(ModelContentWO model) {
		model.addBoolean(KEY_MULTILABEL, multiLabel);
	}

	@Override
	protected void load(ModelContentRO model) throws InvalidSettingsException {
		multiLabel = model.getBoolean(KEY_MULTILABEL, false);
	}

}
