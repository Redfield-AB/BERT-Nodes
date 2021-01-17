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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;

import se.redfield.bert.nodes.port.BertClassifierPortObject;
import se.redfield.bert.setting.BertPredictorSettings;

/**
 * {@link AbstractCellFactory} implementation to construct 'Prediction' column
 * using different modes.
 * 
 * @author Alexander Bondaletov
 *
 */
public abstract class ComputePredictionCellFactory extends AbstractCellFactory {

	protected String[] classes;

	protected ComputePredictionCellFactory(BertPredictorSettings settings, BertClassifierPortObject classifier) {
		super(createPredictionColumnSpec(settings));
		this.classes = classifier.getClasses();
	}

	@Override
	public DataCell[] getCells(DataRow row) {
		return new DataCell[] { new StringCell(getPredictionString(row)) };
	}

	protected abstract String getPredictionString(DataRow row);

	/**
	 * @param settings   The predictor settings.
	 * @param classifier The classifier object.
	 * @return The {@link CellFactory} instance to contruct prediction column.
	 */
	public static CellFactory create(BertPredictorSettings settings, BertClassifierPortObject classifier) {
		if (classifier.isMultiLabel() && settings.getFixNumberOfClasses()) {
			return new FixedNumberOfClassesPredictionsCellFactory(settings, classifier);
		} else if (classifier.isMultiLabel() || settings.getUseCustomThreshould()) {
			return new ThresholdPredictionsCellFactory(settings, classifier);
		} else {
			return new SinglePredictionCellFactory(settings, classifier);
		}
	}

	private static DataColumnSpec createPredictionColumnSpec(BertPredictorSettings settings) {
		DataColumnSpecCreator c = new DataColumnSpecCreator(settings.getPredictionColumn(), StringCell.TYPE);
		return c.createSpec();
	}

	private static class SinglePredictionCellFactory extends ComputePredictionCellFactory {

		protected SinglePredictionCellFactory(BertPredictorSettings settings, BertClassifierPortObject classifier) {
			super(settings, classifier);
		}

		@Override
		protected String getPredictionString(DataRow row) {
			double max = -1;
			int maxIdx = -1;
			int idx = 0;

			for (DataCell cell : row) {
				double probability = ((DoubleCell) cell).getDoubleValue();

				if (probability > max) {
					max = probability;
					maxIdx = idx;
				}
				idx++;
			}
			return classes[maxIdx];
		}

	}

	private abstract static class MultiplePredictionsCellFactory extends ComputePredictionCellFactory {

		private String separator;

		protected MultiplePredictionsCellFactory(BertPredictorSettings settings, BertClassifierPortObject classifier) {
			super(settings, classifier);
			separator = settings.getClassSeparator();
		}

		@Override
		protected String getPredictionString(DataRow row) {
			return getPredictedClasses(row).stream().map(idx -> classes[idx]).collect(Collectors.joining(separator));
		}

		protected abstract Collection<Integer> getPredictedClasses(DataRow row);
	}

	private static class ThresholdPredictionsCellFactory extends MultiplePredictionsCellFactory {

		private double threshold;

		protected ThresholdPredictionsCellFactory(BertPredictorSettings settings, BertClassifierPortObject classifier) {
			super(settings, classifier);
			threshold = settings.getPredictionThreshold();
		}

		@Override
		protected Collection<Integer> getPredictedClasses(DataRow row) {
			List<Integer> indexes = new ArrayList<>();
			int idx = 0;

			for (DataCell cell : row) {
				double probability = ((DoubleCell) cell).getDoubleValue();
				if (probability > threshold) {
					indexes.add(idx);
				}
				idx++;
			}
			return indexes;
		}

	}

	private static class FixedNumberOfClassesPredictionsCellFactory extends MultiplePredictionsCellFactory {

		private int numberOfClasses;

		protected FixedNumberOfClassesPredictionsCellFactory(BertPredictorSettings settings,
				BertClassifierPortObject classifier) {
			super(settings, classifier);
			numberOfClasses = Math.min(settings.getNumberOfClassesPerPrediction(), classifier.getClasses().length);
		}

		@Override
		protected Collection<Integer> getPredictedClasses(DataRow row) {
			ClassProbability[] probabilities = toProbabilitiesArray(row);
			Arrays.sort(probabilities);

			List<Integer> result = new ArrayList<>();
			for (int i = 0; i < numberOfClasses; i++) {
				result.add(probabilities[i].getIndex());
			}
			return result;
		}

		private static ClassProbability[] toProbabilitiesArray(DataRow row) {
			ClassProbability[] result = new ClassProbability[row.getNumCells()];

			int idx = 0;
			Iterator<DataCell> it = row.iterator();
			while (it.hasNext()) {
				DoubleCell cell = (DoubleCell) it.next();
				result[idx] = new ClassProbability(idx, cell.getDoubleValue());
				idx++;
			}

			return result;
		}
	}

	private static class ClassProbability implements Comparable<ClassProbability> {
		private int index;
		private double probability;

		public ClassProbability(int index, double probability) {
			this.index = index;
			this.probability = probability;
		}

		public int getIndex() {
			return index;
		}

		@Override
		public int compareTo(ClassProbability o) {
			return Double.compare(o.probability, probability);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ClassProbability) {
				return this.compareTo((ClassProbability) obj) == 0;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Double.hashCode(probability);
		}
	}
}
