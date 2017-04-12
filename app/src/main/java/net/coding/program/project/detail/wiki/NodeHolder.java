package net.coding.program.project.detail.wiki;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;

import net.coding.program.R;
import net.coding.program.network.model.wiki.Wiki;

/**
 * Created by chenchao on 2017/4/11.
 */
public class NodeHolder extends TreeNode.BaseNodeViewHolder<Wiki> {

    private ImageView icon;
    private TextView title;

    public NodeHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode node, Wiki value) {
        View v = LayoutInflater.from(context).inflate(R.layout.wiki_tree_item, null, false);
        icon = (ImageView) v.findViewById(R.id.icon);
        title = (TextView) v.findViewById(R.id.title);
        title.setText(value.title);

        if (value.children == null || value.children.isEmpty()) {
            icon.setVisibility(View.GONE);
        } else {
            icon.setVisibility(View.VISIBLE);
        }

        return v;
    }

    @Override
    public void toggle(boolean active) {
        icon.setImageResource(active ? R.drawable.triangle_tree_down : R.drawable.triangle_tree_right);
    }

    @Override
    public void toggleSelectionMode(boolean editModeEnabled) {
    }
}
