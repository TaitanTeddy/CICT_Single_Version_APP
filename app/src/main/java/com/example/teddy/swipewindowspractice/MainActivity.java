package com.example.teddy.swipewindowspractice;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import static com.example.teddy.swipewindowspractice.accumulationFragment.FREQUENCY_HALF_HOUR;

public class MainActivity extends AppCompatActivity {

    //宣告SectionPagerAdapter和ViewPager物件
    //SectionPagerAdapter是繼承FragmentPagerAdapter或是FragmentStatePagerAdapter的自訂類別
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    TabLayout tabLayout;

    //定義menu的int
    private static final int MENU_SELECT_VALUE_SETTING = Menu.FIRST,
                            MENU_SELECT_ABOUT = Menu.FIRST+1,
                            MENU_SELECT_REFRESH = Menu.FIRST+2;
    //現在在哪個fragment
    public static final int MENU_REALTIME = 0,MENU_ACCUMULATION = 1;
    //工人狀態
    public static final int STATE_FALL = 1, STATE_COMA = 2, STATE_DROP =3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getSupportActionBar().hide(); // 隱藏APP名稱

        //ActionBar actionBar = getSupportActionBar();
        //actionBar.setBackgroundDrawable(new ColorDrawable(0xFF505050));

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        //設定viewPager和PagerAdapter
        mViewPager = (ViewPager)findViewById(R.id.viewPager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //設定ViewPager給TabLayout，就會顯示tab pages.
        tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(mViewPager);

    }


    // 建立ViewPager和TabLayout選單的class
    public class SectionsPagerAdapter extends FragmentPagerAdapter{
        public SectionsPagerAdapter(FragmentManager fm){
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;

            //根據目前頁面的編號，傳回對應的fragment物件
            switch (position){
                case 0:
                    fragment = new realtimeFragment();
                    break;
                case 1:
                    fragment = new accumulationFragment();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            //傳回頁面總數
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return "即時圖表";
                case 1:
                    return "累計圖表";
                default:
                    return null;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,MENU_SELECT_VALUE_SETTING,0,"參數設定");
        menu.add(0,MENU_SELECT_ABOUT,0,"說明");
        return true;
    }

    //onPrepareOptionsMenu需一起Override
    @Override
    public void invalidateOptionsMenu() {
        super.invalidateOptionsMenu();
    }

    //每次點menu時都進來一次
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        switch (tabLayout.getSelectedTabPosition()){
            //在Real-time Fragment的話就進這個
            case MENU_REALTIME:
                menu.clear();
                menu.add(0,MENU_SELECT_VALUE_SETTING,0,"參數設定");
                menu.add(0,MENU_SELECT_ABOUT,0,"說明");
                return true;
            //在Accumulation Fragment就進這個
            case MENU_ACCUMULATION:
                menu.clear();
                menu.add(0,MENU_SELECT_VALUE_SETTING,0,"參數設定");
                menu.add(0,MENU_SELECT_ABOUT,0,"說明");
                menu.add(0,MENU_SELECT_REFRESH,0,"重新整理");
                return true;
        }
        return true;
    }

    //選擇menu裡的選項
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Real-time中的選項
       if(tabLayout.getSelectedTabPosition() == MENU_REALTIME) {
            switch (item.getItemId()) {
                case MENU_SELECT_VALUE_SETTING:
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                    View mView = getLayoutInflater().inflate(R.layout.treshold_spinner, null);
                    mBuilder.setTitle("參數設定");
                    final Spinner SpnThresholdParameter = (Spinner) mView.findViewById(R.id.spinner_parameter_threshold);

                    ArrayAdapter<String> arrayAdapterThreshold = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item
                            , getResources().getStringArray(R.array.spinner_threshold_array));
                    arrayAdapterThreshold.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    SpnThresholdParameter.setAdapter(arrayAdapterThreshold);


                    mBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (SpnThresholdParameter.getSelectedItemPosition()){
                                case 0:
                                    realtimeFragment.Threshold = realtimeFragment.ThresholdSen;
                                    Log.d("Threshold", Integer.toString(realtimeFragment.Threshold));
                                    break;
                                case 1:
                                    realtimeFragment.Threshold = realtimeFragment.ThresholdNor;
                                    Log.d("Threshold", Integer.toString(realtimeFragment.Threshold));
                                    break;
                                case 2:
                                    realtimeFragment.Threshold = realtimeFragment.ThresholdNotSen;
                                    Log.d("Threshold", Integer.toString(realtimeFragment.Threshold));
                                    break;
                            }
                        }
                    });
                    mBuilder.setView(mView);
                    AlertDialog dialog = mBuilder.create();
                    dialog.show();
                    return true;

                case MENU_SELECT_ABOUT:
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("使用說明")
                            .setMessage("使用說明")
                            .setCancelable(true)
                            .setPositiveButton("了解",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                            .show();
                    return true;
            }
        }

        //Accumulation中的選項
        if(tabLayout.getSelectedTabPosition() == MENU_ACCUMULATION){
            switch (item.getItemId()) {
                case MENU_SELECT_VALUE_SETTING:
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                    View mView = getLayoutInflater().inflate(R.layout.frequency_spinner, null);
                    mBuilder.setTitle("參數設定");
                    final Spinner SpnFrequencyParameter = (Spinner) mView.findViewById(R.id.spinner_parameter_Frequency);
                    ArrayAdapter<String> arrayAdapterFrequency = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item
                            , getResources().getStringArray(R.array.spinner_frequency_array));
                    arrayAdapterFrequency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    SpnFrequencyParameter.setAdapter(arrayAdapterFrequency);
                    mBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (SpnFrequencyParameter.getSelectedItemPosition()){
                                case 0:
                                    accumulationFragment.FrequencyState = FREQUENCY_HALF_HOUR;
                                    break;
                                case 1:
                                    accumulationFragment.FrequencyState = accumulationFragment.FREQUENCY_ONE_HOUR;
                                    break;
                                case 2:
                                    accumulationFragment.FrequencyState = accumulationFragment.FREQUENCY_TWO_HOUR;
                                    break;
                                case 3:
                                    accumulationFragment.FrequencyState = accumulationFragment.FREQUENCY_FOUR_HOUR;
                                    break;
                                case 4:
                                    accumulationFragment.FrequencyState = accumulationFragment.FREQUENCY_EIGHT_HOUR;
                                    break;
                                case 5:
                                    accumulationFragment.FrequencyState = accumulationFragment.FREQUENCY_ONE_DAY;
                                    break;
                            }
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            for (Fragment fragment : fragmentManager.getFragments()) {
                                if (fragment != null && fragment.isVisible() && fragment instanceof accumulationFragment) {
                                    ((accumulationFragment) fragment).setBasicChart();
                                }
                            }
                        }
                    });
                    mBuilder.setView(mView);
                    AlertDialog dialog = mBuilder.create();
                    dialog.show();
                    return true;

                case MENU_SELECT_ABOUT:
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("使用說明")
                            .setMessage("使用說明")
                            .setCancelable(true)
                            .setPositiveButton("了解",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                            .show();
                    return true;
                case MENU_SELECT_REFRESH:
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    for (Fragment fragment : fragmentManager.getFragments()) {
                        if (fragment != null && fragment.isVisible() && fragment instanceof accumulationFragment) {
                            ((accumulationFragment) fragment).setBasicChart();
                        }
                    }
                    return true;

        }
    }
        return super.onOptionsItemSelected(item);
}
}
