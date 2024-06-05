package com.moutamid.daiptv.activities;

import static androidx.media3.ui.PlayerView.SHOW_BUFFERING_WHEN_PLAYING;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.MappingTrackSelector;
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
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
        player =
                new ExoPlayer.Builder(this)
                        .setMediaSourceFactory(
                                new DefaultMediaSourceFactory(this).setLiveTargetOffsetMs(5000))
                        .setTrackSelector(trackSelector)
                        .build();

        MediaItem mediaItem =
                new MediaItem.Builder()
                        .setUri(url)
                        .setLiveConfiguration(
                                new MediaItem.LiveConfiguration.Builder().setMaxPlaybackSpeed(1.02f).build())
                        .build();


        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build();
        player.setAudioAttributes(audioAttributes, true);

        player.setMediaItem(mediaItem);

        PlayerView playerView = binding.playerView;

        playerView.setPlayer(player);

        playerView.setShowBuffering(SHOW_BUFFERING_WHEN_PLAYING);
        player.addListener(new Player.Listener() {

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                Log.d(TAG, "onPlaybackStateChanged:  " + playbackState);
                if (playbackState == Player.STATE_READY) {
                    if (resume != null && !isResumed) {
                        isResumed = true;
                        Log.d(TAG, "onPlaybackStateChanged: RESUMED");
                        player.seekTo(Stash.getLong(resume, 0));
                    }
                    setAudioTrack(trackSelector);
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
                Toast.makeText(VideoPlayerActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });

        player.prepare();

        player.play();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setAudioTrack(DefaultTrackSelector trackSelector) {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            Log.d(TAG, "setAudioTrack: mappedTrackInfo");
            Log.d(TAG, "setAudioTrack: mappedTrackInfo.getRendererCount()  " + mappedTrackInfo.getRendererCount());
            for (int rendererIndex = 0; rendererIndex < mappedTrackInfo.getRendererCount(); rendererIndex++) {
                TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex);
                Log.d(TAG, "trackGroups: " +trackGroups.length);
                for (int groupIndex = 0; groupIndex < trackGroups.length; groupIndex++) {
                    TrackGroup trackGroup = trackGroups.get(groupIndex);
                    for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
                        String mimeType = trackGroup.getFormat(trackIndex).sampleMimeType;
                        if (mimeType != null && MimeTypes.getTrackType(mimeType) == C.TRACK_TYPE_AUDIO) {
                            trackSelector.setParameters(
                                    trackSelector.buildUponParameters()
                                            .setRendererDisabled(rendererIndex, false)
                                            .build()
                            );
                        }
                    }
                }
            }
        } else Log.d(TAG, "MappedTrackInfo is null");
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
