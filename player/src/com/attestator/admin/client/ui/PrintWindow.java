package com.attestator.admin.client.ui;

import br.com.freller.tool.client.Print;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.props.IntegerGreaterZerroPropertyEditor;
import com.attestator.admin.client.rpc.AdminAsyncCallback;
import com.attestator.admin.client.rpc.AdminAsyncUnmaskCallback;
import com.attestator.common.client.helper.WindowHelper;
import com.attestator.common.shared.vo.PrintingPropertiesVO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.editor.client.adapters.SimpleEditor;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.HtmlEditor;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.form.SpinnerField;

public class PrintWindow implements IsWidget, Editor<PrintingPropertiesVO>{
    public static enum Mode {
        print,
        saveAsPdf
    }
    interface DriverImpl extends
    
    SimpleBeanEditorDriver<PrintingPropertiesVO, PrintWindow> {
    }
    interface UiBinderImpl extends UiBinder<Window, PrintWindow> {
    }
    private DriverImpl driver = GWT.create(DriverImpl.class);

    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    
    private Mode mode;
    
    @UiField
    HtmlEditor titlePage;
    
    @UiField
    Window window;
    
    @Path("metatest.name")
    @UiField
    Label metatestName;
    
    SimpleEditor<String> metatestId;
    
    SimpleEditor<Integer> printAttempt;
    
    @UiField
    CheckBox randomQuestionsOrder;
    
    @UiField
    CheckBox doublePage;
    
    @UiField
    CheckBox onePdfPerVariant;
    
    @UiField
    @Ignore
    TextButton printButton;
    
    @UiField(provided = true) 
    SpinnerField<Integer> variantsCount = createVariantsCount();
    private SpinnerField<Integer> createVariantsCount() {
        NumberPropertyEditor<Integer> integerPropertyEditor = new IntegerGreaterZerroPropertyEditor();
        final SpinnerField<Integer> result = new SpinnerField<Integer>(integerPropertyEditor);
        result.setValue(1);
        result.setMinValue(1);
        result.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                if (event.getValue() == null) {
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        @Override
                        public void execute() {
                            result.setValue(1, false);
                        }
                    });                    
                }
            }
        });
            
        return result;
    }
        
    public PrintWindow(PrintingPropertiesVO properties, Mode mode) {
        this.mode = mode;
       
        uiBinder.createAndBindUi(this);
        
        switch (mode) {
        case print:
            onePdfPerVariant.hide();
            break;
        case saveAsPdf:
            printButton.setText("Сохранить в PDF");
            break;
        }

        driver.initialize(this);
        driver.edit(properties);
    }
    
    @UiHandler("cancelButton")
    protected void cancelButtonClick(SelectEvent event) {
        window.hide();
    }
    
    @UiHandler("printButton")
    protected void printButtonClick(SelectEvent event) {
        final PrintingPropertiesVO properties = driver.flush();
        
        WindowHelper.mask("Подготовка к печати ...");
        Admin.RPC.savePrintingProperties(properties, new AdminAsyncUnmaskCallback<Void>() {            
            @Override
            public void onSuccess(Void result) {                
                if (mode == Mode.print) {
                    Admin.RPC.getHtmlForPrinting(properties.getId(), new AdminAsyncUnmaskCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            super.onSuccess(result);
                            Print.it(result);                       
                        }
                    });
                }                
            }            
        });
        
        window.hide();
    }

    
    @Override
    public Window asWidget() {
        return window;
    }
    
    public static void showWindow(final String metatestId, final Mode mode) {
        Admin.RPC.getPrintPropertiesByMetatestId(metatestId, new AdminAsyncCallback<PrintingPropertiesVO>() {
            @Override
            public void onSuccess(PrintingPropertiesVO result) {
                PrintWindow window = new PrintWindow(result, mode);
                window.asWidget().show();                
            }
        });
    }
}
