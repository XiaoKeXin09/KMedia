package com.xiaokexin.kxmedia;

import android.graphics.Bitmap;
import android.text.TextUtils;

/**
 * author:xiaokexin
 * packName:com.xiaokexin.kxmedia
 * Description:
 */
public class KXMediaUtil {

    static {
        System.loadLibrary("avutil-56");
        System.loadLibrary("swresample-3");
        System.loadLibrary("avcodec-58");
        System.loadLibrary("avformat-58");
        System.loadLibrary("swscale-5");
        System.loadLibrary("wlmediautil-1.0.1");
    }

    private static KXMediaUtil instance;

    public KXMediaUtil(){}

    public static KXMediaUtil getInstance() {
        if(instance == null)
        {
            synchronized (KXMediaUtil.class)
            {
                if(instance == null)
                {
                    instance = new KXMediaUtil();
                }
            }
        }

        return instance;
    }

    /**
     * 获取指定时间或者指定位置关键帧图片
     * @param url 数据源地址
     * @param indexOrTime 时间 或者 关键帧位置
     * @param keyframe true:按关键帧获取 false:按时间获取
     * @return
     */
    public Bitmap getVideoPic(String url, double indexOrTime, boolean keyframe)
    {
        if(TextUtils.isEmpty(url))
        {
            return null;
        }
        if(indexOrTime < 0)
        {
            indexOrTime = 0;
        }
        return n_getVideoPicture(url, indexOrTime, keyframe);
    }

    private native Bitmap n_getVideoPicture(String url, double indexOrTime, boolean keyframe);


}
