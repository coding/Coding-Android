package net.coding.program.project.detail.wiki;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;

import net.coding.program.R;
import net.coding.program.common.CodingColor;
import net.coding.program.common.GlobalCommon;
import net.coding.program.network.model.wiki.Wiki;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by chenchao on 2017/4/11.
 * wiki 树控件的辅助类
 */
public class NodeHolder extends TreeNode.BaseNodeViewHolder<Wiki> {

    private ImageView icon;
    private TextView title;
    private View iconLayout;

    public NodeHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode node, Wiki value) {
        View v = LayoutInflater.from(context).inflate(R.layout.wiki_tree_item, null, false);
        iconLayout = v.findViewById(R.id.iconLayout);
        icon = (ImageView) v.findViewById(R.id.icon);
        title = (TextView) v.findViewById(R.id.title);
        View bottomLine = v.findViewById(R.id.bottomLine);


        int iconWidth; // icon 到文字的距离，包括 icon 本身的宽度
        if (value.children == null || value.children.isEmpty()) {
            icon.setVisibility(View.GONE);
            iconWidth = 0;
        } else {
            icon.setVisibility(View.VISIBLE);
            iconWidth = 22;
        }

        // 应设计师要求，多于 4 级的显示为 4 级
        int indentationLevel = node.getLevel();
        if (indentationLevel > 4) {
            indentationLevel = 4;
        }

        int leftSpace = (indentationLevel - 1) * 30 + 20 + iconWidth;
        int leftSpacePx = GlobalCommon.dpToPx(leftSpace);

        ViewGroup.MarginLayoutParams iconLP = (ViewGroup.MarginLayoutParams) iconLayout.getLayoutParams();
        iconLP.width = leftSpacePx;
        iconLayout.setLayoutParams(iconLP);

        ViewGroup.MarginLayoutParams lineLP = (ViewGroup.MarginLayoutParams) bottomLine.getLayoutParams();
        lineLP.leftMargin = leftSpacePx - GlobalCommon.dpToPx(iconWidth);
        bottomLine.setLayoutParams(lineLP);

        setData(node, value);

        return v;
    }

    public void notifyDataSetChanged() {
        Wiki value = (Wiki) mNode.getValue();
        setData(mNode, value);
    }

    private void setData(TreeNode node, Wiki value) {
        title.setText(value.title);
        title.setTag(value);
        title.setOnClickListener(v1 -> {
            if (value.children != null && !value.children.isEmpty()) {
                node.setExpanded(true);
            }

            EventBus.getDefault().post(NodeHolder.this);
        });

        if (node.getLevel() == 1) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        }
    }

    @Override
    public void toggle(boolean active) {
        icon.setImageResource(active ? R.mipmap.wiki_tree_down : R.mipmap.wiki_tree_right);
    }

    @Override
    public void toggleSelectionMode(boolean editModeEnabled) {
    }

    public Wiki getNodeValue() {
        return (Wiki) mNode.getValue();
    }

    public void select(boolean select) {
        if (select) {
            tView.expandNode(mNode);
            title.setTextColor(CodingColor.fontBlue);
        } else {
            title.setTextColor(mNode.getLevel() == 1 ? 0xFF272C33 : CodingColor.font1);
        }
    }
}
