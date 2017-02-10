/*
 * Copyright (C) 2016-17 Andreas Kromke, andreas.kromke@gmail.comn
 *
 * This program is free software; you can redistribute it or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.kromke.andreas.unpopmusicplayerfree;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * activity screen that presents all the tracks belonging to one album
 */
public class TracksOfAlbumActivity extends AppCompatActivity
        implements ServiceConnection, MediaPlayerControl,
        MediaPlayService.MediaPlayServiceControl, MyListView.OnNoItemClickListener
{
    private MyListView mAudioTrackView;            // custom list view!
    static private int mCurrTrackNo = -1;                // must be static to be preserved during "destroy"
    private AudioTrackAdapter mAudioTrackAdapter;

    //service
    static private boolean mServiceStarted = false;
    private MediaPlayService mMediaPlayService = null;
    private Intent mPlayIntent;

    //m_MediaController (showing the prev/next/play/pause buttons inside the activity window)
    private MyMediaController m_MediaController;

    //activity and playback pause flags
    private boolean m_ActivityPaused = false;
    static private boolean buffered_play_state = false;
    private int buffered_position = 0;
    private int buffered_duration = 0;
    private int headerClicked = 0;
    private int origHeaderHeight, origTitleHeight, origImageWidth;

    //preferences
    private int mAutoplayMode;
    private int mChooseOtherTrackMode;
    private int mAutohideModeMs;
    private int mBehaviourOfBackKeyMode;


    /* ########################
     * ### activity methods ###
     * ########################
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d("TracksOfAlbumActivity", "onCreate()");
        super.onCreate(savedInstanceState);

        UserSettings.setStatusBarColour(this);

        setContentView(R.layout.activity_tracks_of_album);

        TextView textAlbumTitle = (TextView) findViewById(R.id.tracks_album_title);
        //TextView textAlbumInfo = (TextView) findViewById(R.id.tracks_album_info);
        ImageView imageAlbum = (ImageView) findViewById(R.id.tracks_album_image);

        // workaround for wrong number of tracks
        if (AppGlobals.currAlbum.album_no_of_tracks != AppGlobals.getAudioAlbumTrackListSize())
        {
            Log.w("TracksOfAlbumActivity", "onCreate() : number of album tracks wrong for album" + AppGlobals.currAlbum.album_name);
            AppGlobals.currAlbum.album_no_of_tracks = AppGlobals.getAudioAlbumTrackListSize();
        }
        String trackText = (AppGlobals.currAlbum.album_no_of_tracks == 1) ? getString(R.string.str_track) : getString(R.string.str_tracks);
        trackText = "(" + AppGlobals.currAlbum.album_no_of_tracks + " " + trackText + ")";

        //textAlbumTitle.setText(AppGlobals.currAlbum.album_name);
        final String theHtmlString = "<body><h3>" + AppGlobals.currAlbum.album_name + "</h3>" +
                "<font color=\"#C8C8C8\">" + trackText + "</font>"
                + "</body>";
        textAlbumTitle.setText(Html.fromHtml(theHtmlString));
        Log.d("TracksOfAlbumActivity", "onCreate() : line count = " + textAlbumTitle.getLineCount());

//        textAlbumInfo.setText(trackText);
        if (!AppGlobals.currAlbum.album_picture_path.isEmpty())
        {
            Drawable d = Drawable.createFromPath(AppGlobals.currAlbum.album_picture_path);
            if (d != null)
            {
                imageAlbum.setImageDrawable(d);
            }
        }

        ViewGroup.LayoutParams params;
        /*
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int imageAlbumSize = Integer.parseInt(sharedPref.getString("prefSizeOfAlbumHeader", "120"));
        */
        UserSettings.SetContext(this);
        int imageAlbumSize = UserSettings.getVal(UserSettings.PREF_SIZE_OF_ALBUM_HEADER, 120);
        boolean showTrackNumbers = UserSettings.getBool(UserSettings.PREF_SHOW_TRACK_NUMBER, false);
        int showConductorMode = UserSettings.getVal(UserSettings.PREF_SHOW_CONDUCTOR, 0);
        boolean showSubtitle = UserSettings.getBool(UserSettings.PREF_SHOW_SUBTITLE, false);
        int genderismMode = UserSettings.getVal(UserSettings.PREF_GENDERISM_NONSENSE, 0);
        mAutoplayMode = UserSettings.getVal(UserSettings.PREF_AUTOPLAY_MODE, 3);
        mChooseOtherTrackMode = UserSettings.getVal(UserSettings.PREF_CHOOSE_OTHER_TRACK, 0);
        mBehaviourOfBackKeyMode = UserSettings.getVal(UserSettings.PREF_BEHAVIOUR_OF_BACK_KEY, 0);
        mAutohideModeMs = UserSettings.getVal(UserSettings.PREF_AUTOHIDE_CONTROLLER, 0);
        if (mAutohideModeMs < 10)
        {
            mAutohideModeMs *= 1000;   // HACK: old settings
        }


        RelativeLayout albumHeader = (RelativeLayout) findViewById(R.id.tracks_album_header);
        params = albumHeader.getLayoutParams();
        params.height += imageAlbumSize - 120;
        albumHeader.setLayoutParams(params);
        albumHeader.setBackgroundColor(UserSettings.getColourFromResId(R.color.track_list_header_background));

        params = imageAlbum.getLayoutParams();
        params.width += imageAlbumSize - 120;
        imageAlbum.setLayoutParams(params);
        imageAlbum.setBackgroundColor(UserSettings.getColourFromResId(R.color.track_list_header_background));

        params = textAlbumTitle.getLayoutParams();
        params.height += imageAlbumSize - 120;        //TODO: HACK
        textAlbumTitle.setLayoutParams(params);
        textAlbumTitle.setBackgroundColor(UserSettings.getColourFromResId(R.color.track_list_header_background));
        textAlbumTitle.setMovementMethod(new ScrollingMovementMethod());



        //TextView albumInfo = (TextView) findViewById(R.id.tracks_album_info);
        //albumInfo.setBackgroundColor(UserSettings.getColourFromResId(R.color.track_list_header_background));

        /*
        ActionBar ab = getSupportActionBar();
        if (ab != null)
        {
            if (!AppGlobals.currAlbum.album_picture_path.isEmpty())
            {
                Log.d("TracksOfAlbumActivity", "onCreate() : art path \"" + AppGlobals.currAlbum.album_picture_path + "\"");
                Drawable d = Drawable.createFromPath(AppGlobals.currAlbum.album_picture_path);
                if (d != null)
                {
                    Rect r = d.copyBounds();
                    Log.d("TracksOfAlbumActivity", "onCreate() : rect (w = " + r.width() + ", h = " + r.height() + ")");
                    // get size of underlying bitmap:
                    Log.d("TracksOfAlbumActivity", "onCreate() : rect (wi = " + d.getIntrinsicWidth() + ", hi = " + d.getIntrinsicHeight() + ")");
                    Log.d("TracksOfAlbumActivity", "onCreate() : try to set icon");
                    ab.setDisplayUseLogoEnabled(false);
                    ab.setDisplayShowHomeEnabled(true);
                    ab.setLogo(d);
                    ab.setIcon(d);
                    //ab.setBackgroundDrawable(d);
                }
                else
                {
                    Log.e("TracksOfAlbumActivity", "onCreate() : cannot create Drawable");
                }
            }
            else
            {
                Log.d("TracksOfAlbumActivity", "onCreate() : no album art path");
            }
            ab.setTitle(AppGlobals.currAlbum.album_name);
            ab.setSubtitle("(" + AppGlobals.currAlbum.album_no_of_tracks + ((AppGlobals.currAlbum.album_no_of_tracks == 1) ? " track)" : " tracks)"));
        }
        */

        //retrieve list view
        mAudioTrackView = (MyListView) findViewById(R.id.audioTrack_list);
        mAudioTrackView.setBackgroundColor(UserSettings.getColourFromResId(R.color.track_list_background_normal));
        // special handling for a custom ListView that passes clicks to non-elements
        mAudioTrackView.setOnNoItemClickListener(this);

        //create and set adapter
        mAudioTrackAdapter = new AudioTrackAdapter(
                this,
                AppGlobals.audioAlbumTrackList,
                mAutohideModeMs == 0,
                showTrackNumbers,
                showConductorMode, showSubtitle, genderismMode);
        mAudioTrackAdapter.setCurrTrack(mCurrTrackNo);      // currTrackNo is static, preserved during destroy/create
        mAudioTrackView.setAdapter(mAudioTrackAdapter);

        // this is used for communication with the service
        mPlayIntent = new Intent(this, MediaPlayService.class);

        //setup m_MediaController
        // after screen rotation the media controller should be made visible,
        // but this is not possible even in resume, because of bad Android design
        createMusicController();

        // we should bind at Start and unbind at Stop, but try to do with create/destroy
        BindToService();
    }

    /**************************************************************************
     *
     * called after onCreate() or after the application has been put back to
     * the foreground.
     * After onStart() the method onResume() is called.
     *
     * onStart() binds to the service, onStop() unbinds.
     *
     *************************************************************************/
    @Override
    protected void onStart()
    {
        Log.d("TracksOfAlbumActivity", "onStart()");

        super.onStart();
        // we should bind at Start and unbind at Stop, but try to do with create/destroy
        //BindToService();
    }

    @Override
    protected void onResume()
    {
        Log.d("TracksOfAlbumActivity", "onResume()");
        super.onResume();
        if (m_ActivityPaused)
        {
            m_ActivityPaused = false;
            // TODO: in case we hided the controller in onStop() we have to reveal it here
        }
    }

    @Override
    public void onAttachedToWindow()
    {
        Log.d("TracksOfAlbumActivity", "onAttachedToWindow()");
        super.onAttachedToWindow();
        if ((mCurrTrackNo >= 0) && (m_MediaController != null) && !(m_MediaController.isShowing()))
        {
            // media controller should be visible, but is not. Show it.
            m_MediaController.show(mAutohideModeMs);
        }
    }

    /**************************************************************************
     *
     * Called before the application is put to the background.
     * After onPause() the method onStop() is called.
     *
     *************************************************************************/
    @Override
    protected void onPause()
    {
        Log.d("TracksOfAlbumActivity", "onPause()");
        super.onPause();
        m_ActivityPaused = true;
    }

    @Override
    protected void onStop()
    {
        Log.d("TracksOfAlbumActivity", "onStop()");
        if (m_MediaController != null)
        {
            // TODO: it might be wise to hide the controller here in order to save energy
        }

        //UnbindFromService();

        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        Log.d("TracksOfAlbumActivity", "onDestroy()");

        UnbindFromService();
        if (isFinishing())
        {
            if (mServiceStarted)
            {
                // only stop service in case app is finishing
                stopService(mPlayIntent);
                mServiceStarted = false;
            }

            mCurrTrackNo = -1;
        }

        if (m_MediaController != null)
        {
            m_MediaController.reallyHide();
            m_MediaController.setEnabled(false);
            m_MediaController = null;
        }

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.d("TracksOfAlbumActivity", "onCreateOptionsMenu()");
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.tracks_of_album_action_bar, menu);

        return true;
    }
