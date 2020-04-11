package com.xiaokexin.kxmedia.surface;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.xiaokexin.kxmedia.KXMedia;
import com.xiaokexin.kxmedia.listener.KXOnVideoViewListener;

/**
 * author:xiaokexin
 * packName:com.xiaokexin.kxmedia.surface
 * Description:
 */
public class KXSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private KXMedia kxMedia;

    private KXOnVideoViewListener onVideoViewListener;
    private boolean init = false;

    private float x_down = 0;
    private double seek_time = 0;
    private boolean ismove = false;

    public void setKXMedia(KXMedia kxMedia) {
        this.kxMedia = kxMedia;
    }

    public KXSurfaceView(Context context) {
        this(context, null);
    }

    public KXSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KXSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init = false;
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(kxMedia != null)
        {
            kxMedia.onSurfaceCreate(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(kxMedia != null)
        {
            kxMedia.onSurfaceChange(width, height, holder.getSurface());
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
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(kxMedia != null)
        {
            kxMedia.onSurfaceDestroy();
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
        if(kxMedia != null && getHolder() != null && getHolder().getSurface() != null)
        {
            kxMedia.onSurfaceDestroy();
            kxMedia.onSurfaceCreate(getHolder().getSurface());
            kxMedia.onSurfaceChange(getWidth(), getHeight(), getHolder().getSurface());
        }
    }

    public void setOnVideoViewListener(KXOnVideoViewListener onVideoViewListener) {
        this.onVideoViewListener = onVideoViewListener;
    }

}
