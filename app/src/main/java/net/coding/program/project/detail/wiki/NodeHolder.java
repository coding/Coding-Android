package net.coding.program.project.detail.wiki;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.network.model.wiki.Wiki;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by chenchao on 2017/4/11.
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
        title.setText(value.title);
        View bottomLine = v.findViewById(R.id.bottomLine);

        int iconWidth; // icon 到文字的距离，包括 icon 本身的宽度
        if (value.children == null || value.children.isEmpty()) {
            icon.setVisibility(View.GONE);
            iconWidth = 0;
        } else {
            icon.setVisibility(View.VISIBLE);
            iconWidth = 22;
        }

        int level = node.getLevel();

        int leftSpace = (level - 1) * 30 + 20 + iconWidth;
        int leftSpacePx = Global.dpToPx(leftSpace);

        ViewGroup.MarginLayoutParams iconLP = (ViewGroup.MarginLayoutParams) iconLayout.getLayoutParams();
        iconLP.width = leftSpacePx;
        iconLayout.setLayoutParams(iconLP);

        ViewGroup.MarginLayoutParams lineLP = (ViewGroup.MarginLayoutParams) bottomLine.getLayoutParams();
        lineLP.leftMargin = leftSpacePx - Global.dpToPx(iconWidth);
        bottomLine.setLayoutParams(lineLP);

        title.setTag(value);
        title.setOnClickListener(v1 -> {
            if (value.children != null && !value.children.isEmpty()) {
                node.setExpanded(true);
            }

            EventBus.getDefault().post(NodeHolder.this);
        });

        return v;
    }

    @Override
    public void toggle(boolean active) {
        icon.setImageResource(active ? R.drawable.triangle_tree_down : R.drawable.triangle_tree_right);
    }

    @Override
    public void toggleSelectionMode(boolean editModeEnabled) {
    }

    public void select(boolean select) {
        if (select) {
            tView.expandNode(mNode);
            title.setTextColor(0xFF2EBE76);
        } else {
            title.setTextColor(mNode.getLevel() == 1 ? 0xFF272C33 : 0xFF323A45);
        }
    }
}
