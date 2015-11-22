package net.coding.program.mall;

import com.twotoasters.jazzylistview.effects.SlideInEffect;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.model.MallItemObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libo on 2015/11/20.
 */
@EFragment(R.layout.fragment_mall_list)
public class MallListFragment extends BaseFragment {

    FragmentCallback callBack;

    public interface FragmentCallback {

        void fragmentCallBack(int viewHeight);
    }

    @FragmentArg
    double userPoint;

    @FragmentArg
    ArrayList<MallItemObject> rawListData = new ArrayList<>();

    @FragmentArg
    ArrayList<String> stringList = new ArrayList<>();

    @FragmentArg
    String testStr = "";

    @ViewById
    HeaderGridView mallListHeaderGridView;

    ArrayList<MallItemObject> data = new ArrayList<>();

    MallListAdapter mallListAdapter;


    @AfterViews
    void initView() {

        //返回gridview高度
        callBack = (FragmentCallback) getActivity();

        if (rawListData != null) {
            data = rawListData;
        }

        mallListAdapter = new MallListAdapter(data);
        mallListHeaderGridView.setTransitionEffect(new UpSlideInEffect());
//        int height = MyApp.sHeightPix;
//        mallListHeaderGridView.setLayoutParams(
//                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        mallListHeaderGridView.setAdapter(mallListAdapter);

        mallListHeaderGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MallItemObject object = data.get(position);
                if (object.getPoints_cost() > userPoint) {
                    Toast.makeText(getActivity(), "您的码币不足！", Toast.LENGTH_SHORT).show();
                } else {
                    MallOrderSubmitActivity_.intent(getActivity())
                            .imgUrl(object.getImage())
                            .point(object.getPoints_cost())
                            .title(object.getName())
                            .start();
                }
            }
        });

        int height = setListViewHeightBasedOnChildren1(mallListHeaderGridView);
        callBack.fragmentCallBack(height);

    }

//    @Override
//    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data)
//            throws JSONException {
//        if (tag.equals(MALL_LIST_TAG)) {
//            if (code == 0) {
//                JSONObject obj = respanse.getJSONObject("data");
//                ArrayList<MallItemObject> datas = new ArrayList<>();
//                JSONArray jsonArray = obj.getJSONArray("list");
//                for (int i = 0; i < jsonArray.length(); ++i) {
//                    datas.add(new MallItemObject(jsonArray.getJSONObject(i)));
//                }
//
//                mallListHeaderGridView.setVisibility(View.VISIBLE);
//                this.data.clear();
//                this.data.addAll(datas);
//                mallListAdapter.removeAll();
//                mallListAdapter.addAll(this.data);
//                mallListAdapter.notifyDataSetChanged();
//            }
//        }
//        super.parseJson(code, respanse, tag, pos, data);
//    }

    class UpSlideInEffect extends SlideInEffect {

        @Override
        public void initView(View item, int position, int scrollDirection) {
            if (scrollDirection > 0) {
                super.initView(item, position, scrollDirection);
            }
        }

        @Override
        public void setupAnimation(View item, int position, int scrollDirection,
                ViewPropertyAnimator animator) {
            if (scrollDirection > 0) {
                super.setupAnimation(item, position, scrollDirection, animator);
            }
        }
    }

    class MallListAdapter extends BaseAdapter {

        private List<MallItemObject> data;

        public MallListAdapter(List<MallItemObject> data) {
            this.data = data;
        }

        public void addAll(List<MallItemObject> data) {
            this.data.addAll(data);
        }

        public void removeAll() {
            data.clear();
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity())
                        .inflate(R.layout.mall_list_item, parent, false);

                vh = new ViewHolder();
                vh.name = (TextView) convertView.findViewById(R.id.mall_list_item_title);
                vh.points_cost = (TextView) convertView.findViewById(R.id.mall_list_item_cost);
                vh.image = (ImageView) convertView.findViewById(R.id.mall_list_item_img);
                vh.exchange = (ImageView) convertView.findViewById(R.id.mall_list_item_exchange);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            MallItemObject object = data.get(position);
            vh.name.setText(object.getName());
            vh.points_cost.setText(object.getPoints_cost() + "");

            String imgUrl = object.getImage();
            getImageLoad().loadImage(vh.image, imgUrl);

            double cost = object.getPoints_cost();
            if (userPoint < cost) {
                vh.exchange.setImageDrawable(getResources().getDrawable(R.drawable.ic_unexchange));
            }
            return convertView;
        }
    }

    class ViewHolder {

        TextView name;

        TextView points_cost;

        ImageView image;

        ImageView exchange;
    }

    public static int setListViewHeightBasedOnChildren1(HeaderGridView listView) {
        //获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return 0;
        }

        int totalHeight = 0;
        int itemHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len;
                i++) { //listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0); //计算子项View 的宽高
            itemHeight = listItem.getMeasuredHeight();
            totalHeight += itemHeight; //统计所有子项的总高度
        }

        if (listAdapter.getCount() / 2 == 1) {
            totalHeight = (totalHeight + itemHeight) / 2;
        } else {
            totalHeight /= 2;
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + Global.dpToPx(60);
        //listView.getDividerHeight()获取子项间分隔符占用的高度
        //params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
        return params.height;
    }
}
