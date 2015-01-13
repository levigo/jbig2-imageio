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

package com.levigo.jbig2.segments;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;

import com.levigo.jbig2.Bitmap;
import com.levigo.jbig2.Dictionary;
import com.levigo.jbig2.JBIG2ImageReader;
import com.levigo.jbig2.Region;
import com.levigo.jbig2.SegmentHeader;
import com.levigo.jbig2.decoder.arithmetic.ArithmeticDecoder;
import com.levigo.jbig2.decoder.arithmetic.ArithmeticIntegerDecoder;
import com.levigo.jbig2.decoder.arithmetic.CX;
import com.levigo.jbig2.decoder.huffman.EncodedTable;
import com.levigo.jbig2.decoder.huffman.HuffmanTable;
import com.levigo.jbig2.decoder.huffman.StandardTables;
import com.levigo.jbig2.err.IntegerMaxValueException;
import com.levigo.jbig2.err.InvalidHeaderValueException;
import com.levigo.jbig2.image.Bitmaps;
import com.levigo.jbig2.io.SubInputStream;
import com.levigo.jbig2.util.log.Logger;
import com.levigo.jbig2.util.log.LoggerFactory;

/**
 * This class represents the data of segment type "Symbol dictionary". Parsing is described in
 * 7.4.2.1.1 - 7.4.1.1.5 and decoding procedure is described in 6.5.
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 * @author Benjamin Zindel
 */
public class SymbolDictionary implements Dictionary {

  private final Logger log = LoggerFactory.getLogger(SymbolDictionary.class);

  private SubInputStream subInputStream;

  /** Symbol dictionary flags, 7.4.2.1.1 */
  private short sdrTemplate;
  private byte sdTemplate;
  private boolean isCodingContextRetained;
  private boolean isCodingContextUsed;
  private short sdHuffAggInstanceSelection;
  private short sdHuffBMSizeSelection;
  private short sdHuffDecodeWidthSelection;
  private short sdHuffDecodeHeightSelection;
  private boolean useRefinementAggregation;
  private boolean isHuffmanEncoded;

  /** Symbol dictionary AT flags, 7.4.2.1.2 */
  private short[] sdATX;
  private short[] sdATY;

  /** Symbol dictionary refinement AT flags, 7.4.2.1.3 */
  private short[] sdrATX;
  private short[] sdrATY;

  /** Number of exported symbols, 7.4.2.1.4 */
  private int amountOfExportSymbolss;

  /** Number of new symbols, 7.4.2.1.5 */
  private int amountOfNewSymbolss;

  /** Further parameters */
  private SegmentHeader segmentHeader;
  private int amountOfImportedSymbolss;
  private ArrayList<Bitmap> importSymbols;
  private int amountOfDecodedSymbols;
  private Bitmap[] newSymbols;

  /** User-supplied tables * */
  private HuffmanTable dhTable;
  private HuffmanTable dwTable;
  private HuffmanTable bmSizeTable;
  private HuffmanTable aggInstTable;

  /** Return value of that segment */
  private ArrayList<Bitmap> exportSymbols;
  private ArrayList<Bitmap> sbSymbols;

  private ArithmeticDecoder arithmeticDecoder;
  private ArithmeticIntegerDecoder iDecoder;

  private TextRegion textRegion;
  private GenericRegion genericRegion;
  private GenericRefinementRegion genericRefinementRegion;
  private CX cx;

  private CX cxIADH;
  private CX cxIADW;
  private CX cxIAAI;
  private CX cxIAEX;
  private CX cxIARDX;
  private CX cxIARDY;
  private CX cxIADT;

  protected CX cxIAID;
  private int sbSymCodeLen;

  public SymbolDictionary() {
  }

  public SymbolDictionary(SubInputStream subInputStream, SegmentHeader segmentHeader) throws IOException {
    this.subInputStream = subInputStream;
    this.segmentHeader = segmentHeader;
  }

