package net.coding.program.common.maopao;

import android.media.MediaPlayer;

/**
 * 语音播放回调接口
 * todo 冒泡的整个模块感觉也可以分出去
 */

public interface VoicePlayCallBack {
    void onStartPlay(String path, int id, MediaPlayer.OnPreparedListener mOnPreparedListener, MediaPlayer.OnCompletionListener mOnCompletionListener);

    String getPlayingVoicePath();

    void onStopPlay();

    void markVoicePlayed(int id);

    int getPlayingVoiceId();
}
