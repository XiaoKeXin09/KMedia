package com.xiaokexin.kxmedia.enums;

/**
 * author:xiaokexin
 * packName:com.xiaokexin.kxmedia.enums
 * Description:
 */
public enum KXTransportModel {
    TRANSPORT_MODEL_NONE("TRANSPORT_MODEL_NONE", 0),
    TRANSPORT_MODEL_UDP("TRANSPORT_MODEL_UDP", 1),
    TRANSPORT_MODEL_TCP("TRANSPORT_MODEL_TCP", 2);

    private String transportModel;
    private int value;

    KXTransportModel(String transportModel, int value)
    {
        this.transportModel = transportModel;
        this.value = value;
    }

    public String getTransportModel() {
        return transportModel;
    }

    public void setTransportModel(String transportModel) {
        this.transportModel = transportModel;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
