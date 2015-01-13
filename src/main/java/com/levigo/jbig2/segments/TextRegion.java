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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.levigo.jbig2.Bitmap;
import com.levigo.jbig2.JBIG2ImageReader;
import com.levigo.jbig2.Region;
import com.levigo.jbig2.SegmentHeader;
import com.levigo.jbig2.decoder.arithmetic.ArithmeticDecoder;
import com.levigo.jbig2.decoder.arithmetic.ArithmeticIntegerDecoder;
import com.levigo.jbig2.decoder.arithmetic.CX;
import com.levigo.jbig2.decoder.huffman.EncodedTable;
import com.levigo.jbig2.decoder.huffman.FixedSizeTable;
import com.levigo.jbig2.decoder.huffman.HuffmanTable;
import com.levigo.jbig2.decoder.huffman.StandardTables;
import com.levigo.jbig2.decoder.huffman.HuffmanTable.Code;
import com.levigo.jbig2.err.IntegerMaxValueException;
import com.levigo.jbig2.err.InvalidHeaderValueException;
import com.levigo.jbig2.image.Bitmaps;
import com.levigo.jbig2.io.SubInputStream;
import com.levigo.jbig2.util.CombinationOperator;
import com.levigo.jbig2.util.log.Logger;
import com.levigo.jbig2.util.log.LoggerFactory;

/**
 * This class represented the segment type "Text region", 7.4.3, page 56.
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 * @author Benjamin Zindel
 */
public class TextRegion implements Region {

  private final Logger log = LoggerFactory.getLogger(TextRegion.class);

  private SubInputStream subInputStream;

  /** Region segment information field, 7.4.1 */
  private RegionSegmentInformation regionInfo;

  /** Text region segment flags, 7.4.3.1.1 */
  private short sbrTemplate;
  private short sbdsOffset; /* 6.4.8 */
  private short defaultPixel;
  private CombinationOperator combinationOperator;
  private short isTransposed;
  private short referenceCorner;
  private short logSBStrips;
  private boolean useRefinement;
  private boolean isHuffmanEncoded;

  /** Text region segment Huffman flags, 7.4.3.1.2 */
  private short sbHuffRSize;
  private short sbHuffRDY;
  private short sbHuffRDX;
  private short sbHuffRDHeight;
  private short sbHuffRDWidth;
  private short sbHuffDT;
  private short sbHuffDS;
  private short sbHuffFS;

  /** Text region refinement AT flags, 7.4.3.1.3 */
  private short sbrATX[];
  private short sbrATY[];

  /** Number of symbol instances, 7.4.3.1.4 */
  private long amountOfSymbolInstances;

  /** Further parameters */
  private long currentS;
  private int sbStrips;
  private int amountOfSymbols;

  private Bitmap regionBitmap;
  private ArrayList<Bitmap> symbols = new ArrayList<Bitmap>();

  private ArithmeticDecoder arithmeticDecoder;
  private ArithmeticIntegerDecoder integerDecoder;
  private GenericRefinementRegion genericRefinementRegion;

  private CX cxIADT;
  private CX cxIAFS;
  private CX cxIADS;
  private CX cxIAIT;
  private CX cxIARI;
  private CX cxIARDW;
  private CX cxIARDH;
  private CX cxIAID;
  private CX cxIARDX;
  private CX cxIARDY;
  private CX cx;

  /** codeTable including a code to each symbol used in that region */
  private int symbolCodeLength;
  private FixedSizeTable symbolCodeTable;
  private SegmentHeader segmentHeader;

  /** User-supplied tables * */
  private HuffmanTable fsTable;
  private HuffmanTable dsTable;
  private HuffmanTable table;
  private HuffmanTable rdwTable;
  private HuffmanTable rdhTable;
  private HuffmanTable rdxTable;
  private HuffmanTable rdyTable;
  private HuffmanTable rSizeTable;

  public TextRegion() {
  }

  public TextRegion(SubInputStream subInputStream, SegmentHeader segmentHeader) {
    this.subInputStream = subInputStream;
    this.regionInfo = new RegionSegmentInformation(subInputStream);
    this.segmentHeader = segmentHeader;
  }

