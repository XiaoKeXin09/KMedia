package com.xiaokexin.kxmedia.enums;

/**
 * author:xiaokexin
 * packName:com.xiaokexin.kxmedia.enums
 * Description:
 */
public enum KXSampleRate {
    SAMPLE_RATE_8000("SAMPLE_RATE_8000", 8000),
    SAMPLE_RATE_11025("SAMPLE_RATE_11025", 11025),
    SAMPLE_RATE_12000("SAMPLE_RATE_12000", 12000),
    SAMPLE_RATE_16000("SAMPLE_RATE_16000", 16000),
    SAMPLE_RATE_22050("SAMPLE_RATE_22050", 22050),
    SAMPLE_RATE_24000("SAMPLE_RATE_24000", 24000),
    SAMPLE_RATE_32000("SAMPLE_RATE_32000", 32000),
    SAMPLE_RATE_44100("SAMPLE_RATE_44100", 44100),
    SAMPLE_RATE_48000("SAMPLE_RATE_48000", 48000);

    private String samplerate;
    private int value;

    KXSampleRate(String samplerate, int value)
    {
        this.samplerate = samplerate;
        this.value = value;
    }

    public String getSamplerate() {
        return samplerate;
    }

    public void setSamplerate(String samplerate) {
        this.samplerate = samplerate;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
