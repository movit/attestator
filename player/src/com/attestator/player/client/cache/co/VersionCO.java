package com.attestator.player.client.cache.co;

import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

public class VersionCO implements JsonSerializable {
    private int version = 0;
    
    public VersionCO() {
    }

    public VersionCO(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
