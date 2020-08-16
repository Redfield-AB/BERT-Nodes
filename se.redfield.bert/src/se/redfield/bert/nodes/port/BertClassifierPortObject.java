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
import java.util.Arrays;

import javax.swing.JComponent;

import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStorePortObject;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.ModelContent;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.node.workflow.ModelContentOutPortView;

/**
 * Port object containing BERT classifier.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertClassifierPortObject extends FileStorePortObject {

	/**
	 * The type of this port.
	 */
	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(BertClassifierPortObject.class);

	private BertClassifierPortObjectSpec spec;

	/**
	 * Creates new instance
	 * 
	 * @param spec      the spec
	 * @param fileStore the file store
	 */
	public BertClassifierPortObject(BertClassifierPortObjectSpec spec, FileStore fileStore) {
		super(Arrays.asList(fileStore));
		this.spec = spec;
	}

	/**
	 * Creates new instance
	 * 
	 * @param spec the spec
	 */
	public BertClassifierPortObject(BertClassifierPortObjectSpec spec) {
		super();
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
	public JComponent[] getViews() {
		final ModelContent model = new ModelContent("Model Content");
		return new JComponent[] { new ModelContentOutPortView(model) };
	}

	/**
	 * @return the file store containing the model
	 */
	public FileStore getFileStore() {
		return getFileStore(0);
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * Serializer for {@link BertClassifierPortObject}
	 */
	public static final class Serializer extends PortObjectSerializer<BertClassifierPortObject> {

		@Override
		public void savePortObject(final BertClassifierPortObject portObject, final PortObjectZipOutputStream out,
				final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
			// Nothing to write. The network is defined by its specs and the file store
		}

		@Override
		public BertClassifierPortObject loadPortObject(final PortObjectZipInputStream in, final PortObjectSpec spec,
				final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
			return new BertClassifierPortObject((BertClassifierPortObjectSpec) spec);
		}

	}

}