  private void parseHeader() throws IOException, InvalidHeaderValueException, IntegerMaxValueException {

    regionInfo.parseHeader();

    readRegionFlags();

    if (isHuffmanEncoded) {
      readHuffmanFlags();
    }

    readUseRefinement();

    readAmountOfSymbolInstances();

    /* 7.4.3.1.7 */
    getSymbols();

    computeSymbolCodeLength();

    this.checkInput();
  }

  private void readRegionFlags() throws IOException {
    /* Bit 15 */
    sbrTemplate = (short) subInputStream.readBit();

    /* Bit 10-14 */
    sbdsOffset = (short) (subInputStream.readBits(5));
    if (sbdsOffset > 0x0f) {
      sbdsOffset -= 0x20;
    }

    /* Bit 9 */
    defaultPixel = (short) subInputStream.readBit();

    /* Bit 7-8 */
    combinationOperator = CombinationOperator.translateOperatorCodeToEnum((short) (subInputStream.readBits(2) & 0x3));

    /* Bit 6 */
    isTransposed = (short) subInputStream.readBit();

    /* Bit 4-5 */
    referenceCorner = (short) (subInputStream.readBits(2) & 0x3);

    /* Bit 2-3 */
    logSBStrips = (short) (subInputStream.readBits(2) & 0x3);
    sbStrips = (1 << logSBStrips);

    /* Bit 1 */
    if (subInputStream.readBit() == 1) {
      useRefinement = true;
    }

    /* Bit 0 */
    if (subInputStream.readBit() == 1) {
      isHuffmanEncoded = true;
    }
  }

  private void readHuffmanFlags() throws IOException {
    /* Bit 15 */
    subInputStream.readBit(); // Dirty read...

    /* Bit 14 */
    sbHuffRSize = (short) subInputStream.readBit();

    /* Bit 12-13 */
    sbHuffRDY = (short) (subInputStream.readBits(2) & 0xf);

    /* Bit 10-11 */
    sbHuffRDX = (short) (subInputStream.readBits(2) & 0xf);

    /* Bit 8-9 */
    sbHuffRDHeight = (short) (subInputStream.readBits(2) & 0xf);

    /* Bit 6-7 */
    sbHuffRDWidth = (short) (subInputStream.readBits(2) & 0xf);

    /* Bit 4-5 */
    sbHuffDT = (short) (subInputStream.readBits(2) & 0xf);

    /* Bit 2-3 */
    sbHuffDS = (short) (subInputStream.readBits(2) & 0xf);

    /* Bit 0-1 */
    sbHuffFS = (short) (subInputStream.readBits(2) & 0xf);
  }

  private void readUseRefinement() throws IOException {
    if (useRefinement && sbrTemplate == 0) {
      sbrATX = new short[2];
      sbrATY = new short[2];

      /* Byte 0 */
      sbrATX[0] = subInputStream.readByte();

      /* Byte 1 */
      sbrATY[0] = subInputStream.readByte();

      /* Byte 2 */
      sbrATX[1] = subInputStream.readByte();

      /* Byte 3 */
      sbrATY[1] = subInputStream.readByte();
    }
  }

  private void readAmountOfSymbolInstances() throws IOException {
    amountOfSymbolInstances = subInputStream.readBits(32) & 0xffffffff;
  }

  private void getSymbols() throws IOException, IntegerMaxValueException, InvalidHeaderValueException {
    if (segmentHeader.getRtSegments() != null) {
      initSymbols();
    }
  }

  private void computeSymbolCodeLength() throws IOException {
    if (isHuffmanEncoded) {
      symbolIDCodeLengths();
    } else {
      symbolCodeLength = (int) Math.ceil((Math.log(amountOfSymbols) / Math.log(2)));
    }
  }

