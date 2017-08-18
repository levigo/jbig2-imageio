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

package com.levigo.jbig2.util.log;

import java.util.logging.Level;

public class JDKLogger implements Logger {
  final java.util.logging.Logger wrappedLogger;

  public JDKLogger(java.util.logging.Logger logger) {
    wrappedLogger = logger;
  }

  public void debug(String msg) {
    wrappedLogger.log(Level.FINE, msg);
  }

  public void debug(String msg, Throwable t) {
    wrappedLogger.log(Level.FINE, msg, t);
  }

  public void info(String msg) {
    wrappedLogger.log(Level.INFO, msg);
  }

  public void info(String msg, Throwable t) {
    wrappedLogger.log(Level.INFO, msg, t);
  }

  public void warn(String msg) {
    wrappedLogger.log(Level.WARNING, msg);
  }

  public void warn(String msg, Throwable t) {
    wrappedLogger.log(Level.WARNING, msg, t);
  }

  public void fatal(String msg) {
    wrappedLogger.log(Level.SEVERE, msg);
  }

  public void fatal(String msg, Throwable t) {
    wrappedLogger.log(Level.SEVERE, msg, t);
  }

  public void error(String msg) {
    wrappedLogger.log(Level.SEVERE, msg);
  }

  public void error(String msg, Throwable t) {
    wrappedLogger.log(Level.SEVERE, msg, t);
  }

  public boolean isDebugEnabled() {
    return wrappedLogger.isLoggable(Level.FINE);
  }

  public boolean isInfoEnabled() {
    return wrappedLogger.isLoggable(Level.INFO);
  }

  public boolean isWarnEnabled() {
    return wrappedLogger.isLoggable(Level.WARNING);
  }

  public boolean isFatalEnabled() {
    return wrappedLogger.isLoggable(Level.SEVERE);
  }

  public boolean isErrorEnabled() {
    return wrappedLogger.isLoggable(Level.SEVERE);
  }
}
