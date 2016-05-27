package net.coding.program.project;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import net.coding.program.common.util.DensityUtil;
import net.coding.program.R;

/**
 * Created by Vernon on 15/11/17.
 */
public class ProjectActionUtil extends PopupWindow implements View.OnClickListener {
    private Context mContext;          // 上下文
    private TextView txtSetting;
    private OnSettingListener listener;
    private int pos;

    public ProjectActionUtil(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public void setListener(OnSettingListener listener) {
        this.listener = listener;
    }

    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.popu_menu_action, null);
        view.setOnClickListener(this);
        setContentView(view);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOutsideTouchable(true);
        setTouchable(true);
        setFocusable(true);
        setBackgroundDrawable(new BitmapDrawable());
        // 初始化动画
        setAnimationStyle(R.style.popwin_anim_style);
        findViewById(view);
        setListener();

    }

    public TextView getTxtSetting() {
        return txtSetting;
    }

    public void close() {
        dismiss();
    }

    private void setListener() {
        txtSetting.setOnClickListener(this);
    }

    private void findViewById(View view) {
        txtSetting = (TextView) view.findViewById(R.id.txtSetting);

    }

    public void show(View parentView, int postion) {
        this.pos = postion;
        showAsDropDown(parentView, -DensityUtil.dip2px(mContext, 24), -DensityUtil.dip2px(mContext, 107));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtSetting:
                listener.doAction(pos);
                break;

        }
    }

    public interface OnSettingListener {
        void doAction(int pos);
    }
}
