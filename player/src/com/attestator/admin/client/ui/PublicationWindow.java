package com.attestator.admin.client.ui;

import java.util.Collections;
import java.util.Comparator;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.props.DoubleGreaterZerroPropertyEditor;
import com.attestator.admin.client.props.IntegerGreaterZerroPropertyEditor;
import com.attestator.admin.client.props.MilisecondsPropertyEditor;
import com.attestator.admin.client.rpc.AdminAsyncCallback;
import com.attestator.admin.client.ui.event.SaveEvent;
import com.attestator.admin.client.ui.event.SaveEvent.HasSaveEventHandlers;
import com.attestator.admin.client.ui.event.SaveEvent.SaveHandler;
import com.attestator.admin.client.ui.widgets.AdditionalQuestionItem;
import com.attestator.admin.client.ui.widgets.AdditionalQuestionsList;
import com.attestator.admin.client.ui.widgets.DateTimeSelector;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.AdditionalQuestionVO;
import com.attestator.common.shared.vo.AdditionalQuestionVO.AnswerTypeEnum;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.ExpandEvent;
import com.sencha.gxt.widget.core.client.event.ExpandEvent.ExpandHandler;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HasHideHandlers;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.FieldSet;
import com.sencha.gxt.widget.core.client.form.HtmlEditor;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;

public class PublicationWindow implements IsWidget, Editor<PublicationVO>, HasSaveEventHandlers<PublicationVO>,  HasHideHandlers {
    interface DriverImpl extends
            SimpleBeanEditorDriver<PublicationVO, PublicationWindow> {
    }

    interface UiBinderImpl extends UiBinder<Widget, PublicationWindow> {
    }
    
    private DriverImpl driver = GWT.create(DriverImpl.class);    
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    
    @UiField
    Window window;
    
    @UiField
    VerticalLayoutContainer top;
    
    @Path("metatest.name")
    @UiField
    Label metatestName;
    
    @UiField
    HtmlEditor introduction;
    
    @UiField 
    DateTimeSelector start;
    
    @UiField 
    DateTimeSelector end;
    
    @UiField 
    NumberField<Integer>  maxAttempts;

    @UiField
    NumberField<Double>   minScore;    
    
    @UiField
    CheckBox              interruptOnFalure;
    
    @UiField
    CheckBox         allowSkipQuestions; 
    
    @UiField
    CheckBox         allowInterruptTest;
    
    @UiField
    CheckBox         randomQuestionsOrder;
    
    @UiField
    CheckBox         askFirstName;
    
    @UiField
    CheckBox         askFirstNameRequired;
    
    @UiField
    CheckBox         askLastName;
    
    @UiField
    CheckBox         askLastNameRequired;

    @UiField
    CheckBox         askMiddleName;
    
    @UiField
    CheckBox         askMiddleNameRequired;

    @UiField
    CheckBox         askEmail;
    
    @UiField
    CheckBox         askEmailRequired;
    
    @UiField
    AdditionalQuestionsList additionalQuestions;
    
    @UiField
    NumberField<Long> maxTakeTestTime;

    @UiField
    NumberField<Long> maxQuestionAnswerTime;

    @UiField(provided = true)
    NumberFormat longNumberFormat = NumberFormat.getDecimalFormat();
    
    @UiField(provided = true)
    NumberFormat doubleNumberFormat = NumberFormat.getFormat("0.00");
    
    @UiField(provided = true)
    NumberPropertyEditor<Integer> integerPropertyEditor = new IntegerGreaterZerroPropertyEditor();    
    
    @UiField(provided = true)
    NumberPropertyEditor<Double> doublePropertyEditor = new DoubleGreaterZerroPropertyEditor();
    
    @UiField(provided = true)
    NumberPropertyEditor<Long> milisecondsPropertyEditor = new MilisecondsPropertyEditor();
    
    @UiField
    FieldSet publicationParamsFieldSet;
    
    @Ignore 
    ListStore<PublicationVO> externalStore;
    
    private String getHeader(EditMode editType) {
        switch (editType) {
        case etCopy:
            return "Публикация - копия";
        case etNew:
            return "Публикация - новая";
        case etExisting:
            return "Публикация - редактирование";
        default:
            return "Публикация";
        }        
    }

    private PublicationWindow(PublicationVO publication, EditMode editMode, ListStore<PublicationVO> externalStore) {
        super();
        
        this.externalStore = externalStore;
        
        uiBinder.createAndBindUi(this);
        publicationParamsFieldSet.addExpandHandler(new ExpandHandler() {            
            @Override
            public void onExpand(ExpandEvent event) {
                top.getScrollSupport().ensureVisible(publicationParamsFieldSet);                
            }
        });
        
        window.setHeadingText(getHeader(editMode));
        
        driver.initialize(this);
        driver.edit(publication);
    }

