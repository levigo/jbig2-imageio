/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pdfbox.jbig2;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

/**
 * @see IIOMetadataFormat
 * @see IIOMetadataFormatImpl
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
