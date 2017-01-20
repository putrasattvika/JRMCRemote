package io.github.putrasattvika.jrmcremote.data.source;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.github.putrasattvika.jrmcremote.data.model.PlayList;
import io.github.putrasattvika.jrmcremote.utils.request.JRiverMCWSRequestUtil;
import rx.Observable;
import rx.Subscriber;

class JRiverDataSource implements AppContract {

    private static final String TAG = "JRiverDataSource";

    private Context mContext;

    public JRiverDataSource(Context context) {
        mContext = context;
    }

    // Play List

    @Override
    public Observable<List<PlayList>> playLists() {
        return Observable.create(new Observable.OnSubscribe<List<PlayList>>() {
            @Override
            public void call(Subscriber<? super List<PlayList>> subscriber) {
                List<PlayList> playLists = new ArrayList<>();

                PlayList playingNow = JRiverMCWSRequestUtil.getInstance().getPlayingNow();

                if (playingNow != null) {
                    playLists.add(playingNow);
                }
                playLists.addAll(JRiverMCWSRequestUtil.getInstance().getPlaylists());

                subscriber.onNext(playLists);
                subscriber.onCompleted();
            }
        });
    }

    @Override
    public List<PlayList> cachedPlayLists() {
        return null;
    }

    @Override
    public Observable<PlayList> create(final PlayList playList) {
        return Observable.create(new Observable.OnSubscribe<PlayList>() {
            @Override
            public void call(Subscriber<? super PlayList> subscriber) {
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        });
    }

    @Override
    public Observable<PlayList> update(final PlayList playList) {
        return Observable.create(new Observable.OnSubscribe<PlayList>() {
            @Override
            public void call(Subscriber<? super PlayList> subscriber) {
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        });
    }

    @Override
    public Observable<PlayList> delete(final PlayList playList) {
        return Observable.create(new Observable.OnSubscribe<PlayList>() {
            @Override
            public void call(Subscriber<? super PlayList> subscriber) {
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        });
    }
}
