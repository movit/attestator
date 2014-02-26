package com.attestator.admin.client.ui.question;

import java.util.Collections;
import java.util.Comparator;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.helper.ClientHelper;
import com.attestator.admin.client.props.MilisecondsPropertyEditor;
import com.attestator.admin.client.rpc.AdminAsyncCallback;
import com.attestator.admin.client.rpc.AdminAsyncEmptyCallback;
import com.attestator.admin.client.ui.EditMode;
import com.attestator.admin.client.ui.event.SaveEvent;
import com.attestator.admin.client.ui.event.SaveEvent.HasSaveEventHandlers;
import com.attestator.admin.client.ui.event.SaveEvent.SaveHandler;
import com.attestator.admin.client.ui.widgets.ChoicesList;
import com.attestator.admin.client.ui.widgets.ChoicesListItem;
import com.attestator.admin.client.ui.widgets.GroupsComboBox;
import com.attestator.admin.client.ui.widgets.HtmlEditorExt;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.ChoiceVO;
import com.attestator.common.shared.vo.QuestionVO;
import com.attestator.common.shared.vo.SingleChoiceQuestionVO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HasHideHandlers;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.DoublePropertyEditor;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.LongPropertyEditor;

public class SCQWindow implements IsWidget, Editor<SingleChoiceQuestionVO>, HasSaveEventHandlers<QuestionVO>,  HasHideHandlers {
    interface DriverImpl extends
            SimpleBeanEditorDriver<SingleChoiceQuestionVO, SCQWindow> {
    }    
    
    
    public static interface TextTemplates extends XTemplates{
      @XTemplate("<div style='background-color: #FFFF99; border: 1px solid; padding: 5px;'>Вы не можете редактировать этот вопрос потому, что он создан пользователем <b>{ownerUsername}</b>. Но вы можете создать копию этого вопроса и редактировать уже ее.</div>")
      public SafeHtml readOnlyBanner(String ownerUsername);
    }
    public static final TextTemplates TEMPLATES = GWT.create(TextTemplates.class);
    
    interface UiBinderImpl extends UiBinder<Widget, SCQWindow> {
    }

    private DriverImpl driver = GWT.create(DriverImpl.class);    
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    private static Comparator<ChoiceVO> choicesOrderComparator = new Comparator<ChoiceVO>() {
        @Override
        public int compare(ChoiceVO o1, ChoiceVO o2) {
            return o1.getOrderOrZero() - o2.getOrderOrZero();
        }
    };
    
    @UiField
    Window window;
    
    @UiField(provided = true)
    NumberFormat longNumberFormat = NumberFormat.getDecimalFormat();
    
    @UiField
    @Ignore
    HTML readOnlyBanner;
    
    @UiField(provided = true)
    NumberFormat doubleNumberFormat = NumberFormat.getFormat("0.00");
    
    @UiField(provided = true)
    NumberPropertyEditor<Long> longPropertyEditor = new LongPropertyEditor();    
    
    @UiField(provided = true)
    NumberPropertyEditor<Double> doublePropertyEditor = new DoublePropertyEditor();
    
    @UiField(provided = true)
    NumberPropertyEditor<Long> milisecondsPropertyEditor = new MilisecondsPropertyEditor();
    
    @UiField
    protected GroupsComboBox groupId;    
    @UiField
    protected HtmlEditorExt text;
    @UiField
    protected NumberField<Long> maxQuestionAnswerTime;
    @UiField
    protected NumberField<Double> score;
    @UiField
    protected NumberField<Double> penalty;
    @UiField
    protected ChoicesList choices;    
    @UiField
    protected CheckBox randomChoiceOrder;
    @UiField
    protected VerticalLayoutContainer top;    
    @UiField
    @Ignore
    protected TextButton saveButton;    
    @UiField
    @Ignore
    protected TextButton cancelButton;
    
    private SCQWindow(SingleChoiceQuestionVO question) {
        super();        

        if (question.getChoices().size() < 1) {
            question.getChoices().add(new ChoiceVO());
        }
        
        uiBinder.createAndBindUi(this);
        
        driver.initialize(this);
        driver.edit(question);
        
        if (ClientHelper.isOwnedByOthertUser(question)) {
            switchToReadOnly(question);
        }
    }
    
    private void switchToReadOnly(QuestionVO question) {
        readOnlyBanner.setVisible(true);
        readOnlyBanner.setHTML(TEMPLATES.readOnlyBanner(question.getOwnerUsername()));
        saveButton.setVisible(false);
        cancelButton.setText("Закрыть");

        groupId.disable();
        text.disable();
        maxQuestionAnswerTime.disable();
        score.disable();
        penalty.disable();
        choices.disable();
        randomChoiceOrder.disable();
    }
    
