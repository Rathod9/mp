package org.jaudiotagger.tag.flac;

import org.jaudiotagger.audio.flac.metadatablock.MetadataBlockDataPicture;
import org.jaudiotagger.logging.ErrorMessage;
import org.jaudiotagger.tag.*;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentFieldKey;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Flac uses Vorbis Comment for most of its metadata and a Flac Picture Block for images
 *
 *
 * This class enscapulates the items into a single tag
 */
public class FlacTag implements Tag
{
    private VorbisCommentTag tag = null;
    private List<MetadataBlockDataPicture> images = new ArrayList<MetadataBlockDataPicture>();

    public FlacTag()
    {
        this(VorbisCommentTag.createNewTag(), new ArrayList< MetadataBlockDataPicture >());
    }

    public FlacTag(VorbisCommentTag tag, List<MetadataBlockDataPicture> images)
    {
        this.tag = tag;
        this.images = images;
    }

    /**
     * @return images
     */
    public List<MetadataBlockDataPicture> getImages()
    {
        return images;
    }

    /**
     * @return the vorbis tag (this is what handles text metadata)
     */
    public VorbisCommentTag getVorbisCommentTag()
    {
        return tag;
    }

    public void addField(TagField field) throws FieldDataInvalidException
    {
        if (field instanceof MetadataBlockDataPicture)
        {
            images.add((MetadataBlockDataPicture) field);
        }
        else
        {
            tag.addField(field);
        }
    }

    public List<TagField> getFields(String id)
    {
        if (id.equals(FieldKey.COVER_ART.name()))
        {
            List<TagField> castImages = new ArrayList<TagField>();
            for (MetadataBlockDataPicture image : images)
            {
                castImages.add(image);
            }
            return castImages;
        }
        else
        {
            return tag.getFields(id);
        }
    }

     /**
     * Maps the generic key to the specific key and return the list of values for this field as strings
     *
     * @param genericKey
     * @return
     * @throws KeyNotFoundException
     */
    public List<String> getAll(FieldKey genericKey) throws KeyNotFoundException
    {
        if (genericKey==FieldKey.COVER_ART)
        {
            throw new UnsupportedOperationException(ErrorMessage.ARTWORK_CANNOT_BE_CREATED_WITH_THIS_METHOD.getMsg());
        }
        else
        {
            return tag.getAll(genericKey);
        }
    }

    public boolean hasCommonFields()
    {
        return tag.hasCommonFields();
    }




    /**
     * Determines whether the tag has no fields specified.<br>
     *
     * <p>If there are no images we return empty if either there is no VorbisTag or if there is a
     * VorbisTag but it is empty
     *
     * @return <code>true</code> if tag contains no field.
     */
    public boolean isEmpty()
    {
        return (tag == null || tag.isEmpty()) && images.size() == 0;
    }

    public void setField(FieldKey genericKey, String value) throws KeyNotFoundException, FieldDataInvalidException
    {
        TagField tagfield = createField(genericKey,value);
        setField(tagfield);
    }

    public void addField(FieldKey genericKey, String value) throws KeyNotFoundException, FieldDataInvalidException
    {
        TagField tagfield = createField(genericKey,value);
        addField(tagfield);
    }

    /**
     * Create and add field with name of vorbisCommentkey
     * @param vorbisCommentKey
     * @param value
     * @throws KeyNotFoundException
     * @throws FieldDataInvalidException
     */
    public void addField(String vorbisCommentKey, String value) throws KeyNotFoundException, FieldDataInvalidException
    {
        TagField tagfield = createField(vorbisCommentKey,value);
        addField(tagfield);
    }

    /**
     * @param field
     * @throws FieldDataInvalidException
     */
    public void setField(TagField field) throws FieldDataInvalidException
    {
        if (field instanceof MetadataBlockDataPicture)
        {
            if (images.size() == 0)
            {
                images.add(0, (MetadataBlockDataPicture) field);
            }
            else
            {
                images.set(0, (MetadataBlockDataPicture) field);
            }
        }
        else
        {
            tag.setField(field);
        }
    }

    public TagField createField(FieldKey genericKey, String value) throws KeyNotFoundException, FieldDataInvalidException
    {
        if (genericKey.equals(FieldKey.COVER_ART))
        {
            throw new UnsupportedOperationException(ErrorMessage.ARTWORK_CANNOT_BE_CREATED_WITH_THIS_METHOD.getMsg());
        }
        else
        {
            return tag.createField(genericKey, value);
        }
    }

