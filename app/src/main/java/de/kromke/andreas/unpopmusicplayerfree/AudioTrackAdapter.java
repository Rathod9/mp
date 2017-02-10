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

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Fills the audio track list with data
 */

class AudioTrackAdapter extends BaseAdapter
{
    private Context mContext;
    // settings
    private boolean mShowTrackNumbers;
    private int mShowConductorMode;
    private boolean mShowSubtile;
    private int mGenderismMode;

    //song list and layout
    private ArrayList<AudioTrack> mAudioTracks;
    private LayoutInflater mAudioTrackInf;
    private int currTrack = -1;
    private boolean mAddDummyTrack;

    //constructor
    AudioTrackAdapter(
            Context c,
            ArrayList<AudioTrack> theAudioTracks,
            boolean addDummyTrack,
            boolean showTrackNumbers,
            int showConductorMode,
            boolean showSubtitle,
            int genderismMode)
    {
        mContext = c;
        mAudioTracks = theAudioTracks;
        mAddDummyTrack = addDummyTrack;
        mAudioTrackInf = LayoutInflater.from(mContext);
        mShowTrackNumbers = showTrackNumbers;
        mShowConductorMode = showConductorMode;
        mShowSubtile = showSubtitle;
        mGenderismMode = genderismMode;
    }

    void setCurrTrack(int curr)
    {
        Log.d("AudioTrackAdapter", "setCurrTrack(" + curr + ")");
        currTrack = curr;
    }

    @Override
    public int getCount()
    {
        return (mAddDummyTrack) ? mAudioTracks.size() + 1 : mAudioTracks.size();
    }

    @Override
    public Object getItem(int arg0)
    {
        return null;
    }

    @Override
    public long getItemId(int arg0)
    {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        //Log.d("AudioTrackAdapter", "getView(position = " + position + ")");
        //map to audio track layout
        LinearLayout audioTrackLay = (LinearLayout) mAudioTrackInf.inflate(R.layout.audio_track, parent, false);
        // apply colour theme
        //audioTrackLay.setBackgroundColor(UserSettings.getColourFromResId(R.color.track_list_header_background));
        //get title and artist views
        TextView composerAndWorkView = (TextView) audioTrackLay.findViewById(R.id.track_composer_and_work);
        TextView titleView = (TextView) audioTrackLay.findViewById(R.id.track_title);
        TextView artistView = (TextView) audioTrackLay.findViewById(R.id.track_interpreter);

        if (mAddDummyTrack && position == mAudioTracks.size())
        {
            // the dummy entry
            titleView.setHeight(120);
            titleView.setEnabled(false);        // shall make it non-clickable
            composerAndWorkView.setHeight(0);
            artistView.setHeight(0);
            audioTrackLay.setTag(-1);
            return audioTrackLay;
        }

        //get song using position
        AudioTrack currAudioTrack = mAudioTracks.get(position);
        String trackComposer    = currAudioTrack.composer;
        String trackGroup       = currAudioTrack.grouping;
        String trackTitle       = currAudioTrack.title;
        String trackInterpreter = currAudioTrack.interpreter;
        long   trackYear        = currAudioTrack.year;

        // precede track number, if set in preferences
        if (mShowTrackNumbers)
        {
            trackTitle = "[" + currAudioTrack.track_no + "] " + trackTitle;
        }

        // add subtitle, if set in preferences
        if (mShowSubtile)
        {
            trackTitle += ", " + currAudioTrack.subtitle;
        }

        // add conductor, if set in preferences
        if ((mShowConductorMode != 0) && !currAudioTrack.conductor.isEmpty())
        {
            if (!trackInterpreter.isEmpty())
            {
                if (mShowConductorMode == 2)
                {
                    trackInterpreter += "\n";
                } else
                {
                    trackInterpreter += ", ";
                }
            }

            String role = "";
            switch (mGenderismMode)
            {
                case 0:
                    role = mContext.getString(R.string.str_conductor_gen);
                    break;
                case 1:
                    role = mContext.getString(R.string.str_conductor_abbrev);
                    break;
                case 2:
                    role = mContext.getString(R.string.str_conductor_ess);
                    break;
                case 3:
                    role = mContext.getString(R.string.str_conductor_fm);
                    break;
                case 4:
                    role = mContext.getString(R.string.str_conductor_mf);
                    break;
                case 5:
                    role = mContext.getString(R.string.str_conductor_capI);
                    break;
                case 6:
                    role = mContext.getString(R.string.str_conductor_underscore);
                    break;
                case 7:
                    role = mContext.getString(R.string.str_conductor_x);
                    break;
                case 8:
                    role = mContext.getString(R.string.str_conductor_f);
                    break;
            }
            trackInterpreter += role + ": " + currAudioTrack.conductor;
        }

        // if there is no grouping, use title instead
        if (trackGroup.isEmpty())
        {
            trackGroup = trackTitle;
            trackTitle = "";
        }

        // if it's not the first one of the group, omit composer, work and interpreter
        if (currAudioTrack.group_no != 0)
        {
            trackComposer = "";
            trackGroup = "";
        }

        if (!currAudioTrack.group_last)
        {
            // for a group, add the interpreter only to the last element
            trackInterpreter = "";
            trackYear = 0;
        }

        // composer and work resp. title
        if (!trackGroup.equals(""))
        {
            String textShown;
            if (trackComposer.equals(""))
            {
                textShown = trackGroup;
            }
            else
            {
                textShown = trackComposer + ":\n" + trackGroup;
            }
            // work without movements/pieces: add duration here
            if (trackTitle.isEmpty())
            {
                textShown = textShown + " [" + AppGlobals.convertMsToHMmSs(currAudioTrack.duration) + "]";
            }
            composerAndWorkView.setText(textShown);
        }
        else
        {
            // neither composer nor work
            composerAndWorkView.setHeight(0);
        }

        // title and duration
        if (!trackTitle.isEmpty())
        {
            String titleShown = trackTitle + " [" + AppGlobals.convertMsToHMmSs(currAudioTrack.duration) + "]";
            titleView.setText(titleShown);
        }
        else
        {
            // work without movements/pieces
            titleView.setHeight(0);
        }

        // interpreter and year
        if (!trackInterpreter.isEmpty() || (trackYear != 0))
        {
            String textShown = trackInterpreter;
            if (!trackInterpreter.isEmpty() && (trackYear != 0))
            {
                textShown = textShown + " ";
            }
            if (trackYear != 0)
            {
                textShown = textShown + "(" + trackYear + ")";
            }
            artistView.setText(textShown);
        }
        else
        {
            // not first movement of the work, or there is no interpreter
            artistView.setHeight(0);
        }

        if (position == currTrack)
        {
            //Log.d("AudioTrackAdapter", "getView(position = " + position + ") -- selection colour");
            audioTrackLay.setBackgroundColor(UserSettings.getColourFromResId(R.color.track_list_background_selected));
        }
        else
        {
            audioTrackLay.setBackgroundColor(UserSettings.getColourFromResId(R.color.track_list_background_normal));
        }

        //set position as tag
        audioTrackLay.setTag(position);
        return audioTrackLay;
    }

}
