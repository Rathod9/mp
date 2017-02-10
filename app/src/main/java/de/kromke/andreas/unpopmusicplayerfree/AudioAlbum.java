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

import android.annotation.SuppressLint;

/**
 * all necessary information about an album
 */
class AudioAlbum
{
    long album_id;                   // from MediaStore.Audio.Albums._ID, not "ALBUM_ID" (Android BUG!)
    String album_name;
    String album_artist;
//  String album_key;                // for future use
//  int album_no_of_discs;           // for future use
    long album_duration;             // for future use
//  String album_genre;              // for future use
    long album_no_of_tracks;
    // see http://tools.android.com/tips/lint/suppressing-lint-warnings
    // see http://tools.android.com/tips/lint-checks
    @SuppressLint({"UnusedAttribute"})
    @SuppressWarnings("unused")
    private long album_first_year;   // for future use
    @SuppressLint("UnusedAttribute")
    @SuppressWarnings("unused")
    private long album_last_year;    // for future use
    String album_picture_path;       // cover art (path of jpg or png file)

    AudioAlbum(
            long id,
            String name,
            String artist,
//          String audioTrackAlbumKey,
            long no_of_tracks,
            long first_year,
            long last_year,
            String picture_path)
    {
        album_id           = id;
        album_name         = name;
        album_artist       = (artist != null) ? artist : "";
//      album_key          = audioTrackAlbumKey;
        album_no_of_tracks = no_of_tracks;
        album_first_year   = first_year;
        album_last_year    = last_year;
        album_picture_path = (picture_path != null) ? picture_path : "";

        // to be set later:
        album_duration = 0;
    }
}
