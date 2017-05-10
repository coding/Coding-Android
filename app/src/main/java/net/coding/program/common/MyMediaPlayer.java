package net.coding.program.common;

import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by Carlos2015 on 2015/8/14.
 */
public class MyMediaPlayer extends MediaPlayer {
    private OnCompletionListener onCompletionListener;
    private String path;
    private OnPreparedListener mOnPreparedListener;
    private int voiceId = -1;

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

    public String getDataSource() {
        return path;
    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        super.setDataSource(path);
        this.path = path;
    }

    public int getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(int id) {
        voiceId = id;
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
        path = null;
        voiceId = -1;
        onCompletionListener.onCompletion(this);
        onCompletionListener = null;
        mOnPreparedListener = null;
    }
}
