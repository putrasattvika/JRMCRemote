package io.github.putrasattvika.jrmcremote.ui.music;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import io.github.putrasattvika.jrmcremote.data.model.Song;
import io.github.putrasattvika.jrmcremote.player.PlayMode;
import io.github.putrasattvika.jrmcremote.player.PlaybackService;
import io.github.putrasattvika.jrmcremote.ui.base.BasePresenter;
import io.github.putrasattvika.jrmcremote.ui.base.BaseView;

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com
 * Date: 9/12/16
 * Time: 8:27 AM
 * Desc: MusicPlayerContract
 */
/* package */ interface MusicPlayerContract {

    interface View extends BaseView<Presenter> {

        void handleError(Throwable error);

        void onPlaybackServiceBound(PlaybackService service);

        void onPlaybackServiceUnbound();

        void onSongSetAsFavorite(@NonNull Song song);

        void onSongUpdated(@Nullable Song song);

        void updatePlayMode(PlayMode playMode);

        void updatePlayToggle(boolean play);

        void updateFavoriteToggle(boolean favorite);
    }

    interface Presenter extends BasePresenter {

        void retrieveLastPlayMode();

        void bindPlaybackService();

        void unbindPlaybackService();
    }
}
