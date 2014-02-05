package com.attestator.admin.client.ui.widgets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.attestator.admin.client.props.SharingEntyVOPropertyAccess;
import com.attestator.admin.client.ui.event.MultyLinikSelectEvent;
import com.attestator.admin.client.ui.event.MultyLinikSelectEvent.MultyLinikSelectHandler;
import com.attestator.common.client.ui.resolurces.Resources;
import com.attestator.common.shared.vo.SharingEntryVO;
import com.attestator.common.shared.vo.UserVO;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Ignore;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.ValueAwareEditor;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.client.editor.ListStoreEditor;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.DateField;
import com.sencha.gxt.widget.core.client.form.DateTimePropertyEditor;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;

public class SharingEntriesList extends Composite implements IsEditor<ValueAwareEditor<List<SharingEntryVO>>> {
    public static interface Templates extends XTemplates {
        @XTemplate("<span class='{anchorClassName}'><img src='{imgUrl}'/></span>")
        public SafeHtml imageAction(String anchorClassName, SafeUri imgUrl);        
        @XTemplate("<span class='{anchorClassName}'><a href='#'>{text}</a></span>")
        public SafeHtml textLinkAction(String anchorClassName, String text);        
        @XTemplate("<span class='{anchorClassName}'>{text}</span>")
        public SafeHtml textAction(String anchorClassName, String text);        
    }
    public static final Templates TEMPLATES = GWT.create(Templates.class);
    
    private static final String DELETE_ENTRY_LINK_ID = "deleteSharingEntry";
    private static final SharingEntyVOPropertyAccess props = GWT.create(SharingEntyVOPropertyAccess.class);
    
    interface UiBinderImpl extends UiBinder<Widget, SharingEntriesList> {
    }
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    
    @UiField
    VerticalLayoutContainer top;
    
    ListStore<SharingEntryVO> listStore;
    ListStore<SharingEntryVO> createListStore() {
        ListStore<SharingEntryVO> result = new ListStore<SharingEntryVO>(props.id());
        return result;
    }
    
