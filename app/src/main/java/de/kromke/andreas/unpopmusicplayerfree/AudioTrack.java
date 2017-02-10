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


/**
 * all data for a single audio file
 */
class AudioTrack
{
    long id;
    String track_no;
    public String title;           // the title of the "track" resp. movement, e.g. "I. Allegro"
    public long duration;
    public String grouping;        // the name of the complete piece, i.e. "Symphony No. 5"
    public String subtitle;        //  maybe like "op. 42"?
    public String composer;        // the composer of the complete piece, i.e. Wolfgang Mozart
    String interpreter;     // performer(s) (called "artist" in subculture), i.e. NDR Radiosinfonie
    public String conductor;
    public long year;
    int group_no;
    boolean group_last;          // last in group
    @SuppressWarnings("unused")
    private boolean audio_tags_read;     // needed to remember if the file had been opened to read composer, grouping etc.
    public String path;                 // needed to later read the audio tags from file

    AudioTrack(
            long audioTrackID,
            String audioTrackNo,
            String audioTrackTitle,
            String audioTrackPath,
            long audioDuration,
            String audioTrackArtist,
            long audioTrackYear)
    {
        id           = audioTrackID;
        track_no     = audioTrackNo;        // disc and track number (disc number 1000, 2000, ...)
        title        = (audioTrackTitle != null) ? audioTrackTitle : "";
        path         = audioTrackPath;
        duration     = audioDuration;
        interpreter  = (audioTrackArtist != null) ? audioTrackArtist : "";
        year         = audioTrackYear;

        grouping     = "";
        subtitle     = "";
        conductor    = "";
        group_no     = 0;
        group_last   = true;
        composer     = "";
        audio_tags_read = false;
    }

    void addTagInfo(
        String audioTrackGrouping,
        String audioTrackSubtitle,
        String audioTrackComposer,
        String audioTrackConductor)
    {
        grouping  = (audioTrackGrouping != null) ? audioTrackGrouping : "";
        subtitle  = (audioTrackSubtitle != null) ? audioTrackSubtitle : "";
        composer  = (audioTrackComposer != null) ? audioTrackComposer : "";
        conductor = (audioTrackConductor != null) ? audioTrackConductor : "";
        audio_tags_read = true;
    }

	// The composer matches if both are not set or if they are identical.
    @SuppressWarnings("SimplifiableIfStatement")
	static private boolean composerMatch(final String composer1, final String composer2)
	{
		if (composer1 == null)
		{
			// only match, if both are null
			return (composer2 == null);
		}

		if (composer2 == null)
		{
			// only match, if both are null, and composer1 is not (see above)
			return false;
		}

		return (composer1.equals(composer2));
	}

	// Two tracks belong to the same group if grouping is valid, and
	// both grouping and composer match.
    @SuppressWarnings("SimplifiableIfStatement")
	boolean isSameGroup(final AudioTrack anotherTrack)
	{
		if ((anotherTrack != null) &&
		    (anotherTrack.grouping != null) &&
		    !(anotherTrack.grouping.isEmpty()) &&
		    (grouping != null) &&
		    (grouping.equals(anotherTrack.grouping)))
		{
			return composerMatch(composer, anotherTrack.composer);
		}
		else
		{
			// no grouping or different group
			return false;
		}
	}
}
