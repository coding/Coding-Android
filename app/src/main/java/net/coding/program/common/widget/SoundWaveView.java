package net.coding.program.common.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import net.coding.program.R;
import net.coding.program.common.Global;

import java.util.ArrayList;

/**
 * Created by Carlos2015 on 2015/8/6.
 * 根据音量大小产生音波扩散效果
 */
public class SoundWaveView extends View {
    private Context context;
    //音波颜色
    private int waveColor = 0xfffb8638;
    //最小声音大小(分贝)
    private final float minDecibel = 15.0f;
    //最大声音大小
    private final float maxDecibel = 85.0f;

    private final int Orientation_Left = 1;
    private final int Orientation_Right = 0;
    //音波震源位置
    private int soundOriginOrientation = Orientation_Right;

    private final int waveCout = 9;
    //存储声音的队列
    private ArrayList<Float> queen = new ArrayList<Float>(waveCout);
    private Paint mPaint;
    public SoundWaveView(Context context) {
        super(context);
        init(context,null);
    }

    public SoundWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public SoundWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SoundWaveView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context,attrs);
    }

    private void init(Context context,AttributeSet attrs){
        this.context = context;
        if(attrs!=null){
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SoundWaveView);
            waveColor = array.getColor(R.styleable.SoundWaveView_waveColor, 0xfffb8638);
            soundOriginOrientation = array.getInt(R.styleable.SoundWaveView_soundOriginOrientation, Orientation_Right);
            //array.getInteger(R.styleable.SoundWaveView_soundOriginOrientation,Orientation_Right);
            array.recycle();
        }
        for(int i=0;i<waveCout;i++){
            queen.add(minDecibel);
        }
        mPaint =  new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(waveColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //9个音波，每个宽度6px(2dp),最小高度9px,最大高度36px(12dp)
        setMeasuredDimension(Global.dpToPx((waveCout * 2 - 1) * 2), Global.dpToPx(12));
    }

    /**
     * 设置当前音量
     * @param volume
     */
    public synchronized void setVolume(float volume){
        queen.remove(queen.size()-1);
        queen.add(0,volume);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float scope = maxDecibel - minDecibel;
        int height = getMeasuredHeight();
        for(int i = 0;i<queen.size();i++){
            float v = queen.get(i);
            if( v>= maxDecibel){
                v = maxDecibel;
            }else if(v <= minDecibel){
                v = minDecibel;
            }
            drawWave(canvas,i,(int)(height*(v-minDecibel)/scope));
        }
    }

    public void reSet(){
        for(int i=0;i<waveCout;i++){
            queen.set(0,minDecibel);
        }
        postInvalidate();
    }

    /**
     * 绘制单个音波
     * @param canvas
     * @param position
     * @param waveHeight
     */
    private void drawWave(Canvas canvas,int position,int waveHeight){
        int minWaveHeight = Global.dpToPx(3);
        if(waveHeight <= minWaveHeight){
            waveHeight = minWaveHeight;
        }
        int waveWidth = getMeasuredWidth()/(queen.size()*2-1);
        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;
        switch (soundOriginOrientation){
            case Orientation_Left:
                left = ((position+1)*2-1)*waveWidth-waveWidth;
                break;
            case Orientation_Right:
                left = ((queen.size()-1-position+1)*2-1)*waveWidth-waveWidth;
                break;
        }
        right = left + waveWidth;
        top = (getMeasuredHeight()-waveHeight)/2;
        bottom = top + waveHeight;
        int angle = Global.dpToPx(1);
        canvas.drawRoundRect(new RectF(left,top,right,bottom),angle,angle,mPaint);
    }
}
