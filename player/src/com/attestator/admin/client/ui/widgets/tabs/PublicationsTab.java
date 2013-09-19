package com.attestator.admin.client.ui.widgets.tabs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.props.PublicationVOPropertyAccess;
import com.attestator.admin.client.rpc.AdminAsyncCallback;
import com.attestator.admin.client.ui.widgets.BooleanCell;
import com.attestator.admin.client.ui.widgets.ClickableAnchorCell;
import com.attestator.admin.client.ui.widgets.GroupSummaryViewExt;
import com.attestator.common.shared.vo.MetaTestVO;
import com.attestator.common.shared.vo.PublicationVO;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.theme.base.client.grid.GroupingViewDefaultAppearance;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GroupingView.GroupingData;
import com.sencha.gxt.widget.core.client.grid.SummaryColumnConfig;
import com.sencha.gxt.widget.core.client.grid.SummaryRenderer;
import com.sencha.gxt.widget.core.client.info.Info;

public class PublicationsTab extends Composite {
    interface UiBinderImpl extends UiBinder<Widget, PublicationsTab> {
    }
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);
    private static PublicationVOPropertyAccess publicationProperties = GWT.create(PublicationVOPropertyAccess.class);

    @UiField(provided = true)
    GroupSummaryViewExt<PublicationVO> gridView;
    GroupSummaryViewExt<PublicationVO> createGridView() {
        GroupSummaryViewExt<PublicationVO> result = new GroupSummaryViewExt<PublicationVO>(new GroupingViewDefaultAppearance() {
            @Override
            public SafeHtml renderGroupHeader(GroupingData<?> groupInfo) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                if (groupInfo.getValue() instanceof MetaTestVO) {
                    sb.appendEscaped(((MetaTestVO)groupInfo.getValue()).getName());
                    sb.appendHtmlConstant("<a href='aaaa'>test link</a>");
                }
                else if (groupInfo.getValue() != null) {
                    sb.appendEscaped(groupInfo.getValue().toString());
                }
                else {
                    sb.appendEscaped(" ");
                }
                return sb.toSafeHtml();
            }
        });
        result.setForceFit(true);
        result.setShowGroupedColumn(false);
        result.setEnableGroupingMenu(false);
        return result;
    }
    
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

//    @UiField(provided = true)
//    CheckBoxSelectionModel<PublicationVO> gridSm;

