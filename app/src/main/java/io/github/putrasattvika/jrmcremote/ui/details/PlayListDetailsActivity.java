package io.github.putrasattvika.jrmcremote.ui.details;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.putrasattvika.jrmcremote.R;
import io.github.putrasattvika.jrmcremote.RxBus;
import io.github.putrasattvika.jrmcremote.data.model.PlayList;
import io.github.putrasattvika.jrmcremote.data.model.Song;
import io.github.putrasattvika.jrmcremote.data.source.AppRepository;
import io.github.putrasattvika.jrmcremote.event.PlayListNowEvent;
import io.github.putrasattvika.jrmcremote.ui.base.BaseActivity;
import io.github.putrasattvika.jrmcremote.ui.base.adapter.OnItemClickListener;
import io.github.putrasattvika.jrmcremote.ui.common.DefaultDividerDecoration;

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com
 * Date: 9/11/16
 * Time: 6:34 AM
 * Desc: PlayListDetailsActivity
 */
public class PlayListDetailsActivity extends BaseActivity implements PlayListDetailsContract.View {

    private static final String TAG = "PlayListDetailsActivity";

    public static final String EXTRA_PLAY_LIST = "extraPlayList";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.text_view_empty)
    View emptyView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    boolean isFolder;
    PlayList mPlayList;

    SongAdapter mAdapter;

    PlayListDetailsContract.Presenter mPresenter;
    int mDeleteIndex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mPlayList = getIntent().getParcelableExtra(EXTRA_PLAY_LIST);
        if (mPlayList == null) {
            Log.e(TAG, "onCreate: folder & play list can't be both null!");
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list_details);
        ButterKnife.bind(this);
        supportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mPlayList.getName());
        }

        mAdapter = new SongAdapter(this, mPlayList.getSongs());
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                RxBus.getInstance().post(new PlayListNowEvent(mPlayList, position));
            }
        });
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DefaultDividerDecoration());
        emptyView.setVisibility(mPlayList.getNumOfSongs() > 0 ? View.GONE : View.VISIBLE);

        new PlayListDetailsPresenter(AppRepository.getInstance(), this).subscribe();
    }

    @Override
    protected void onDestroy() {
        mPresenter.unsubscribe();
        super.onDestroy();
    }

    public static Intent launchIntentForPlayList(Context context, PlayList playList) {
        Intent intent = new Intent(context, PlayListDetailsActivity.class);
        intent.putExtra(EXTRA_PLAY_LIST, playList);
        return intent;
    }

    // MVP View

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void handleError(Throwable e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSongDeleted(Song song) {
        mAdapter.notifyItemRemoved(mDeleteIndex);
        mAdapter.updateSummaryText();
    }

    @Override
    public void setPresenter(PlayListDetailsContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
