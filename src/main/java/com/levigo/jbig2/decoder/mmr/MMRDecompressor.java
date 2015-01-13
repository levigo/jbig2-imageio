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

package com.levigo.jbig2.decoder.mmr;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.stream.ImageInputStream;

import com.levigo.jbig2.Bitmap;

/**
 * A decompressor for MMR compression.
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 * @author Benjamin Zindel
 */
public class MMRDecompressor {

  private int width;
  private int height;

  /**
   * A class encapsulating the compressed raw data.
   */
  private final class RunData {
    private static final int MAX_RUN_DATA_BUFFER = 1024 << 7; // 1024 * 128
    private static final int MIN_RUN_DATA_BUFFER = 3; // min. bytes to decompress
    private static final int CODE_OFFSET = 24;

    /** Compressed data stream. */
    ImageInputStream stream;

    int offset;
    int lastOffset = 0;
    int lastCode = 0;

    byte buffer[];
    int bufferBase;
    int bufferTop;

    RunData(ImageInputStream stream) {
      this.stream = stream;
      offset = 0;
      lastOffset = 1;

      try {
        long len = stream.length();

        len = Math.min(Math.max(MIN_RUN_DATA_BUFFER, len), MAX_RUN_DATA_BUFFER);

        buffer = new byte[(int) len];
        fillBuffer(0);
      } catch (IOException e) {
        buffer = new byte[10];
        e.printStackTrace();
      }
    }

    private final Code uncompressGetCode(Code table[]) {
      return uncompressGetCodeLittleEndian(table);
    }

    private final Code uncompressGetCodeLittleEndian(Code table[]) {
      final int code = uncompressGetNextCodeLittleEndian() & 0xffffff;
      Code result = table[code >> CODE_OFFSET - FIRST_LEVEL_TABLE_SIZE];

      // perform second-level lookup
      if (null != result && null != result.subTable) {
        result = result.subTable[(code >> CODE_OFFSET - FIRST_LEVEL_TABLE_SIZE - SECOND_LEVEL_TABLE_SIZE)
            & SECOND_LEVEL_TABLE_MASK];
      }

      return result;
    }

    /**
     * Fill up the code word in little endian mode. This is a hotspot, therefore the algorithm is
     * heavily optimised. For the frequent cases (i.e. short words) we try to get away with as
     * little work as possible. <br>
     * This method returns code words of 16 bits, which are aligned to the 24th bit. The lowest 8
     * bits are used as a "queue" of bits so that an access to the actual data is only needed, when
     * this queue becomes empty.
     */
    private final int uncompressGetNextCodeLittleEndian() {
      try {

        // the number of bits to fill (offset difference)
        int bitsToFill = offset - lastOffset;

        // check whether we can refill, or need to fill in absolute mode
        if (bitsToFill < 0 || bitsToFill > 24) {
          // refill at absolute offset
          int byteOffset = (offset >> 3) - bufferBase; // offset>>3 is equivalent to offset/8

          if (byteOffset >= bufferTop) {
            byteOffset += bufferBase;
            fillBuffer(byteOffset);
            byteOffset -= bufferBase;
          }

          lastCode = (buffer[byteOffset] & 0xff) << 16 | (buffer[byteOffset + 1] & 0xff) << 8
              | (buffer[byteOffset + 2] & 0xff);

          int bitOffset = offset & 7; // equivalent to offset%8
          lastCode <<= bitOffset;
        } else {
          // the offset to the next byte boundary as seen from the last offset
          int bitOffset = lastOffset & 7;
          final int avail = 7 - bitOffset;

          // check whether there are enough bits in the "queue"
          if (bitsToFill <= avail) {
            lastCode <<= bitsToFill;
          } else {
            int byteOffset = (lastOffset >> 3) + 3 - bufferBase;

            if (byteOffset >= bufferTop) {
              byteOffset += bufferBase;
              fillBuffer(byteOffset);
              byteOffset -= bufferBase;
            }

            bitOffset = 8 - bitOffset;
            do {
              lastCode <<= bitOffset;
              lastCode |= buffer[byteOffset] & 0xff;
              bitsToFill -= bitOffset;
              byteOffset++;
              bitOffset = 8;
            } while (bitsToFill >= 8);

            lastCode <<= bitsToFill; // shift the rest
          }
        }
        lastOffset = offset;

        return lastCode;
      } catch (IOException e) {
        // will this actually happen? only with broken data, I'd say.
        throw new ArrayIndexOutOfBoundsException("Corrupted RLE data caused by an IOException while reading raw data: "
            + e.toString());
      }
    }

