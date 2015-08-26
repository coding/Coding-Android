package net.coding.program.common;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Carlos2015 on 2015/8/14.
 */
public class MyMediaPlayer extends MediaPlayer {
    private MediaPlayer.OnCompletionListener onCompletionListener;
    private String path;
    private OnPreparedListener mOnPreparedListener;

    @Override
    public void setOnCompletionListener(OnCompletionListener onCompletionListener) {
        super.setOnCompletionListener(onCompletionListener);
        this.onCompletionListener = onCompletionListener;
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        super.setOnPreparedListener(listener);
        this.mOnPreparedListener = mOnPreparedListener;
    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        super.setDataSource(path);
        this.path = path;
    }

    public String getDataSource(){
        return path;
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
        path = null;
        onCompletionListener.onCompletion(this);
        onCompletionListener = null;
        mOnPreparedListener = null;
    }
}
