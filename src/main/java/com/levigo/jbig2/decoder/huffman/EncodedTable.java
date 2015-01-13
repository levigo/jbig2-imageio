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
import java.util.ArrayList;
import java.util.List;

import com.levigo.jbig2.io.SubInputStream;
import com.levigo.jbig2.segments.Table;

/**
 * This class represents a encoded huffman table.
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 * @author Benjamin Zindel
 */
public class EncodedTable extends HuffmanTable {

  private Table table;

  public EncodedTable(Table table) throws IOException {
    this.table = table;
    parseTable();
  }

  public void parseTable() throws IOException {

    SubInputStream sis = table.getSubInputStream();

    List<Code> codeTable = new ArrayList<Code>();

    int prefLen, rangeLen, rangeLow;
    int curRangeLow = table.getHtLow();

    /* Annex B.2 5) - decode table lines */
    while (curRangeLow < table.getHtHigh()) {
      prefLen = (int) sis.readBits(table.getHtPS());
      rangeLen = (int) sis.readBits(table.getHtRS());
      rangeLow = curRangeLow;

      codeTable.add(new Code(prefLen, rangeLen, rangeLow, false));

      curRangeLow += 1 << rangeLen;
    }

    /* Annex B.2 6) */
    prefLen = (int) sis.readBits(table.getHtPS());

    /*
     * Annex B.2 7) - lower range table line
     * 
     * Made some correction. Spec specifies an incorrect variable -> Replaced highPrefLen with
     * lowPrefLen
     */
    rangeLen = 32;
    rangeLow = table.getHtHigh() - 1;
    codeTable.add(new Code(prefLen, rangeLen, rangeLow, true));
    // }

    /* Annex B.2 8) */
    prefLen = (int) sis.readBits(table.getHtPS());

    /* Annex B.2 9) - upper range table line */
    rangeLen = 32;
    rangeLow = table.getHtHigh();
    codeTable.add(new Code(prefLen, rangeLen, rangeLow, false));

    /* Annex B.2 10) - out-of-band table line */
    if (table.getHtOOB() == 1) {
      prefLen = (int) sis.readBits(table.getHtPS());
      codeTable.add(new Code(prefLen, -1, -1, false));
    }

    System.out.println(codeTableToString(codeTable));

    initTree(codeTable);
  }
}