package com.attestator.admin.client.ui.widgets;

import com.attestator.common.client.props.EnumLaybelProvider;
import com.attestator.common.client.props.EnumStore;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.widget.core.client.form.ComboBox;

public class EnumComboBox<E extends Enum<E>> extends ComboBox<E> {
    public EnumComboBox(Class<E> clazz) {
        super(new EnumStore<E>(clazz), new EnumLaybelProvider<E>());
        setTriggerAction(TriggerAction.ALL);
        setForceSelection(true);
    }
}