  private void parseHeader() throws IOException, InvalidHeaderValueException, IntegerMaxValueException {
    readRegionFlags();
    setAtPixels();
    setRefinementAtPixels();
    readAmountOfExportedSymbols();
    readAmountOfNewSymbols();
    setInSyms();

    if (isCodingContextUsed) {
      SegmentHeader[] rtSegments = segmentHeader.getRtSegments();

      for (int i = rtSegments.length - 1; i >= 0; i--) {

        if (rtSegments[i].getSegmentType() == 0) {
          SymbolDictionary symbolDictionary = (SymbolDictionary) rtSegments[i].getSegmentData();

          if (symbolDictionary.isCodingContextRetained) {
            /* 7.4.2.2 3) */
            setRetainedCodingContexts(symbolDictionary);
          }
          break;
        }
      }
    }

    this.checkInput();
  }

  private void readRegionFlags() throws IOException {
    /* Bit 13-15 */
    subInputStream.readBits(3); // Dirty read... reserved bits must be 0

    /* Bit 12 */
    sdrTemplate = (short) subInputStream.readBit();

    /* Bit 10-11 */
    sdTemplate = (byte) (subInputStream.readBits(2) & 0xf);

    /* Bit 9 */
    if (subInputStream.readBit() == 1) {
      isCodingContextRetained = true;
    }

    /* Bit 8 */
    if (subInputStream.readBit() == 1) {
      isCodingContextUsed = true;
    }

    /* Bit 7 */
    sdHuffAggInstanceSelection = (short) subInputStream.readBit();

    /* Bit 6 */
    sdHuffBMSizeSelection = (short) subInputStream.readBit();

    /* Bit 4-5 */
    sdHuffDecodeWidthSelection = (short) (subInputStream.readBits(2) & 0xf);

    /* Bit 2-3 */
    sdHuffDecodeHeightSelection = (short) (subInputStream.readBits(2) & 0xf);

    /* Bit 1 */
    if (subInputStream.readBit() == 1) {
      useRefinementAggregation = true;
    }

    /* Bit 0 */
    if (subInputStream.readBit() == 1) {
      isHuffmanEncoded = true;
    }
  }

  private void setAtPixels() throws IOException {
    if (!isHuffmanEncoded) {
      if (sdTemplate == 0) {
        readAtPixels(4);
      } else {
        readAtPixels(1);
      }
    }
  }

  private void setRefinementAtPixels() throws IOException {
    if (useRefinementAggregation && sdrTemplate == 0) {
      readRefinementAtPixels(2);
    }
  }

  private void readAtPixels(final int amountOfPixels) throws IOException {
    sdATX = new short[amountOfPixels];
    sdATY = new short[amountOfPixels];

    for (int i = 0; i < amountOfPixels; i++) {
      sdATX[i] = subInputStream.readByte();
      sdATY[i] = subInputStream.readByte();
    }
  }

  private void readRefinementAtPixels(final int amountOfAtPixels) throws IOException {
    sdrATX = new short[amountOfAtPixels];
    sdrATY = new short[amountOfAtPixels];

    for (int i = 0; i < amountOfAtPixels; i++) {
      sdrATX[i] = subInputStream.readByte();
      sdrATY[i] = subInputStream.readByte();
    }
  }

  private void readAmountOfExportedSymbols() throws IOException {
    amountOfExportSymbolss = (int) subInputStream.readBits(32); // & 0xffffffff;
  }

  private void readAmountOfNewSymbols() throws IOException {
    amountOfNewSymbolss = (int) subInputStream.readBits(32); // & 0xffffffff;
  }

  private void setInSyms() throws IOException, InvalidHeaderValueException, IntegerMaxValueException {
    if (segmentHeader.getRtSegments() != null) {
      retrieveImportSymbols();
    } else {
      importSymbols = new ArrayList<Bitmap>();
    }
  }

  private void setRetainedCodingContexts(final SymbolDictionary sd) {
    this.arithmeticDecoder = sd.arithmeticDecoder;
    this.isHuffmanEncoded = sd.isHuffmanEncoded;
    this.useRefinementAggregation = sd.useRefinementAggregation;
    this.sdTemplate = sd.sdTemplate;
    this.sdrTemplate = sd.sdrTemplate;
    this.sdATX = sd.sdATX;
    this.sdATY = sd.sdATY;
    this.sdrATX = sd.sdrATX;
    this.sdrATY = sd.sdrATY;
    this.cx = sd.cx;
  }

