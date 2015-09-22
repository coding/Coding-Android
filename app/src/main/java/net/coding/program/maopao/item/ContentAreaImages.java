package net.coding.program.maopao.item;

import android.annotation.SuppressLint;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.HtmlContent;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.widget.GifMarkImageView;
import net.coding.program.maopao.MaopaoListBaseFragment;
import net.coding.program.maopao.MaopaoListFragment;
import net.coding.program.model.BaseComment;
import net.coding.program.model.Maopao;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chenchao on 15/3/31.
 * 有6张图片的控件，比如任务的评论
 * @update 添加支持语音消息
 */
@SuppressLint("NewApi")
public class ContentAreaImages extends ContentAreaBase {

    private static final int[] itemImages = new int[]{
            R.id.image0,
            R.id.image1,
            R.id.image2,
            R.id.image3,
            R.id.image4,
            R.id.image5
    };
    private static final int itemImagesMaxCount = itemImages.length;
    protected ImageLoadTool imageLoad;
    protected View imageLayout0;
    protected View imageLayout1;
    protected ImageView voice_play;
    protected LinearLayout voiceLayout;
    protected View linearLayout;//气泡
    protected boolean isRight;
    private AnimationDrawable voicePlayAnim;
    protected DisplayImageOptions imageOptions = new DisplayImageOptions
            .Builder()
            .showImageOnLoading(R.drawable.ic_default_image)
            .showImageForEmptyUri(R.drawable.ic_default_image)
            .showImageOnFail(R.drawable.ic_default_image)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .build();
    private int contentMarginBottom = 0;
    private ImageView images[] = new ImageView[itemImagesMaxCount];

    private static final HashMap<String, Boolean> mUpdating = new HashMap<>();

    public ContentAreaImages(View convertView, View.OnClickListener onClickContent, View.OnClickListener onclickImage, Html.ImageGetter imageGetterParamer, ImageLoadTool loadParams, int pxImageWidth) {
        super(convertView, onClickContent, imageGetterParamer);
        isRight = R.id.message_list_list_item_right == convertView.getId();
        imageLoad = loadParams;

        imageLayout0 = convertView.findViewById(R.id.imagesLayout0);
        imageLayout1 = convertView.findViewById(R.id.imagesLayout1);
        voice_play = (ImageView) convertView.findViewById(R.id.voice_play);
        voiceLayout = (LinearLayout) convertView.findViewById(R.id.voiceLayout);
        linearLayout = convertView.findViewById(R.id.linearLayout);

        for (int i = 0; i < itemImagesMaxCount; ++i) {
            images[i] = (ImageView) convertView.findViewById(itemImages[i]);
            images[i].setOnClickListener(onclickImage);
            images[i].setFocusable(false);
            images[i].setLongClickable(true);
            ViewGroup.LayoutParams lp = images[i].getLayoutParams();
            lp.width = pxImageWidth;
            lp.height = pxImageWidth;
        }

        contentMarginBottom = convertView.getResources().getDimensionPixelSize(R.dimen.message_text_margin_bottom);
    }

    // 用来设置冒泡的
    public void setData(Maopao.MaopaoObject maopaoObject) {
        setDataContent(maopaoObject.content, maopaoObject);
    }

    public void setData(BaseComment comment) {
        setDataContent(comment.content, comment);
    }

