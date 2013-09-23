package com.attestator.admin.client.ui.widgets.tabs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.props.PublicationVOPropertyAccess;
import com.attestator.admin.client.rpc.AdminAsyncCallback;
import com.attestator.admin.client.ui.event.GridGroupClickEvent;
import com.attestator.admin.client.ui.event.GridGroupClickEvent.GridGroupClickHandler;
import com.attestator.admin.client.ui.widgets.BooleanCell;
import com.attestator.admin.client.ui.widgets.ClicableGroupingViewDefaultApperance;
import com.attestator.admin.client.ui.widgets.GroupingViewExt;
import com.attestator.admin.client.ui.widgets.MultylinkCell;
import com.attestator.admin.client.ui.widgets.MultylinkCell.MultyLinikSelectEvent;
import com.attestator.common.shared.helper.ReportHelper;
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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GroupingView.GroupingData;
import com.sencha.gxt.widget.core.client.info.Info;

public class PublicationsTab extends Composite {
    private static final String NEW_PUBLICATION_LINK_ID = "newPublication";
    private static final String EDIT_TEST_LINK_ID = "editTest";
    private static final String DELETE_TEST_LINK_ID = "deleteTest";
    private static final String COPY_TEST_LINK_ID = "copyTest";

    private static final String EDIT_PUBLICATION = "editPublication";
    private static final String COPY_PUBLICATION = "copyPublication";
    private static final String DELETE_PUBLICATION = "deletePublication";
    
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
                    
                    sb.append(createClicableElement(NEW_PUBLICATION_LINK_ID, metatest.getId(), "добавить публикацию"));
                    sb.append(createClicableElement(EDIT_TEST_LINK_ID, metatest.getId(), "изменить"));
                    sb.append(createClicableElement(COPY_TEST_LINK_ID, metatest.getId(), "копировать"));
                    sb.append(createClicableElement(DELETE_TEST_LINK_ID, metatest.getId(), "удалить"));
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
        
        result.setForceFit(true);
        result.setShowGroupedColumn(false);
        result.setEnableGroupingMenu(false);
        result.addGridGroupClickHandler(new GridGroupClickHandler<XElement>() {
            @Override
            public void onClick(GridGroupClickEvent<XElement> event) {
                Info.display("Group click", "ID " + event.getValue().getId());
            }
        });
        
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
        ColumnConfig<PublicationVO, Boolean> result = new ColumnConfig<PublicationVO, Boolean>(valueProvider, width, header);
        result.setCell(new BooleanCell());
        return result;
    }

    private ColumnConfig<PublicationVO, Date> createDateColumnConfig(ValueProvider<PublicationVO, Date> valueProvider, int width, String header) {
        ColumnConfig<PublicationVO, Date> result = new ColumnConfig<PublicationVO, Date>(valueProvider, width, header);
        result.setCell(new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT)));
        return result;
    }

    private ColumnConfig<PublicationVO, PublicationVO> createPublicationActionsColumnConfig(IdentityValueProvider<PublicationVO> valueProvider, int width, String header) {
        ColumnConfig<PublicationVO, PublicationVO> result = new ColumnConfig<PublicationVO, PublicationVO>(valueProvider, width, header);
        
        MultylinkCell<PublicationVO> cell = new MultylinkCell<PublicationVO>() {
            @Override
            public void render(com.google.gwt.cell.client.Cell.Context context,
                    PublicationVO value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<b>Публикация</b>");
                sb.append(createClicableElement(EDIT_PUBLICATION, "изменить"));
                sb.append(createClicableElement(COPY_PUBLICATION, "копировать"));
                sb.append(createClicableElement(DELETE_PUBLICATION, "удалить"));
            }
        };
        cell.addSelectHandler(new SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                @SuppressWarnings("unchecked")
                MultyLinikSelectEvent<PublicationVO> multyLinkSelectEvent = (MultyLinikSelectEvent<PublicationVO>) event;
                Info.display("Publication click", "Type: " + multyLinkSelectEvent.getLinkType() + " Publication: " + multyLinkSelectEvent.getValue().getId());
            }
        });
        result.setCell(new MultylinkCell<PublicationVO>() {
            @Override
            public void render(com.google.gwt.cell.client.Cell.Context context,
                    PublicationVO value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<b>Публикация</b>");
                sb.append(createClicableElement(EDIT_PUBLICATION, "изменить"));
                sb.append(createClicableElement(COPY_PUBLICATION, "копировать"));
                sb.append(createClicableElement(DELETE_PUBLICATION, "удалить"));
            }
        });
        
        return result;
    }
    
    private ColumnConfig<PublicationVO, PublicationVO> createPublicationColumnConfig(IdentityValueProvider<PublicationVO> valueProvider, int width, String header) {
        ColumnConfig<PublicationVO, PublicationVO> result = new ColumnConfig<PublicationVO, PublicationVO>(valueProvider, width, header);
        
        result.setCell(new AbstractCell<PublicationVO>() {
            @Override
            public void render(Context context,
                    PublicationVO value, SafeHtmlBuilder sb) {                                
            }
        });

//        result.setRenderer(new Renderer<PublicationVO>() {            
//            @Override
//            public SafeHtml render(Number value,
//                    Map<ValueProvider<? super PublicationVO, ?>, Number> data) {
//                return SafeHtmlUtils.fromTrustedString("<a href='#b'>Опубликовать</a> <a href='#a'>Изменить</a> <a href='#b'>Копировать</a> <a href='#b'>Удалить</a>");
//            }
//        });
        
        
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
        l.add(createPublicationActionsColumnConfig(new IdentityValueProvider<PublicationVO>(), 60, ""));
//        l.add(createPublicationColumnConfig(new IdentityValueProvider<PublicationVO>(), 40, ""));
//        l.add(new ColumnConfig<PublicationVO, Long>(publicationProperties.reportsCount() , 20, "Отчетов"));
        l.add(createDateColumnConfig(publicationProperties.start(), 30, "Начало"));
        l.add(createDateColumnConfig(publicationProperties.end(), 30, "Конец"));        
//        l.add(new ColumnConfig<PublicationVO, String>(publicationProperties.introduction() , 20, "Ведение"));
        l.add(new ColumnConfig<PublicationVO, Integer>(publicationProperties.maxAttempts() , 20, "Макс. попыток"));

        l.add(new ColumnConfig<PublicationVO, Double>(publicationProperties.minScore(), 20, "Нужно баллов"));
        
//        l.add(createBooleanColumnConfig(publicationProperties.interruptOnFalure(), 20, "Прерывать ли"));
        
        l.add(new ColumnConfig<PublicationVO, Long>(publicationProperties.maxTakeTestTime(), 20, "Время на тест"));
//        l.add(new ColumnConfig<PublicationVO, Long>(publicationProperties.maxQuestionAnswerTime(), 20, "Время на вопрос"));

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
