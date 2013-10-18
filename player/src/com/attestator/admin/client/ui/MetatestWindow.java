package com.attestator.admin.client.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.props.GroupVOPropertyAccess;
import com.attestator.admin.client.props.MetatestEntryVOPropertyAccess;
import com.attestator.admin.client.props.PublicationVOPropertyAccess;
import com.attestator.admin.client.props.PublicationsTreePropertyAccess;
import com.attestator.admin.client.props.QuestionVOPropertyAccess;
import com.attestator.admin.client.rpc.AdminAsyncCallback;
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
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.BaseVO;
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
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.sencha.gxt.cell.core.client.ResizeCell;
import com.sencha.gxt.core.client.IdentityValueProvider;
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
import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.ContentPanel.ContentPanelAppearance;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.AccordionLayoutContainer;
import com.sencha.gxt.widget.core.client.event.HideEvent.HasHideHandlers;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.CheckBoxSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridSelectionModel;
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

    @UiFactory
    public ContentPanel createContentPanel(ContentPanelAppearance appearance) {
      return new ContentPanel(appearance);
    }
    
    SimpleEditor<String> id = SimpleEditor.of();
    
    @UiField
    TextField name;
    
    @Ignore
    @UiField
    Window window;
    
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
    TextButton addQuestionButton;

    @UiField
    @Ignore
    TextButton addGroupButton;
    
    @UiField
    @Ignore
    TextButton removeEntryButton;
        
    @Ignore
    @UiField(provided = true)
    SearchField questionsSearchField;
    
    @Ignore
    @UiField(provided = true)
    SearchField groupsSearchField;

    @Ignore
    SearchField createSearchField() {
        SearchField result = new SearchField();
        return result;
    }
    
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
    
    @Ignore
    @UiField
    PagingToolBar questionsPager;

    @Ignore
    @UiField
    PagingToolBar groupsPager;
    
    @Ignore
    ListStore<MetaTestEntryVO> entriesStore;
    ListStoreEditor<MetaTestEntryVO> entries;
    ListStoreEditor<MetaTestEntryVO> createEntriesStoreEditor(ListStore<MetaTestEntryVO> store) {
        return new ListStoreEditor<MetaTestEntryVO>(store);
    }

    @Ignore
    ListStore<QuestionVO> questionsStore;
    @Ignore
    ListStore<GroupVO> groupsStore;
    @Ignore
    ListStore<PublicationVO> publicationsStore;
    
    <T> ListStore<T> createStore(ModelKeyProvider<T> keyProvider) {
        ListStore<T> result = new ListStore<T>(keyProvider);
        return result;
    }

    @Ignore
    RpcProxy<FilterPagingLoadConfig, PagingLoadResult<GroupVO>> groupsRpcProxy; 
    RpcProxy<FilterPagingLoadConfig, PagingLoadResult<GroupVO>> createGroupsRpcProxy() {
        return new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<GroupVO>>() {
             @Override
             public void load(FilterPagingLoadConfig loadConfig,
                     AsyncCallback<PagingLoadResult<GroupVO>> callback) {
                 Admin.RPC.loadGroupsPage(loadConfig, callback);
             }
        };
    }
    
    @Ignore
    RpcProxy<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>> questionsRpcProxy; 
    RpcProxy<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>> createQuestionsRpcProxy() {
        return new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>>() {
             @Override
             public void load(FilterPagingLoadConfig loadConfig,
                     AsyncCallback<PagingLoadResult<QuestionVO>> callback) {
                 Admin.RPC.loadQuestions(loadConfig, callback);
             }
        };
    }

    @Ignore
    RpcProxy<ListLoadConfig, ListLoadResult<PublicationVO>> publicationsRpcProxy; 
    RpcProxy<ListLoadConfig, ListLoadResult<PublicationVO>> createPublicationsRpcProxy() {
        return new RpcProxy<ListLoadConfig, ListLoadResult<PublicationVO>>() {
             @Override
             public void load(ListLoadConfig loadConfig,
                     AsyncCallback<ListLoadResult<PublicationVO>> callback) {                 
                 Admin.RPC.loadPublicationsByMetatestId(id.getValue(), loadConfig, callback);
             }
        };
    }
    
    @Ignore
    PagingLoader<FilterPagingLoadConfig, PagingLoadResult<GroupVO>> groupsLoader;
    @Ignore
    PagingLoader<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>> questionsLoader;
    @Ignore
    ListLoader<ListLoadConfig, ListLoadResult<PublicationVO>> publicationsLoader;
    
    <T> ListLoader<ListLoadConfig, ListLoadResult<T>> createListLoader(
            final ListStore<T> store, RpcProxy<ListLoadConfig, ListLoadResult<T>> rpcProxy) {        
        ListLoader<ListLoadConfig, ListLoadResult<T>> result = new ListLoader<ListLoadConfig, ListLoadResult<T>>(
                rpcProxy);        
        result.addLoadHandler(new LoadResultListStoreBinding<ListLoadConfig, T, ListLoadResult<T>>(store));
        return result;
    }

    <T> PagingLoader<FilterPagingLoadConfig, PagingLoadResult<T>> createPagingLoader(
            final ListStore<T> store, RpcProxy<FilterPagingLoadConfig, PagingLoadResult<T>> rpcProxy, final SearchField searchField) {

        PagingLoader<FilterPagingLoadConfig, PagingLoadResult<T>> result = new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<T>>(
                rpcProxy) {
            @Override
            protected FilterPagingLoadConfig newLoadConfig() {
                FilterPagingLoadConfig result = new FilterPagingLoadConfigBean();

                if (StringHelper.isNotEmptyOrNull(searchField.getNewFilterValue())) {
                    String filterValue = searchField.getNewFilterValue();
                    filterValue = filterValue.trim();
                    filterValue = filterValue.toLowerCase();
                    filterValue = filterValue.replaceAll("\\s+", " ");

                    FilterConfig textFilter = new FilterConfigBean();
                    textFilter.setComparison("contains");
                    textFilter.setField("text");
                    textFilter.setType("string");
                    textFilter.setValue(filterValue);

                    result.getFilters().add(textFilter);
                }

                return result;
            }
        };
        result.setRemoteSort(true);
        result.addLoadHandler(new LoadResultListStoreBinding<FilterPagingLoadConfig, T, PagingLoadResult<T>>(store));

        return result;
    }
    
    @Ignore
    CheckBoxSelectionModel<GroupVO> groupsSm;
    @Ignore
    CheckBoxSelectionModel<QuestionVO> questionsSm;
    @Ignore
    CheckBoxSelectionModel<MetaTestEntryVO> entriesSm;

    @Ignore
    CheckBoxSelectionModel<PublicationVO> publicationsSm;
    
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
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    <T> CheckBoxSelectionModel<T> createSm(SelectionChangedHandler selectionCahngeHandler) {
        IdentityValueProvider<T> identity = new IdentityValueProvider<T>();
        CheckBoxSelectionModel<T> result = new CheckBoxSelectionModel<T>(identity);
        if (selectionCahngeHandler != null) {
            result.addSelectionChangedHandler(selectionCahngeHandler);
        }
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
//                        sb.appendHtmlConstant(ReportHelper.formatQuestion(question, "<b>Вопрос: </b>"));
                        sb.appendHtmlConstant("<b>Вопрос: </b>" + question.getText());
                    }
                }
                else if (value instanceof MTEGroupVO) {
                    GroupVO group = ((MTEGroupVO) value).getGroup();
                    if (group != null) {
                        sb.appendHtmlConstant("<b>Группа: </b>" + group.getName());
                    }
                }
                sb.appendHtmlConstant("</div>");
            }
        });
        
        return result;

    }

    private ColumnConfig<PublicationVO, PublicationVO> createPublicationActionsColumnConfig(IdentityValueProvider<PublicationVO> valueProvider) {
        ColumnConfig<PublicationVO, PublicationVO> result = new ColumnConfig<PublicationVO, PublicationVO>(valueProvider);
        
        MultylinkCell<PublicationVO> cell = new MultylinkCell<PublicationVO>() {
            @Override
            public void render(Context context,
                    PublicationVO value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<div style='text-align: right;'>");
                if (value instanceof PublicationVO) {
                    sb.append(createClickableElement(EDIT_PUBLICATION_LINK_ID, "изменить публикацию", Resources.ICONS.edit16x16()));
                    sb.append(createClickableElement(COPY_PUBLICATION_LINK_ID, "копировать публикацию", Resources.ICONS.copy16x16()));
                    sb.append(createClickableElement(DELETE_PUBLICATION_LINK_ID, "удалить публикацию", Resources.ICONS.delete16x16()));
                }
                sb.appendHtmlConstant("</div>");
            }
        };
        cell.addMultyLinikSelectHandler(new MultyLinikSelectHandler<PublicationVO>() {
            @Override
            public void onSelect(MultyLinikSelectEvent<PublicationVO> event) {
                
                if (EDIT_PUBLICATION_LINK_ID.equals(event.getLinkType())) {
                    PublicationVO publication = (PublicationVO)event.getValue();
                    showPublicationWindow(publication);
                }
                else if (COPY_PUBLICATION_LINK_ID.equals(event.getLinkType())) {
                    PublicationVO publication = (PublicationVO)event.getValue();
                    publication.setId(BaseVO.idString());
                    showPublicationWindow(publication);
                }
                else if (DELETE_PUBLICATION_LINK_ID.equals(event.getLinkType())) {
                    final PublicationVO publication = (PublicationVO)event.getValue();
                    Admin.RPC.deletePublications(Arrays.asList(publication.getId()), new AdminAsyncCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
//                            gridStore.remove(publication);                            
                        }
                    });
                }
            }
        });
        result.setCell(cell);
        result.setSortable(false);
        result.setHideable(false);
        result.setResizable(false);
        result.setWidth(120);
        result.setFixed(true);
        result.setMenuDisabled(true);        
        return result;
    }
    
    @Ignore
    ColumnModel<PublicationVO> publicationsCm;
    ColumnModel<PublicationVO> createPublicationsCm(CheckBoxSelectionModel<PublicationVO> sm){
        List<ColumnConfig<PublicationVO, ?>> l = new ArrayList<ColumnConfig<PublicationVO, ?>>();
        
        l.add(new ColumnConfig<PublicationVO, Long>(publicationsTreeProperties.reportsCount, 14, "Отчетов"));
        
        l.add(new ColumnConfig<PublicationVO, String>(publicationsTreeProperties.start, 20, "Начало"));
        l.add(new ColumnConfig<PublicationVO, String>(publicationsTreeProperties.end, 20, "Конец"));
        
        l.add(new ColumnConfig<PublicationVO, String>(publicationsTreeProperties.fillBeforeTest, 40, "Заполнять перед тестом"));
        l.add(new ColumnConfig<PublicationVO, String>(publicationsTreeProperties.maxAttempts , 20, "Макс. попыток"));
        l.add(new ColumnConfig<PublicationVO, String>(publicationsTreeProperties.minScore, 15, "Мин. баллов"));
        
        l.add(new ColumnConfig<PublicationVO, String>(publicationsTreeProperties.maxTakeTestTime, 20, "Времени на тест"));
        l.add(createPublicationActionsColumnConfig(new IdentityValueProvider<PublicationVO>()));
        
        ColumnModel<PublicationVO> result = new ColumnModel<PublicationVO>(l);

        return result;        
    }
    
    @Ignore
    ColumnModel<MetaTestEntryVO> entriesCm;
    ColumnModel<MetaTestEntryVO> createEntriesCm(
            CheckBoxSelectionModel<MetaTestEntryVO> sm) {

        List<ColumnConfig<MetaTestEntryVO, ?>> l = new ArrayList<ColumnConfig<MetaTestEntryVO, ?>>();
        l.add(sm.getColumn());        
        l.add(createEntryDescriptionColumnConfig(new IdentityValueProvider<MetaTestEntryVO>(),
                300, "Элемент теста"));
        l.add(new ColumnConfig<MetaTestEntryVO, Integer>(
                  entryPropertyAccess.numberOfQuestions(), 5, "Вопрсов"));
        
        ColumnModel<MetaTestEntryVO> result = new ColumnModel<MetaTestEntryVO>(l);

        return result;
    }
    
    @Ignore
    ColumnModel<GroupVO> groupsCm;
    ColumnModel<GroupVO> createGroupsCm(
            CheckBoxSelectionModel<GroupVO> sm) {

        List<ColumnConfig<GroupVO, ?>> l = new ArrayList<ColumnConfig<GroupVO, ?>>();
        l.add(sm.getColumn());
        
        l.add(new ColumnConfig<GroupVO, String>(
                groupPropertyAccess.name(), 100, "Название"));
        
        l.add(new ColumnConfig<GroupVO, Long>(
                groupPropertyAccess.questionsCount(), 20, "Вопросов"));
        
        ColumnModel<GroupVO> result = new ColumnModel<GroupVO>(l);

        return result;
    }

    
    @Ignore
    ColumnModel<QuestionVO> questionsCm;
    ColumnModel<QuestionVO> createQuestionsCm(
            CheckBoxSelectionModel<QuestionVO> sm) {
//ContentPanel
//ToolButton
//AccordionLayoutContainer
//BorderLayoutContainer        
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
    
    public MetatestWindow(MetaTestVO metatest) {
                
        questionsSm = createSm(dualListSelectionHandler);
        questionsCm = createQuestionsCm(questionsSm);
        questionsStore = createStore(questionPropertyAccess.id());
        questionsRpcProxy = createQuestionsRpcProxy();
        questionsSearchField = createSearchField();
        questionsLoader = createPagingLoader(questionsStore, questionsRpcProxy, questionsSearchField);
        questionsGrid = createGrid(questionsLoader, questionsStore, questionsCm, questionsSm);

        groupsSm = createSm(dualListSelectionHandler);
        groupsCm = createGroupsCm(groupsSm);
        groupsStore = createStore(groupPropertyAccess.id());
        groupsRpcProxy = createGroupsRpcProxy();
        groupsSearchField = createSearchField();
        groupsLoader = createPagingLoader(groupsStore, groupsRpcProxy, groupsSearchField);
        groupsGrid = createGrid(groupsLoader, groupsStore, groupsCm, groupsSm);
        
        entriesSm = createSm(dualListSelectionHandler);
        entriesCm = createEntriesCm(entriesSm);
        entriesStore = createStore(entryPropertyAccess.id());
        entries = createEntriesStoreEditor(entriesStore);
        entriesGrid = createGrid(entriesStore, entriesCm, entriesSm);
        
        publicationsSm = createSm(null);
        publicationsCm = createPublicationsCm(publicationsSm);
        publicationsStore = createStore(publicationProperties.id());
        publicationsRpcProxy = createPublicationsRpcProxy();
        publicationsLoader = createListLoader(publicationsStore, publicationsRpcProxy);
        publicationsGrid = createGrid(publicationsStore, publicationsCm, publicationsSm);
        
        // Create UI
        uiBinder.createAndBindUi(this);        
        
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
                publicationsLoader.load();
            }
        });
        
        enableDualListButtons();
        
        driver.initialize(this);
        driver.edit(metatest);
    }
    
    private void showPublicationWindow(final PublicationVO publication) {
        PublicationWindow window = new PublicationWindow(publication);
        window.addSaveHandler(new SaveHandler<PublicationVO>() {
            @Override
            public void onSave(SaveEvent<PublicationVO> event) {
//TODO                refreshGrid(publication.getMetatest().getId(), publication.getId());
            }
        });
        window.asWidget().show();
    }
    
    @UiHandler("cancelButton")
    protected void cancelButtonClick(SelectEvent event) {
        window.hide();
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
    
    private void enableDualListButtons() {
        boolean isSomeQuestionSelected = questionsSm.getSelectedItems().size() > 0;
        boolean isSomeGroupSelected = groupsSm.getSelectedItems().size() > 0;
        boolean isSomeEntrySelected = entriesSm.getSelectedItems().size() > 0;
        addQuestionButton.setEnabled(isSomeQuestionSelected);
        addGroupButton.setEnabled(isSomeGroupSelected);
        removeEntryButton.setEnabled(isSomeEntrySelected);        
    }
}