//    CheckBoxSelectionModel<PublicationVO> createGridSm() {
//        IdentityValueProvider<PublicationVO> identity = new IdentityValueProvider<PublicationVO>();
//        CheckBoxSelectionModel<PublicationVO> result = new CheckBoxSelectionModel<PublicationVO>(
//                identity) {
//            @Override
//            protected void onSelectChange(PublicationVO model,
//                    boolean select) {
//                super.onSelectChange(model, select);
//                enableButtons();
//            }
//        };
//
//        return result;
//    }

    @UiField(provided = true)
    ColumnModel<PublicationVO> gridCm;

    private ColumnConfig<PublicationVO, Boolean> createBooleanColumnConfig(ValueProvider<PublicationVO, Boolean> valueProvider, int width, String header) {
        ColumnConfig<PublicationVO, Boolean> result = new SummaryColumnConfig<PublicationVO, Boolean>(valueProvider, width, header);
        result.setCell(new BooleanCell());
        return result;
    }

    private ColumnConfig<PublicationVO, Date> createDateColumnConfig(ValueProvider<PublicationVO, Date> valueProvider, int width, String header) {
        ColumnConfig<PublicationVO, Date> result = new SummaryColumnConfig<PublicationVO, Date>(valueProvider, width, header);
        result.setCell(new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT)));
        return result;
    }

    private SummaryColumnConfig<PublicationVO, PublicationVO> createPublicationActionsColumnConfig(IdentityValueProvider<PublicationVO> valueProvider, int width, String header) {
        SummaryColumnConfig<PublicationVO, PublicationVO> result = new SummaryColumnConfig<PublicationVO, PublicationVO>(valueProvider, width, header);
        
        result.setCell(new AbstractCell<PublicationVO>() {
            @Override
            public void render(Context context,
                    PublicationVO value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<a href='#a'>Изменить</a> <a href='#b'>Копировать</a> <a href='#b'>Удалить</a>");                
            }
        });
        
        return result;
    }
    
    private SummaryColumnConfig<PublicationVO, PublicationVO> createPublicationSummaryColumnConfig(IdentityValueProvider<PublicationVO> valueProvider, int width, String header) {
        SummaryColumnConfig<PublicationVO, PublicationVO> result = new SummaryColumnConfig<PublicationVO, PublicationVO>(valueProvider, width, header);
        
        result.setCell(new AbstractCell<PublicationVO>() {
            @Override
            public void render(Context context,
                    PublicationVO value, SafeHtmlBuilder sb) {                                
            }
        });

        result.setSummaryRenderer(new SummaryRenderer<PublicationVO>() {            
            @Override
            public SafeHtml render(Number value,
                    Map<ValueProvider<? super PublicationVO, ?>, Number> data) {
                return SafeHtmlUtils.fromTrustedString("<a href='#b'>Опубликовать</a> <a href='#a'>Изменить</a> <a href='#b'>Копировать</a> <a href='#b'>Удалить</a>");
            }
        });
        
        
        return result;
    }

    private ColumnConfig<PublicationVO, MetaTestVO> createMetatestColumnConfig(ValueProvider<PublicationVO, MetaTestVO> valueProvider, int width, String header) {
        ColumnConfig<PublicationVO, MetaTestVO> result = new SummaryColumnConfig<PublicationVO, MetaTestVO>(valueProvider, width, header);
        result.setCell(new AbstractCell<MetaTestVO>() {
            @Override
            public void render(Context context,
                    MetaTestVO value, SafeHtmlBuilder sb) {
                sb.appendEscaped(value.getName());                
            }
        });
        return result;
    }
    
    ColumnModel<PublicationVO> createGridCm() {
        
        List<ColumnConfig<PublicationVO, ?>> l = new ArrayList<ColumnConfig<PublicationVO, ?>>();
        
        l.add(createMetatestColumnConfig(publicationProperties.metatest(), 20, "Тест"));
        l.add(createPublicationSummaryColumnConfig(new IdentityValueProvider<PublicationVO>(), 40, ""));
//        l.add(new SummaryColumnConfig<PublicationVO, Long>(publicationProperties.reportsCount() , 20, "Отчетов"));
        l.add(createDateColumnConfig(publicationProperties.start(), 30, "Начало"));
        l.add(createDateColumnConfig(publicationProperties.end(), 30, "Конец"));        
//        l.add(new SummaryColumnConfig<PublicationVO, String>(publicationProperties.introduction() , 20, "Ведение"));
        l.add(new SummaryColumnConfig<PublicationVO, Integer>(publicationProperties.maxAttempts() , 20, "Макс. попыток"));

        l.add(new SummaryColumnConfig<PublicationVO, Double>(publicationProperties.minScore(), 20, "Нужно баллов"));
        
//        l.add(createBooleanColumnConfig(publicationProperties.interruptOnFalure(), 20, "Прерывать ли"));
        
        l.add(new SummaryColumnConfig<PublicationVO, Long>(publicationProperties.maxTakeTestTime(), 20, "Время на тест"));
//        l.add(new SummaryColumnConfig<PublicationVO, Long>(publicationProperties.maxQuestionAnswerTime(), 20, "Время на вопрос"));

//        l.add(createBooleanColumnConfig(publicationProperties.allowSkipQuestions(), 20, "Можно пропускать"));
//        l.add(createBooleanColumnConfig(publicationProperties.allowInterruptTest(), 20, "Прерывать тест"));
//        l.add(createBooleanColumnConfig(publicationProperties.randomQuestionsOrder(), 20, "Случайный порядок"));
//        
//        l.add(createBooleanColumnConfig(publicationProperties.askFirstName(), 20, "Спрашивать имя"));
//        l.add(createBooleanColumnConfig(publicationProperties.askFirstNameRequired(), 20, "Имя обязательно"));
//        
//        l.add(createBooleanColumnConfig(publicationProperties.askLastName(), 20, "Спрашивать фамилию"));
//        l.add(createBooleanColumnConfig(publicationProperties.askLastNameRequired(), 20, "Фамилия обязательна"));
//        
//        l.add(createBooleanColumnConfig(publicationProperties.askMiddleName(), 20, "Спрашивать отчество"));
//        l.add(createBooleanColumnConfig(publicationProperties.askMiddleNameRequired(), 20, "Отчество обязательно"));
        l.add(createPublicationActionsColumnConfig(new IdentityValueProvider<PublicationVO>(), 40, ""));
        
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
    
//    @UiField
//    TextButton deleteReportsButton;
//
//    @UiField
//    TextButton showReportButton;    

    public PublicationsTab() {
        // Prepare fields for UiBuilder
        top = createTop();
        
//        gridSm = createGridSm();
        gridView = createGridView();
        gridCm = createGridCm();
        gridStore = createGridStore();
        grid = createGrid(gridStore, gridCm);
        
        // Create UI
        initWidget(uiBinder.createAndBindUi(this));
        gridView.groupBy(gridCm.getColumn(0));
//        gridView.setGr
//        gridView.setShowGroupedColumn(false);
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
//        boolean isOneSelected = gridSm.getSelectedItems().size() == 1;
//        boolean isSomeSelected = gridSm.getSelectedItems().size() > 0;
//        showReportButton.setEnabled(isOneSelected);
//        deleteReportsButton.setEnabled(isSomeSelected);
    }
}
