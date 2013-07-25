package com.attestator.player.client.rpc;

import java.util.ArrayList;
import java.util.List;

import com.attestator.common.shared.vo.BaseVO;

public class RpcCallDescriptor extends BaseVO {
    private static final long serialVersionUID = 6410939068324133127L;

    private String method;
    
    private List<String> arguments = new ArrayList<String>();

    public RpcCallDescriptor() {
        super();
    }    

    public RpcCallDescriptor(String method, String ... arguments){
        this.method = method;
        for (String argument: arguments) {
            this.arguments.add(argument);
        }
    }
    
    public String getMethod() {
        return method;
    }
    
    public List<String> getArguments() {
        return arguments;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RpcCallDescriptor other = (RpcCallDescriptor) obj;
        if (getId() == null) {
            if (other.getId() != null)
                return false;
        } else if (!getId().equals(other.getId()))
            return false;
        return true;
    }
    
}