    //[voice]{'id':1,'voiceUrl':'/sd/voice/a.amr','voiceDuration':10,'played':1}[voice]
    public static Global.MessageParse parseVoice(String s){
        String str = s.substring(7, s.length() - 7);
        Global.MessageParse mp = new Global.MessageParse();
        mp.text = "";
        try {
            JSONObject jo = new JSONObject(str);
            mp.voiceUrl = jo.getString("voiceUrl");
            mp.voiceDuration = jo.getInt("voiceDuration");
            mp.isVoice = true;
            mp.played = jo.optInt("played");
            mp.id = jo.optInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
       return mp;
    }

    private void setDataContent(String data, Object contentObject) {
        Global.MessageParse maopaoData = HtmlContent.parseMaopao(data);
        if (maopaoData.text.isEmpty()) {
            content.setVisibility(View.GONE);

        } else {
            content.setTag(MaopaoListBaseFragment.TAG_COMMENT_TEXT, maopaoData.text);
            content.setVisibility(View.VISIBLE);
            content.setText(Global.changeHyperlinkColorMaopao(maopaoData.text, imageGetter, Global.tagHandler,
                    content.getContext().getAssets()), TextView.BufferType.EDITABLE);
            content.setTag(contentObject);

        }

        setImageUrl(maopaoData.uris);
    }




    public String getVocicePath(){
        return voicePath;
    }

    public void setVoicePlayCallBack(VoicePlayCallBack mVoicePlayCallBack){
        this.mVoicePlayCallBack = mVoicePlayCallBack;
    }

    private void downVoiceFile(){
        //服务器录音文件对应的本地文件路径
        final String voiceFilePath = Global.sVoiceDir + File.separator + voicePath.substring(voicePath.lastIndexOf('/')+1);
        //根据录音文件链接查找本地是否存在文件名相同的amr文件，如果不存在，就下载
        if(!new File(voiceFilePath).exists()){
            //确保一个录音文件只有一个请求去下载
            if(!mUpdating.containsKey(voicePath) ||  !mUpdating.get(voicePath)){
                mUpdating.put(voicePath,true);
                AsyncHttpClient client=new AsyncHttpClient();
                client.get(voicePath,new AsyncHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        if(statusCode == 200){
                            //保存录音文件到本地
                            BufferedOutputStream bos = null;
                            try {
                                File f = new File(voiceFilePath).getParentFile();
                                if(!f.exists()){
                                    f.mkdirs();
                                }
                                bos = new BufferedOutputStream(new FileOutputStream(voiceFilePath));
                                bos.write(responseBody);
                                bos.flush();
                                voicePath = voiceFilePath;
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }finally {
                                mUpdating.put(voicePath,false);
                                if(bos!=null){
                                    try {
                                        bos.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        mUpdating.put(voicePath, false);
//                        Log.e("Voice","Down the voice file failed,statusCode:"+statusCode+",\n error:"+error!=null?error.toString():""+",\n file:"+voicePath);
                    }
                });
            }

        }else{
            voicePath = voiceFilePath;
        }
    }

    private Handler mHandler = new Handler();
    int frame = 0;
    private boolean isAnimRuning;

    private void playVoiceAnim(){
        if(mVoicePlayCallBack.getPlayingVoiceId() == id){
            isAnimRuning = true;
            frame ++;
            if(frame>2){
                frame = 0;
            }

            if(Build.VERSION.SDK_INT>=16){
                voice_play.setBackground(voicePlayAnim.getFrame(frame));
            }else{
                voice_play.setBackgroundDrawable(voicePlayAnim.getFrame(frame));
            }
            mHandler.postDelayed(task,200);
        }

    }

    private void stopPlayVoiceAnim(){
        
        isAnimRuning = false;
        mHandler.removeCallbacksAndMessages(null);
        frame = 0;
        if(Build.VERSION.SDK_INT>=16){
            voice_play.setBackground(voicePlayAnim.getFrame(0));
        }else{
            voice_play.setBackgroundDrawable(voicePlayAnim.getFrame(0));
        }
    }


    private Runnable task = new Runnable() {
        @Override
        public void run() {
            if(isAnimRuning){
                playVoiceAnim();
            }

        }
    };


    public void setVoiceNeedDownload(boolean isNeedDownload){
        this.isNeedDownload = isNeedDownload;
    }

    private int id;
    private boolean isNeedDownload = true;
    private String voicePath;
    private VoicePlayCallBack mVoicePlayCallBack;
    // 用来设置message的
    public void setData(String data) {
        final Global.MessageParse maopaoData = data.startsWith("[voice]{")&&data.endsWith("}[voice]")?parseVoice(data):HtmlContent.parseMaopao(data);
        LinearLayout.LayoutParams lp_voiceLayout = (LinearLayout.LayoutParams) voiceLayout.getLayoutParams();
        if (maopaoData.text.isEmpty()) {
            content.setVisibility(View.GONE);
            content.setText("");
        } else {
            content.setTag(MaopaoListBaseFragment.TAG_COMMENT_TEXT, maopaoData.text);
            content.setVisibility(View.VISIBLE);
            content.setText(Global.changeHyperlinkColorMaopao(maopaoData.text, imageGetter,
                    Global.tagHandler, content.getContext().getAssets()));
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) content.getLayoutParams();
            if (maopaoData.uris.size() > 0) {
                lp.bottomMargin = contentMarginBottom;
            } else {
                lp.bottomMargin = 0;
            }
            content.setLayoutParams(lp);
        }

        if(maopaoData.isVoice){
            id = maopaoData.id;
            voicePath = maopaoData.voiceUrl;
            //下载录音文件并缓存到本地
            if(voicePath.startsWith("https://") || voicePath.startsWith("http://")){
                if(isNeedDownload){
                    downVoiceFile();
                }else{
                    String voiceFilePath = Global.sVoiceDir + File.separator + voicePath.substring(voicePath.lastIndexOf('/')+1);
                    if(new File(voiceFilePath).exists()){
                        voicePath = voiceFilePath;
                    }
                }
            }
            voice_play.setVisibility(View.VISIBLE);
            content.setVisibility(View.VISIBLE);
            content.setText("" + maopaoData.voiceDuration + " \"");
            content.setBackgroundDrawable(null);
            content.setFocusable(false);
            content.setFocusableInTouchMode(false);

            //让气泡的宽度随着录音长度变化 为什么还要在减去一个32dp?根据布局文件来算不需要的
            int maxWidth = MyApp.sWidthPix - (isRight?Global.dpToPx(57+53+36+32):Global.dpToPx(57+53+24+32));
            int minWidth = Global.dpToPx(60);
            int s = maopaoData.voiceDuration>= 60 ?60:maopaoData.voiceDuration;
            int width = minWidth + (maxWidth - minWidth)*s/60;
            width = width< minWidth?minWidth:width;
            lp_voiceLayout.width = width;
            if(isRight){
                lp_voiceLayout.gravity = Gravity.LEFT;
            }else{
                lp_voiceLayout.gravity = Gravity.RIGHT;
            }
            voicePlayAnim = (AnimationDrawable) voice_play.getResources().getDrawable(isRight?R.drawable.anim_play_voice_right:R.drawable.anim_play_voice_left);
            //voicePlayAnim.selectDrawable(0);
            //voicePlayAnim。stop();
            //这里没有用AnimationDrawable做帧动画，而是自己根据其原理实现帧动画，因为在内存中同一资源id的多个drawable实例的状态好像是一样的
            //而上面注释代码由于activity每隔一段时间就会刷新ui,在播放动画时就会执行到这里，从而打断动画，造成语音播放还没完动画就突然停止的现象
            //
            if(mVoicePlayCallBack==null || mVoicePlayCallBack.getPlayingVoiceId() != id){
                if(Build.VERSION.SDK_INT>=16){
                    voice_play.setBackground(voicePlayAnim.getFrame(0));
                }else{
                    voice_play.setBackgroundDrawable(voicePlayAnim.getFrame(0));
                }
            }else{
                stopPlayVoiceAnim();
                if(mVoicePlayCallBack.getPlayingVoicePath()!=null){
                    playVoiceAnim();
                }

            }

            View.OnClickListener mOnClickListener =  new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.w("Play Voice","onClicked");
                    if(voicePath.startsWith("https://") || voicePath.startsWith("http://")){
                        downVoiceFile();
                        return;
                    }

                    if(mVoicePlayCallBack!=null){
                        if(!isRight && maopaoData.played == 0){
                            mVoicePlayCallBack.markVoicePlayed(maopaoData.id);
                        }
                        String currentPlayingVoicePath = mVoicePlayCallBack.getPlayingVoicePath();
                        if(voicePath.equals(currentPlayingVoicePath)){
                            mVoicePlayCallBack.onStopPlay();
                        }else{
                            if(currentPlayingVoicePath!=null){
                                mVoicePlayCallBack.onStopPlay();
                            }
                            mVoicePlayCallBack.onStartPlay(voicePath,id,new MediaPlayer.OnPreparedListener(){

                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    playVoiceAnim();
                                }
                            },new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    Log.w("Play Voice", "stop anim");
                                    stopPlayVoiceAnim();
                                }
                            });
                        }
                    }
                }
            };
            linearLayout.setOnClickListener(mOnClickListener);
            content.setOnClickListener(mOnClickListener);
        }else{
            voice_play.setVisibility(View.GONE);
            linearLayout.setOnClickListener(null);
            content.setOnClickListener(null);
            lp_voiceLayout.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp_voiceLayout.gravity = Gravity.NO_GRAVITY;
        }
        voiceLayout.setLayoutParams(lp_voiceLayout);
        setImageUrl(maopaoData.uris);
