package it.univaq.byte_predator.shiptracker.Controllers.Main;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import it.univaq.byte_predator.shiptracker.R;

/**
 * Created by byte-predator on 21/02/18.
 */

public class TabAdapter extends FragmentPagerAdapter {
    private Context context;

    public TabAdapter(Context context, FragmentManager fm){
        super(fm);
        this.context = context;
    }
    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        if(position == 0){
            fragment = new TracksFragment();
        }else{
            fragment = new TracesFragment();
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 0)
            return context.getString(R.string.races_tab_name);
        else
            return context.getString(R.string.tracking_tab_name);
    }
}
