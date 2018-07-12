package net.coding.program.search;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


/**
 * Created by Vernon on 15/11/21.
 */
public class SearchFramgentAdapter extends FragmentStatePagerAdapter {


    public static final String MERGE_REQUEST = "merge_requests";
    private static final String[] TITLES = {"项目", "任务", "冒泡", "文件", "用户", "合并请求"};
    private static final String[] tab = {"projects", "tasks", "tweets", "files", "friends", MERGE_REQUEST};

    private String key;

    public SearchFramgentAdapter(FragmentManager fm, String key) {
        super(fm);
        this.key = key;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                SearchResultListFragment fragment = new SearchResultListFragment_();
                fragment.setKeyword(key);
                fragment.setTabPrams(tab[position]);
                return fragment;
            case 1:
                SearchTaskFragment task = new SearchTaskFragment_();
                task.setKeyword(key);
                task.setTabPrams(tab[position]);
                return task;
            case 2:
                SearchMaopaoFragment maopaoFragment = new SearchMaopaoFragment_();
                maopaoFragment.setTabPrams(tab[position]);
                maopaoFragment.setKeyword(key);
                return maopaoFragment;
            case 3:
                SearchFileFragment searchFileFragment = new SearchFileFragment_();
                searchFileFragment.setTabPrams(tab[position]);
                searchFileFragment.setKeyword(key);
                return searchFileFragment;
            case 4:
                SearchUserFragment searchUserFragment = new SearchUserFragment_();
                searchUserFragment.setKeyword(key);
                searchUserFragment.setTabPrams(tab[position]);
                return searchUserFragment;
            default:
                SearchMergeRequestsFragment searchMergeRequestsFragment = new SearchMergeRequestsFragment_();
                searchMergeRequestsFragment.setKeyword(key);
                searchMergeRequestsFragment.setTabPrams(tab[position]);
                return searchMergeRequestsFragment;
        }
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }
}
