package com.attestator.player.client.ui;

import com.attestator.common.shared.helper.NullHelper;
import com.attestator.common.shared.helper.ReportHelper;
import com.attestator.common.shared.helper.ReportHelper.ReportType;
import com.attestator.common.shared.vo.ReportVO;
import com.attestator.player.client.MainScreen;
import com.attestator.player.client.Player;
import com.attestator.player.client.helper.HistoryHelper.HistoryToken;
import com.attestator.player.client.helper.WindowHelper;
import com.attestator.player.client.rpc.PlayerAsyncCallback;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;

public class ReportScreen extends MainScreen {
    public static final String HISTORY_TOKEN = "report";
    private static ReportScreen instance;
    
    private VerticalLayoutContainer vl = new VerticalLayoutContainer();
    private ContentPanel            cp = new ContentPanel();
    
    public ReportScreen() {
        cp.getElement().getStyle().setWidth(600, Unit.PX);
        cp.getElement().getStyle().setProperty("margin", "auto");        
        cp.getElement().getStyle().setMarginTop(40, Unit.PX);
        cp.setHeaderVisible(false);
        cp.add(vl);
    }
    
    private void clear() {
        vl.clear();
    }
    
    private void initEmpty() {
        clear();
    }
    
    private void initFromReport(ReportVO report) {
        clear();
        String reportText = ReportHelper.getReport(report, ReportType.onlyErrors);
        HTML reportHtml = new HTML(reportText);
        vl.add(new Hyperlink("доступные тесты", newToken("publications")), new VerticalLayoutData(-1, -1, new Margins(10)));
        vl.add(reportHtml, new VerticalLayoutData(-1, -1, new Margins(10)));
        vl.add(new Hyperlink("доступные тесты", newToken("publications")), new VerticalLayoutData(-1, -1, new Margins(10)));
        cp.forceLayout();
    }
    
    public static ReportScreen instance() {
        if (instance == null) {
            instance = new ReportScreen();
        }
        return instance;
    }

    @Override
    public Widget asWidget() {
        return cp;
    }

    @Override
    public void initContent(HistoryToken token) {
        clear();
        String reportId = token.getProperties().get("id");
        
        if (TestScreen.instance().getReport() != null && NullHelper.nullSafeEquals(TestScreen.instance().getReport().getId(), reportId)) {
            initFromReport(TestScreen.instance().getReport());
            return;
        }
        
        vl.mask("Загрузка...");
        Player.rpc.getReport(getTenantId(), reportId, new PlayerAsyncCallback<ReportVO>() {
            @Override
            public void onSuccess(ReportVO result) {
                vl.unmask();
                if (result == null) {
                    initEmpty();
                    WindowHelper.setBrowserWindowTitle("Отчет");
                }
                else {
                    initFromReport(result);
                    WindowHelper.setBrowserWindowTitle("Отчет - " + result.getPublication().getMetatest().getName());
                }
            }
            
            @Override
            public void onFailure(Throwable caught) {
                vl.unmask();
                super.onFailure(caught);
                WindowHelper.setBrowserWindowTitle("Отчет");
            }
        });
    }
}
