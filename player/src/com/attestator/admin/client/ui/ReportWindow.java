package com.attestator.admin.client.ui;

import com.attestator.common.shared.helper.TestHelper;
import com.attestator.common.shared.helper.TestHelper.ReportType;
import com.attestator.common.shared.vo.ReportVO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.core.client.util.ToggleGroup;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.Radio;

public class ReportWindow implements IsWidget {

    interface UiBinderImpl extends UiBinder<Window, ReportWindow> {
    }

    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);

    private ReportVO report;
    
    @UiField
    Radio fullRadio;

    @UiField
    Radio onlyErrorsRadio;

    @UiField
    Radio errorsAndNotUnsweredRadio;
    
    @UiField
    HTML reportHtml;
    
    @UiField
    Window window;
    
    @UiField
    VerticalLayoutContainer topContainer;
    
    @UiField
    VerticalLayoutContainer scrollContainer;
    
    public ReportWindow(ReportVO value) {
        report = value;
        uiBinder.createAndBindUi(this);
        
        ToggleGroup toggle = new ToggleGroup();
        toggle.add(fullRadio);
        toggle.add(errorsAndNotUnsweredRadio);
        toggle.add(onlyErrorsRadio);
        toggle.addValueChangeHandler(new ValueChangeHandler<HasValue<Boolean>>() {            
            @Override
            public void onValueChange(ValueChangeEvent<HasValue<Boolean>> event) {
                if (errorsAndNotUnsweredRadio.getValue()) {
                    reportHtml.setHTML(TestHelper.getReport(report, ReportType.errorsAndNotUnswered));
                }
                else if (onlyErrorsRadio.getValue()) {
                    reportHtml.setHTML(TestHelper.getReport(report, ReportType.onlyErrors));
                }
                else {
                    reportHtml.setHTML(TestHelper.getReport(report, ReportType.full));
                }
                scrollContainer.getScrollSupport().scrollToTop();
            }
        });
        toggle.setValue(onlyErrorsRadio);
        reportHtml.setHTML(TestHelper.getReport(report, ReportType.onlyErrors));
    }
    
    @UiHandler("cancelButton")
    protected void cancelButtonClick(SelectEvent event) {
        window.hide();
    }
    
    @Override
    public Window asWidget() {
        return window;
    }
}
