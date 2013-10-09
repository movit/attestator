package com.attestator.admin.client.ui.widgets.tabs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.props.PublicationsTreePropertyAccess;
import com.attestator.admin.client.rpc.AdminAsyncCallback;
import com.attestator.admin.client.ui.PublicationWindow;
import com.attestator.admin.client.ui.event.MultyLinikSelectEvent;
import com.attestator.admin.client.ui.event.MultyLinikSelectEvent.MultyLinikSelectHandler;
import com.attestator.admin.client.ui.event.SaveEvent;
import com.attestator.admin.client.ui.event.SaveEvent.SaveHandler;
import com.attestator.admin.client.ui.widgets.MultylinkCell;
import com.attestator.common.client.ui.resolurces.Resources;
import com.attestator.common.shared.helper.ReportHelper;
import com.attestator.common.shared.vo.BaseVO;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.ModificationDateAwareVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.attestator.common.shared.vo.PublicationsTreeItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.HandlerRegistration;
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
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.data.shared.loader.BeforeLoadEvent;
import com.sencha.gxt.data.shared.loader.ChildTreeStoreBinding;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.LoadExceptionEvent;
import com.sencha.gxt.data.shared.loader.LoaderHandler;
import com.sencha.gxt.data.shared.loader.TreeLoader;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent.RowDoubleClickHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

public class PublicationsTab extends Composite {
    private static final String NEW_PUBLICATION_LINK_ID = "newPublication";
    private static final String EDIT_TEST_LINK_ID = "editTest";
    private static final String DELETE_TEST_LINK_ID = "deleteTest";
    private static final String COPY_TEST_LINK_ID = "copyTest";

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
        TreeGrid<PublicationsTreeItem> result = new TreeGrid<PublicationsTreeItem>(store, cm, cc);
        result.getStyle().setLeafIcon(Resources.ICONS.file16x16());
        result.getStyle().setNodeOpenIcon(Resources.ICONS.checkBoxChecked16x16());
        result.getStyle().setNodeCloseIcon(Resources.ICONS.checkBoxChecked16x16());
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
                PublicationsTreeItem item = store.getAll().get(event.getRowIndex());
                if (item instanceof PublicationVO) {
                    showPublicationWindow((PublicationVO)item);
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
                    sb.appendHtmlConstant("<b>Тест &laquo;" + metatest.getName() + "&raquo;</b> (" + ReportHelper.formatNumberOfQuestions(metatest.getNumberOfQuestions()) + ")");
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
                    sb.append(createClickableElement(EDIT_PUBLICATION_LINK_ID, "изменить публикацию", Resources.ICONS.edit16x16()));
                    sb.append(createClickableElement(COPY_PUBLICATION_LINK_ID, "копировать публикацию", Resources.ICONS.copy16x16()));
                    sb.append(createClickableElement(DELETE_PUBLICATION_LINK_ID, "удалить публикацию", Resources.ICONS.delete16x16()));
                }
                else {
                    sb.append(createClickableElement(EDIT_TEST_LINK_ID, "изменить тест", Resources.ICONS.edit16x16()));
                    sb.append(createClickableElement(NEW_PUBLICATION_LINK_ID, "добавить публикацию", Resources.ICONS.addFile16x16()));
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
                            gridStore.remove(publication);                            
                        }
                    });
                }
                else if (NEW_PUBLICATION_LINK_ID.equals(event.getLinkType())) { 
                    final MetaTestVO metatest = (MetaTestVO) event.getValue();
                    PublicationVO publication = new PublicationVO();
                    publication.setMetatestId(metatest.getId());
                    publication.setMetatest(metatest);
                    showPublicationWindow(publication);
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
    }
    
    private void refresh() {
        refreshGrid(null, null); //TODO enableButtons() called after grid refresh is finished to reflect new selected state
    }
    
    private HandlerRegistration selectAfterLoadRegistration;
    private void refreshGrid(final String metaTestId, final String publicationId) {
        if (metaTestId != null) {
            selectAfterLoadRegistration = gridLoader.addLoaderHandler(new LoaderHandler<PublicationsTreeItem, List<PublicationsTreeItem>>() {
                private void unregister() {
                    if (selectAfterLoadRegistration != null) {
                        selectAfterLoadRegistration.removeHandler();
                    } 
                }
                
                @Override
                public void onLoad(
                        LoadEvent<PublicationsTreeItem, List<PublicationsTreeItem>> event) {
                    if (publicationId != null && event.getLoadConfig() != null) {
                        if (metaTestId.equals(((MetaTestVO)event.getLoadConfig()).getId())) {
                            PublicationVO publication = (PublicationVO)gridStore.findModelWithKey(publicationId);
                            grid.getSelectionModel().select(false, publication);
                            unregister();
                        }
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
        gridLoader.load();
    }
    
    private void showPublicationWindow(final PublicationVO publication) {
        PublicationWindow window = new PublicationWindow(publication);
        window.addSaveHandler(new SaveHandler<PublicationVO>() {
            @Override
            public void onSave(SaveEvent<PublicationVO> event) {
                refreshGrid(publication.getMetatest().getId(), publication.getId());
            }
        });
        window.asWidget().show();
    }
    
    @UiHandler("refreshButton") 
    public void refreshButtonClick(SelectEvent event) {
        refreshGrid(null, null);
    }
}
