package com.xiaokexin.kxmedia.util;

/**
 * author:xiaokexin
 * packName:com.xiaokexin.kxmedia.util
 * Description:
 */
public class KXTimeUtil {

    static StringBuilder time = new StringBuilder();
    /**
     * format times
     * @param secds
     * @return
     */
    public static String secdsToDateFormat(int secds) {

        if(secds < 0)
        {
            secds = 0;
        }

        if(time.length() > 0)
        {
            time.delete(0, time.length());
        }

        long hours = secds / (60 * 60);
        long minutes = (secds % (60 * 60)) / (60);
        long seconds = secds % (60);

        if(hours > 0)
        {
            time.append((hours >= 10) ? hours : "0" + hours);
            time.append(":");
        }
        time.append((minutes >= 10) ? minutes : "0" + minutes);
        time.append(":");
        time.append((seconds >= 10) ? seconds : "0" + seconds);
        return time.toString();
    }
}
