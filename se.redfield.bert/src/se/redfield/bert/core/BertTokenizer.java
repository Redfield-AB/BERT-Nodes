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

import se.redfield.bert.setting.BertTokenizerSettings;

public class BertTokenizer {
	@SuppressWarnings("unused")
	private static final NodeLogger LOGGER = NodeLogger.getLogger(BertTokenizer.class);

	private static final String IDS_COLUMN = "ids";
	private static final String MASKS_COLUMN = "masks";
	private static final String SEGMENTS_COLUMN = "segments";

	private static final int MAX_SEQ_LEN = 128;

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

	public BufferedDataTable tokenize(String bertModel, BufferedDataTable inTable, ExecutionContext exec)
			throws DLInvalidEnvironmentException, PythonKernelCleanupException, PythonIOException,
			CanceledExecutionException {
		try (BertCommands commands = new BertCommands()) {
			commands.putDataTable(inTable, exec);
			commands.loadBertModel(bertModel, exec);
			commands.createTokenizer(settings.getInputSettings(), exec);
			commands.executeInKernel(getTokenizeScript(), exec);
			return commands.getDataTable(BertCommands.VAR_OUTPUT_TABLE, exec, exec);
		}
	}

	private static String getTokenizeScript() {
		DLPythonSourceCodeBuilder b = DLPythonUtils.createSourceCodeBuilder();
		b.a("ids, masks, segments = ").a(BertCommands.VAR_TOKENIZER).a(".tokenize(").a(BertCommands.VAR_INPUT_TABLE)
				.a(")").n();
		b.a(BertCommands.VAR_OUTPUT_TABLE).a(" = ").a(BertCommands.VAR_INPUT_TABLE).a(".copy()").n();

		b.a(BertCommands.VAR_OUTPUT_TABLE).a("[").as(IDS_COLUMN).a("] = ids").n();
		b.a(BertCommands.VAR_OUTPUT_TABLE).a("[").as(MASKS_COLUMN).a("] = masks").n();
		b.a(BertCommands.VAR_OUTPUT_TABLE).a("[").as(SEGMENTS_COLUMN).a("] = segments").n();

		return b.toString();
	}

}
