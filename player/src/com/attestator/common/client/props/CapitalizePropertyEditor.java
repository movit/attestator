package com.attestator.common.client.props;

import java.text.ParseException;

import com.attestator.common.shared.helper.StringHelper;
import com.sencha.gxt.widget.core.client.form.PropertyEditor;

public class CapitalizePropertyEditor extends PropertyEditor<String> {
	public static final CapitalizePropertyEditor DEFAULT = new CapitalizePropertyEditor();

	@Override
	public String parse(CharSequence text) throws ParseException {
		String result = StringHelper.capitalizeAll(text.toString());
		result = StringHelper.nullSafeTrim(result);
		return result;
	}

	@Override
	public String render(String text) {
		String result = text == null ? "" : StringHelper.capitalizeAll(text);
		result = StringHelper.nullSafeTrim(result);
		return result;
	}
}