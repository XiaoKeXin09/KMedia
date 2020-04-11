package com.xiaokexin.kxmedia.surface;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;

import com.xiaokexin.kxmedia.KXMedia;
import com.xiaokexin.kxmedia.listener.KXOnVideoViewListener;

/**
 * author:xiaokexin
 * packName:com.xiaokexin.kxmedia.surface
 * Description:
 */
public class KXTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    private KXMedia kxMedia;
    private Surface surface;
    private SurfaceTexture surfaceTexture;
    private boolean init = false;
    private KXOnVideoViewListener onVideoViewListener;
    private float x_down = 0;
    private double seek_time = 0;
    private boolean ismove = false;

    public KXTextureView(Context context) {
        this(context, null);
    }

    public KXTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KXTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSurfaceTextureListener(this);
    }

    public void setKXMedia(KXMedia kxMedia) {
        this.kxMedia = kxMedia;
    }

    public void setOnVideoViewListener(KXOnVideoViewListener onVideoViewListener) {
        this.onVideoViewListener = onVideoViewListener;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        if(this.surfaceTexture == null)
        {
            this.surfaceTexture = surfaceTexture;
        }
        else
        {
            setSurfaceTexture(this.surfaceTexture);
        }
        if(surface == null)
        {
            surface = new Surface(surfaceTexture);
            if(kxMedia != null)
            {
                kxMedia.onSurfaceCreate(surface);
            }
        }
        if(kxMedia != null)
        {
            kxMedia.onSurfaceChange(width, height, surface);
            if(!init)
            {
                init = true;
                if(onVideoViewListener != null)
                {
                    onVideoViewListener.initSuccess();
                }
            }
        }

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        if(kxMedia != null)
        {
            kxMedia.onSurfaceChange(width, height, surface);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    public void release()
    {
        if(surfaceTexture != null)
        {
            surfaceTexture.release();
            surfaceTexture = null;
        }
        if(surface != null)
        {
            surface.release();
            surface = null;
        }
        if(kxMedia != null)
        {
            kxMedia = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(kxMedia == null)
        {
            return super.onTouchEvent(event);
        }
        int action = event.getAction();
        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                x_down = event.getX();
                ismove = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float offset_move = event.getX() - x_down;
                if(Math.abs(offset_move) > 50)
                {
                    ismove = true;
                    if(onVideoViewListener != null)
                    {
                        if(kxMedia != null && kxMedia.getDuration() > 0)
                        {
                            kxMedia.seekNoCallTime();
                            double seek_move_time;
                            if(offset_move > 0)
                            {
                                seek_move_time = (offset_move - 50) / (getWidth() * 3);
                            }
                            else
                            {
                                seek_move_time = (offset_move + 50) / (getWidth() * 3);
                            }
                            seek_time = kxMedia.getNowClock() + seek_move_time * kxMedia.getDuration();
                            if(seek_time < 0)
                            {
                                seek_time = 0;
                            }
                            if(seek_time > kxMedia.getDuration())
                            {
                                seek_time = kxMedia.getDuration();
                            }
                            onVideoViewListener.moveSlide(seek_time);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(ismove)
                {
                    if(onVideoViewListener != null)
                    {
                        if(seek_time < 0)
                        {
                            seek_time = 0;
                        }
                        if(seek_time > kxMedia.getDuration())
                        {
                            seek_time = kxMedia.getDuration();
                        }
                        onVideoViewListener.moveFinish(seek_time);
                        seek_time = 0;
                    }
                }
                break;
        }
        return true;
    }

    public void updateMedia(KXMedia kxMedia)
    {
        this.kxMedia = kxMedia;
        if(kxMedia != null && surface != null)
        {
            kxMedia.onSurfaceDestroy();
            kxMedia.onSurfaceCreate(surface);
            kxMedia.onSurfaceChange(getWidth(), getHeight(), surface);
        }
    }
}
