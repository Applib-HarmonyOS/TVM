/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tvm;

import org.apache.tvm.contrib.TVMRuntimeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

class NativeLibraryLoader {
  private static final String TAG = NativeLibraryLoader.class.getName();
  private static final String LIB_PATH_IN_JAR = "/lib/native/";
  private static final String WINDOWS = "Windows";
  private static File tempDir;

  static {
    try {
      tempDir = File.createTempFile("tvm4j", "");
      Files.deleteIfExists(tempDir.toPath());
      if (!tempDir.mkdir()) {
        throw new IOException("Couldn't create directory " + tempDir.getAbsolutePath());
      }

      /*
       * Different cleanup strategies for Windows and Linux.
       * TODO: shutdown hook won't work on Windows
       */
      if (!WINDOWS.equals(getUnifiedOSName())) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
          @Override public void run() {
            for (File f : tempDir.listFiles()) {
              LogUtil.info(TAG,"Deleting " + f.getAbsolutePath());
              try {
                Files.delete(f.toPath());
              } catch (IOException e) {
                LogUtil.error(TAG,"Couldn't delete temporary file: " + e.getMessage());
              }
            }
            LogUtil.info(TAG,"Deleting " + tempDir.getAbsolutePath());
            try {
              Files.delete(tempDir.toPath());
            } catch (IOException e) {
              LogUtil.error(TAG,"Couldn't delete temporary directory: " + e.getMessage());
            }
          }
        });
      } else {
        throw new TVMRuntimeException("Windows not supported yet.");
      }
    } catch (IOException ex) {
      LogUtil.error(TAG,"Couldn't create temporary directory: " + ex.getMessage());
      throw new TVMRuntimeException(ex.getMessage());
    }
  }

  private NativeLibraryLoader() {
    /* Do nothing */
  }

  /**
   * Find the library as a resource in jar, copy it to a tempfile
   * and load it using System.load(). The name of the library has to be the
   * base name, it is mapped to the corresponding system name using
   * System.mapLibraryName(). e.g., the library "foo" is called "libfoo.so"
   * under Linux and "foo.dll" under Windows, but you just have to pass "foo" to
   * the loadLibrary().
   *
   * @param libname basename of the library
   * @throws UnsatisfiedLinkError if library not found.
   * @throws IOException if file not found.
   */
  public static void loadLibrary(String libname) throws UnsatisfiedLinkError, IOException {
    String mappedLibname = System.mapLibraryName(libname);
    String loadLibname = mappedLibname;
    if (mappedLibname.endsWith("dylib")) {
      LogUtil.info(TAG,"Replaced .dylib with .jnilib");
      loadLibname = mappedLibname.replace(".dylib", ".jnilib");
    }
    LogUtil.info(TAG,"Attempting to load " + loadLibname);
    extractResourceFileToTempDir(loadLibname, target -> {LogUtil.info(TAG,"Loading library from " + target.getPath());
    System.load(target.getPath());});
  }

  /**
   * Translate all those Windows to "Windows". ("Windows XP", "Windows Vista", "Windows 7", etc.)
   */
  private static String unifyOSName(String osname) {
    if (osname.startsWith(WINDOWS)) {
      return WINDOWS;
    }
    return osname;
  }

  private static String getUnifiedOSName() {
    return unifyOSName(System.getProperty("os.name"));
  }

  private static File createTempFile(String name) throws NullPointerException {
    return new File(tempDir + File.separator + name);
  }

  static interface Action {
    public void invoke(File file);
  }

  /**
   * Copies the resource file to a temp file and do an action.
   * @param filename source file name (in lib/native).
   * @param action callback function to deal with the copied file.
   */
  public static void extractResourceFileToTempDir(String filename, Action action)
      throws IOException {
    final String libFileInJar = LIB_PATH_IN_JAR + filename;
    InputStream is = NativeLibraryLoader.class.getResourceAsStream(libFileInJar);
    if (is == null) {
      throw new UnsatisfiedLinkError("Couldn't find the resource " + filename);
    }
    LogUtil.info(TAG, String.format("Loading %s from %s", filename, LIB_PATH_IN_JAR));

    File tempfile = null;
    try {
      tempfile = createTempFile(filename);
    } catch (NullPointerException io) {
      LogUtil.error(TAG,"[ERROR] Could not create the temp file: " + io.toString());
      throw io;
    } catch (UnsatisfiedLinkError ule) {
      LogUtil.error(TAG,"Couldn't load copied link file: " + ule.toString());
      throw ule;
    }

    FileInputStream lock = null;
    try (FileOutputStream os = new FileOutputStream(tempfile);) {
      final long savedTime = System.currentTimeMillis();
      byte[] buf = new byte[8192];
      int len = is.read(buf);
      while (len > 0) {
        os.write(buf, 0, len);
        len = is.read(buf);
      }
      os.flush();
      lock = new FileInputStream(tempfile);
      double seconds = (System.currentTimeMillis() - savedTime) / 1e3;
      LogUtil.info(TAG, String.format("Copying took %.2f seconds.", seconds));
      action.invoke(tempfile);
      lock.close();
    } catch (IOException io) {
      LogUtil.error(TAG,"[ERROR] Could not create the temp file: " + io.toString());
      throw io;
    } catch (UnsatisfiedLinkError ule) {
      LogUtil.error(TAG,"Couldn't load copied link file: " + ule.toString());
      throw ule;
    } finally {
      if (lock != null) lock.close();
    }
  }
}