    ColumnConfig<SharingEntryVO, Date> startColumn;
    ColumnConfig<SharingEntryVO, Date> endColumn;
    ColumnConfig<SharingEntryVO, Date> createDateColumn(ValueProvider<SharingEntryVO, Date> valueProvider, String title) {
        ColumnConfig<SharingEntryVO, Date> result = new ColumnConfig<SharingEntryVO, Date>(valueProvider, 100, title);
        result.setCell(new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM)) {
            @Override
            public void render(com.google.gwt.cell.client.Cell.Context context,
                    Date value, SafeHtmlBuilder sb) {
                if (value != null) {
                    super.render(context, value, sb);
                }
                else {
                    sb.appendHtmlConstant("не указано");
                }
                sb.append(
                        TEMPLATES.textLinkAction(MultylinkCell.RESOURCES.multyLinkCellCss().multyLink(), 
                                "изменить"));
            }
        });
        return result;
    }
    
    
    ColumnConfig<SharingEntryVO, SharingEntryVO> actionsColumn;
    private ColumnConfig<SharingEntryVO, SharingEntryVO> createSharingEntryActionsColumnConfig(IdentityValueProvider<SharingEntryVO> valueProvider, final ListStore<SharingEntryVO> listStore) {
        ColumnConfig<SharingEntryVO, SharingEntryVO> result = new ColumnConfig<SharingEntryVO, SharingEntryVO>(valueProvider);
        
        MultylinkCell<SharingEntryVO> cell = new MultylinkCell<SharingEntryVO>() {
            @Override
            public void render(Context context,
                    SharingEntryVO value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<div style='text-align: right;'>");
                sb.append(createClickableElement(DELETE_ENTRY_LINK_ID, "удалить пользователя из списка", Resources.ICONS.delete16x16()));
                sb.appendHtmlConstant("</div>");
            }
        };
        cell.addMultyLinikSelectHandler(new MultyLinikSelectHandler<SharingEntryVO>() {
            @Override
            public void onSelect(MultyLinikSelectEvent<SharingEntryVO> event) {
                if (DELETE_ENTRY_LINK_ID.equals(event.getLinkType())) {
                    SharingEntryVO entry = (SharingEntryVO)event.getValue();
                    listStore.remove(entry);
                }
            }
        });
        result.setCell(cell);
        result.setHideable(false);
        result.setResizable(false);
        result.setWidth(40);
        result.setFixed(true);
        result.setMenuDisabled(true);
        result.setSortable(false);
        return result;
    }

    
    @Ignore
    ColumnModel<SharingEntryVO> cm;
    ColumnModel<SharingEntryVO> createCm(ColumnConfig<SharingEntryVO, Date> startColumn, ColumnConfig<SharingEntryVO, Date> endColumn, ColumnConfig<SharingEntryVO, SharingEntryVO> actionsColumn) {
        List<ColumnConfig<SharingEntryVO, ?>> l = new ArrayList<ColumnConfig<SharingEntryVO,?>>();
        l.add(new ColumnConfig<SharingEntryVO, String>(props.username(), 100, "Пользователь"));
        l.add(startColumn);
        l.add(endColumn);
        l.add(actionsColumn);
        ColumnModel<SharingEntryVO> result = new ColumnModel<SharingEntryVO>(l);
        return result;
    }
    
    @Ignore
    @UiField(provided=true)
    Grid<SharingEntryVO> grid;
    Grid<SharingEntryVO> createGrid(ListStore<SharingEntryVO> listStore, ColumnModel<SharingEntryVO> cm) {
        Grid<SharingEntryVO> result = new Grid<SharingEntryVO>(listStore, cm);
        result.getView().setAutoFill(true);
        return result;
    }

    
    @Ignore
    ListStoreEditor<SharingEntryVO> listStoreEditor;
    ListStoreEditor<SharingEntryVO> createListStoreEditor(Grid<SharingEntryVO> grid, ListStore<SharingEntryVO> listStore) {
        ListStoreEditor<SharingEntryVO> result = new ListStoreEditor<SharingEntryVO>(listStore);
        return result;
    }
    
    @Ignore
    GridInlineEditing<SharingEntryVO> gridInlineEditing;
    GridInlineEditing<SharingEntryVO> createGridInlineEditing(Grid<SharingEntryVO> grid, ColumnConfig<SharingEntryVO, Date> startColumn, ColumnConfig<SharingEntryVO, Date> endColumn) {
        GridInlineEditing<SharingEntryVO> result = new GridInlineEditing<SharingEntryVO>(grid);
        result.addEditor(startColumn, new DateField(new DateTimePropertyEditor(DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM))));
        result.addEditor(endColumn, new DateField(new DateTimePropertyEditor(DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM))));        
        return result;
    }
    
    @UiField
    UserComboBox userComboBox;    
    
    @UiConstructor
    public SharingEntriesList() {
        listStore = createListStore();
        startColumn = createDateColumn(props.start(), "Начиная с");
        endColumn = createDateColumn(props.end(), "Доступен по");
        actionsColumn = createSharingEntryActionsColumnConfig(new IdentityValueProvider<SharingEntryVO>(), listStore);
        cm = createCm(startColumn, endColumn, actionsColumn);
        grid = createGrid(listStore, cm);
        listStoreEditor = createListStoreEditor(grid, listStore);
        gridInlineEditing = createGridInlineEditing(grid, startColumn, endColumn);
        
        initWidget(uiBinder.createAndBindUi(this));        
    }
    
    @UiHandler("addButton")
    void addButtonClick(SelectEvent event) {
        UserVO user = userComboBox.asComboBox().getCurrentValue();
        if (user == null) {
            return;
        }
        SharingEntryVO newEntry = new SharingEntryVO();
        newEntry.setTenantId(user.getTenantId());
        newEntry.setUsername(user.getUsername());
        listStore.add(newEntry);
    }    
    
    @Override
    public ValueAwareEditor<List<SharingEntryVO>> asEditor() {
        return listStoreEditor;
    }
}