    @UiHandler("cancelButton")
    protected void cancelButtonClick(SelectEvent event) {
        window.hide();
    }

    private boolean validate(PublicationVO publication) {
        StringBuilder sb = new StringBuilder();
        Widget ensureVisibleWidget = null;
        Component focusWidget = null;
        
        for (AdditionalQuestionVO aq : publication.getAdditionalQuestions()) {
            if (StringHelper.isEmptyOrNull(aq.getText())) {
                sb.append("Название поля не может быть пустым" + "<br>");                
                if (ensureVisibleWidget == null) {
                    AdditionalQuestionItem aqItem = additionalQuestions.getAdditonalQuestionItem(aq.getOrder());
                    ensureVisibleWidget = aqItem.getTextField();
                    focusWidget = aqItem.getTextField();
                }         
            }
            
            if (aq.getAnswerType() == AnswerTypeEnum.key) {
                if (StringHelper.isEmptyOrNull(aq.getCheckValue())) {
                    sb.append("Секретный ключ не может быть пустым" + "<br>");                
                    if (ensureVisibleWidget == null) {
                        AdditionalQuestionItem aqItem = additionalQuestions.getAdditonalQuestionItem(aq.getOrder());
                        ensureVisibleWidget = aqItem.getCheckValueField();
                        focusWidget = aqItem.getCheckValueField();
                    }
                }
            }
        }
            
        if (publication.getStart() != null && publication.getEnd() != null
        &&  publication.getEnd().before(publication.getStart())) {
            sb.append("Начало публикации должно быть раньше окончания" + "<br>");            
            if (ensureVisibleWidget == null) {
                ensureVisibleWidget = start;
                focusWidget = start;
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
    public HandlerRegistration addSaveHandler(SaveHandler<PublicationVO> handler) {
        return window.addHandler(handler, SaveEvent.getType());
    }

    @Override
    public HandlerRegistration addHideHandler(HideHandler handler) {
        return window.addHideHandler(handler);
    }
    
    public static void showWindow(final EditMode editMode, String id, MetaTestVO metatest, final SaveHandler<PublicationVO> saveHandler, final HideHandler hideHandler) {
        showWindow(editMode, id, metatest, null, saveHandler, hideHandler);
    }
    
    public static void showWindow(final EditMode editMode, String id, MetaTestVO metatest, final ListStore<PublicationVO> externalStore, final SaveHandler<PublicationVO> saveHandler, final HideHandler hideHandler) {
        final AdminAsyncCallback<PublicationVO> showWindowCallback = new AdminAsyncCallback<PublicationVO>() {
            @Override
            public void onSuccess(PublicationVO result) {
                PublicationWindow window = new PublicationWindow(result, editMode, externalStore);
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
            if (externalStore != null) {
                PublicationVO publication = externalStore.findModelWithKey(id);
                showWindowCallback.onSuccess(publication);
            }
            else {
                Admin.RPC.get(PublicationVO.class.getName(), id, showWindowCallback);                   
            }
            break;
        case etCopy: {
            if (externalStore != null) {
                PublicationVO publication = externalStore.findModelWithKey(id);
                publication.resetIdentity();
                showWindowCallback.onSuccess(publication);
            }
            else {
                Admin.RPC.copy(PublicationVO.class.getName(), id, showWindowCallback);                   
            }
            break;
        }
        case etNew:
            PublicationVO publication = new PublicationVO();
            publication.setMetatestId(metatest.getId());
            publication.setMetatest(metatest);
            showWindowCallback.onSuccess(publication);
            break;       
        }
    }
    
    
    @UiHandler("saveButton")
    protected void saveButtonClick(SelectEvent event) {
        final PublicationVO publication = driver.flush();
        
        Collections.sort(publication.getAdditionalQuestions(), new Comparator<AdditionalQuestionVO>() {
            @Override
            public int compare(AdditionalQuestionVO o1, AdditionalQuestionVO o2) {
                return o1.getOrder() - o2.getOrder();
            }
        });
        
        if (!validate(publication)) {
            return;
        }
        
        if (externalStore != null) {
            if (externalStore.findModelWithKey(publication.getId()) != null) {
                externalStore.update(publication);
            }
            else {
                externalStore.add(publication);
            }
            window.hide();
            fireEvent(new SaveEvent<PublicationVO>(publication));
        }
        else {
            Admin.RPC.savePublication(publication, new AdminAsyncCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    window.hide();
                    fireEvent(new SaveEvent<PublicationVO>(publication));
                }
            });
        }
    }
}
