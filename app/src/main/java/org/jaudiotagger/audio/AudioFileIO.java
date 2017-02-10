/*
 * Entagged Audio Tag library
 * Copyright (c) 2003-2005 Raphaël Slinckx <raphael@slinckx.net>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jaudiotagger.audio;

import org.jaudiotagger.audio.aiff.AiffFileReader;
import org.jaudiotagger.audio.asf.AsfFileReader;
import org.jaudiotagger.audio.dsf.DsfAudioFileReader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.flac.FlacFileReader;
import org.jaudiotagger.audio.generic.*;
import org.jaudiotagger.audio.mp3.MP3FileReader;
import org.jaudiotagger.audio.mp4.Mp4FileReader;
import org.jaudiotagger.audio.ogg.OggFileReader;
import org.jaudiotagger.audio.real.RealFileReader;
import org.jaudiotagger.audio.wav.WavFileReader;
import org.jaudiotagger.logging.ErrorMessage;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * The main entry point for the Tag Reading/Writing operations, this class will
 * select the appropriate reader/writer for the given file.
 * 
 *
 * It selects the appropriate reader/writer based on the file extension (case
 * ignored).
 * 
 *
 * Here is an simple example of use:
 * 
 *
 * <code>
 * AudioFile audioFile = AudioFileIO.read(new File("audiofile.mp3")); //Reads the given file.
 * int bitrate = audioFile.getBitrate(); //Retreives the bitrate of the file.
 * String artist = audioFile.getTag().getFirst(TagFieldKey.ARTIST); //Retreive the artist name.
 * audioFile.getTag().setGenre("Progressive Rock"); //Sets the genre to Prog. Rock, note the file on disk is still unmodified.
 * AudioFileIO.write(audioFile); //Write the modifications in the file on disk.
 * </code>
 * 
 *
 * You can also use the <code>commit()</code> method defined for
 * <code>AudioFile</code>s to achieve the same goal as
 * <code>AudioFileIO.write(File)</code>, like this:
 * 
 *
 * <code>
 * AudioFile audioFile = AudioFileIO.read(new File("audiofile.mp3"));
 * audioFile.getTag().setGenre("Progressive Rock");
 * audioFile.commit(); //Write the modifications in the file on disk.
 * </code>
 * 
 *
 * @author Raphael Slinckx
 * @version $Id$
 * @see AudioFile
 * @see org.jaudiotagger.tag.Tag
 * @since v0.01
 */
public class AudioFileIO
{

    //Logger
    public static Logger logger = Logger.getLogger("org.jaudiotagger.audio");

    // !! Do not forget to also add new supported extensions to AudioFileFilter
    // !!

    /**
     * This field contains the default instance for static use.
     */
    private static AudioFileIO defaultInstance;


    /**
     * This method returns the default instance for static use.<br>
     *
     * @return The default instance.
     */
    public static AudioFileIO getDefaultAudioFileIO()
    {
        if (defaultInstance == null)
        {
            defaultInstance = new AudioFileIO();
        }
        return defaultInstance;
    }

    /**
     *
     * Read the tag contained in the given file.
     * 
     *
     * @param f The file to read.
     * @param ext The extension to be used.
     * @return The AudioFile with the file tag and the file encoding info.
     * @throws org.jaudiotagger.audio.exceptions.CannotReadException If the file could not be read, the extension wasn't
     *                             recognized, or an IO error occurred during the read.
     * @throws org.jaudiotagger.tag.TagException
     * @throws org.jaudiotagger.audio.exceptions.ReadOnlyFileException
     * @throws java.io.IOException
     * @throws org.jaudiotagger.audio.exceptions.InvalidAudioFrameException
     */
    public static AudioFile readAs(File f,String ext)
            throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException
    {
        return getDefaultAudioFileIO().readFileAs(f,ext);
    }

    /**
    *
    * Read the tag contained in the given file.
    * 
    *
    * @param f The file to read.
    * @return The AudioFile with the file tag and the file encoding info.
    * @throws org.jaudiotagger.audio.exceptions.CannotReadException If the file could not be read, the extension wasn't
    *                             recognized, or an IO error occurred during the read.
    * @throws org.jaudiotagger.tag.TagException
    * @throws org.jaudiotagger.audio.exceptions.ReadOnlyFileException
    * @throws java.io.IOException
    * @throws org.jaudiotagger.audio.exceptions.InvalidAudioFrameException
    */
   public static AudioFile readMagic(File f)
           throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException
   {
       return getDefaultAudioFileIO().readFileMagic(f);
   }

   /**
   *
   * Read the tag contained in the given file.
   * 
   *
   * @param f The file to read.
   * @return The AudioFile with the file tag and the file encoding info.
   * @throws org.jaudiotagger.audio.exceptions.CannotReadException If the file could not be read, the extension wasn't
   *                             recognized, or an IO error occurred during the read.
   * @throws org.jaudiotagger.tag.TagException
   * @throws org.jaudiotagger.audio.exceptions.ReadOnlyFileException
   * @throws java.io.IOException
   * @throws org.jaudiotagger.audio.exceptions.InvalidAudioFrameException
   */
  public static AudioFile read(File f)
          throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException
  {
      return getDefaultAudioFileIO().readFile(f);
  }


    // These tables contains all the readers/writers associated with extension
    // as a key
    private Map<String, AudioFileReader> readers = new HashMap<String, AudioFileReader>();


    /**
     * Creates an instance.
     */
    public AudioFileIO()
    {
        prepareReadersAndWriters();
    }


