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

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

import com.levigo.jbig2.err.JBIG2Exception;

public class JBIG2DocumentFacade extends JBIG2Document {

  public JBIG2DocumentFacade(ImageInputStream input) throws IOException {
    super(input);
  }

  public JBIG2DocumentFacade(ImageInputStream input, JBIG2Globals globals) throws IOException {
    super(input, globals);
  }

  public JBIG2Page getPage(int pageNumber) {
    return super.getPage(pageNumber);
  }

  public Bitmap getPageBitmap(int pageNumber) throws JBIG2Exception, IOException {
    return getPage(pageNumber).getBitmap();
  }

}
