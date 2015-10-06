package net.coding.program.common.enter;

import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.readystatesoftware.viewbadger.BadgeView;

import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.message.AmrAudioRecorder;
import net.coding.program.common.FileUtil;
import net.coding.program.common.Global;
import net.coding.program.common.RedPointTip;
import net.coding.program.common.widget.SoundWaveView;
import net.coding.program.maopao.item.ContentAreaImages;

import java.io.File;
import java.util.UUID;

/**
 * Created by Carlos2015 on 2015/8/6.
 * 语音输入框，支持文本，图片，表情
 * 注意其所用的控件布局文件必须是common_enter_emoji,不然会崩溃!!!
 */
public class EnterVoiceLayout extends EnterEmojiLayout {
    public static final int MAX_LENGTH = 1000 * 60;// 最大录音时长1000*60;
    private LinearLayout mInputLayout;
    private RelativeLayout arrowLayout;
    private Button btn_emoji;
    private CheckBox arrow, popVoice;
    private ActionBarActivity activity;
    private ImageButton voiceRecordButton;
    private SoundWaveView soundWaveLeft, soundWaveRight;
    private TextView recordTime, tips_hold_to_talk;
    private ViewGroup soundWaveLayout;
    private AnimationDrawable voiceRecrodAnimtion;
    private float touchY;
    private String out = null;
    //判断是否为新功能，如果是，语音按钮会显示红点
    private boolean isNewFunction = true;
    private BadgeView mBadgeView;
    //    private long startTime = 0l;
//    private long recordDuration = 0l;
    // private MediaRecorder mMediaRecorder;
    private AmrAudioRecorder mAmrAudioRecorder;
    private boolean isRecoding;
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

    public EnterVoiceLayout(ActionBarActivity activity, View.OnClickListener sendTextOnClick, Type type, EmojiType emojiType) {
        super(activity, sendTextOnClick, type, emojiType);
        init(activity);
    }

    public EnterVoiceLayout(ActionBarActivity activity, View.OnClickListener sendTextOnClick) {
        super(activity, sendTextOnClick);
        init(activity);
    }

