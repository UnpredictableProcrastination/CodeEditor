package co.unpredictable_procrastination.codeeditor;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;


public class CodeEditFragmentPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<CodeEditFragment> fragments;
    private ArrayList<String> tabTitles;
    private Context context;
    private ViewPager vPager;

    public CodeEditFragmentPagerAdapter(FragmentManager fm, Context context, ViewPager vPager) {
        super(fm);
        this.vPager = vPager;
        fragments = new ArrayList<>();
        tabTitles = new ArrayList<>();
        //newWindow("whoami");
        this.context = context;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public int getItemPosition(Object object) {
        int index = fragments.indexOf(object);
        if (index == -1)
            return POSITION_NONE;
        else
            return index;
    }

    @Override
    public Fragment getItem(int position) {
        int count = fragments.size();
        if (position >= count) {
            //fragments.set(position, CodeEditFragment.newInstance());
            position = count - 1;
        }
        return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // генерируем заголовок в зависимости от позиции
        return tabTitles.get(position);
    }

    public void newWindow() {
        this.newWindow(null);
    }

    public void newWindow(String path) {
        fragments.add(CodeEditFragment.newInstance(path));
        tabTitles.add("Tab" + fragments.size() + path);
        notifyDataSetChanged();
        vPager.setCurrentItem(fragments.size() - 1);
    }

}