    /**
     * Create Tag Field using ogg key
     *
     * @param vorbisCommentFieldKey
     * @param value
     * @return
     * @throws org.jaudiotagger.tag.KeyNotFoundException
     * @throws org.jaudiotagger.tag.FieldDataInvalidException
     */
    public TagField createField(VorbisCommentFieldKey vorbisCommentFieldKey, String value) throws KeyNotFoundException,FieldDataInvalidException
    {
        if (vorbisCommentFieldKey.equals(VorbisCommentFieldKey.COVERART))
        {
            throw new UnsupportedOperationException(ErrorMessage.ARTWORK_CANNOT_BE_CREATED_WITH_THIS_METHOD.getMsg());
        }
        return tag.createField(vorbisCommentFieldKey,value);
    }

    /**
     * Create Tag Field using ogg key
     *
     * This method is provided to allow you to create key of any value because VorbisComment allows
     * arbitary keys.
     *
     * @param vorbisCommentFieldKey
     * @param value
     * @return
     */
    public TagField createField(String vorbisCommentFieldKey, String value)
    {
        if (vorbisCommentFieldKey.equals(VorbisCommentFieldKey.COVERART.getFieldName()))
        {
            throw new UnsupportedOperationException(ErrorMessage.ARTWORK_CANNOT_BE_CREATED_WITH_THIS_METHOD.getMsg());
        }
        return tag.createField(vorbisCommentFieldKey,value);
    }

    public String getFirst(String id)
    {
        if (id.equals(FieldKey.COVER_ART.name()))
        {
            throw new UnsupportedOperationException(ErrorMessage.ARTWORK_CANNOT_BE_CREATED_WITH_THIS_METHOD.getMsg());
        }
        else
        {
            return tag.getFirst(id);
        }
    }

    public String getValue(FieldKey id,int index) throws KeyNotFoundException
    {
        if (id.equals(FieldKey.COVER_ART))
        {
            throw new UnsupportedOperationException(ErrorMessage.ARTWORK_CANNOT_BE_RETRIEVED_WITH_THIS_METHOD.getMsg());
        }
        else
        {
            return tag.getValue(id, index);
        }
    }

    public String getFirst(FieldKey id) throws KeyNotFoundException
    {
        return getValue(id,0);
    }

    public TagField getFirstField(String id)
    {
        if (id.equals(FieldKey.COVER_ART.name()))
        {
            if (images.size() > 0)
            {
                return images.get(0);
            }
            else
            {
                return null;
            }
        }
        else
        {
            return tag.getFirstField(id);
        }
    }

    public TagField getFirstField(FieldKey genericKey) throws KeyNotFoundException
    {
        if (genericKey == null)
        {
            throw new KeyNotFoundException();
        }

        if(genericKey == FieldKey.COVER_ART )
        {
            return getFirstField(FieldKey.COVER_ART.name());
        }
        else
        {
            return tag.getFirstField(genericKey);            
        }
    }

    /**
     * Delete any instance of tag fields with this key
     *
     * @param fieldKey
     */
    public void deleteField(FieldKey fieldKey) throws KeyNotFoundException
    {
        if (fieldKey.equals(FieldKey.COVER_ART))
        {
            images.clear();
        }
        else
        {
            tag.deleteField(fieldKey);
        }
    }

    public void deleteField(String id) throws KeyNotFoundException
      {
          if (id.equals(FieldKey.COVER_ART.name()))
          {
              images.clear();
          }
          else
          {
              tag.deleteField(id);
          }
      }


    //TODO addField images to iterator
    public Iterator<TagField> getFields()
    {
        return tag.getFields();
    }

    public int getFieldCount()
    {
        return tag.getFieldCount() + images.size();
    }

    public int getFieldCountIncludingSubValues()
    {
       return getFieldCount();
    }

    @Override
    public boolean setEncoding(Charset enc) throws FieldDataInvalidException
    {
        return tag.setEncoding(enc);
    }

    public List<TagField> getFields(FieldKey id) throws KeyNotFoundException
    {
        if (id.equals(FieldKey.COVER_ART))
        {
            List<TagField> castImages = new ArrayList<TagField>();
            for (MetadataBlockDataPicture image : images)
            {
                castImages.add(image);
            }
            return castImages;
        }
        else
        {
            return tag.getFields(id);
        }
     }


     /**
     *
     * @param genericKey
     * @return
     */
    public boolean hasField(FieldKey genericKey)
    {
        if (genericKey==FieldKey.COVER_ART)
        {
            return images.size() > 0;
        }
        else
        {
            return tag.hasField(genericKey);
        }
    }

    /**
     *
     * @param vorbisFieldKey
     * @return
     */
    public boolean hasField(VorbisCommentFieldKey vorbisFieldKey)
    {
        return tag.hasField(vorbisFieldKey);
    }

    public boolean hasField(String id)
    {
       if (id.equals(FieldKey.COVER_ART.name()))
       {
           return images.size() > 0;
       }
       else
       {
           return tag.hasField(id);
       }
   }

    public TagField createCompilationField(boolean value) throws KeyNotFoundException, FieldDataInvalidException
    {
        return tag.createCompilationField(value);
    }
}