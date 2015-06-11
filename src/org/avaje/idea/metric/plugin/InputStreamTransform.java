package org.avaje.idea.metric.plugin;

import org.avaje.metric.agent.Transformer;

import java.io.*;
import java.lang.instrument.IllegalClassFormatException;


/**
 * Utility object that handles input streams for reading and writing.
 */
public class InputStreamTransform {

  private final Transformer transformer;

  private final ClassLoader classLoader;

  public InputStreamTransform(Transformer transformer, ClassLoader classLoader) {
    this.transformer = transformer;
    this.classLoader = classLoader;
  }

//  public void log(int level, String msg, String extra) {
//    transformer.log(level, msg, extra);
//  }

  /**
   * Transform a file.
   */
  public byte[] transform(String className, File file) throws IOException, IllegalClassFormatException {
    try {
      return transform(className, new FileInputStream(file));

    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Transform a input stream.
   */
  public byte[] transform(String className, InputStream is) throws IOException, IllegalClassFormatException {

    try {

      byte[] classBytes = readBytes(is);

      return transformer.transform(classLoader, className, null, null, classBytes);

    } finally {
      if (is != null) {
        is.close();
      }
    }
  }

//  /**
//   * Helper method to write bytes to a file.
//   */
//  public static void writeBytes(byte[] bytes, File file) throws IOException {
//    writeBytes(bytes, new FileOutputStream(file));
//  }

//  /**
//   * Helper method to write bytes to a OutputStream.
//   */
//  public static void writeBytes(byte[] bytes, OutputStream os) throws IOException {
//
//    BufferedOutputStream bos = new BufferedOutputStream(os);
//
//    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
//
//    byte[] buf = new byte[1028];
//
//    int len = 0;
//    while ((len = bis.read(buf, 0, buf.length)) > -1){
//      bos.write(buf, 0, len);
//    }
//
//    bos.flush();
//    bos.close();
//
//    bis.close();
//  }


  public static byte[] readBytes(InputStream is) throws IOException {

    BufferedInputStream bis = new BufferedInputStream(is);

    ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);

    byte[] buf = new byte[1028];

    int len;
    while ((len = bis.read(buf, 0, buf.length)) > -1) {
      baos.write(buf, 0, len);
    }
    baos.flush();
    baos.close();
    return baos.toByteArray();
  }
}
