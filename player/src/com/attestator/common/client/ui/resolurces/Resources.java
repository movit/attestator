package com.attestator.common.client.ui.resolurces;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public class Resources {
    public static interface TestScreenCss extends CssResource {
        String navButtonCurrent();
        String timerLabel();
        String questionNoLabel();
    }
    
    public static interface PublicationScreenCss extends CssResource {
        String publicationTitle();
        String publicationData();
        String publicationReportLink();
    }

    public static interface PlayerCss extends CssResource {
        String noSelection();
    }
    
    public static interface Styles extends ClientBundle {
        @Source("TestScreen.css")
        TestScreenCss testScreenCss();
        
        @Source("PublicationScreen.css")
        PublicationScreenCss publicationScreenCss();
        
        @Source("Player.css")
        PlayerCss playerCss();        
    }
    
    public static interface Icons extends ClientBundle {        
        ImageResource up16x16();
        ImageResource down16x16();
        
        ImageResource delete16x16();
        ImageResource next16x16();
        ImageResource checkMark16x16();
        
        ImageResource checkBoxUnchecked16x16();
        ImageResource checkBoxChecked16x16();
    }
    
    public static Icons  ICONS  = GWT.create(Icons.class);
    public static Styles STYLES = GWT.create(Styles.class);
    
    static {
        STYLES.testScreenCss().ensureInjected();
        STYLES.publicationScreenCss().ensureInjected();
        STYLES.playerCss().ensureInjected();
    }        
}
