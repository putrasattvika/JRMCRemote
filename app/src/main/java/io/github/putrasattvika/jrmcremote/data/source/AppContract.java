package io.github.putrasattvika.jrmcremote.data.source;

import java.util.List;

import io.github.putrasattvika.jrmcremote.data.model.PlayList;
import rx.Observable;

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com
 * Date: 9/10/16
 * Time: 4:52 PM
 * Desc: AppContract
 */
/* package */ interface AppContract {

    // Play List

    Observable<List<PlayList>> playLists();

    List<PlayList> cachedPlayLists();

    Observable<PlayList> create(PlayList playList);

    Observable<PlayList> update(PlayList playList);

    Observable<PlayList> delete(PlayList playList);

}
