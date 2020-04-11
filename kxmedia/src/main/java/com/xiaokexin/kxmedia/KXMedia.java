package com.xiaokexin.kxmedia;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Surface;

import com.xiaokexin.kxmedia.bean.KXErrorBean;
import com.xiaokexin.kxmedia.bean.KXPcmInfoBean;
import com.xiaokexin.kxmedia.enums.KXCodecType;
import com.xiaokexin.kxmedia.enums.KXMute;
import com.xiaokexin.kxmedia.enums.KXPlayModel;
import com.xiaokexin.kxmedia.enums.KXSampleRate;
import com.xiaokexin.kxmedia.enums.KXTransportModel;
import com.xiaokexin.kxmedia.listener.KXOnCompleteListener;
import com.xiaokexin.kxmedia.listener.KXOnDecryptListener;
import com.xiaokexin.kxmedia.listener.KXOnErrorListener;
import com.xiaokexin.kxmedia.listener.KXOnLoadListener;
import com.xiaokexin.kxmedia.listener.KXOnPcmDataListener;
import com.xiaokexin.kxmedia.listener.KXOnPreparedListener;
import com.xiaokexin.kxmedia.listener.KXOnTakePictureListener;
import com.xiaokexin.kxmedia.listener.KXOnTimeInfoListener;
import com.xiaokexin.kxmedia.log.KXLog;
import com.xiaokexin.kxmedia.message.KXHandleMessage;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * author:xiaolkexin
 * packName:com.xiaokexin.kxmedia
 * Description:
 */
public class KXMedia {

    /**
     * loading media .so library
     */
    static {
        System.loadLibrary("avutil-56");
        System.loadLibrary("swresample-3");
        System.loadLibrary("avcodec-58");
        System.loadLibrary("avformat-58");
        System.loadLibrary("swscale-5");
        System.loadLibrary("wlmedia-1.0.7");
    }


    /**
     * 对象hashcode 用于多实例区分
     */
    private int hashcode = -1;

    //----------------------------------------------fields--------------------------------------------------------------------

    private MediaHancler handler;
    private static final int DEFAULT_CONNECTION_TIMES = 5;
    private boolean isPlay = false;//全局播放状态
    private boolean isNext = false;//是否切换
    private boolean iseof = true;//播放完成时是否是 end of file，不是的话则进行重连
    private double nowPts = 0;//记录当前pts，用于重连时定位
    private double duration = 0;//时长
    private Surface surface;//显示video
    private int surfaceWidth = 0;//
    private int surfaceHeight = 0;//
    private String vShader;//预留opengl顶点shader
    private String fShader;//预留opengl着色器shader
    private int playModel = KXPlayModel.PLAYMODEL_AUDIO_VIDEO.getValue();//播放模式(音频 视频 音视频)
    private int codecType = KXCodecType.CODEC_MEDIACODEC.getValue();//默认硬解码
    private int transportModel = KXTransportModel.TRANSPORT_MODEL_NONE.getValue();//rtsp播放模式（udp/tcp）
    private boolean bufferSource = false;
    private boolean encryptFileSource = false;
    private boolean clearLastPicture = true;


    /**
     * the url source address
     */
    private String source;

    private KXErrorBean errorBean;

    /**
     * （选择）音轨语言
     */
    private String[] audioLanguage = null;

    /**
     * 设置返回的PCM采样率，方便和其他音频做处理
     */
    private int sampleRate = -1;

    /**
     * 声道
     */
    private int mute = KXMute.MUTE_CENTER.getValue();

    /**
     * 播放音量 （0 ~ 100 %）
     */
    private int volume = 100;//音量

    /**
     * 改变音量是否改变数据源PCM
     */
    private boolean volume_change_pcm = false;

    /**
     * 播放速度 （0.5f ~ 2.0f）
     */
    private float speed = 1.0f;

    /**
     * 播放音调 （0.5f ~ 2.0f）
     */
    private float pitch = 1.0f;

    private KXPcmInfoBean kxPcmInfoBean;

