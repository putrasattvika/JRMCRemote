package io.github.putrasattvika.jrmcremote.ui.music;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.putrasattvika.jrmcremote.R;
import io.github.putrasattvika.jrmcremote.RxBus;
import io.github.putrasattvika.jrmcremote.data.model.PlayList;
import io.github.putrasattvika.jrmcremote.data.model.Song;
import io.github.putrasattvika.jrmcremote.data.source.AppRepository;
import io.github.putrasattvika.jrmcremote.event.PlayListNowEvent;
import io.github.putrasattvika.jrmcremote.event.PlaySongEvent;
import io.github.putrasattvika.jrmcremote.event.ServerConnectedEvent;
import io.github.putrasattvika.jrmcremote.player.IPlayback;
import io.github.putrasattvika.jrmcremote.player.PlayMode;
import io.github.putrasattvika.jrmcremote.player.PlaybackService;
import io.github.putrasattvika.jrmcremote.ui.base.BaseFragment;
import io.github.putrasattvika.jrmcremote.ui.widget.ShadowImageView;
import io.github.putrasattvika.jrmcremote.utils.AlbumUtils;
import io.github.putrasattvika.jrmcremote.utils.TimeUtils;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com
 * Date: 9/1/16
 * Time: 9:58 PM
 * Desc: MusicPlayerFragment
 */

public class MusicPlayerFragment extends BaseFragment implements MusicPlayerContract.View, IPlayback.Callback {

    // private static final String TAG = "MusicPlayerFragment";

    // Update seek bar every second
    private static final long UPDATE_PROGRESS_INTERVAL = 1000;

    @BindView(R.id.image_view_album)
    ShadowImageView imageViewAlbum;
    @BindView(R.id.text_view_name)
    TextView textViewName;
    @BindView(R.id.text_view_artist)
    TextView textViewArtist;
    @BindView(R.id.text_view_progress)
    TextView textViewProgress;
    @BindView(R.id.text_view_duration)
    TextView textViewDuration;
    @BindView(R.id.seek_bar)
    SeekBar seekBarProgress;

    @BindView(R.id.button_play_toggle)
    ImageView buttonPlayToggle;

    private PlaybackService mPlayer;

    private HandlerThread mThread;
    private Handler mHandler;
    private Song lastPlayingSong;
    private boolean lastPlayingStatus;

    private MusicPlayerContract.Presenter mPresenter;

    private Runnable mPlayerInfoCallback = new Runnable() {
        @Override
        public void run() {
            if (isDetached()) return;

            AsyncPlayerInfoUpdate asyncTask = new AsyncPlayerInfoUpdate();
            asyncTask.execute(seekBarProgress.getMax(), getCurrentSongDuration(), mPlayer.getProgress());
        }
    };

    private class AsyncPlayerInfoUpdate extends AsyncTask<Integer, Void, Pair<Integer, Integer>> {
        @Override
        protected Pair<Integer, Integer> doInBackground(Integer... params) {
            int seekbarMax = params[0];
            int cSongDuration = params[1];
            int playerProgress = params[2];

            int progressPercentage = (int) (seekbarMax * ((float) playerProgress / (float) cSongDuration));

            return new Pair<>(progressPercentage, playerProgress);
        }

        @Override
        protected void onPostExecute(Pair<Integer, Integer> result) {
            int progressPercentage = result.first;
            int playerProgress = result.second;

            updateProgressTextWithDuration(playerProgress);

            // Update Play/Pause status
            if (lastPlayingStatus != mPlayer.isPlaying()) {
                lastPlayingStatus = mPlayer.isPlaying();

                updatePlayToggle(mPlayer.isPlaying());
                mPlayer.onPlayStatusChanged(mPlayer.isPlaying());
            }

            // Update album art rotation animation
            if (mPlayer.isPlaying() && !imageViewAlbum.isRotateAnimationRunning()) {
                imageViewAlbum.resumeRotateAnimation();
            } else if(!mPlayer.isPlaying() && imageViewAlbum.isRotateAnimationRunning()) {
                imageViewAlbum.pauseRotateAnimation();
            }

            // Update song info
            if (mPlayer.getPlayingSong().equals(lastPlayingSong)) {
                onSongUpdated(mPlayer.getPlayingSong(), false);
            } else {
                onSongUpdated(mPlayer.getPlayingSong(), true);
                lastPlayingSong = mPlayer.getPlayingSong();
            }

            // Update seekbar progress
            if (progressPercentage >= 0 && progressPercentage <= seekBarProgress.getMax()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    seekBarProgress.setProgress(progressPercentage, true);
                } else {
                    seekBarProgress.setProgress(progressPercentage);
                }

                mHandler.postDelayed(mPlayerInfoCallback, UPDATE_PROGRESS_INTERVAL);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mThread = new HandlerThread("ProgressHandlerThread");
        mThread.start();
        mHandler = new Handler(mThread.getLooper());
        lastPlayingSong = null;
        lastPlayingStatus = false;

        seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    updateProgressTextWithProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mPlayerInfoCallback);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekTo(getDuration(seekBar.getProgress()));

