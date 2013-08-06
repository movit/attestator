package com.attestator.common.client.ui.widgets;


import java.text.ParseException;

import com.attestator.common.client.props.CapitalizePropertyEditor;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.sencha.gxt.cell.core.client.form.TextInputCell;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.form.TextField;

public class NameField extends TextField {	
	static class NameFieldCell extends TextInputCell {
    	  @Override
    	  public void finishEditing(Element parent, String value, Object key, ValueUpdater<String> valueUpdater) {
      	    	String newValue = getText(XElement.as(parent));
      	    	if ("".equals(newValue)) {
      	    		newValue = null;
      	    	}
      	    	else {
	      	    	try {
						newValue = getPropertyEditor().parse(newValue);
					} catch (ParseException e) {
						newValue = null;
					}
      	    	}
      	    	setText(XElement.as(parent), newValue);      	    	
      	    	super.finishEditing(parent, newValue, key, valueUpdater);
    	  }
    }

	public NameField() {
		super(new NameFieldCell(), CapitalizePropertyEditor.DEFAULT);		
	}
}
