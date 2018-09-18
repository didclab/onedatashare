package org.onedatashare.server.model.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;

public class Slice {
  private final long offset;
  private final ByteBuf buffer;

  /**
   * An empty slice with no specified offset.
   */
//  public static final Slice EMPTY = new Slice(Unpooled.EMPTY_BUFFER, -1);

  /**
   * Wrap a {@code byte[]} in a {@code Slice} with an unspecified offset.
   *
   * @param array a {@code byte[]} to wrap in a {@code Slice}.
   */
  public Slice(byte[] array) {
    this(array, -1);
  }

  /**
   * Wrap a {@code byte[]} in a {@code Slice} with the given offset.
   *
   * @param array  a {@code byte[]} to wrap in a {@code Slice}.
   * @param offset the offset of the {@code Slice}.
   */
  public Slice(byte[] array, long offset) {
    this(Unpooled.wrappedBuffer(array), offset);
  }

  /**
   * Wrap a NIO {@code ByteBuffer} in a {@code Slice} with an unspecified
   * offset.
   *
   * @param buffer a {@code ByteBuffer} to wrap in a {@code Slice}.
   */
  public Slice(ByteBuffer buffer) {
    this(buffer, -1);
  }

  /**
   * Wrap a NIO {@code ByteBuffer} in a {@code Slice} with the given offset.
   *
   * @param buffer a {@code ByteBuffer} to wrap in a {@code Slice}.
   * @param offset the offset of the {@code Slice}.
   */
  public Slice(ByteBuffer buffer, long offset) {
    this(Unpooled.wrappedBuffer(buffer), offset);
  }

  /**
   * Wrap a Netty {@code ByteBuf} in a {@code Slice} with an unspecified
   * offset.
   *
   * @param buffer a {@code ByteBuf} to wrap in a {@code Slice}.
   */
  public Slice(ByteBuf buffer) {
    this(buffer, -1);
  }

  /**
   * Wrap a Netty {@code ByteBuf} in a {@code Slice} with the given offset.
   *
   * @param buffer a {@code ByteBuf} to wrap in a {@code Slice}.
   * @param offset the offset of the {@code Slice}.
   */
  public Slice(ByteBuf buffer, long offset) {
    this.buffer = buffer;
    this.offset = offset;
  }

  /**
   * Create a {@code Slice} that is identical to {@code slice}.
   *
   * @param slice the {@code Slice} to base this {@code Slice} on.
   */
  public Slice(Slice slice) {
    this.buffer = slice.buffer;
    this.offset = slice.offset;
  }

  /**
   * Return the length of the payload in bytes, or -1 if unknown. Specifically,
   * this is the length of the plaintext bytes represented by this slice, which
   * may be different than the raw size.
   *
   * @return The plaintext length in bytes.
   */
  public int length() {
    return buffer.readableBytes();
  }

  /**
   * Return the byte offset of the plaintext payload relative to the 0th byte
   * of the resource, or {@code -1} if unspecified.
   *
   * @return The offset of the {@code Slice}.
   */
  public long offset() {
    return offset;
  }

  /**
   * Return a {@code Slice} that encapsulates the same data but with its offset
   * at {@code offset}.
   *
   * @param offset the offset to set.
   * @return A {@code Slice} based on this one positioned at {@code offset}.
   */
  public Slice offset(long offset) {
    return (offset == this.offset) ? this : new Slice(buffer, offset);
  }

  /**
   * Return the byte payload encapsulated by this slice.
   *
   * @return The data as a Netty {@link ByteBuf}.
   */
  public final ByteBuf asByteBuf() {
    return buffer;
  }

  /**
   * Return the byte payload encapsulated by this slice as a {@code
   * ByteBuffer}.
   *
   * @return The data as a NIO {@link ByteBuffer}.
   */
  public final ByteBuffer asByteBuffer() {
    return buffer.nioBuffer();
  }

  /**
   * Return the data wrapped by the slice as a byte array. This buffer may be
   * the backing array of the {@code Slice}, and therefore should not be
   * altered by the caller.
   *
   * @return The data as a byte array.
   */
  public final byte[] asBytes() {
    return buffer.hasArray() ? buffer.array() : asBytes(new byte[length()]);
  }

  /**
   * Copy the data wrapped by this {@code Slice} into a byte array. If {@code
   * array} is too small, the output will be truncated. If {@code array} is too
   * big, the excess portion will be left as-is.
   *
   * @param array the {@code byte[]} to copy data into.
   * @return The {@code byte[]} passed in as {@code array}.
   */
  public final byte[] asBytes(byte[] array) {
    buffer.getBytes(0, array);
    return array;
  }

  /**
   * Check if a {@code Slice} is empty.
   *
   * @return {@code true} if this slice is empty; {@code false} otherwise.
   */
  public boolean isEmpty() {
    return length() == 0;
  }

  /**
   * Get the {@link Resource} this slice came from. Can be {@code null} if the slice is
   * anonymous.
   *
   * @return The resource this slice came from, or {@code null}.
   * @see Resource
   */
  public Resource resource() {
    return null;
  }

  /**
   * Return a string representation of the slice that can be useful for
   * debugging.
   *
   * @return A string representation describing this slice.
   */
  public String toString() {
    int w = 16;
    StringBuilder main = new StringBuilder();
    StringBuilder line = new StringBuilder();
    StringBuilder ascii = new StringBuilder();
    long off = (offset < 0) ? 0 : offset;
    int count = 0;
    for (byte b : asBytes()) {
      char c = (char) (b & 0xFF);
      line.append(String.format("%02x", (int) c));
      ascii.append(
              (Character.isISOControl(c)) ? '.' :
                      (Character.isSpaceChar(c)) ? ' ' : (char) (b & 0xFF)
      );
      if (count == 0) {
        main.append(String.format("%07x: ", off));
      }
      if (count++ % 2 == 1) {
        line.append(" ");
      }
      if (count == w) {
        off += count;
        count = 0;
        main.append(line.toString()).append(" ");
        main.append(ascii.toString()).append("\n");
        line.setLength(0);
        ascii.setLength(0);
      }
    }

    // Fill the last line with padding.
    if (count > 0) {
      while (count < w)
        line.append((count++ % 2 == 0) ? "  " : "   ");
      main.append(line.toString()).append(" ").append(ascii.toString());
    }

    return main.toString();
  }
}
