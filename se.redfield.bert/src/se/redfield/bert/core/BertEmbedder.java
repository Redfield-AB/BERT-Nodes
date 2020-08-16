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
package se.redfield.bert.core;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.dl.core.DLInvalidEnvironmentException;
import org.knime.dl.python.util.DLPythonSourceCodeBuilder;
import org.knime.dl.python.util.DLPythonUtils;
import org.knime.python2.kernel.PythonIOException;
import org.knime.python2.kernel.PythonKernelCleanupException;

import se.redfield.bert.setting.BertEmbedderSettings;

public class BertEmbedder {

	private static final String EMBEDDING_COLUMN = "embeddings";

	private BertEmbedderSettings settings;

	public BertEmbedder(BertEmbedderSettings settings) {
		this.settings = settings;
	}

	public DataTableSpec createSpec(DataTableSpec inTableSpec) {
		DataColumnSpec embeddings = new DataColumnSpecCreator(EMBEDDING_COLUMN,
				ListCell.getCollectionType(DoubleCell.TYPE)).createSpec();

		return new DataTableSpec(inTableSpec, new DataTableSpec(embeddings));
	}

	public BufferedDataTable computeEmbeddings(String bertModel, BufferedDataTable inTable, ExecutionContext exec)
			throws PythonIOException, CanceledExecutionException, PythonKernelCleanupException,
			DLInvalidEnvironmentException {
		try (BertCommands commands = new BertCommands()) {
			commands.putDataTable(inTable, exec.createSubProgress(0.1));
			commands.executeInKernel(computeEmbeddingsScript(bertModel), exec.createSubProgress(0.8));
			return commands.getDataTable(BertCommands.VAR_OUTPUT_TABLE, exec, exec.createSubProgress(0.1));
		}
	}

	private String computeEmbeddingsScript(String bertModel) {
		DLPythonSourceCodeBuilder b = DLPythonUtils.createSourceCodeBuilder("from BertEmbedder import BertEmbedder");
		b.a(BertCommands.VAR_OUTPUT_TABLE).a(" = BertEmbedder.run(").n();

		BertCommands.putInputTableArgs(b);
		BertCommands.putBertModelArgs(b, bertModel);
		BertCommands.putArgs(b, settings.getInputSettings());

		b.a("embeddings_column = ").as(EMBEDDING_COLUMN).a(",").n();
		b.a(")").n();

		return b.toString();
	}

}