    private void fillBuffer(int byteOffset) throws IOException {
      bufferBase = byteOffset;
      synchronized (stream) {
        try {
          stream.seek(byteOffset);
          bufferTop = stream.read(buffer);
        } catch (EOFException e) {
          // you never know which kind of EOF will kick in
          bufferTop = -1;
        }
        // check filling degree
        if (bufferTop > -1 && bufferTop < 3) {
          // CK: if filling degree is too small,
          // smoothly fill up to the next three bytes or substitute with with
          // empty bytes
          int read = 0;
          while (bufferTop < 3) {
            try {
              read = stream.read();
            } catch (EOFException e) {
              read = -1;
            }
            buffer[bufferTop++] = read == -1 ? 0 : (byte) (read & 0xff);
          }
        }
      }
      // leave some room, in order to save a few tests in the calling code
      bufferTop -= 3;

      if (bufferTop < 0) {
        // if we're at EOF, just supply zero-bytes
        Arrays.fill(buffer, (byte) 0);
        bufferTop = buffer.length - 3;
      }
    }

    /**
     * Skip to next byte
     */
    private void align() {
      offset = ((offset + 7) >> 3) << 3;
    }
  }

  private static final class Code {
    Code subTable[] = null;

    final int bitLength, codeWord, runLength;

    Code(int codeData[]) {
      bitLength = codeData[0];
      codeWord = codeData[1];
      runLength = codeData[2];
    }

    public String toString() {
      return bitLength + "/" + codeWord + "/" + runLength;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    public boolean equals(Object obj) {
      return (obj instanceof Code) && //
          ((Code) obj).bitLength == bitLength && //
          ((Code) obj).codeWord == codeWord && //
          ((Code) obj).runLength == runLength;
    }
  }

  private static final int FIRST_LEVEL_TABLE_SIZE = 8;
  private static final int FIRST_LEVEL_TABLE_MASK = (1 << FIRST_LEVEL_TABLE_SIZE) - 1;
  private static final int SECOND_LEVEL_TABLE_SIZE = 5;
  private static final int SECOND_LEVEL_TABLE_MASK = (1 << SECOND_LEVEL_TABLE_SIZE) - 1;

  private static Code whiteTable[] = null;
  private static Code blackTable[] = null;
  private static Code modeTable[] = null;

  private RunData data;

  private synchronized final static void initTables() {
    if (null == whiteTable) {
      whiteTable = createLittleEndianTable(MMRConstants.WhiteCodes);
      blackTable = createLittleEndianTable(MMRConstants.BlackCodes);
      modeTable = createLittleEndianTable(MMRConstants.ModeCodes);
    }
  }

