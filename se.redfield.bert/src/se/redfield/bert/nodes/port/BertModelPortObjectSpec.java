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

	private static final String KEY_MODE = "mode";
	private static final String KEY_HANDLE = "handle";

	private String mode;
	private String handle;

	/**
	 * Creates new instance.
	 */
	public BertModelPortObjectSpec() {
		this("", "");
	}

	/**
	 * @param mode   Selection mode
	 * @param handle The model handle (URL or path)
	 */
	public BertModelPortObjectSpec(String mode, String handle) {
		this.mode = mode;
		this.handle = handle;
	}

	@Override
	protected void save(ModelContentWO model) {
		model.addString(KEY_MODE, mode);
		model.addString(KEY_HANDLE, handle);
	}

	@Override
	protected void load(ModelContentRO model) throws InvalidSettingsException {
		mode = model.getString(KEY_MODE, "");
		handle = model.getString(KEY_HANDLE, "");
	}

	/**
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * @return the handle
	 */
	public String getHandle() {
		return handle;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Selection mode: ").append(mode).append("\n");
		sb.append("Handle: ").append(handle).append("\n");
		return sb.toString();
	}
}
