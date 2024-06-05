package com.moutamid.daiptv.activities;

import static androidx.media3.ui.PlayerView.SHOW_BUFFERING_WHEN_PLAYING;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.ui.PlayerView;

import com.fxn.stash.Stash;
import com.moutamid.daiptv.BaseActivity;
import com.moutamid.daiptv.databinding.ActivityVideoPlayerBinding;
import com.moutamid.daiptv.utilis.Constants;
import com.moutamid.daiptv.utilis.Features;

public class VideoPlayerActivity extends BaseActivity {
    ActivityVideoPlayerBinding binding;
    private static final String TAG = "VideoPlayerActivity";
    ExoPlayer player;
    String resume;
    boolean isResumed = false;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String url = getIntent().getStringExtra("url");
        String name = getIntent().getStringExtra("name");
        resume = getIntent().getStringExtra("resume");

        Log.d("VideoURLPlayer", "" + url);
        Log.d("VideoURLPlayer", "resume   " + resume);

        Constants.checkFeature(VideoPlayerActivity.this, Features.VIDEO_PLAYER);

        player =
                new ExoPlayer.Builder(this)
                        .setMediaSourceFactory(
                                new DefaultMediaSourceFactory(this).setLiveTargetOffsetMs(5000))
                        .build();

        MediaItem mediaItem =
                new MediaItem.Builder()
                        .setUri(url)
                        .setLiveConfiguration(
                                new MediaItem.LiveConfiguration.Builder().setMaxPlaybackSpeed(1.02f).build())
                        .build();
        player.setMediaItem(mediaItem);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();

        PlayerView playerView = binding.playerView;

        playerView.setPlayer(player);

        playerView.setShowBuffering(SHOW_BUFFERING_WHEN_PLAYING);
        player.addListener(new Player.Listener() {

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                Log.d(TAG, "onPlaybackStateChanged:  " + playbackState);
                if (playbackState == Player.STATE_READY && !isResumed) {
                    if (resume != null) {
                        isResumed = true;
                        Log.d(TAG, "onPlaybackStateChanged: RESUMED");
                        player.seekTo(Stash.getLong(resume, 0));
                    }
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
                Toast.makeText(VideoPlayerActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        player.setPlayWhenReady(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (resume != null) {
            long currentPosition = player.getCurrentPosition();
            Stash.put(resume, currentPosition);
        }
        player.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resume != null) {
            long currentPosition = player.getCurrentPosition();
            Stash.put(resume, currentPosition);
        }
        player.release();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (resume != null) {
            long currentPosition = player.getCurrentPosition();
            Stash.put(resume, currentPosition);
        }
        finish();
    }

}
