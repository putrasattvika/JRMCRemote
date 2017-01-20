package io.github.putrasattvika.jrmcremote.ui.playlist;

import io.github.putrasattvika.jrmcremote.data.model.PlayList;
import io.github.putrasattvika.jrmcremote.ui.base.BasePresenter;
import io.github.putrasattvika.jrmcremote.ui.base.BaseView;

import java.util.List;

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com
 * Date: 9/11/16
 * Time: 1:25 AM
 * Desc: PlayListContract
 */
/* package */ interface PlayListContract {

    interface View extends BaseView<Presenter> {

        void showLoading();

        void hideLoading();

        void handleError(Throwable error);

        void onPlayListsLoaded(List<PlayList> playLists);
    }

    interface Presenter extends BasePresenter {
        void loadPlayLists();
    }
}