    private boolean showPcm = false;

    private int scale_w = 0;
    private int scale_h = 0;



    private KXOnErrorListener onErrorListener;
    private KXOnCompleteListener onCompleteListener;
    private KXOnPreparedListener onPreparedListener;
    private KXOnTimeInfoListener onTimeInfoListener;
    private KXOnLoadListener onLoadListener;
    private KXOnPcmDataListener onPcmDataListener;
    private KXOnTakePictureListener onTakePictureListener;
    private KXOnDecryptListener onDecryptListener;

    //----------------------------------------------method--------------------------------------------------------------------

    /**
     * 公共静态构造函数（后期根据反馈可能会改成private）
     */
    public KXMedia()
    {
        hashcode = this.hashCode();
        handler = new MediaHancler(this);
    }

    /**
     * 设置数据源
     * @param source
     */
    public void setSource(String source)
    {
        if(TextUtils.isEmpty(source))
        {
            this.source = "";
            return;
        }
        this.source = source;
    }

    /**
     * 异步播放准备 完成后会回调preparedlistener
     */
    public void prepared()
    {
        isNext = false;
        if((!bufferSource) || (bufferSource && encryptFileSource))
        {
            if(TextUtils.isEmpty(source))
            {
                KXLog.e("source is null");
                onError(-1, "the source is empty !");
                return;
            }
        }
        if((playModel == KXPlayModel.PLAYMODEL_AUDIO_VIDEO.getValue() || playModel == KXPlayModel.PLAYMODEL_ONLY_VIDEO.getValue())
                &&
                (surface == null || surfaceWidth <= 0 || surfaceHeight <= 0))
        {
            KXLog.e("play with video but the surface not init!");
            onError(-2, "play with video but the surface not init!");
            return;
        }
        if(isPlay)
        {
            KXLog.e("the player is already play");
            onError(-3, "the player is already play !");
            return;
        }
        isPlay = true;
        audioLanguage = null;
        handler.sendEmptyMessage(KXHandleMessage.WLMSG_START_PREPARED);
    }

    /**
     * 异步准备好后，开始播放
     */
    public void start()
    {
        handler.sendEmptyMessage(KXHandleMessage.WLMSG_START_PLAY);
    }

    /**
     * 停止 播放视频是只会回收播放器资源 不会回收surface资源 所以还需要再complete回调里面调用release回收surface资源
     */
    public void stop()
    {
        if(!isPlay)
        {
            KXLog.d("the player is not in play");
            onError(-4, "the player is not in play !");
            return;
        }
        handler.sendEmptyMessage(KXHandleMessage.WLMSG_START_STOP);
    }

    /**
     * 暂停
     */
    public void pause()
    {
        handler.sendEmptyMessage(KXHandleMessage.WLMSG_START_PLAY_PAUSE);
    }

    /**
     * 播放（对应暂停）
     */
    public void resume()
    {
        handler.sendEmptyMessage(KXHandleMessage.WLMSG_START_PLAY_RESUME);
    }

    /**
     * 播放器是否在播放中
     * @return
     */
    public boolean isPlay() {
        return isPlay;
    }

    /**
     * 得到音轨对应的语言
     *
     * @return 返回值为音轨语言数组，对应音轨索引为数组索引 如：
     *          返回值["eng", "zho"]
     *          切换为"eng"对应音轨为：setAudioChannel(0)
     *          切换为"zho"对应音轨为：setAudioChannel(1)
     */
    public String[] getAudioChannels()
    {
        if(audioLanguage == null)
        {
            audioLanguage = n_getAudioChannels(hashcode);
        }
        return audioLanguage;
    }


    /**
     * 设置音轨
     * 得到所有音轨{@link #getAudioChannels()}
     * @param index
     */
    public void setAudioChannel(int index)
    {
        Message message = Message.obtain();
        message.arg1 = index;
        message.what = KXHandleMessage.WLMSG_START_CHANGE_AUDIO_TRACK;
        handler.sendMessage(message);
    }

