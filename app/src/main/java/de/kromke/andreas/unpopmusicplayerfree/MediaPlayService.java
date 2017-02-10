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

import java.util.ArrayList;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

/*
 * A service keeps running while our application is
 * suspended. Handles MediaPlayer and Notifications.
 */

public class MediaPlayService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener
{
    // interface (in fact kind a set of callback functions)
    MediaPlayServiceControl mMediaPlayServiceController = null;
    //media m_TheMediaPlayer
    private MediaPlayer m_TheMediaPlayer;
    //song list
    private ArrayList<AudioTrack> m_audio_tracks;
    //current position
    private int m_AudioTrackArrayNumber;
    //binder
    private final IBinder m_MediaPlayerBind = new MediaPlayServiceBinder();
    //title of current audio file
    private String m_audioInformationFirstLine = "";
    private String m_audioInformationSecondLine = "";
    private String m_audioInformationThirdLine = "";
    //notification id
    private static final int NOTIFY_ID = 1;

    /*
    * callback mechanism
     */
    public interface MediaPlayServiceControl
    {
        void onAdvanceToNextTrack(int trackNumber);
    }

    /* #######################
     * ### service methods ###
     * #######################
     */

    /**************************************************************************
     *
     * Service function
     *
     *************************************************************************/
    public void onCreate()
    {
        //create the service
        super.onCreate();
        //initialize position
        m_AudioTrackArrayNumber = 0;
        //create m_TheMediaPlayer
        m_TheMediaPlayer = new MediaPlayer();
        //initialize
        InitMediaPlayer();
    }

    /**************************************************************************
     *
     * Service function
     *
     *************************************************************************/
    @Override
    public void onDestroy()
    {
        Log.d("MediaPlayService", "onDestroy()");
        if (m_TheMediaPlayer.isPlaying())
        {
            m_TheMediaPlayer.stop();
        }
        m_TheMediaPlayer.release();

        stopForeground(true);
    }

    /**************************************************************************
     *
     * Service function: will bind to service
     *
     *************************************************************************/
    @Override
    public IBinder onBind(Intent intent)
    {
        Log.d("MediaPlayService", "onBind()");
        return m_MediaPlayerBind;
    }

    /**************************************************************************
     *
     * Service function: release resources
     *
     *************************************************************************/
    @Override
    public boolean onUnbind(Intent intent)
    {
        Log.d("MediaPlayService", "onUnbind()");
        mMediaPlayServiceController = null;
        /*
        if (m_TheMediaPlayer.isPlaying())
        {
            m_TheMediaPlayer.stop();
        }
        m_TheMediaPlayer.release();
        */
        return false;
    }


    /**************************************************************************
     *
     * called once from onCreate()
     *
     *************************************************************************/
    public void InitMediaPlayer()
    {
        //set m_TheMediaPlayer properties
        m_TheMediaPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        m_TheMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //set listeners
        m_TheMediaPlayer.setOnPreparedListener(this);
        m_TheMediaPlayer.setOnCompletionListener(this);
        m_TheMediaPlayer.setOnErrorListener(this);
    }


    /* ###################################################
     * ### methods called from activity, i.e. commands ###
     * ###################################################
     */

    /**************************************************************************
     *
     * pass audio track list
     *
     *************************************************************************/
    public void setList(ArrayList<AudioTrack> theAudioTracks)
    {
        m_audio_tracks = theAudioTracks;
    }

    //binder
    public class MediaPlayServiceBinder extends Binder
    {
        MediaPlayService getService()
        {
            return MediaPlayService.this;
        }
    }


    /**************************************************************************
     *
     * play the current audio track
     *
     *************************************************************************/
    public void playAudioTrack()
    {
        //play
        m_TheMediaPlayer.reset();

        //get audio track information
        AudioTrack currAudioTrack = m_audio_tracks.get(m_AudioTrackArrayNumber);

        // set information lines
        if (!currAudioTrack.composer.isEmpty())
        {
            m_audioInformationFirstLine = currAudioTrack.composer + ":";
        }
        else
        {
            m_audioInformationFirstLine = getString(R.string.str_unknown_composer);
        }

        if (!currAudioTrack.grouping.isEmpty())
        {
            m_audioInformationSecondLine = currAudioTrack.grouping;
            m_audioInformationThirdLine = currAudioTrack.title;
        }
        else
        {
            m_audioInformationSecondLine = currAudioTrack.title;
            m_audioInformationThirdLine = "";
        }

        //get id
        long currAudioTrackId = currAudioTrack.id;
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currAudioTrackId);
        //set the data source
        try
        {
            m_TheMediaPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e)
        {
            Log.e("MediaPlayService", "Error on setDataSource", e);
        }

        // start asynchronously, later onPrepared() will be called

