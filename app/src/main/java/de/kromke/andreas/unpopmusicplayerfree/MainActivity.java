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

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ListView;
import android.view.View;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * The album list, which is also the initial activity
 */

public class MainActivity extends AppCompatActivity
{
    public final static String EXTRA_MESSAGE = "com.kromke.unpopmusicplayerfree.ALBUM";
    private final static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 11;
    private ListView m_audioAlbumView;
    private int m_currAlbumNo;
    AudioAlbumAdapter m_audioAlbumAdt;
    private static final int RESULT_SETTINGS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d("MainActivity", "onCreate()");
        super.onCreate(savedInstanceState);

        // all needed preference settings
        UserSettings.SetContext(this);
        UserSettings.setStatusBarColour(this);

        // check app permissions at runtime, necessary for Android 6
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED)
        {
            initAlbumList();
        }
        else
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }


    private void initAlbumList()
    {
        // create global variables
        AppGlobals.initAppGlobals(getContentResolver());

        ActionBar ab = getSupportActionBar();
        if (ab != null)
        {
            ab.setDisplayShowHomeEnabled(true);
            //ab.setLogo(R.mipmap.ic_launcher);     rather ugly
            ab.setSubtitle("(" + AppGlobals.audioAlbumList.size() + " " +
                    ((AppGlobals.audioAlbumList.size() == 1) ? getString(R.string.str_album) : getString(R.string.str_albums)) + ")");
            //ab.setDisplayUseLogoEnabled(true);
            ab.setBackgroundDrawable(new ColorDrawable(UserSettings.getColourFromResId(R.color.album_list_header_background)));
        }

        m_currAlbumNo = -1;
        setContentView(R.layout.activity_albums);
        //retrieve list view
        m_audioAlbumView = (ListView) findViewById(R.id.audioAlbum_list);
        m_audioAlbumView.setBackgroundColor(UserSettings.getColourFromResId(R.color.album_list_background_normal));

        //create and set adapter
        int albumImageSize = UserSettings.getVal(UserSettings.PREF_SIZE_ALBUM_LIST_ALBUM_ART, 80);
        boolean showAlbumDuration = UserSettings.getBool(UserSettings.PREF_SHOW_ALBUM_DURATION, false);
        m_audioAlbumAdt = new AudioAlbumAdapter(this, AppGlobals.audioAlbumList, albumImageSize, showAlbumDuration);
        m_audioAlbumView.setAdapter(m_audioAlbumAdt);
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults)
    {
        switch (requestCode)
        {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
            {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
                {
                    initAlbumList();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.d("MainActivity", "onCreateOptionsMenu()");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_action_bar, menu);
        return true;
    }

    // user album select
    public void albumPicked(View view)
    {
        String theTag = view.getTag().toString();
        int newAlbumNo;
        try
        {
            newAlbumNo = Integer.parseInt(theTag);
        }
        catch(NumberFormatException e)
        {
            newAlbumNo = 0;
        }
        if (newAlbumNo != m_currAlbumNo)
        {
            m_audioAlbumAdt.setCurrAlbum(newAlbumNo);

            if (m_currAlbumNo >= 0)
            {
                View v = getViewByPosition(m_currAlbumNo, m_audioAlbumView);
                if (v != null)
                {
                    // back to normal
                    v.setBackgroundColor(UserSettings.getColourFromResId(R.color.album_list_background_normal));
                }
            }
            if (newAlbumNo >= 0)
            {
                View v = getViewByPosition(newAlbumNo, m_audioAlbumView);
                if (v != null)
                {
                    // selected
                    v.setBackgroundColor(UserSettings.getColourFromResId(R.color.album_list_background_selected));
                }
            }
            m_currAlbumNo = newAlbumNo;
        }

        AudioAlbum theAlbum = AppGlobals.audioAlbumList.get(newAlbumNo);

        Log.v("MainActivity", "theTag = " + theTag);
        Intent intent = new Intent(this, TracksOfAlbumActivity.class);
        intent.putExtra(EXTRA_MESSAGE, theAlbum.album_name);
        AppGlobals.openAudioAlbum(theAlbum);
        startActivity(intent);
    }

    @Override
    protected void onDestroy()
    {
        Log.d("MainActivity", "onDestroy()");
        super.onDestroy();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case RESULT_SETTINGS:
                //showUserSettings();
                break;
        }
    }

    private void DialogAbout()
    {
        PackageInfo packageinfo = null;
        ApplicationInfo ai = null;
        String strCreationTime = null;

        try
        {
            packageinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            Log.d("MainActivity", e.getMessage());
        }

        String strVersion = "";
        if (packageinfo != null)
        {
            strVersion = packageinfo.versionName;
            try
            {
                ai = packageinfo.applicationInfo;
                ZipFile zf = new ZipFile(ai.sourceDir);
                ZipEntry ze = zf.getEntry("META-INF/MANIFEST.MF");
                long time = ze.getTime();
                // get ISO8601 date instead of dumb US format (Z = time zone) ...
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
                //SimpleDateFormat formatter = (SimpleDateFormat) SimpleDateFormat.getInstance("yyyy-MM-dd'T'HH:mmZ");
                //df.setTimeZone(TimeZone.getTimeZone("gmt"));
                strCreationTime = df.format(new java.util.Date(time));
                zf.close();
            }
            catch(Exception e)
            {
            }
        }

        final String strTitle = "Unpopular Music Player FREE";
        final String strDescription = getString(R.string.str_app_description);
        final String strAuthor = getString(R.string.str_author);

        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(strTitle);
        alertDialog.setIcon(R.drawable.app_icon_noborder);
        alertDialog.setMessage(
                        strDescription + "\n\n" +
                        strAuthor + "Andreas Kromke" + "\n\n" +
                        "Version " + strVersion + "\n" +
                        "(" + strCreationTime + ")");
        alertDialog.setCancelable(true);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void DialogHelp()
    {
        WebView webView = new WebView(this);
        webView.loadUrl("file:///android_asset/html-" + getString(R.string.locale_prefix) + "/help.html");
//        webView.loadData(SampleStats.boxScore1, "text/html", "utf-8");
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setView(webView);
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.d("MainActivity", "onOptionsItemSelected()");
        switch (item.getItemId())
        {
            case R.id.action_about:
                Log.d("MainActivity", "onOptionsItemSelected() -- action_about");
                DialogAbout();
                break;

            case R.id.action_help:
                Log.d("MainActivity", "onOptionsItemSelected() -- action_help");
                DialogHelp();
                break;

            case R.id.action_settings:
                Log.d("MainActivity", "onOptionsItemSelected() -- action_settings");
                // User chose the "Settings" item, show the app settings UI...
                Intent i = new Intent(this, UserSettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                break;

            case R.id.action_exit:
                System.exit(0);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
        return true;
    }
}
