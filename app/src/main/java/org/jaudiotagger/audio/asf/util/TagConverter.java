/*
 * Entagged Audio Tag library
 * Copyright (c) 2004-2005 Christian Laireiter <liree@web.de>
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
package org.jaudiotagger.audio.asf.util;

import org.jaudiotagger.audio.asf.data.*;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.asf.*;
import org.jaudiotagger.tag.reference.GenreTypes;

import java.util.Iterator;
import java.util.List;

/**
 * This class provides functionality to convert
 * {@link org.jaudiotagger.audio.asf.data.AsfHeader}objects into
 * {@link org.jaudiotagger.tag.Tag}objects.<br>
 * 
 * @author Christian Laireiter (liree)
 */
public final class TagConverter {

    /**
     * This method creates a {@link Tag}and fills it with the contents of the
     * given {@link AsfHeader}.<br>
     * 
     * @param source
     *            The ASF header which contains the information. <br>
     * @return A Tag with all its values.
     */
    public static AsfTag createTagOf(AsfHeader source) {
        // TODO do we need to copy here.
        AsfTag result = new AsfTag(true);
        for (int i = 0; i < ContainerType.values().length; i++) {
            MetadataContainer current = source
                    .findMetadataContainer(ContainerType.values()[i]);
            if (current != null) {
                List<MetadataDescriptor> descriptors = current.getDescriptors();
                for (MetadataDescriptor descriptor : descriptors) {
                    AsfTagField toAdd;
                    if (descriptor.getType() == MetadataDescriptor.TYPE_BINARY) {
                        if (descriptor.getName().equals(
                                AsfFieldKey.COVER_ART.getFieldName())) {
                            toAdd = new AsfTagCoverField(descriptor);
                        } else if (descriptor.getName().equals(
                                AsfFieldKey.BANNER_IMAGE.getFieldName())) {
                            toAdd = new AsfTagBannerField(descriptor);
                        } else {
                            toAdd = new AsfTagField(descriptor);
                        }
                    } else {
                        toAdd = new AsfTagTextField(descriptor);
                    }
                    result.addField(toAdd);
                }
            }
        }
        return result;
    }


    /**
     * Hidden utility class constructor.
     */
    private TagConverter() {
        // Nothing to do.
    }

}