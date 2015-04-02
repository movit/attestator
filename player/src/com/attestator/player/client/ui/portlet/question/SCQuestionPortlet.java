package com.attestator.player.client.ui.portlet.question;

import java.util.Date;

import com.attestator.common.client.helper.WindowHelper;
import com.attestator.common.shared.helper.NullHelper;
import com.attestator.common.shared.vo.AnswerVO;
import com.attestator.common.shared.vo.ChoiceVO;
import com.attestator.common.shared.vo.SCQAnswerVO;
import com.attestator.common.shared.vo.SingleChoiceQuestionVO;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.client.util.ToggleGroup;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutData;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer.HBoxLayoutAlign;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.form.Radio;

public class SCQuestionPortlet implements QuestionPortlet {
    protected ValueChangeHandler<HasValue<Boolean>> handler = new ValueChangeHandler<HasValue<Boolean>>() {
        @Override
        public void onValueChange(ValueChangeEvent<HasValue<Boolean>> event) {
            AnswerVO newValue = getValue((Radio)event.getValue());
            ValueChangeEvent.fire(SCQuestionPortlet.this, newValue);        
        }
    };
    
    
    private HandlerManager           handlerManager;
    private boolean                  enabled = true;
    private HTML                     questionHtml = new HTML(); 
    private VerticalLayoutContainer  vl = new VerticalLayoutContainer();
    private ToggleGroup              tg = new ToggleGroup();
    private SingleChoiceQuestionVO   question;
    
    public SCQuestionPortlet() {
        tg.addValueChangeHandler(handler);
        clear();
    }

    private void clear() {
        tg.clear();
        vl.clear();
        WindowHelper.setElementMargins(questionHtml.getElement(), 10, 0, 10, 0, Unit.PX);
        vl.add(questionHtml, new VerticalLayoutData(1, -1, new Margins(0, 0, 0, 0)));
    }
    
    public void init(SingleChoiceQuestionVO question) {
        clear();
        this.question = question;
        questionHtml.setText(question.getText());
        
        for (ChoiceVO choice: question.getChoices()) {
            HBoxLayoutContainer hBox = new HBoxLayoutContainer(HBoxLayoutAlign.TOP);            
            
            final Radio radio = new Radio();
            radio.setBoxLabel(" ");
            radio.setData("choice", choice);
            radio.setEnabled(enabled);
            tg.add(radio);
            
            hBox.add(radio, new BoxLayoutData(new Margins(5, 0, 0, 5)));
            
            HTML  choiceHtml = new HTML();
            choiceHtml.setText(choice.getText());
            choiceHtml.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (enabled) {
                        radio.setValue(true, true);
                    }
                }
            });

            BoxLayoutData bl = new BoxLayoutData(new Margins(5, 0, 0, 5));
            bl.setFlex(1);
            hBox.add(choiceHtml, bl);
            
            vl.add(hBox, new VerticalLayoutData(1, -1, new Margins(0, 0, 0, 0)));
        }
    }
    
    @Override
    public Widget asWidget() {
        return vl;
    }

    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<AnswerVO> handler) {
        return ensureHandlers().addHandler(ValueChangeEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        if (handlerManager != null) {
            handlerManager.fireEvent(event);
        }
    }

    private AnswerVO getValue(Radio radio) {
        if (radio == null) {
            return null;
        }
        
        ChoiceVO choice = radio.getData("choice");
        
        SCQAnswerVO result = new SCQAnswerVO();
        result.setQuestionId(question.getId());
        result.setTime(new Date());
        result.setChoiceId(choice.getId());
        
        return result;
    }
    
    @Override
    public AnswerVO getValue() {
        Radio radio = (Radio)tg.getValue();
        return getValue(radio);
    }

    @Override
    public void setValue(AnswerVO value) {
        setValue(value, false);        
    }

    @Override
    public void setValue(AnswerVO value, boolean fireEvents) {        
        Radio radio = null;
        
        if (value instanceof SCQAnswerVO) {
            SCQAnswerVO scqAnswer = (SCQAnswerVO) value;
            for (HasValue<Boolean> nextItem: tg) {
                Radio nextRadio = (Radio) nextItem;
                ChoiceVO nextChoice = nextRadio.getData("choice");                
                if (NullHelper.nullSafeEquals(scqAnswer.getChoiceId(), nextChoice.getId())) {
                    radio = nextRadio;
                    break;
                }
            }
        }

        if (radio == null) {
            value = null;
        }
        
        AnswerVO oldValue = getValue();        
        
        if (tg.getValue() != radio) {
            tg.setValue(radio);
        }
        
        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean value) {
        if (enabled != value) {
            for (HasValue<Boolean> hv: tg) {
                Radio radio = (Radio) hv;
                radio.setEnabled(value);
            }
            enabled = value;
        }
    }
    
    protected HandlerManager ensureHandlers() {
        return handlerManager == null ? handlerManager = new HandlerManager(this) : handlerManager;
    }
}
