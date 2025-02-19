/*
 * Original Author -> Harry Yang (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2021 All Rights Reserved.
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER
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
 * along with this program.  If not, see [http://www.gnu.org/licenses/]
 */

package cn.taketoday.core.io.buffer;

import cn.taketoday.lang.Assert;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.function.IntPredicate;

/**
 * Basic abstraction over byte buffers.
 *
 * <p>{@code DataBuffer}s has a separate {@linkplain #readPosition() read} and
 * {@linkplain #writePosition() write} position, as opposed to {@code ByteBuffer}'s
 * single {@linkplain ByteBuffer#position() position}. As such, the {@code DataBuffer}
 * does not require a {@linkplain ByteBuffer#flip() flip} to read after writing. In general,
 * the following invariant holds for the read and write positions, and the capacity:
 *
 * <blockquote>
 * <tt>0</tt> <tt>&lt;=</tt>
 * <i>readPosition</i> <tt>&lt;=</tt>
 * <i>writePosition</i> <tt>&lt;=</tt>
 * <i>capacity</i>
 * </blockquote>
 *
 * <p>The {@linkplain #capacity() capacity} of a {@code DataBuffer} is expanded on demand,
 * similar to {@code StringBuilder}.
 *
 * <p>The main purpose of the {@code DataBuffer} abstraction is to provide a convenient wrapper
 * around {@link ByteBuffer} which is similar to Netty's {@link io.netty.buffer.ByteBuf} but
 * can also be used on non-Netty platforms (i.e. Servlet containers).
 *
 * @author Arjen Poutsma
 * @author Brian Clozel
 * @see DataBufferFactory
 * @since 4.0
 */
public interface DataBuffer {

  /**
   * Return the {@link DataBufferFactory} that created this buffer.
   *
   * @return the creating buffer factory
   */
  DataBufferFactory factory();

  /**
   * Return the index of the first byte in this buffer that matches
   * the given predicate.
   *
   * @param predicate the predicate to match
   * @param fromIndex the index to start the search from
   * @return the index of the first byte that matches {@code predicate};
   * or {@code -1} if none match
   */
  int indexOf(IntPredicate predicate, int fromIndex);

  /**
   * Return the index of the last byte in this buffer that matches
   * the given predicate.
   *
   * @param predicate the predicate to match
   * @param fromIndex the index to start the search from
   * @return the index of the last byte that matches {@code predicate};
   * or {@code -1} if none match
   */
  int lastIndexOf(IntPredicate predicate, int fromIndex);

  /**
   * Return the number of bytes that can be read from this data buffer.
   *
   * @return the readable byte count
   */
  int readableByteCount();

  /**
   * Return the number of bytes that can be written to this data buffer.
   *
   * @return the writable byte count
   */
  int writableByteCount();

  /**
   * Return the number of bytes that this buffer can contain.
   *
   * @return the capacity
   */
  int capacity();

  /**
   * Set the number of bytes that this buffer can contain.
   * <p>If the new capacity is lower than the current capacity, the contents
   * of this buffer will be truncated. If the new capacity is higher than
   * the current capacity, it will be expanded.
   *
   * @param capacity the new capacity
   * @return this buffer
   */
  DataBuffer capacity(int capacity);

  /**
   * Ensure that the current buffer has enough {@link #writableByteCount()}
   * to write the amount of data given as an argument. If not, the missing
   * capacity will be added to the buffer.
   *
   * @param capacity the writable capacity to check for
   * @return this buffer
   */
  default DataBuffer ensureCapacity(int capacity) {
    return this;
  }

  /**
   * Return the position from which this buffer will read.
   *
   * @return the read position
   */
  int readPosition();

  /**
   * Set the position from which this buffer will read.
   *
   * @param readPosition the new read position
   * @return this buffer
   * @throws IndexOutOfBoundsException if {@code readPosition} is smaller than 0
   * or greater than {@link #writePosition()}
   */
  DataBuffer readPosition(int readPosition);

  /**
   * Return the position to which this buffer will write.
   *
   * @return the write position
   */
  int writePosition();

