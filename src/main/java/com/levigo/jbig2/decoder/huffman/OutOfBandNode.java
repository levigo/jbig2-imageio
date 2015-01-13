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

package com.levigo.jbig2.decoder.huffman;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

import com.levigo.jbig2.decoder.huffman.HuffmanTable.Code;


/**
 * Represents a out of band node in a huffman tree.
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 * @author Benjamin Zindel
 */
class OutOfBandNode extends Node {

  protected OutOfBandNode(Code c) {
  }

  @Override
  protected long decode(ImageInputStream iis) throws IOException {
    return Long.MAX_VALUE;
  }

}
