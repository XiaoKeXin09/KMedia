package com.xiaokexin.kxmedia.bean;

/**
 * author:xiaokexin
 * packName:com.xiaokexin.kxmedia.bean
 * Description:
 */
public class KXErrorBean {
    private int code;
    private String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "WlErrorBean{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
