package com.attestator.admin.client.ui.widgets;

import com.attestator.admin.client.Admin;
import com.attestator.admin.client.props.UserVOPropertyAccess;
import com.attestator.common.shared.helper.StringHelper;
import com.attestator.common.shared.vo.UserVO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.loader.BeforeLoadEvent;
import com.sencha.gxt.data.shared.loader.BeforeLoadEvent.BeforeLoadHandler;
import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterConfigBean;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfigBean;
import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.form.ComboBox;

public class UserComboBox extends Composite {
    private static final UserVOPropertyAccess USER_PROPERTY_ACESS = GWT.create(UserVOPropertyAccess.class);
    
    private RpcProxy<FilterPagingLoadConfig, PagingLoadResult<UserVO>> rpcProxy = createUsersRpcProxy();
    private RpcProxy<FilterPagingLoadConfig, PagingLoadResult<UserVO>> createUsersRpcProxy() {
        return new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<UserVO>>() {
             @Override
             public void load(FilterPagingLoadConfig loadConfig,
                     AsyncCallback<PagingLoadResult<UserVO>> callback) {                 
                 Admin.RPC.loadUsers(loadConfig, callback);
             }
        };
    }
    
    private ListStore<UserVO> store = createStore(USER_PROPERTY_ACESS.id());
    private ListStore<UserVO> createStore(ModelKeyProvider<UserVO> keyProvider) {
        ListStore<UserVO> result = new ListStore<UserVO>(keyProvider);
        return result;
    }
    
    private PagingLoader<FilterPagingLoadConfig, PagingLoadResult<UserVO>> loader = createLoader(store, rpcProxy);
    private PagingLoader<FilterPagingLoadConfig, PagingLoadResult<UserVO>> createLoader(
            final ListStore<UserVO> store, RpcProxy<FilterPagingLoadConfig, PagingLoadResult<UserVO>> rpcProxy) {        
        PagingLoader<FilterPagingLoadConfig, PagingLoadResult<UserVO>> result = new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<UserVO>>(rpcProxy);        
        result.addBeforeLoadHandler(new BeforeLoadHandler<FilterPagingLoadConfig>() {

            @Override
            public void onBeforeLoad(
                    BeforeLoadEvent<FilterPagingLoadConfig> event) {
                String query = comboBox.getText();
                event.getLoadConfig().getFilters().clear();
                if (!StringHelper.isEmptyOrNull(query)) {
                    FilterConfig usernameFilter = new FilterConfigBean();
                    usernameFilter.setField("username");
                    usernameFilter.setComparison("contains");
                    usernameFilter.setValue(query);
                    event.getLoadConfig().getFilters().add(usernameFilter);
                }
                
                FilterConfig currentUserFilter = new FilterConfigBean();
                currentUserFilter.setField("tenantId");
                currentUserFilter.setComparison("notEq");
                currentUserFilter.setValue(Admin.getLoggedUser().getTenantId());
                event.getLoadConfig().getFilters().add(currentUserFilter);
            }
        });
        
        result.addLoadHandler(new LoadResultListStoreBinding<FilterPagingLoadConfig, UserVO, PagingLoadResult<UserVO>>(store));
        result.useLoadConfig(new FilterPagingLoadConfigBean());
        return result;
    }
    
    private ComboBox<UserVO> comboBox = createComboBox(store);
    private ComboBox<UserVO> createComboBox(ListStore<UserVO> store) {
        ComboBox<UserVO> result = new ComboBox<UserVO>(store, USER_PROPERTY_ACESS.usernameLabel());
        result.setLoader(loader);
        result.setTypeAhead(false);
        result.setMinChars(2);
        result.setPageSize(10);
        result.setWidth(450);
        result.setHideTrigger(true);
        return result;
    }
    
    public ComboBox<UserVO> asComboBox() {
        return comboBox;
    }
    
    public UserComboBox() {
        initWidget(comboBox);
    }
}
