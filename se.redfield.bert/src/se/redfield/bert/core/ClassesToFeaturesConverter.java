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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.MissingValue;
import org.knime.core.data.MissingValueException;
import org.knime.core.data.StringValue;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;

import se.redfield.bert.util.InputUtils;

/**
 * Class intended for processing input training and validation tables for the
 * classifier. Extracts available classes and converts class column to numerical
 * representation.
 * 
 * @author Alexander Bondaletov
 *
 */
public class ClassesToFeaturesConverter {

	private final String sentenceColumn;
	private final String classColumn;
	private final boolean multiLabel;
	private final String classSeparator;

	/**
	 * @param sentenceColumn The sentence column.
	 * @param classColumn    The class column.
	 * @param multiLabel     Whether multi-label classification mode is used.
	 * @param classSeparator The class separator for multi-label mode.
	 */
	public ClassesToFeaturesConverter(String sentenceColumn, String classColumn, boolean multiLabel,
			String classSeparator) {
		this.sentenceColumn = sentenceColumn;
		this.classColumn = classColumn;
		this.multiLabel = multiLabel;
		this.classSeparator = classSeparator;
	}

	/**
	 * Performs processing of the training and (optional) validation tables.
	 * Extracts available classes and converts classes to numerical representation.
	 * 
	 * @param inTrainingTable   The training table.
	 * @param inValidationTable The validation table (may be null).
	 * @param exec              The execution context.
	 * @return The object containing processed training and validation tables as
	 *         well as all available classes.
	 * @throws InvalidSettingsException
	 * @throws CanceledExecutionException
	 */
	public ClassifierInput process(BufferedDataTable inTrainingTable, BufferedDataTable inValidationTable,
			ExecutionContext exec) throws InvalidSettingsException, CanceledExecutionException {
		List<String> classes = extractAndValidateClasses(inTrainingTable, inValidationTable);

		Map<String, Integer> indexedClasses = new HashMap<>();
		for (String c : classes) {
			indexedClasses.put(c, indexedClasses.size());
		}
		exec.setMessage("Prepare training table");
		BufferedDataTable outTrainingTable = convertTable(inTrainingTable, indexedClasses, exec);
		if (inValidationTable != null) {
			exec.setMessage("Prepare validation table");
		}
		BufferedDataTable outValidationTable = inValidationTable == null ? null
				: convertTable(inValidationTable, indexedClasses, exec);

		return new ClassifierInput(classes, outTrainingTable, outValidationTable);
	}

	private BufferedDataTable convertTable(BufferedDataTable inTable, Map<String, Integer> classes,
			ExecutionContext exec) throws CanceledExecutionException {
		ColumnRearranger r = new ColumnRearranger(inTable.getDataTableSpec());

		int classColumnIdx = inTable.getSpec().findColumnIndex(classColumn);
		r.replace(new ClassConverterCellFactory(classes, classColumnIdx, createClassColumnSpec()), classColumnIdx);

		r.keepOnly(sentenceColumn, classColumn);
		InputUtils.convertColumnsToString(r, sentenceColumn);
		return exec.createColumnRearrangeTable(inTable, r, exec.createSubProgress(0));
	}

	private DataColumnSpec createClassColumnSpec() {
		DataColumnSpecCreator c = new DataColumnSpecCreator(classColumn, ListCell.getCollectionType(DoubleCell.TYPE));
		return c.createSpec();
	}

	private List<String> extractAndValidateClasses(BufferedDataTable inTrainingTable,
			BufferedDataTable inValidationTable) throws InvalidSettingsException {
		Set<String> classes = extractClasses(inTrainingTable);
		validateClasses(classes, inValidationTable);
		return new ArrayList<>(classes);
	}

	private Set<String> extractClasses(BufferedDataTable inTrainingTable) {
		int idx = inTrainingTable.getSpec().findColumnIndex(classColumn);
		Set<String> classes = new LinkedHashSet<>();

		for (DataRow row : inTrainingTable) {
			DataCell c = row.getCell(idx);

			if (c.isMissing()) {
				throw new MissingValueException((MissingValue) c, "Class column contains missing values");
			}

			classes.addAll(getClassesFromCell((StringValue)c));
		}

		return classes;
	}

