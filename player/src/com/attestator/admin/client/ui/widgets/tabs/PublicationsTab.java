package com.attestator.admin.client.ui.widgets.tabs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.props.PublicationVOPropertyAccess;
import com.attestator.admin.client.rpc.AdminAsyncCallback;
import com.attestator.admin.client.ui.PublicationWindow;
import com.attestator.admin.client.ui.event.GridGroupClickEvent;
import com.attestator.admin.client.ui.event.GridGroupClickEvent.GridGroupClickHandler;
import com.attestator.admin.client.ui.event.MultyLinikSelectEvent;
import com.attestator.admin.client.ui.event.MultyLinikSelectEvent.MultyLinikSelectHandler;
import com.attestator.admin.client.ui.event.SaveEvent;
import com.attestator.admin.client.ui.event.SaveEvent.SaveHandler;
import com.attestator.admin.client.ui.widgets.ClicableGroupingViewDefaultApperance;
import com.attestator.admin.client.ui.widgets.GroupingViewExt;
import com.attestator.admin.client.ui.widgets.MultylinkCell;
import com.attestator.common.shared.helper.DateHelper;
import com.attestator.common.shared.helper.NullHelper;
import com.attestator.common.shared.helper.ReportHelper;
import com.attestator.common.shared.vo.AdditionalQuestionVO;
import com.attestator.common.shared.vo.BaseVO;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.ResizeCell;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.loader.ListLoadConfig;
import com.sencha.gxt.data.shared.loader.ListLoadResult;
import com.sencha.gxt.data.shared.loader.ListLoader;
import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GroupingView.GroupingData;
import com.sencha.gxt.widget.core.client.info.Info;

public class PublicationsTab extends Composite {
    private static final String NEW_PUBLICATION_LINK_ID = "newPublication";
//    private static final String EDIT_TEST_LINK_ID = "editTest";
//    private static final String DELETE_TEST_LINK_ID = "deleteTest";
//    private static final String COPY_TEST_LINK_ID = "copyTest";

    private static final String EDIT_PUBLICATION_LINK_ID = "editPublication";
    private static final String COPY_PUBLICATION_LINK_ID = "copyPublication";
    private static final String DELETE_PUBLICATION_LINK_ID = "deletePublication";
    
    interface UiBinderImpl extends UiBinder<Widget, PublicationsTab> {
    }
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    private static PublicationVOPropertyAccess publicationProperties = GWT.create(PublicationVOPropertyAccess.class);

    @UiField(provided = true)
    GroupingViewExt<PublicationVO> gridView;
    GroupingViewExt<PublicationVO> createGridView() {
        ClicableGroupingViewDefaultApperance apperance = new ClicableGroupingViewDefaultApperance() {
            @Override
            public SafeHtml renderGroupHeader(GroupingData<?> groupInfo) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                if (groupInfo.getValue() instanceof MetaTestVO) {
                    MetaTestVO metatest = (MetaTestVO)groupInfo.getValue();
                    
                    sb.appendEscaped("Тест ");
                    sb.appendHtmlConstant("&laquo;").appendEscaped(metatest.getName()).appendHtmlConstant("&raquo;");
                    sb.appendEscaped(" (" + ReportHelper.formatNumberOfQuestions(metatest.getNumberOfQuestions()) + ") ");                    
                    
                    sb.append(createClickableElement(NEW_PUBLICATION_LINK_ID, metatest.getId(), "создать публикацию"));
//                    sb.append(createClicableElement(EDIT_TEST_LINK_ID, metatest.getId(), "изменить тест"));
//                    sb.append(createClicableElement(COPY_TEST_LINK_ID, metatest.getId(), "копировать"));
//                    sb.append(createClicableElement(DELETE_TEST_LINK_ID, metatest.getId(), "удалить"));
                }
                else if (groupInfo.getValue() != null) {
                    sb.appendEscaped(groupInfo.getValue().toString());
                }
                else {
                    sb.appendEscaped(" ");
                }
                return sb.toSafeHtml();
            }
        }; 
        
        GroupingViewExt<PublicationVO> result = new GroupingViewExt<PublicationVO>(apperance);
        
        result.setStripeRows(true);
        result.setAutoFill(true);
        result.setForceFit(true);
        result.setShowGroupedColumn(false);
        result.setEnableGroupingMenu(false);
        
