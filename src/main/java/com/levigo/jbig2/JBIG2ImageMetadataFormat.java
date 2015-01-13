/**
 * Copyright (C) 1995-2015 levigo holding gmbh.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.levigo.jbig2;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

/**
 * @see IIOMetadataFormat
 * @see IIOMetadataFormatImpl
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 * 
 */
public class JBIG2ImageMetadataFormat extends IIOMetadataFormatImpl {

  private static IIOMetadataFormat instance = null;

  private JBIG2ImageMetadataFormat() {
    super(JBIG2ImageMetadata.IMAGE_METADATA_FORMAT_NAME, CHILD_POLICY_SOME);

    // root -> ImageDescriptor
    addElement("ImageDescriptor", JBIG2ImageMetadata.IMAGE_METADATA_FORMAT_NAME, CHILD_POLICY_EMPTY);
    addAttribute("ImageDescriptor", "imageWidth", DATATYPE_INTEGER, true, null, "1", "65535", true, true);
    addAttribute("ImageDescriptor", "imageHeight", DATATYPE_INTEGER, true, null, "1", "65535", true, true);
    addAttribute("ImageDescriptor", "Xdensity", DATATYPE_FLOAT, true, null, "1", "65535", true, true);
    addAttribute("ImageDescriptor", "Ydensity", DATATYPE_FLOAT, true, null, "1", "65535", true, true);
  }

  public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) {
    return true;
  }

  public static synchronized IIOMetadataFormat getInstance() {
    if (instance == null) {
      instance = new JBIG2ImageMetadataFormat();
    }
    return instance;
  }
}
