package com.attestator.admin.client.ui.widgets;

import com.attestator.admin.client.ui.event.DeleteEvent;
import com.attestator.admin.client.ui.event.DeleteEvent.DeleteHandler;
import com.attestator.admin.client.ui.event.DeleteEvent.HasDeleteEventHandlers;
import com.attestator.admin.client.ui.event.RearrangeEvent;
import com.attestator.admin.client.ui.event.RearrangeEvent.HasRearrangeEventHandlers;
import com.attestator.admin.client.ui.event.RearrangeEvent.RearrangeHandler;
import com.attestator.common.shared.vo.AdditionalQuestionVO;
import com.attestator.common.shared.vo.AdditionalQuestionVO.AnswerTypeEnum;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.EditorDelegate;
import com.google.gwt.editor.client.ValueAwareEditor;
import com.google.gwt.editor.client.adapters.SimpleEditor;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class AdditionalQuestionItem extends Composite implements ValueAwareEditor<AdditionalQuestionVO>, HasDeleteEventHandlers, HasRearrangeEventHandlers {
    interface UiBinderImpl extends UiBinder<Widget, AdditionalQuestionItem> {
    }
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    
    @UiField
    @Ignore
    ToolBar container;
    
    @UiField
    @Ignore
    TextButton deleteButton;
    
    @UiField(provided = true)
    EnumComboBox<AdditionalQuestionVO.AnswerTypeEnum> answerType = new EnumComboBox<AdditionalQuestionVO.AnswerTypeEnum>(AdditionalQuestionVO.AnswerTypeEnum.class);
    
    @UiField
    TextField text;

    @UiField
    TextField checkValue;
    
    @UiField
    CheckBox required; 
    
    protected SimpleEditor<Integer> order = SimpleEditor.of();
    
    @UiConstructor
    public AdditionalQuestionItem() {
        initWidget(uiBinder.createAndBindUi(this));
        answerType.addValueChangeHandler(new ValueChangeHandler<AdditionalQuestionVO.AnswerTypeEnum>() {            
            @Override
            public void onValueChange(ValueChangeEvent<AnswerTypeEnum> event) {
                switchToKeyItem(event.getValue() == AnswerTypeEnum.key);
            }
        });
        answerType.addSelectionHandler(new SelectionHandler<AdditionalQuestionVO.AnswerTypeEnum>() {
            @Override
            public void onSelection(SelectionEvent<AnswerTypeEnum> event) {
                switchToKeyItem(event.getSelectedItem() == AnswerTypeEnum.key);
            }
        });
    }
    
    private void switchToKeyItem(boolean on) {
        if (checkValue.isVisible() != on) {
            checkValue.setVisible(on);
            if (on) {
                required.setValue(on);
            }
            required.setEnabled(!on);
            container.forceLayout();
        }
    }
    
    @Override
    public final HandlerRegistration addDeleteHandler(DeleteHandler handler) {
        return addHandler(handler, DeleteEvent.getType());
    }
    
    @Override
    public HandlerRegistration addRearrangeHandler(RearrangeHandler handler) {
        return addHandler(handler, RearrangeEvent.getType());
    } 
    
    @UiHandler("deleteButton")
    public void deleteButtonClick(SelectEvent event) {
        fireEvent(new DeleteEvent());
    }

    @UiHandler("upButton")
    public void upButtonClick(SelectEvent event) {
        fireEvent(new RearrangeEvent(true));
    }

    @UiHandler("downButton")
    public void downButtonClick(SelectEvent event) {
        fireEvent(new RearrangeEvent(false));
    }
    
    @Ignore
    public TextButton getDeleteButton() {
        return deleteButton;
    }
    
    @Override
    public void setDelegate(EditorDelegate<AdditionalQuestionVO> delegate) {
    }

    @Override
    public void flush() {
    }

    @Override
    public void onPropertyChange(String... paths) {
    }

    @Override
    public void setValue(AdditionalQuestionVO value) {
        switchToKeyItem(value.getAnswerType() == AnswerTypeEnum.key);
    }

    @Ignore
    public TextField getTextField() {
        return text;
    }

    @Ignore
    public TextField getCheckValueField() {
        return checkValue;
    }
}
