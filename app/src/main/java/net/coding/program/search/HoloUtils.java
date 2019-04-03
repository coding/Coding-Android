package net.coding.program.search;

import android.databinding.BindingAdapter;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

/**
 * Created by Vernon on 15/12/3.
 */
public class HoloUtils {
    /**
     * 关键词加高亮
     *
     * @param textView
     * @param content
     */
    @BindingAdapter({"textEM"})
    public static void setHoloText(TextView textView, String content) {
        if (content.contains("<em>")) {
            int start = content.indexOf("<em>");
            int end = content.indexOf("</em>") - 4;
            content = content.replace("<em>", "").replace("</em>", "");
            SpannableString sp = new SpannableString(content);
            sp.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            textView.setText(sp);
        } else {
            textView.setText(content);
        }
    }
}