  private void checkInput() throws InvalidHeaderValueException {
    if (sdHuffDecodeHeightSelection == 2) {
      log.info("sdHuffDecodeHeightSelection = " + sdHuffDecodeHeightSelection + " (value not permitted)");
    }

    if (sdHuffDecodeWidthSelection == 2) {
      log.info("sdHuffDecodeWidthSelection = " + sdHuffDecodeWidthSelection + " (value not permitted)");
    }

    if (isHuffmanEncoded) {
      if (sdTemplate != 0) {
        log.info("sdTemplate = " + sdTemplate + " (should be 0)");
        sdTemplate = 0;
      }
      if (!useRefinementAggregation) {
        if (isCodingContextRetained) {
          log.info("isCodingContextRetained = " + isCodingContextRetained + " (should be 0)");
          isCodingContextRetained = false;
        }

        if (isCodingContextUsed) {
          log.info("isCodingContextUsed = " + isCodingContextUsed + " (should be 0)");
          isCodingContextUsed = false;
        }
      }

    } else {
      if (sdHuffBMSizeSelection != 0) {
        log.info("sdHuffBMSizeSelection should be 0");
        sdHuffBMSizeSelection = 0;
      }
      if (sdHuffDecodeWidthSelection != 0) {
        log.info("sdHuffDecodeWidthSelection should be 0");
        sdHuffDecodeWidthSelection = 0;
      }
      if (sdHuffDecodeHeightSelection != 0) {
        log.info("sdHuffDecodeHeightSelection should be 0");
        sdHuffDecodeHeightSelection = 0;
      }
    }

    if (!useRefinementAggregation) {
      if (sdrTemplate != 0) {
        log.info("sdrTemplate = " + sdrTemplate + " (should be 0)");
        sdrTemplate = 0;
      }
    }

    if (!isHuffmanEncoded || !useRefinementAggregation) {
      if (sdHuffAggInstanceSelection != 0) {
        log.info("sdHuffAggInstanceSelection = " + sdHuffAggInstanceSelection + " (should be 0)");
        sdHuffAggInstanceSelection = 0;
      }
    }
  }

  /**
   * 6.5.5 Decoding the symbol dictionary
   * 
   * @return List of decoded symbol bitmaps as an <code>ArrayList</code>
   */
  public ArrayList<Bitmap> getDictionary() throws IOException, IntegerMaxValueException, InvalidHeaderValueException {
    long timestamp = System.currentTimeMillis();
    if (null == exportSymbols) {

      if (useRefinementAggregation)
        sbSymCodeLen = getSbSymCodeLen();

      if (!isHuffmanEncoded) {
        setCodingStatistics();
      }

      /* 6.5.5 1) */
      newSymbols = new Bitmap[amountOfNewSymbolss];

      /* 6.5.5 2) */
      int[] newSymbolsWidths = null;
      if (isHuffmanEncoded && !useRefinementAggregation) {
        newSymbolsWidths = new int[amountOfNewSymbolss];
      }

      setSymbolsArray();

      /* 6.5.5 3) */
      int heightClassHeight = 0;
      amountOfDecodedSymbols = 0;

      /* 6.5.5 4 a) */
      while (amountOfDecodedSymbols != amountOfNewSymbolss) {

        /* 6.5.5 4 b) */
        heightClassHeight += decodeHeightClassDeltaHeight();
        int symbolWidth = 0;
        int totalWidth = 0;
        final int heightClassFirstSymbolIndex = amountOfDecodedSymbols;

        /* 6.5.5 4 c) */

        // Repeat until OOB - OOB sends a break;
        while (true) {
          /* 4 c) i) */
          final long differenceWidth = decodeDifferenceWidth();

          // If result is OOB, then all the symbols in this height
          // class has been decoded; proceed to step 4 d)
          if (differenceWidth == Long.MAX_VALUE) {
            break;
          }

          symbolWidth += differenceWidth;
          totalWidth += symbolWidth;

          /* 4 c) ii) */
          if (!isHuffmanEncoded || useRefinementAggregation) {
            if (!useRefinementAggregation) {
              // 6.5.8.1 - Direct coded
              decodeDirectlyThroughGenericRegion(symbolWidth, heightClassHeight);
            } else {
              // 6.5.8.2 - Refinement/Aggregate-coded
              decodeAggregate(symbolWidth, heightClassHeight);
            }
          } else if (isHuffmanEncoded && !useRefinementAggregation) {
            /* 4 c) iii) */
            newSymbolsWidths[amountOfDecodedSymbols] = symbolWidth;
          }
          amountOfDecodedSymbols++;
        }

        /* 6.5.5 4 d) */
        if (isHuffmanEncoded && !useRefinementAggregation) {
          /* 6.5.9 */
          final long bmSize;
          if (sdHuffBMSizeSelection == 0) {
            bmSize = StandardTables.getTable(1).decode(subInputStream);
          } else {
            bmSize = huffDecodeBmSize();
          }

          subInputStream.skipBits();

          final Bitmap heightClassCollectiveBitmap = decodeHeightClassCollectiveBitmap(bmSize, heightClassHeight,
              totalWidth);

          subInputStream.skipBits();
          decodeHeightClassBitmap(heightClassCollectiveBitmap, heightClassFirstSymbolIndex, heightClassHeight,
              newSymbolsWidths);
        }
      }

      /* 5) */
      /* 6.5.10 1) - 5) */

      final int[] exFlags = getToExportFlags();

      /* 6.5.10 6) - 8) */
      setExportedSymbols(exFlags);
    }

    if (JBIG2ImageReader.PERFORMANCE_TEST)
      log.info("SYMBOL DECODING: " + (System.currentTimeMillis() - timestamp) + " ms");

    // DictionaryViewer.viewSymbols(sdExSyms);

    return exportSymbols;
  }

