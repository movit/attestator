package com.attestator.common.shared.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HtmlBuilder {
    
    private final StringBuilder sb = new StringBuilder();

    public static class Attribute {
        
        String name;
        String value;
        
        public Attribute(String name, String value){
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }
        
        public String getValue() {
            return value;
        }
        
        @Override
        public String toString(){
            return name+"=\""+value+"\"";
        }        
    }

    public HtmlBuilder startHtml(String docType){
        if(docType == null) {
            docType = "<!DOCTYPE html>";
        } 
        appendText(docType);
        startTag("html");
        return this;
    }

    public HtmlBuilder endHtml(){
        endTag("html");
        return this;
    }
    
    public HtmlBuilder startHead(){
        startTag("head");
        return this;
    }

    public HtmlBuilder endHead(){
        endTag("head");
        return this;
    }

    public HtmlBuilder startBody(List<Attribute> attributes){
        startTag("body", attributes);
        return this;
    }

    public HtmlBuilder endBody(){
        endTag("body");
        return this;
    }
    
    public HtmlBuilder addTitle( String title ){
        startTag("title").appendText(title).endTag("title");
        return this;
    }

    public HtmlBuilder addStyle( String type, String media, String styleBlock ){
        List<Attribute> attributes = new ArrayList<Attribute>();
        if(type == null){
            type = "text/css";
        }
        attributes.add(new Attribute("type", type));
        if(media != null){
            attributes.add(new Attribute("media", media));
        }
        startTag("style", attributes);
        appendText(styleBlock);
        endTag("style");
        return this;
    }
    
    
    public HtmlBuilder addHeader( int level, String text ){
        if(level < 1) {
            level = 1;
        }
        if(level > 6) {
            level = 6;
        }
        startTag("h"+level);
        appendText(text);
        endTag("h"+level);
        return this;
    }
    
    public HtmlBuilder addAnchor( String href, String text ){
        assert href != null;
        startTag("a", Arrays.asList(new Attribute[]{new Attribute("href", href)}));
        appendText(text);
        endTag("a");
        return this;
    }
    
    public HtmlBuilder startTable(int cellspacing, int cellpadding, String clazz, String... columnsWidth){
        return startTable(cellspacing, cellpadding, 0, clazz, columnsWidth);
    }
    
    public HtmlBuilder startTable(int cellspacing, int cellpadding, int border, String clazz, String... columnsWidth){
        if(cellspacing < 0) {
            cellspacing = 0;
        }
        if(cellpadding < 0) {
            cellpadding = 0;
        }
        List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("cellspacing",cellspacing+""));
        attributes.add(new Attribute("cellpadding",cellpadding+""));
        if(border>0){
            attributes.add(new Attribute("border",border+""));
        }
        startTag("table", clazz, attributes);
        if(columnsWidth != null && columnsWidth.length>0){
            startTag("colgroup");
            for(String colWidth : columnsWidth){
                if(colWidth.length()>0) {
                    startTag("col", Arrays.asList(new Attribute[]{new Attribute("style", "width: "+colWidth+";")})).endTag("col");
                }
                else {
                    appendText("<col>");
                }
            }
            endTag("colgroup");
        }
        return this;
    }
    
    public HtmlBuilder endTable(){
        return endTag("table");
    }
    
    public HtmlBuilder startTag(String tagName){
        return startTag(tagName, null, null, null);
    }

    public HtmlBuilder startTag(String tagName, String clazz){
        return startTag(tagName, clazz, null, null);
    }

    public HtmlBuilder startTag(String tagName, List<Attribute> attributes){
        return startTag(tagName, null, null, attributes);
    }
    
    public HtmlBuilder startTag(String tagName, String clazz, List<Attribute> attributes){
        return startTag(tagName, clazz, null, attributes);
    }

    public HtmlBuilder startTag(String tagName, String clazz, String id, List<Attribute> attributes){
        sb.append("<").append(tagName);
        if(clazz != null) {
            sb.append(" class=\"").append(clazz).append("\"");
        }
        if(id != null) {
            sb.append(" id=\"").append(id).append("\"");
        }
        if(attributes != null){
            for(Attribute attribute : attributes){
                if(attribute != null) {
                    sb.append(" ").append(attribute.toString());
                }
            }
        }
        sb.append(">");
        return this;
    }
    
    public HtmlBuilder endTag(String tagName){
        sb.append("</").append(tagName).append(">");
        return this;
    }
    
    public HtmlBuilder appendText(String text){
        if(text != null) {
            sb.append(text);
        }
        return this;
    }
 
    @Override
    public String toString(){
        return sb.toString();
    }
}