    @UiHandler("cancelButton")
    protected void cancelButtonClick(SelectEvent event) {
        window.hide();
    }

    @UiHandler("saveButton")
    protected void saveButtonClick(SelectEvent event) {
        SingleChoiceQuestionVO question = driver.flush();
        Collections.sort(question.getChoices(), choicesOrderComparator);
        
        if (validate(question)) {
            Admin.RPC.saveQuestion(question, new AdminAsyncEmptyCallback<Void>());
            fireEvent(new SaveEvent<SingleChoiceQuestionVO>(question));
            window.hide();
        }
    }
    
    private boolean validate(SingleChoiceQuestionVO question) {
        StringBuilder sb = new StringBuilder();
        
        Widget ensureVisibleWidget = null;
        Component focusWidget = null;
        
        String plainText = StringHelper.stripHtmlTags(question.getText());
        if (StringHelper.isEmptyOrNull(plainText)) {
            sb.append("Текст вопроса не может быть пустым" + "<br>");            
            if (ensureVisibleWidget == null) {
                ensureVisibleWidget = text;
                focusWidget = text;
            }
        }
        
        for (ChoiceVO choice: question.getChoices()) {
            if (StringHelper.isEmptyOrNull(choice.getText())) {
                sb.append("Текст ответа не может быть пустым" + "<br>");

                if (ensureVisibleWidget == null) {
                    ChoicesListItem emptyItem = choices.getEmptyChoiceItem();
                    if (emptyItem != null) {
                        ensureVisibleWidget = emptyItem;
                        focusWidget = emptyItem.getTextField();
                    }
                }
                
                break;
            }
        }
        
        boolean rightFound = false;
        for (ChoiceVO choice: question.getChoices()) {
            if (choice.isThisRight()) {
                rightFound = true;
                break;
            }
        }
        
        if (!rightFound) {
            sb.append("Ни один ответ не помечен как правильный");
            
            if (ensureVisibleWidget == null) {
                ensureVisibleWidget = choices;
                focusWidget = choices;
            }
        }
        
        if (sb.length() > 0) {
            AlertMessageBox alert = new AlertMessageBox("Ошибка", sb.toString());
            
            if (ensureVisibleWidget != null) {
                final Widget finalEnsureVisibleWidget = ensureVisibleWidget;
                final Component finalFocusWiget = focusWidget;
                alert.addHideHandler(new HideHandler() {
                    @Override
                    public void onHide(HideEvent event) {
                        Scheduler.get().scheduleDeferred(new ScheduledCommand() {                            
                            @Override
                            public void execute() {
                                top.getScrollSupport().ensureVisible(finalEnsureVisibleWidget);
                                if (finalFocusWiget != null) {
                                    finalFocusWiget.focus();
                                }
                            }
                        });
                    }
                });
            }
            
            alert.show();
            return false;
        }
        
        return true;
    }
    
    @Override
    public Window asWidget() {
        return window;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        window.fireEvent(event);        
    }

    @Override
    public HandlerRegistration addSaveHandler(SaveHandler<QuestionVO> handler) {
        return window.addHandler(handler, SaveEvent.getType());
    }

    @Override
    public HandlerRegistration addHideHandler(HideHandler handler) {
        return window.addHideHandler(handler);
    }
    
    public static void showWindow(final EditMode editMode, String id, final SaveHandler<QuestionVO> saveHandler, final HideHandler hideHandler) {
        final AdminAsyncCallback<SingleChoiceQuestionVO> showWindowCallback = new AdminAsyncCallback<SingleChoiceQuestionVO>() {
            @Override
            public void onSuccess(SingleChoiceQuestionVO result) {
                SCQWindow window = new SCQWindow(result);
                if (hideHandler != null) {
                    window.addHideHandler(hideHandler);
                }
                if (saveHandler != null) {                
                    window.addSaveHandler(saveHandler);
                }
                window.asWidget().show();                
            }
        };
        
        switch (editMode) {
        case etExisting:
            Admin.RPC.get(SingleChoiceQuestionVO.class.getName(), id, showWindowCallback);
            break;
        case etCopy:
            Admin.RPC.copy(SingleChoiceQuestionVO.class.getName(), id, showWindowCallback);
            break;
        case etNew:
            showWindowCallback.onSuccess(new SingleChoiceQuestionVO());
            break;       
        }
    }
}
