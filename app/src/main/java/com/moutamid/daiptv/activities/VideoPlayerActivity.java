package com.moutamid.daiptv.activities;

import static androidx.media3.ui.PlayerView.SHOW_BUFFERING_WHEN_PLAYING;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
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
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.database.AppDatabase;
import com.moutamid.daiptv.databinding.ActivityVideoPlayerBinding;
import com.moutamid.daiptv.models.EPGModel;
import com.moutamid.daiptv.models.FavoriteModel;
import com.moutamid.daiptv.models.SeriesModel;
import com.moutamid.daiptv.models.VodModel;
import com.moutamid.daiptv.utilis.Constants;
import com.moutamid.daiptv.utilis.Features;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class VideoPlayerActivity extends BaseActivity {
    ActivityVideoPlayerBinding binding;
    private static final String TAG = "VideoPlayerActivity";
    ExoPlayer player;
    String resume;
    boolean isResumed = false;
    String type;
    VodModel vodModel;
    SeriesModel seriesModel;
    String banner;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String url = getIntent().getStringExtra("url");
        String name = getIntent().getStringExtra("name");
        String channel_id = getIntent().getStringExtra("channel_id");
        banner = getIntent().getStringExtra("banner");
        resume = getIntent().getStringExtra("resume");
        type = getIntent().getStringExtra("type");

        if (type != null) {
            if (type.equals(Constants.TYPE_MOVIE)) {
                vodModel = (VodModel) Stash.getObject(Constants.TYPE_MOVIE, VodModel.class);
            } else if (type.equals(Constants.TYPE_SERIES)) {
                seriesModel = (SeriesModel) Stash.getObject(Constants.TYPE_SERIES, SeriesModel.class);
            }
        }
        binding.title.setText(name);
        if (channel_id != null) {
            StringBuilder titleWithEpg = new StringBuilder(name);
            List<EPGModel> epgList = AppDatabase.getInstance(this).epgDAO().getTitle(channel_id.trim());
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            int i = 0;
            for (EPGModel e : epgList) {
                i++;
                Date startDate = Constants.parseDate(e.getStart());
                Date endDate = Constants.parseDate(e.getStop());
                titleWithEpg.append("\n\u2022 ").append(dateFormat.format(startDate)).append(" > ").append(dateFormat.format(endDate)).append(" ").append(e.getTitle());
                if (i == 5) {
                    break;
                }
            }
            binding.title.setText(titleWithEpg);
        }

        Log.d("VideoURLPlayer", "" + url);
        Log.d("VideoURLPlayer", "resume   " + resume);
        Log.d("VideoURLPlayer", "name   " + name);

        Constants.checkFeature(VideoPlayerActivity.this, Features.VIDEO_PLAYER);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
        player =
                new ExoPlayer.Builder(this)
                        .setMediaSourceFactory(
                                new DefaultMediaSourceFactory(this).setLiveTargetOffsetMs(5000))
                        .setTrackSelector(trackSelector)
                        .build();

        MediaMetadata mediaMetadata = new MediaMetadata.Builder()
                .setTitle(name).setDisplayTitle(name)
                .build();

        MediaItem mediaItem =
                new MediaItem.Builder()
                        .setMediaMetadata(mediaMetadata)
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

        playerView.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility -> {
            binding.title.setVisibility(visibility);
        });

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
                Log.d(TAG, "trackGroups: " + trackGroups.length);
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
//            long duration = player.getDuration();
//            if (duration <= 0) {
//                System.out.println("Error: Media duration is invalid.");
//                return;
//            }
//            boolean isPastTenMinutes = currentPosition > (10 * 60 * 1000);
//            if (isPastTenMinutes) {
//                if (!type.equals(Constants.TYPE_CHANNEL)) {
//                    ArrayList<FavoriteModel> list = Stash.getArrayList(Constants.RESUME, FavoriteModel.class);
//                    if (type.equals(Constants.TYPE_MOVIE)) {
//                        FavoriteModel favoriteModel = new FavoriteModel();
//                        favoriteModel.id = UUID.randomUUID().toString();
//                        favoriteModel.image = vodModel.stream_icon;
//                        favoriteModel.name = vodModel.name;
//                        favoriteModel.extension = vodModel.container_extension;
//                        favoriteModel.category_id = String.valueOf(vodModel.category_id);
//                        favoriteModel.type = Constants.RESUME;
//                        favoriteModel.steam_id = vodModel.stream_id;
//                        list.add(favoriteModel);
//                    } else {
//                        FavoriteModel favoriteModel = new FavoriteModel();
//                        favoriteModel.id = UUID.randomUUID().toString();
//                        favoriteModel.image = seriesModel.cover;
//                        favoriteModel.name = seriesModel.name;
//                        favoriteModel.category_id = seriesModel.category_id;
//                        favoriteModel.type = Constants.RESUME;
//                        favoriteModel.extension = seriesInfoModel.container_extension;
//                        favoriteModel.steam_id = Integer.parseInt(resume);
//                        list.add(favoriteModel);
//                    }
//                    Stash.put(Constants.RESUME, list);
//                }
//            }
        }
        player.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resume != null) {
            long currentPosition = player.getCurrentPosition();
            Stash.put(resume, currentPosition);
            long duration = player.getDuration();
            if (duration <= 0) {
                System.out.println("Error: Media duration is invalid.");
                return;
            }
            boolean isPastTenMinutes = currentPosition > 1000;
            if (isPastTenMinutes) {
                Log.d(TAG, "onDestroy: PAST 1 sec");
                if (!type.equals(Constants.TYPE_CHANNEL)) {
                    ArrayList<FavoriteModel> list = Stash.getArrayList(Constants.RESUME, FavoriteModel.class);
                    if (type.equals(Constants.TYPE_MOVIE)) {
                        boolean check = list.stream().anyMatch(favoriteModel -> favoriteModel.stream_id == vodModel.stream_id);
                        if (!check) {
                            FavoriteModel favoriteModel = new FavoriteModel();
                            favoriteModel.id = UUID.randomUUID().toString();
                            favoriteModel.image = banner;
                            favoriteModel.name = vodModel.name;
                            favoriteModel.extension = vodModel.container_extension;
                            favoriteModel.category_id = String.valueOf(vodModel.category_id);
                            favoriteModel.type = Constants.TYPE_MOVIE;
                            favoriteModel.stream_id = vodModel.stream_id;
                            list.add(favoriteModel);
                        }
                    } else {
                        boolean check = list.stream().anyMatch(favoriteModel -> favoriteModel.stream_id == Integer.parseInt(resume));
                        if (!check){
                            FavoriteModel favoriteModel = new FavoriteModel();
                            favoriteModel.id = UUID.randomUUID().toString();
                            favoriteModel.image = banner;
                            favoriteModel.name = seriesModel.name;
                            favoriteModel.category_id = seriesModel.category_id;
                            favoriteModel.type = Constants.TYPE_SERIES;
                            favoriteModel.extension = seriesModel.extension;
                            favoriteModel.stream_id = Integer.parseInt(resume);
                            list.add(favoriteModel);
                        }
                    }
                    Stash.put(Constants.RESUME, list);
                }
            }
        }
        player.release();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (resume != null) {
            long currentPosition = player.getCurrentPosition();
            Stash.put(resume, currentPosition);
//            long duration = player.getDuration();
//            if (duration <= 0) {
//                System.out.println("Error: Media duration is invalid.");
//                return;
//            }
//            boolean isPastTenMinutes = currentPosition > (10 * 60 * 1000);
//            if (isPastTenMinutes) {
//                if (!type.equals(Constants.TYPE_CHANNEL)) {
//                    ArrayList<FavoriteModel> list = Stash.getArrayList(Constants.RESUME, FavoriteModel.class);
//                    if (type.equals(Constants.TYPE_MOVIE)) {
//                        FavoriteModel favoriteModel = new FavoriteModel();
//                        favoriteModel.id = UUID.randomUUID().toString();
//                        favoriteModel.image = vodModel.stream_icon;
//                        favoriteModel.name = vodModel.name;
//                        favoriteModel.extension = vodModel.container_extension;
//                        favoriteModel.category_id = String.valueOf(vodModel.category_id);
//                        favoriteModel.type = Constants.RESUME;
//                        favoriteModel.steam_id = vodModel.stream_id;
//                        list.add(favoriteModel);
//                    } else {
//                        FavoriteModel favoriteModel = new FavoriteModel();
//                        favoriteModel.id = UUID.randomUUID().toString();
//                        favoriteModel.image = seriesModel.cover;
//                        favoriteModel.name = seriesModel.name;
//                        favoriteModel.category_id = seriesModel.category_id;
//                        favoriteModel.type = Constants.RESUME;
//                        favoriteModel.extension = seriesInfoModel.container_extension;
//                        favoriteModel.steam_id = Integer.parseInt(resume);
//                        list.add(favoriteModel);
//                    }
//                    Stash.put(Constants.RESUME, list);
//                }
//            }
        }
        finish();
    }

}
