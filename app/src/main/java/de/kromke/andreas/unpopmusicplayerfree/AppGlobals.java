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
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
//import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * global variables, shared by all activities
 */
class AppGlobals
{
    // global settings
    static private final boolean ignore_notifications_and_ringtones = true; // otherwise they appear as music...
    // music list
    static ArrayList<AudioAlbum> audioAlbumList;         // all albums
    static AudioAlbum currAlbum = null;
    static ArrayList<AudioTrack> audioAlbumTrackList = null;    // all tracks for currAlbum

    static private ContentResolver resolver;

    /**************************************************************************
     *
     * initialisation
     *
     *************************************************************************/
    static void initAppGlobals(ContentResolver musicResolver)
    {
        initAudioAlbumList(musicResolver);
    }

    static int getAudioAlbumTrackListSize()
    {
        if (audioAlbumTrackList == null)
            return 0;
        else
            return audioAlbumTrackList.size();
    }

    /**************************************************************************
     *
     * get all albums
     *
     *************************************************************************/
    static private void initAudioAlbumList(ContentResolver musicResolver)
    {
        // query external audio:
        //  Media       or
        //  Genres      or
        //  Playlists   or
        //  Artists     or
        //  Albums      or
        //  Radio

        audioAlbumList = new ArrayList</*AudioAlbum*/>();
        resolver = musicResolver;
        final String[] columns =
        {
            android.provider.MediaStore.Audio.Albums._ID,
            android.provider.MediaStore.Audio.Albums.ALBUM,
            android.provider.MediaStore.Audio.Albums.ARTIST,
            android.provider.MediaStore.Audio.Albums.NUMBER_OF_SONGS,
            android.provider.MediaStore.Audio.Albums.FIRST_YEAR,
            android.provider.MediaStore.Audio.Albums.LAST_YEAR,
            android.provider.MediaStore.Audio.Albums.ALBUM_ART
        };
        final String orderBy = MediaStore.Audio.Albums.ALBUM;

        // this is kind of SQL query:
        Cursor theCursor = resolver.query(
                android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                columns,    // columns to return, null returns all rows
                null,       // rows to return, as "WHERE ...", null returns all rows
                null,       // selection arguments, replacing question marks in previous argument
                orderBy     // sort order, null: unordered
        );
        // handle error cases
        if (theCursor == null)
        {
            Log.v("AppGlobals", "no cursor");
            return;
        }
        if (!theCursor.moveToFirst())
        {
            Log.v("AppGlobals", "no album found");
            theCursor.close();
            return;
        }

        // from class AlbumColumns:
        // this is an Android bug and leads to an exception:
        // -> http://stackoverflow.com/questions/34151038/why-is-androids-mediastore-audio-album-album-id-causing-an-illegalstateexceptio
        //int idColumn         = theCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID);
        int idColumn         = theCursor.getColumnIndex(MediaStore.Audio.Albums._ID);
        int albumColumn      = theCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
        int artistColumn     = theCursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST);
        int noOfTracksColumn = theCursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS);
        int firstYearColumn  = theCursor.getColumnIndex(MediaStore.Audio.Albums.FIRST_YEAR);
        int lastYearColumn   = theCursor.getColumnIndex(MediaStore.Audio.Albums.LAST_YEAR);
        int albumArtColumn   = theCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);

        //
        // loop to add all album data to the global album list
        //
        do
        {
            long thisAlbumId = theCursor.getLong(idColumn);
            String thisAlbumName = theCursor.getString(albumColumn);
            String thisInterpreter = theCursor.getString(artistColumn);
            long thisNoOfTracks = theCursor.getInt(noOfTracksColumn);
            long thisFirstYear  = theCursor.getInt(firstYearColumn);
            long thisLastYear   = theCursor.getInt(lastYearColumn);
            String thisAlbumArt = theCursor.getString(albumArtColumn);
            Log.v("AppGlobals", "Album Id (_ID)     = " + thisAlbumId);
            Log.v("AppGlobals", "Album Name         = " + thisAlbumName);
            Log.v("AppGlobals", "Album Interpreter  = " + thisInterpreter);
            Log.v("AppGlobals", "Album No of Tracks = " + thisNoOfTracks);
            Log.v("AppGlobals", "Album First Year   = " + thisFirstYear);
            Log.v("AppGlobals", "Album Last Year    = " + thisLastYear);
            Log.v("AppGlobals", "Album Art          = " + thisAlbumArt);
            Log.v("AppGlobals", "==============================================\n");

            if (!ignore_notifications_and_ringtones || !isAlbumIllegal(thisAlbumName))
            {
                AudioAlbum theAlbum = new AudioAlbum(
                            thisAlbumId,
                            thisAlbumName,
                            thisInterpreter,
                            thisNoOfTracks,
                            thisFirstYear,
                            thisLastYear,
                            thisAlbumArt);
                audioAlbumList.add(theAlbum);
            }
        }
        while (theCursor.moveToNext());
        theCursor.close();
        Log.d("AppGlobals", "found " + audioAlbumList.size() + " albums");
    }

    /**************************************************************************
     *
     * get all tracks for current album
     *
     *************************************************************************/
    static private void initAudioTrackListForCurrentAlbum(final AudioAlbum theAlbum)
    {
        if ((currAlbum != null) && (currAlbum.album_id == theAlbum.album_id))
        {
            // no change
            return;
        }
        currAlbum = theAlbum;
        audioAlbumTrackList = new ArrayList</*AudioTrack*/>();

        // query external audio:
        //  Media       or
        //  Genres      or
        //  Playlists   or
        //  Artists     or
        //  Albums      or
        //  Radio
        final String[] columns =
        {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.COMPOSER,
            MediaStore.Audio.Media.ARTIST,
            /* MediaStore.Audio.Media.ALBUM_ARTIST, is hidden via @hide */
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.YEAR,
            // from base class MediaColumns:
            MediaStore.Audio.Media.DATA,  // here: the path
            MediaStore.Audio.Media.SIZE
        };
//        final String where = MediaStore.Audio.Media.ALBUM + "=?";
        final String where = MediaStore.Audio.Media.ALBUM_ID + "=?";
//        final String whereVal[] = { currAlbum.album_name };
        final String whereVal[] = { "" + currAlbum.album_id };
        final String orderBy = MediaStore.Audio.Media.TRACK;

        // this is kind of SQL query:
        Cursor theCursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                columns,    // columns to return, null returns all rows
                where,      // rows to return, as "WHERE ...", null returns all rows
                whereVal,   // selection arguments, replacing question marks in previous argument
                orderBy     // sort order, null: unordered
        );
        // handle error cases
        if (theCursor == null)
        {
            Log.v("AppGlobals", "no cursor");
            return;
        }
        if (!theCursor.moveToFirst())
        {
            Log.v("AppGlobals", "no track for album found");
            theCursor.close();
            return;
        }

        // from class AudioColumns:
        int idColumn          = theCursor.getColumnIndex(MediaStore.Audio.Media._ID);
        int trackColumn       = theCursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
        int titleColumn       = theCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int composerColumn    = theCursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER);
        int artistColumn      = theCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int durationColumn    = theCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
        int yearColumn        = theCursor.getColumnIndex(MediaStore.Audio.Media.YEAR);
        // from base class MediaColumns:
        int dataColumn        = theCursor.getColumnIndex(MediaStore.Audio.Media.DATA);  // data stream
        int sizeColumn        = theCursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
        //int albumColumn       = theCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int albumIdColumn       = theCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
        //int albumArtistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ARTIST);
        //int albumKeyColumn    = theCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_KEY);

        //
        // loop to add all music files to the album music list
        //
        do
        {
            long thisId            = theCursor.getLong(idColumn);
            long albumId           = theCursor.getLong(albumIdColumn);
            String thisTrack       = theCursor.getString(trackColumn);
            String thisTitle       = theCursor.getString(titleColumn);
            String thisComposer    = theCursor.getString(composerColumn);
            String thisInterpreter = theCursor.getString(artistColumn);     // performer/interpreter
            long thisDuration      = theCursor.getInt(durationColumn);
            long thisYear = theCursor.getInt(yearColumn);
            String thisPath        = theCursor.getString(dataColumn);
            long thisSize          = theCursor.getInt(sizeColumn);

            Log.v("AppGlobals", "thisId          = " + thisId);
            Log.v("AppGlobals", "albumId         = " + albumId);
            Log.v("AppGlobals", "thisTrack       = " + thisTrack);
            Log.v("AppGlobals", "thisTitle       = " + thisTitle);
            Log.v("AppGlobals", "thisComposer    = " + thisComposer);
            Log.v("AppGlobals", "thisInterpreter = " + thisInterpreter);
            Log.v("AppGlobals", "thisDuration    = " + thisDuration);
            Log.v("AppGlobals", "thisPath        = " + thisPath);
            Log.v("AppGlobals", "thisYear        = " + thisYear);
            Log.v("AppGlobals", "thisSize        = " + thisSize);
            Log.v("AppGlobals", "==============================================\n");

            if (thisInterpreter.equals("<unknown>"))
                thisInterpreter = "";

            AudioTrack theTrack = new AudioTrack(
                    thisId,
                    thisTrack,
                    thisTitle,
                    thisPath,
                    thisDuration,
                    thisInterpreter,
                    thisYear);

            TagsFromFile theTagsFromFile = getTagsUsingJaudiotagger(thisPath);
            theTrack.addTagInfo(
                    theTagsFromFile.tagGrouping,
                    theTagsFromFile.tagSubtitle,
                    theTagsFromFile.tagComposer,
                    theTagsFromFile.tagConductor);
            audioAlbumTrackList.add(theTrack);
        }
        while (theCursor.moveToNext());

        theCursor.close();
        Log.d("AppGlobals", "found " + audioAlbumTrackList.size() + " tracks");
    }

    private static class TagsFromFile
    {
        String tagComposer;
        String tagConductor;
        String tagGrouping;
        String tagSubtitle;
        String tagAlbum;
        String tagAlbumArtist;
        String tagDiscNo;
        String tagDiscTotal;
        String tagTrack;
        String tagTrackTotal;
    }

    /*
    * method to retrieve info from audio file
    */
    static private TagsFromFile getTagsUsingJaudiotagger(String thisPath)
    {
        TagsFromFile ret = new TagsFromFile();
        File theAudioFile = new File(thisPath);
        AudioFile f = null;
        Tag tag = null;

        try
        {
            f = AudioFileIO.read(theAudioFile);
        } catch (IOException e)
        {
            Log.e("AppGlobals", "cannot read file " + thisPath + "because of:" + e.getMessage());
        } catch (org.jaudiotagger.audio.exceptions.CannotReadException e)
        {
            Log.e("AppGlobals", "cannot read audio file " + thisPath + "because of:" + e.getMessage());
        } catch (org.jaudiotagger.audio.exceptions.ReadOnlyFileException e)
        {
            Log.e("AppGlobals", "cannot open read-only audio file " + thisPath + "because of:" + e.getMessage());
        } catch (org.jaudiotagger.tag.TagException e)
        {
            Log.e("AppGlobals", "cannot read tag from audio file " + thisPath + "because of:" + e.getMessage());
        } catch (org.jaudiotagger.audio.exceptions.InvalidAudioFrameException e)
        {
            Log.e("AppGlobals", "invalid audio frame in " + thisPath + "because of:" + e.getMessage());
        }
        if (f != null)
        {
            // audio file information that cannot be written to:
            //AudioHeader ah = f.getAudioHeader();
            // audio file information that could be changed:
            tag = f.getTag();
        }

        if (tag == null)
        {
            Log.w("AppGlobals", "no audio tags for " + thisPath);
            ret.tagComposer = null;
            ret.tagConductor = null;
            ret.tagGrouping = null;
            ret.tagSubtitle = null;
            ret.tagAlbum = null;
            ret.tagAlbumArtist = null;
            ret.tagDiscNo = null;
            ret.tagDiscTotal = null;
            ret.tagTrack = null;
            ret.tagTrackTotal = null;
        }
        else
        {
            ret.tagComposer = tag.getFirst(FieldKey.COMPOSER);
            ret.tagConductor = tag.getFirst(FieldKey.CONDUCTOR);
            ret.tagGrouping = tag.getFirst(FieldKey.GROUPING);
            ret.tagSubtitle = tag.getFirst(FieldKey.SUBTITLE);
            ret.tagAlbum = tag.getFirst(FieldKey.ALBUM);
            ret.tagAlbumArtist = tag.getFirst(FieldKey.ALBUM_ARTIST);
            ret.tagDiscNo = tag.getFirst(FieldKey.DISC_NO);
            ret.tagDiscTotal = tag.getFirst(FieldKey.DISC_TOTAL);
            ret.tagTrack = tag.getFirst(FieldKey.TRACK);
            ret.tagTrackTotal = tag.getFirst(FieldKey.TRACK_TOTAL);
        }
        Log.v("AppGlobals", "tagComposer    = " + ret.tagComposer);
        Log.v("AppGlobals", "tagConductor   = " + ret.tagConductor);
        Log.v("AppGlobals", "tagGrouping    = " + ret.tagGrouping);
        Log.v("AppGlobals", "tagSubtitle    = " + ret.tagSubtitle);
        Log.v("AppGlobals", "tagAlbum       = " + ret.tagAlbum);
        Log.v("AppGlobals", "tagAlbumArtist = " + ret.tagAlbumArtist);
        Log.v("AppGlobals", "tagDiscNo      = " + ret.tagDiscNo);
        Log.v("AppGlobals", "tagDiscNoTotal = " + ret.tagDiscTotal);
        Log.v("AppGlobals", "tagTrack       = " + ret.tagTrack);
        Log.v("AppGlobals", "tagTrackTotal  = " + ret.tagTrackTotal);
        Log.v("AppGlobals", "=====================================\n\n");

        return ret;
    }

    // helper function to ignore internal "Albums"
    static private boolean isAlbumIllegal(final String thisAlbumName)
    {
        return ((thisAlbumName.equals("Notifications")) ||
                (thisAlbumName.equals("Ringtones")));
    }


    /*
    * select album and extract list of corresponding tracks to audioAlbumTrackList
    */
    static void openAudioAlbum(AudioAlbum album)
    {
        Log.v("AppGlobals", "openAudioAlbum() -- albumId == " + album.album_id);
        initAudioTrackListForCurrentAlbum(album);

        AudioTrack prevTrack = null;
        int noInGroup = 0;
        long albumDuration = 0;
        for (int i = 0; i < audioAlbumTrackList.size(); i++)
        {
            AudioTrack theTrack = audioAlbumTrackList.get(i);

            if (!theTrack.isSameGroup(prevTrack))
            {
                // this is the first group or a new one in the sorted list or there is no group
                noInGroup = 0;
                if (prevTrack != null)
                {
                    prevTrack.group_last = true;
                }
            }

            theTrack.group_no = noInGroup++;
            theTrack.group_last = false;		// may be changed later
            albumDuration += theTrack.duration;
            prevTrack = theTrack;
        }

        if (prevTrack != null)
        {
            prevTrack.group_last = true;
        }

        album.album_duration = albumDuration;
    }

    /* helper function */
    // see http://tools.android.com/tips/lint/suppressing-lint-warnings
    // see http://tools.android.com/tips/lint-checks
    @SuppressLint("DefaultLocale")
    static String convertMsToHMmSs(long ms)
    {
        long seconds = (ms + 999) / 1000;       // round up
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        if (h > 0)
            return String.format("%d:%02d:%02d", h, m, s);
        else
            return String.format("%02d:%02d", m, s);
    }
}
