package io.github.putrasattvika.jrmcremote.ui.playlist;

import java.util.List;

import io.github.putrasattvika.jrmcremote.data.model.PlayList;
import io.github.putrasattvika.jrmcremote.data.source.AppRepository;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com
 * Date: 9/11/16
 * Time: 1:28 AM
 * Desc: PlayListPresenter
 */
public class JRiverPlayListPresenter implements PlayListContract.Presenter {

    private PlayListContract.View mView;
    private AppRepository mRepository;
    private CompositeSubscription mSubscriptions;

    public JRiverPlayListPresenter(AppRepository repository, PlayListContract.View view) {
        mView = view;
        mRepository = repository;
        mSubscriptions = new CompositeSubscription();
        mView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        loadPlayLists();
    }

    @Override
    public void unsubscribe() {
        mView = null;
        mSubscriptions.clear();
    }

    @Override
    public void loadPlayLists() {
        Subscription subscription = mRepository.playLists()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<PlayList>>() {
                    @Override
                    public void onStart() {
                        mView.showLoading();
                    }

                    @Override
                    public void onCompleted() {
                        mView.hideLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.hideLoading();
                        mView.handleError(e);
                    }

                    @Override
                    public void onNext(List<PlayList> playLists) {
                        mView.onPlayListsLoaded(playLists);
                    }
                });
        mSubscriptions.add(subscription);
    }
}
