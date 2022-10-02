package com.itsaky.androidide.treesitter;

import java.io.UnsupportedEncodingException;

public class TSParser implements AutoCloseable {
  private final long pointer;

  public TSParser(long pointer) {
    this.pointer = pointer;
  }

  public TSParser() {
    this(Native.newParser());
  }

  /**
   * Set the language of the given parser.
   *
   * @param language The language to set.
   * @see TSLanguage
   * @see TSLanguages
   */
  public void setLanguage(TSLanguage language) {
    Native.setLanguage(pointer, language.pointer);
  }

  /**
   * Get the language for this parser instance.
   *
   * @return The language instance.
   */
  public TSLanguage getLanguage() {
    return new TSLanguage(Native.getLanguage(this.pointer));
  }

  /**
   * Parses the given String source. Uses {@link TSInputEncoding#TSInputEncodingUTF8} as the default
   * encoding.
   *
   * @param source The source code to parse.
   * @return The parsed tree.
   * @throws UnsupportedEncodingException
   */
  public TSTree parseString(String source) throws UnsupportedEncodingException {
    return parseString(source, TSInputEncoding.TSInputEncodingUTF8);
  }

  /**
   * Parses the given String source with the given encoding.
   *
   * @param source The source code to parse.
   * @param encoding The encoding to of the source.
   * @return The parsed tree.
   * @throws UnsupportedEncodingException
   */
  public TSTree parseString(String source, TSInputEncoding encoding)
      throws UnsupportedEncodingException {
    byte[] bytes = source.getBytes(encoding.getCharset());
    return new TSTree(Native.parseBytes(pointer, bytes, bytes.length, encoding.getFlag()));
  }

  /**
   * Parses the given bytes.
   *
   * @param bytes The bytes to parse.
   * @param bytesLength The length of bytes to parse.
   * @param encodingFlag The encoding of the source.
   * @return The parsed tree.
   */
  public TSTree parseBytes(byte[] bytes, int bytesLength, int encodingFlag) {
    return new TSTree(Native.parseBytes(pointer, bytes, bytesLength, encodingFlag));
  }

  /**
   * @see #parseString(TSTree, String, TSInputEncoding)
   */
  public TSTree parseString(TSTree oldTree, String source) throws UnsupportedEncodingException {
    return parseString(oldTree, source, TSInputEncoding.TSInputEncodingUTF8);
  }

  /**
   * Parses the given string source code.
   *
   * @param oldTree If earlier version of the same document has been parsed and you intend to do an
   *     incremental parsing, then this should be the earlier parsed syntax tree. Otherwise <code>
   *     null</code>.
   * @param source The source code to parse.
   * @param encoding The encoding of the source code.
   * @return The parsed tree.
   * @throws UnsupportedEncodingException
   */
  public TSTree parseString(TSTree oldTree, String source, TSInputEncoding encoding)
      throws UnsupportedEncodingException {
    byte[] bytes = source.getBytes(encoding.getCharset());
    return new TSTree(
        Native.incrementalParseBytes(
            pointer, oldTree.getPointer(), bytes, bytes.length, encoding.getFlag()));
  }

  /**
   * Instruct the parser to start the next parse from the beginning.
   *
   * <p>If the parser previously failed because of a timeout or a cancellation, then by default, it
   * will resume where it left off on the next call to any of the parsing functions. If you don't
   * want to resume, and instead intend to use this parser to parse some other document, you must
   * call this function first.
   */
  public void reset() {
    Native.reset(this.pointer);
  }

  /**
   * Set the maximum duration in microseconds that parsing should be allowed to take before halting.
   *
   * <p>If parsing takes longer than this, it will halt early, returning <code>null</code>.
   */
  public void setTimeout(long microseconds) {
    Native.setTimeout(this.pointer, microseconds);
  }

  /**
   * Get the duration in microseconds that parsing is allowed to take.
   *
   * @return The timeout in microseconds.
   */
  public long getTimeout() {
    return Native.getTimeout(this.pointer);
  }

  /** Closes and deletes the current parser. */
  @Override
  public void close() {
    Native.delete(pointer);
  }

  private static class Native {
    public static native long newParser();

    public static native void delete(long parser);

    public static native void setLanguage(long parser, long language);

    public static native long getLanguage(long parser);

    public static native long parseBytes(long parser, byte[] source, int length, int encoding);

    public static native long incrementalParseBytes(
        long parser, long old_tree, byte[] source, int length, int encoding);

    public static native void reset(long parser);

    public static native void setTimeout(long parser, long timeout);

    public static native long getTimeout(long parser);
  }
}