  private void setCodingStatistics() throws IOException {
    if (cxIADT == null) {
      cxIADT = new CX(512, 1);
    }

    if (cxIADH == null) {
      cxIADH = new CX(512, 1);
    }

    if (cxIADW == null) {
      cxIADW = new CX(512, 1);
    }

    if (cxIAAI == null) {
      cxIAAI = new CX(512, 1);
    }

    if (cxIAEX == null) {
      cxIAEX = new CX(512, 1);
    }

    if (useRefinementAggregation && cxIAID == null) {
      cxIAID = new CX(1 << sbSymCodeLen, 1);
      cxIARDX = new CX(512, 1);
      cxIARDY = new CX(512, 1);
    }

    if (cx == null) {
      cx = new CX(65536, 1);
    }

    if (arithmeticDecoder == null) {
      arithmeticDecoder = new ArithmeticDecoder(subInputStream);
    }

    if (iDecoder == null) {
      iDecoder = new ArithmeticIntegerDecoder(arithmeticDecoder);
    }

  }

  private final void decodeHeightClassBitmap(final Bitmap heightClassCollectiveBitmap,
      final int heightClassFirstSymbol, final int heightClassHeight, final int[] newSymbolsWidths)
      throws IntegerMaxValueException, InvalidHeaderValueException, IOException {

    for (int i = heightClassFirstSymbol; i < amountOfDecodedSymbols; i++) {
      int startColumn = 0;

      for (int j = heightClassFirstSymbol; j <= i - 1; j++) {
        startColumn += newSymbolsWidths[j];
      }

      final Rectangle roi = new Rectangle(startColumn, 0, newSymbolsWidths[i], heightClassHeight);
      final Bitmap symbolBitmap = Bitmaps.extract(roi, heightClassCollectiveBitmap);
      newSymbols[i] = symbolBitmap;
      sbSymbols.add(symbolBitmap);
    }
  }

  private final void decodeAggregate(final int symbolWidth, final int heightClassHeight) throws IOException,
      InvalidHeaderValueException, IntegerMaxValueException {
    // 6.5.8.2 1)
    // 6.5.8.2.1 - Number of symbol instances in aggregation
    final long amountOfRefinementAggregationInstances;
    if (isHuffmanEncoded) {
      log.info("Refinement or aggregate-coded symbols may couse problems with huffman decoding!");
      amountOfRefinementAggregationInstances = huffDecodeRefAggNInst();
    } else {
      amountOfRefinementAggregationInstances = iDecoder.decode(cxIAAI);
    }

    if (amountOfRefinementAggregationInstances > 1) {
      // 6.5.8.2 2)
      decodeThroughTextRegion(symbolWidth, heightClassHeight, amountOfRefinementAggregationInstances);
    } else if (amountOfRefinementAggregationInstances == 1) {
      // 6.5.8.2 3) refers to 6.5.8.2.2
      decodeRefinedSymbol(symbolWidth, heightClassHeight);
    }
  }

