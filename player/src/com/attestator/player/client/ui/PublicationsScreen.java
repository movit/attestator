package com.attestator.player.client.ui;

import java.util.List;

import com.attestator.common.client.ui.resolurces.Resources;
import com.attestator.common.shared.helper.HtmlBuilder;
import com.attestator.common.shared.helper.NullHelper;
import com.attestator.player.client.MainScreen;
import com.attestator.player.client.Player;
import com.attestator.player.client.helper.HistoryHelper.HistoryToken;
import com.attestator.player.client.helper.WindowHelper;
import com.attestator.player.client.rpc.PlayerAsyncCallback;
import com.attestator.player.shared.dto.ActivePublicationDTO;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.MarginData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;

public class PublicationsScreen extends MainScreen {
    public static final String HISTORY_TOKEN = "publications";
    private static PublicationsScreen instance;
    
    private VerticalLayoutContainer vl = new VerticalLayoutContainer();
    private ContentPanel            cp = new ContentPanel();
    
    public PublicationsScreen() {
        cp.getElement().getStyle().setWidth(600, Unit.PX);
        cp.getElement().getStyle().setProperty("margin", "auto");        
        cp.getElement().getStyle().setMarginTop(40, Unit.PX);
        cp.setHeadingText("Доступные тесты");        
        cp.add(vl, new MarginData(0, 5, 5, 5));
    }
    
    private void clear() {
        vl.clear();
    }
    
    private void initEmpty() {
        clear();
        vl.add(new HTML("<b>Доступных тестов нет</b>"));
    }
    
    private String preparePublicationItem(ActivePublicationDTO publicationData) {
        HtmlBuilder hb = new HtmlBuilder();
        
        hb.startTag("div", Resources.STYLES.publicationScreenCss().publicationTitle());
        if (publicationData.getPublication().isUnlimitedAttempts() || publicationData.getAttemptsLeft() > 0) {
            hb.addAnchor(
                    "#" + newToken("test", "publicationId", publicationData.getPublication().getId()), 
                    publicationData.getPublication().getMetatest().getName());
        }
        else {
            hb.appendText(publicationData.getPublication().getMetatest().getName());
        }
        hb.endTag("div");
        
        hb.startTag("div", Resources.STYLES.publicationScreenCss().publicationData());
        
        hb.appendText("Осталось попыток: ");
        if (publicationData.getPublication().isUnlimitedAttempts()) {
            hb.appendText("не ограничено");
        }
        else {
            hb.appendText("" + publicationData.getAttemptsLeft());
        }
        hb.appendText(" ");
        
        if (publicationData.getLastFullReportId() != null) {
            hb.startTag("span", Resources.STYLES.publicationScreenCss().publicationReportLink());
            hb.addAnchor(                    
                    "#" + newToken("report", "id", publicationData.getLastFullReportId()),
                    "последний отчет");
            hb.endTag("span");
        }
        
        hb.endTag("div");        
        
        return hb.toString();
    }
    
    private void initFromPublications(List<ActivePublicationDTO> publications) {
        clear();
        
        for (int i = 0; i < publications.size(); i++) {
            ActivePublicationDTO publicationData = publications.get(i);
            String publicationItem = preparePublicationItem(publicationData);
            
            vl.add(new HTML(publicationItem), new VerticalLayoutData(-1, -1, new Margins(8, 0, 0, 0)));
        }
    }
    
    public static PublicationsScreen instance() {
        if (instance == null) {
            instance = new PublicationsScreen();
        }
        return instance;
    }

    @Override
    public Widget asWidget() {        
        return cp;
    }

    @Override
    public void initContent(HistoryToken token) {
        vl.mask("Загрузка...");
        Player.rpc.getActivePulications(getTenantId(), new PlayerAsyncCallback<List<ActivePublicationDTO>>() {
            @Override
            public void onSuccess(List<ActivePublicationDTO> result) {
                vl.unmask();
                if (NullHelper.isEmptyOrNull(result)) {
                    initEmpty();
                }
                else {
                    initFromPublications(result);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                vl.unmask();
                initEmpty();
            }
        });
        
        WindowHelper.setBrowserWindowTitle("Доступные тесты");
    }
}
