package com.attestator.admin.client.ui.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.GridView.GridAppearance;
import com.sencha.gxt.widget.core.client.tree.Tree.TreeAppearance;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

public class TreeGridExt<M> extends TreeGrid<M> {
    public TreeGridExt(TreeStore<M> store, ColumnModel<M> cm,
            ColumnConfig<M, ?> treeColumn, GridAppearance appearance,
            TreeAppearance treeAppearance) {
        super(store, cm, treeColumn, appearance, treeAppearance);
    }

    public TreeGridExt(TreeStore<M> store, ColumnModel<M> cm,
            ColumnConfig<M, ?> treeColumn, GridAppearance appearance) {
        super(store, cm, treeColumn, appearance);
    }

    public TreeGridExt(TreeStore<M> store, ColumnModel<M> cm,
            ColumnConfig<M, ?> treeColumn) {
        super(store, cm, treeColumn);
    }
    
    @Override
    protected void onDoubleClick(Event e) {
        // Copy paste from Grid. We do not need TreeGrid staff here
        Element target = Element.as(e.getEventTarget());
        int rowIndex = view.findRowIndex(target);
        if (rowIndex != -1) {
          int colIndex = view.findCellIndex(target, null);
          if (colIndex != -1) {
            fireEvent(new CellDoubleClickEvent(rowIndex, colIndex, e));
          }
          fireEvent(new RowDoubleClickEvent(rowIndex, colIndex, e));
        }
    }    
}
