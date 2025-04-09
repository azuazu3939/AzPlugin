package com.github.azuazu3939.azPlugin.lib.holder;

public class RegisterAzHolder extends EmptyAzHolder {

    private final String shopId;

    public RegisterAzHolder(int row, String name, String shopId) {
        super(row, name);
        this.shopId = shopId;
    }

    public String getShopId() {
        return shopId;
    }
}
