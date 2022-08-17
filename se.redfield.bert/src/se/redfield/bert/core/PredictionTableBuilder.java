/*
 * Copyright (c) 2022 Redfield AB.
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

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;

import se.redfield.bert.setting.BertPredictorSettings;

/**
 * Builds an output prediction table from the input table and the the table with
 * probabilities received from python.
 * 
 * @author Alexander Bondaletov
 *
 */
public class PredictionTableBuilder {

	private final BertPredictorSettings settings;
	private final boolean multilabel;
	private final String[] classes;

	/**
	 * @param settings   The predictor settings.
	 * @param multilabel Whether the multilabel mode is used.
	 * @param classes    The classes.
	 */
	public PredictionTableBuilder(BertPredictorSettings settings, boolean multilabel, String[] classes) {
		this.settings = settings;
		this.multilabel = multilabel;
		this.classes = classes;
	}

	/**
	 * @param inTable            The input table.
	 * @param probabilitiesTable The table with probabilities for individual
	 *                           classes.
	 * @param exec               The execution context.
	 * @return The output table.
	 * @throws CanceledExecutionException
	 */
	public BufferedDataTable buildPredictionTable(BufferedDataTable inTable, BufferedDataTable probabilitiesTable,
			ExecutionContext exec) throws CanceledExecutionException {
		probabilitiesTable = renameProbabilitiesColumns(probabilitiesTable, exec.createSubExecutionContext(0));

		ColumnRearranger r = new ColumnRearranger(probabilitiesTable.getDataTableSpec());
		r.insertAt(0, ComputePredictionCellFactory.create(settings, multilabel, classes));

		if (!settings.getOutputProbabilities()) {
			r.keepOnly(0);
		}

		BufferedDataTable result = exec.createColumnRearrangeTable(probabilitiesTable, r, exec.createSubProgress(0.5));

		return exec.createJoinedTable(inTable, result, exec.createSubProgress(0.5));

	}

	private BufferedDataTable renameProbabilitiesColumns(BufferedDataTable probTable, ExecutionContext exec) {
		if (settings.getOutputProbabilities()) {
			return exec.createSpecReplacerTable(probTable,
					new DataTableSpec(createProbabilitiesSpecs().toArray(new DataColumnSpec[] {})));
		} else {
			return probTable;
		}
	}

	private List<DataColumnSpec> createProbabilitiesSpecs() {
		return Arrays.stream(classes)//
				.map(c -> String.format("P (%s)%s", c, settings.getProbabilitiesColumnSuffix()))
				.map(name -> new DataColumnSpecCreator(name, DoubleCell.TYPE).createSpec())
				.collect(Collectors.toList());
	}

	/**
	 * @param inTableSpec The input table spec.
	 * @return The output table spec. May return <code>null</code> if "append
	 *         individual probabilities" option is enabled, but classes are not
	 *         available.
	 */
	public DataTableSpec createSpec(DataTableSpec inTableSpec) {
		if (settings.getOutputProbabilities() && classes == null) {
			// Cannot compute probabilities columns without classes available
			return null;
		}

		List<DataColumnSpec> columns = new ArrayList<>();
		for (int i = 0; i < inTableSpec.getNumColumns(); i++) {
			columns.add(inTableSpec.getColumnSpec(i));
		}
		columns.add(new DataColumnSpecCreator(settings.getPredictionColumn(), StringCell.TYPE).createSpec());

		if (settings.getOutputProbabilities()) {
			columns.addAll(createProbabilitiesSpecs());
		}
		return new DataTableSpec(columns.toArray(new DataColumnSpec[] {}));
	}
}