  private void checkInput() throws InvalidHeaderValueException {

    if (!useRefinement) {
      if (sbrTemplate != 0) {
        log.info("sbrTemplate should be 0");
        sbrTemplate = 0;
      }
    }

    if (sbHuffFS == 2 || sbHuffRDWidth == 2 || sbHuffRDHeight == 2 || sbHuffRDX == 2 || sbHuffRDY == 2) {
      throw new InvalidHeaderValueException("Huffman flag value of text region segment is not permitted");
    }

    if (!useRefinement) {
      if (sbHuffRSize != 0) {
        log.info("sbHuffRSize should be 0");
        sbHuffRSize = 0;
      }
      if (sbHuffRDY != 0) {
        log.info("sbHuffRDY should be 0");
        sbHuffRDY = 0;
      }
      if (sbHuffRDX != 0) {
        log.info("sbHuffRDX should be 0");
        sbHuffRDX = 0;
      }
      if (sbHuffRDWidth != 0) {
        log.info("sbHuffRDWidth should be 0");
        sbHuffRDWidth = 0;
      }
      if (sbHuffRDHeight != 0) {
        log.info("sbHuffRDHeight should be 0");
        sbHuffRDHeight = 0;
      }
    }
  }

  public Bitmap getRegionBitmap() throws IOException, IntegerMaxValueException, InvalidHeaderValueException {

    if (!isHuffmanEncoded) {
      setCodingStatistics();
    }

    createRegionBitmap();
    decodeSymbolInstances();

    /* 4) */
    return regionBitmap;
  }

  private void setCodingStatistics() throws IOException {
    if (cxIADT == null)
      cxIADT = new CX(512, 1);

    if (cxIAFS == null)
      cxIAFS = new CX(512, 1);

    if (cxIADS == null)
      cxIADS = new CX(512, 1);

    if (cxIAIT == null)
      cxIAIT = new CX(512, 1);

    if (cxIARI == null)
      cxIARI = new CX(512, 1);

    if (cxIARDW == null)
      cxIARDW = new CX(512, 1);

    if (cxIARDH == null)
      cxIARDH = new CX(512, 1);

    if (cxIAID == null)
      cxIAID = new CX(1 << symbolCodeLength, 1);

    if (cxIARDX == null)
      cxIARDX = new CX(512, 1);

    if (cxIARDY == null)
      cxIARDY = new CX(512, 1);

    if (arithmeticDecoder == null)
      arithmeticDecoder = new ArithmeticDecoder(subInputStream);

    if (integerDecoder == null)
      integerDecoder = new ArithmeticIntegerDecoder(arithmeticDecoder);
  }

  private void createRegionBitmap() {

    /* 6.4.5 */
    final int width = regionInfo.getBitmapWidth();
    final int height = regionInfo.getBitmapHeight();
    regionBitmap = new Bitmap(width, height);

    /* 1) */
    if (defaultPixel != 0) {
      Arrays.fill(regionBitmap.getByteArray(), (byte) 0xff);
    }
  }

  private final long decodeStripT() throws IOException, InvalidHeaderValueException {

    long stripT = 0;
    /* 2) */
    if (isHuffmanEncoded) {
      /* 6.4.6 */
      if (sbHuffDT == 3) {
        // TODO test user-specified table
        if (table == null) {
          int dtNr = 0;

          if (sbHuffFS == 3) {
            dtNr++;
          }

          if (sbHuffDS == 3) {
            dtNr++;
          }

          table = getUserTable(dtNr);
        }
        stripT = table.decode(subInputStream);
      } else {
        stripT = StandardTables.getTable(11 + sbHuffDT).decode(subInputStream);
      }
    } else {
      stripT = integerDecoder.decode(cxIADT);
    }

    return stripT * -(sbStrips);
  }