/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        //menu item selected
        switch (item.getItemId())
        {
            case R.id.action_end:
                Log.d("TracksOfAlbumActivity", "onOptionsItemSelected() : end");
                if (mCurrTrackNo >= 0)
                {
                    StopPlaying();
                }
                //unbindService(musicConnection);
                //mMediaPlayService = null;
                //finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
*/
    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        int keyCode = event.getKeyCode();
        int actionCode = event.getAction();
        Log.d("TracksOfAlbumActivity", "dispatchKeyEvent() : keyCode = " + keyCode);
        //final boolean uniqueDown = (event.getRepeatCount() == 0) && (event.getAction() == KeyEvent.ACTION_DOWN);
        //if (uniqueDown)
        {
            if ((keyCode == KeyEvent.KEYCODE_BACK) && (mCurrTrackNo >= 0))
            {
                // currently playing. Stop on key release, but also consume key down event
                if (actionCode == KeyEvent.ACTION_UP)
                {
                    if (mBehaviourOfBackKeyMode == 0)
                    {
                        if ((mAutohideModeMs != 0) && (m_MediaController != null) && (!m_MediaController.isShowing()))
                        {
                            // show hidden controls
                            m_MediaController.show(mAutohideModeMs);
                        }
                        else
                        {
                            // controls are visible
                            StopPlaying();
                        }
                    }
                    else
                    if (mBehaviourOfBackKeyMode == 1)
                    {
                        if (buffered_play_state)
                        {
                            pause();
                        }
                        else
                        {
                            StopPlaying();
                        }
                    }
                    else
                    if (mBehaviourOfBackKeyMode == 2)
                    {
                        StopPlaying();
                    }
                }
                return true;    // event was consumed
            }
            else
            if (keyCode == KeyEvent.KEYCODE_MENU)
            {
                // reserved for future use
            }
        }

        return super.dispatchKeyEvent(event);
    }



    private int changeViewWidthAbsOrRelative(View v, int wabs, int wdelta)
    {
        int wprev;
        ViewGroup.LayoutParams params = v.getLayoutParams();
        wprev = params.width;
        if (wabs >= 0)
            params.width = wabs;
        else
            params.width += wdelta;
        v.setLayoutParams(params);
        return wprev;
    }

    private int changeViewHeightAbsOrRelative(View v, int habs, int hdelta)
    {
        int hprev;
        ViewGroup.LayoutParams params = v.getLayoutParams();
        hprev = params.height;
        if (habs >= 0)
            params.height = habs;
        else
            params.height += hdelta;
        v.setLayoutParams(params);
        return hprev;
    }

    private int changeViewGroupHeightAbsOrRelative(ViewGroup v, int habs, int hdelta)
    {
        int hprev;
        ViewGroup.LayoutParams params = v.getLayoutParams();
        hprev = params.height;
        if (habs >= 0)
            params.height = habs;
        else
            params.height += hdelta;
        v.setLayoutParams(params);
        return hprev;
    }

    public void onAlbumImageClicked(View v)
    {
        Log.d("TracksOfAlbumActivity", "onAlbumImageClicked()");

        TextView textAlbumTitle = (TextView) findViewById(R.id.tracks_album_title);
        Log.d("TracksOfAlbumActivity", "onStart() : line count = " + textAlbumTitle.getLineCount());

        RelativeLayout albumHeader = (RelativeLayout) findViewById(R.id.tracks_album_header);
        TextView albumTitle = (TextView) findViewById(R.id.tracks_album_title);
        ImageView albumImage = (ImageView) findViewById(R.id.tracks_album_image);
        if (headerClicked == 0)
        {
            // shrink
            origHeaderHeight = changeViewGroupHeightAbsOrRelative(albumHeader, -1, -40);
            origTitleHeight = changeViewHeightAbsOrRelative(albumTitle, -1, -40);
            origImageWidth = changeViewWidthAbsOrRelative(albumImage, -1, -40);
            headerClicked++;
        }
        else
        if (headerClicked == 3)
        {
            // sizes back to normal
            changeViewGroupHeightAbsOrRelative(albumHeader, origHeaderHeight, 0);
            changeViewHeightAbsOrRelative(albumTitle, origTitleHeight, 0);
            changeViewWidthAbsOrRelative(albumImage, origImageWidth, 0);
            headerClicked = 0;
        }
        else
        if (headerClicked == 2)
        {
            // shrink
            changeViewGroupHeightAbsOrRelative(albumHeader, -1, -30);
            changeViewHeightAbsOrRelative(albumTitle, -1, -30);
            changeViewWidthAbsOrRelative(albumImage, -1, -30);
            headerClicked++;
        }
        else
        {
            // shrink
            changeViewGroupHeightAbsOrRelative(albumHeader, -1, -40);
            changeViewHeightAbsOrRelative(albumTitle, -1, -40);
            changeViewWidthAbsOrRelative(albumImage, -1, -40);
            headerClicked++;
        }
    }


    /* ####################################
     * ### MediaPlayerControl callbacks ###
     * ####################################
     */

    @Override
    public boolean canPause()
    {
        return true;
    }

    @Override
    public boolean canSeekBackward()
    {
        return true;
    }

    @Override
    public boolean canSeekForward()
    {
        return true;
    }

    @Override
    public int getAudioSessionId()
    {
        return 0;
    }

    @Override
    public int getBufferPercentage()
    {
        return 0;
    }

    @Override
    public int getCurrentPosition()
    {
        Log.v("TracksOfAlbumActivity", "getCurrentPosition()");
        if ((mMediaPlayService != null) && (mMediaPlayService.isPlaying()))
        {
            buffered_position = mMediaPlayService.getPosition();
        }
        updateControllerAndList();

        return buffered_position;
    }

    @Override
    public int getDuration()
    {
        Log.v("TracksOfAlbumActivity", "getDuration()");
        if ((mMediaPlayService != null) && (mMediaPlayService.isPlaying()))
        {
            buffered_duration = mMediaPlayService.getDuration();
        }
//        updateControllerAndList();

        return buffered_duration;
    }

    @Override
    public boolean isPlaying()
    {
        if ((mMediaPlayService != null) && (mMediaPlayService.isPlaying()))
        {
            buffered_play_state = mMediaPlayService.isPlaying();
        }
        Log.v("TracksOfAlbumActivity", "isPlaying() => " + buffered_play_state);

        return buffered_play_state;
    }

    // called when the media m_MediaController PAUSE button had been pressed
    @Override
    public void pause()
    {
        Log.d("TracksOfAlbumActivity", "pause()");
        buffered_play_state = false;   // HACK
        mMediaPlayService.pausePlayer();
    }

    @Override
    public void seekTo(int pos)
    {
        Log.d("TracksOfAlbumActivity", "seekTo()");
        buffered_position = pos;
        mMediaPlayService.seek(pos);
    }

    // called when the media m_MediaController PLAY button had been pressed
    @Override
    public void start()
    {
        Log.v("TracksOfAlbumActivity", "start()");
        if (mMediaPlayService != null)
        {
            mMediaPlayService.startPlayer();
        }
    }

    // called when the media m_MediaController PLAY_NEXT button had been pressed
    private void playNext()
    {
        Log.d("TracksOfAlbumActivity", "playNext()");
        gotoTrackRel(1);
    }

    // called when the media m_MediaController PLAY_PREV button had been pressed
    private void playPrev()
    {
        Log.d("TracksOfAlbumActivity", "playPrev()");
        gotoTrackRel(-1);
    }


    /* ##############################
     * ### MediaPlayService callbacks ###
     * ##############################
     */

    @Override
    public void onAdvanceToNextTrack(int trackNumber)
    {
        Log.d("TracksOfAlbumActivity", "onAdvanceToNextTrack()");
        if (mCurrTrackNo < 0)
            return;     // should not happen?!?

        if (mAutoplayMode == 3)
        {
            // stop after album
            playNext();
        }
        else
        if (mAutoplayMode == 2)
        {
            // stop after group
            AudioTrack theTrack = AppGlobals.audioAlbumTrackList.get(mCurrTrackNo);
            if ((theTrack != null) && (!theTrack.group_last))
            {
                // current track was not the last in group
                playNext();
            }
            else
            {
                // stop
                gotoTrack(-1);
            }
        }
    }


    /* ########################
     * ### helper functions ###
     * ########################
     */

    private void BindToService()
    {
        if (!mServiceStarted)
        {
            // this is the first start. Start background service.
            // make sure service keeps running when activity is unbound
            startService(mPlayIntent);
            mServiceStarted = true;
        }

        // bind in order to be able to control the service
        bindService(mPlayIntent, this, Context.BIND_AUTO_CREATE);
    }

    private void UnbindFromService()
    {
        unbindService(this);
        mMediaPlayService = null;
    }

