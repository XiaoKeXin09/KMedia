package com.xiaokexin.kxmedia.enums;

/**
 * author:xiaokexin
 * packName:com.xiaokexin.kxmedia.enums
 * Description:
 */
public enum KXPlayModel {
    PLAYMODEL_AUDIO_VIDEO("PLAYMODEL_AUDIO_VIDEO", 0),
    PLAYMODEL_ONLY_AUDIO("PLAYMODEL_ONLY_AUDIO", 1),
    PLAYMODEL_ONLY_VIDEO("PLAYMODEL_ONLY_VIDEO", 2);

    private String playModel;
    private int value;

    KXPlayModel(String playModel, int value)
    {
        this.playModel = playModel;
        this.value = value;
    }

    public String getPlayModel() {
        return playModel;
    }

    public void setPlayModel(String playModel) {
        this.playModel = playModel;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}