                mHandler.removeCallbacks(mPlayerInfoCallback);
                mHandler.post(mPlayerInfoCallback);
            }
        });

        new MusicPlayerPresenter(getActivity(), AppRepository.getInstance(), this).subscribe();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPlayer != null) {
            mHandler.removeCallbacks(mPlayerInfoCallback);
            mHandler.post(mPlayerInfoCallback);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mPlayerInfoCallback);
    }

    @Override
    public void onDestroyView() {
        mPresenter.unsubscribe();
        super.onDestroyView();
    }

    // Click Events
    @OnClick(R.id.button_play_toggle)
    public void onPlayToggleAction(View view) {
        if (mPlayer == null) return;

        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.play();
        }

        mPlayer.onPlayStatusChanged(mPlayer.isPlaying());
        updatePlayToggle(mPlayer.isPlaying());
    }

    @OnClick(R.id.button_play_last)
    public void onPlayLastAction(View view) {
        if (mPlayer == null) return;

        mPlayer.playLast();
        onPlayStatusChanged(true);

        imageViewAlbum.pauseRotateAnimation();
        imageViewAlbum.resumeRotateAnimation();
    }

    @OnClick(R.id.button_play_next)
    public void onPlayNextAction(View view) {
        if (mPlayer == null) return;

        mPlayer.playNext();
        onPlayStatusChanged(true);

        imageViewAlbum.pauseRotateAnimation();
        imageViewAlbum.resumeRotateAnimation();
    }

    // RXBus Events

    @Override
    protected Subscription subscribeEvents() {
        return RxBus.getInstance().toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (o instanceof PlaySongEvent) {
                            onPlaySongEvent((PlaySongEvent) o);
                        } else if (o instanceof PlayListNowEvent) {
                            onPlayListNowEvent((PlayListNowEvent) o);
                        } else if (o instanceof ServerConnectedEvent) {
                            onServerConnectedEvent((ServerConnectedEvent) o);
                        }
                    }
                })
                .subscribe(RxBus.defaultSubscriber());
    }

    private void onPlaySongEvent(PlaySongEvent event) {
        Song song = event.song;
        playSong(song);
    }

    private void onPlayListNowEvent(PlayListNowEvent event) {
        PlayList playList = event.playList;
        int playIndex = event.playIndex;
        playSong(playList, playIndex);
    }

    private void onServerConnectedEvent(ServerConnectedEvent event) {
        mHandler.removeCallbacks(mPlayerInfoCallback);
        mHandler.post(mPlayerInfoCallback);

        onPlayStatusChanged(mPlayer.isPlaying());
        mPlayer.onPlayStatusChanged(mPlayer.isPlaying());
    }

    // Music Controls

    private void playSong(Song song) {
        return;
    }

    private void playSong(PlayList playList, int playIndex) {
        if (playList == null) return;

        mPlayer.play(playList, playIndex);
    }

    private void updateProgressTextWithProgress(int progress) {
        int targetDuration = getDuration(progress);
        textViewProgress.setText(TimeUtils.formatDuration(targetDuration));
    }

    private void updateProgressTextWithDuration(int duration) {
        textViewProgress.setText(TimeUtils.formatDuration(duration));
    }

    private void seekTo(int duration) {
        mPlayer.seekTo(duration);
    }

    private int getDuration(int progress) {
        return (int) (getCurrentSongDuration() * ((float) progress / seekBarProgress.getMax()));
    }

    private int getCurrentSongDuration() {
        Song currentSong = mPlayer.getPlayingSong();
        int duration = 0;
        if (currentSong != null) {
            duration = currentSong.getDuration();
        }
        return duration;
    }

    // Player Callbacks

    @Override
    public void onSwitchLast(Song last) {
        onSongUpdated(last);
    }

    @Override
    public void onSwitchNext(Song next) {
        onSongUpdated(next);
    }

    @Override
    public void onComplete(Song next) {
        onSongUpdated(next);
    }

    @Override
    public void onPlayStatusChanged(boolean isPlaying) {
        updatePlayToggle(isPlaying);
        mPlayer.onPlayStatusChanged(isPlaying);

        if (isPlaying) {
            imageViewAlbum.resumeRotateAnimation();
        } else {
            imageViewAlbum.pauseRotateAnimation();
        }
    }

    // MVP View

    @Override
    public void handleError(Throwable error) {
        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPlaybackServiceBound(PlaybackService service) {
        mPlayer = service;
        mPlayer.registerCallback(this);
    }

    @Override
    public void onPlaybackServiceUnbound() {
        mPlayer.unregisterCallback(this);
        mPlayer = null;
    }

    @Override
    public void onSongSetAsFavorite(@NonNull Song song) {
        updateFavoriteToggle(song.isFavorite());
    }

    public void onSongUpdated(@Nullable Song song) {
        if (song == null) {
            imageViewAlbum.cancelRotateAnimation();
            buttonPlayToggle.setImageResource(R.drawable.ic_play);
            seekBarProgress.setProgress(0);
            updateProgressTextWithProgress(0);
            seekTo(0);
            mHandler.removeCallbacks(mPlayerInfoCallback);
            return;
        }

        // Step 1: Song name and artist
        textViewName.setText(song.getDisplayName());
        textViewArtist.setText(song.getArtist());

        // Step 3: Duration
        textViewDuration.setText(TimeUtils.formatDuration(song.getDuration()));

        // Step 4: Keep these things updated
        // - Album rotation
        // - Progress(textViewProgress & seekBarProgress)
        Bitmap bitmap = AlbumUtils.parseAlbum(song);
        if (bitmap == null) {
            imageViewAlbum.setImageResource(R.drawable.default_record_album);
        } else {
            imageViewAlbum.setImageBitmap(AlbumUtils.getCroppedBitmap(bitmap));
        }

        mPlayer.onPlayStatusChanged(mPlayer.isPlaying());
        imageViewAlbum.pauseRotateAnimation();
        mHandler.removeCallbacks(mPlayerInfoCallback);
        if (mPlayer.isPlaying()) {
            imageViewAlbum.startRotateAnimation();
            mHandler.post(mPlayerInfoCallback);
            buttonPlayToggle.setImageResource(R.drawable.ic_pause);
        }
    }

    public void onSongUpdated(@Nullable Song song, boolean updateAlbumArt) {
        if (song == null) {
            imageViewAlbum.cancelRotateAnimation();
            buttonPlayToggle.setImageResource(R.drawable.ic_play);
            seekBarProgress.setProgress(0);
            updateProgressTextWithProgress(0);
            seekTo(0);
            mHandler.removeCallbacks(mPlayerInfoCallback);
            return;
        }

        // Step 1: Song name and artist
        textViewName.setText(song.getDisplayName());
        textViewArtist.setText(song.getArtist());

        // Step 3: Duration
        textViewDuration.setText(TimeUtils.formatDuration(song.getDuration()));

        // Step 4: Keep these things updated
        // - Album rotation
        // - Progress(textViewProgress & seekBarProgress)
        if (updateAlbumArt) {
            Bitmap bitmap = AlbumUtils.parseAlbum(song);
            if (bitmap == null) {
                imageViewAlbum.setImageResource(R.drawable.default_record_album);
            } else {
                imageViewAlbum.setImageBitmap(AlbumUtils.getCroppedBitmap(bitmap));
            }

            mPlayer.onPlayStatusChanged(mPlayer.isPlaying());
        }

        mHandler.removeCallbacks(mPlayerInfoCallback);
        if (mPlayer.isPlaying()) {
            mHandler.post(mPlayerInfoCallback);
            buttonPlayToggle.setImageResource(R.drawable.ic_pause);
        }
    }

    @Override
    public void updatePlayMode(PlayMode playMode) {
        // TODO
    }

    @Override
    public void updatePlayToggle(boolean play) {
        buttonPlayToggle.setImageResource(play ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    @Override
    public void updateFavoriteToggle(boolean favorite) {
    }

    @Override
    public void setPresenter(MusicPlayerContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
