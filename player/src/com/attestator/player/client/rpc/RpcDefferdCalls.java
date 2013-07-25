package com.attestator.player.client.rpc;

import java.util.ArrayList;
import java.util.List;

import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

public class RpcDefferdCalls implements JsonSerializable{
    private List<RpcCallDescriptor> calls = new ArrayList<RpcCallDescriptor>();

    public List<RpcCallDescriptor> getCalls() {
        return calls;
    }

    public void setCalls(List<RpcCallDescriptor> calls) {
        this.calls = calls;
    }
}