	private void validateClasses(Set<String> classes, BufferedDataTable inValidationTable)
			throws InvalidSettingsException {
		if (classes.size() < 2) {
			throw new InvalidSettingsException("The class column should contain at least 2 classes");
		}

		if (inValidationTable != null) {
			int idx = inValidationTable.getSpec().findColumnIndex(classColumn);
			for (DataRow row : inValidationTable) {
				DataCell c = row.getCell(idx);

				if (c.isMissing()) {
					throw new MissingValueException((MissingValue) c,
							"Validation table: class column contains missing values");
				}

				Collection<String> rowClasses = getClassesFromCell((StringValue)c);
				for (String rowClass : rowClasses) {
					if (!classes.contains(rowClass)) {
						throw new InvalidSettingsException(
								"Validation table contains class missing from the training table: " + rowClass);
					}
				}
			}
		}
	}

	private Collection<String> getClassesFromCell(StringValue cell) {
		if (multiLabel) {
			return Stream.of(cell.getStringValue().split(classSeparator))//
					.map(String::trim)//
					.filter(s -> !s.isEmpty())//
					.collect(Collectors.toList());
		} else {
			return List.of(cell.getStringValue());
		}
	}

	private class ClassConverterCellFactory extends AbstractCellFactory {

		private final Map<String, Integer> classes;
		private final int classColumnIdx;

		public ClassConverterCellFactory(Map<String, Integer> classes, int classColumnIdx, DataColumnSpec spec) {
			super(spec);
			this.classes = classes;
			this.classColumnIdx = classColumnIdx;
		}

		@Override
		public DataCell[] getCells(DataRow row) {
			Collection<String> rowClasses = getClassesFromCell((StringValue)row.getCell(classColumnIdx));

			DataCell cell = CollectionCellFactory.createListCell(toFeatureArray(rowClasses));

			return new DataCell[] { cell };
		}

		private Collection<DataCell> toFeatureArray(Collection<String> rowClasses) {
			double[] features = new double[classes.size()];
			Arrays.fill(features, 0.0);
			for (String rowClass : rowClasses) {
				features[getIndexForClass(rowClass)] = 1.0;
			}

			return Arrays.stream(features).mapToObj(DoubleCell::new).collect(Collectors.toList());
		}

		private int getIndexForClass(String cls) {
			if (classes.containsKey(cls)) {
				return classes.get(cls);
			} else {
				throw new IllegalStateException("Unknown class: " + cls);
			}
		}
	}

	/**
	 * Class holding the results of the processing done by
	 * {@link ClassesToFeaturesConverter}.
	 * 
	 * @author Alexander Bondaletov
	 *
	 */
	public static class ClassifierInput {
		private final BufferedDataTable trainingTable;
		private final BufferedDataTable validationTable;
		private final List<String> classes;

		/**
		 * @param classes         Available classes.
		 * @param trainingTable   Processed training table.
		 * @param validationTable Processed validation table (may be null).
		 */
		public ClassifierInput(List<String> classes, BufferedDataTable trainingTable,
				BufferedDataTable validationTable) {
			this.classes = classes;
			this.trainingTable = trainingTable;
			this.validationTable = validationTable;
		}

		/**
		 * @return Available classes
		 */
		public List<String> getClasses() {
			return classes;
		}

		/**
		 * @return The number of classes.
		 */
		public int getClassesCount() {
			return classes.size();
		}

		/**
		 * @return The processed training table.
		 */
		public BufferedDataTable getTrainingTable() {
			return trainingTable;
		}

		/**
		 * @return The processed validation table (may be null).
		 */
		public BufferedDataTable getValidationTable() {
			return validationTable;
		}

		/**
		 * @return <code>true</code> if the validation table is available,
		 *         <code>false</code> otherwise.
		 */
		public boolean hasValidationTable() {
			return validationTable != null;
		}
	}
}
