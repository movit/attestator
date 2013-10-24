package com.attestator.admin.client.ui.widgets;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.shared.HandlerManager;
import com.sencha.gxt.cell.core.client.form.SpinnerFieldCell;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.cell.HandlerManagerContext;
import com.sencha.gxt.widget.core.client.event.CellBeforeSelectionEvent;
import com.sencha.gxt.widget.core.client.event.CellSelectionEvent;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;

public class SpinnerFieldCellExt<N extends Number> extends SpinnerFieldCell<N> {

    public SpinnerFieldCellExt(NumberPropertyEditor<N> propertyEditor) {
        super(propertyEditor);
    }

    public SpinnerFieldCellExt(
            NumberPropertyEditor<N> propertyEditor,
            com.sencha.gxt.cell.core.client.form.SpinnerFieldCell.SpinnerFieldAppearance appearance) {
        super(propertyEditor, appearance);
    }

    @Override
    protected void doSpin(Cell.Context context, XElement parent, N value, ValueUpdater<N> updater, boolean up) {
        if (!isReadOnly()) {
          // use the current value in the input element
          InputElement input = getInputElement(parent);
          String v = input.getValue();

          if (!"".equals(v)) {
            try {
              value = getPropertyEditor().parse(v);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }

          boolean cancelled = false;
          if (context instanceof HandlerManagerContext) {
            HandlerManager manager = ((HandlerManagerContext) context).getHandlerManager();
            CellBeforeSelectionEvent<N> event = CellBeforeSelectionEvent.fire(manager, context, value);
            if (event != null && event.isCanceled()) {
              cancelled = true;
            }
          } else {
            CellBeforeSelectionEvent<N> event = CellBeforeSelectionEvent.fire(this, context, value);
            if (!fireCancellableEvent(event)) {
              cancelled = true;
            }
          }

          if (!cancelled) {
            N newVal = null;
            if (up) {
              newVal = getPropertyEditor().incr(value);
              if (newVal.doubleValue() > getMaxValue(context).doubleValue() || newVal.doubleValue() < getMinValue(context).doubleValue()) {
                return;
              }
              input.setValue(getPropertyEditor().render(newVal));
            } else {
              newVal = getPropertyEditor().decr(value);
              if (newVal.doubleValue() > getMaxValue(context).doubleValue() || newVal.doubleValue() < getMinValue(context).doubleValue()) {
                return;
              }
              input.setValue(getPropertyEditor().render(newVal));
            }
            if (context instanceof HandlerManagerContext) {
              HandlerManager manager = ((HandlerManagerContext) context).getHandlerManager();
              CellSelectionEvent.fire(manager, context, newVal);
            } else {
              CellSelectionEvent.fire(this, context, newVal);
            }
          }
        }
      }
    
}
