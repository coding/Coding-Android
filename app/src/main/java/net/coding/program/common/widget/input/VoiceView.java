package net.coding.program.common.widget.input;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.GlobalData;
import net.coding.program.common.audio.AmrAudioRecorder;
import net.coding.program.common.maopao.VoicePlayCallBack;
import net.coding.program.common.util.FileUtil;
import net.coding.program.common.util.PermissionUtil;
import net.coding.program.common.widget.SoundWaveView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.LongClick;
import org.androidannotations.annotations.Touch;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.UUID;

/**
 * Created by chenchao on 16/1/21.
 * 按下发语音模块
 */

@EViewGroup(R.layout.input_view_voice_view)
public class VoiceView extends FrameLayout {

    @ViewById
    ViewGroup soundWaveLayout;
    @ViewById
    ImageButton voiceRecordButton;
    @ViewById
    SoundWaveView soundWaveLeft, soundWaveRight;
    @ViewById
    TextView recordTime, tips_hold_to_talk;
    private AppCompatActivity activity;
    private AnimationDrawable voiceRecrodAnimtion;
    private AmrAudioRecorder mAmrAudioRecorder;
    private boolean isRecoding;
    private String out = null;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            long dur = (long) data.get("duration");
            double volume = (double) data.get("volume");
            float db = (float) volume * 1.618f;
            soundWaveLeft.setVolume(db);
            soundWaveRight.setVolume(db);
            if (dur >= 55000) {
                recordTime.setTextColor(0xFFFF3C30);
            } else {
                recordTime.setTextColor(0xFF50AEEA);
            }
            int t = (int) dur / 1000;
            int min = 0;
            int sec = 0;
            if (dur < 60) {
                sec = t;
            } else {
                min = t / 60;
                sec = t % 60;
            }
            String m = min < 10 ? "0" + min : "" + min;
            String s = sec < 10 ? "0" + sec : "" + sec;
            recordTime.setText(m + ":" + s);
            //录音时长在60.05s到60.5s的范围内时停止录音
            if (dur > 60100 && dur < 60500) {
                sendVoice();
            }
        }
    };
    private float touchY;
    private AmrAudioRecorder.VoiceRecordingCallBack mVoiceRecordingCallBack = new AmrAudioRecorder.VoiceRecordingCallBack() {
        @Override
        public void onRecord(long duration, double volume) {
            Message m = Message.obtain();
            Bundle data = new Bundle();
            data.putDouble("volume", volume);
            data.putLong("duration", duration);
            m.setData(data);
            mHandler.sendMessage(m);
        }
    };


    public VoiceView(Context context, AttributeSet attrs) {
        super(context, attrs);


        activity = (AppCompatActivity) Global.getActivityFromView(this);
    }

    @AfterViews
    void initVoiceView() {
        voiceRecrodAnimtion = (AnimationDrawable) voiceRecordButton.getBackground();
    }

    private void sendVoice() {
        tips_hold_to_talk.setText(R.string.hold_to_talk);
        soundWaveLayout.setVisibility(View.GONE);
        long recordDuration = stopRecord();
        if (recordDuration > 0l && out != null) {
            Log.w("test", "recordDuration=" + recordDuration);
            if (recordDuration >= 1000l && new File(out).length() > 100) {
                //录音发送
                VoiceRecordCompleteCallback callback = (VoiceRecordCompleteCallback) activity;

                callback.recordFinished(recordDuration >= 60000 ? 60000 : recordDuration, out);
            } else {
                cancelRecord();
                showToast(R.string.voice_can_not_send_because_duration_too_short);
            }

        }
        out = null;
    }

    private void pause() {

        try {
            if (mAmrAudioRecorder != null) {
                isRecoding = false;
                mAmrAudioRecorder.pause();

            }
        } catch (Exception e) {
            if (out != null) {
                File f = new File(out);
                if (f.exists()) {
                    f.delete();
                }
                out = null;
            }

        }
    }

    private boolean isTouchInside(int x, int y) {
        int w = voiceRecordButton.getMeasuredWidth();
        int h = voiceRecordButton.getMeasuredHeight();
        if (x >= 0 && x <= w && y >= 0 && y <= h) {
            int centX = w / 2;
            int centY = h / 2;
            return Math.sqrt((x - centX) * (x - centX) + (y - centY) * (y - centY)) <= w / 2;
        }
        return false;
    }

    private void reset() {
        try {
            mAmrAudioRecorder.continueRecord();
            isRecoding = true;
        } catch (Exception e) {
            e.printStackTrace();
            mAmrAudioRecorder.stop();
        }
    }

    @SuppressLint("CheckResult")
    @LongClick
    boolean voiceRecordButton() {
        new RxPermissions((FragmentActivity) Global.getActivityFromView(this))
                .request(PermissionUtil.AUDIO)
                .subscribe(granted -> {
                    if (granted) {
                        if (activity instanceof VoicePlayCallBack) {
                            VoicePlayCallBack voicePlayCallBack = (VoicePlayCallBack) activity;
                            voicePlayCallBack.onStopPlay();
                        }

                        //开始录音...
                        startRecord();
                    }
                });

        return true;
    }

    @Touch
    boolean voiceRecordButton(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            //long nowTime = System.currentTimeMillis();
            float nowY = event.getRawY();
            if (nowY - touchY <= -100) {
                //上滑取消录音发送
                cancelRecord();
                showToast(R.string.record_has_canceled);
            } else {
                sendVoice();
            }
            return false;
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchY = event.getRawY();
            return false;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (out != null) {
                if (!isTouchInside((int) event.getX(), (int) event.getY())) {
                    if (isRecoding) {
                        tips_hold_to_talk.setVisibility(View.VISIBLE);
                        tips_hold_to_talk.setText(R.string.loosen_to_cancel);
                        soundWaveLayout.setVisibility(View.GONE);
                        pause();
                    }
                } else {
                    if (!isRecoding) {
                        touchY = event.getRawY();
                        tips_hold_to_talk.setVisibility(View.GONE);
                        tips_hold_to_talk.setText(R.string.hold_to_talk);
                        soundWaveLayout.setVisibility(View.VISIBLE);
                        reset();
                    }
                }
            }

        }
        return false;
    }

    private void cancelRecord() {
        stopRecord();
        isRecoding = false;
        if (out != null) {
            File f = new File(out);
            f.delete();
            out = null;
        }
    }

    private long stopRecord() {
        isRecoding = false;
        //voiceRecrodAnimtion.stop();
        voiceRecrodAnimtion.selectDrawable(0);
        tips_hold_to_talk.setVisibility(View.VISIBLE);
        soundWaveLayout.setVisibility(View.GONE);
        recordTime.setTextColor(0xFF50AEEA);
        if (mAmrAudioRecorder == null) {
            return 0L;
        }
        mAmrAudioRecorder.stop();
        long dur = mAmrAudioRecorder.getDuration();
        mAmrAudioRecorder = null;
        return dur;
    }

    private void startRecord() {
        if (Global.sVoiceDir == null) {
            try {
                Global.sVoiceDir = FileUtil.getDestinationInExternalFilesDir(activity, Environment.DIRECTORY_MUSIC, FileUtil.getDownloadFolder()).getAbsolutePath();
            } catch (Exception e) {
                Global.errorLog(e);
            }

            if (Global.sVoiceDir == null) {
                showToast(R.string.record_failed_no_enough_storage_space);
                stopRecord();
                return;
            }
        }

        voiceRecrodAnimtion.selectDrawable(1);
        tips_hold_to_talk.setVisibility(View.GONE);
        soundWaveLayout.setVisibility(View.VISIBLE);
        recordTime.setText("00:00");
        soundWaveLeft.reSet();
        soundWaveRight.reSet();
        out = Global.sVoiceDir + File.separator + "coding_voice_" + UUID.randomUUID().toString() + ".amr";
        mAmrAudioRecorder = new AmrAudioRecorder(MediaRecorder.AudioSource.MIC, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, out);
        mAmrAudioRecorder.setVoiceRecordingCallBack(mVoiceRecordingCallBack);
        mAmrAudioRecorder.prepare();
        mAmrAudioRecorder.start();
        if (AmrAudioRecorder.State.ERROR == mAmrAudioRecorder.getState()) {
            showToast(R.string.record_failed);
        } else {
            isRecoding = true;
        }
    }

    private void showToast(int rid) {
        TextView tv = new TextView(activity);
        tv.setText(rid);
        tv.setTextSize(16);
        tv.setTextColor(Color.WHITE);
        tv.setBackgroundResource(R.drawable.tips_background);
        tv.setWidth((int) (GlobalData.sWidthPix * 0.7));
        tv.setHeight(GlobalCommon.dpToPx(48));
        tv.setGravity(Gravity.CENTER);
        Toast toast = new Toast(activity);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, activity.getSupportActionBar().getHeight() + 30);
        toast.setView(tv);
        toast.show();
    }

}
