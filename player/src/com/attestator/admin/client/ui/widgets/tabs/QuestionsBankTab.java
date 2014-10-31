package com.attestator.admin.client.ui.widgets.tabs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.props.QuestionVOPropertyAccess;
import com.attestator.admin.client.rpc.AdminAsyncCallback;
import com.attestator.admin.client.ui.EditMode;
import com.attestator.admin.client.ui.GroupsWindow;
import com.attestator.admin.client.ui.event.FilterEvent;
import com.attestator.admin.client.ui.event.FilterEvent.FilterHandler;
import com.attestator.admin.client.ui.event.MultyLinikSelectEvent;
import com.attestator.admin.client.ui.event.MultyLinikSelectEvent.MultyLinikSelectHandler;
import com.attestator.admin.client.ui.event.SaveEvent;
import com.attestator.admin.client.ui.event.SaveEvent.SaveHandler;
import com.attestator.admin.client.ui.question.SCQWindow;
import com.attestator.admin.client.ui.widgets.ButtonFileUpload;
import com.attestator.admin.client.ui.widgets.ButtonFileUpload.FileUploadFieldMessages;
import com.attestator.admin.client.ui.widgets.MultylinkCell;
import com.attestator.admin.client.ui.widgets.SearchField;
import com.attestator.common.client.helper.WindowHelper;
import com.attestator.common.client.ui.resolurces.Resources;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.helper.VOHelper;
import com.attestator.common.shared.vo.GroupVO;
import com.attestator.common.shared.vo.QuestionVO;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterConfigBean;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfigBean;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent.RowDoubleClickHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SubmitCompleteEvent;
import com.sencha.gxt.widget.core.client.event.SubmitCompleteEvent.SubmitCompleteHandler;
import com.sencha.gxt.widget.core.client.form.FormPanel;
import com.sencha.gxt.widget.core.client.grid.CheckBoxSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.menu.SeparatorMenuItem;
import com.sencha.gxt.widget.core.client.toolbar.PagingToolBar;

public class QuestionsBankTab extends Tab {
    private static final String EDIT_TEST_LINK_ID = "editTest";
    private static final String DELETE_TEST_LINK_ID = "deleteTest";
    private static final String COPY_TEST_LINK_ID = "copyTest";

    
    interface UiBinderImpl extends UiBinder<Widget, QuestionsBankTab> {
    }
    private static UiBinderImpl uiBinder = GWT.create(UiBinderImpl.class);

    @UiField(provided = true)
    Grid<QuestionVO> questionsBankGrid;

    Grid<QuestionVO> createQuestionsBankGrid(
            final PagingLoader<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>> loader,
            final ListStore<QuestionVO> store, ColumnModel<QuestionVO> cm) {
        
        Grid<QuestionVO> result = new Grid<QuestionVO>(store, cm);        
        result.setLoader(loader);
        result.addRowDoubleClickHandler(new RowDoubleClickHandler() {
            @Override
            public void onRowDoubleClick(RowDoubleClickEvent event) {
                showQuestionWindow(store.get(event.getRowIndex()), EditMode.etExisting);
            }
        });
        return result;
    }

    @UiField(provided = true)
    SearchField questionSearchField;

    SearchField createQuestionSearchField() {
        return new SearchField();
    }

    @UiField
    PagingToolBar questionsBankPager;

    @UiField(provided = true)
    ListStore<QuestionVO> questionsBankStore;

    ListStore<QuestionVO> createQuestionsBankStore() {
        ListStore<QuestionVO> result = new ListStore<QuestionVO>(
                new ModelKeyProvider<QuestionVO>() {
                    @Override
                    public String getKey(QuestionVO item) {
                        return item.getId();
                    }
                });
        return result;
    }

    @UiField(provided = true)
    CheckBoxSelectionModel<QuestionVO> questionsBankSm;

    CheckBoxSelectionModel<QuestionVO> createQuestionsBankSm() {
        IdentityValueProvider<QuestionVO> identity = new IdentityValueProvider<QuestionVO>();
        CheckBoxSelectionModel<QuestionVO> result = new CheckBoxSelectionModel<QuestionVO>(
                identity) {
            @Override
            protected void onSelectChange(QuestionVO model,
                    boolean select) {
                super.onSelectChange(model, select);
                enableButtons();
            }
        };

        return result;
    }

