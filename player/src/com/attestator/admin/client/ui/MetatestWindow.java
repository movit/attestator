package com.attestator.admin.client.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.helper.WidgetHelpr;
import com.attestator.admin.client.props.BoundedConverter;
import com.attestator.admin.client.props.GroupVOPropertyAccess;
import com.attestator.admin.client.props.MetatestEntryVOPropertyAccess;
import com.attestator.admin.client.props.PublicationVOPropertyAccess;
import com.attestator.admin.client.props.PublicationsTreePropertyAccess;
import com.attestator.admin.client.props.QuestionVOPropertyAccess;
import com.attestator.admin.client.rpc.AdminAsyncCallback;
import com.attestator.admin.client.ui.PublicationWindow.SaveMode;
import com.attestator.admin.client.ui.event.FilterEvent;
import com.attestator.admin.client.ui.event.FilterEvent.FilterHandler;
import com.attestator.admin.client.ui.event.MultyLinikSelectEvent;
import com.attestator.admin.client.ui.event.MultyLinikSelectEvent.MultyLinikSelectHandler;
import com.attestator.admin.client.ui.event.SaveEvent;
import com.attestator.admin.client.ui.event.SaveEvent.HasSaveEventHandlers;
import com.attestator.admin.client.ui.event.SaveEvent.SaveHandler;
import com.attestator.admin.client.ui.widgets.MultylinkCell;
import com.attestator.admin.client.ui.widgets.SearchField;
import com.attestator.common.client.ui.resolurces.Resources;
import com.attestator.common.shared.helper.NullHelper;
import com.attestator.common.shared.helper.ReportHelper;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.helper.VOHelper;
import com.attestator.common.shared.vo.GroupVO;
import com.attestator.common.shared.vo.MTEGroupVO;
import com.attestator.common.shared.vo.MTEQuestionVO;
import com.attestator.common.shared.vo.MetaTestEntryVO;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.QuestionVO;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.editor.client.adapters.SimpleEditor;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.ResizeCell;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.client.editor.ListStoreEditor;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterConfigBean;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfigBean;
import com.sencha.gxt.data.shared.loader.ListLoadConfig;
import com.sencha.gxt.data.shared.loader.ListLoadResult;
import com.sencha.gxt.data.shared.loader.ListLoader;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.LoadHandler;
import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.ContentPanel.ContentPanelAppearance;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.AccordionLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.BeforeStartEditEvent;
import com.sencha.gxt.widget.core.client.event.BeforeStartEditEvent.BeforeStartEditHandler;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HasHideHandlers;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent.RowDoubleClickHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.IntegerPropertyEditor;
import com.sencha.gxt.widget.core.client.form.SpinnerField;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.AggregationNumberSummaryRenderer;
import com.sencha.gxt.widget.core.client.grid.AggregationRowConfig;
import com.sencha.gxt.widget.core.client.grid.AggregationSafeHtmlRenderer;
import com.sencha.gxt.widget.core.client.grid.CheckBoxSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridSelectionModel;
import com.sencha.gxt.widget.core.client.grid.SummaryType.SumSummaryType;
import com.sencha.gxt.widget.core.client.grid.editing.GridEditing;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;

public class MetatestWindow implements IsWidget, Editor<MetaTestVO>, HasSaveEventHandlers<MetaTestVO>,  HasHideHandlers{
    private static final String EDIT_PUBLICATION_LINK_ID = "editPublication";
    private static final String COPY_PUBLICATION_LINK_ID = "copyPublication";
    private static final String DELETE_PUBLICATION_LINK_ID = "deletePublication";
    
    interface DriverImpl extends SimpleBeanEditorDriver<MetaTestVO, MetatestWindow> {
    }
    interface UiBinderImpl extends UiBinder<Window, MetatestWindow> {
    }
    
    public static interface Templates extends XTemplates {
        @XTemplate("<span class='{anchorClassName}'><img src='{imgUrl}'/></span>")
        public SafeHtml imageAction(String anchorClassName, SafeUri imgUrl);        
        @XTemplate("<span class='{anchorClassName}'><a href='#'>{text}</a></span>")
        public SafeHtml textLinkAction(String anchorClassName, String text);        
        @XTemplate("<span class='{anchorClassName}'>{text}</span>")
        public SafeHtml textAction(String anchorClassName, String text);        
    }
    public static final Templates TEMPLATES = GWT.create(Templates.class);
    
    private static MetatestEntryVOPropertyAccess entryPropertyAccess = GWT
            .create(MetatestEntryVOPropertyAccess.class);
    
    private static QuestionVOPropertyAccess questionPropertyAccess = GWT
            .create(QuestionVOPropertyAccess.class);

    private static GroupVOPropertyAccess groupPropertyAccess = GWT
            .create(GroupVOPropertyAccess.class);
    
    private static PublicationsTreePropertyAccess publicationsTreeProperties = 
            new PublicationsTreePropertyAccess();
    
    private static PublicationVOPropertyAccess publicationProperties = GWT
            .create(PublicationVOPropertyAccess.class);
    
