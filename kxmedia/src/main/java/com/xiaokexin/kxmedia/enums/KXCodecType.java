package com.xiaokexin.kxmedia.enums;

/**
 * author:xiaokexin
 * packName:com.xiaokexin.kxmedia.enums
 * Description:
 */
public enum KXCodecType {

    CODEC_SOFT("SOFT", 0), // 只是软解码
    CODEC_MEDIACODEC("MEDIACODEC", 1); // 硬解码优先

    private String type;
    private int value;

    KXCodecType(String type, int value)
    {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
