package io.github.putrasattvika.jrmcremote.ui.playlist;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.putrasattvika.jrmcremote.R;
import io.github.putrasattvika.jrmcremote.RxBus;
import io.github.putrasattvika.jrmcremote.data.model.PlayList;
import io.github.putrasattvika.jrmcremote.data.source.AppRepository;
import io.github.putrasattvika.jrmcremote.event.PlayListNowEvent;
import io.github.putrasattvika.jrmcremote.event.PlayListUpdatedEvent;
import io.github.putrasattvika.jrmcremote.event.ServerConnectedEvent;
import io.github.putrasattvika.jrmcremote.ui.base.BaseFragment;
import io.github.putrasattvika.jrmcremote.ui.base.adapter.OnItemClickListener;
import io.github.putrasattvika.jrmcremote.ui.common.DefaultDividerDecoration;
import io.github.putrasattvika.jrmcremote.ui.details.PlayListDetailsActivity;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com
 * Date: 9/1/16
 * Time: 9:58 PM
 * Desc: PlayListFragment
 */
public class PlayListFragment extends BaseFragment implements PlayListContract.View,
        JRiverPlayListAdapter.PlayListCallback {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.text_view_empty)
    TextView noPlaylistsAvailable;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private JRiverPlayListAdapter mAdapter;

    PlayListContract.Presenter mPresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mAdapter = new JRiverPlayListAdapter(getActivity(), null);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                PlayList playList = mAdapter.getItem(position);
                startActivity(PlayListDetailsActivity.launchIntentForPlayList(getActivity(), playList));
            }
        });
        mAdapter.setAddPlayListCallback(this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DefaultDividerDecoration());

        hideLoading();
        new JRiverPlayListPresenter(AppRepository.getInstance(), this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.unsubscribe();
    }

    // RxBus Events

    @Override
    protected Subscription subscribeEvents() {
        return RxBus.getInstance().toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (o instanceof PlayListUpdatedEvent) {
                            onPlayListUpdatedEvent((PlayListUpdatedEvent) o);
                        } else if (o instanceof ServerConnectedEvent) {
                            onServerConnectedEvent((ServerConnectedEvent) o);
                        }
                    }
                })
                .subscribe(RxBus.defaultSubscriber());
    }

    public void onPlayListUpdatedEvent(PlayListUpdatedEvent event) {
        mPresenter.loadPlayLists();
    }

    public void onServerConnectedEvent(ServerConnectedEvent event) {
        noPlaylistsAvailable.setVisibility(View.GONE);
        mAdapter.setRefreshPlaylistVisibility(true);
    }

    // Adapter Callbacks

    @Override
    public void onAction(View actionView, final int position) {
        final PlayList playList = mAdapter.getItem(position);
        PopupMenu actionMenu = new PopupMenu(getActivity(), actionView, Gravity.END | Gravity.BOTTOM);
        actionMenu.inflate(R.menu.play_list_action);

        actionMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_item_play_now) {
                    PlayListNowEvent playListNowEvent = new PlayListNowEvent(playList, 0);
                    RxBus.getInstance().post(playListNowEvent);
                }
                return true;
            }
        });
        actionMenu.show();
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
    public void handleError(Throwable error) {
        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPlayListsLoaded(List<PlayList> playLists) {
        mAdapter.setData(playLists);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void setPresenter(PlayListContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
