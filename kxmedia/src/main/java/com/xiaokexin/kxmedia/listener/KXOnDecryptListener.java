package com.xiaokexin.kxmedia.listener;

/**
 * author:xiaokexin
 * packName:com.xiaokexin.kxmedia.listener
 * Description: 解密算法回调
 */
public interface KXOnDecryptListener {

    byte[] decrypt(byte[] encrypt_data);

}
