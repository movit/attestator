package com.attestator.admin.client.ui.widgets;

import java.util.List;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.helper.WidgetHelpr;
import com.attestator.admin.client.rpc.AdminAsyncCallback;
import com.attestator.admin.client.ui.GroupsWindow;
import com.attestator.admin.client.ui.event.SaveEvent;
import com.attestator.admin.client.ui.event.SaveEvent.SaveHandler;
import com.attestator.common.shared.helper.NullHelper;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.helper.VOHelper;
import com.attestator.common.shared.vo.GroupVO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.form.ComboBox;

public class GroupsComboBox extends Composite implements LeafValueEditor<String> {
    public static class GroupsComboItem {
        public static final GroupsComboItem CREATE_ITEM = new GroupsComboItem("new", "Создать");
        public static final GroupsComboItem EDIT_ITEM = new GroupsComboItem("edit", "Редактировать группы...");
        
        private String id;
        private String text;
        
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getText() {
            return text;
        }
        public void setText(String text) {
            this.text = text;
        }
        public GroupsComboItem(String id, String text) {
            this.id = id;
            this.text = text;
        }
    }
    
    public interface GroupsComboItemProperties extends PropertyAccess<GroupsComboItem> {
        ValueProvider<GroupsComboItem, String> text();
        @Path("text")
        LabelProvider<GroupsComboItem> textLabel();
        ModelKeyProvider<GroupsComboItem> id();
    }
    
    interface UiBinderImpl extends UiBinder<Widget, GroupsComboBox> {
    }
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    
    private static final GroupsComboItemProperties GROUP_PROPS = GWT.<GroupsComboItemProperties>create(GroupsComboItemProperties.class);

    @UiField(provided = true)
    @Ignore
    protected ListStore<GroupsComboItem> groupStore = new ListStore<GroupsComboBox.GroupsComboItem>(GROUP_PROPS.id()) {
        private boolean containsItemWithText(String text) {
            for (GroupsComboItem item: getAll()) {
                if (item == GroupsComboItem.EDIT_ITEM) {
                    continue;
                }
                if (item == GroupsComboItem.CREATE_ITEM) {
                    continue;
                }
                if (NullHelper.nullSafeEquals(item.getText(), text)) {
                    return true;
                }
            }
            return false;
        }
        @Override
        protected boolean isFilteredOut(GroupsComboBox.GroupsComboItem item) {
            if (item == GroupsComboItem.EDIT_ITEM) {
                return false;
            }
            else if (item == GroupsComboItem.CREATE_ITEM) {                
                return StringHelper.isEmptyOrNull(comboBox.getText()) || containsItemWithText(comboBox.getText().trim());
            }
            else {
                return super.isFilteredOut(item);
            }
        }
    };

    @Ignore
    private SafeHtmlRenderer<GroupsComboItem> groupRenderer = new AbstractSafeHtmlRenderer<GroupsComboBox.GroupsComboItem>() {
        @Override
        public SafeHtml render(GroupsComboItem object) {
            SafeHtmlBuilder shb = new SafeHtmlBuilder();
            if (object == GroupsComboItem.CREATE_ITEM) {
                shb.appendEscaped("(Создать) " + comboBox.getText());
            }
            else if (object == GroupsComboItem.EDIT_ITEM) {
                shb.appendHtmlConstant("<div style=\"border-top: 1px solid\">" + object.getText() + "</div>");
            }
            else {
                shb.appendEscaped(object.getText());
            }
            return shb.toSafeHtml();
        }
    }; 
    
    
    @UiField(provided = true)
    @Ignore
    protected LabelProvider<GroupsComboItem> groupLabelProvider = GROUP_PROPS.textLabel();
    
    @UiField(provided = true)
    @Ignore
    protected ComboBox<GroupsComboItem> comboBox;
    
    private String valueHolder;
    
    private void loadGroups() {
        Admin.RPC.getGroups(new AdminAsyncCallback<List<GroupVO>>() {
            @Override
            public void onSuccess(List<GroupVO> result) {
                groupStore.clear();
                comboBox.clear();
                
                for (GroupVO group: result) {
                    groupStore.add(new GroupsComboItem(group.getId(), group.getName()));
                }
                groupStore.add(GroupsComboItem.CREATE_ITEM);
                groupStore.add(GroupsComboItem.EDIT_ITEM);
                
                List<String> groupNames = VOHelper.getNames(result);
                comboBox.setMinListWidth(WidgetHelpr.width(groupNames));
                
                //Force filtering to hide CREATE_ITEM
                comboBox.doQuery("qqq", true);
                comboBox.doQuery("", true);
                comboBox.collapse();
                
                setValue(valueHolder);
            }
        });
    }
    
//    @UiConstructor
//    public GroupsComboBox(String emptyText) {
//        this();
//        comboBox.setEmptyText(emptyText);
//    }
    
    @UiConstructor
    public GroupsComboBox() {
        
        comboBox = new ComboBox<GroupsComboBox.GroupsComboItem>(groupStore, groupLabelProvider, groupRenderer);
        
        initWidget(uiBinder.createAndBindUi(this));
        
        loadGroups();
        
        comboBox.addBeforeSelectionHandler(new BeforeSelectionHandler<GroupsComboBox.GroupsComboItem>() {            

            @Override
            public void onBeforeSelection(
                    BeforeSelectionEvent<GroupsComboItem> event) {
                
                GroupsComboItem item = event.getItem();
                
                if (item == GroupsComboItem.CREATE_ITEM) {
                    event.cancel();
                    
                    final GroupVO group = new GroupVO(comboBox.getText());
                    valueHolder = group.getId();
                    
                    Admin.RPC.saveGroup(group, new AdminAsyncCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            loadGroups();                            
                        }
                    });
                    
                }
                else if (item == GroupsComboItem.EDIT_ITEM) {
                    event.cancel();
                    valueHolder = getValue();
                    
                    GroupsWindow window = new GroupsWindow();                    
                    window.addSaveHandler(new SaveHandler<List<GroupVO>>() {
                        @Override
                        public void onSave(SaveEvent<List<GroupVO>> event) {
                            loadGroups();                    
                        }
                    });
                    window.show();
                }
            }
        });
        
    }

    @Override
    public void setValue(String value) {
        if (groupStore.size() > 0) {
            GroupsComboItem item = groupStore.findModelWithKey(value); 
            if (item == null) {
                value = Admin.getLoggedUser().getDefaultGroupId();
                item = groupStore.findModelWithKey(value);
            }
            comboBox.setValue(item);           
        }
        valueHolder = value;
    }

    @Override
    public String getValue() {
        if (comboBox.getValue() != null) {
            return groupStore.getKeyProvider().getKey(comboBox.getValue());
        }
        return null;
    }   
}
