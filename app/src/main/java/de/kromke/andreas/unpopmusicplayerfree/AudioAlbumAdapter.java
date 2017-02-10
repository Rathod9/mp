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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * fills the album list with data
 */
class AudioAlbumAdapter extends BaseAdapter
{
    private Context mContext;
    //song list and layout
    private ArrayList<AudioAlbum> mAudioAlbums;
    private LayoutInflater mTheInflater;
    //private ArrayList<Drawable> theCoverBitmaps;
    private Drawable mDefaultBitmap;
    private int mCurrAlbum = -1;
    private int mAlbumImageSize;
    private boolean mShowAlbumDuration;

    //constructor
    // BUG in Android, see: http://stackoverflow.com/questions/13685275/outofmemory-error-in-custom-listview-adapter-mono-android
    AudioAlbumAdapter(Context c, ArrayList<AudioAlbum> theAudioAlbums, int albumImageSize, boolean showAlbumDuration)
    {
        mContext = c;
        mAudioAlbums = theAudioAlbums;
        mTheInflater = LayoutInflater.from(c);
        mAlbumImageSize = albumImageSize;
        mShowAlbumDuration = showAlbumDuration;

        /*
        // due to Android bug create all bitmaps here
        theCoverBitmaps = new ArrayList<Drawable>();
        for (int i = 0; i < theAudioAlbums.size(); i++)
        {
            AudioAlbum theAlbum = theAudioAlbums.get(i);
            Drawable d;
            if (!theAlbum.album_picture_path.isEmpty())
            {
                d = Drawable.createFromPath(theAlbum.album_picture_path);
            }
            else
            {
                d = null;
            }
            theCoverBitmaps.add(d);
        }
        */
        // default bitmap
        //        mDefaultBitmap = c.getResources().getDrawable(R.drawable.no_album_art);  // deprecated
        mDefaultBitmap = ContextCompat.getDrawable(c, R.drawable.no_album_art);

        // create bitmap cache
        BitmapUtils.LruCacheCreate(20);
    }

    void setCurrAlbum(int curr)
    {
        mCurrAlbum = curr;
    }

    @Override
    public int getCount()
    {
        return mAudioAlbums.size();
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


    /**************************************************************************
     *
     * convertView can be reused, if not null and has correct type, see Adapter.java
     *
     *************************************************************************/
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        RelativeLayout audioAlbumLay;

        if (convertView != null)
        {
            // just recycle the View. Can be sure it's the correct type
            Log.d("AudioAlbumAdapter", "getView(" + position + ") : recycle");
            audioAlbumLay = (RelativeLayout) convertView;
        }
        else
        {
            // inflate a new hierarchy from the xml layout, containing all text and image objects
            Log.d("AudioAlbumAdapter", "getView(" + position + ") : create");
            audioAlbumLay = (RelativeLayout) mTheInflater.inflate(R.layout.audio_album, parent, false);
        }

        if (audioAlbumLay != null)
        {
            //get title and artist sub-views from the just created hierarchy
            TextView albumView = (TextView) audioAlbumLay.findViewById(R.id.album_title);
            TextView albumInfoView = (TextView) audioAlbumLay.findViewById(R.id.album_info);
            ImageView albumCoverImageView = (ImageView) audioAlbumLay.findViewById(R.id.album_cover_image);
            {
                // set cover image view from Preferences
                ViewGroup.LayoutParams params;
                params = albumCoverImageView.getLayoutParams();
                params.width = mAlbumImageSize;
                params.height = mAlbumImageSize;
                albumCoverImageView.setLayoutParams(params);
            }

            //get album using position
            AudioAlbum currAudioAlbum = mAudioAlbums.get(position);

            //get title and artist strings
            albumView.setText(currAudioAlbum.album_name);

            String infoText = "";
            if ((currAudioAlbum.album_artist != null) && !currAudioAlbum.album_artist.isEmpty())
            {
                infoText = currAudioAlbum.album_artist + "\n";
            }
            String trackText = (currAudioAlbum.album_no_of_tracks == 1) ? mContext.getString(R.string.str_track) : mContext.getString(R.string.str_tracks);
            infoText += "(" + currAudioAlbum.album_no_of_tracks + " " + trackText;
            if (mShowAlbumDuration)
            {
                infoText += ", " + AppGlobals.convertMsToHMmSs(currAudioAlbum.album_duration);
            }
            infoText += ")";
            albumInfoView.setText(infoText);

            //if (albumCoverImageView.getDrawable() == null)
            {
                /*
                Drawable d = theCoverBitmaps.get(position);
                if (d != null)
                {
                    albumCoverImageView.setImageDrawable(d);
                }
                else
                {
                    albumCoverImageView.setImageDrawable(mDefaultBitmap);
                    // causes out of memory error
    //                albumCoverImageView.setImageResource(R.drawable.no_album_art);
                }
                */

                Bitmap bitmap = null;
                if (!currAudioAlbum.album_picture_path.isEmpty())
                {
                    Log.d("AudioAlbumAdapter", "getView() : art path \"" + currAudioAlbum.album_picture_path + "\"");
                    // cache mechanism
                    if (BitmapUtils.LruCacheContainsKey(position))
                    {
                        // note that bitmap maybe null for invalid album art files
                        bitmap = BitmapUtils.LruCacheGet(position);
                        Log.d("AudioAlbumAdapter", "getView() : got bitmap from cache");
                    }
                    else
                    {
                        // not cached, yet. Create and put to cache
                        bitmap = BitmapUtils.decodeSampledBitmapFromFile(currAudioAlbum.album_picture_path, mAlbumImageSize, mAlbumImageSize);
                        BitmapUtils.LruCachePut(position, bitmap);
                    }
                    /*
                    Drawable d = Drawable.createFromPath(currAudioAlbum.album_picture_path);
                    if (d != null)
                    {
                        albumCoverImageView.setImageDrawable(d);
                    }
                    */
                }

                if (bitmap != null)
                {
                    albumCoverImageView.setImageBitmap(bitmap);
                }
                else
                {
                    albumCoverImageView.setImageDrawable(mDefaultBitmap);
                    // causes out of memory error
                    //                albumCoverImageView.setImageResource(R.drawable.no_album_art);
                }

            }

            if (position == mCurrAlbum)
            {
                audioAlbumLay.setBackgroundColor(UserSettings.getColourFromResId(R.color.album_list_background_selected));
            }
            else
            {
                audioAlbumLay.setBackgroundColor(UserSettings.getColourFromResId(R.color.album_list_background_normal));
            }
            //set position as tag
            audioAlbumLay.setTag(position);
        }
        return audioAlbumLay;
    }

}
