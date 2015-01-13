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

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.Node;

/**
 * @see IIOMetadata
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 * 
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
