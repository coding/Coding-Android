package net.coding.program.project.detail;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import net.coding.program.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by Neutra on 2015/4/24.
 */
@EViewGroup(R.layout.dropdown_tab_list)
public class DropdownListView extends ScrollView {
    @ViewById
    LinearLayout linearLayout;

    DropdownItemObject current;
    List<? extends DropdownItemObject> list;
    DropdownTabButton button;

    public DropdownListView(Context context) {
        super(context);
    }

    public DropdownListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DropdownListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DropdownListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void flush() {
        for (int i = 0, n = linearLayout.getChildCount(); i < n; i++) {
            View view = linearLayout.getChildAt(i);
            if (view instanceof DropdownListItemView) {
                DropdownListItemView itemView = (DropdownListItemView) view;
                DropdownItemObject data = (DropdownItemObject) itemView.getTag();
                boolean checked = data == current;
                itemView.bind(data.suffix == null ? data.text : data.text + data.suffix, checked);
                if (checked) button.setText(data.text);
            }
        }
    }

    public void bind(List<? extends DropdownItemObject> list, DropdownTabButton button, final Container container, DropdownItemObject selected) {
        current = selected;
        this.list = list;
        this.button = button;
        // 数据量不大，直接清空重建
        linearLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        boolean isFirst = true;
        for (DropdownItemObject item : list) {
            if (isFirst) {
                isFirst = false;
            } else {
                View divider = inflater.inflate(R.layout.dropdown_tab_list_divider, linearLayout, false);
                linearLayout.addView(divider);
            }
            DropdownListItemView view = (DropdownListItemView) inflater.inflate(R.layout.dropdown_tab_list_item, linearLayout, false);
            view.setTag(item);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    DropdownItemObject oldOne = current;
                    current = (DropdownItemObject) v.getTag();
                    flush();
                    container.hide();
                    if (oldOne != current) container.flushTopics();
                }
            });
            linearLayout.addView(view);
        }

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getVisibility() == View.VISIBLE) {
                    container.hide();
                } else {
                    container.show(DropdownListView.this);
                }
            }
        });
        if (current == null && list.size() > 0) {
            current = list.get(0);
        }
        flush();
    }

    public static interface Container {
        void show(DropdownListView listView);

        void hide();

        void flushTopics();
    }
}