  /**
   * Set the position to which this buffer will write.
   *
   * @param writePosition the new write position
   * @return this buffer
   * @throws IndexOutOfBoundsException if {@code writePosition} is smaller than
   * {@link #readPosition()} or greater than {@link #capacity()}
   */
  DataBuffer writePosition(int writePosition);

  /**
   * Read a single byte at the given index from this data buffer.
   *
   * @param index the index at which the byte will be read
   * @return the byte at the given index
   * @throws IndexOutOfBoundsException when {@code index} is out of bounds
   */
  byte getByte(int index);

  /**
   * Read a single byte from the current reading position from this data buffer.
   *
   * @return the byte at this buffer's current reading position
   */
  byte read();

  /**
   * Read this buffer's data into the specified destination, starting at the current
   * reading position of this buffer.
   *
   * @param destination the array into which the bytes are to be written
   * @return this buffer
   */
  DataBuffer read(byte[] destination);

  /**
   * Read at most {@code length} bytes of this buffer into the specified destination,
   * starting at the current reading position of this buffer.
   *
   * @param destination the array into which the bytes are to be written
   * @param offset the index within {@code destination} of the first byte to be written
   * @param length the maximum number of bytes to be written in {@code destination}
   * @return this buffer
   */
  DataBuffer read(byte[] destination, int offset, int length);

  /**
   * Write a single byte into this buffer at the current writing position.
   *
   * @param b the byte to be written
   * @return this buffer
   */
  DataBuffer write(byte b);

  /**
   * Write the given source into this buffer, starting at the current writing position
   * of this buffer.
   *
   * @param source the bytes to be written into this buffer
   * @return this buffer
   */
  DataBuffer write(byte[] source);

  /**
   * Write at most {@code length} bytes of the given source into this buffer, starting
   * at the current writing position of this buffer.
   *
   * @param source the bytes to be written into this buffer
   * @param offset the index within {@code source} to start writing from
   * @param length the maximum number of bytes to be written from {@code source}
   * @return this buffer
   */
  DataBuffer write(byte[] source, int offset, int length);

  /**
   * Write one or more {@code DataBuffer}s to this buffer, starting at the current
   * writing position. It is the responsibility of the caller to
   * {@linkplain DataBufferUtils#release(DataBuffer) release} the given data buffers.
   *
   * @param buffers the byte buffers to write into this buffer
   * @return this buffer
   */
  DataBuffer write(DataBuffer... buffers);

  /**
   * Write one or more {@link ByteBuffer} to this buffer, starting at the current
   * writing position.
   *
   * @param buffers the byte buffers to write into this buffer
   * @return this buffer
   */
  DataBuffer write(ByteBuffer... buffers);

  /**
   * Write the given {@code CharSequence} using the given {@code Charset},
   * starting at the current writing position.
   *
   * @param charSequence the char sequence to write into this buffer
   * @param charset the charset to encode the char sequence with
   * @return this buffer
   */
  default DataBuffer write(CharSequence charSequence, Charset charset) {
    Assert.notNull(charSequence, "CharSequence must not be null");
    Assert.notNull(charset, "Charset must not be null");
    if (charSequence.length() != 0) {
      CharsetEncoder charsetEncoder = charset.newEncoder()
              .onMalformedInput(CodingErrorAction.REPLACE)
              .onUnmappableCharacter(CodingErrorAction.REPLACE);
      CharBuffer inBuffer = CharBuffer.wrap(charSequence);
      int estimatedSize = (int) (inBuffer.remaining() * charsetEncoder.averageBytesPerChar());
      ByteBuffer outBuffer = ensureCapacity(estimatedSize)
              .asByteBuffer(writePosition(), writableByteCount());
      while (true) {
        CoderResult cr = inBuffer.hasRemaining()
                ? charsetEncoder.encode(inBuffer, outBuffer, true)
                : CoderResult.UNDERFLOW;
        if (cr.isUnderflow()) {
          cr = charsetEncoder.flush(outBuffer);
        }
        if (cr.isUnderflow()) {
          break;
        }
        if (cr.isOverflow()) {
          writePosition(writePosition() + outBuffer.position());
          int maximumSize = (int) (inBuffer.remaining() * charsetEncoder.maxBytesPerChar());
          ensureCapacity(maximumSize);
          outBuffer = asByteBuffer(writePosition(), writableByteCount());
        }
      }
      writePosition(writePosition() + outBuffer.position());
    }
    return this;
  }