  private void decodeSymbolInstances() throws IOException, InvalidHeaderValueException, IntegerMaxValueException {

    long stripT = decodeStripT();

    /* Last two sentences of 6.4.5 2) */
    long firstS = 0;
    int instanceCounter = 0;

    /* 6.4.5 3 a) */
    while (instanceCounter < amountOfSymbolInstances) {

      final long dT = decodeDT();
      stripT += dT;
      long dfS = 0;

      /* 3 c) symbol instances in the strip */
      boolean first = true;
      currentS = 0;

      // do until OOB
      for (;;) {
        /* 3 c) i) - first symbol instance in the strip */
        if (first) {
          /* 6.4.7 */
          dfS = decodeDfS();
          firstS += dfS;
          currentS = firstS;
          first = false;
          /* 3 c) ii) - the remaining symbol instances in the strip */
        } else {
          /* 6.4.8 */
          final long idS = decodeIdS();
          /*
           * If result is OOB, then all the symbol instances in this strip have been decoded;
           * proceed to step 3 d) respectively 3 b)
           */
          if (idS == Long.MAX_VALUE)
            break;

          currentS += (idS + sbdsOffset);
        }

        /* 3 c) iii) */
        final long currentT = decodeCurrentT();
        final long t = stripT + currentT;

        /* 3 c) iv) */
        final long id = decodeID();

        /* 3 c) v) */
        final long r = decodeRI();
        /* 6.4.11 */
        final Bitmap ib = decodeIb(r, id);

        /* vi) */
        blit(ib, t);

        instanceCounter++;
      }
    }
  }

  private final long decodeDT() throws IOException {
    /* 3) b) */
    /* 6.4.6 */
    long dT;
    if (isHuffmanEncoded) {
      if (sbHuffDT == 3) {
        dT = table.decode(subInputStream);
      } else {
        dT = StandardTables.getTable(11 + sbHuffDT).decode(subInputStream);
      }
    } else {
      dT = integerDecoder.decode(cxIADT);
    }

    return (dT * sbStrips);
  }

  private final long decodeDfS() throws IOException, InvalidHeaderValueException {
    if (isHuffmanEncoded) {
      if (sbHuffFS == 3) {
        if (fsTable == null) {
          fsTable = getUserTable(0);
        }
        return fsTable.decode(subInputStream);
      } else {
        return StandardTables.getTable(6 + sbHuffFS).decode(subInputStream);
      }
    } else {
      return integerDecoder.decode(cxIAFS);
    }
  }

  private final long decodeIdS() throws IOException, InvalidHeaderValueException {
    if (isHuffmanEncoded) {
      if (sbHuffDS == 3) {
        // TODO test user-specified table
        if (dsTable == null) {
          int dsNr = 0;
          if (sbHuffFS == 3) {
            dsNr++;
          }

          dsTable = getUserTable(dsNr);
        }
        return dsTable.decode(subInputStream);

      } else {
        return StandardTables.getTable(8 + sbHuffDS).decode(subInputStream);
      }
    } else {
      return integerDecoder.decode(cxIADS);
    }
  }

  private final long decodeCurrentT() throws IOException {
    if (sbStrips != 1) {
      if (isHuffmanEncoded) {
        return subInputStream.readBits(logSBStrips);
      } else {
        return integerDecoder.decode(cxIAIT);
      }
    }

    return 0;
  }

  private final long decodeID() throws IOException {
    if (isHuffmanEncoded) {
      if (symbolCodeTable == null) {
        return subInputStream.readBits(symbolCodeLength);
      }

      return symbolCodeTable.decode(subInputStream);
    } else {
      return integerDecoder.decodeIAID(cxIAID, symbolCodeLength);
    }
  }

  private final long decodeRI() throws IOException {
    if (useRefinement) {
      if (isHuffmanEncoded) {
        return subInputStream.readBit();
      } else {
        return integerDecoder.decode(cxIARI);
      }
    }
    return 0;
  }