    /**
     * 设置声道
     * @param mute
     */
    public void setMute(KXMute mute)
    {
        this.mute = mute.getValue();
        handler.sendEmptyMessage(KXHandleMessage.WLMSG_START_AUDIO_MUTE);
    }

    /**
     * 设置音量（0~100）
     * @param percent
     */
    public void setVolume(int percent)
    {
        if(percent < 0 || percent > 100)
        {
            return;
        }
        this.volume = percent;
        this.volume_change_pcm = false;
        handler.sendEmptyMessage(KXHandleMessage.WLMSG_START_AUDIO_VOLUME);
    }

    public void setVolume(int percent, boolean volume_change_pcm)
    {
        if(percent < 0 || percent > 100)
        {
            return;
        }
        this.volume = percent;
        this.volume_change_pcm = volume_change_pcm;
        handler.sendEmptyMessage(KXHandleMessage.WLMSG_START_AUDIO_VOLUME);
    }


    public int getVolume()
    {
        return volume;
    }

    /**
     * 设置播放速度（0.5~2.0）
     * @param speed
     */
    public void setSpeed(float speed)
    {
        if(speed < 0.5 || speed > 2.0)
        {
            return;
        }
        this.speed = speed;
        handler.sendEmptyMessage(KXHandleMessage.WLMSG_START_AUDIO_SPEED);
    }

    /**
     * 设置音调（0.5~2.0）
     * @param pitch
     */
    public void setPitch(float pitch)
    {
        if(pitch < 0.5 || pitch > 2.0)
        {
            return;
        }
        this.pitch = pitch;
        handler.sendEmptyMessage(KXHandleMessage.WLMSG_START_AUDIO_PITCH);
    }

    /**
     * seek
     * @param secds
     */
    public void seek(double secds)
    {
        Message message = Message.obtain();
        message.obj = secds;
        message.what = KXHandleMessage.WLMSG_START_SEEK;
        handler.sendMessage(message);
    }

    /**
     * 设置采样率
     * @param wlSampleRate
     */
    public void setSampleRate(KXSampleRate wlSampleRate)
    {
        this.sampleRate = wlSampleRate.getValue();
    }

    /**
     * 是否回调PCM数据
     * @param showPcm
     */
    public void setShowPcm(boolean showPcm) {
        this.showPcm = showPcm;
    }

    /**
     * 播放下一曲
     */
    public void next()
    {
        if(!isNext)
        {
            isNext = true;
            handler.sendEmptyMessage(KXHandleMessage.WLMSG_START_PLAY_NEXT);
        }
    }

    /**
     * 获取总时间
     * @return
     */
    public double getDuration()
    {
        if(duration == 0)
        {
            duration = n_duration(hashcode);
        }
        return duration;
    }

    /**
     * 截屏
     */
    public void takePicture()
    {
        handler.sendEmptyMessage(KXHandleMessage.WLMSG_TAKE_PICTURE);
    }

    /**
     * 以buffer的方式提供数据源 buffer_len == 0 时 返回底层数据队列数
     * @param buffer
     */
    public int putBufferSource(byte[] buffer, int buffer_len)
    {
        return n_putbufferSource(hashcode, buffer, buffer_len);
    }

    /**
     * 设置seek时不回调时间
     */
    public void seekNoCallTime()
    {
        n_seeknotimecb(hashcode);
    }


    public void onSurfaceCreate(Surface surface)
    {
        this.surface = surface;
        n_surfaceCreate(hashcode);
    }

    public void onSurfaceChange(int width, int height, Surface surface)
    {
        this.surfaceWidth = width;
        this.surfaceHeight = height;
        this.surface = surface;
        n_surfaceChange(hashcode);
    }

    public void onSurfaceDestroy()
    {
        n_surfaceDestroy(hashcode);
    }

    /**
     * 释放surface资源（后期根据反馈可能会改成private）
     */
    public void release()
    {
        handler.sendEmptyMessage(KXHandleMessage.WLMSG_RELEASE_SURFACE);
    }

