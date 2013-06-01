package com.mirko.csr;





import com.mirko.csr.fragments.Sources;
import com.mirko.csr.fragments.TestersAndTranslators;
import com.mirko.csr.fragments.Thanks;
import com.mirko.csr.ui.DepthAnimation;
import com.mirko.csr.ui.TabsAdapter;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

public class AboutFragment extends FragmentActivity{
	
    private ViewPager mViewPager;
    private TabsAdapter mTabsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about); 
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        
        setTitle (getString(R.string.about));

        setupTabs();

        mViewPager.setPageTransformer(true, new DepthAnimation());

 
    }

    private void setupTabs()
    {
        ActionBar ab = getActionBar();
        Tab tab1 = ab.newTab().setText(getString(R.string.thanks));
        Tab tab2 = ab.newTab().setText(getString(R.string.sources));
        Tab tab3 = ab.newTab().setText(getString(R.string.contributors));

        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mTabsAdapter = new TabsAdapter(this, mViewPager);
        mTabsAdapter.addTab(tab1, Thanks.class,null);
        mTabsAdapter.addTab(tab2, Sources.class,null);
        mTabsAdapter.addTab(tab3, TestersAndTranslators.class,null);
    }

}
