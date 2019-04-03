package net.coding.program.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;

/**
 * Created by chenchao on 16/9/23.
 */

public class BottomToolBar extends FrameLayout implements View.OnClickListener {

    private int xmlResource;
    private OnClickListener clickListener;
    private boolean showShadow = true;

    public BottomToolBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.BottomToolBar, 0, 0);

        try {
            xmlResource = ta.getResourceId(R.styleable.BottomToolBar_btb_xml, 0);
            showShadow = ta.getBoolean(R.styleable.BottomToolBar_btb_shadow, true);
        } finally {
            ta.recycle();
        }

        inflate(context, R.layout.widget_bottom, this);
        ViewGroup layout = (ViewGroup) findViewById(R.id.bottomContainer);
        LayoutInflater inflater = LayoutInflater.from(context);

        if (!showShadow) {
            findViewById(R.id.topShadow).setVisibility(GONE);
        }

        XmlResourceParser parse = getResources().getXml(xmlResource);
        try {
            parse.next();

            int eventType = parse.getEventType();
            boolean isFirst = true;
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG &&
                        parse.getName().equals("tab")) {

                    View v = inflater.inflate(R.layout.widget_bottom_item, layout, false);
                    TextView text = (TextView) v.findViewById(R.id.itemText);
                    for (int i = 0; i < parse.getAttributeCount(); ++i) {
                        String attrName = parse.getAttributeName(i);
                        switch (attrName) {
                            case "id":
                                v.setId(parse.getIdAttributeResourceValue(0));
                                break;
                            case "title":
                                text.setText(parse.getAttributeValue(i));
                                break;
                            case "icon":
                                text.setCompoundDrawablesWithIntrinsicBounds(parse.getAttributeResourceValue(i, 0), 0, 0, 0);
                                break;
                        }

                    }

                    if (isFirst) {
                        isFirst = false;
                    } else {
                        // item 之间添加分割线
                        inflater.inflate(R.layout.widget_bottom_item_divide, layout, true);
                    }

                    v.setOnClickListener(this);
                    layout.addView(v);
                }

                eventType = parse.next();
            }
        } catch (Exception e) {
            Global.errorLog(e);
        }
    }

    public void setClick(OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onClick(View v) {
        if (clickListener != null) {
            clickListener.onClick(v);
        }
    }

    public void disable(int id) {
        View v = findViewById(id);
        if (v == null) {
            return;
        }

        v.setEnabled(false);
        View textView = v.findViewById(R.id.itemText);
        if (textView != null) {
            textView.setEnabled(false);
        }
    }
}