    /**
     * 音频、视频等播放模式设置 默认音频视频都播放
     * @param playModel
     */
    public void setPlayModel(KXPlayModel playModel)
    {
        this.playModel = playModel.getValue();
    }

    /**
     * 解码模式设置 默认优先硬解码
     * @param codecType
     */
    public void setCodecType(KXCodecType codecType)
    {
        this.codecType = codecType.getValue();
    }

    /**
     * 设置rtsp播放模式 udp tcp none
     * @param transportModel
     */
    public void setTransportModel(KXTransportModel transportModel)
    {
        this.transportModel = transportModel.getValue();
    }

    public int getTransportModel() {
        return transportModel;
    }

    /**
     * 设置播放结束是否清屏还是停留在最后一帧
     * @param clearLastPicture
     */
    public void setClearLastPicture(boolean clearLastPicture) {
        this.clearLastPicture = clearLastPicture;
    }

    /**
     * 设置数据源模式
     * 1、bufferSource为true，encryptFileSource为false时，是byte[]模式
     * 2、bufferSource为true，encryptFileSource为true时，是file模式，可用于加密视频播放，（回调里面自己解密）
     * @param bufferSource byte[]模式
     * @param encryptFileSource
     */
    public void setBufferSource(boolean bufferSource, boolean encryptFileSource) {
        this.bufferSource = bufferSource;
        this.encryptFileSource = encryptFileSource;
    }

    /**
     * 错误回调
     * @param code
     * @param msg
     */
    private void onError(int code, String msg)
    {
        if(onErrorListener != null)
        {
            onErrorListener.onError(code, msg);
        }
    }

    /**
     * 自定义顶点着色器
     * @param vShader
     */
    public void setvShader(String vShader) {
        this.vShader = vShader;
    }

    public void setfShader(String fShader) {
        this.fShader = fShader;
    }

    private int getVshaderLen()
    {
        if(TextUtils.isEmpty(vShader))
        {
            return 0;
        }
        return vShader.length();
    }

    /**
     * 自定义纹理着色器
     * @return
     */
    private int getFshaderLen()
    {
        if(TextUtils.isEmpty(fShader))
        {
            return 0;
        }
        return fShader.length();
    }

    /**
     * 更改滤镜（配合自定义着色器使用）
     */
    public void changeFilter()
    {
        n_changefilter(hashcode);
    }

    /**
     * 得到当前时间戳
     * @return
     */
    public double getNowClock()
    {
        return nowPts;
    }

    /**
     * 设置宽高比
     * @param w
     * @param h
     */
    public void scaleVideo(int w, int h)
    {
        this.scale_w = w;
        this.scale_h = h;
        n_scale(hashcode, w, h);
    }

    /**
     * 视频宽
     * @return
     */
    public int getVideoWidth()
    {
        return n_getVideoWidth(hashcode);
    }

    /**
     * 视频高
     * @return
     */
    public int getVideoHeight()
    {
        return n_getVideoHeight(hashcode);
    }

    /**
     * 设置延迟相对时间(单位秒)
     * @param offsetTime
     */
    public void setDelayOffsetTime(double offsetTime)
    {
        n_setDelayOffsetTime(hashcode, offsetTime);
    }