  private final long huffDecodeRefAggNInst() throws IOException, InvalidHeaderValueException {
    if (sdHuffAggInstanceSelection == 0) {
      return StandardTables.getTable(1).decode(subInputStream);
    } else if (sdHuffAggInstanceSelection == 1) {
      if (aggInstTable == null) {
        int aggregationInstanceNumber = 0;

        if (sdHuffDecodeHeightSelection == 3) {
          aggregationInstanceNumber++;
        }
        if (sdHuffDecodeWidthSelection == 3) {
          aggregationInstanceNumber++;
        }
        if (sdHuffBMSizeSelection == 3) {
          aggregationInstanceNumber++;
        }

        aggInstTable = getUserTable(aggregationInstanceNumber);
      }
      return aggInstTable.decode(subInputStream);
    }
    return 0;
  }

  private final void decodeThroughTextRegion(final int symbolWidth, final int heightClassHeight,
      final long amountOfRefinementAggregationInstances) throws IOException, IntegerMaxValueException,
      InvalidHeaderValueException {
    if (textRegion == null) {
      textRegion = new TextRegion(subInputStream, null);

      textRegion.setContexts(cx, // default context
          new CX(512, 1), // IADT
          new CX(512, 1), // IAFS
          new CX(512, 1), // IADS
          new CX(512, 1), // IAIT
          cxIAID, // IAID
          new CX(512, 1), // IARDW
          new CX(512, 1), // IARDH
          new CX(512, 1), // IARDX
          new CX(512, 1) // IARDY
      );
    }

    // 6.5.8.2.4 Concatenating the array used as parameter later.
    setSymbolsArray();

    // 6.5.8.2 2) Parameters set according to Table 17, page 36
    textRegion.setParameters(arithmeticDecoder, iDecoder, isHuffmanEncoded, true, symbolWidth, heightClassHeight,
        amountOfRefinementAggregationInstances, 1, (amountOfImportedSymbolss + amountOfDecodedSymbols), (short) 0,
        (short) 0, (short) 0, (short) 1, (short) 0, (short) 0, (short) 0, (short) 0, (short) 0, (short) 0, (short) 0,
        (short) 0, (short) 0, sdrTemplate, sdrATX, sdrATY, sbSymbols, sbSymCodeLen);

    addSymbol(textRegion);
  }

  private final void decodeRefinedSymbol(final int symbolWidth, final int heightClassHeight) throws IOException,
      InvalidHeaderValueException, IntegerMaxValueException {

    final int id;
    final int rdx;
    final int rdy;
    // long symInRefSize = 0;
    if (isHuffmanEncoded) {
      /* 2) - 4) */
      id = (int) subInputStream.readBits(sbSymCodeLen);
      rdx = (int) StandardTables.getTable(15).decode(subInputStream);
      rdy = (int) StandardTables.getTable(15).decode(subInputStream);

      /* 5) a) */
      /* symInRefSize = */StandardTables.getTable(1).decode(subInputStream);

      /* 5) b) - Skip over remaining bits */
      subInputStream.skipBits();
    } else {
      /* 2) - 4) */
      id = iDecoder.decodeIAID(cxIAID, sbSymCodeLen);
      rdx = (int) iDecoder.decode(cxIARDX);
      rdy = (int) iDecoder.decode(cxIARDY);
    }

    /* 6) */
    setSymbolsArray();
    final Bitmap ibo = sbSymbols.get(id);
    decodeNewSymbols(symbolWidth, heightClassHeight, ibo, rdx, rdy);

    /* 7) */
    if (isHuffmanEncoded) {
      subInputStream.skipBits();
      // Make sure that the processed bytes are equal to the value read in step 5 a)
    }
  }

