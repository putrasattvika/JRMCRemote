package io.github.putrasattvika.jrmcremote.ui.main;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.widget.RadioButton;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import io.github.putrasattvika.jrmcremote.R;
import io.github.putrasattvika.jrmcremote.RxBus;
import io.github.putrasattvika.jrmcremote.event.ExitAppEvent;
import io.github.putrasattvika.jrmcremote.event.ResetViewPageEvent;
import io.github.putrasattvika.jrmcremote.ui.base.BaseActivity;
import io.github.putrasattvika.jrmcremote.ui.base.BaseFragment;
import io.github.putrasattvika.jrmcremote.ui.music.MusicPlayerFragment;
import io.github.putrasattvika.jrmcremote.ui.playlist.PlayListFragment;
import io.github.putrasattvika.jrmcremote.ui.settings.SettingsFragment;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MainActivity extends BaseActivity {
    static final int DEFAULT_PAGE_INDEX = 1;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.view_pager)
    ViewPager viewPager;

    @BindViews({R.id.radio_button_play_list, R.id.radio_button_music, R.id.radio_button_settings})
    List<RadioButton> radioButtons;

    String[] mTitles;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        // Main Controls' Titles
        mTitles = getResources().getStringArray(R.array.mp_main_titles);

        // Fragments
        BaseFragment[] fragments = new BaseFragment[mTitles.length];
        fragments[0] = new PlayListFragment();
        fragments[1] = new MusicPlayerFragment();
        fragments[2] = new SettingsFragment();

        // Inflate ViewPager
        MainPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager(), mTitles, fragments);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(adapter.getCount() - 1);
        viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.mp_margin_large));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Empty
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Empty
            }

            @Override
            public void onPageSelected(int position) {
                radioButtons.get(position).setChecked(true);
            }
        });

        radioButtons.get(DEFAULT_PAGE_INDEX).setChecked(true);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @OnCheckedChanged({R.id.radio_button_play_list, R.id.radio_button_music, R.id.radio_button_settings})
    public void onRadioButtonChecked(RadioButton button, boolean isChecked) {
        if (isChecked) {
            onItemChecked(radioButtons.indexOf(button));
        }
    }

    private void onItemChecked(int position) {
        viewPager.setCurrentItem(position);
        toolbar.setTitle(mTitles[position]);
    }

    @Override
    protected Subscription subscribeEvents() {
        return RxBus.getInstance().toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (o instanceof ExitAppEvent) {
                            finish();
                        } else if (o instanceof ResetViewPageEvent) {
                            radioButtons.get(DEFAULT_PAGE_INDEX).setChecked(true);
                        }
                    }
                })
                .subscribe(RxBus.defaultSubscriber());
    }
}
