package com.xiaokexin.kxmedia.log;

import android.text.TextUtils;
import android.util.Log;

/**
 * author:xiaokexin
 * packName:com.xiaokexin.kxmedia.log
 * Description:
 */
public class KXLog {

    private static final String TAG = "ywl5320";
    private static boolean debug = true;

    public static void setDebug(boolean debug) {
        KXLog.debug = debug;
    }

    public static void d(String msg)
    {
        if(!debug)
        {
            return;
        }
        if(!TextUtils.isEmpty(msg))
        {
            Log.d(TAG, msg);
        }
    }

    public static void e(String msg)
    {
        if(!debug)
        {
            return;
        }
        if(!TextUtils.isEmpty(msg))
        {
            Log.e(TAG, msg);
        }
    }
}