        try
        {
            m_TheMediaPlayer.prepareAsync();
        } catch (Exception e)
        {
            Log.e("MediaPlayService", "Error on prepareAsync()", e);
        }
    }

    // set the callback functions
    public void setController(MediaPlayServiceControl controller)
    {
        mMediaPlayServiceController = controller;
    }


    /**************************************************************************
     *
     * pass the number of the current audio track
     *
     *************************************************************************/
    public void setAudioTrack(int audioTrackIndex)
    {
        m_AudioTrackArrayNumber = audioTrackIndex;
    }


    /**************************************************************************
     *
     * playback methods
     *
     *************************************************************************/
    public int getPosition()
    {
        return m_TheMediaPlayer.getCurrentPosition();
    }

    public int getDuration()
    {
        return m_TheMediaPlayer.getDuration();
    }

    public boolean isPlaying()
    {
        return m_TheMediaPlayer.isPlaying();
    }

    public int getAudioTrackArrayNumber() { return m_AudioTrackArrayNumber; }

    public void pausePlayer()
    {
        // change notification to show PAUSE instead of PLAY icon
        setNotification(true /* pause */);

        try
        {
            m_TheMediaPlayer.pause();
        } catch (Exception e)
        {
            Log.e("MediaPlayService", "Error on pause()", e);
        }
    }

    public void stopPlayer()
    {
        // remove notification
        stopForeground(true);

        try
        {
            m_TheMediaPlayer.pause();
        } catch (Exception e)
        {
            Log.e("MediaPlayService", "Error on pause()", e);
        }
    }

    public void seek(int posn)
    {
        m_TheMediaPlayer.seekTo(posn);
    }

    public void startPlayer()
    {
        // change notification to show PLAY instead of PAUSE icon
        setNotification(false /* play */);

        try
        {
            m_TheMediaPlayer.start();
        } catch (Exception e)
        {
            Log.e("MediaPlayService", "Error on start()", e);
        }
    }

    //skip to next (delta = 1) or previous (delta = -1) track, but do not play, yet
    public int playSkip(int delta, boolean wrap)
    {
        Log.d("MediaPlayService", "playSkip() -- " + m_AudioTrackArrayNumber + " -> " + (m_AudioTrackArrayNumber + delta));
        m_AudioTrackArrayNumber += delta;
        if (wrap)
        {
            // keep track number in valid range
            if (m_AudioTrackArrayNumber < 0)
            {
                m_AudioTrackArrayNumber = m_audio_tracks.size() - 1;
            }
            if (m_AudioTrackArrayNumber >= m_audio_tracks.size())
            {
                m_AudioTrackArrayNumber = 0;
            }
        }

        if ((m_AudioTrackArrayNumber < 0) || (m_AudioTrackArrayNumber >= m_audio_tracks.size()))
        {
            // invalid
            m_AudioTrackArrayNumber = -1;
        }

        return m_AudioTrackArrayNumber;
    }


    /* ###########################################
     * ### callback functions from MediaPlayer ###
     * ###########################################
     */

    // asynchronous start
    @Override
    public void onPrepared(MediaPlayer mp)
    {
        //start playback
        try
        {
            mp.start();
        } catch (Exception e)
        {
            Log.e("MediaPlayService", "Error on start()", e);
        }
        setNotification(false /* play */);
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        Log.d("MediaPlayService", "onCompletion()");
        //check if playback has reached the end of a track
    //    if (m_TheMediaPlayer.getCurrentPosition() > 0)
        {
            mp.reset();
            // remove notification
            stopForeground(true);
     //       playSkip(1, false  /* do not wrap */);
     //       if (m_AudioTrackArrayNumber >= 0)
            {
                if (mMediaPlayServiceController == null)
                {
                    Log.e("MediaPlayService", "onCompletion() -- controller lost");
                }
                else
                {
                    mMediaPlayServiceController.onAdvanceToNextTrack(m_AudioTrackArrayNumber);
                }
                //playAudioTrack();
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra)
    {
        Log.v("MediaPlayService", "onError()");
        mp.reset();
        return false;
    }


    /* ########################
     * ### helper functions ###
     * ########################
     */


    /**************************************************************************
     *
     * set notification or update current one, if any
     *
     *************************************************************************/
    private void setNotification(boolean isPause)
    {
        Intent notificationIntent = new Intent(this, TracksOfAlbumActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)                       // intend to be sent when the notification is clicked on
                .setSmallIcon(
                        (isPause) ?
                        R.drawable.pause :
                        R.drawable.play)
                .setTicker(m_audioInformationSecondLine)
                .setOngoing(true)                               // notification cannot be dismissed
                .setShowWhen(false)                             // do not show time of the day when notification was sent
                .setContentTitle(m_audioInformationFirstLine)
                .setContentText(m_audioInformationSecondLine)
                .setSubText(m_audioInformationThirdLine);
        Notification not = builder.build();
        startForeground(NOTIFY_ID, not);
    }

}
