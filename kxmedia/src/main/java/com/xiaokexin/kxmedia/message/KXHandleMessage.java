package com.xiaokexin.kxmedia.message;

/**
 * author:xiaokexin
 * packName:com.xiaokexin.kxmedia.message
 * Description:
 */
public final class KXHandleMessage {

    private static final int DEFAULT_CODE = 0X1001;
    public static final int WLMSG_START_PREPARED = DEFAULT_CODE + 1;//开始异步准备
    public static final int WLMSG_START_STOP = DEFAULT_CODE + 2;//异步停止
    public static final int WLMSG_START_RELEASE_COMPLETE = DEFAULT_CODE + 3;//停止后回收资源完成
    public static final int WLMSG_START_ERROR = DEFAULT_CODE + 4;//出错
    public static final int WLMSG_START_RELEASE = DEFAULT_CODE + 5;//停止后开始回收资源
    public static final int WLMSG_START_PREPARED_OK = DEFAULT_CODE + 6;//准备好了，可以播放了
    public static final int WLMSG_START_PLAY = DEFAULT_CODE + 7;//开始播放
    public static final int WLMSG_START_CHANGE_AUDIO_TRACK = DEFAULT_CODE + 8;//切换音轨
    public static final int WLMSG_START_PLAY_PAUSE = DEFAULT_CODE + 9;//暂停
    public static final int WLMSG_START_PLAY_RESUME = DEFAULT_CODE + 10;//播放
    public static final int WLMSG_START_AUDIO_MUTE = DEFAULT_CODE + 11;//声道选择
    public static final int WLMSG_START_AUDIO_VOLUME = DEFAULT_CODE + 12;//音量
    public static final int WLMSG_START_AUDIO_SPEED = DEFAULT_CODE + 13;//播放速度
    public static final int WLMSG_START_AUDIO_PITCH = DEFAULT_CODE + 14;//播放音调
    public static final int WLMSG_START_SEEK = DEFAULT_CODE + 15;//seek
    public static final int WLMSG_START_PLAY_TIME = DEFAULT_CODE + 16;//时间回调
    public static final int WLMSG_START_PLAY_LOAD = DEFAULT_CODE + 17;//加载状态
    public static final int WLMSG_START_AUDIO_INFO = DEFAULT_CODE + 18;//pcm属性
    public static final int WLMSG_START_PLAY_NEXT = DEFAULT_CODE + 19;//切换
    public static final int WLMSG_TAKE_PICTURE = DEFAULT_CODE + 20;//截屏
    public static final int WLMSG_TAKE_PICTURE_BITMAP = DEFAULT_CODE + 21;//截屏回调
    public static final int WLMSG_RELEASE_SURFACE = DEFAULT_CODE + 22;//回收surface
}
