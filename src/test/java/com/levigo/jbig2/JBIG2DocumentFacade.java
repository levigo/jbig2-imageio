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

package com.levigo.jbig2;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

import com.levigo.jbig2.err.JBIG2Exception;

public class JBIG2DocumentFacade extends JBIG2Document {
  
  public static JBIG2Document doc(ImageInputStream doc, ImageInputStream globals) throws IOException {
    final JBIG2Document globalsDoc = new JBIG2Document(globals);
    return new JBIG2Document(doc, globalsDoc.getGlobalSegments());
  }

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
