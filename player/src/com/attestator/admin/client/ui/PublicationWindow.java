package com.attestator.admin.client.ui;

import com.attestator.admin.client.props.MilisecondsPropertyEditor;
import com.attestator.admin.client.ui.event.CancelEvent;
import com.attestator.admin.client.ui.event.SaveEvent;
import com.attestator.admin.client.ui.event.SaveEvent.HasSaveEventHandlers;
import com.attestator.admin.client.ui.event.SaveEvent.SaveHandler;
import com.attestator.admin.client.ui.widgets.DateTimeSelector;
import com.attestator.common.shared.vo.PublicationVO;
import com.google.gwt.core.client.GWT;
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
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.HideEvent.HasHideHandlers;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.HtmlEditor;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.DoublePropertyEditor;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.LongPropertyEditor;

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
    NumberField<Long> maxTakeTestTime;

    @UiField
    NumberField<Long> maxQuestionAnswerTime;

    @UiField(provided = true)
    NumberFormat longNumberFormat = NumberFormat.getDecimalFormat();
    
    @UiField(provided = true)
    NumberFormat doubleNumberFormat = NumberFormat.getFormat("0.00");
    
    @UiField(provided = true)
    NumberPropertyEditor<Long> longPropertyEditor = new LongPropertyEditor();    
    
    @UiField(provided = true)
    NumberPropertyEditor<Double> doublePropertyEditor = new DoublePropertyEditor();
    
    @UiField(provided = true)
    NumberPropertyEditor<Long> milisecondsPropertyEditor = new MilisecondsPropertyEditor();
    
    public PublicationWindow() {
        this(null); 
    }
    
    public PublicationWindow(PublicationVO publication) {
        super();
        
        uiBinder.createAndBindUi(this);
        
        driver.initialize(this);
        driver.edit(publication);
    }

    @UiHandler("cancelButton")
    protected void cancelButtonClick(SelectEvent event) {
        fireEvent(new CancelEvent());
        window.hide();
    }

    private boolean validate(PublicationVO question) {
        
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

}
