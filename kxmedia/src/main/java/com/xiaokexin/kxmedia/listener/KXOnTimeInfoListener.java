package com.xiaokexin.kxmedia.listener;

/**
 * author:xiaokexin
 * packName:com.xiaokexin.kxmedia.listener
 * Description: 播放时间回调
 */
public interface KXOnTimeInfoListener {
    /**
     * 音视频播放时长
     * @param currentTime
     */
    void onTimeInfo(double currentTime);

}