  private final Bitmap decodeIb(long r, long id) throws IOException, InvalidHeaderValueException,
      IntegerMaxValueException {
    Bitmap ib;

    if (r == 0) {
      ib = symbols.get((int) id);
    } else {
      /* 1) - 4) */
      final long rdw = decodeRdw();
      final long rdh = decodeRdh();
      final long rdx = decodeRdx();
      final long rdy = decodeRdy();

      /* 5) */
      /* long symInRefSize = 0; */
      if (isHuffmanEncoded) {
        /* symInRefSize = */decodeSymInRefSize();
        subInputStream.skipBits();
      }

      /* 6) */
      final Bitmap ibo = symbols.get((int) id);
      final int wo = ibo.getWidth();
      final int ho = ibo.getHeight();

      final int genericRegionReferenceDX = (int) ((rdw >> 1) + rdx);
      final int genericRegionReferenceDY = (int) ((rdh >> 1) + rdy);

      if (genericRefinementRegion == null) {
        genericRefinementRegion = new GenericRefinementRegion(subInputStream);
      }

      genericRefinementRegion.setParameters(cx, arithmeticDecoder, sbrTemplate, (int) (wo + rdw), (int) (ho + rdh),
          ibo, genericRegionReferenceDX, genericRegionReferenceDY, false, sbrATX, sbrATY);

      ib = genericRefinementRegion.getRegionBitmap();

      /* 7 */
      if (isHuffmanEncoded) {
        subInputStream.skipBits();
      }
    }
    return ib;
  }

  private final long decodeRdw() throws IOException, InvalidHeaderValueException {
    if (isHuffmanEncoded) {
      if (sbHuffRDWidth == 3) {
        // TODO test user-specified table
        if (rdwTable == null) {
          int rdwNr = 0;
          if (sbHuffFS == 3) {
            rdwNr++;
          }

          if (sbHuffDS == 3) {
            rdwNr++;
          }

          if (sbHuffDT == 3) {
            rdwNr++;
          }

          rdwTable = getUserTable(rdwNr);
        }
        return rdwTable.decode(subInputStream);

      } else {
        return StandardTables.getTable(14 + sbHuffRDWidth).decode(subInputStream);
      }
    } else {
      return integerDecoder.decode(cxIARDW);
    }
  }

  private final long decodeRdh() throws IOException, InvalidHeaderValueException {
    if (isHuffmanEncoded) {
      if (sbHuffRDHeight == 3) {
        if (rdhTable == null) {
          int rdhNr = 0;

          if (sbHuffFS == 3) {
            rdhNr++;
          }

          if (sbHuffDS == 3) {
            rdhNr++;
          }

          if (sbHuffDT == 3) {
            rdhNr++;
          }

          if (sbHuffRDWidth == 3) {
            rdhNr++;
          }

          rdhTable = getUserTable(rdhNr);
        }
        return rdhTable.decode(subInputStream);
      } else {
        return StandardTables.getTable(14 + sbHuffRDHeight).decode(subInputStream);
      }
    } else {
      return integerDecoder.decode(cxIARDH);
    }
  }

  private final long decodeRdx() throws IOException, InvalidHeaderValueException {
    if (isHuffmanEncoded) {
      if (sbHuffRDX == 3) {
        if (rdxTable == null) {
          int rdxNr = 0;
          if (sbHuffFS == 3) {
            rdxNr++;
          }

          if (sbHuffDS == 3) {
            rdxNr++;
          }

          if (sbHuffDT == 3) {
            rdxNr++;
          }

          if (sbHuffRDWidth == 3) {
            rdxNr++;
          }

          if (sbHuffRDHeight == 3) {
            rdxNr++;
          }

          rdxTable = getUserTable(rdxNr);
        }
        return rdxTable.decode(subInputStream);
      } else {
        return StandardTables.getTable(14 + sbHuffRDX).decode(subInputStream);
      }
    } else {
      return integerDecoder.decode(cxIARDX);
    }
  }

  private final long decodeRdy() throws IOException, InvalidHeaderValueException {
    if (isHuffmanEncoded) {
      if (sbHuffRDY == 3) {
        if (rdyTable == null) {
          int rdyNr = 0;
          if (sbHuffFS == 3) {
            rdyNr++;
          }

          if (sbHuffDS == 3) {
            rdyNr++;
          }

          if (sbHuffDT == 3) {
            rdyNr++;
          }

          if (sbHuffRDWidth == 3) {
            rdyNr++;
          }

          if (sbHuffRDHeight == 3) {
            rdyNr++;
          }

          if (sbHuffRDX == 3) {
            rdyNr++;
          }

          rdyTable = getUserTable(rdyNr);
        }
        return rdyTable.decode(subInputStream);
      } else {
        return StandardTables.getTable(14 + sbHuffRDY).decode(subInputStream);
      }
    } else {
      return integerDecoder.decode(cxIARDY);
    }
  }