  /**
   * Create a new {@code DataBuffer} whose contents is a shared subsequence of this
   * data buffer's content.  Data between this data buffer and the returned buffer is
   * shared; though changes in the returned buffer's position will not be reflected
   * in the reading nor writing position of this data buffer.
   * <p><strong>Note</strong> that this method will <strong>not</strong> call
   * {@link DataBufferUtils#retain(DataBuffer)} on the resulting slice: the reference
   * count will not be increased.
   *
   * @param index the index at which to start the slice
   * @param length the length of the slice
   * @return the specified slice of this data buffer
   */
  DataBuffer slice(int index, int length);

  /**
   * Create a new {@code DataBuffer} whose contents is a shared, retained subsequence of this
   * data buffer's content.  Data between this data buffer and the returned buffer is
   * shared; though changes in the returned buffer's position will not be reflected
   * in the reading nor writing position of this data buffer.
   * <p><strong>Note</strong> that unlike {@link #slice(int, int)}, this method
   * <strong>will</strong> call {@link DataBufferUtils#retain(DataBuffer)} (or equivalent) on the
   * resulting slice.
   *
   * @param index the index at which to start the slice
   * @param length the length of the slice
   * @return the specified, retained slice of this data buffer
   */
  default DataBuffer retainedSlice(int index, int length) {
    return DataBufferUtils.retain(slice(index, length));
  }

  /**
   * Expose this buffer's bytes as a {@link ByteBuffer}. Data between this
   * {@code DataBuffer} and the returned {@code ByteBuffer} is shared; though
   * changes in the returned buffer's {@linkplain ByteBuffer#position() position}
   * will not be reflected in the reading nor writing position of this data buffer.
   *
   * @return this data buffer as a byte buffer
   */
  ByteBuffer asByteBuffer();

  /**
   * Expose a subsequence of this buffer's bytes as a {@link ByteBuffer}. Data between
   * this {@code DataBuffer} and the returned {@code ByteBuffer} is shared; though
   * changes in the returned buffer's {@linkplain ByteBuffer#position() position}
   * will not be reflected in the reading nor writing position of this data buffer.
   *
   * @param index the index at which to start the byte buffer
   * @param length the length of the returned byte buffer
   * @return this data buffer as a byte buffer
   */
  ByteBuffer asByteBuffer(int index, int length);

  /**
   * Expose this buffer's data as an {@link InputStream}. Both data and read position are
   * shared between the returned stream and this data buffer. The underlying buffer will
   * <strong>not</strong> be {@linkplain DataBufferUtils#release(DataBuffer) released}
   * when the input stream is {@linkplain InputStream#close() closed}.
   *
   * @return this data buffer as an input stream
   * @see #asInputStream(boolean)
   */
  InputStream asInputStream();

  /**
   * Expose this buffer's data as an {@link InputStream}. Both data and read position are
   * shared between the returned stream and this data buffer.
   *
   * @param releaseOnClose whether the underlying buffer will be
   * {@linkplain DataBufferUtils#release(DataBuffer) released} when the input stream is
   * {@linkplain InputStream#close() closed}.
   * @return this data buffer as an input stream
   */
  InputStream asInputStream(boolean releaseOnClose);

  /**
   * Expose this buffer's data as an {@link OutputStream}. Both data and write position are
   * shared between the returned stream and this data buffer.
   *
   * @return this data buffer as an output stream
   */
  OutputStream asOutputStream();

  /**
   * Return this buffer's data a String using the specified charset. Default implementation
   * delegates to {@code toString(readPosition(), readableByteCount(), charset)}.
   *
   * @param charset the character set to use
   * @return a string representation of all this buffers data
   */
  default String toString(Charset charset) {
    Assert.notNull(charset, "Charset must not be null");
    return toString(readPosition(), readableByteCount(), charset);
  }

  /**
   * Return a part of this buffer's data as a String using the specified charset.
   *
   * @param index the index at which to start the string
   * @param length the number of bytes to use for the string
   * @param charset the charset to use
   * @return a string representation of a part of this buffers data
   */
  String toString(int index, int length, Charset charset);

}
