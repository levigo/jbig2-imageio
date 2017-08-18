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

package org.apache.pdfbox.jbig2.decoder.huffman;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.jbig2.io.SubInputStream;
import org.apache.pdfbox.jbig2.segments.Table;

/**
 * This class represents a encoded huffman table.
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