/*
    // variable used to connect to the service
    private ServiceConnection musicConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            Log.d("TracksOfAlbumActivity", "onServiceConnected()");
            MediaPlayService.MediaPlayServiceBinder binder = (MediaPlayService.MediaPlayServiceBinder) service;
            //get service
            mMediaPlayService = binder.getService();
            //pass list
            mMediaPlayService.setList(AppGlobals.audioAlbumTrackList);
            mMediaPlayServiceIsBound = true;
            // register for callbacks
            mMediaPlayService.setController(this);     // TODO: do this only once TODO: avoid hack
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            Log.d("TracksOfAlbumActivity", "onServiceDisconnected()");
            mMediaPlayServiceIsBound = false;
        }
    };
*/
    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        Log.d("TracksOfAlbumActivity", "onServiceConnected()");
        MediaPlayService.MediaPlayServiceBinder binder = (MediaPlayService.MediaPlayServiceBinder) service;
        //get service
        mMediaPlayService = binder.getService();
        //pass list
        mMediaPlayService.setList(AppGlobals.audioAlbumTrackList);
        // register for callbacks
        mMediaPlayService.setController(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {
        Log.d("TracksOfAlbumActivity", "onServiceDisconnected()");
        mMediaPlayService = null;
    }

    //set the m_MediaController up
    private void createMusicController()
    {
        Log.d("TracksOfAlbumActivity", "createMusicController()");
        m_MediaController = new MyMediaController(this, mAutohideModeMs != 0);
        //set previous and next button listeners
        m_MediaController.setPrevNextListeners(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                playNext();
            }
        }, new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                playPrev();
            }
        });
        //set and show
        m_MediaController.setMediaPlayer(this);
        View v;
        /*
        v = findViewById(R.id.tracks_album_header);
        Log.d("TracksOfAlbumActivity", "createMusicController() : header w = " + v.getMeasuredWidth() + " h = " + v.getMeasuredHeight());
        v = findViewById(R.id.audioTrack_all);
        Log.d("TracksOfAlbumActivity", "createMusicController() : space  w = " + v.getMeasuredWidth() + " h = " + v.getMeasuredHeight());
        */
        v = findViewById(R.id.audioTrack_list);
        //Log.d("TracksOfAlbumActivity", "createMusicController() : list   w = " + v.getMeasuredWidth() + " h = " + v.getMeasuredHeight());
        m_MediaController.setAnchorView(v);
        m_MediaController.setEnabled(true);
    }

    public View getViewByPosition(int pos, ListView listView)
    {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition)
        {
            return listView.getAdapter().getView(pos, null, listView);
        } else
        {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    // update list with selection colour etc
    private void updateList(int newTrackNo)
    {
        mAudioTrackAdapter.setCurrTrack(newTrackNo);
        if (mCurrTrackNo >= 0)
        {
            View v = getViewByPosition(mCurrTrackNo, mAudioTrackView);
            if (v != null)
            {
                // back to normal
                v.setBackgroundColor(UserSettings.getColourFromResId(R.color.track_list_background_normal));
            }
        }
        if (newTrackNo >= 0)
        {
            View v = getViewByPosition(newTrackNo, mAudioTrackView);
            if (v != null)
            {
                // selected
                v.setBackgroundColor(UserSettings.getColourFromResId(R.color.track_list_background_selected));
            }
        }
        if (newTrackNo != mCurrTrackNo)
        {
            if (newTrackNo >= 0)
            {
                mAudioTrackView.smoothScrollToPosition(newTrackNo);
            }
        }
        mCurrTrackNo = newTrackNo;
    }

    // update list and player controls
    private void updateControllerAndList()
    {
        //Log.d("TracksOfAlbumActivity", "updateControllerAndList()");
        if (mMediaPlayService != null)
        {
            int newTrackNo = mMediaPlayService.getAudioTrackArrayNumber();
            if ((mCurrTrackNo >= 0) && (newTrackNo < 0) && (m_MediaController != null))
            {
                // hide the controller overlay view
                m_MediaController.reallyHide();
            }
            if (mCurrTrackNo != newTrackNo)
            {
                updateList(newTrackNo);
            }
        }
    }

    // for skip forward and backward
    public void gotoTrack(int newTrackNo)
    {
        Log.d("TracksOfAlbumActivity", "gotoTrack(" + newTrackNo + ") -- mCurrTrackNo == " + mCurrTrackNo);
        mMediaPlayService.setAudioTrack(newTrackNo);
        if ((mCurrTrackNo >= 0) && (newTrackNo < 0))
        {
            // hide the controller overlay view
            m_MediaController.reallyHide();
            mMediaPlayService.stopPlayer();
        }
        updateList(newTrackNo);

        // now mCurrTrackNo has been updated with newTrackNo
        if (mCurrTrackNo >= 0)
        {
            buffered_play_state = true;
            Log.d("TracksOfAlbumActivity", "gotoTrack() : buffered_play_state set to true");
            mMediaPlayService.playAudioTrack();
            m_MediaController.show(mAutohideModeMs);
        }
    }

    // for skip forward (deltaTrack = 1) and backward (deltaTrack = -1)
    public void gotoTrackRel(int deltaTrack)
    {
        buffered_position = 0;
        buffered_duration = 0;
        int newTrackNo = mMediaPlayService.playSkip(deltaTrack, false /* do not wrap */);
        gotoTrack(newTrackNo);
    }

    public void StopPlaying()
    {
        buffered_play_state = false;
        mMediaPlayService.stopPlayer();
        if (m_MediaController != null)
        {
            m_MediaController.reallyHide();
        }
        //stopService(mPlayIntent); ???
        updateList(-1); // new active track number is invalid
    }

    // note that this will never be called once we call setMovementMethod() for the album title view
    public void onSpacePicked(View view)
    {
        Log.d("TracksOfAlbumActivity", "onSpacePicked()");
        if (mAutohideModeMs != 0)
        {
            if (mCurrTrackNo >= 0)
            {
                // currently playing
                if (m_MediaController != null)
                {
                    m_MediaController.show(mAutohideModeMs);
                }
            }
        }
    }

    @Override
    // callback for custom ListView that reports clicks to empty space
    public void onNoItemClicked()
    {
        Log.d("TracksOfAlbumActivity", "onNoItemClicked()");
        if (mAutohideModeMs != 0)
        {
            if (mCurrTrackNo >= 0)
            {
                // currently playing
                if (m_MediaController != null)
                {
                    m_MediaController.show(mAutohideModeMs);
                }
            }
        }
    }

    // callback for user track select (as defined in "audio_track.xml")
    public void onTrackPicked(View view)
    {
        int trackNo = Integer.parseInt(view.getTag().toString());
        Log.d("TracksOfAlbumActivity", "onTrackPicked(" + trackNo + ")");
        boolean trackValid = (trackNo >= 0) && (trackNo < AppGlobals.currAlbum.album_no_of_tracks);
//        mMediaPlayService.setController(this);     // TODO: do this only once

        if (mAutohideModeMs != 0)
        {
            // media controller is automatically hidden after some time

            if (mCurrTrackNo >= 0)
            {
                // currently playing
                if (m_MediaController != null)
                {
                    m_MediaController.show(mAutohideModeMs);
                }
            }
            else
            {
                // play new track
                if (trackValid)
                {
                    gotoTrack(trackNo);
                }
            }
        }
        else
        {
            // media controller remains visible during playback
/*
            if ((mCurrTrackNo >= 0) && (m_MediaController != null) && !(m_MediaController.isShowing()))
            {
                // media controller should be visible, but is not. Show it.
                m_MediaController.show(mAutohideModeMs);
            }
            else */
            if (mChooseOtherTrackMode == 0)
            {
                // play selected track
                if (trackValid)
                    gotoTrack(trackNo);
            }
            else
            if (mChooseOtherTrackMode == 1)
            {
                // pause
                if (mCurrTrackNo >= 0)
                    pause();
            }
            else if (mChooseOtherTrackMode == 2)
            {
                // stop
                if (mCurrTrackNo >= 0)
                    StopPlaying();
            }
        }
    }
}