    private DriverImpl driver = GWT.create(DriverImpl.class);    
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);

    SimpleEditor<String> id = SimpleEditor.of();
    
    @UiField
    TextField name;
    
    @Ignore
    @UiField
    Window window;
    
    @UiField
    @Ignore
    VerticalLayoutContainer top;
    
    @UiField
    @Ignore
    AccordionLayoutContainer elementsSourceLayout;
    
    @UiField
    @Ignore
    ContentPanel questionsPanel;
    
    @UiField
    @Ignore
    ContentPanel groupsPanel;
    
    @UiField
    @Ignore
    TextButton addPublicationButton;
    
    @UiField
    @Ignore
    TextButton removePublicationButton;

    @UiField
    @Ignore
    TextButton copyPublicationButton;
    
    @UiField
    @Ignore
    TextButton editPublicationButton;

    @UiField
    @Ignore
    TextButton addQuestionButton;

    @UiField
    @Ignore
    TextButton addGroupButton;
    
    @UiField
    @Ignore
    TextButton removeEntryButton;
    
    @UiField
    @Ignore
    TextButton moveEntryUpButton;
    
    @UiField
    @Ignore
    TextButton moveEntryDownButton;
        
    @Ignore
    @UiField(provided = true)
    SearchField questionsSearchField;
    
    @Ignore
    @UiField(provided = true)
    SearchField groupsSearchField;

    @Ignore
    @UiField(provided = true)
    Grid<QuestionVO> questionsGrid;

    @Ignore
    @UiField(provided = true)
    Grid<GroupVO> groupsGrid;

    @Ignore
    @UiField(provided = true)
    Grid<MetaTestEntryVO> entriesGrid;

    @Ignore
    @UiField(provided = true)
    Grid<PublicationVO> publicationsGrid;
    
    @Ignore
    GridEditing<MetaTestEntryVO> entriesEditing;
    @Ignore
    @UiField
    PagingToolBar questionsPager;

    @Ignore
    @UiField
    PagingToolBar groupsPager;
    
    @Ignore
    ListStore<MetaTestEntryVO> entriesStore;
    ListStoreEditor<MetaTestEntryVO> entries;
    @Ignore
    ListStore<QuestionVO> questionsStore;
    @Ignore
    ListStore<GroupVO> groupsStore;
    @Ignore
    ListStore<PublicationVO> publicationsStore;
    
    @Ignore
    RpcProxy<FilterPagingLoadConfig, PagingLoadResult<GroupVO>> groupsRpcProxy; 
    @Ignore
    RpcProxy<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>> questionsRpcProxy; 
    @Ignore
    RpcProxy<ListLoadConfig, ListLoadResult<PublicationVO>> publicationsRpcProxy; 
    @Ignore
    PagingLoader<FilterPagingLoadConfig, PagingLoadResult<GroupVO>> groupsLoader;
    @Ignore
    PagingLoader<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>> questionsLoader;
    @Ignore
    ListLoader<ListLoadConfig, ListLoadResult<PublicationVO>> publicationsLoader;
    
    @Ignore
    CheckBoxSelectionModel<GroupVO> groupsSm;
    @Ignore
    CheckBoxSelectionModel<QuestionVO> questionsSm;
    @Ignore
    CheckBoxSelectionModel<MetaTestEntryVO> entriesSm;

    @Ignore
    CheckBoxSelectionModel<PublicationVO> publicationsSm;
    
    @Ignore
    MetaTestVO originalMetatestInstance;
    
    @SuppressWarnings("rawtypes")
    @Ignore 
    SelectionChangedHandler dualListSelectionHandler = new SelectionChangedHandler() {
        private boolean ignore = false;
        @Override
        public void onSelectionChanged(SelectionChangedEvent event) {
            if (ignore) {
                return;
            }
            try {
                ignore = true;                
                if (event.getSource() != questionsSm) {
                    questionsSm.deselectAll();
                }                
                if (event.getSource() != groupsSm) {
                    groupsSm.deselectAll();
                }                
                if (event.getSource() != entriesSm) {
                    entriesSm.deselectAll();
                }                
                enableDualListButtons();                
            }
            finally {
                ignore = false;
            }
        }
    }; 
    
    @SuppressWarnings("rawtypes")
    @Ignore 
    SelectionChangedHandler publicationSelectionHandler = new SelectionChangedHandler() {
        @Override
        public void onSelectionChanged(SelectionChangedEvent event) {
            enablePublicationsButtons();                
        }
    }; 
    
    @Ignore
    ColumnModel<PublicationVO> publicationsCm;
    @Ignore
    ColumnModel<MetaTestEntryVO> entriesCm;
    
    @Ignore
    ColumnConfig<MetaTestEntryVO, Integer> entriesQuestionsCountColumn;    
    
    @Ignore
    ColumnModel<GroupVO> groupsCm;
    @Ignore
    ColumnModel<QuestionVO> questionsCm;
    @Ignore
    SearchField createSearchField() {
        SearchField result = new SearchField();
        return result;
    }

    <T> ListStore<T> createStore(ModelKeyProvider<T> keyProvider) {
        ListStore<T> result = new ListStore<T>(keyProvider);
        return result;
    }

    <T> ListLoader<ListLoadConfig, ListLoadResult<T>> createListLoader(
            final ListStore<T> store, RpcProxy<ListLoadConfig, ListLoadResult<T>> rpcProxy, boolean remoteSort) {        
        ListLoader<ListLoadConfig, ListLoadResult<T>> result = new ListLoader<ListLoadConfig, ListLoadResult<T>>(
                rpcProxy);        
        result.setRemoteSort(remoteSort);
        result.addLoadHandler(new LoadResultListStoreBinding<ListLoadConfig, T, ListLoadResult<T>>(store));
        return result;
    }

    Grid<PublicationVO> createPublicationsGrid(final ListStore<PublicationVO> store, ColumnModel<PublicationVO> cm, GridSelectionModel<PublicationVO> sm) {
        Grid<PublicationVO> result = createGrid(store, cm, sm);
        result.addRowDoubleClickHandler(new RowDoubleClickHandler() {
            @Override
            public void onRowDoubleClick(RowDoubleClickEvent event) {
                PublicationVO item = store.getAll().get(event.getRowIndex());
                if (item instanceof PublicationVO) {
                    showPublicationWindow((PublicationVO)item, EditMode.etExisting);
                }        
            }
        });
        return result;
    }
    
    <T> Grid<T> createGrid(final ListStore<T> store, ColumnModel<T> cm, GridSelectionModel<T> sm) {        
        Grid<T> result = new Grid<T>(store, cm);
        result.setSelectionModel(sm);
        result.getView().setAutoFill(true);
        result.getView().setForceFit(true);
        result.setLoadMask(true);
        return result;
    }

    <T> Grid<T> createGrid(
            final ListLoader<? extends ListLoadConfig, ? extends ListLoadResult<T>> loader,
            final ListStore<T> store, ColumnModel<T> cm, GridSelectionModel<T> sm) {        
        Grid<T> result = new Grid<T>(store, cm);
        result.setSelectionModel(sm);
        result.setLoader(loader);
        result.getView().setAutoFill(true);
        result.getView().setForceFit(true);
        result.setLoadMask(true);
        return result;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    <T> CheckBoxSelectionModel<T> createSm(SelectionChangedHandler selectionCahngeHandler) {
        IdentityValueProvider<T> identity = new IdentityValueProvider<T>();
        CheckBoxSelectionModel<T> result = new CheckBoxSelectionModel<T>(identity);
        if (selectionCahngeHandler != null) {
            result.addSelectionChangedHandler(selectionCahngeHandler);
        }
        return result;
    }

    GridEditing<MetaTestEntryVO> createEntriesEditing(final Grid<MetaTestEntryVO> grid, ColumnConfig<MetaTestEntryVO, Integer> questionsCountColumn) {
        final GridInlineEditing<MetaTestEntryVO> result = new GridInlineEditing<MetaTestEntryVO>(grid);
    
        final SpinnerField<Integer> questionsCountEditor = new SpinnerField<Integer>(new IntegerPropertyEditor());
        questionsCountEditor.addValueChangeHandler(new ValueChangeHandler<Integer>() {            
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        grid.getStore().commitChanges();
                    }
                });
            }
        });
        
        final BoundedConverter<Integer> questionsCountConverter = 
                new BoundedConverter<Integer>();            
        
        result.addBeforeStartEditHandler(new BeforeStartEditHandler<MetaTestEntryVO>() {
            @Override
            public void onBeforeStartEdit(
                    BeforeStartEditEvent<MetaTestEntryVO> event) {
                int row = event.getEditCell().getRow();
                MetaTestEntryVO entry = grid.getStore().get(row);
                if (entry instanceof MTEQuestionVO) {
                    event.setCancelled(true);
                }
                else {
                    questionsCountConverter.setPrevValue(entry.getNumberOfQuestions());
                    questionsCountConverter.setMinValue(1);
                    questionsCountConverter.setMaxValue(((MTEGroupVO)entry).getGroup().getQuestionsCount().intValue());
                    
                    questionsCountEditor.clearInvalid();
                }
            }
        });
        
        
        result.addEditor(questionsCountColumn, questionsCountConverter, questionsCountEditor);
        
        return result; 
    }

    ListStoreEditor<MetaTestEntryVO> createEntriesStoreEditor(ListStore<MetaTestEntryVO> store) {
        return new ListStoreEditor<MetaTestEntryVO>(store);
    }

    ColumnModel<MetaTestEntryVO> createEntriesCm(
                CheckBoxSelectionModel<MetaTestEntryVO> sm, ColumnConfig<MetaTestEntryVO, Integer> questionsCountColumn) {
    
            List<ColumnConfig<MetaTestEntryVO, ?>> l = new ArrayList<ColumnConfig<MetaTestEntryVO, ?>>();
            l.add(sm.getColumn());
            
            ColumnConfig<MetaTestEntryVO, MetaTestEntryVO> entryColumn = createEntryDescriptionColumnConfig(new IdentityValueProvider<MetaTestEntryVO>(),
                    300, "Элемент теста"); 
            
            l.add(entryColumn);
            
    //        ColumnConfig<MetaTestEntryVO, Integer> questionsCountColumn = createQuestonsCountColumnConfig(entryPropertyAccess.numberOfQuestions(), 60, "Вопросов");
            l.add(questionsCountColumn);
            
            ColumnModel<MetaTestEntryVO> result = new ColumnModel<MetaTestEntryVO>(l);
            
            AggregationRowConfig<MetaTestEntryVO> totalQuestionsCount = new AggregationRowConfig<MetaTestEntryVO>();
            totalQuestionsCount.setRenderer(entryColumn, new AggregationSafeHtmlRenderer<MetaTestEntryVO>("Всего вопросов в тесте"));        
            totalQuestionsCount.setRenderer(questionsCountColumn, 
                    new AggregationNumberSummaryRenderer<MetaTestEntryVO, Integer>(
                            NumberFormat.getDecimalFormat(),
                    new SumSummaryType<Integer>()));
            result.addAggregationRow(totalQuestionsCount);
            
            return result;
        }

    private ColumnConfig<MetaTestEntryVO, MetaTestEntryVO> createEntryDescriptionColumnConfig(IdentityValueProvider<MetaTestEntryVO> valueProvider, int width, String header) {
        ColumnConfig<MetaTestEntryVO, MetaTestEntryVO> result = new ColumnConfig<MetaTestEntryVO, MetaTestEntryVO>(valueProvider, width, header);
        result.setCell(new ResizeCell<MetaTestEntryVO>() {
            @Override
            public void render(Context context,
                    MetaTestEntryVO value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<div style=\"white-space:normal !important;\">");                
                if (value instanceof MTEQuestionVO) {
                    QuestionVO question = ((MTEQuestionVO) value).getQuestion();
                    if (question != null) {
                        sb.appendHtmlConstant(
                                "<b>Вопрос: </b> из группы &laquo;" + question.getGroupName() + "&raquo;<br/>" + question.getText());
                    }
                }
                else if (value instanceof MTEGroupVO) {
                    GroupVO group = ((MTEGroupVO) value).getGroup();
                    if (group != null) {
                        sb.appendHtmlConstant("<b>Группа: </b> &laquo;"  + group.getName() + "&raquo;, всего " + 
                                ReportHelper.formatQuestionsCount(group.getQuestionsCount().intValue()));
                    }
                }
                sb.appendHtmlConstant("</div>");
            }
        });
        result.setMenuDisabled(true);
        result.setSortable(false);
        
        return result;
    
    }

    private ColumnConfig<MetaTestEntryVO, Integer> createQuestonsCountColumnConfig(ValueProvider<MetaTestEntryVO, Integer> valueProvider, int width, String header) {
        ColumnConfig<MetaTestEntryVO, Integer> result = new ColumnConfig<MetaTestEntryVO, Integer>(
                valueProvider, width, header);
        
        ResizeCell<Integer> questionsCountCell = new ResizeCell<Integer>() {
            @Override
            public void render(Context context,
                    Integer value, SafeHtmlBuilder sb) {
                sb.append(value);
                MetaTestEntryVO entry = entriesStore.findModelWithKey((String)context.getKey());
                if (entry instanceof MTEGroupVO) {
                    sb.append(
                            TEMPLATES.textLinkAction(MultylinkCell.RESOURCES.multyLinkCellCss().multyLink(), 
                                    "изменить"));
                }
            }
        };
        result.setMenuDisabled(true);
        result.setSortable(false);
        result.setCell(questionsCountCell);
        
        return result;        
    }

    RpcProxy<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>> createQuestionsRpcProxy() {
        return new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>>() {
             @Override
             public void load(FilterPagingLoadConfig loadConfig,
                     AsyncCallback<PagingLoadResult<QuestionVO>> callback) {
                 Admin.RPC.loadQuestions(loadConfig, callback);
             }
        };
    }

    ColumnModel<QuestionVO> createQuestionsCm(
            CheckBoxSelectionModel<QuestionVO> sm) {
        ColumnConfig<QuestionVO, String> textColumn = new ColumnConfig<QuestionVO, String>(
                questionPropertyAccess.text(), 100, "Вопрос");
        textColumn.setCell(new AbstractCell<String>() {
            @Override
            public void render(Context context, String value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<div style=\"white-space:normal !important;\">"
                        + value + "</div>");
            }
        });
    
        ColumnConfig<QuestionVO, String> groupNameColumn = new ColumnConfig<QuestionVO, String>(
                questionPropertyAccess.groupName(), 15, "Группа");
        groupNameColumn.setCell(new AbstractCell<String>() {
            @Override
            public void render(Context context, String value,
                    SafeHtmlBuilder sb) {
                if (value == null) {
                    sb.appendEscaped("");
                } else {
                    sb.appendEscaped(value);
                }
            }
        });
    
        List<ColumnConfig<QuestionVO, ?>> l = new ArrayList<ColumnConfig<QuestionVO, ?>>();
        l.add(sm.getColumn());
        l.add(textColumn);
        l.add(groupNameColumn);
        ColumnModel<QuestionVO> result = new ColumnModel<QuestionVO>(l);
    
        return result;
    }

    PagingLoader<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>> createQuestionsPagingLoader(
            final ListStore<QuestionVO> store, RpcProxy<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>> rpcProxy, final SearchField searchField) {
    
        PagingLoader<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>> result = new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>>(
                rpcProxy) {
            @Override
            protected FilterPagingLoadConfig newLoadConfig() {
                FilterPagingLoadConfig result = new FilterPagingLoadConfigBean();
    
                if (StringHelper.isNotEmptyOrNull(searchField.getNewFilterValue())) {
                    String filterValue = searchField.getNewFilterValue();
                    filterValue = filterValue.trim();
                    filterValue = filterValue.toLowerCase();
                    filterValue = filterValue.replaceAll("\\s+", " ");
    
                    FilterConfig filter = new FilterConfigBean();
                    filter.setComparison("contains");
                    filter.setField("text");
                    filter.setType("string");
                    filter.setValue(filterValue);
    
                    result.getFilters().add(filter);
                }
                
                Set<String> entriesQuestionIds = VOHelper.getQuestionsIds(entriesStore.getAll());
                if (!NullHelper.isEmptyOrNull(entriesQuestionIds)) {
                    FilterConfig filter = new FilterConfigBean();
                    filter.setComparison("notIn");
                    filter.setField("_id");
                    filter.setType("list");
                    filter.setValue(StringHelper.combine(entriesQuestionIds.toArray(new String[0]), ", "));
                    
                    result.getFilters().add(filter);
                }
    
                return result;
            }
        };
        result.setRemoteSort(true);
        result.addLoadHandler(new LoadResultListStoreBinding<FilterPagingLoadConfig, QuestionVO, PagingLoadResult<QuestionVO>>(store));
        result.addLoadHandler(new LoadHandler<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>>(){
            @Override
            public void onLoad(
                    LoadEvent<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>> event) {
                questionsPager.setVisible(event.getLoadResult().getTotalLength() > questionsPager.getPageSize());              
            }
        });
    
        return result;
    }

    PagingLoader<FilterPagingLoadConfig, PagingLoadResult<GroupVO>> createGroupsPagingLoader(
            final ListStore<GroupVO> store, RpcProxy<FilterPagingLoadConfig, PagingLoadResult<GroupVO>> rpcProxy, final SearchField searchField) {
    
        PagingLoader<FilterPagingLoadConfig, PagingLoadResult<GroupVO>> result = new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<GroupVO>>(
                rpcProxy) {
            @Override
            protected FilterPagingLoadConfig newLoadConfig() {
                FilterPagingLoadConfig result = new FilterPagingLoadConfigBean();
    
                if (StringHelper.isNotEmptyOrNull(searchField.getNewFilterValue())) {
                    String filterValue = searchField.getNewFilterValue();
                    filterValue = filterValue.trim();
                    filterValue = filterValue.toLowerCase();
                    filterValue = filterValue.replaceAll("\\s+", " ");
    
                    FilterConfig filter = new FilterConfigBean();
                    filter.setComparison("contains");
                    filter.setField("name");
                    filter.setType("string");
                    filter.setValue(filterValue);
    
                    result.getFilters().add(filter);
                }
                
                Set<String> entriesGroupIds = VOHelper.getGroupsIds(entriesStore.getAll());
                if (!NullHelper.isEmptyOrNull(entriesGroupIds)) {
                    FilterConfig filter = new FilterConfigBean();
                    filter.setComparison("notIn");
                    filter.setField("_id");
                    filter.setType("list");
                    filter.setValue(StringHelper.combine(entriesGroupIds.toArray(new String[0]), ", "));
                    
                    result.getFilters().add(filter);
                }
    
                return result;
            }
        };
        result.setRemoteSort(true);
        result.addLoadHandler(new LoadResultListStoreBinding<FilterPagingLoadConfig, GroupVO, PagingLoadResult<GroupVO>>(store));
        result.addLoadHandler(new LoadHandler<FilterPagingLoadConfig, PagingLoadResult<GroupVO>>(){
            @Override
            public void onLoad(
                    LoadEvent<FilterPagingLoadConfig, PagingLoadResult<GroupVO>> event) {
                groupsPager.setVisible(event.getLoadResult().getTotalLength() > groupsPager.getPageSize());              
            }
        });
        
        return result;
    }

    ColumnModel<GroupVO> createGroupsCm(
            CheckBoxSelectionModel<GroupVO> sm) {
    
        List<ColumnConfig<GroupVO, ?>> l = new ArrayList<ColumnConfig<GroupVO, ?>>();
        l.add(sm.getColumn());
        
        ColumnConfig<GroupVO, String> nameColumn = new ColumnConfig<GroupVO, String>(
                groupPropertyAccess.name(), 100, "Название"); 
        l.add(nameColumn);
        
        ColumnConfig<GroupVO, Long> questionsCountColumn = new ColumnConfig<GroupVO, Long>(
                groupPropertyAccess.questionsCount(), 20, "Вопросов"); 
        questionsCountColumn.setSortable(false);        
        l.add(questionsCountColumn);
        
        ColumnModel<GroupVO> result = new ColumnModel<GroupVO>(l);
    
        return result;
    }

    RpcProxy<FilterPagingLoadConfig, PagingLoadResult<GroupVO>> createGroupsRpcProxy() {
        return new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<GroupVO>>() {
             @Override
             public void load(FilterPagingLoadConfig loadConfig,
                     AsyncCallback<PagingLoadResult<GroupVO>> callback) {
                 Admin.RPC.loadGroupsPage(loadConfig, callback);
             }
        };
    }

    RpcProxy<ListLoadConfig, ListLoadResult<PublicationVO>> createPublicationsRpcProxy() {
        return new RpcProxy<ListLoadConfig, ListLoadResult<PublicationVO>>() {
             @Override
             public void load(ListLoadConfig loadConfig,
                     AsyncCallback<ListLoadResult<PublicationVO>> callback) {                 
                 Admin.RPC.loadPublicationsByMetatestId(id.getValue(), loadConfig, callback);
             }
        };
    }

    ColumnModel<PublicationVO> createPublicationsCm(CheckBoxSelectionModel<PublicationVO> sm){
        List<ColumnConfig<PublicationVO, ?>> l = new ArrayList<ColumnConfig<PublicationVO, ?>>();
        
        l.add(sm.getColumn());
        
        l.add(new ColumnConfig<PublicationVO, Long>(publicationsTreeProperties.reportsCount, 14, "Отчетов"));
        
        l.add(new ColumnConfig<PublicationVO, String>(publicationsTreeProperties.start, 20, "Начало"));
        l.add(new ColumnConfig<PublicationVO, String>(publicationsTreeProperties.end, 20, "Конец"));
        
        l.add(new ColumnConfig<PublicationVO, String>(publicationsTreeProperties.fillBeforeTest, 40, "Заполнять перед тестом"));
        l.add(new ColumnConfig<PublicationVO, String>(publicationsTreeProperties.maxAttempts , 20, "Макс. попыток"));
        l.add(new ColumnConfig<PublicationVO, String>(publicationsTreeProperties.minScore, 15, "Мин. баллов"));
        
        l.add(new ColumnConfig<PublicationVO, String>(publicationsTreeProperties.maxTakeTestTime, 20, "Времени на тест"));
        l.add(createPublicationActionsColumnConfig(new IdentityValueProvider<PublicationVO>()));
        
        WidgetHelpr.disableColumnHeaderOperations(l);

        ColumnModel<PublicationVO> result = new ColumnModel<PublicationVO>(l);
    
        return result;        
    }

    private ColumnConfig<PublicationVO, PublicationVO> createPublicationActionsColumnConfig(IdentityValueProvider<PublicationVO> valueProvider) {
        ColumnConfig<PublicationVO, PublicationVO> result = new ColumnConfig<PublicationVO, PublicationVO>(valueProvider);
        
        MultylinkCell<PublicationVO> cell = new MultylinkCell<PublicationVO>() {
            @Override
            public void render(Context context,
                    PublicationVO value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<div style='text-align: right;'>");
                sb.append(createClickableElement(EDIT_PUBLICATION_LINK_ID, "изменить публикацию", Resources.ICONS.edit16x16()));
                sb.append(createClickableElement(COPY_PUBLICATION_LINK_ID, "копировать публикацию", Resources.ICONS.copy16x16()));
                sb.append(createClickableElement(DELETE_PUBLICATION_LINK_ID, "удалить публикацию", Resources.ICONS.delete16x16()));
                sb.appendHtmlConstant("</div>");
            }
        };
        cell.addMultyLinikSelectHandler(new MultyLinikSelectHandler<PublicationVO>() {
            @Override
            public void onSelect(MultyLinikSelectEvent<PublicationVO> event) {
                if (EDIT_PUBLICATION_LINK_ID.equals(event.getLinkType())) {
                    PublicationVO publication = (PublicationVO)event.getValue();
                    showPublicationWindow(publication, EditMode.etExisting);
                }
                else if (COPY_PUBLICATION_LINK_ID.equals(event.getLinkType())) {
                    PublicationVO publication = (PublicationVO)event.getValue();
                    showPublicationWindow(publication, EditMode.etCopy);
                }
                else if (DELETE_PUBLICATION_LINK_ID.equals(event.getLinkType())) {
                    final PublicationVO publication = (PublicationVO)event.getValue();
                    publicationsStore.remove(publication);
                }
            }
        });
        result.setCell(cell);
        result.setSortable(false);
        result.setHideable(false);
        result.setResizable(false);
        result.setWidth(90);
        result.setFixed(true);
        result.setMenuDisabled(true);        
        return result;
    }
    
    @UiFactory
    ContentPanel createContentPanel(ContentPanelAppearance appearance) {
      return new ContentPanel(appearance);
    }

    @UiHandler("addQuestionButton")
    void addQuestionButtonClick(SelectEvent event) {
        List<MetaTestEntryVO> entriesToAdd = new ArrayList<MetaTestEntryVO>();
        
        for (QuestionVO question: questionsSm.getSelectedItems()) {
            MTEQuestionVO entry = new MTEQuestionVO();
            entry.setQuestionId(question.getId());
            entry.setQuestion(question);
            entriesToAdd.add(entry);
        }
        
        int rowToMakeVisible = entriesStore.size();
        entriesStore.addAll(entriesToAdd);
        entriesSm.select(entriesToAdd, false);
        entriesGrid.getView().ensureVisible(rowToMakeVisible, 0, true);
        
        questionsPager.refresh();
    }
    
    @UiHandler("addGroupButton")
    void addGroupButtonClick(SelectEvent event) {
        List<MetaTestEntryVO> entriesToAdd = new ArrayList<MetaTestEntryVO>();
        
        for (GroupVO group: groupsSm.getSelectedItems()) {
            MTEGroupVO entry = new MTEGroupVO();
            entry.setGroupId(group.getId());
            entry.setGroup(group);
            entry.setNumberOfQuestions(group.getQuestionsCount().intValue());
            entriesToAdd.add(entry);
        }
        
        int rowToMakeVisible = entriesStore.size();
        entriesStore.addAll(entriesToAdd);
        entriesSm.select(entriesToAdd, false);
        entriesGrid.getView().ensureVisible(rowToMakeVisible, 0, true);
        
        groupsPager.refresh();
    }
    
    @UiHandler("removeEntryButton")
    void removeEntryButtonClick(SelectEvent event) {
        for(MetaTestEntryVO entry: entriesSm.getSelectedItems()) {
            entriesStore.remove(entry);
        }
        groupsPager.refresh();
        questionsPager.refresh();
    }
    
    @UiHandler("moveEntryUpButton")
    void moveEntryUpButtonClick(SelectEvent event) {
        List<MetaTestEntryVO> entries = entriesSm.getSelectedItems();
        
        int index = entriesStore.indexOf(entries.get(0));
        index = index > 0 ? index - 1 : 0;
        
        for(MetaTestEntryVO entry: entries) {
            entriesStore.remove(entry);
        }
        
        entriesStore.addAll(index, entries);
        entriesSm.select(entries, false);
        entriesGrid.getView().ensureVisible(index, 0, true);        
    }
    
    @UiHandler("moveEntryDownButton")
    void moveEntryDownButtonClick(SelectEvent event) {
        List<MetaTestEntryVO> entries = entriesSm.getSelectedItems();
        
        int index = entriesStore.indexOf(entries.get(0));
        
        for(MetaTestEntryVO entry: entries) {
            entriesStore.remove(entry);
        }
        
        index = index < entriesStore.size() ? index + 1 : entriesStore.size();
        
        entriesStore.addAll(index, entries);
        entriesSm.select(entries, false);
        entriesGrid.getView().ensureVisible(index, 0, true);        
    }    
    
    private boolean validate() {
        StringBuilder sb = new StringBuilder();
        Widget ensureVisibleWidget = null;
        Component focusWidget = null;
        
        if (StringHelper.isEmptyOrNull(name.getValue())) {
            sb.append("Название теста не может быть пустым" + "<br>");            
            if (ensureVisibleWidget == null) {
                ensureVisibleWidget = name;
                focusWidget = name;
            }
        }
        
        if (entriesStore.size() == 0) {
            sb.append("В тесте должен быть хотя бы одинн элемент" + "<br>");            
        }
        
        if (sb.length() > 0) {
            AlertMessageBox alert = new AlertMessageBox("Ошибка", sb.toString());
            
            if (ensureVisibleWidget != null) {
                final Widget finalEnsureVisibleWidget = ensureVisibleWidget;
                final Component finalFocusWiget = focusWidget;
                alert.addHideHandler(new HideHandler() {
                    @Override
                    public void onHide(HideEvent event) {
                        Scheduler.get().scheduleDeferred(new ScheduledCommand() {                            
                            @Override
                            public void execute() {
                                top.getScrollSupport().ensureVisible(finalEnsureVisibleWidget);
                                if (finalFocusWiget != null) {
                                    finalFocusWiget.focus();
                                }
                            }
                        });
                    }
                });
            }
            
            alert.show();
            return false;
        }
        
        return true;
        
        
    }
    
    @UiHandler("saveButton")
    void saveButtonClick(SelectEvent event) {
        if (!validate()) {
            return;
        }
        
        final MetaTestVO metatest = driver.flush();
        Admin.RPC.saveMetatest(metatest, new AdminAsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                List<PublicationVO> publications = new ArrayList<PublicationVO>(publicationsStore.getAll());
                Admin.RPC.setPublicationsForMetatest(metatest.getId(), publications, new AdminAsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        window.hide();
                        fireEvent(new SaveEvent<MetaTestVO>(metatest));
                    }
                });                
            }
        });
        
    }
    
    @UiHandler("cancelButton")
    void cancelButtonClick(SelectEvent event) {
        window.hide();
    }
    
    @UiHandler("addPublicationButton")
    void addPublicationButtonClick(SelectEvent event) {
        showPublicationWindow(null, EditMode.etNew);
    }
    
    @UiHandler("copyPublicationButton")
    void copyPublicationButtonClick(SelectEvent event) {
        if (publicationsSm.getSelectedItem() != null) {
            showPublicationWindow(publicationsSm.getSelectedItem(), EditMode.etCopy);
        }
    }
    
    @UiHandler("editPublicationButton")
    void editPublicationButtonClick(SelectEvent event) {
        if (publicationsSm.getSelectedItem() != null) {
            showPublicationWindow(publicationsSm.getSelectedItem(), EditMode.etExisting);
        }
    }
    
    @UiHandler("removePublicationButton")
    void removePublicationButtonClick(SelectEvent event) {
        if (!NullHelper.isEmptyOrNull(publicationsSm.getSelectedItems())) {
            for (PublicationVO publication: publicationsSm.getSelectedItems()) {
                publicationsStore.remove(publication);
            }
        }
    }
    
    private void showPublicationWindow(PublicationVO publication, final EditMode editMode) {        
        switch (editMode) {
        case etNew:
            publication = new PublicationVO();
            publication.setReportsCount(0l);
            publication.setMetatestId(id.getValue());
            publication.setMetatest(VOHelper.cloneMeatestForPublicationEditor(originalMetatestInstance));
            break;
        case etCopy:
            publication = VOHelper.clonePublicationForEditor(publication);
            publication.makeNew();
            break;
        case etExisting:
            break;
        }
            
        final PublicationVO finalPublication = publication;
        
        PublicationWindow window = new PublicationWindow(publication, editMode, SaveMode.saveToObject);
        window.addSaveHandler(new SaveHandler<PublicationVO>() {
            @Override
            public void onSave(SaveEvent<PublicationVO> event) {
                if (editMode == EditMode.etNew || editMode == EditMode.etCopy) {
                    int rowToMakeVisible = publicationsStore.size();
                    publicationsStore.add(finalPublication);
                    publicationsSm.select(false, finalPublication);
                    publicationsGrid.getView().ensureVisible(rowToMakeVisible, 0, false);
                }
                else {
                    publicationsGrid.getView().refresh(false);
                }
            }
        });
        window.asWidget().show();
    }

    private void bindPagerAndSearchField(SearchField searchField, final PagingToolBar pager) {
        searchField.addFilterChangeHandler(new FilterHandler<String>() {
            @Override
            public void onFilter(FilterEvent<String> event) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        pager.first();
                    }
                });
            }
        });
    }
    
    private String getHeader(EditMode editType) {
        switch (editType) {
        case etCopy:
            return "Тест - копия";
        case etNew:
            return "Тест - новый";
        case etExisting:
            return "Тест - редактирование";
        default:
            return "Тест";
        }        
    }
    
    private void enablePublicationsButtons() {
        boolean isSomePublicationSelected = publicationsSm.getSelectedItems().size() > 0;
        boolean isOnePublicationSelected = publicationsSm.getSelectedItems().size() == 1;
        removePublicationButton.setEnabled(isSomePublicationSelected);
        copyPublicationButton.setEnabled(isOnePublicationSelected);
        editPublicationButton.setEnabled(isOnePublicationSelected);
    }

    private void enableDualListButtons() {
        boolean isSomeQuestionSelected = questionsSm.getSelectedItems().size() > 0;
        boolean isSomeGroupSelected = groupsSm.getSelectedItems().size() > 0;
        boolean isSomeEntrySelected = entriesSm.getSelectedItems().size() > 0;
        addQuestionButton.setEnabled(isSomeQuestionSelected);
        addGroupButton.setEnabled(isSomeGroupSelected);
        removeEntryButton.setEnabled(isSomeEntrySelected);
        moveEntryUpButton.setEnabled(isSomeEntrySelected);
        moveEntryDownButton.setEnabled(isSomeEntrySelected);
    }

    public MetatestWindow(MetaTestVO metatest, final EditMode editMode) {
        originalMetatestInstance = metatest;
        
        questionsSm = createSm(dualListSelectionHandler);
        questionsCm = createQuestionsCm(questionsSm);
        questionsStore = createStore(questionPropertyAccess.id());
        questionsRpcProxy = createQuestionsRpcProxy();
        questionsSearchField = createSearchField();
        questionsLoader = createQuestionsPagingLoader(questionsStore, questionsRpcProxy, questionsSearchField);
        questionsGrid = createGrid(questionsLoader, questionsStore, questionsCm, questionsSm);
    
        groupsSm = createSm(dualListSelectionHandler);
        groupsCm = createGroupsCm(groupsSm);
        groupsStore = createStore(groupPropertyAccess.id());
        groupsRpcProxy = createGroupsRpcProxy();
        groupsSearchField = createSearchField();
        groupsLoader = createGroupsPagingLoader(groupsStore, groupsRpcProxy, groupsSearchField);
        groupsGrid = createGrid(groupsLoader, groupsStore, groupsCm, groupsSm);
        
        entriesSm = createSm(dualListSelectionHandler);
        entriesQuestionsCountColumn = createQuestonsCountColumnConfig(entryPropertyAccess.numberOfQuestions(), 60, "Вопросов в тесте");
        entriesCm = createEntriesCm(entriesSm, entriesQuestionsCountColumn);
        entriesStore = createStore(entryPropertyAccess.id());
        entries = createEntriesStoreEditor(entriesStore);
        entriesGrid = createGrid(entriesStore, entriesCm, entriesSm);
        entriesEditing = createEntriesEditing(entriesGrid, entriesQuestionsCountColumn);
        
        publicationsSm = createSm(publicationSelectionHandler);
        publicationsCm = createPublicationsCm(publicationsSm);
        publicationsStore = createStore(publicationProperties.id());
        publicationsRpcProxy = createPublicationsRpcProxy();
        publicationsLoader = createListLoader(publicationsStore, publicationsRpcProxy, false);
        publicationsGrid = createPublicationsGrid(publicationsStore, publicationsCm, publicationsSm);

        // Create UI
        uiBinder.createAndBindUi(this);        
        
        window.setHeadingText(getHeader(editMode));
        
        elementsSourceLayout.setActiveWidget(questionsPanel);
        
        questionsPager.bind(questionsLoader);
        bindPagerAndSearchField(questionsSearchField, questionsPager);
        
        groupsPager.bind(groupsLoader);
        bindPagerAndSearchField(groupsSearchField, groupsPager);
        
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                questionsPager.first();
                groupsPager.first();
                if (editMode == EditMode.etExisting) {
                    publicationsLoader.load();
                }
                
                window.maximize();
            }
        });
        
        enableDualListButtons();
        enablePublicationsButtons();
        
        driver.initialize(this);
        driver.edit(metatest);
        
        if (editMode != EditMode.etExisting) {
            PublicationVO publication = new PublicationVO();
            publication.setReportsCount(0l);
            publication.setMetatestId(id.getValue());
            publication.setMetatest(VOHelper.cloneMeatestForPublicationEditor(originalMetatestInstance));
            publicationsStore.add(publication);
        }
    }

    @Override
    public Window asWidget() {
        return window;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        window.fireEvent(event);        
    }

    @Override
    public HandlerRegistration addSaveHandler(SaveHandler<MetaTestVO> handler) {
        return window.addHandler(handler, SaveEvent.getType());
    }

    @Override
    public HandlerRegistration addHideHandler(HideHandler handler) {
        return window.addHideHandler(handler);
    }
}