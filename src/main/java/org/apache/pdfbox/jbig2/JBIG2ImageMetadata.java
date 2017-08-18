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

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.Node;

/**
 * @see IIOMetadata
 */
public class JBIG2ImageMetadata extends IIOMetadata {

  static final String IMAGE_METADATA_FORMAT_NAME = "jbig2";

  private final JBIG2Page page;

  public JBIG2ImageMetadata(JBIG2Page page) {
    super(true, IMAGE_METADATA_FORMAT_NAME, JBIG2ImageMetadataFormat.class.getName(), null, null);

    if (page == null)
      throw new IllegalArgumentException("page must not be null");

    this.page = page;
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  @Override
  public Node getAsTree(String formatName) {
    if (formatName.equals(nativeMetadataFormatName)) {
      return getNativeTree();
    } else if (formatName.equals(IIOMetadataFormatImpl.standardMetadataFormatName)) {
      return getStandardTree();
    } else {
      throw new IllegalArgumentException("Not a recognized format!");
    }
  }

  private Node getNativeTree() {
    IIOMetadataNode root = new IIOMetadataNode(nativeMetadataFormatName);
    root.appendChild(getStandardDimensionNode());
    return root;
  }

  @Override
  public IIOMetadataNode getStandardDimensionNode() {
    IIOMetadataNode dimensionNode = new IIOMetadataNode("Dimension");
    IIOMetadataNode node = null; // scratch node

    node = new IIOMetadataNode("PixelAspectRatio");
    node.setAttribute("value", "1.0");
    dimensionNode.appendChild(node);

    node = new IIOMetadataNode("ImageOrientation");
    node.setAttribute("value", "Normal");
    dimensionNode.appendChild(node);

    if (page.getResolutionX() != 0) {
      String pixelResolution = Float.toString(25.4f / (page.getResolutionX() / 39.3701f));

      node = new IIOMetadataNode("HorizontalPixelSize");
      node.setAttribute("value", pixelResolution);
      dimensionNode.appendChild(node);
    }

    if (page.getResolutionY() != 0) {
      String pixelResolution = Float.toString(25.4f / (page.getResolutionY() / 39.3701f));

      node = new IIOMetadataNode("VerticalPixelSize");
      node.setAttribute("value", pixelResolution);
      dimensionNode.appendChild(node);
    }

    return dimensionNode;
  }

  @Override
  public void mergeTree(String formatName, Node root) {
    throw new IllegalStateException("Metadata is read-only!");
  }

  @Override
  public void reset() {
    throw new IllegalStateException("Metadata is read-only!");
  }
}
