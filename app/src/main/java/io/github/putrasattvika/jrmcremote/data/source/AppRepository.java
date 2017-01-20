package io.github.putrasattvika.jrmcremote.data.source;

import java.util.ArrayList;
import java.util.List;

import io.github.putrasattvika.jrmcremote.Injection;
import io.github.putrasattvika.jrmcremote.data.model.PlayList;
import rx.Observable;
import rx.functions.Action1;

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com
 * Date: 9/10/16
 * Time: 4:17 PM
 * Desc: AppRepository
 */
public class AppRepository implements AppContract {

    private static volatile AppRepository sInstance;

    private AppContract mLocalDataSource;

    private List<PlayList> mCachedPlayLists;

    private AppRepository() {
        mLocalDataSource = new JRiverDataSource(Injection.provideContext());
    }

    public static AppRepository getInstance() {
        if (sInstance == null) {
            synchronized (AppRepository.class) {
                if (sInstance == null) {
                    sInstance = new AppRepository();
                }
            }
        }
        return sInstance;
    }

    // Play List

    @Override
    public Observable<List<PlayList>> playLists() {
        return mLocalDataSource.playLists()
                .doOnNext(new Action1<List<PlayList>>() {
                    @Override
                    public void call(List<PlayList> playLists) {
                        mCachedPlayLists = playLists;
                    }
                });
    }

    @Override
    public List<PlayList> cachedPlayLists() {
        if (mCachedPlayLists == null) {
            return new ArrayList<>(0);
        }
        return mCachedPlayLists;
    }

    @Override
    public Observable<PlayList> create(PlayList playList) {
        return mLocalDataSource.create(playList);
    }

    @Override
    public Observable<PlayList> update(PlayList playList) {
        return mLocalDataSource.update(playList);
    }

    @Override
    public Observable<PlayList> delete(PlayList playList) {
        return mLocalDataSource.delete(playList);
    }
}
