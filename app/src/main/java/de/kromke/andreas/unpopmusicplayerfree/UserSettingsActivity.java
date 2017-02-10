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

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;

import java.util.List;

/**
 * combines the three preferences pages
 */
public class UserSettingsActivity extends PreferenceActivity
{
    static public class MyPreferenceFragmentBehaviour extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_behaviour);
        }
    }

    static public class MyPreferenceFragmentView extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_view);
        }
    }

    static public class MyPreferenceFragmentScaleUi extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_scale_ui);
        }
    }

    @Override
    protected void onPause()
    {
        Log.d("UserSettingsActivity", "onPause()");
        super.onPause();
    }

    /* THIS IS NEEDED IN CASE THERE ARE TWO LEVELS OF SETTINGS, A HEADER AND SOME FRAGMENTS */
    @Override
    public void onBuildHeaders(List<Header> target)
    {
        loadHeadersFromResource(R.xml.headers_settings, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return MyPreferenceFragmentBehaviour.class.getName().equals(fragmentName) ||
                MyPreferenceFragmentView.class.getName().equals(fragmentName) ||
                MyPreferenceFragmentScaleUi.class.getName().equals(fragmentName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        UserSettings.setStatusBarColour(this);      // TODO: this does not work, don't know why
        // THIS IS NEEDED IN CASE THERE IS ONLY ONE LEVEL OF SETTINGS, I.E. NO HEADERS, ONLY ONE FRAGMENT
//        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragmentBehaviour()).commit();

        //checkValues();
    }

    /*
    private void checkValues()
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String strUserName = sharedPrefs.getString("username", "NA");
        boolean bAppUpdates = sharedPrefs.getBoolean("applicationUpdates",false);
        String downloadType = sharedPrefs.getString("downloadType","1");

        String msg = "Cur Values: ";
        msg += "\n userName = " + strUserName;
        msg += "\n bAppUpdates = " + bAppUpdates;
        msg += "\n downloadType = " + downloadType;

        MyToast.toastShort(this, msg);
    }
*/

/*
    PreferenceFragment m_prefFragment;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        m_prefFragment = new PreferenceFragment()
        {
            @Override
            public void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);


                // Make sure default values are applied.  In a real app, you would
                // want this in a shared function that is used to retrieve the
                // SharedPreferences wherever they are needed.
                //PreferenceManager.setDefaultValues(getActivity(),
                //        R.xml.advanced_preferences, false);

                // Load the preferences from an XML resource
                addPreferencesFromResource(R.xml.settings);
            }
        };
    }
     */
}
