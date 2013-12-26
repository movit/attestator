package com.attestator.admin.client.ui.widgets.tabs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.helper.WidgetHelpr;
import com.attestator.admin.client.props.PublicationsTreePropertyAccess;
import com.attestator.admin.client.rpc.AdminAsyncCallback;
import com.attestator.admin.client.ui.EditMode;
import com.attestator.admin.client.ui.MetatestWindow;
import com.attestator.admin.client.ui.PrintWindow;
import com.attestator.admin.client.ui.PrintWindow.Mode;
import com.attestator.admin.client.ui.PublicationWindow;
import com.attestator.admin.client.ui.event.MultyLinikSelectEvent;
import com.attestator.admin.client.ui.event.MultyLinikSelectEvent.MultyLinikSelectHandler;
import com.attestator.admin.client.ui.event.SaveEvent;
import com.attestator.admin.client.ui.event.SaveEvent.SaveHandler;
import com.attestator.admin.client.ui.widgets.MultylinkCell;
import com.attestator.admin.client.ui.widgets.TreeGridExt;
import com.attestator.common.client.ui.resolurces.Resources;
import com.attestator.common.shared.helper.ReportHelper;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.ModificationDateAwareVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.PublicationsTreeItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.ResizeCell;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.IconProvider;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.data.shared.loader.BeforeLoadEvent;
import com.sencha.gxt.data.shared.loader.ChildTreeStoreBinding;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.LoadExceptionEvent;
import com.sencha.gxt.data.shared.loader.LoaderHandler;
import com.sencha.gxt.data.shared.loader.TreeLoader;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent.RowDoubleClickHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

public class PublicationsTab extends Tab {
    private static final String NEW_PUBLICATION_LINK_ID = "newPublication";
    private static final String EDIT_TEST_LINK_ID = "editTest";
    private static final String DELETE_TEST_LINK_ID = "deleteTest";
    private static final String COPY_TEST_LINK_ID = "copyTest";
    private static final String PRINT_TEST_LINK_ID = "printTest";
    private static final String PRINT_TO_PDF_TEST_LINK_ID = "printToPdfTest";

    private static final String EDIT_PUBLICATION_LINK_ID = "editPublication";
    private static final String COPY_PUBLICATION_LINK_ID = "copyPublication";
    private static final String DELETE_PUBLICATION_LINK_ID = "deletePublication";
    
    interface UiBinderImpl extends UiBinder<Widget, PublicationsTab> {
    }
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    private static PublicationsTreePropertyAccess publicationProperties = new PublicationsTreePropertyAccess();
    
    TreeLoader<PublicationsTreeItem> gridLoader;

    TreeLoader<PublicationsTreeItem> createLoader(
            final TreeStore<PublicationsTreeItem> store) {
      
        RpcProxy<PublicationsTreeItem, List<PublicationsTreeItem>> rpcProxy = new RpcProxy<PublicationsTreeItem, List<PublicationsTreeItem>>() {
            @Override
            public void load(PublicationsTreeItem root,
                    AsyncCallback<List<PublicationsTreeItem>> callback) {
                Admin.RPC.loadPublicationsTree(root, callback);                
            }
        };
        
        TreeLoader<PublicationsTreeItem> result = 
                new TreeLoader<PublicationsTreeItem>(rpcProxy) {
            @Override
            public boolean hasChildren(PublicationsTreeItem parent) {
                return parent instanceof MetaTestVO;
            }            
        };
        
        result.addLoadHandler(
                new ChildTreeStoreBinding<PublicationsTreeItem>(store));

        return result;
    }
    
    @UiField(provided = true)    
    TreeGrid<PublicationsTreeItem> grid;
    TreeGrid<PublicationsTreeItem> createGrid(TreeLoader<PublicationsTreeItem> loader, final TreeStore<PublicationsTreeItem> store, ColumnModel<PublicationsTreeItem> cm, ColumnConfig<PublicationsTreeItem, ?> cc) {        
        final TreeGrid<PublicationsTreeItem> result = new TreeGridExt<PublicationsTreeItem>(store, cm, cc);
        result.setIconProvider(new IconProvider<PublicationsTreeItem>() {            
            @Override
            public ImageResource getIcon(PublicationsTreeItem model) {
                if (model instanceof MetaTestVO) {
                    return Resources.ICONS.checkBoxChecked16x16();
                }
                else {
                    return Resources.ICONS.file16x16();
                }                
            }
        });
        result.getView().setAutoFill(true);
        result.getView().setForceFit(true);
        result.getView().setSortingEnabled(false);
        result.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        result.setAutoExpand(true);        
        result.setAutoLoad(true);        
        result.setTreeLoader(loader);
        result.addRowDoubleClickHandler(new RowDoubleClickHandler() {
            @Override
            public void onRowDoubleClick(RowDoubleClickEvent event) {
                final PublicationsTreeItem item = result.getSelectionModel().getSelectedItem();
                if (item instanceof PublicationVO) {
                    showPublicationWindow(((PublicationVO)item).getId(), null, EditMode.etExisting);
                }
                else if (item instanceof MetaTestVO) {
                    showMetatestWindow(((MetaTestVO)item).getId(), EditMode.etExisting);
                }
            }
        });
        return result;
    }

