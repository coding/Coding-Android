package net.coding.program.common.ui;

import android.widget.AdapterView;

public class ActivityParamBuilder {
    private String mTitle;
    private String mUrl;
    private Class mViewHold;
    private AdapterView.OnItemClickListener mClick;

    public ActivityParamBuilder setTitle(String mTitle) {
        this.mTitle = mTitle;
        return this;
    }

    public ActivityParamBuilder setUrl(String mUrl) {
        this.mUrl = mUrl;
        return this;
    }

    public ActivityParamBuilder setViewHold(Class mViewHold) {
        this.mViewHold = mViewHold;
        return this;
    }

    public ActivityParamBuilder setItemClick(AdapterView.OnItemClickListener click) {
        mClick = click;
        return this;
    }

    public BaseListActivity.ActivityParam createActivityParam() {
        return new BaseListActivity.ActivityParam(mTitle, mUrl, mViewHold, mClick);
    }
}