package com.attestator.player.client.ui.portlet;

import java.util.ArrayList;
import java.util.List;

import com.attestator.common.client.ui.widgets.NameField;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.AdditionalQuestionAnswerVO;
import com.attestator.common.shared.vo.AdditionalQuestionVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.ReportVO;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.FormPanel;
import com.sencha.gxt.widget.core.client.form.IsField;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.Validator;
import com.sencha.gxt.widget.core.client.form.validator.RegExValidator;

public class PublicationPortlet implements IsWidget {
    private VerticalLayoutContainer   vl = new VerticalLayoutContainer();
    private FormPanel                 frm = new FormPanel();
    private HTML                      introduction = new HTML();
    private List<TextField>           fields = new ArrayList<TextField>();    
    
    public PublicationPortlet() {
        frm.add(vl);
    }
    
    private void clear() {
        vl.clear();
        introduction = new HTML();
        fields = new ArrayList<TextField>();    
    }
    
    public  void init(PublicationVO publication) {
        clear();
        
        if (publication == null) {
            return;
        }
        
        if (publication.getIntroduction() != null) {
            introduction.setText(publication.getIntroduction());
            vl.add(introduction, new VerticalLayoutData(-1, -1, new Margins(5, 0, 20, 0)));
        }
        
        if (publication.isThisAskLastName()) {
        	addNameField("lastName", "Фамилия", publication.isThisAskLastNameRequired(), null);
        }
        if (publication.isThisAskFirstName()) {
        	addNameField("firstName", "Имя", publication.isThisAskFirstNameRequired(), null);
        }
        if (publication.isThisAskMiddleName()) {
        	addNameField("middleName", "Отчество", publication.isThisAskMiddleNameRequired(), null);
        }
        if (publication.isThisAskEmail()) {
            addQuestionField("email", "email", publication.isThisAskEmailRequired(), new RegExValidator("^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$", "Неправильный email"));
        }
        
        for (AdditionalQuestionVO aq: publication.getAdditionalQuestions()) {
            Validator<String> validator = null;
            if (aq.getCheckValue() != null) {
                String regex = "^" + StringHelper.escapeRegexpLiteral(aq.getCheckValue()) + "$";
                validator = new RegExValidator(regex, "Неправильное значение");
            }
            addQuestionField(aq, aq.getText(), aq.getRequired(), validator);
        }
        frm.forceLayout();
    }

    private TextField addQuestionField(TextField tf, Object question, String text, boolean required, Validator<String> validator) {
        tf.setData("question", question);        
        if (required) {
            tf.setAllowBlank(false);
        }
        if (validator != null) {
            tf.addValidator(validator);
        }
        fields.add(tf);
        vl.add(new FieldLabel(tf, text), new VerticalLayoutData());
        return tf;
    }
    
    private TextField addQuestionField(Object question, String text, boolean required, Validator<String> validator) {
        TextField tf = new TextField();
        return addQuestionField(tf, question, text, required, validator);
    }

    private TextField addNameField(Object question, String text, boolean required, Validator<String> validator) {
        NameField tf = new NameField();
        return addQuestionField(tf, question, text, required, validator);
    }
    
    public void focusInvalid() {        
        for (IsField<?> fld: frm.getFields()) {
            if (!fld.isValid(true)) {
                ((Component) fld).focus();
                break;
            }
        }
    }
    
    public void fillReport(ReportVO report) {
        for (IsField<?> isf: frm.getFields()) {
            TextField tf = (TextField) isf;
            
            Object data = tf.getData("question");
            
            if ("email".equals(data)) {
                report.setEmail(tf.getText());
            } else if ("firstName".equals(data)) {
                report.setFirstName(tf.getText());
            } else if ("lastName".equals(data)) {
                report.setLastName(tf.getText());
            } else if ("middleName".equals(data)) {
                report.setMiddleName(tf.getText());
            } else {
                AdditionalQuestionVO question = (AdditionalQuestionVO)data;
                AdditionalQuestionAnswerVO answer = new AdditionalQuestionAnswerVO();
                answer.setQuestionId(question.getId());
                answer.setQuestion(question.getText());
                answer.setAnswer(tf.getText());
                
                if (question.getCheckValue() != null) {
                    answer.setValueChecked(question.getCheckValue().equals(answer.getAnswer()));
                }
                
                report.getAdditionalAnswers().add(answer);
            }
        }
    }
    
    public boolean isValid() {
        return frm.isValid();
    }
    
    @Override
    public Widget asWidget() {
        return frm;
    }
}
