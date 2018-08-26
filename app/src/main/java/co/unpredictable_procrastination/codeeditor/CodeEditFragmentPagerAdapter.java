package co.unpredictable_procrastination.codeeditor;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;


public class CodeEditFragmentPagerAdapter extends FragmentPagerAdapter {
    private final int PAGE_COUNT = 3;
    private String tabTitles[] = new String[] {"Tab1", "Tab2", "Tab3"};
    private Context context;

    public CodeEditFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override public int getCount() {
        return PAGE_COUNT;
    }

    @Override public Fragment getItem(int position) {
        return CodeEditFragment.newInstance("");
    }

    @Override public CharSequence getPageTitle(int position) {
        // генерируем заголовок в зависимости от позиции
        return tabTitles[position];
    }
}
