package com.bigbass.nep.recipes.delivery;

import javax.json.JsonObject;

public class PeerConfig {
    public String alias, url, md5Url, unpackKernel, importKernel;

    public PeerConfig(String alias, String url, String md5Url, String unpackKernel, String importKernel) {
        this.alias = alias;
        this.url = url;
        this.md5Url = md5Url;
        this.unpackKernel = unpackKernel;
        this.importKernel = importKernel;
    }

    public static PeerConfig fromJson(JsonObject json) {
        return new PeerConfig(
                json.getString("alias", null),
                json.getString("url", null),
                json.getString("md5Url", null),
                json.getString("unpackKernel", null),
                json.getString("importKernel", null)
        );
    }
}
