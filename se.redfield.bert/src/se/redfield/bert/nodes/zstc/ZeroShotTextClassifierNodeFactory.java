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

package se.redfield.bert.nodes.zstc;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * This is an example implementation of the node factory of the
 * "ZeroShotTextClassifier" node.
 *
 * @author Abderrahim Alakouche
 */

public class ZeroShotTextClassifierNodeFactory extends NodeFactory<ZeroShotTextClassifierNodeModel> {


    @Override
    public ZeroShotTextClassifierNodeModel createNodeModel() {
        return new ZeroShotTextClassifierNodeModel();
    }

    @Override
    public int getNrNodeViews() {
        return 0;
    }

    @Override
    public NodeView<ZeroShotTextClassifierNodeModel> createNodeView(int viewIndex, ZeroShotTextClassifierNodeModel nodeModel) {
		return null;
    }

    
    @Override
    public boolean hasDialog() {
        return true;
    }

    
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new ZeroShotTextClassifierNodeDialog();
    }

}