  private final int uncompress2D(RunData runData, int[] referenceOffsets, int refRunLength, int[] runOffsets, int width) {

    int referenceBufferOffset = 0;
    int currentBufferOffset = 0;
    int currentLineBitPosition = 0;

    boolean whiteRun = true; // Always start with a white run
    Code code = null; // Storage var for current code being processed

    referenceOffsets[refRunLength] = referenceOffsets[refRunLength + 1] = width;
    referenceOffsets[refRunLength + 2] = referenceOffsets[refRunLength + 3] = width + 1;

    try {
      decodeLoop : while (currentLineBitPosition < width) {

        // Get the mode code
        code = runData.uncompressGetCode(modeTable);

        if (code == null) {
          runData.offset++;
          break decodeLoop;
        }

        // Add the code length to the bit offset
        runData.offset += code.bitLength;

        switch (code.runLength){
          case MMRConstants.CODE_V0 :
            currentLineBitPosition = referenceOffsets[referenceBufferOffset];
            break;

          case MMRConstants.CODE_VR1 :
            currentLineBitPosition = referenceOffsets[referenceBufferOffset] + 1;
            break;

          case MMRConstants.CODE_VL1 :
            currentLineBitPosition = referenceOffsets[referenceBufferOffset] - 1;
            break;

          case MMRConstants.CODE_H :
            for (int ever = 1; ever > 0;) {

              code = runData.uncompressGetCode(whiteRun == true ? whiteTable : blackTable);

              if (code == null)
                break decodeLoop;

              runData.offset += code.bitLength;
              if (code.runLength < 64) {
                if (code.runLength < 0) {
                  runOffsets[currentBufferOffset++] = currentLineBitPosition;
                  code = null;
                  break decodeLoop;
                }
                currentLineBitPosition += code.runLength;
                runOffsets[currentBufferOffset++] = currentLineBitPosition;
                break;
              }
              currentLineBitPosition += code.runLength;
            }

            final int firstHalfBitPos = currentLineBitPosition;
            for (int ever1 = 1; ever1 > 0;) {
              code = runData.uncompressGetCode(whiteRun != true ? whiteTable : blackTable);
              if (code == null)
                break decodeLoop;

              runData.offset += code.bitLength;
              if (code.runLength < 64) {
                if (code.runLength < 0) {
                  runOffsets[currentBufferOffset++] = currentLineBitPosition;
                  break decodeLoop;
                }
                currentLineBitPosition += code.runLength;
                // don't generate 0-length run at EOL for cases where the line ends in an H-run.
                if (currentLineBitPosition < width || currentLineBitPosition != firstHalfBitPos)
                  runOffsets[currentBufferOffset++] = currentLineBitPosition;
                break;
              }
              currentLineBitPosition += code.runLength;
            }

            while (currentLineBitPosition < width && referenceOffsets[referenceBufferOffset] <= currentLineBitPosition) {
              referenceBufferOffset += 2;
            }
            continue decodeLoop;

          case MMRConstants.CODE_P :
            referenceBufferOffset++;
            currentLineBitPosition = referenceOffsets[referenceBufferOffset++];
            continue decodeLoop;

          case MMRConstants.CODE_VR2 :
            currentLineBitPosition = referenceOffsets[referenceBufferOffset] + 2;
            break;

          case MMRConstants.CODE_VL2 :
            currentLineBitPosition = referenceOffsets[referenceBufferOffset] - 2;
            break;

          case MMRConstants.CODE_VR3 :
            currentLineBitPosition = referenceOffsets[referenceBufferOffset] + 3;
            break;

          case MMRConstants.CODE_VL3 :
            currentLineBitPosition = referenceOffsets[referenceBufferOffset] - 3;
            break;

          case MMRConstants.EOL :
          default :
            System.err.println("Should not happen!");
            // Possibly MMR Decoded
            if (runData.offset == 12 && code.runLength == MMRConstants.EOL) {
              runData.offset = 0;
              uncompress1D(runData, referenceOffsets, width);
              runData.offset++;
              uncompress1D(runData, runOffsets, width);
              int retCode = uncompress1D(runData, referenceOffsets, width);
              runData.offset++;
              return retCode;
            }
            currentLineBitPosition = width;
            continue decodeLoop;
        }

        // Only vertical modes get this far
        if (currentLineBitPosition <= width) {
          whiteRun = !whiteRun;

          runOffsets[currentBufferOffset++] = currentLineBitPosition;

          if (referenceBufferOffset > 0) {
            referenceBufferOffset--;
          } else {
            referenceBufferOffset++;
          }

          while (currentLineBitPosition < width && referenceOffsets[referenceBufferOffset] <= currentLineBitPosition) {
            referenceBufferOffset += 2;
          }
        }
      }
    } catch (Throwable t) {
      StringBuffer strBuf = new StringBuffer();
      strBuf.append("whiteRun           = ");
      strBuf.append(whiteRun);
      strBuf.append("\n");
      strBuf.append("code               = ");
      strBuf.append(code);
      strBuf.append("\n");
      strBuf.append("refOffset          = ");
      strBuf.append(referenceBufferOffset);
      strBuf.append("\n");
      strBuf.append("curOffset          = ");
      strBuf.append(currentBufferOffset);
      strBuf.append("\n");
      strBuf.append("bitPos             = ");
      strBuf.append(currentLineBitPosition);
      strBuf.append("\n");
      strBuf.append("runData.offset = ");
      strBuf.append(runData.offset);
      strBuf.append(" ( byte:");
      strBuf.append(runData.offset / 8);
      strBuf.append(", bit:");
      strBuf.append(runData.offset & 0x07);
      strBuf.append(" )");

      System.out.println(strBuf.toString());

      return MMRConstants.EOF;
    }

    if (runOffsets[currentBufferOffset] != width) {
      runOffsets[currentBufferOffset] = width;
    }

    if (code == null) {
      return MMRConstants.EOL;
    }
    return currentBufferOffset;
  }

  public MMRDecompressor(int width, int height, ImageInputStream stream) {
    this.width = width;
    this.height = height;

    data = new RunData(stream);

    initTables();
  }

  public Bitmap uncompress() {
    final Bitmap result = new Bitmap(width, height);

    int[] currentOffsets = new int[width + 5];
    int[] referenceOffsets = new int[width + 5];
    referenceOffsets[0] = width;
    int refRunLength = 1;

    int count = 0;

    for (int line = 0; line < height; line++) {
      count = uncompress2D(data, referenceOffsets, refRunLength, currentOffsets, width);

      if (count == MMRConstants.EOF) {
        break;
      }

      if (count > 0) {
        fillBitmap(result, line, currentOffsets, count);
      }

      // Swap lines
      int tempOffsets[] = referenceOffsets;
      referenceOffsets = currentOffsets;
      currentOffsets = tempOffsets;
      refRunLength = count;
    }

    detectAndSkipEOL();

    data.align();

    return result;
  }

