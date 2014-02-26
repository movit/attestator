package com.attestator.admin.client.ui.widgets;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Image;

public class AnchorImage extends Anchor {
    
    @UiConstructor
    public AnchorImage(ImageResource image) {
        super();
        getElement().appendChild(new Image(image).getElement());
    }    
}
