package com.attestator.player.client.ui;

import java.util.List;

import com.attestator.common.client.helper.HistoryHelper.HistoryToken;
import com.attestator.common.client.helper.WindowHelper;
import com.attestator.common.client.ui.resolurces.Resources;
import com.attestator.common.shared.helper.HtmlBuilder;
import com.attestator.common.shared.helper.NullHelper;
import com.attestator.player.client.MainScreen;
import com.attestator.player.client.Player;
import com.attestator.player.client.rpc.PlayerAsyncCallback;
import com.attestator.player.shared.dto.ActivePublicationDTO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;

public class PublicationsScreen extends MainScreen {
    interface UiBinderImpl extends UiBinder<Widget, PublicationsScreen> {
    }
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    
    public static interface TextTemplates extends XTemplates{
        @XTemplate("<b>Доступных тестов нет</b>")
        public SafeHtml emptyPublicationsMessage();
    }
    
    public static final TextTemplates TEMPLATES = GWT.create(TextTemplates.class);

    public static final String HISTORY_TOKEN = "publications";
    private static PublicationsScreen instance;
    
    @UiField
    protected VerticalLayoutContainer vl;
    @UiField
    protected ContentPanel cp;
    
    private void clear() {
        vl.clear();
    }
    
    private PublicationsScreen() {
        uiBinder.createAndBindUi(this);
    }
    
    private String preparePublicationItem(ActivePublicationDTO publicationData) {
        HtmlBuilder hb = new HtmlBuilder();
        
        hb.startTag("div", Resources.STYLES.publicationScreenCss().publicationTitle());
        if (publicationData.getPublication().isThisUnlimitedAttempts() || publicationData.getAttemptsLeftOrZero() > 0) {
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
        if (publicationData.getPublication().isThisUnlimitedAttempts()) {
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
    
    private void initEmpty() {
        clear();
        vl.add(new HTML(TEMPLATES.emptyPublicationsMessage()));
    }

    private void initFromPublications(List<ActivePublicationDTO> publications) {
        clear();
        
        for (int i = 0; i < publications.size(); i++) {
            ActivePublicationDTO publicationData = publications.get(i);
            String publicationItem = preparePublicationItem(publicationData);
            HTML publicationItemHtml = new HTML(publicationItem);
            WindowHelper.setElementMargins(publicationItemHtml.getElement(), 8, 0, 0, 0, Unit.PX);
            vl.add(publicationItemHtml, new VerticalLayoutData(1, -1));
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
//        cp.setHeadingText("Cache version: " + PlayerStorageCache.VERSION);
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