    @UiField(provided = true)
    ColumnModel<QuestionVO> questionsBankCm;

    ColumnModel<QuestionVO> createQuestionsBankCm(
            CheckBoxSelectionModel<QuestionVO> sm) {
        QuestionVOPropertyAccess props = GWT
                .create(QuestionVOPropertyAccess.class);

        ColumnConfig<QuestionVO, String> textColumn = new ColumnConfig<QuestionVO, String>(
                props.text(), 300, "Вопрос");
        textColumn.setCell(new AbstractCell<String>() {
            @Override
            public void render(Context context, String value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<div style=\"white-space:normal !important;\">"
                        + value + "</div>");
            }
        });
        
        ColumnConfig<QuestionVO, String> ownerUsernameColumn = new ColumnConfig<QuestionVO, String>(
                props.ownerUsername(), 50, "Автор");
        

        ColumnConfig<QuestionVO, String> groupNameColumn = new ColumnConfig<QuestionVO, String>(
                props.groupName(), 50, "Группа");
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

        ColumnConfig<QuestionVO, QuestionVO> multilinkColumn = new ColumnConfig<QuestionVO, QuestionVO>(new IdentityValueProvider<QuestionVO>());        
        MultylinkCell<QuestionVO> cell = new MultylinkCell<QuestionVO>() {
            @Override
            public void render(Context context,
                    QuestionVO value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<div style='text-align: right;'>");
                sb.append(createClickableElement(EDIT_TEST_LINK_ID, "редактировать вопрос", Resources.ICONS.edit16x16()));
                sb.append(createClickableElement(COPY_TEST_LINK_ID, "копировать вопрос", Resources.ICONS.copy16x16()));
                sb.append(createClickableElement(DELETE_TEST_LINK_ID, "удалить вопрос", Resources.ICONS.delete16x16()));
                sb.appendHtmlConstant("</div>");
            }
        };        
        cell.addMultyLinikSelectHandler(new MultyLinikSelectHandler<QuestionVO>() {
            @Override
            public void onSelect(MultyLinikSelectEvent<QuestionVO> event) {                
                if (EDIT_TEST_LINK_ID.equals(event.getLinkType())) {
                    showQuestionWindow(((QuestionVO)event.getValue()), EditMode.etExisting);
                }                
                else if (COPY_TEST_LINK_ID.equals(event.getLinkType())) {
                    showQuestionWindow(((QuestionVO)event.getValue()), EditMode.etCopy);
                }
                else if (DELETE_TEST_LINK_ID.equals(event.getLinkType())) {
                    QuestionVO question = (QuestionVO) event.getValue();
                    Admin.RPC.deleteQuestions(Arrays.asList(question.getId()), new AdminAsyncCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            refreshGrid();
                        }
                    });
                }
            }
        });
        multilinkColumn.setCell(cell);
        multilinkColumn.setHideable(false);
        multilinkColumn.setResizable(false);
        multilinkColumn.setWidth(90);
        multilinkColumn.setFixed(true);
        multilinkColumn.setMenuDisabled(true);
        multilinkColumn.setSortable(false);
        
        List<ColumnConfig<QuestionVO, ?>> l = new ArrayList<ColumnConfig<QuestionVO, ?>>();
        l.add(sm.getColumn());
        l.add(textColumn);
        l.add(groupNameColumn);
        l.add(ownerUsernameColumn);
        l.add(multilinkColumn);
        
        ColumnModel<QuestionVO> result = new ColumnModel<QuestionVO>(l);

        return result;
    }

    PagingLoader<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>> questionsBankLoader;

    PagingLoader<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>> createQuestionsBankLoader(
            final ListStore<QuestionVO> store, final SearchField searchField) {
        RpcProxy<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>> rpcProxy = new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>>() {
            @Override
            public void load(FilterPagingLoadConfig loadConfig,
                    AsyncCallback<PagingLoadResult<QuestionVO>> callback) {
                Admin.RPC.loadQuestions(loadConfig, callback);
            }
        };

        PagingLoader<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>> result = new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>>(
                rpcProxy) {
            @Override
            protected FilterPagingLoadConfig newLoadConfig() {
                FilterPagingLoadConfig result = new FilterPagingLoadConfigBean();

                if (StringHelper.isNotEmptyOrNull(questionBankFilterValue)) {
                    String filterValue = questionBankFilterValue;
                    filterValue = filterValue.trim();
                    filterValue = filterValue.toLowerCase();
                    filterValue = filterValue.replaceAll("\\s+", " ");

                    FilterConfig textFilter = new FilterConfigBean();
                    textFilter.setComparison("contains");
                    textFilter.setField("text or groupName");
                    textFilter.setType("string");
                    textFilter.setValue(filterValue);

                    result.getFilters().add(textFilter);
                }

                return result;
            }
        };
        result.setRemoteSort(true);
        result.addLoadHandler(new LoadResultListStoreBinding<FilterPagingLoadConfig, QuestionVO, PagingLoadResult<QuestionVO>>(
                store) {
            @Override
            public void onLoad(LoadEvent<FilterPagingLoadConfig, PagingLoadResult<QuestionVO>> event) {
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
    ButtonFileUpload.FileUploadFieldMessages uploadQusetionsMessages = new FileUploadFieldMessages() {
        @Override
        public String browserText() {
            return "Загрузить...";
        }
    };
    
    @UiField
    protected TextButton editQuestionButton;
    @UiField
    protected TextButton deleteQuestionsButton;
    @UiField
    protected TextButton exportQuestionsButton;
    
    @UiField 
    protected Menu saveMenu;

    @UiField
    protected SeparatorMenuItem saveGroupsMenuSeparator;
    @UiField
    protected MenuItem saveAllMenuItem;
    @UiField
    protected MenuItem saveSelectedMenuItem;
    @UiField
    protected TextButton setGroupButton;
    @UiField 
    protected Menu setGroupMenu;
    @UiField
    protected SeparatorMenuItem setGroupMenuSeparator;
    @UiField
    protected MenuItem setGroupEditGroupsMenuItem;    
    @UiField 
    ButtonFileUpload uploadQuestionsField;
    @UiField 
    FormPanel uploadQuestionsForm;
    
    private String questionBankFilterValue;    
    
    public QuestionsBankTab() {
        // Prepare fields for UiBuilder
        top = createTop();
        
        questionSearchField = createQuestionSearchField();
        questionsBankSm = createQuestionsBankSm();
        questionsBankCm = createQuestionsBankCm(questionsBankSm);
        questionsBankStore = createQuestionsBankStore();
        questionsBankLoader = createQuestionsBankLoader(questionsBankStore,
                questionSearchField);
        questionsBankGrid = createQuestionsBankGrid(questionsBankLoader,
                questionsBankStore, questionsBankCm);

        // Create UI
        initWidget(uiBinder.createAndBindUi(this));
        
        // Use created by UiBuilder fields to finish configuration
        questionsBankPager.bind(questionsBankLoader);
        questionSearchField.addFilterChangeHandler(new FilterHandler<String>() {
            @Override
            public void onFilter(FilterEvent<String> event) {
                questionBankFilterValue = event.getValue();
                questionsBankPager.first();
            }
        });
        
        uploadQuestionsForm.addSubmitCompleteHandler(new SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
                refresh();                
            }
        });
        
        uploadQuestionsField.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        uploadQuestionsForm.submit();
                        uploadQuestionsField.setValue("");
                    }
                });
            }
        });
        
        addSelectionHandler(new SelectionHandler<Tab>() {            
            @Override
            public void onSelection(SelectionEvent<Tab> event) {
                refresh();                
            }
        });
    }
    
    private SaveHandler<QuestionVO> saveQuestionHandler = new SaveHandler<QuestionVO>() {
        @Override
        public void onSave(SaveEvent<QuestionVO> event) {
            refreshGrid();
        }
    };
    
    private HideHandler hideQuestionHandler = new HideHandler() {
        @Override
        public void onHide(HideEvent event) {
            refreshGroups();
        }
    };
    
    private void showQuestionWindow(QuestionVO question, EditMode editMode) {
        SCQWindow.showWindow(editMode, question != null ? question.getId() : null, saveQuestionHandler, hideQuestionHandler);
    }
    
    @UiHandler("newQuestion")
    public void newQuestionClick(SelectEvent event) {
        showQuestionWindow(null, EditMode.etNew);
    }
    
    @UiHandler("editQuestionButton") 
    public void editQuestionButtonClick(SelectEvent event) {
        if (questionsBankSm.getSelectedItems().size() == 1) {
            showQuestionWindow(questionsBankSm.getSelectedItem(), EditMode.etExisting);
        }
    }
    @UiHandler("deleteQuestionsButton") 
    public void deleteQuestionsButtonClick(SelectEvent event) {
        if (questionsBankSm.getSelectedItems().size() > 0) {
            List<String> questionIds = VOHelper.getIds(questionsBankSm.getSelectedItems());
            Admin.RPC.deleteQuestions(questionIds, new AdminAsyncCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    refreshGrid();
                }
            });
        }
    }
    
    @UiHandler("setGroupEditGroupsMenuItem") 
    public void setGroupEditGroupsMenuItemClick(SelectionEvent<Item> event) {
        GroupsWindow window = new GroupsWindow();
        window.addSaveHandler(new SaveHandler<List<GroupVO>>() {
            @Override
            public void onSave(SaveEvent<List<GroupVO>> event) {
                refresh();                
            }
        });
        window.show();
    }
    
    private void refresh() {
        refreshGrid(); //enableButtons() called after grid refresh is finished to reflect new selected state
        refreshGroups();
    }
    
    private void refreshGrid() {
        questionsBankLoader.load();
    }
    
    private void refreshGroups() {
        Admin.RPC.loadOwnGroups(new AdminAsyncCallback<List<GroupVO>>() {

            @Override
            public void onSuccess(List<GroupVO> result) {
                // Remove all group related save menu items
                Iterator<Widget> iterator = saveMenu.iterator();                
                while (iterator.hasNext()) {
                    Component menuItem = (Component)iterator.next();
                    if (menuItem == saveAllMenuItem
                    ||  menuItem == saveSelectedMenuItem
                    ||  menuItem == saveGroupsMenuSeparator) {
                        continue;
                    }
                    
                    iterator.remove();
                }

                // Remove all group related set group menu items
                iterator = setGroupMenu.iterator();                
                while (iterator.hasNext()) {
                    Component menuItem = (Component)iterator.next();
                    if (menuItem == setGroupEditGroupsMenuItem
                    ||  menuItem == setGroupMenuSeparator) {
                        continue;
                    }
                    
                    iterator.remove();
                }
                
                int setGroupsSeparatorIndex = setGroupMenu.getWidgetIndex(setGroupMenuSeparator);
                for (GroupVO group: result) {
                    final String groupId = group.getId();
                    
                    // Add set menu item
                    MenuItem setItem = new MenuItem();
                    setItem.setText(group.getName());
                    setItem.addSelectionHandler(new SelectionHandler<Item>() {                        
                        @Override
                        public void onSelection(SelectionEvent<Item> event) {
                            if (questionsBankSm.getSelectedItems().size() > 0) {
                                List<String> questionIds = VOHelper.getIds(questionsBankSm.getSelectedItems());
                                Admin.RPC.setQuestionsGroup(questionIds, groupId, new AdminAsyncCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        refreshGrid();
                                    }
                                });
                            }                            
                        }
                    });
                    setGroupMenu.insert(setItem, setGroupsSeparatorIndex++);
                    
                    // Add save menu item
                    MenuItem saveItem = new MenuItem();
                    saveItem.setText(group.getName());
                    saveMenu.add(saveItem);
                    
                    // Adjust menus width
                    //TODO bug here adjust menu width correctly
//                    if (setGroupMenu.getElement().getWidth(true) > 700) {
//                        setGroupMenu.setWidth(700);
//                    }
//                    if (saveMenu.getElement().getWidth(true) > 700) {
//                        saveMenu.setWidth(700);
//                    }
                }
            }
        });
    }
    
    @UiHandler("exportQuestionsButton")
    protected void exportQuestionsClick(SelectEvent event) {
        String url = "admin/exportquestions";
        WindowHelper.downloadFile(url);
    }

    private void enableButtons() {
        boolean isOneSelected = questionsBankSm.getSelectedItems().size() == 1;
        boolean isSomeSelected = questionsBankSm.getSelectedItems().size() > 0;
        editQuestionButton.setEnabled(isOneSelected);
        deleteQuestionsButton.setEnabled(isSomeSelected);
        saveSelectedMenuItem.setEnabled(isSomeSelected);
        setGroupButton.setEnabled(isSomeSelected);
    }
    
}
