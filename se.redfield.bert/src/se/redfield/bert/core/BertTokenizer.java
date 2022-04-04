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
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.knime.dl.core.DLInvalidEnvironmentException;
import org.knime.dl.python.util.DLPythonSourceCodeBuilder;
import org.knime.dl.python.util.DLPythonUtils;
import org.knime.python2.kernel.PythonIOException;
import org.knime.python2.kernel.PythonKernelCleanupException;

import se.redfield.bert.nodes.port.BertModelConfig;
import se.redfield.bert.setting.BertTokenizerSettings;

public class BertTokenizer {
	@SuppressWarnings("unused")
	private static final NodeLogger LOGGER = NodeLogger.getLogger(BertTokenizer.class);

	private static final String IDS_COLUMN = "ids";
	private static final String MASKS_COLUMN = "masks";
	private static final String SEGMENTS_COLUMN = "segments";

	private final BertTokenizerSettings settings;

	public BertTokenizer(BertTokenizerSettings settings) {
		this.settings = settings;
	}

	public DataTableSpec createSpec(DataTableSpec inTableSpec) {
		DataColumnSpec ids = new DataColumnSpecCreator(IDS_COLUMN, ListCell.getCollectionType(IntCell.TYPE))
				.createSpec();
		DataColumnSpec masks = new DataColumnSpecCreator(MASKS_COLUMN, ListCell.getCollectionType(IntCell.TYPE))
				.createSpec();
		DataColumnSpec segments = new DataColumnSpecCreator(SEGMENTS_COLUMN, ListCell.getCollectionType(IntCell.TYPE))
				.createSpec();

		return new DataTableSpec(inTableSpec, new DataTableSpec(ids, masks, segments));
	}

	public BufferedDataTable tokenize(BertModelConfig bertModel, BufferedDataTable inTable, ExecutionContext exec)
			throws DLInvalidEnvironmentException, PythonKernelCleanupException, PythonIOException,
			CanceledExecutionException {
		try (BertCommands commands = new BertCommands(settings.getPythonCommand(), 1)) {
			commands.putDataTable(inTable, exec.createSubProgress(0.1));
			commands.executeInKernel(tokenizeScript(bertModel), exec.createSubProgress(0.8));
			return commands.getDataTable(exec, exec.createSubProgress(0.1));
		}
	}

	private String tokenizeScript(BertModelConfig bertModel) {
		DLPythonSourceCodeBuilder b = DLPythonUtils.createSourceCodeBuilder("from BertTokenizer import BertTokenizer");
		b.a("BertTokenizer.run(").n();

		BertCommands.putInputTableArgs(b);
		BertCommands.putBertModelArgs(b, bertModel);
		BertCommands.putArgs(b, settings.getInputSettings());

		b.a("ids_column = ").as(IDS_COLUMN).a(",").n();
		b.a("masks_column = ").as(MASKS_COLUMN).a(",").n();
		b.a("segments_column = ").as(SEGMENTS_COLUMN).a(",").n();
		b.a(")").n();

		return b.toString();
	}
}