  private final long decodeSymInRefSize() throws IOException, InvalidHeaderValueException {
    if (sbHuffRSize == 0) {
      return StandardTables.getTable(1).decode(subInputStream);
    } else {
      if (rSizeTable == null) {
        int rSizeNr = 0;

        if (sbHuffFS == 3) {
          rSizeNr++;
        }

        if (sbHuffDS == 3) {
          rSizeNr++;
        }

        if (sbHuffDT == 3) {
          rSizeNr++;
        }

        if (sbHuffRDWidth == 3) {
          rSizeNr++;
        }

        if (sbHuffRDHeight == 3) {
          rSizeNr++;
        }

        if (sbHuffRDX == 3) {
          rSizeNr++;
        }

        if (sbHuffRDY == 3) {
          rSizeNr++;
        }

        rSizeTable = getUserTable(rSizeNr);
      }
      return rSizeTable.decode(subInputStream);
    }

  }

  private final void blit(Bitmap ib, long t) {
    if (isTransposed == 0 && (referenceCorner == 2 || referenceCorner == 3)) {
      currentS += ib.getWidth() - 1;
    } else if (isTransposed == 1 && (referenceCorner == 0 || referenceCorner == 2)) {
      currentS += ib.getHeight() - 1;
    }

    /* vii) */
    long s = currentS;

    /* viii) */
    if (isTransposed == 1) {
      final long swap = t;
      t = s;
      s = swap;
    }

    if (referenceCorner != 1) {
      if (referenceCorner == 0) {
        // BL
        t -= ib.getHeight() - 1;
      } else if (referenceCorner == 2) {
        // BR
        t -= ib.getHeight() - 1;
        s -= ib.getWidth() - 1;
      } else if (referenceCorner == 3) {
        // TR
        s -= ib.getWidth() - 1;
      }
    }

    Bitmaps.blit(ib, regionBitmap, (int) s, (int) t, combinationOperator);

    /* x) */
    if (isTransposed == 0 && (referenceCorner == 0 || referenceCorner == 1)) {
      currentS += ib.getWidth() - 1;
    }

    if (isTransposed == 1 && (referenceCorner == 1 || referenceCorner == 3)) {
      currentS += ib.getHeight() - 1;
    }

  }

  private void initSymbols() throws IOException, IntegerMaxValueException, InvalidHeaderValueException {
    for (final SegmentHeader segment : segmentHeader.getRtSegments()) {
      if (segment.getSegmentType() == 0) {
        final SymbolDictionary sd = (SymbolDictionary) segment.getSegmentData();

        sd.cxIAID = cxIAID;
        symbols.addAll(sd.getDictionary());
      }
    }
    amountOfSymbols = symbols.size();
  }

  private HuffmanTable getUserTable(int referToTable) throws InvalidHeaderValueException, IOException {
    final SegmentHeader s = segmentHeader.getRtSegments()[referToTable];
    final Table t = (Table) s.getSegmentData();
    return new EncodedTable(t);
  }

  private void symbolIDCodeLengths() throws IOException {
    /* 1) - 2) */
    final List<Code> runCodeTable = new ArrayList<Code>();

    for (int i = 0; i < 35; i++) {
      final int prefLen = (int) (subInputStream.readBits(4) & 0xf);
      if (prefLen > 0) {
        runCodeTable.add(new Code(prefLen, 0, i, false));
      }
    }

    if (JBIG2ImageReader.DEBUG)
      log.debug(HuffmanTable.codeTableToString(runCodeTable));

    HuffmanTable ht = new FixedSizeTable(runCodeTable);

    /* 3) - 5) */
    long previousCodeLength = 0;

    int counter = 0;
    final List<Code> sbSymCodes = new ArrayList<Code>();
    while (counter < amountOfSymbols) {
      final long code = ht.decode(subInputStream);
      if (code < 32) {
        if (code > 0) {
          sbSymCodes.add(new Code((int) code, 0, counter, false));
        }

        previousCodeLength = code;
        counter++;
      } else {

        long runLength = 0;
        if (code == 32) {
          runLength = 3 + subInputStream.readBits(2);
        } else if (code == 33) {
          runLength = 3 + subInputStream.readBits(3);
        } else if (code == 34) {
          runLength = 11 + subInputStream.readBits(7);
        }

        long currCodeLength = 0;
        if (counter > 0) {
          currCodeLength = previousCodeLength;
        }

        for (int j = 0; j < runLength; j++) {
          if (currCodeLength > 0) {
            sbSymCodes.add(new Code((int) currCodeLength, 0, counter, false));
          }
          counter++;
        }
      }
    }

    /* 6) - Skip over remaining bits in the last Byte read */
    subInputStream.skipBits();

    /* 7) */
    symbolCodeTable = new FixedSizeTable(sbSymCodes);

  }

