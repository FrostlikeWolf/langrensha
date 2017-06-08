package com.wzc.lrsdemo.ui;

import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wzc.lrsdemo.R;
import com.wzc.lrsdemo.base.AgoraActivity;
import com.wzc.lrsdemo.base.BaseActivity;


public class MainActivity extends AgoraActivity {
    private int[] arr_tab = new int[]{R.mipmap.icon_tab_game, R.mipmap.icon_tab_list, R.mipmap.icon_tab_message};
    private int[] arr_tab_s = new int[]{R.mipmap.icon_tab_game_s, R.mipmap.icon_tab_list_s, R.mipmap.icon_tab_message_s};

    private LinearLayout tag_v;
    private ViewPager vp;

    private ScaleAnimation scaleAnimation;

    //    public native String stringFromJNI();
    static {
//        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initTagbar();
        // Example of a call to a native method
//    TextView tv = (TextView) findViewById(R.id.sample_text);
//    tv.setText(stringFromJNI());
    }

    private void init() {
        tag_v = (LinearLayout) findViewById(R.id.main_tag_v);
        vp = (ViewPager) findViewById(R.id.main_vp);


        final Fragment[] arr_fg = new Fragment[]{new HomeFragment(), new RankingFragment(), new MessageFragment()};
        vp.setOffscreenPageLimit(arr_fg.length);

        vp.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return arr_fg[position];
            }

            @Override
            public int getCount() {
                return 3;
            }
        });
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < arr_fg.length; i++) {
                    TextView textView = (TextView) tag_v.getChildAt(i);
                    if (i == position) {
                        textView.setTextColor(getResources().getColor(R.color.text_red));
                        Drawable drawable = getResources().getDrawable(arr_tab_s[i]);
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        textView.setCompoundDrawables(null, drawable, null, null);
                    } else {
                        textView.setTextColor(getResources().getColor(R.color.text_gray));
                        Drawable drawable = getResources().getDrawable(arr_tab[i]);
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        textView.setCompoundDrawables(null, drawable, null, null);
                    }
                }
                startAnimation(tag_v.getChildAt(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void initTagbar() {
        for (int i = 0; i < arr_tab.length; i++) {
            final int j = i;
            tag_v.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vp.setCurrentItem(j);
                }
            });
        }
    }

    private void startAnimation(View view) {
        if (scaleAnimation == null) {
            scaleAnimation = new ScaleAnimation(1.2F, 1F, 1.2F, 1F, Animation.RELATIVE_TO_SELF, 0.5F, Animation.RELATIVE_TO_SELF, 0.5F);
        }
        scaleAnimation.setInterpolator(new BounceInterpolator());
        scaleAnimation.setDuration(350);
        view.startAnimation(scaleAnimation);
    }


}
