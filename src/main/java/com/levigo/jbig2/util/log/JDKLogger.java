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

package com.levigo.jbig2.util.log;

import java.util.logging.Level;

/**
 * 
 * @author <a href="mailto:m.krzikalla@levigo.de">Matthäus Krzikalla</a>
 * 
 */
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
