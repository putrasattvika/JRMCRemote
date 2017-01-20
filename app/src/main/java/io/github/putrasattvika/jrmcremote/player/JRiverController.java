package io.github.putrasattvika.jrmcremote.player;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.github.putrasattvika.jrmcremote.data.model.PlayList;
import io.github.putrasattvika.jrmcremote.data.model.Song;
import io.github.putrasattvika.jrmcremote.utils.request.JRiverMCWSRequestUtil;

/**
 * Created by Sattvika on 14-Jan-17.
 */

public class JRiverController implements IPlayback, MediaPlayer.OnCompletionListener {
    private static volatile JRiverController sInstance;

    private final int FETCH_INTERVAL = 1000;

    private List<Callback> mCallbacks = new ArrayList<>(2);

    private boolean isPaused;

    private final HandlerThread fetcherThread;
    private final Handler fetcherHandler;
    private final Runnable fetcherRunnable;

    private JRiverController() {
        isPaused = true;

        fetcherThread = new HandlerThread("FetcherThread");
        fetcherThread.start();
        fetcherHandler = new Handler(fetcherThread.getLooper());

        fetcherRunnable = new Runnable() {
            @Override
            public void run() {
                JRiverMCWSRequestUtil.getInstance().refetchPlaybackInfo();
                isPaused = !JRiverMCWSRequestUtil.getInstance().isPlaying();

                fetcherHandler.postDelayed(fetcherRunnable, FETCH_INTERVAL);
            }
        };

        fetcherHandler.post(fetcherRunnable);
    }

    public static JRiverController getInstance() {
        if (sInstance == null) {
            synchronized (JRiverController.class) {
                if (sInstance == null) {
                    sInstance = new JRiverController();
                }
            }
        }

        return sInstance;
    }

    public boolean isServerConnected() {
        return JRiverMCWSRequestUtil.getInstance().isConnected();
    }

    @Override
    public void setPlayList(PlayList list) {
        if (JRiverMCWSRequestUtil.getInstance().playPlaylist(list)) {
            pause();
        }
    }

    @Override
    public boolean play() {
        if (JRiverMCWSRequestUtil.getInstance().play()) {
            notifyPlayStatusChanged(true);
            isPaused = false;

            return true;
        }

        return false;
    }

    @Override
    public boolean play(PlayList list) {
        if (JRiverMCWSRequestUtil.getInstance().playPlaylist(list)) {
            notifyPlayStatusChanged(true);
            isPaused = false;

            return true;
        }

        return false;
    }

    @Override
    public boolean play(PlayList list, int startIndex) {
        if (JRiverMCWSRequestUtil.getInstance().playPlaylist(list, startIndex)) {
            notifyPlayStatusChanged(true);
            isPaused = false;

            return true;
        }

        return false;
    }

    @Override
    public boolean play(Song song) {
        if (JRiverMCWSRequestUtil.getInstance().playSong(song)) {
            notifyPlayStatusChanged(true);
            isPaused = false;

            return true;
        }

        return false;
    }

    @Override
    public boolean playLast() {
        if (JRiverMCWSRequestUtil.getInstance().playPrev()) {
            notifyPlayStatusChanged(false);

            return true;
        }

        return false;
    }

    @Override
    public boolean playNext() {
        if (JRiverMCWSRequestUtil.getInstance().playNext()) {
            notifyPlayStatusChanged(false);

            return true;
        }

        return false;
    }

    @Override
    public boolean pause() {
        if (JRiverMCWSRequestUtil.getInstance().pause()) {
            notifyPlayStatusChanged(false);
            isPaused = true;

            return true;
        }

        return false;
    }

    @Override
    public boolean isPlaying() {
        return !isPaused;
    }

    @Override
    public int getProgress() {
        return JRiverMCWSRequestUtil.getInstance().getProgress();
    }

    @Nullable
    @Override
    public Song getPlayingSong() {
        return JRiverMCWSRequestUtil.getInstance().getPlayingSong();
    }

    @Override
    public boolean seekTo(int progress) {
        return JRiverMCWSRequestUtil.getInstance().seekTo(progress);
    }

    @Override
    public void setPlayMode(PlayMode playMode) {
        // TODO: playmode
    }

    // Listeners

    @Override
    public void onCompletion(MediaPlayer mp) {
        // TODO
//        Song next = null;
//        // There is only one limited play mode which is list, player should be stopped when hitting the list end
//        if (mPlayList.getPlayMode() == PlayMode.LIST && mPlayList.getPlayingIndex() == mPlayList.getNumOfSongs() - 1) {
//            // In the end of the list
//            // Do nothing, just deliver the callback
//        } else if (mPlayList.getPlayMode() == PlayMode.SINGLE) {
//            next = mPlayList.getCurrentSong();
//            play();
//        } else {
//            boolean hasNext = mPlayList.hasNext(true);
//            if (hasNext) {
//                next = mPlayList.next();
//                play();
//            }
//        }
//        notifyComplete(next);
    }

    @Override
    public void releasePlayer() {
        fetcherHandler.removeCallbacks(fetcherRunnable);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            fetcherThread.quitSafely();
        } else {
            fetcherThread.quit();
        }
        sInstance = null;
    }

    // Callbacks

    @Override
    public void registerCallback(Callback callback) {
        mCallbacks.add(callback);
    }

    @Override
    public void unregisterCallback(Callback callback) {
        mCallbacks.remove(callback);
    }

    @Override
    public void removeCallbacks() {
        mCallbacks.clear();
    }

    private void notifyPlayStatusChanged(boolean isPlaying) {
        for (Callback callback : mCallbacks) {
            callback.onPlayStatusChanged(isPlaying);
        }
    }

    private void notifyPlayLast(Song song) {
        for (Callback callback : mCallbacks) {
            callback.onSwitchLast(song);
        }
    }

    private void notifyPlayNext(Song song) {
        for (Callback callback : mCallbacks) {
            callback.onSwitchNext(song);
        }
    }

    private void notifyComplete(Song song) {
        for (Callback callback : mCallbacks) {
            callback.onComplete(song);
        }
    }
}
