package com.attestator.admin.client.ui.widgets.tabs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.props.PublicationVOPropertyAccess;
import com.attestator.admin.client.rpc.AdminAsyncCallback;
import com.attestator.admin.client.ui.widgets.BooleanCell;
import com.attestator.common.shared.vo.PublicationVO;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfigBean;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.grid.CheckBoxSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;

public class PublicationsTab extends Composite {
    interface UiBinderImpl extends UiBinder<Widget, PublicationsTab> {
    }
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    private static PublicationVOPropertyAccess publicationProperties = GWT.create(PublicationVOPropertyAccess.class);

    @UiField(provided = true)
    Grid<PublicationVO> grid;
    Grid<PublicationVO> createGrid(final ListStore<PublicationVO> store, ColumnModel<PublicationVO> cm) {        
        Grid<PublicationVO> result = new Grid<PublicationVO>(store, cm);
        return result;
    }

    @UiField(provided = true)
    ListStore<PublicationVO> gridStore;

    ListStore<PublicationVO> createGridStore() {
        ListStore<PublicationVO> result = new ListStore<PublicationVO>(publicationProperties.id());        
        return result;
    }

    @UiField(provided = true)
    CheckBoxSelectionModel<PublicationVO> gridSm;

    CheckBoxSelectionModel<PublicationVO> createGridSm() {
        IdentityValueProvider<PublicationVO> identity = new IdentityValueProvider<PublicationVO>();
        CheckBoxSelectionModel<PublicationVO> result = new CheckBoxSelectionModel<PublicationVO>(
                identity) {
            @Override
            protected void onSelectChange(PublicationVO model,
                    boolean select) {
                super.onSelectChange(model, select);
                enableButtons();
            }
        };

        return result;
    }

    @UiField(provided = true)
    ColumnModel<PublicationVO> gridCm;

    private ColumnConfig<PublicationVO, Boolean> createBooleanColumnConfig(ValueProvider<PublicationVO, Boolean> valueProvider, int width, String header) {
        ColumnConfig<PublicationVO, Boolean> result = new ColumnConfig<PublicationVO, Boolean>(valueProvider, 20, header);
        result.setCell(new BooleanCell());
        return result;
    }

    private ColumnConfig<PublicationVO, Date> createDateColumnConfig(ValueProvider<PublicationVO, Date> valueProvider, int width, String header) {
        ColumnConfig<PublicationVO, Date> result = new ColumnConfig<PublicationVO, Date>(valueProvider, 20, header);
        result.setCell(new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT)));
        return result;
    }
    
    ColumnModel<PublicationVO> createGridCm(CheckBoxSelectionModel<PublicationVO> sm) {
        
        List<ColumnConfig<PublicationVO, ?>> l = new ArrayList<ColumnConfig<PublicationVO, ?>>();
        
        l.add(sm.getColumn());
        l.add(new ColumnConfig<PublicationVO, String>(publicationProperties.metatestId() , 20, "Тест ID"));
        l.add(new ColumnConfig<PublicationVO, String>(publicationProperties.metatestName() , 20, "Тест"));
        l.add(new ColumnConfig<PublicationVO, Long>(publicationProperties.reportsCount() , 20, "Отчетов"));
        l.add(createDateColumnConfig(publicationProperties.start(), 30, "Начало"));
        l.add(createDateColumnConfig(publicationProperties.end(), 30, "Конец"));        
        l.add(new ColumnConfig<PublicationVO, String>(publicationProperties.introduction() , 20, "Ведение"));
        l.add(new ColumnConfig<PublicationVO, Integer>(publicationProperties.maxAttempts() , 20, "Макс. попыток"));

        l.add(new ColumnConfig<PublicationVO, Double>(publicationProperties.minScore(), 20, "Нужно баллов"));
        
        l.add(createBooleanColumnConfig(publicationProperties.interruptOnFalure(), 20, "Прерывать ли"));
        
        l.add(new ColumnConfig<PublicationVO, Long>(publicationProperties.maxTakeTestTime(), 20, "Время на тест"));
        l.add(new ColumnConfig<PublicationVO, Long>(publicationProperties.maxQuestionAnswerTime(), 20, "Время на вопрос"));

        l.add(createBooleanColumnConfig(publicationProperties.allowSkipQuestions(), 20, "Можно пропускать"));
        l.add(createBooleanColumnConfig(publicationProperties.allowInterruptTest(), 20, "Прерывать тест"));
        l.add(createBooleanColumnConfig(publicationProperties.randomQuestionsOrder(), 20, "Случайный порядок"));
        
        l.add(createBooleanColumnConfig(publicationProperties.askFirstName(), 20, "Спрашивать имя"));
        l.add(createBooleanColumnConfig(publicationProperties.askFirstNameRequired(), 20, "Имя обязательно"));
        
        l.add(createBooleanColumnConfig(publicationProperties.askLastName(), 20, "Спрашивать фамилию"));
        l.add(createBooleanColumnConfig(publicationProperties.askLastNameRequired(), 20, "Фамилия обязательна"));
        
        l.add(createBooleanColumnConfig(publicationProperties.askMiddleName(), 20, "Спрашивать отчество"));
        l.add(createBooleanColumnConfig(publicationProperties.askMiddleNameRequired(), 20, "Отчество обязательно"));        
        
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
    
    @UiField
    TextButton deleteReportsButton;

    @UiField
    TextButton showReportButton;    

    public PublicationsTab() {
        // Prepare fields for UiBuilder
        top = createTop();
        
        gridSm = createGridSm();
        gridCm = createGridCm(gridSm);
        gridStore = createGridStore();
        grid = createGrid(gridStore, gridCm);

        // Create UI
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    private void refresh() {
        refreshGrid(); //TODO enableButtons() called after grid refresh is finished to reflect new selected state
    }
    
    private void refreshGrid() {
        Admin.RPC.loadAllPublications(new AdminAsyncCallback<List<PublicationVO>>() {
            @Override
            public void onSuccess(List<PublicationVO> result) {
                gridStore.clear();
                gridStore.addAll(result);
            }
        });
    }
    
    private void enableButtons() {
        boolean isOneSelected = gridSm.getSelectedItems().size() == 1;
        boolean isSomeSelected = gridSm.getSelectedItems().size() > 0;
        showReportButton.setEnabled(isOneSelected);
        deleteReportsButton.setEnabled(isSomeSelected);
    }
}
