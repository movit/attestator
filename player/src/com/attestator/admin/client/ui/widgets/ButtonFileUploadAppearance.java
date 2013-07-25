package com.attestator.admin.client.ui.widgets;

import com.attestator.admin.client.ui.widgets.ButtonFileUpload.FileUploadFieldAppearance;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.core.client.resources.StyleInjectorHelper;

public class ButtonFileUploadAppearance implements FileUploadFieldAppearance {
    public interface ButtonFileUploadResources extends ClientBundle {
        @Source("ButtonFileUpload.css")
        ButtonFileUploadStyle css();
      }

      public interface ButtonFileUploadStyle extends CssResource {
        String buttonWrap();

        String file();

        String input();

        String wrap();

      }

      public interface ButtonFileUploadTemplate extends XTemplates {
        @XTemplate("<div class='{style.wrap}'></div>")
        SafeHtml render(ButtonFileUploadStyle style);
      }

      private final ButtonFileUploadResources resources;
      private final ButtonFileUploadStyle style;
      private final ButtonFileUploadTemplate template;

      public ButtonFileUploadAppearance() {
        this(GWT.<ButtonFileUploadResources> create(ButtonFileUploadResources.class));
      }

      public ButtonFileUploadAppearance(ButtonFileUploadResources resources) {
        this.resources = resources;
        this.style = this.resources.css();

        StyleInjectorHelper.ensureInjected(this.style, true);

        this.template = GWT.create(ButtonFileUploadTemplate.class);
      }

      @Override
      public String fileInputClass() {
        return style.file();
      }

      @Override
      public void render(SafeHtmlBuilder sb) {
        sb.append(template.render(style));
      }

      @Override
      public String wrapClass() {
        return style.wrap();
      }

      @Override
      public String textFieldClass() {
        return style.input();
      }

      @Override
      public String buttonClass() {
        return style.buttonWrap();
      }
}
