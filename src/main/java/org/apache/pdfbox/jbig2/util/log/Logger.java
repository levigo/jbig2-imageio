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

package org.apache.pdfbox.jbig2.util.log;

public interface Logger {


  /**
   * Log a message at the DEBUG level.
   * 
   * @param msg the message string to be logged
   */
  public void debug(String msg);

  /**
   * Log an exception ({@link Throwable}) at the DEBUG level with an accompanying message.
   * 
   * @param msg the message accompanying the exception.
   * @param t the exception ({@link Throwable}) to log.
   */
  public void debug(String msg, Throwable t);

  /**
   * Log a message at the INFO level.
   * 
   * @param msg the message string to be logged
   */
  public void info(String msg);

  /**
   * Log an exception ({@link Throwable}) at the INFO level with an accompanying message.
   * 
   * @param msg the message accompanying the exception
   * @param t the exception ({@link Throwable}) to log
   */
  public void info(String msg, Throwable t);

  /**
   * Log a message at the WARN level.
   * 
   * @param msg the message string to be logged
   */
  public void warn(String msg);

  /**
   * Log an exception ({@link Throwable}) at the WARN level with an accompanying message.
   * 
   * @param msg the message accompanying the exception
   * @param t the exception ({@link Throwable}) to log
   */
  public void warn(String msg, Throwable t);

  /**
   * Log a message at the WARN level.
   * 
   * @param msg the message string to be logged
   */
  public void fatal(String msg);

  /**
   * Log an exception ({@link Throwable}) at the WARN level with an accompanying message.
   * 
   * @param msg the message accompanying the exception
   * @param t the exception ({@link Throwable}) to log
   */
  public void fatal(String msg, Throwable t);

  /**
   * Log a message at the ERROR level.
   * 
   * @param msg the message string to be logged
   */
  public void error(String msg);

  /**
   * Log an exception ({@link Throwable}) at the ERROR level with an accompanying message.
   * 
   * @param msg the message accompanying the exception
   * @param t the exception ({@link Throwable}) to log
   */
  public void error(String msg, Throwable t);

  /**
   * Is the logger instance enabled for the DEBUG level?
   * 
   * @return True if this Logger is enabled for the DEBUG level, false otherwise.
   * 
   */
  public boolean isDebugEnabled();

  /**
   * Is the logger instance enabled for the INFO level?
   * 
   * @return True if this Logger is enabled for the INFO level, false otherwise.
   */
  public boolean isInfoEnabled();

  /**
   * Is the logger instance enabled for the WARN level?
   * 
   * @return True if this Logger is enabled for the WARN level, false otherwise.
   */
  public boolean isWarnEnabled();

  /**
   * Is the logger instance enabled for the FATAL level?
   * 
   * @return True if this Logger is enabled for the FATAL level, false otherwise.
   */
  public boolean isFatalEnabled();

  /**
   * Is the logger instance enabled for the ERROR level?
   * 
   * @return True if this Logger is enabled for the ERROR level, false otherwise.
   */
  public boolean isErrorEnabled();


}
