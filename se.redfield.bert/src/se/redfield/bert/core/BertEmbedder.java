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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.dl.core.DLInvalidEnvironmentException;
import org.knime.dl.python.util.DLPythonSourceCodeBuilder;
import org.knime.dl.python.util.DLPythonUtils;
import org.knime.python2.kernel.PythonIOException;
import org.knime.python2.kernel.PythonKernelCleanupException;

import se.redfield.bert.nodes.port.BertClassifierPortObject;
import se.redfield.bert.nodes.port.BertModelPortObject;
import se.redfield.bert.nodes.port.BertPortObjectBase;
import se.redfield.bert.setting.BertEmbedderSettings;

public class BertEmbedder {

	private static final String EMBEDDING_COLUMN = "embeddings";
	private static final String SEQ_EMBEDDING_COLUMN_PREFIX = "sequence_embeddings_";

	private BertEmbedderSettings settings;

	public BertEmbedder(BertEmbedderSettings settings) {
		this.settings = settings;
	}

	public DataTableSpec createSpec(DataTableSpec inTableSpec) {
		List<DataColumnSpec> columns = new ArrayList<>();
		columns.add(
				new DataColumnSpecCreator(EMBEDDING_COLUMN, ListCell.getCollectionType(DoubleCell.TYPE)).createSpec());

		if (settings.getIncludeSeqEmbeddings()) {
			int seqLength = settings.getInputSettings().getMaxSeqLength();
			for (int i = 0; i < seqLength; i++) {
				columns.add(new DataColumnSpecCreator(SEQ_EMBEDDING_COLUMN_PREFIX + i,
						ListCell.getCollectionType(DoubleCell.TYPE)).createSpec());
			}
		}

		return new DataTableSpec(inTableSpec, new DataTableSpec(columns.toArray(new DataColumnSpec[] {})));
	}

	public BufferedDataTable computeEmbeddings(BertPortObjectBase bertObject, BufferedDataTable inTable,
			ExecutionContext exec) throws PythonIOException, CanceledExecutionException, PythonKernelCleanupException,
			DLInvalidEnvironmentException {
		try (BertCommands commands = new BertCommands(settings.getPythonCommand(), 1)) {
			commands.putDataTable(inTable, exec.createSubProgress(0.1));
			commands.executeInKernel(computeEmbeddingsScript(bertObject), exec.createSubProgress(0.85));
			return commands.getDataTable(exec, exec.createSubProgress(0.05));
		}
	}

	private String computeEmbeddingsScript(BertPortObjectBase bertObject) {
		DLPythonSourceCodeBuilder b = DLPythonUtils.createSourceCodeBuilder("from BertEmbedder import BertEmbedder");
		b.a("BertEmbedder.").a(getRunMethodName(bertObject)).a("(").n();

		BertCommands.putInputTableArgs(b);
		putBertObjectArgs(bertObject, b);
		BertCommands.putArgs(b, settings.getInputSettings());
		BertCommands.putBatchSizeArgs(b, settings.getBatchSize());

		b.a("embeddings_column = ").as(EMBEDDING_COLUMN).a(",").n();
		b.a("sequence_embedding_column_prefix = ").as(SEQ_EMBEDDING_COLUMN_PREFIX).a(",").n();
		b.a("include_sequence_embeddings = ").a(settings.getIncludeSeqEmbeddings()).a(",").n();
		b.a(")").n();

		return b.toString();
	}

	private static String getRunMethodName(BertPortObjectBase bertObject) {
		switch (bertObject.getType()) {
		case BERT_MODEL:
			return "run_from_pretrained";
		case CLASSIFIER:
			return "run_from_classifier";
		default:
			throw new IllegalArgumentException("Unsupported port object type:" + bertObject.getType());
		}
	}

	private static void putBertObjectArgs(BertPortObjectBase bertObject, DLPythonSourceCodeBuilder builder) {
		switch (bertObject.getType()) {
		case BERT_MODEL:
			putBertModelArgs((BertModelPortObject) bertObject, builder);
			break;
		case CLASSIFIER:
			putClassifierArgs((BertClassifierPortObject) bertObject, builder);
			break;
		default:
			throw new IllegalArgumentException("Unsupported port object type:" + bertObject.getType());
		}
	}

	private static void putBertModelArgs(BertModelPortObject bertModel, DLPythonSourceCodeBuilder builder) {
		BertCommands.putBertModelArgs(builder, bertModel.getModel());
	}

	private static void putClassifierArgs(BertClassifierPortObject classifier, DLPythonSourceCodeBuilder builder) {
		BertCommands.putFileStoreArgs(builder, classifier.getFileStore());
		BertCommands.putModelTypeArg(builder, classifier.getModelType());
	}
}