    public void setOnErrorListener(KXOnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public void setOnCompleteListener(KXOnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    public void setOnPreparedListener(KXOnPreparedListener onPreparedListener) {
        this.onPreparedListener = onPreparedListener;
    }

    public void setOnTimeInfoListener(KXOnTimeInfoListener onTimeInfoListener) {
        this.onTimeInfoListener = onTimeInfoListener;
    }

    public void setOnLoadListener(KXOnLoadListener onLoadListener) {
        this.onLoadListener = onLoadListener;
    }

    public void setOnPcmDataListener(KXOnPcmDataListener onPcmDataListener) {
        this.onPcmDataListener = onPcmDataListener;
    }

    public void setOnTakePictureListener(KXOnTakePictureListener onTakePictureListener) {
        this.onTakePictureListener = onTakePictureListener;
    }

    public void setOnDecryptListener(KXOnDecryptListener onDecryptListener) {
        this.onDecryptListener = onDecryptListener;
    }

    //-------------------------------------------------native call method------------------------------------------------------

    private void nCallPrepared(){
        handler.sendEmptyMessage(KXHandleMessage.WLMSG_START_PREPARED_OK);
    }

    private void nCallError(int code, String msg)
    {
        if(errorBean == null)
        {
            errorBean = new KXErrorBean();
            errorBean.setCode(code);
            errorBean.setMsg(msg);
        }
        Message message = Message.obtain();
        message.obj = errorBean;
        message.what = KXHandleMessage.WLMSG_START_ERROR;
        handler.sendMessage(message);
    }

    private void nCallReleaseStart()
    {
        handler.sendEmptyMessage(KXHandleMessage.WLMSG_START_RELEASE);
    }


    private void nCallReleaseComplete(boolean error, boolean iseof)
    {
        this.iseof = iseof;
        if(!iseof)
        {
            isNext = true;
        }
        Message message = Message.obtain();
        message.what = KXHandleMessage.WLMSG_START_RELEASE_COMPLETE;
        message.obj = error;
        handler.sendMessage(message);
    }

    private void nCallStop()
    {
        stop();
    }

    private void nCallTimeInfo(double time)
    {
        nowPts = time;
        Message message = Message.obtain();
        message.obj = time;
        message.what = KXHandleMessage.WLMSG_START_PLAY_TIME;
        handler.sendMessage(message);
    }

    private void nCallLoad(boolean load)
    {
        Message message = Message.obtain();
        message.obj = load;
        message.what = KXHandleMessage.WLMSG_START_PLAY_LOAD;
        handler.sendMessage(message);
    }

    /**
     * pcm 属性回调
     * @param bit
     * @param channel
     * @param samplerate
     */
    private void nCallPcmInfo(int bit, int channel, int samplerate)
    {
        if(kxPcmInfoBean == null)
        {
            kxPcmInfoBean = new KXPcmInfoBean();
        }
        kxPcmInfoBean.setBit(bit);
        kxPcmInfoBean.setChannel(channel);
        kxPcmInfoBean.setSamplerate(samplerate);
        handler.sendEmptyMessage(KXHandleMessage.WLMSG_START_AUDIO_INFO);
    }

    /**
     * pcm 音频数据回调
     * （注：如果在此回调中是比较耗时的操作，可使用队列临时缓存数据，以避免播放线程阻塞！）
     * @param size
     * @param data
     */
    private void nCallPcmData(int size, byte[] data)
    {
        if(onPcmDataListener != null)
        {
            onPcmDataListener.onPcmData(size, data);
        }
    }

    private void nCallTakePicture(byte[] pixels, int w, int h)
    {
        KXLog.d("nCallTakePicture : " + w + " * " + h);
        Bitmap stitchBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        stitchBmp.copyPixelsFromBuffer(ByteBuffer.wrap(pixels));
        Matrix m = new Matrix();
        m.setScale(1, -1);
        Bitmap newBitmap = Bitmap.createBitmap(stitchBmp, 0, 0, w, h, m, true);
        Message message = Message.obtain();
        message.obj = newBitmap;
        message.what = KXHandleMessage.WLMSG_TAKE_PICTURE_BITMAP;
        handler.sendMessage(message);
    }

    private byte[] nCallDecrypt(byte[] encrypt_data)
    {
        if(onDecryptListener != null)
        {
            KXLog.d("nCallDecrypt");
            return onDecryptListener.decrypt(encrypt_data);
        }
        return null;
    }

    //-------------------------------------------------native method-----------------------------------------------------------
    /**
     * native 准备方法
     * @param url
     */
    private native int n_prepared(int hashcode, String url);

    /**
     * prepared准备好了 开始播放
     * @return
     */
    private native int n_start(int hashcode);

    /**
     * native stop
     * @return
     */
    private native int n_stop(int hashcode);


    /**
     *
     * @return
     */
    private native int n_release(int hashcode);

    /**
     * 设置音轨
     * @param index
     * @return
     */
    private native int n_setAudioChannel(int hashcode, int index);

    private native int n_pause(int hashcode);

    private native int n_resume(int hashcode);

    private native String[] n_getAudioChannels(int hashcode);

    /**
     * 设置声道（左声道 右声道 立体声）
     * @param mute
     * @return
     */
    private native int n_setMute(int hashcode, int mute);

    /**
     * 设置音量大小（0~100）
     * @param volume
     * @return
     */
    private native int n_setVolume(int hashcode, int volume, boolean volume_change_pcm);

    /**
     * 设置播放速度（0.5~2.0）
     * @param speed
     * @return
     */
    private native int n_setSpeed(int hashcode, float speed);

    /**
     * 设置音调（0.5~2.0）
     * @param pitch
     * @return
     */
    private native int n_setPitch(int hashcode, float pitch);

    /**
     * seek time
     * @param pts
     */
    private native void n_seek(int hashcode, double pts);

    /**
     * 总的时长
     * @return
     */
    private native double n_duration(int hashcode);

    /**
     * seek的时候不回调时间数据
     */
    private native void n_seeknotimecb(int hashcode);

    private native void n_surfaceCreate(int hashcode);

    private native void n_surfaceChange(int hashcode);

    private native void n_surfaceDestroy(int hashcode);

    private native void n_releaseSurface(int hashcode);

    private native void n_takePicture(int hashcode);

    private native int n_putbufferSource(int hashcode, byte[] buffer, int buffer_len);

    private native int n_changefilter(int hashcode);

    private native int n_scale(int hashcode, int w, int h);

    private native int n_getVideoWidth(int hashcode);

    private native int n_getVideoHeight(int hashcode);

    private native int n_setDelayOffsetTime(int hashcode, double offsetTime);


    //------------------------------------------------handle message-----------------------------------------------------------


    public static class MediaHancler extends Handler
    {
        private WeakReference<KXMedia> reference;
        public MediaHancler(KXMedia media) {
            reference = new WeakReference<KXMedia>(media);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            KXMedia wlMedia = reference.get();
            if(wlMedia != null)
            {
                int ret = -1;
                switch (msg.what)
                {
                    case KXHandleMessage.WLMSG_START_PREPARED:
                        if(wlMedia.bufferSource)
                        {
                            if(wlMedia.encryptFileSource)
                            {
                                ret = wlMedia.n_prepared(wlMedia.hashcode, wlMedia.source);
                            }
                            else
                            {
                                ret = wlMedia.n_prepared(wlMedia.hashcode, "source is buffer type !");
                            }
                        }
                        else
                        {
                            ret = wlMedia.n_prepared(wlMedia.hashcode, wlMedia.source);
                        }

                        if(ret != 0)
                        {

                        }
                        break;
                    case KXHandleMessage.WLMSG_START_PREPARED_OK:
                        if(wlMedia.onPreparedListener != null)
                        {
                            if(!wlMedia.iseof)
                            {
                                wlMedia.iseof = true;
                                wlMedia.seek(wlMedia.nowPts);
                            }
                            wlMedia.onPreparedListener.onPrepared();
                        }
                        break;
                    case KXHandleMessage.WLMSG_START_STOP:
                        ret = wlMedia.n_stop(wlMedia.hashcode);
                        if(ret != 0)
                        {

                        }
                        break;
                    case KXHandleMessage.WLMSG_START_RELEASE:
                        wlMedia.n_release(wlMedia.hashcode);
                        break;
                    case KXHandleMessage.WLMSG_START_RELEASE_COMPLETE:
                        boolean isError = (boolean) msg.obj;
                        wlMedia.isPlay = false;
                        wlMedia.duration = 0;
                        if(isError)
                        {
                            if(wlMedia.errorBean != null)
                            {
                                wlMedia.onError(wlMedia.errorBean.getCode(), wlMedia.errorBean.getMsg());
                                wlMedia.errorBean = null;
                            }
                        }
                        else if(wlMedia.isNext)
                        {
                            wlMedia.prepared();
                        }
                        else
                        {
                            if(wlMedia.onCompleteListener != null)
                            {
                                wlMedia.onCompleteListener.onComplete();
                            }
                        }
                        break;
                    case KXHandleMessage.WLMSG_START_ERROR:
                        wlMedia.stop();
                        break;
                    case KXHandleMessage.WLMSG_START_PLAY:
                        ret = wlMedia.n_start(wlMedia.hashcode);
                        break;
                    case KXHandleMessage.WLMSG_START_CHANGE_AUDIO_TRACK:
                        wlMedia.n_setAudioChannel(wlMedia.hashcode, msg.arg1);
                        break;
                    case KXHandleMessage.WLMSG_START_PLAY_PAUSE:
                        wlMedia.n_pause(wlMedia.hashcode);
                        break;
                    case KXHandleMessage.WLMSG_START_PLAY_RESUME:
                        wlMedia.n_resume(wlMedia.hashcode);
                        break;
                    case KXHandleMessage.WLMSG_START_AUDIO_MUTE:
                        wlMedia.n_setMute(wlMedia.hashcode, wlMedia.mute);
                        break;
                    case KXHandleMessage.WLMSG_START_AUDIO_VOLUME:
                        wlMedia.n_setVolume(wlMedia.hashcode, wlMedia.volume, wlMedia.volume_change_pcm);
                        break;
                    case KXHandleMessage.WLMSG_START_AUDIO_SPEED:
                        wlMedia.n_setSpeed(wlMedia.hashcode, wlMedia.speed);
                        break;
                    case KXHandleMessage.WLMSG_START_AUDIO_PITCH:
                        wlMedia.n_setPitch(wlMedia.hashcode, wlMedia.pitch);
                        break;
                    case KXHandleMessage.WLMSG_START_SEEK:
                        wlMedia.n_seek(wlMedia.hashcode, (Double) msg.obj);
                        break;
                    case KXHandleMessage.WLMSG_START_PLAY_TIME:
                        if(wlMedia.onTimeInfoListener != null)
                        {
                            wlMedia.onTimeInfoListener.onTimeInfo((Double) msg.obj);
                        }
                        break;
                    case KXHandleMessage.WLMSG_START_PLAY_LOAD:
                        if(wlMedia.onLoadListener != null)
                        {
                            wlMedia.onLoadListener.onLoad((Boolean) msg.obj);
                        }
                        break;
                    case KXHandleMessage.WLMSG_START_AUDIO_INFO:
                        if(wlMedia.onPcmDataListener != null)
                        {
                            if(wlMedia.kxPcmInfoBean != null)
                            {
                                wlMedia.onPcmDataListener.onPcmInfo(wlMedia.kxPcmInfoBean.getBit(),
                                        wlMedia.kxPcmInfoBean.getChannel(), wlMedia.kxPcmInfoBean.getSamplerate());
                            }
                        }
                        break;
                    case KXHandleMessage.WLMSG_START_PLAY_NEXT:
                        if(wlMedia.isPlay)
                        {
                            wlMedia.stop();
                        }
                        else
                        {
                            wlMedia.prepared();
                        }
                        break;
                    case KXHandleMessage.WLMSG_TAKE_PICTURE:
                        wlMedia.n_takePicture(wlMedia.hashcode);
                        break;
                    case KXHandleMessage.WLMSG_TAKE_PICTURE_BITMAP:
                        Bitmap bitmap = (Bitmap) msg.obj;
                        if(wlMedia.onTakePictureListener != null)
                        {
                            wlMedia.onTakePictureListener.takePicture(bitmap);
                        }
                        break;
                    case KXHandleMessage.WLMSG_RELEASE_SURFACE:
                        wlMedia.n_releaseSurface(wlMedia.hashcode);
                        break;
                }
            }
        }
    }
}
