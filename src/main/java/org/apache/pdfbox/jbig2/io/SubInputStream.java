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

package org.apache.pdfbox.jbig2.io;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

/**
 * A wrapper for an {@link ImageInputStream} which is able to provide a view of a specific part of
 * the wrapped stream. Read accesses to the wrapped stream are synchronized, so that users of this
 * stream need to deal with synchronization against other users of the same instance, but not
 * against other users of the wrapped stream.
 */
public class SubInputStream extends ImageInputStreamImpl {

  protected final ImageInputStream wrappedStream;

  /**
   * The position in the wrapped stream at which the window starts. Offset is an absolut value.
   */
  protected final long offset;

  /**
   * The length of the window. Length is an relative value.
   */
  protected final long length;

  /**
   * A buffer which is used to improve read performance.
   */
  private final byte buffer[] = new byte[4096];

  /**
   * Location of the first byte in the buffer with respect to the start of the stream.
   */
  long bufferBase;

  /**
   * Location of the last byte in the buffer with respect to the start of the stream.
   */
  long bufferTop;

  /**
   * Construct a new SubInputStream which provides a view of the wrapped stream.
   * 
   * @param iis - The stream to be wrapped.
   * @param offset - The absolute position in the wrapped stream at which the sub-stream starts.
   * @param length - The length of the sub-stream.
   */
  public SubInputStream(ImageInputStream iis, long offset, long length) {
    assert null != iis;
    assert length >= 0;
    assert offset >= 0;

    this.wrappedStream = iis;
    this.offset = offset;
    this.length = length;
  }

  @Override
  public int read() throws IOException {
    if (streamPos >= length) {
      return -1;
    }

    if (streamPos >= bufferTop || streamPos < bufferBase) {
      if (!fillBuffer()) {
        return -1;
      }
    }

    int read = 0xff & buffer[(int) (streamPos - bufferBase)];

    streamPos++;

    return read;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (streamPos >= length) {
      return -1;
    }

    synchronized (wrappedStream) {
      if (wrappedStream.getStreamPosition() != streamPos + offset) {
        wrappedStream.seek(streamPos + offset);
      }

      int toRead = (int) Math.min(len, length - streamPos);
      int read = wrappedStream.read(b, off, toRead);
      streamPos += read;

      return read;
    }
  }

  /**
   * Fill the buffer at the current stream position.
   * 
   * @throws IOException
   * @return Boolean flag. {@code true} if successful, {@code false} if not.
   */
  private boolean fillBuffer() throws IOException {
    synchronized (wrappedStream) {
      if (wrappedStream.getStreamPosition() != streamPos + offset) {
        wrappedStream.seek(streamPos + offset);
      }

      bufferBase = streamPos;
      int toRead = (int) Math.min(buffer.length, length - streamPos);
      int read = wrappedStream.read(buffer, 0, toRead);
      bufferTop = bufferBase + read;

      return read > 0;
    }
  }

  @Override
  public long length() {
    return length;
  }

  /**
   * Skips remaining bits in the current byte.
   */
  public void skipBits() {
    if (bitOffset != 0) {
      bitOffset = 0;
      streamPos++;
    }
  }
}