  private final void decodeNewSymbols(final int symWidth, final int hcHeight, final Bitmap ibo, final int rdx,
      final int rdy) throws IOException, InvalidHeaderValueException, IntegerMaxValueException {
    if (genericRefinementRegion == null) {
      genericRefinementRegion = new GenericRefinementRegion(subInputStream);

      if (arithmeticDecoder == null) {
        arithmeticDecoder = new ArithmeticDecoder(subInputStream);
      }

      if (cx == null) {
        cx = new CX(65536, 1);
      }
    }

    // Parameters as shown in Table 18, page 36
    genericRefinementRegion.setParameters(cx, arithmeticDecoder, sdrTemplate, symWidth, hcHeight, ibo, rdx, rdy, false,
        sdrATX, sdrATY);

    addSymbol(genericRefinementRegion);
  }

  private final void decodeDirectlyThroughGenericRegion(final int symWidth, final int hcHeight) throws IOException,
      IntegerMaxValueException, InvalidHeaderValueException {
    if (genericRegion == null) {
      genericRegion = new GenericRegion(subInputStream);
    }

    // Parameters set according to Table 16, page 35
    genericRegion.setParameters(false, sdTemplate, false, false, sdATX, sdATY, symWidth, hcHeight, cx,
        arithmeticDecoder);

    addSymbol(genericRegion);
  }

  private final void addSymbol(final Region region) throws IntegerMaxValueException, InvalidHeaderValueException,
      IOException {
    final Bitmap symbol = region.getRegionBitmap();
    newSymbols[amountOfDecodedSymbols] = symbol;
    sbSymbols.add(symbol);
  }

  private final long decodeDifferenceWidth() throws IOException, InvalidHeaderValueException {
    if (isHuffmanEncoded) {
      switch (sdHuffDecodeWidthSelection){
        case 0 :
          return StandardTables.getTable(2).decode(subInputStream);
        case 1 :
          return StandardTables.getTable(3).decode(subInputStream);
        case 3 :
          if (dwTable == null) {
            int dwNr = 0;

            if (sdHuffDecodeHeightSelection == 3) {
              dwNr++;
            }
            dwTable = getUserTable(dwNr);
          }

          return dwTable.decode(subInputStream);
      }
    } else {
      return iDecoder.decode(cxIADW);
    }
    return 0;
  }

  private final long decodeHeightClassDeltaHeight() throws IOException, InvalidHeaderValueException {
    if (isHuffmanEncoded) {
      return decodeHeightClassDeltaHeightWithHuffman();
    } else {
      return iDecoder.decode(cxIADH);
    }
  }

  /**
   * 6.5.6 if isHuffmanEncoded
   * 
   * @return long - Result of decoding HCDH
   * @throws IOException
   * @throws InvalidHeaderValueException
   */
  private final long decodeHeightClassDeltaHeightWithHuffman() throws IOException, InvalidHeaderValueException {
    switch (sdHuffDecodeHeightSelection){
      case 0 :
        return StandardTables.getTable(4).decode(subInputStream);
      case 1 :
        return StandardTables.getTable(5).decode(subInputStream);
      case 3 :
        if (dhTable == null) {
          dhTable = getUserTable(0);
        }
        return dhTable.decode(subInputStream);
    }

    return 0;
  }

  private final Bitmap decodeHeightClassCollectiveBitmap(final long bmSize, final int heightClassHeight,
      final int totalWidth) throws IOException {
    if (bmSize == 0) {
      final Bitmap heightClassCollectiveBitmap = new Bitmap(totalWidth, heightClassHeight);

      for (int i = 0; i < heightClassCollectiveBitmap.getByteArray().length; i++) {
        heightClassCollectiveBitmap.setByte(i, subInputStream.readByte());
      }

      return heightClassCollectiveBitmap;
    } else {
      if (genericRegion == null) {
        genericRegion = new GenericRegion(subInputStream);
      }

      genericRegion.setParameters(true, subInputStream.getStreamPosition(), bmSize, heightClassHeight, totalWidth);

      return genericRegion.getRegionBitmap();
    }
  }

  private void setExportedSymbols(int[] toExportFlags) {
    exportSymbols = new ArrayList<Bitmap>(amountOfExportSymbolss);

    for (int i = 0; i < amountOfImportedSymbolss + amountOfNewSymbolss; i++) {

      if (toExportFlags[i] == 1) {
        if (i < amountOfImportedSymbolss) {
          exportSymbols.add(importSymbols.get(i));
        } else {
          exportSymbols.add(newSymbols[i - amountOfImportedSymbolss]);
        }
      }
    }
  }

