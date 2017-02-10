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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

/**
 * helper class for user settings (preferences)
 */
class UserSettings
{
    static final String PREF_AUTOPLAY_MODE = "prefAutoplayMode";
    static final String PREF_CHOOSE_OTHER_TRACK = "prefChooseOtherTrack";
    static final String PREF_BEHAVIOUR_OF_BACK_KEY = "prefBehaviourOfBackKey";
    static final String PREF_AUTOHIDE_CONTROLLER = "prefAutoHideController";
    static final String PREF_SIZE_OF_ALBUM_HEADER = "prefSizeOfAlbumHeader";
    static final String PREF_SIZE_ALBUM_LIST_ALBUM_ART = "prefSizeOfAlbumArtInAlbumList";
    static final String PREF_SHOW_TRACK_NUMBER = "prefShowTracknumber";
    static final String PREF_SHOW_ALBUM_DURATION = "prefShowAlbumDuration";
    static final String PREF_SHOW_CONDUCTOR = "prefShowConductor";
    static final String PREF_SHOW_SUBTITLE = "prefShowSubtitle";
    static final String PREF_GENDERISM_NONSENSE = "prefGenderismNonsense";
    private static final String PREF_THEME = "prefTheme";

    private static Context mContext;
    private static SharedPreferences mSharedPrefs;
    private static int mTheme;

    static void SetContext(Context context)
    {
        mContext = context;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mTheme = getVal(PREF_THEME, 0);
    }

    /*
    * get a numerical value from the preferences and repair it, if necessary
     */
    static int getVal(final String key, int defaultVal)
    {
        String vds = Integer.toString(defaultVal);
        String vs = mSharedPrefs.getString(key, vds);
        int v;
        try
        {
            v = Integer.parseInt(vs);
        }
        catch(NumberFormatException e)
        {
            v = defaultVal;
            SharedPreferences.Editor prefEditor = mSharedPrefs.edit();
            prefEditor.putString(key, vds);
            prefEditor.commit();
        }

        return v;
    }

    /*
    * get a boolean value from the preferences and repair it, if necessary
     */
    static boolean getBool(final String key, boolean defaultVal)
    {
        boolean v = mSharedPrefs.getBoolean(key, defaultVal);
        return v;
    }

    /*
    * get a colour value
     */

    static int getColourFromResId(int id)
    {
        if (mTheme != 0)
        {
            switch (id)
            {
                case R.color.colorPrimaryDark:
                    id = R.color.gray_colorPrimaryDark;
                    break;
                case R.color.album_list_header_background:
                    id = R.color.gray_album_list_header_background;
                    break;
                case R.color.album_list_background_normal:
                    id = R.color.gray_album_list_background_normal;
                    break;
                case R.color.album_list_background_selected:
                    id = R.color.gray_album_list_background_selected;
                    break;
                case R.color.track_list_header_background:
                    id = R.color.gray_track_list_header_background;
                    break;
                case R.color.track_list_background_normal:
                    id = R.color.gray_track_list_background_normal;
                    break;
                case R.color.track_list_background_selected:
                    id = R.color.gray_track_list_background_selected;
                    break;
            }
        }

        // strange compatibility library
        // API23: getResources().getColor(R.color.track_list_background_normal, null)
        return ContextCompat.getColor(mContext, id);
    }

    /*
    * set the status bar colour for activity (Android 5.0 and above)
     */

    static void setStatusBarColour(Activity activity)
    {
        if (android.os.Build.VERSION.SDK_INT >= 21)
        {
            Log.d("MainActivity", "onCreate() : For API >= 21 set system bar colour");
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(UserSettings.getColourFromResId(R.color.colorPrimaryDark));
        } else
        {
        }
    }
}