    private void showToast(int rid){
        TextView tv = new TextView(activity);
        tv.setText(rid);
        tv.setTextSize(16);
        tv.setTextColor(Color.WHITE);
        tv.setBackgroundResource(R.drawable.tips_background);
        tv.setWidth((int) (MyApp.sWidthPix * 0.7));
        tv.setHeight(Global.dpToPx(48));
        tv.setGravity(Gravity.CENTER);
        Toast toast = new Toast(activity);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, activity.getSupportActionBar().getHeight() + 30);
        toast.setView(tv);
        toast.show();
    }

    private void init(final ActionBarActivity activity) {
        this.activity = activity;

        mInputLayout = (LinearLayout) activity.findViewById(R.id.mInputLayout);
        voiceRecordButton = (ImageButton) voiceLayout.findViewById(R.id.voiceRecordButton);
        arrowLayout = (RelativeLayout) activity.findViewById(R.id.arrowLayout);
        btn_emoji = (Button) activity.findViewById(R.id.btn_emoji);
        arrow = (CheckBox) activity.findViewById(R.id.arrow);
        popVoice = (CheckBox) activity.findViewById(R.id.popVoice);

        tips_hold_to_talk = (TextView) activity.findViewById(R.id.tips_hold_to_talk);
        soundWaveLayout = (ViewGroup) activity.findViewById(R.id.soundWaveLayout);
        soundWaveLeft = (SoundWaveView) soundWaveLayout.findViewById(R.id.soundWaveLeft);
        soundWaveRight = (SoundWaveView) soundWaveLayout.findViewById(R.id.soundWaveRight);
        recordTime = (TextView) soundWaveLayout.findViewById(R.id.recordTime);
        recordTime.setGravity(Gravity.CENTER);
        popVoice.setVisibility(View.VISIBLE);
        content.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if ("".equals(s.toString().trim())) {
                    popVoice.setVisibility(View.VISIBLE);
                } else {
                    popVoice.setVisibility(View.GONE);
                }
            }
        });
        popVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBadgeView != null && mBadgeView.isShown()) {
                    RedPointTip.markUsed(activity, RedPointTip.Type.Voice320);
                    // 设置进入的移动动画，设置了插值器，可以实现颤动的效果
                    TranslateAnimation anim = new TranslateAnimation(-100, 0, 0, 0);
                    anim.setInterpolator(new BounceInterpolator());
                    // 设置动画的持续时间
                    anim.setDuration(10000);
                    // 设置退出的移动动画
                    TranslateAnimation anim2 = new TranslateAnimation(0, -100, 0, 0);
                    anim2.setDuration(500);
                    mBadgeView.toggle(anim, anim2);
                }
                if (popVoice.isChecked()) {
                    rootViewHigh = rootView.getHeight();

                    final int bottomHigh = Global.dpToPx(100); // 底部虚拟按键高度，nexus5是73dp，以防万一，所以设大一点
                    int rootParentHigh = rootView.getRootView().getHeight();

                    if (isSoftKeyBoard) {
                        // 说明键盘已经弹出来了，等键盘消失后再设置 emoji keyboard 可见
                        toggleInputTypeWithCloseSoftkeyboard(InputType.Voice);
                        // 魅族手机的 rootView 无论输入法是否弹出高度都是不变的，只好搞个延时做这个事
                        rootView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                rootView.setLayoutParams(rootView.getLayoutParams());
                            }
                        }, 50);

                    } else {
                        toggleNoTextInput(InputType.Voice);
                        rootViewHigh = 0;
                    }
                } else {
                    mInputLayout.setVisibility(View.VISIBLE);
                    arrowLayout.setVisibility(View.GONE);
                    toggleSoftkeyboardWithCloseNoTextInput(InputType.Voice);
                }
            }
        });

        arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onArrowClick();
            }
        });
        btn_emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popVoice.setChecked(false);
                openEnterPanel();
            }
        });

        voiceRecrodAnimtion = (AnimationDrawable) voiceRecordButton.getBackground();
        voiceRecordButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //录音前先停止播放语音
                if (activity instanceof ContentAreaImages.VoicePlayCallBack) {
                    ContentAreaImages.VoicePlayCallBack voicePlayCallBack = (ContentAreaImages.VoicePlayCallBack) activity;
                    voicePlayCallBack.onStopPlay();
                }

                //开始录音...
                startRecord();
                return true;
            }
        });
        voiceRecordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
        });


        isNewFunction = RedPointTip.show(activity, RedPointTip.Type.Voice320);
        if (isNewFunction) {
            mBadgeView = new BadgeView(activity, popVoice);
            GradientDrawable sd = (GradientDrawable) activity.getResources().getDrawable(R.drawable.shape_red_circle);
            int size = Global.dpToPx(9);
            sd.setSize(size, size);
            if (Build.VERSION.SDK_INT >= 16) {
                mBadgeView.setBackground(sd);
            }else{
                mBadgeView.setBackgroundDrawable(sd);
            }
            mBadgeView.setText("");
            mBadgeView.setWidth(size);
            mBadgeView.setHeight(size);
            mBadgeView.setBadgeMargin(0, 0);
            mBadgeView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
            mBadgeView.show();
            ViewGroup.LayoutParams lp = ((ViewGroup) mBadgeView.getParent()).getLayoutParams();
            lp.width = Global.dpToPx(32 + 9);
            lp.height = lp.width;
            FrameLayout.LayoutParams fl = (FrameLayout.LayoutParams) popVoice.getLayoutParams();
            fl.gravity = Gravity.CENTER_VERTICAL;

        }
        closeEnterPanel();
    }

    private void onArrowClick() {
        arrowLayout.setVisibility(View.GONE);
        mInputLayout.setVisibility(View.VISIBLE);
        popVoice.setChecked(false);
        animEnterLayoutStatusChanaged(false);
    }

    @Override
    public void closeEnterPanel() {
        if (voiceLayout.getVisibility() == View.VISIBLE) {
            onArrowClick();
        } else {
            super.closeEnterPanel();
        }
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

    @Override
    protected void toggleSoftkeyboardWithCloseNoTextInput(InputType type) {
        popVoice.setChecked(false);
        super.toggleSoftkeyboardWithCloseNoTextInput(type);
    }

    @Override
    protected void toggleInputTypeWithCloseSoftkeyboard(InputType type) {
        if (type != null) {
            if (type == InputType.Voice) {
                mInputLayout.setVisibility(View.GONE);
                arrowLayout.setVisibility(View.VISIBLE);
            } else {
                mInputLayout.setVisibility(View.VISIBLE);
                arrowLayout.setVisibility(View.GONE);
            }
        }
        super.toggleInputTypeWithCloseSoftkeyboard(type);
    }

    @Override
    protected void toggleNoTextInput(InputType type) {
        if (type != null) {
            if (type == InputType.Voice) {
                mInputLayout.setVisibility(View.GONE);
                arrowLayout.setVisibility(View.VISIBLE);
            } else {
                mInputLayout.setVisibility(View.VISIBLE);
                arrowLayout.setVisibility(View.GONE);
            }
        }
        super.toggleNoTextInput(type);
    }

    private void startRecord() {
        if (Global.sVoiceDir == null) {
            try {
                Global.sVoiceDir = FileUtil.getDestinationInExternalFilesDir(activity, Environment.DIRECTORY_MUSIC, FileUtil.DOWNLOAD_FOLDER).getAbsolutePath();
            } catch (Exception e) {
                Global.errorLog(e);
            } finally {
                if (Global.sVoiceDir == null) {
                    showToast(R.string.record_failed_no_enough_storage_space);
                    stopRecord();
                    return;
                }
            }

        }

        voiceRecrodAnimtion.selectDrawable(1);
        voiceLayout.startRippleAnimation();
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
        if(AmrAudioRecorder.State.ERROR == mAmrAudioRecorder.getState()){
            showToast(R.string.record_failed);
        }else{
            isRecoding = true;
        }


//        if (mMediaRecorder == null) {
//            mMediaRecorder = new MediaRecorder();
//        }
//        try {
//            /* 设置麦克风*/
//            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//            /* 设置输出音频文件的格式：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 */
//            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
//           /*
//            * 设置音频的编码：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
//            * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
//            *
//             */
//            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//            if(isRecoding && out!=null && out.endsWith("temp")){
//
//            }else{
//                out = Global.sVoiceDir + File.separator + "coding_voice_" + UUID.randomUUID().toString() + ".amr";
//            }
//            File p = new File(out).getParentFile();
//            if (!p.exists()) {
//                p.mkdirs();
//            }
//            mMediaRecorder.setAudioChannels(1);
//            mMediaRecorder.setAudioSamplingRate(8000);
//            mMediaRecorder.setOutputFile(out);
//            mMediaRecorder.setMaxDuration(MAX_LENGTH);
//            /* 准备 */
//            mMediaRecorder.prepare();
//            /* 开始 */
//            mMediaRecorder.start();
//            isRecoding = true;
//        } catch (Exception e) {
//            isRecoding = false;
//            recordDuration = 0;
//            startTime = 0l;
//            showToast(R.string.record_failed);
//            Global.errorLog(e);
//            File f = new File(out);
//            if (f.exists()) {
//                f.delete();
//            }
//            out = null;
//            stopRecord();
//        }finally {
//            if(isRecoding && out!=null){
//                startTime = System.currentTimeMillis();
//                updateMicStatus();
//            }
//        }


    }

    private boolean isTouchInside(int x,int y){
        int w = voiceRecordButton.getMeasuredWidth();
        int h = voiceRecordButton.getMeasuredHeight();
        if(x>= 0 && x<=w && y>= 0 && y<=h){
            int centX = w/2;
            int centY = h/2;
            return Math.sqrt((x-centX)*(x-centX) + (y - centY)*(y - centY))<=w/2;
        }
        return false;
    }

//    private void appendRecordFile(){
//        if(out.endsWith("temp")){
//            File f = new File(out.replace("temp","amr"));
//            if(f.length()!=0){
//                File temp = new File(out);
//                BufferedInputStream bis = null;
//                BufferedOutputStream bos = null;
//                try {
//                    bis = new BufferedInputStream(new FileInputStream(temp));
//                    bos = new BufferedOutputStream(new FileOutputStream(f,true));
//                    //
//                    byte[] buf = new byte[6];
//                    int len = bis.read(buf);
//                    buf = new byte[1024];
//                    if(len==6){
//                        while((len = bis.read(buf))!=-1){
//                            bos.write(buf,0,len);
//                            buf = new byte[1024];
//                        }
//                    }
//
//                    bos.flush();
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }finally {
//                    try {
//                        if(bis!=null){
//                            bis.close();
//                        }
//                        if(bos!=null){
//                            bos.close();
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    temp.delete();
//                }
//            }
//        }
//    }
//
    private void pause(){

        try{
            if(mAmrAudioRecorder!=null){
                isRecoding = false;
                mAmrAudioRecorder.pause();

            }
        }catch (Exception e){
            if(out!=null){
                File f = new File(out);
                if (f.exists()) {
                    f.delete();
                }
                out = null;
            }

        }
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
        voiceLayout.stopRippleAnimation();
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




    /**
     * 录音完成回调接口
     */
    public interface VoiceRecordCompleteCallback{
        void recordFinished(long duration,String voicePath);
    }

}