  private int[] getToExportFlags() throws IOException, InvalidHeaderValueException {
    int currentExportFlag = 0;
    long exRunLength = 0;
    final int[] exportFlags = new int[amountOfImportedSymbolss + amountOfNewSymbolss];

    for (int exportIndex = 0; exportIndex < amountOfImportedSymbolss + amountOfNewSymbolss; exportIndex += exRunLength) {

      if (isHuffmanEncoded) {
        exRunLength = StandardTables.getTable(1).decode(subInputStream);
      } else {
        exRunLength = iDecoder.decode(cxIAEX);
      }

      if (exRunLength != 0) {
        for (int index = exportIndex; index < exportIndex + exRunLength; index++) {
          exportFlags[index] = currentExportFlag;
        }
      }

      currentExportFlag = (currentExportFlag == 0) ? 1 : 0;
    }

    return exportFlags;
  }

  private final long huffDecodeBmSize() throws IOException, InvalidHeaderValueException {
    if (bmSizeTable == null) {
      int bmNr = 0;

      if (sdHuffDecodeHeightSelection == 3) {
        bmNr++;
      }

      if (sdHuffDecodeWidthSelection == 3) {
        bmNr++;
      }

      bmSizeTable = getUserTable(bmNr);
    }
    return bmSizeTable.decode(subInputStream);
  }

  /**
   * 6.5.8.2.3 - Setting SBSYMCODES and SBSYMCODELEN
   * 
   * @return Result of computing SBSYMCODELEN
   * @throws IOException
   */
  private int getSbSymCodeLen() throws IOException {
    if (isHuffmanEncoded) {
      return Math.max((int) (Math.ceil(Math.log(amountOfImportedSymbolss + amountOfNewSymbolss) / Math.log(2))), 1);
    } else {
      return (int) (Math.ceil(Math.log(amountOfImportedSymbolss + amountOfNewSymbolss) / Math.log(2)));
    }
  }

  /**
   * 6.5.8.2.4 - Setting SBSYMS
   * 
   * @throws IOException
   * @throws InvalidHeaderValueException
   * @throws IntegerMaxValueException
   */
  private final void setSymbolsArray() throws IOException, InvalidHeaderValueException, IntegerMaxValueException {
    if (importSymbols == null) {
      retrieveImportSymbols();
    }

    if (sbSymbols == null) {
      sbSymbols = new ArrayList<Bitmap>();
      sbSymbols.addAll(importSymbols);
    }
  }

  /**
   * Concatenates symbols from all referred-to segments.
   * 
   * @throws IOException
   * @throws InvalidHeaderValueException
   * @throws IntegerMaxValueException
   */
  private void retrieveImportSymbols() throws IOException, InvalidHeaderValueException, IntegerMaxValueException {
    importSymbols = new ArrayList<Bitmap>();
    for (final SegmentHeader referredToSegmentHeader : segmentHeader.getRtSegments()) {
      if (referredToSegmentHeader.getSegmentType() == 0) {
        final SymbolDictionary sd = (SymbolDictionary) referredToSegmentHeader.getSegmentData();
        importSymbols.addAll(sd.getDictionary());
        amountOfImportedSymbolss += sd.amountOfExportSymbolss;
      }
    }
  }

  private HuffmanTable getUserTable(final int tablePosition) throws InvalidHeaderValueException, IOException {
    int tableCounter = 0;

    for (final SegmentHeader referredToSegmentHeader : segmentHeader.getRtSegments()) {
      if (referredToSegmentHeader.getSegmentType() == 53) {
        if (tableCounter == tablePosition) {
          final Table t = (Table) referredToSegmentHeader.getSegmentData();
          return new EncodedTable(t);
        } else {
          tableCounter++;
        }
      }
    }
    return null;
  }

  public void init(final SegmentHeader header, final SubInputStream sis) throws InvalidHeaderValueException,
      IntegerMaxValueException, IOException {
    this.subInputStream = sis;
    this.segmentHeader = header;
    parseHeader();
  }
}