        result.addGridGroupClickHandler(new GridGroupClickHandler<XElement>() {
            @Override
            public void onClick(GridGroupClickEvent<XElement> event) {
                if (NEW_PUBLICATION_LINK_ID.equals(event.getLinkType())) {
                    final String metatestId = event.getId();
                    PublicationVO existingPublication = Iterables.find(grid.getStore().getAll(), new Predicate<PublicationVO>() {
                        @Override
                        public boolean apply(PublicationVO pub) {                            
                            return metatestId.equals(pub.getMetatestId()); 
                        }
                    });                    
                    
                    PublicationVO publication = new PublicationVO();
                    publication.setMetatestId(existingPublication.getMetatestId());
                    publication.setMetatest(existingPublication.getMetatest());
                    
                    showPublicationWindow(publication);
                    
                }
            }
        });
        
        return result;
    }
    
    ListLoader<ListLoadConfig, ListLoadResult<PublicationVO>> gridLoader;

    ListLoader<ListLoadConfig, ListLoadResult<PublicationVO>> createLoader(
            final ListStore<PublicationVO> store) {
      
        RpcProxy<ListLoadConfig, ListLoadResult<PublicationVO>> rpcProxy = new RpcProxy<ListLoadConfig, ListLoadResult<PublicationVO>>() {
            @Override
            public void load(ListLoadConfig loadConfig,
                    AsyncCallback<ListLoadResult<PublicationVO>> callback) {
                Admin.RPC.loadPublications(callback);                
            }
        };
        
        ListLoader<ListLoadConfig, ListLoadResult<PublicationVO>> result = 
                new ListLoader<ListLoadConfig, ListLoadResult<PublicationVO>>(rpcProxy);
        result.addLoadHandler(
                new LoadResultListStoreBinding<ListLoadConfig, PublicationVO, ListLoadResult<PublicationVO>>(store));

        return result;
    }
    
    @UiField(provided = true)    
    Grid<PublicationVO> grid;
    Grid<PublicationVO> createGrid(ListLoader<ListLoadConfig, ListLoadResult<PublicationVO>> loader, ListStore<PublicationVO> store, ColumnModel<PublicationVO> cm) {        
        Grid<PublicationVO> result = new Grid<PublicationVO>(store, cm);
        result.setLoader(loader);
        return result;
    }

    @UiField(provided = true)
    ListStore<PublicationVO> gridStore;

    ListStore<PublicationVO> createGridStore() {
        ListStore<PublicationVO> result = new ListStore<PublicationVO>(publicationProperties.id());        
        return result;
    }

    @UiField(provided = true)
    ColumnModel<PublicationVO> gridCm;

    private ColumnConfig<PublicationVO, Date> createDateColumnConfig(ValueProvider<PublicationVO, Date> valueProvider, int width, String header) {
        ColumnConfig<PublicationVO, Date> result = new ColumnConfig<PublicationVO, Date>(valueProvider, width, header);
        result.setCell(new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT)){
            @Override
            public void render(com.google.gwt.cell.client.Cell.Context context,
                    Date value, SafeHtmlBuilder sb) {
                if (value != null) {
                    super.render(context, value, sb);
                }
                else {
                    sb.appendEscaped("не указано");
                }
            }
        });
        return result;
    }
    
    private ColumnConfig<PublicationVO, Integer> createAttemptsColumnConfig(ValueProvider<PublicationVO, Integer> valueProvider, int width, String header) {
        ColumnConfig<PublicationVO, Integer> result = new ColumnConfig<PublicationVO, Integer>(valueProvider, width, header);
        result.setCell(new ResizeCell<Integer>() {
            @Override
            public void render(Context context,
                    Integer value, SafeHtmlBuilder sb) {
                if (NullHelper.nullSafeIntegerOrZerro(value) > 0) {
                    sb.appendEscaped(value.toString());
                }
                else {
                    sb.appendEscaped("неограничено");
                }
            }
        });
        return result;
    }
    
    private ColumnConfig<PublicationVO, Long> createTestTimeColumnConfig(ValueProvider<PublicationVO, Long> valueProvider, int width, String header) {
        ColumnConfig<PublicationVO, Long> result = new ColumnConfig<PublicationVO, Long>(valueProvider, width, header);
        result.setCell(new ResizeCell<Long>() {
            @Override
            public void render(Context context,
                    Long value, SafeHtmlBuilder sb) {
                if (NullHelper.nullSafeIntegerOrZerro(value) > 0) {                    
                    sb.appendEscaped(DateHelper.formatTimeValue(value / 1000));
                }
                else {
                    sb.appendEscaped("неограничено");
                }
            }
        });
        return result;
    }

    private ColumnConfig<PublicationVO, PublicationVO> createIsActiveColumnConfig(IdentityValueProvider<PublicationVO> valueProvider, int width, String header) {
        ColumnConfig<PublicationVO, PublicationVO> result = new ColumnConfig<PublicationVO, PublicationVO>(valueProvider, width, header);
        
        result.setCell(new ResizeCell<PublicationVO>() {
            @Override
            public void render(Context context,
                    PublicationVO value, SafeHtmlBuilder sb) {
                
                sb.appendEscaped("Публикация ");
                
                Date now = new Date();
                
                if (value.getStart() != null && value.getStart().after(now)) {
                    sb.appendEscaped("неактивна (еще не началась)");
                    return;
                }
                
                if (value.getEnd() != null && value.getEnd().before(now)) {
                    sb.appendEscaped("неактивна (уже закончилась)");
                    return;
                }
                
                sb.appendEscaped("активна");
                
                sb.appendHtmlConstant(" <a target='_blank' href='/player/#test?t=" + Admin.getLoggedUser().getTenantId() + "&publicationId=" + value.getId() +"'>посмотреть</a>");
            }
        });
        
        return result;
    }
    
    private ColumnConfig<PublicationVO, PublicationVO> createAdditionalQuestionsColumnConfig(IdentityValueProvider<PublicationVO> valueProvider, int width, String header) {
        ColumnConfig<PublicationVO, PublicationVO> result = new ColumnConfig<PublicationVO, PublicationVO>(valueProvider, width, header);
        
        result.setCell(new ResizeCell<PublicationVO>() {
            @Override
            public void render(Context context,
                    PublicationVO value, SafeHtmlBuilder sb) {
                boolean someQuestions = false;
                if (value.isThisAskLastName()) {
                    sb.appendEscaped("Фамилию");
                    if (value.isThisAskLastNameRequired()) {
                        sb.appendHtmlConstant("*");
                    }
                    someQuestions = true;
                }
                
                if (value.isThisAskFirstName()) {
                    if (someQuestions) {
                        sb.appendEscaped(", ");
                    }
                    sb.appendEscaped("Имя");
                    if (value.isThisAskFirstNameRequired()) {
                        sb.appendHtmlConstant("*");
                    }
                    someQuestions = true;
                }
                
                if (value.isThisAskMiddleName()) {
                    if (someQuestions) {
                        sb.appendEscaped(", ");
                    }
                    sb.appendEscaped("Отчество");
                    if (value.isThisAskMiddleNameRequired()) {
                        sb.appendHtmlConstant("*");
                    }                
                    someQuestions = true;
                }
                
                for (AdditionalQuestionVO aq : value.getAdditionalQuestions()) {
                    if (someQuestions) {
                        sb.appendEscaped(", ");
                    }
                    sb.appendEscaped(aq.getText());
                    if (aq.isThisRequired()) {
                        sb.appendHtmlConstant("*");
                    }                
                    someQuestions = true;
                }
                
                if (!someQuestions) {
                    sb.appendEscaped("ничего не нужно");
                }
            }
        });
        
        return result;
    }
    
    
    private ColumnConfig<PublicationVO, PublicationVO> createPublicationActionsColumnConfig(IdentityValueProvider<PublicationVO> valueProvider, int width, String header) {
        ColumnConfig<PublicationVO, PublicationVO> result = new ColumnConfig<PublicationVO, PublicationVO>(valueProvider, width, header);
        
        MultylinkCell<PublicationVO> cell = new MultylinkCell<PublicationVO>() {
            @Override
            public void render(Context context,
                    PublicationVO value, SafeHtmlBuilder sb) {
                sb.append(createClickableElement(EDIT_PUBLICATION_LINK_ID, "изменить"));
                sb.append(createClickableElement(COPY_PUBLICATION_LINK_ID, "копировать"));
                sb.append(createClickableElement(DELETE_PUBLICATION_LINK_ID, "удалить"));
            }
        };
        cell.addMultyLinikSelectHandler(new MultyLinikSelectHandler<PublicationVO>() {
            @Override
            public void onSelect(MultyLinikSelectEvent<PublicationVO> event) {
                if (EDIT_PUBLICATION_LINK_ID.equals(event.getLinkType())) {
                    showPublicationWindow(event.getValue());
                }
                else if (COPY_PUBLICATION_LINK_ID.equals(event.getLinkType())) {
                    PublicationVO publication = event.getValue();
                    publication.setId(BaseVO.idString());
                    showPublicationWindow(publication);
                }
                else if (DELETE_PUBLICATION_LINK_ID.equals(event.getLinkType())) {
                    Admin.RPC.deletePublications(Arrays.asList(event.getValue().getId()), new AdminAsyncCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            refreshGrid();                            
                        }
                    });
                }
            }
        });
        result.setCell(cell);
        
        return result;
    }
    
    private ColumnConfig<PublicationVO, MetaTestVO> createMetatestColumnConfig(ValueProvider<PublicationVO, MetaTestVO> valueProvider, int width, String header) {
        ColumnConfig<PublicationVO, MetaTestVO> result = new ColumnConfig<PublicationVO, MetaTestVO>(valueProvider, width, header);
        result.setCell(new AbstractCell<MetaTestVO>() {
            @Override
            public void render(Context context,
                    MetaTestVO value, SafeHtmlBuilder sb) {
                sb.appendEscaped(value.getName());                
            }
        });
        result.setComparator(new Comparator<MetaTestVO>() {
            
            @Override
            public int compare(MetaTestVO o1, MetaTestVO o2) {
                if (o1.getId() != null && o2.getId() != null ) {
                    return o1.getId().compareTo(o2.getId()); 
                }                
                return o1.hashCode() - o2.hashCode(); 
            }
        });
        return result;
    }
    
    ColumnModel<PublicationVO> createGridCm() {
        
        List<ColumnConfig<PublicationVO, ?>> l = new ArrayList<ColumnConfig<PublicationVO, ?>>();
        
        l.add(createMetatestColumnConfig(publicationProperties.metatest(), 20, "Тест"));
        l.add(createIsActiveColumnConfig(new IdentityValueProvider<PublicationVO>(), 40, "Активна"));
        l.add(new ColumnConfig<PublicationVO, Long>(publicationProperties.reportsCount() , 20, "Отчетов"));
        l.add(createDateColumnConfig(publicationProperties.start(), 20, "Начало"));
        l.add(createDateColumnConfig(publicationProperties.end(), 20, "Конец"));
        
        l.add(createAdditionalQuestionsColumnConfig(new IdentityValueProvider<PublicationVO>(), 40, "Заполнять перед тестом"));
        l.add(createAttemptsColumnConfig(publicationProperties.maxAttempts() , 20, "Макс. попыток"));
        l.add(new ColumnConfig<PublicationVO, Double>(publicationProperties.minScore(), 20, "Нужно баллов"));
        
        l.add(createTestTimeColumnConfig(publicationProperties.maxTakeTestTime(), 20, "Времени на тест"));
        l.add(createPublicationActionsColumnConfig(new IdentityValueProvider<PublicationVO>(), 35, ""));
        
        ColumnModel<PublicationVO> result = new ColumnModel<PublicationVO>(l);

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
        
        gridView = createGridView();
        gridCm = createGridCm();
        gridStore = createGridStore();
        gridLoader = createLoader(gridStore);
        grid = createGrid(gridLoader, gridStore, gridCm);
        
        // Create UI
        initWidget(uiBinder.createAndBindUi(this));
        gridView.groupBy(gridCm.getColumn(0));
    }
    
    private void refresh() {
        refreshGrid(); //TODO enableButtons() called after grid refresh is finished to reflect new selected state
    }
    
    private void refreshGrid() {
        gridLoader.load();
    }
    
    private void showPublicationWindow(PublicationVO publication) {
        PublicationWindow window = new PublicationWindow(publication);
        window.addSaveHandler(new SaveHandler<PublicationVO>() {
            @Override
            public void onSave(SaveEvent<PublicationVO> event) {
                refreshGrid();
            }
        });
        window.asWidget().show();
    }
    
}
