package com.attestator.admin.client.ui.widgets.tabs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.props.ReportVOPropertyAccess;
import com.attestator.admin.client.rpc.AdminAsyncCallback;
import com.attestator.admin.client.ui.ReportWindow;
import com.attestator.admin.client.ui.event.FilterEvent;
import com.attestator.admin.client.ui.event.FilterEvent.FilterHandler;
import com.attestator.admin.client.ui.widgets.PageringGridFilters;
import com.attestator.admin.client.ui.widgets.SearchField;
import com.attestator.common.client.helper.DateFilterHandler;
import com.attestator.common.shared.helper.NullHelper;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.helper.VOHelper;
import com.attestator.common.shared.vo.ReportVO;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.loader.BeforeLoadEvent;
import com.sencha.gxt.data.shared.loader.BeforeLoadEvent.BeforeLoadHandler;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterConfigBean;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfigBean;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
import com.sencha.gxt.data.shared.loader.Loader;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent.RowDoubleClickHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.DoublePropertyEditor;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.IntegerPropertyEditor;
import com.sencha.gxt.widget.core.client.grid.CheckBoxSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.filters.BooleanFilter;
import com.sencha.gxt.widget.core.client.grid.filters.DateFilter;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.NumericFilter;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;

public class ReportsTab extends Tab {
    interface UiBinderImpl extends UiBinder<Widget, ReportsTab> {
    }
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    private static ReportVOPropertyAccess reportProperties = GWT.create(ReportVOPropertyAccess.class);

    @UiField(provided=true)
    SearchField reportsSearchField;
    SearchField createSearchField() {
        return new SearchField();
    }
    
    @UiField(provided = true)
    Grid<ReportVO> reportsGrid;
    Grid<ReportVO> createReportsGrid(
            final PagingLoader<FilterPagingLoadConfig, PagingLoadResult<ReportVO>> loader,
            final ListStore<ReportVO> store, ColumnModel<ReportVO> cm) {
        
        Grid<ReportVO> result = new Grid<ReportVO>(store, cm);        
        result.setLoader(loader);
        result.getView().setForceFit(true);
        result.getView().setAutoFill(true);
        result.getView().setStripeRows(true);
        
        result.addRowDoubleClickHandler(new RowDoubleClickHandler() {            
            @Override
            public void onRowDoubleClick(RowDoubleClickEvent event) {
                showReportWindow(store.get(event.getRowIndex()));
            }
        });

        return result;
    }

    ListStore<ReportVO> reportsStore;

    ListStore<ReportVO> createReportsStore() {
        ListStore<ReportVO> result = new ListStore<ReportVO>(reportProperties.id());
        return result;
    }

    CheckBoxSelectionModel<ReportVO> reportsSm;

    CheckBoxSelectionModel<ReportVO> createReportsSm() {
        IdentityValueProvider<ReportVO> identity = new IdentityValueProvider<ReportVO>();
        CheckBoxSelectionModel<ReportVO> result = new CheckBoxSelectionModel<ReportVO>(
                identity) {
            @Override
            protected void onSelectChange(ReportVO model,
                    boolean select) {
                super.onSelectChange(model, select);
                enableButtons();
            }
        };

        return result;
    }

    ColumnModel<ReportVO> reportsCm;