    @UiField(provided = true)
    TreeStore<PublicationsTreeItem> gridStore;

    TreeStore<PublicationsTreeItem> createGridStore() {
        TreeStore<PublicationsTreeItem> result = new TreeStore<PublicationsTreeItem>(publicationProperties.id);        
        return result;
    }

    @UiField(provided = true)
    ColumnModel<PublicationsTreeItem> gridCm;

    private ColumnConfig<PublicationsTreeItem, PublicationsTreeItem> createHeaderColumnConfig(IdentityValueProvider<PublicationsTreeItem> valueProvider, int width, String header) {
        ColumnConfig<PublicationsTreeItem, PublicationsTreeItem> result = new ColumnConfig<PublicationsTreeItem, PublicationsTreeItem>(valueProvider, width, header);
        
        result.setCell(new ResizeCell<PublicationsTreeItem>() {
            @Override
            public void render(Context context,
                    PublicationsTreeItem value, SafeHtmlBuilder sb) {
                
                if (value instanceof PublicationVO) {
                    PublicationVO publication = (PublicationVO) value;
                    sb.appendEscaped("Публикация ");
                    
                    Date now = new Date();
                    
                    if (publication.getStart() != null && publication.getStart().after(now)) {
                        sb.appendEscaped("неактивна (еще не началась)");
                        return;
                    }                    
                    if (publication.getEnd() != null && publication.getEnd().before(now)) {
                        sb.appendEscaped("неактивна (уже закончилась)");
                        return;
                    }                    
                    sb.appendEscaped("активна");                    
                    sb.appendHtmlConstant(" <a target='_blank' href='/player/#test?t=" + Admin.getLoggedUser().getTenantId() + "&publicationId=" + publication.getId() +"'>посмотреть</a>");
                }
                else if (value instanceof MetaTestVO) {
                    MetaTestVO metatest = (MetaTestVO) value;
                    sb.appendHtmlConstant("<b>Тест &laquo;" + metatest.getName() + "&raquo;</b> (" + ReportHelper.formatQuestionsCount(metatest.getNumberOfQuestions()) + ")");
                }
            }
        });        
        result.setComparator(new Comparator<PublicationsTreeItem>() {
            @Override
            public int compare(PublicationsTreeItem o1, PublicationsTreeItem o2) {                
                return ((ModificationDateAwareVO) o1).getCreated().compareTo(((ModificationDateAwareVO) o2).getCreated());
            }
        });
        result.setSortable(false);
        result.setHideable(false);
        return result;
    }
    
    
    private ColumnConfig<PublicationsTreeItem, PublicationsTreeItem> createPublicationActionsColumnConfig(IdentityValueProvider<PublicationsTreeItem> valueProvider) {
        ColumnConfig<PublicationsTreeItem, PublicationsTreeItem> result = new ColumnConfig<PublicationsTreeItem, PublicationsTreeItem>(valueProvider);
        
        MultylinkCell<PublicationsTreeItem> cell = new MultylinkCell<PublicationsTreeItem>() {
            @Override
            public void render(Context context,
                    PublicationsTreeItem value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<div style='text-align: right;'>");
                if (value instanceof PublicationVO) {
                    sb.append(createClickableElement(EDIT_PUBLICATION_LINK_ID, "редактировать публикацию", Resources.ICONS.edit16x16()));
                    sb.append(createClickableElement(COPY_PUBLICATION_LINK_ID, "копировать публикацию", Resources.ICONS.copy16x16()));
                    sb.append(createClickableElement(DELETE_PUBLICATION_LINK_ID, "удалить публикацию", Resources.ICONS.delete16x16()));
                }
                else {
                    sb.append(createClickableElement(PRINT_TEST_LINK_ID, "печать теста", Resources.ICONS.print16x16()));
                    sb.append(createClickableElement(PRINT_TO_PDF_TEST_LINK_ID, "печать теста в PDF", Resources.ICONS.pdf16x16()));
                    sb.append(createClickableElement(NEW_PUBLICATION_LINK_ID, "добавить публикацию", Resources.ICONS.addFile16x16()));
                    sb.append(createClickableElement(EDIT_TEST_LINK_ID, "редактировать тест", Resources.ICONS.edit16x16()));
                    sb.append(createClickableElement(COPY_TEST_LINK_ID, "копировать тест", Resources.ICONS.copy16x16()));
                    sb.append(createClickableElement(DELETE_TEST_LINK_ID, "удалить тест", Resources.ICONS.delete16x16()));
                }
                sb.appendHtmlConstant("</div>");
            }
        };
        cell.addMultyLinikSelectHandler(new MultyLinikSelectHandler<PublicationsTreeItem>() {
            @Override
            public void onSelect(MultyLinikSelectEvent<PublicationsTreeItem> event) {
                
                if (EDIT_PUBLICATION_LINK_ID.equals(event.getLinkType())) {
                    PublicationVO publication = (PublicationVO)event.getValue();
                    showPublicationWindow(publication.getId(), null, EditMode.etExisting);
                }
                else if (COPY_PUBLICATION_LINK_ID.equals(event.getLinkType())) {
                    PublicationVO publication = (PublicationVO)event.getValue();
                    showPublicationWindow(publication.getId(), null, EditMode.etCopy);
                }
                else if (DELETE_PUBLICATION_LINK_ID.equals(event.getLinkType())) {
                    final PublicationVO publication = (PublicationVO)event.getValue();
                    Admin.RPC.deletePublications(Arrays.asList(publication.getId()), new AdminAsyncCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            refreshGrid(null, null);                            
                        }
                    });
                }
                else if (NEW_PUBLICATION_LINK_ID.equals(event.getLinkType())) { 
                    final MetaTestVO metatest = (MetaTestVO) event.getValue();
                    showPublicationWindow(null, metatest, EditMode.etNew);
                }
                else if (EDIT_TEST_LINK_ID.equals(event.getLinkType())) {
                    showMetatestWindow(((MetaTestVO)event.getValue()).getId(), EditMode.etExisting);
                }                
                else if (COPY_TEST_LINK_ID.equals(event.getLinkType())) {
                    showMetatestWindow(((MetaTestVO)event.getValue()).getId(), EditMode.etCopy);
                }
                else if (PRINT_TEST_LINK_ID.equals(event.getLinkType())) { 
                    final MetaTestVO metatest = (MetaTestVO) event.getValue();
                    PrintWindow.showWindow(metatest.getId(), Mode.print);
                }
                else if (PRINT_TO_PDF_TEST_LINK_ID.equals(event.getLinkType())) { 
                    final MetaTestVO metatest = (MetaTestVO) event.getValue();
                    PrintWindow.showWindow(metatest.getId(), Mode.saveAsPdf);
                }
                else if (DELETE_TEST_LINK_ID.equals(event.getLinkType())) {
                    MetaTestVO metatest = (MetaTestVO) event.getValue();
                    Admin.RPC.deleteMetatests(Arrays.asList(metatest.getId()), new AdminAsyncCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            refreshGrid(null, null);
                        }
                    });
                }
            }
        });
        result.setCell(cell);
        result.setHideable(false);
        result.setResizable(false);
        result.setWidth(180);
        result.setFixed(true);
        result.setMenuDisabled(true);
        result.setSortable(false);
        return result;
    }
    
    private ColumnModel<PublicationsTreeItem> createGridCm() {
        
        List<ColumnConfig<PublicationsTreeItem, ?>> l = new ArrayList<ColumnConfig<PublicationsTreeItem, ?>>();
        
        l.add(createHeaderColumnConfig(new IdentityValueProvider<PublicationsTreeItem>(), 100, "Тест / Публикация"));
        l.add(new ColumnConfig<PublicationsTreeItem, Long>(publicationProperties.reportsCount, 14, "Отчетов"));
        
        l.add(new ColumnConfig<PublicationsTreeItem, String>(publicationProperties.start, 20, "Начало"));
        l.add(new ColumnConfig<PublicationsTreeItem, String>(publicationProperties.end, 20, "Конец"));
        
        l.add(new ColumnConfig<PublicationsTreeItem, String>(publicationProperties.fillBeforeTest, 40, "Заполнять перед тестом"));
        l.add(new ColumnConfig<PublicationsTreeItem, String>(publicationProperties.maxAttempts , 20, "Макс. попыток"));
        l.add(new ColumnConfig<PublicationsTreeItem, String>(publicationProperties.minScore, 15, "Мин. баллов"));
        
        l.add(new ColumnConfig<PublicationsTreeItem, String>(publicationProperties.maxTakeTestTime, 20, "Времени на тест"));
        l.add(createPublicationActionsColumnConfig(new IdentityValueProvider<PublicationsTreeItem>()));
        
        WidgetHelpr.disableColumnHeaderOperations(l);
        
        ColumnModel<PublicationsTreeItem> result = new ColumnModel<PublicationsTreeItem>(l);

        return result;
    }

    @UiField(provided=true)
    protected VerticalLayoutContainer top;
    private VerticalLayoutContainer createTop() {
        VerticalLayoutContainer result = new VerticalLayoutContainer() {
            @Override
            protected void onAfterFirstAttach() {
                super.onAfterFirstAttach();
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        refresh();
                    }
                });
            } 
        };
        return result;
    }
    

    public PublicationsTab() {
        // Prepare fields for UiBuilder
        top = createTop();
        
        gridCm = createGridCm();
        gridStore = createGridStore();
        gridLoader = createLoader(gridStore);
        grid = createGrid(gridLoader, gridStore, gridCm, gridCm.getColumn(0));
        
        // Create UI
        initWidget(uiBinder.createAndBindUi(this));
        
        addSelectionHandler(new SelectionHandler<Tab>() {            
            @Override
            public void onSelection(SelectionEvent<Tab> event) {
                refresh();                
            }
        });
    }
    
    private void refresh() {
        refreshGrid(null, null);
    }
    
    private HandlerRegistration selectAfterLoadRegistration;
    private void refreshGrid(final String metaTestIdToSelect, final String publicationIdToSelect) {
        if (metaTestIdToSelect != null) {
            selectAfterLoadRegistration = gridLoader.addLoaderHandler(new LoaderHandler<PublicationsTreeItem, List<PublicationsTreeItem>>() {
                private void unregister() {
                    if (selectAfterLoadRegistration != null) {
                        selectAfterLoadRegistration.removeHandler();
                    } 
                }
                
                @Override
                public void onLoad(
                        LoadEvent<PublicationsTreeItem, List<PublicationsTreeItem>> event) {
                    if (publicationIdToSelect != null && event.getLoadConfig() != null) {
                        // Loaded metatest subtree and we need select specific publication
                        if (metaTestIdToSelect.equals(((MetaTestVO)event.getLoadConfig()).getId())) {
                            PublicationVO publication = (PublicationVO)gridStore.findModelWithKey(publicationIdToSelect);
                            grid.getSelectionModel().select(false, publication);
                            unregister();
                        }
                    }
                    else if (publicationIdToSelect == null && event.getLoadConfig() == null) {
                        // Loaded root tree and we need select specific metatest
                        MetaTestVO metatest = (MetaTestVO)gridStore.findModelWithKey(metaTestIdToSelect);
                        grid.getSelectionModel().select(false, metatest);
                        unregister();
                    }
                }
                
                @Override
                public void onLoadException(LoadExceptionEvent<PublicationsTreeItem> event) {
                    unregister();
                }
                
                @Override
                public void onBeforeLoad(BeforeLoadEvent<PublicationsTreeItem> event) {
                }
            });
        }
        gridStore.clear();
        gridLoader.load();
    }
    
    private void showMetatestWindow(String metatestId, EditMode editMode) {
        SaveHandler<MetaTestVO> metatestSaveHandler = new SaveHandler<MetaTestVO>() {
            @Override
            public void onSave(SaveEvent<MetaTestVO> event) {
                refreshGrid(event.getValue().getId(), null);
            }
        };        
        MetatestWindow.showWindow(editMode, metatestId, metatestSaveHandler, null);
    }
    
    private void showPublicationWindow(String publicationId, MetaTestVO metatest, EditMode editMode) {
        SaveHandler<PublicationVO> savePublicationHandler = new SaveHandler<PublicationVO>() {
            @Override
            public void onSave(SaveEvent<PublicationVO> event) {
                refreshGrid(event.getValue().getMetatestId(), event.getValue().getId());
            }
        };        
        PublicationWindow.showWindow(editMode, publicationId, metatest, savePublicationHandler, null);
    }
    
    @UiHandler("newTestButton") 
    public void newTestButton(SelectEvent event) {
        showMetatestWindow(null, EditMode.etNew);
    }
    
    @UiHandler("refreshButton") 
    public void refreshButtonClick(SelectEvent event) {
        refreshGrid(null, null);
    }
}
