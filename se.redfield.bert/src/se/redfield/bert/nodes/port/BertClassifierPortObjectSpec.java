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
public class BertClassifierPortObjectSpec extends AbstractSimplePortObjectSpec implements BertPortObjectSpecBase {

	/**
	 * The serializer for the {@link BertClassifierPortObjectSpec}
	 *
	 */
	public static final class Serializer extends AbstractSimplePortObjectSpecSerializer<BertClassifierPortObjectSpec> {
	}

	private static final String KEY_MAX_SEQ_LENGTH = "maxSeqLength";
	private static final String KEY_MULTILABEL = "multiLabel";
	private static final String KEY_CLASS_SEPARATOR = "classSeparator";
	private static final String KEY_BERT_MODEL_TYPE = "bertModelType";

	private int maxSeqLength;
	private boolean multiLabel;
	private String classSeparator;
	private BertModelType modelType;

	/**
	 * Creates new instance.
	 */
	public BertClassifierPortObjectSpec() {
		this(0, false, "", BertModelType.TFHUB);
	}

	/**
	 * @param maxSeqLength   the max sequence length.
	 * @param multiLabel     whenever the multilabel classification mode is used.
	 * @param classSeparator class separator character.
	 * @param modelType      Bert model type.
	 */
	public BertClassifierPortObjectSpec(int maxSeqLength, boolean multiLabel, String classSeparator,
			BertModelType modelType) {
		this.maxSeqLength = maxSeqLength;
		this.multiLabel = multiLabel;
		this.classSeparator = classSeparator;
		this.modelType = modelType;
	}

	/**
	 * @return the max sequence length.
	 */
	public int getMaxSeqLength() {
		return maxSeqLength;
	}

	/**
	 * @return whenever the multi-label classification mode is used.
	 */
	public boolean isMultiLabel() {
		return multiLabel;
	}

	/**
	 * @return the class separator value used during training in case of multi-label
	 *         classification mode.
	 */
	public String getClassSeparator() {
		return classSeparator;
	}

	/**
	 * @return Bert model type.
	 */
	public BertModelType getModelType() {
		return modelType;
	}

	@Override
	protected void save(ModelContentWO model) {
		model.addInt(KEY_MAX_SEQ_LENGTH, maxSeqLength);
		model.addBoolean(KEY_MULTILABEL, multiLabel);
		model.addString(KEY_CLASS_SEPARATOR, classSeparator);
		modelType.save(model, KEY_BERT_MODEL_TYPE);
	}

	@Override
	protected void load(ModelContentRO model) throws InvalidSettingsException {
		maxSeqLength = model.getInt(KEY_MAX_SEQ_LENGTH);
		multiLabel = model.getBoolean(KEY_MULTILABEL);
		classSeparator = model.getString(KEY_CLASS_SEPARATOR);
		modelType = BertModelType.load(model, KEY_BERT_MODEL_TYPE);
	}

	@Override
	public BertPortObjectType getType() {
		return BertPortObjectType.CLASSIFIER;
	}

}