    ColumnModel<ReportVO> createReportsCm(CheckBoxSelectionModel<ReportVO> sm) {
        
        List<ColumnConfig<ReportVO, ?>> l = new ArrayList<ColumnConfig<ReportVO, ?>>();
        l.add(sm.getColumn());
        l.add(new ColumnConfig<ReportVO, String>(reportProperties.metatestName(), 60, "Тест"));
        l.add(new ColumnConfig<ReportVO, String>(reportProperties.fullName(), 80, "Имя"));
        l.add(new ColumnConfig<ReportVO, String>(reportProperties.host(), 30, "Хост"));
        l.add(new ColumnConfig<ReportVO, String>(reportProperties.clientId(), 20, "ID клиента"));
        ColumnConfig<ReportVO, Date> ccStart = new ColumnConfig<ReportVO, Date>(reportProperties.start(), 30, "Первый ответ");
        ccStart.setCell(new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT)));
        l.add(ccStart);
        ColumnConfig<ReportVO, Date> ccEnd = new ColumnConfig<ReportVO, Date>(reportProperties.end(), 30, "Последний ответ");
        ccEnd.setCell(new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT)));
        l.add(ccEnd);
        ColumnConfig<ReportVO, Boolean> ccFinished = new ColumnConfig<ReportVO, Boolean>(reportProperties.finished(), 30, "Тест завершен");
        ccFinished.setCell(new AbstractCell<Boolean>() {
            @Override
            public void render(Context context,
                    Boolean value, SafeHtmlBuilder sb) {
                if (NullHelper.nullSafeTrue(value)) {
                    sb.appendEscaped("да");
                }
                else {
                    sb.appendEscaped("нет");
                }
            }
        });
        l.add(ccFinished);        
        l.add(new ColumnConfig<ReportVO, Double>(reportProperties.score(), 30, "Набрано баллов"));
        l.add(new ColumnConfig<ReportVO, Integer>(reportProperties.numAnswers(), 30, "Всего ответов"));
        l.add(new ColumnConfig<ReportVO, Integer>(reportProperties.numUnanswered(), 30, "Неотвечено"));
        l.add(new ColumnConfig<ReportVO, Integer>(reportProperties.numErrors(), 30, "Ошибок"));
        
        ColumnModel<ReportVO> result = new ColumnModel<ReportVO>(l);

        return result;
    }

    GridFilters<ReportVO> reportsGf;
    GridFilters<ReportVO> createReportsGf(Grid<ReportVO> grid, Loader<FilterPagingLoadConfig, ?> loader, final SearchField searchField, PagingToolBar pager) {
        GridFilters<ReportVO> result = new PageringGridFilters<ReportVO>(loader, pager);
        result.initPlugin(grid);
        
        DateFilter<ReportVO> startFilter = new DateFilter<ReportVO>(reportProperties.start());
        startFilter.setHandler(new DateFilterHandler());        
        result.addFilter(startFilter);
        
        DateFilter<ReportVO> endFilter = new DateFilter<ReportVO>(reportProperties.end());
        endFilter.setHandler(new DateFilterHandler());        
        result.addFilter(endFilter);
        
        BooleanFilter<ReportVO> finishedFilter = new BooleanFilter<ReportVO>(reportProperties.finished());
        result.addFilter(finishedFilter);

        NumericFilter<ReportVO, Double> scoreFilter = new NumericFilter<ReportVO, Double>(reportProperties.score(), new DoublePropertyEditor(NumberFormat.getFormat("0.00")));
        result.addFilter(scoreFilter);
        
        NumericFilter<ReportVO, Integer> numAnswersFilter = new NumericFilter<ReportVO, Integer>(reportProperties.numAnswers(), new IntegerPropertyEditor());
        result.addFilter(numAnswersFilter);

        NumericFilter<ReportVO, Integer> numUnansweredFilter = new NumericFilter<ReportVO, Integer>(reportProperties.numUnanswered(), new IntegerPropertyEditor());
        result.addFilter(numUnansweredFilter);

        NumericFilter<ReportVO, Integer> numErrorsFilter = new NumericFilter<ReportVO, Integer>(reportProperties.numErrors(), new IntegerPropertyEditor());
        result.addFilter(numErrorsFilter);
        
        //IMPORTANT! Any BeforeLoadHandler should be added after GridFilters binded to grid
        //because GridFilters is clear LoadConfig on own before load handler
        loader.addBeforeLoadHandler(new BeforeLoadHandler<FilterPagingLoadConfig>() {
            @Override
            public void onBeforeLoad(
                    BeforeLoadEvent<FilterPagingLoadConfig> event) {
                if (!StringHelper.isEmptyOrNull(searchField.getNewFilterValue())) {
                    FilterConfig filter = new FilterConfigBean();
                    filter.setField("firstName or lastName or middleName or metatestName");
                    filter.setComparison("contains");
                    filter.setValue(searchField.getNewFilterValue());
                    event.getLoadConfig().getFilters().add(filter);
                }
            }
        });
        
        return result;
    }
    
    PagingLoader<FilterPagingLoadConfig, PagingLoadResult<ReportVO>> reportsLoader;

    PagingLoader<FilterPagingLoadConfig, PagingLoadResult<ReportVO>> createReportsLoader(
            final ListStore<ReportVO> store, final SearchField searchField) {
        RpcProxy<FilterPagingLoadConfig, PagingLoadResult<ReportVO>> rpcProxy = new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<ReportVO>>() {
            @Override
            public void load(FilterPagingLoadConfig loadConfig,
                    AsyncCallback<PagingLoadResult<ReportVO>> callback) {
                Admin.RPC.loadReports(loadConfig, callback);
            }
        };

        final PagingLoader<FilterPagingLoadConfig, PagingLoadResult<ReportVO>> result = new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<ReportVO>>(
                rpcProxy) {
            @Override
            protected FilterPagingLoadConfig newLoadConfig() {
                FilterPagingLoadConfig result = new FilterPagingLoadConfigBean();
                return result;
            }
        };
        
        result.setRemoteSort(true);

        result.addLoadHandler(new LoadResultListStoreBinding<FilterPagingLoadConfig, ReportVO, PagingLoadResult<ReportVO>>(
                store) {
            @Override
            public void onLoad(LoadEvent<FilterPagingLoadConfig, PagingLoadResult<ReportVO>> event) {
                super.onLoad(event);
                enableButtons();
            }
        });
        
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
    
    @UiField(provided = true)
    PagingToolBar reportsPager;
    PagingToolBar createReportsPager(int pageSize) {
        return new PagingToolBar(pageSize);
    }
    
    @UiField
    TextButton deleteReportsButton;

    @UiField
    TextButton showReportButton;    

    public ReportsTab() {
        // Prepare fields for UiBuilder
        top = createTop();
        
        reportsSearchField = createSearchField();
        reportsSm = createReportsSm();
        reportsCm = createReportsCm(reportsSm);
        reportsStore = createReportsStore();
        reportsLoader = createReportsLoader(reportsStore, reportsSearchField);
        reportsPager = createReportsPager(50);
        reportsGrid = createReportsGrid(reportsLoader, reportsStore, reportsCm);
        reportsGf = createReportsGf(reportsGrid, reportsLoader, reportsSearchField, reportsPager);

        // Create UI
        initWidget(uiBinder.createAndBindUi(this));
        
        // Use created by UiBuilder fields to finish configuration
        reportsPager.bind(reportsLoader);
        
        bindPagerAndSearchField(reportsSearchField, reportsPager);
        
        addSelectionHandler(new SelectionHandler<Tab>() {            
            @Override
            public void onSelection(SelectionEvent<Tab> event) {
                refresh();                
            }
        });
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
    
    private void showReportWindow(ReportVO report) {
        Admin.RPC.loadReport(report.getId(), new AdminAsyncCallback<ReportVO>() {
            @Override
            public void onSuccess(ReportVO result) {
                ReportWindow window = new ReportWindow(result);
                window.asWidget().show();
            }
        });
    }
    
    @UiHandler("showReportButton") 
    public void showReportClick(SelectEvent event) {
        if (reportsSm.getSelectedItems().size() == 1) {
            showReportWindow(reportsSm.getSelectedItem());
        }
    }

    @UiHandler("deleteReportsButton") 
    public void deleteReportsButtonClick(SelectEvent event) {
        if (reportsSm.getSelectedItems().size() > 0) {
            List<String> ids = VOHelper.getIds(reportsSm.getSelectedItems());
            Admin.RPC.deleteReports(ids, new AdminAsyncCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    refreshGrid();
                }
            });
        }
    }
    
    private void refresh() {
        refreshGrid(); //enableButtons() called after grid refresh is finished to reflect new selected state
    }
    
    private void refreshGrid() {
        reportsLoader.load();
    }
    
    private void enableButtons() {
        boolean isOneSelected = reportsSm.getSelectedItems().size() == 1;
        boolean isSomeSelected = reportsSm.getSelectedItems().size() > 0;
        showReportButton.setEnabled(isOneSelected);
        deleteReportsButton.setEnabled(isSomeSelected);
    }
}
