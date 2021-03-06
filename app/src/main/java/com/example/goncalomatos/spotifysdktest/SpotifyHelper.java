package com.example.goncalomatos.spotifysdktest;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.PlayerStateCallback;
import com.spotify.sdk.android.player.Spotify;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SpotifyHelper implements
        PlayerNotificationCallback, ConnectionStateCallback {

    private static final String CLIENT_ID = "68528c82f0a14b1da759976535533f48";
    private static final String ECHONEST_KEY = "BM5IMCRRSRYJMLZVK";
    private static final String REDIRECT_URI = "my-first-spotify-app://callback";
    private static final int REQUEST_CODE = 1337;

    //TODO create interface for this kind of activity
    private RunActivity bindedActivity;
    private AuthenticationResponse authenticationResponse;
    private Player mPlayer;
    private Config playerConfig;
    private boolean isPaused = false;

    public SpotifyHelper(RunActivity activity, AuthenticationResponse authResponse){
        bindedActivity = activity;
        authenticationResponse = authResponse;

        Config playerConfig = new Config(bindedActivity, authenticationResponse.getAccessToken(), CLIENT_ID);
        Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                mPlayer = player;
                mPlayer.addConnectionStateCallback(SpotifyHelper.this);
                mPlayer.addPlayerNotificationCallback(SpotifyHelper.this);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("Spotify Helper", "Could not initialize player: " + throwable.getMessage());
            }
        });
    }

    public void play(String spotifyUri){
        if(isPaused) {
            isPaused = false;
        }
        if (mPlayer == null)
        {
            Log.e("SpotifyHelper", "Spotify player not yet initiliazed");
        }

        mPlayer.play(spotifyUri);
    }

    public void pause() {
        isPaused = true;
        mPlayer.pause();
    }

    public void play(List<String> spotifyUriList){
        if (isPaused) {
            isPaused = false;
        }
        if (mPlayer == null)
        {
            Log.e("SpotifyHelper", "Spotify player not yet initiliazed");
        }

        mPlayer.play(spotifyUriList);
    }

    public void skipSong() {
        mPlayer.skipToNext();
    }

    protected String buildEchoNestRequest(double pace, String style){
        String request = "http://developer.echonest.com/api/v4/playlist/static?api_key=" + ECHONEST_KEY +
                "&style=" + style +
                "&min_tempo=" + pace +
                "&max_tempo=" + (pace + 50) +
                "&bucket=id:spotify" +
                "&bucket=tracks" +
                "&results=10" +
                "&limit=true" +
                "&min_duration=60" +
                "&artist_min_familiarity=.2" +
                "&type=artist-description";
        Log.d("echonest", request);
        return request;
    }

    public void queryAndPlay(final double pace){
        Log.d("cenas", "query and play");
        mPlayer.getPlayerState(new PlayerStateCallback() {
            @Override
            public void onPlayerState(PlayerState playerState) {
                if (isPaused) {
                    mPlayer.resume();
                    isPaused = false;
                } else {
                    String style = PreferenceManager
                            .getDefaultSharedPreferences(bindedActivity)
                            .getString("pref_style", "rock");
                    new RequestHttpTask() {
                        @Override
                        protected void onPostExecute(String result) {
                            super.onPostExecute(result);
                            List<String> spotifyURIList = new ArrayList<String>();
                            Log.d("cenas", "got song");
                            //getSong
                            try {
                                if (result != null) {
                                    JSONObject jObject = new JSONObject(result);

                                    JSONArray jArray = jObject.getJSONObject("response").getJSONArray("songs");
                                    int length = jArray.length();
                                    for (int i = 0; i < length; i++) {
                                        try {
                                            String songId = jArray.getJSONObject(i).getJSONArray("tracks").getJSONObject(0).getString("foreign_id");
                                            spotifyURIList.add(songId);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (length == 0) {
                                        Toast.makeText(bindedActivity, "Found no songs!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if (spotifyURIList.isEmpty()) {
                                //queryAndPlay(pace - 20);
                            } else {
                                play(spotifyURIList);
                            }

                        }
                    }.execute(buildEchoNestRequest(pace, style));
                }
            }
        });

    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        //Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        //Log.d("MainActivity", "Received connection message: " + message);
    }

    public String buildEchoNestSpotifyTrackInfo(String spotifyURI){
        return "http://developer.echonest.com/api/v4/song/profile?api_key=" + ECHONEST_KEY +
            "&track_id=" + spotifyURI + "&bucket=id:spotify";
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        if(eventType == EventType.TRACK_CHANGED){
            new RequestHttpTask() {

                @Override
                protected void onPostExecute(String result) {
                    super.onPostExecute(result);

                    //getSong
                    try {
                        JSONObject jObject = new JSONObject(result);
                        JSONObject jsonSong = jObject.getJSONObject("response").getJSONArray("songs").getJSONObject(0);

                        String trackName = jsonSong.getString("title");
                        String artist = jsonSong.getString("artist_name");

                        bindedActivity.updateTrackInfo(trackName + " by " + artist);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.execute(buildEchoNestSpotifyTrackInfo(playerState.trackUri));
        }
        //Log.d("MainActivity", "Playback event received: " + eventType.name());
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d("MainActivity", "Playback error received: " + errorType.name());
    }

}
