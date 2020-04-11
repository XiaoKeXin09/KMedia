package com.xiaokexin.kxmedia.listener;

/**
 * author:xiaokexin
 * packName:com.xiaokexin.kxmedia.listener
 * Description: surface 初始化完成回调
 */
public interface KXOnVideoViewListener {

    void initSuccess();

    void moveSlide(double value);

    void moveFinish(double value);

}