  private void detectAndSkipEOL() {
    while (true) {
      Code code = data.uncompressGetCode(modeTable);
      if (null != code && code.runLength == MMRConstants.EOL) {
        data.offset += code.bitLength;
      } else
        break;
    }
  }

  private void fillBitmap(Bitmap result, int line, int[] currentOffsets, int count) {

    int x = 0;
    int targetByte = result.getByteIndex(0, line);
    byte targetByteValue = 0;
    for (int index = 0; index < count; index++) {

      final int offset = currentOffsets[index];
      byte value;

      if ((index & 1) == 0) {
        value = 0;
      } else {
        value = 1;
      }

      while (x < offset) {
        targetByteValue = (byte) ((targetByteValue << 1) | value);
        x++;

        if ((x & 7) == 0) {
          result.setByte(targetByte++, targetByteValue);
          targetByteValue = 0;
        }
      }
    }

    if ((x & 7) != 0) {
      targetByteValue <<= 8 - (x & 7);
      result.setByte(targetByte, targetByteValue);
    }
  }

  private final int uncompress1D(RunData runData, int[] runOffsets, int width) {

    boolean whiteRun = true;
    int iBitPos = 0;
    Code code = null;
    int refOffset = 0;

    loop : while (iBitPos < width) {
      while (true) {
        if (whiteRun) {
          code = runData.uncompressGetCode(whiteTable);
        } else {
          code = runData.uncompressGetCode(blackTable);
        }

        runData.offset += code.bitLength;

        if (code.runLength < 0) {
          break loop;
        }

        iBitPos += code.runLength;

        if (code.runLength < 64) {
          whiteRun = !whiteRun;
          runOffsets[refOffset++] = iBitPos;
          break;
        }
      }
    }

    if (runOffsets[refOffset] != width) {
      runOffsets[refOffset] = width;
    }

    return code != null && code.runLength != MMRConstants.EOL ? refOffset : MMRConstants.EOL;
  }

  /**
   * For little endian, the tables are structured like this:
   * 
   * <pre>
   *  v--------v length = FIRST_LEVEL_TABLE_LENGTH
   *                v-----v length = SECOND_LEVEL_TABLE_LENGTH
   * 
   *  A code word which fits into the first level table (length=3)
   *  [Cccvvvvv]
   * 
   *  A code word which needs the second level table also (length=10)
   *  [Cccccccc] -&gt; [ccvvv]
   * 
   *  &quot;C&quot; denotes the first code word bit
   *  &quot;c&quot; denotes a code word bit
   *  &quot;v&quot; denotes a variant bit
   * </pre>
   * 
   */
  private static Code[] createLittleEndianTable(int codes[][]) {
    final Code firstLevelTable[] = new Code[FIRST_LEVEL_TABLE_MASK + 1];
    for (int i = 0; i < codes.length; i++) {
      final Code code = new Code(codes[i]);

      if (code.bitLength <= FIRST_LEVEL_TABLE_SIZE) {
        final int variantLength = FIRST_LEVEL_TABLE_SIZE - code.bitLength;
        final int baseWord = code.codeWord << variantLength;

        for (int variant = (1 << variantLength) - 1; variant >= 0; variant--) {
          final int index = baseWord | variant;
          firstLevelTable[index] = code;
        }
      } else {
        // init second level table
        final int firstLevelIndex = code.codeWord >>> code.bitLength - FIRST_LEVEL_TABLE_SIZE;

        if (firstLevelTable[firstLevelIndex] == null) {
          final Code firstLevelCode = new Code(new int[3]);
          firstLevelCode.subTable = new Code[SECOND_LEVEL_TABLE_MASK + 1];
          firstLevelTable[firstLevelIndex] = firstLevelCode;
        }

        // fill second level table
        if (code.bitLength <= FIRST_LEVEL_TABLE_SIZE + SECOND_LEVEL_TABLE_SIZE) {
          final Code secondLevelTable[] = firstLevelTable[firstLevelIndex].subTable;
          final int variantLength = FIRST_LEVEL_TABLE_SIZE + SECOND_LEVEL_TABLE_SIZE - code.bitLength;
          final int baseWord = (code.codeWord << variantLength) & SECOND_LEVEL_TABLE_MASK;

          for (int variant = (1 << variantLength) - 1; variant >= 0; variant--) {
            secondLevelTable[baseWord | variant] = code;
          }
        } else
          throw new IllegalArgumentException("Code table overflow in MMRDecompressor");
      }
    }
    return firstLevelTable;
  }
}