  public void init(SegmentHeader header, SubInputStream sis) throws InvalidHeaderValueException,
      IntegerMaxValueException, IOException {
    this.segmentHeader = header;
    this.subInputStream = sis;
    this.regionInfo = new RegionSegmentInformation(subInputStream);
    parseHeader();
  }

  protected void setContexts(CX cx, CX cxIADT, CX cxIAFS, CX cxIADS, CX cxIAIT, CX cxIAID, CX cxIARDW, CX cxIARDH,
      CX cxIARDX, CX cxIARDY) {
    this.cx = cx;

    this.cxIADT = cxIADT;
    this.cxIAFS = cxIAFS;
    this.cxIADS = cxIADS;
    this.cxIAIT = cxIAIT;

    this.cxIAID = cxIAID;

    this.cxIARDW = cxIARDW;
    this.cxIARDH = cxIARDH;
    this.cxIARDX = cxIARDX;
    this.cxIARDY = cxIARDY;
  }

  protected void setParameters(ArithmeticDecoder arithmeticDecoder, ArithmeticIntegerDecoder iDecoder,
      boolean isHuffmanEncoded, boolean sbRefine, int sbw, int sbh, long sbNumInstances, int sbStrips, int sbNumSyms,
      short sbDefaultPixel, short sbCombinationOperator, short transposed, short refCorner, short sbdsOffset,
      short sbHuffFS, short sbHuffDS, short sbHuffDT, short sbHuffRDWidth, short sbHuffRDHeight, short sbHuffRDX,
      short sbHuffRDY, short sbHuffRSize, short sbrTemplate, short sbrATX[], short sbrATY[], ArrayList<Bitmap> sbSyms,
      int sbSymCodeLen) {

    this.arithmeticDecoder = arithmeticDecoder;

    this.integerDecoder = iDecoder;

    this.isHuffmanEncoded = isHuffmanEncoded;
    this.useRefinement = sbRefine;

    this.regionInfo.setBitmapWidth(sbw);
    this.regionInfo.setBitmapHeight(sbh);

    this.amountOfSymbolInstances = sbNumInstances;
    this.sbStrips = sbStrips;
    this.amountOfSymbols = sbNumSyms;
    this.defaultPixel = sbDefaultPixel;
    this.combinationOperator = CombinationOperator.translateOperatorCodeToEnum(sbCombinationOperator);
    this.isTransposed = transposed;
    this.referenceCorner = refCorner;
    this.sbdsOffset = sbdsOffset;

    this.sbHuffFS = sbHuffFS;
    this.sbHuffDS = sbHuffDS;
    this.sbHuffDT = sbHuffDT;
    this.sbHuffRDWidth = sbHuffRDWidth;
    this.sbHuffRDHeight = sbHuffRDHeight;
    this.sbHuffRDX = sbHuffRDX;
    this.sbHuffRDY = sbHuffRDY;
    this.sbHuffRSize = sbHuffRSize;

    this.sbrTemplate = sbrTemplate;
    this.sbrATX = sbrATX;
    this.sbrATY = sbrATY;

    this.symbols = sbSyms;
    this.symbolCodeLength = sbSymCodeLen;
  }

  public RegionSegmentInformation getRegionInfo() {
    return regionInfo;
  }
}