    /**
     * Creates the readers and writers.
     */
    private void prepareReadersAndWriters()
    {

        // Tag Readers
        readers.put(SupportedFileFormat.OGG.getFilesuffix(), new OggFileReader());
        readers.put(SupportedFileFormat.FLAC.getFilesuffix(),new FlacFileReader());
        readers.put(SupportedFileFormat.MP3.getFilesuffix(), new MP3FileReader());
        readers.put(SupportedFileFormat.MP4.getFilesuffix(), new Mp4FileReader());
        readers.put(SupportedFileFormat.M4A.getFilesuffix(), new Mp4FileReader());
        readers.put(SupportedFileFormat.M4P.getFilesuffix(), new Mp4FileReader());
        readers.put(SupportedFileFormat.M4B.getFilesuffix(), new Mp4FileReader());
        //readers.put(SupportedFileFormat.AAC.getFilesuffix(), new Mp4FileReader());  // A.K.
        readers.put(SupportedFileFormat.WAV.getFilesuffix(), new WavFileReader());
        readers.put(SupportedFileFormat.WMA.getFilesuffix(), new AsfFileReader());
        readers.put(SupportedFileFormat.AIF.getFilesuffix(), new AiffFileReader());
        readers.put(SupportedFileFormat.AIFC.getFilesuffix(), new AiffFileReader());
        readers.put(SupportedFileFormat.AIFF.getFilesuffix(), new AiffFileReader());
        readers.put(SupportedFileFormat.DSF.getFilesuffix(), new DsfAudioFileReader());
        final RealFileReader realReader = new RealFileReader();
        readers.put(SupportedFileFormat.RA.getFilesuffix(), realReader);
        readers.put(SupportedFileFormat.RM.getFilesuffix(), realReader);
    }

    /**
     *
     * Read the tag contained in the given file.
     * 
     *
     * @param f The file to read.
     * @return The AudioFile with the file tag and the file encoding info.
     * @throws org.jaudiotagger.audio.exceptions.CannotReadException If the file could not be read, the extension wasn't
     *                             recognized, or an IO error occurred during the read.
     * @throws org.jaudiotagger.tag.TagException
     * @throws org.jaudiotagger.audio.exceptions.ReadOnlyFileException
     * @throws java.io.IOException
     * @throws org.jaudiotagger.audio.exceptions.InvalidAudioFrameException
     */
    public AudioFile readFile(File f)
            throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException
    {
        checkFileExists(f);
        String ext = Utils.getExtension(f);

        AudioFileReader afr = readers.get(ext);
        if (afr == null)
        {
            throw new CannotReadException(ErrorMessage.NO_READER_FOR_THIS_FORMAT.getMsg(ext));
        }
        AudioFile tempFile = afr.read(f);
        tempFile.setExt(ext);
        return tempFile;
    }

    /**
    *
    * Read the tag contained in the given file.
    * 
    *
    * @param f The file to read.
    * @return The AudioFile with the file tag and the file encoding info.
    * @throws org.jaudiotagger.audio.exceptions.CannotReadException If the file could not be read, the extension wasn't
    *                             recognized, or an IO error occurred during the read.
    * @throws org.jaudiotagger.tag.TagException
    * @throws org.jaudiotagger.audio.exceptions.ReadOnlyFileException
    * @throws java.io.IOException
    * @throws org.jaudiotagger.audio.exceptions.InvalidAudioFrameException
    */
   public AudioFile readFileMagic(File f)
           throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException
   {
       checkFileExists(f);
       String ext = Utils.getMagicExtension(f);

       AudioFileReader afr = readers.get(ext);
       if (afr == null)
       {
           throw new CannotReadException(ErrorMessage.NO_READER_FOR_THIS_FORMAT.getMsg(ext));
       }

       AudioFile tempFile = afr.read(f);
       tempFile.setExt(ext);
       return tempFile;

   }

   /**
   *
   * Read the tag contained in the given file.
   * 
   *
   * @param f The file to read.
   * @param ext The extension to be used.
   * @return The AudioFile with the file tag and the file encoding info.
   * @throws org.jaudiotagger.audio.exceptions.CannotReadException If the file could not be read, the extension wasn't
   *                             recognized, or an IO error occurred during the read.
   * @throws org.jaudiotagger.tag.TagException
   * @throws org.jaudiotagger.audio.exceptions.ReadOnlyFileException
   * @throws java.io.IOException
   * @throws org.jaudiotagger.audio.exceptions.InvalidAudioFrameException
   */
  public AudioFile readFileAs(File f,String ext)
          throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException
  {
      checkFileExists(f);
//      String ext = Utils.getExtension(f);

      AudioFileReader afr = readers.get(ext);
      if (afr == null)
      {
          throw new CannotReadException(ErrorMessage.NO_READER_FOR_THIS_FORMAT.getMsg(ext));
      }

      AudioFile tempFile = afr.read(f);
      tempFile.setExt(ext);
      return tempFile;

  }

    /**
     * Check does file exist
     *
     * @param file
     * @throws java.io.FileNotFoundException
     */
    public void checkFileExists(File file)throws FileNotFoundException
    {
        logger.config("Reading file:" + "path" + file.getPath() + ":abs:" + file.getAbsolutePath());
        if (!file.exists())
        {
            logger.severe("Unable to find:" + file.getPath());
            throw new FileNotFoundException(ErrorMessage.UNABLE_TO_FIND_FILE.getMsg(file.getPath()));
        }
    }
}
