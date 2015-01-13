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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 * This is a utility class. It can be used to show intermediary results.
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 * 
 */
public class TestImage extends JFrame {
  private static final long serialVersionUID = 7353175320371957550L;

  public static void main(String[] args) {
    int w = 250;
    int h = 250;

    // (w+7) / 8 entspricht Aufrundung!
    int scanlineStride = (w + 7) / 8;

    // hier sind die Daten
    byte data[] = new byte[h * scanlineStride];

    // dummy-Daten erzeugen
    for (int i = 0; i < data.length; i++)
      data[i] = (byte) i;

    new TestImage(data, w, h, scanlineStride);
  }

  static class ImageComponent extends JComponent {
    private static final long serialVersionUID = -5921296548288376287L;
    Image myImage;
    int imgWidth = -1;
    int imgHeight = -1;
    Dimension prefSize = null;
    private int scale = 1;

    /**
     * Constructor for ImageComponent.
     */
    protected ImageComponent() {
      super();
    }

    /**
     * Constructor for ImageComponent.
     */
    public ImageComponent(Image image) {
      super();
      setImage(image);
    }

    /**
     * Gets the preffered Size of the Component
     * 
     * @param image java.awt.Image
     */
    public Dimension getPreferredSize() {
      if (prefSize != null)
        return this.prefSize;
      else
        return super.getPreferredSize();
    }

    /**
     * Gets the minimum Size of the Component
     * 
     * @param image java.awt.Image
     */
    public Dimension getMinimumSize() {
      if (prefSize != null)
        return prefSize;
      else
        return super.getMinimumSize();
    }

    /**
     * Sets an image to be shown
     * 
     * @param image java.awt.Image
     */
    public void setImage(Image image) {
      if (myImage != null) {
        myImage.flush();
      }

      myImage = image;

      if (myImage != null) {
        MediaTracker mt = new MediaTracker(this);

        mt.addImage(myImage, 0);

        try {
          mt.waitForAll();
        } catch (Exception ex) {
        }

        imgWidth = myImage.getWidth(this);
        imgHeight = myImage.getHeight(this);

        setSize(imgWidth * scale, imgHeight * scale);
        prefSize = getSize();
        invalidate();
        validate();
        repaint();
      }
    }

    /**
     * Get the Insets fo the Component
     * 
     * @return Insets the Insets of the Component
     */
    public Insets getInsets() {
      return new Insets(1, 1, 1, 1);
    }

    /**
     * Paints the component
     * 
     * @param g java.awt.Graphics
     */
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g;
      if (myImage != null) {
        g2.scale(scale, scale);
        g2.drawImage(myImage, 1, 1, imgWidth, imgHeight, this);
      }
    }

    public void setScale(int scale) {
      this.scale = scale;

      setSize(imgWidth * scale, imgHeight * scale);
      prefSize = getSize();

      revalidate();
      repaint();
    }

    public int getScale() {
      return scale;
    }
  }

  public TestImage(byte data[], int w, int h, int scanlineStride) {
    super("Demobild");

    // Color-Model sagt: bit = 0 -> schwarz, bit = 1 -> weiss. Ggf. umdrehen.
    ColorModel colorModel = new IndexColorModel(1, 2, new byte[]{
        (byte) 0xff, 0x00
    }, new byte[]{
        (byte) 0xff, 0x00
    }, new byte[]{
        (byte) 0xff, 0x00
    });

    DataBuffer dataBuffer = new DataBufferByte(data, data.length);
    SampleModel sampleModel = new MultiPixelPackedSampleModel(DataBuffer.TYPE_BYTE, w, h, 1, scanlineStride, 0);
    WritableRaster writableRaster = Raster.createWritableRaster(sampleModel, dataBuffer, new Point(0, 0));

    BufferedImage image = new BufferedImage(colorModel, writableRaster, false, null);

    ImageComponent imageComponent = new ImageComponent(image);
    // imageComponent.setScale(4);

    JScrollPane sp = new JScrollPane(imageComponent);

    setContentPane(sp);

    pack();
    setSize(new Dimension(1600, 900));
    setVisible(true);

    try {
      System.in.read();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public TestImage(BufferedImage bufferedImage) {
    super("Demobild");

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    ImageComponent imageComponent = new ImageComponent(bufferedImage);
    imageComponent.setScale(1);

    JScrollPane sp = new JScrollPane(imageComponent);

    setContentPane(sp);

    pack();
    setSize(new Dimension(1600, 900));
    setVisible(true);

    try {
      System.in.read();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