//        linearLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                View resend = ((ViewGroup)v.getParent()).findViewById(R.id.resend);
//                int[] p = new int[2];
//                resend.getLocationOnScreen(p);
//                int x1 = p[0];
//                v.getLocationOnScreen(p);
//                int x2 = isRight?p[0]:p[0]+v.getMeasuredWidth();
//                Log.w("Margin",""+Global.pxToDp(Math.abs(x1-x2)));
//            }
//        });
    }

    protected void setImageUrl(ArrayList<String> uris) {
        if (uris.size() == 0) {
            imageLayout0.setVisibility(View.GONE);
            imageLayout1.setVisibility(View.GONE);
        } else if (uris.size() < 3) {
            imageLayout0.setVisibility(View.VISIBLE);
            imageLayout1.setVisibility(View.GONE);
        } else {
            imageLayout0.setVisibility(View.VISIBLE);
            imageLayout1.setVisibility(View.VISIBLE);
        }

        int min = Math.min(uris.size(), images.length);
        int i = 0;
        for (; i < min; ++i) {
            images[i].setVisibility(View.VISIBLE);
            images[i].setTag(new MaopaoListFragment.ClickImageParam(uris, i, false));
            if (images[i] instanceof GifMarkImageView) {
                ((GifMarkImageView) images[i]).showGifFlag(uris.get(i));
            }

            imageLoad.loadImage(images[i], uris.get(i), imageOptions);
        }

        for (; i < itemImagesMaxCount; ++i) {
            images[i].setVisibility(View.GONE);
        }
    }

    /**
     * 语音播放回调接口
     */
    public interface VoicePlayCallBack{
        void onStartPlay(String path,int id,MediaPlayer.OnPreparedListener mOnPreparedListener,MediaPlayer.OnCompletionListener mOnCompletionListener);
        String getPlayingVoicePath();
        void onStopPlay();
        void markVoicePlayed(int id);
        int getPlayingVoiceId();
    }

}
