package com.nc.io;

import static com.hazelcast.nio.Bits.CHAR_SIZE_IN_BYTES;
import static com.hazelcast.nio.Bits.INT_SIZE_IN_BYTES;
import static com.hazelcast.nio.Bits.LONG_SIZE_IN_BYTES;
import static com.hazelcast.nio.Bits.NULL_ARRAY_LENGTH;
import static com.hazelcast.nio.Bits.SHORT_SIZE_IN_BYTES;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import com.hazelcast.internal.serialization.SerializationService;
import com.hazelcast.internal.serialization.impl.HeapData;
import com.hazelcast.nio.Bits;
import com.hazelcast.nio.BufferObjectDataInput;
import com.hazelcast.nio.serialization.Data;
import com.nc.gs.io.UTF8Util;

public class ByteArrayObjectDataInput extends InputStream implements BufferObjectDataInput {

	byte[] data;

	int size;

	int pos;

	int mark;

	final SerializationService service;

	char[] charBuffer;

	private final boolean bigEndian;

	ByteArrayObjectDataInput(byte[] data, int offset, SerializationService service, ByteOrder byteOrder) {
		this.data = data;
		this.size = data != null ? data.length : 0;
		this.service = service;
		this.pos = offset;
		this.bigEndian = byteOrder == ByteOrder.BIG_ENDIAN;
	}

	public ByteArrayObjectDataInput(byte[] data, SerializationService service, ByteOrder byteOrder) {
		this(data, 0, service, byteOrder);
	}

	@Override
	public final int available() {
		return size - pos;
	}

	final void checkAvailable(int pos, int k) throws IOException {
		if (pos < 0) {
			throw new IllegalArgumentException("Negative pos! -> " + pos);
		}
		if ((size - pos) < k) {
			throw new EOFException("Cannot read " + k + " bytes!");
		}
	}

	@Override
	public void clear() {
		this.data = null;
		this.pos = 0;
		this.size = 0;
		this.mark = 0;
		if (charBuffer != null && charBuffer.length > UTF_BUFFER_SIZE * 8) {
			this.charBuffer = new char[UTF_BUFFER_SIZE * 8];
		}
	}

	@Override
	public final void close() {
		data = null;
		charBuffer = null;
	}

	@Override
	public ByteOrder getByteOrder() {
		return bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
	}

	@Override
	public final ClassLoader getClassLoader() {
		return service.getClassLoader();
	}

	public final void inflateUTF(char[] dst) throws IOException {
		int charCount = readInt();
		if (charCount == NULL_ARRAY_LENGTH) {
			return;
		}
		if (dst.length < charCount) {
			throw new IllegalArgumentException();
		}

		byte b;
		for (int i = 0; i < charCount; i++) {
			b = readByte();
			if (b < 0) {
				dst[i] = Bits.readUtf8Char(this, b);
			} else {
				dst[i] = (char) b;
			}
		}
	}

	public final void inflateUTFJNI(char[] dst) throws IOException {
		int charCount = readInt();
		if (charCount == NULL_ARRAY_LENGTH) {
			return;
		}
		if (dst.length < charCount) {
			throw new IllegalArgumentException();
		}

		int read = UTF8Util.utf8BytesToArray(data, charBuffer, pos);

		pos += read;
	}

	@Override
	public void init(byte[] data, int offset) {
		this.data = data;
		this.size = data != null ? data.length : 0;
		this.pos = offset;
	}

	@Override
	public final void mark(int readlimit) {
		mark = pos;
	}

	@Override
	public final boolean markSupported() {
		return true;
	}

	/**
	 * Returns this buffer's position.
	 */
	@Override
	public final int position() {
		return pos;
	}

	@Override
	public final void position(int newPos) {
		if ((newPos > size) || (newPos < 0)) {
			throw new IllegalArgumentException();
		}
		pos = newPos;
		if (mark > pos) {
			mark = -1;
		}
	}

	@Override
	public int read() throws IOException {
		return (pos < size) ? (data[pos++] & 0xff) : -1;
	}

	@Override
	public final int read(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}
		if (pos >= size) {
			return -1;
		}
		if (pos + len > size) {
			len = size - pos;
		}
		System.arraycopy(data, pos, b, off, len);
		pos += len;
		return len;
	}

	@Override
	public int read(int position) throws IOException {
		return (position < size) ? (data[position] & 0xff) : -1;
	}

	@Override
	public final boolean readBoolean() throws IOException {
		final int ch = read();
		if (ch < 0) {
			throw new EOFException();
		}
		return (ch != 0);
	}

	@Override
	public final boolean readBoolean(int position) throws IOException {
		final int ch = read(position);
		if (ch < 0) {
			throw new EOFException();
		}
		return (ch != 0);
	}

	@Override
	public boolean[] readBooleanArray() throws IOException {
		int len = readInt();
		if (len == NULL_ARRAY_LENGTH) {
			return null;
		}
		if (len > 0) {
			boolean[] values = new boolean[len];
			for (int i = 0; i < len; i++) {
				values[i] = readBoolean();
			}
			return values;
		}
		return new boolean[0];
	}

	/**
	 * See the general contract of the <code>readByte</code> method of <code>DataInput</code>.
	 * <p/>
	 * Bytes for this operation are read from the contained input stream.
	 *
	 * @return the next byte of this input stream as a signed 8-bit <code>byte</code>.
	 * @throws java.io.EOFException
	 *             if this input stream has reached the end.
	 * @throws java.io.IOException
	 *             if an I/O error occurs.
	 * @see java.io.FilterInputStream#in
	 */
	@Override
	public final byte readByte() throws IOException {
		final int ch = read();
		if (ch < 0) {
			throw new EOFException();
		}
		return (byte) (ch);
	}

	@Override
	public final byte readByte(int position) throws IOException {
		final int ch = read(position);
		if (ch < 0) {
			throw new EOFException();
		}
		return (byte) (ch);
	}

	@Override
	public byte[] readByteArray() throws IOException {
		int len = readInt();
		if (len == NULL_ARRAY_LENGTH) {
			return null;
		}
		if (len > 0) {
			byte[] b = new byte[len];
			readFully(b);
			return b;
		}
		return new byte[0];
	}

	/**
	 * See the general contract of the <code>readChar</code> method of <code>DataInput</code>.
	 * <p/>
	 * Bytes for this operation are read from the contained input stream.
	 *
	 * @return the next two bytes of this input stream as a Unicode character.
	 * @throws java.io.EOFException
	 *             if this input stream reaches the end before reading two bytes.
	 * @throws java.io.IOException
	 *             if an I/O error occurs.
	 * @see java.io.FilterInputStream#in
	 */
	@Override
	public final char readChar() throws IOException {
		final char c = readChar(pos);
		pos += CHAR_SIZE_IN_BYTES;
		return c;
	}

	@Override
	public char readChar(int position) throws IOException {
		checkAvailable(position, CHAR_SIZE_IN_BYTES);
		return Bits.readChar(data, position, bigEndian);
	}

	@Override
	public char[] readCharArray() throws IOException {
		int len = readInt();
		if (len == NULL_ARRAY_LENGTH) {
			return null;
		}
		if (len > 0) {
			char[] values = new char[len];
			for (int i = 0; i < len; i++) {
				values[i] = readChar();
			}
			return values;
		}
		return new char[0];
	}

	@Override
	public final Data readData() throws IOException {
		byte[] bytes = readByteArray();
		Data data = bytes == null ? null : new HeapData(bytes);
		return data;
	}

	/**
	 * See the general contract of the <code>readDouble</code> method of <code>DataInput</code>.
	 * <p/>
	 * Bytes for this operation are read from the contained input stream.
	 *
	 * @return the next eight bytes of this input stream, interpreted as a <code>double</code>.
	 * @throws java.io.EOFException
	 *             if this input stream reaches the end before reading eight bytes.
	 * @throws java.io.IOException
	 *             if an I/O error occurs.
	 * @see java.io.DataInputStream#readLong()
	 * @see Double#longBitsToDouble(long)
	 */
	@Override
	public double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	@Override
	public double readDouble(ByteOrder byteOrder) throws IOException {
		return Double.longBitsToDouble(readLong(byteOrder));
	}

	@Override
	public double readDouble(int position) throws IOException {
		return Double.longBitsToDouble(readLong(position));
	}

	@Override
	public double readDouble(int position, ByteOrder byteOrder) throws IOException {
		return Double.longBitsToDouble(readLong(position, byteOrder));
	}

	@Override
	public double[] readDoubleArray() throws IOException {
		int len = readInt();
		if (len == NULL_ARRAY_LENGTH) {
			return null;
		}
		if (len > 0) {
			double[] values = new double[len];
			for (int i = 0; i < len; i++) {
				values[i] = readDouble();
			}
			return values;
		}
		return new double[0];
	}

	/**
	 * See the general contract of the <code>readFloat</code> method of <code>DataInput</code>.
	 * <p/>
	 * Bytes for this operation are read from the contained input stream.
	 *
	 * @return the next four bytes of this input stream, interpreted as a <code>float</code>.
	 * @throws java.io.EOFException
	 *             if this input stream reaches the end before reading four bytes.
	 * @throws java.io.IOException
	 *             if an I/O error occurs.
	 * @see java.io.DataInputStream#readInt()
	 * @see Float#intBitsToFloat(int)
	 */
	@Override
	public float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	@Override
	public float readFloat(ByteOrder byteOrder) throws IOException {
		return Float.intBitsToFloat(readInt(byteOrder));
	}

	@Override
	public float readFloat(int position) throws IOException {
		return Float.intBitsToFloat(readInt(position));
	}

	@Override
	public float readFloat(int position, ByteOrder byteOrder) throws IOException {
		return Float.intBitsToFloat(readInt(position, byteOrder));
	}

	@Override
	public float[] readFloatArray() throws IOException {
		int len = readInt();
		if (len == NULL_ARRAY_LENGTH) {
			return null;
		}
		if (len > 0) {
			float[] values = new float[len];
			for (int i = 0; i < len; i++) {
				values[i] = readFloat();
			}
			return values;
		}
		return new float[0];
	}

	@Override
	public void readFully(final byte[] b) throws IOException {
		if (read(b) == -1) {
			throw new EOFException("End of stream reached");
		}
	}

	@Override
	public void readFully(final byte[] b, final int off, final int len) throws IOException {
		if (read(b, off, len) == -1) {
			throw new EOFException("End of stream reached");
		}
	}

	/**
	 * See the general contract of the <code>readInt</code> method of <code>DataInput</code>.
	 * <p/>
	 * Bytes for this operation are read from the contained input stream.
	 *
	 * @return the next four bytes of this input stream, interpreted as an <code>int</code>.
	 * @throws java.io.EOFException
	 *             if this input stream reaches the end before reading four bytes.
	 * @throws java.io.IOException
	 *             if an I/O error occurs.
	 * @see java.io.FilterInputStream#in
	 */
	@Override
	public final int readInt() throws IOException {
		final int i = readInt(pos);
		pos += INT_SIZE_IN_BYTES;
		return i;
	}

	@Override
	public final int readInt(ByteOrder byteOrder) throws IOException {
		final int i = readInt(pos, byteOrder);
		pos += INT_SIZE_IN_BYTES;
		return i;
	}

	@Override
	public int readInt(int position) throws IOException {
		checkAvailable(position, INT_SIZE_IN_BYTES);
		return Bits.readInt(data, position, bigEndian);
	}

	@Override
	public int readInt(int position, ByteOrder byteOrder) throws IOException {
		checkAvailable(position, INT_SIZE_IN_BYTES);
		return Bits.readInt(data, position, byteOrder == ByteOrder.BIG_ENDIAN);
	}

	@Override
	public int[] readIntArray() throws IOException {
		int len = readInt();
		if (len == NULL_ARRAY_LENGTH) {
			return null;
		}
		if (len > 0) {
			int[] values = new int[len];
			for (int i = 0; i < len; i++) {
				values[i] = readInt();
			}
			return values;
		}
		return new int[0];
	}

	@Override
	@Deprecated
	public final String readLine() throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * See the general contract of the <code>readLong</code> method of <code>DataInput</code>.
	 * <p/>
	 * Bytes for this operation are read from the contained input stream.
	 *
	 * @return the next eight bytes of this input stream, interpreted as a <code>long</code>.
	 * @throws java.io.EOFException
	 *             if this input stream reaches the end before reading eight bytes.
	 * @throws java.io.IOException
	 *             if an I/O error occurs.
	 * @see java.io.FilterInputStream#in
	 */
	@Override
	public final long readLong() throws IOException {
		final long l = readLong(pos);
		pos += LONG_SIZE_IN_BYTES;
		return l;
	}

	@Override
	public final long readLong(ByteOrder byteOrder) throws IOException {
		final long l = readLong(pos, byteOrder);
		pos += LONG_SIZE_IN_BYTES;
		return l;
	}

	@Override
	public long readLong(int position) throws IOException {
		checkAvailable(position, LONG_SIZE_IN_BYTES);
		return Bits.readLong(data, position, bigEndian);
	}

	@Override
	public long readLong(int position, ByteOrder byteOrder) throws IOException {
		checkAvailable(position, LONG_SIZE_IN_BYTES);
		return Bits.readLong(data, position, byteOrder == ByteOrder.BIG_ENDIAN);
	}

	@Override
	public long[] readLongArray() throws IOException {
		int len = readInt();
		if (len == NULL_ARRAY_LENGTH) {
			return null;
		}
		if (len > 0) {
			long[] values = new long[len];
			for (int i = 0; i < len; i++) {
				values[i] = readLong();
			}
			return values;
		}
		return new long[0];
	}

	@SuppressWarnings("unchecked")
	@Override
	public final Object readObject() throws IOException {
		return service.readObject(this);
	}

	/**
	 * See the general contract of the <code>readShort</code> method of <code>DataInput</code>.
	 * <p/>
	 * Bytes for this operation are read from the contained input stream.
	 *
	 * @return the next two bytes of this input stream, interpreted as a signed 16-bit number.
	 * @throws java.io.EOFException
	 *             if this input stream reaches the end before reading two bytes.
	 * @throws java.io.IOException
	 *             if an I/O error occurs.
	 * @see java.io.FilterInputStream#in
	 */
	@Override
	public final short readShort() throws IOException {
		short s = readShort(pos);
		pos += SHORT_SIZE_IN_BYTES;
		return s;
	}

	@Override
	public final short readShort(ByteOrder byteOrder) throws IOException {
		short s = readShort(pos, byteOrder);
		pos += SHORT_SIZE_IN_BYTES;
		return s;
	}

	@Override
	public short readShort(int position) throws IOException {
		checkAvailable(position, SHORT_SIZE_IN_BYTES);
		return Bits.readShort(data, position, bigEndian);
	}

	@Override
	public short readShort(int position, ByteOrder byteOrder) throws IOException {
		checkAvailable(position, SHORT_SIZE_IN_BYTES);
		return Bits.readShort(data, position, byteOrder == ByteOrder.BIG_ENDIAN);
	}

	@Override
	public short[] readShortArray() throws IOException {
		int len = readInt();
		if (len == NULL_ARRAY_LENGTH) {
			return null;
		}
		if (len > 0) {
			short[] values = new short[len];
			for (int i = 0; i < len; i++) {
				values[i] = readShort();
			}
			return values;
		}
		return new short[0];
	}

	/**
	 * See the general contract of the <code>readUnsignedByte</code> method of
	 * <code>DataInput</code>.
	 * <p/>
	 * Bytes for this operation are read from the contained input stream.
	 *
	 * @return the next byte of this input stream, interpreted as an unsigned 8-bit number.
	 * @throws java.io.EOFException
	 *             if this input stream has reached the end.
	 * @throws java.io.IOException
	 *             if an I/O error occurs.
	 * @see java.io.FilterInputStream#in
	 */
	@Override
	public int readUnsignedByte() throws IOException {
		return readByte() & 0xFF;
	}

	/**
	 * See the general contract of the <code>readUnsignedShort</code> method of
	 * <code>DataInput</code>.
	 * <p/>
	 * Bytes for this operation are read from the contained input stream.
	 *
	 * @return the next two bytes of this input stream, interpreted as an unsigned 16-bit integer.
	 * @throws java.io.EOFException
	 *             if this input stream reaches the end before reading two bytes.
	 * @throws java.io.IOException
	 *             if an I/O error occurs.
	 * @see java.io.FilterInputStream#in
	 */
	@Override
	public int readUnsignedShort() throws IOException {
		return readShort() & 0xffff;
	}

	/**
	 * See the general contract of the <code>readUTF</code> method of <code>DataInput</code>.
	 * <p/>
	 * Bytes for this operation are read from the contained input stream.
	 *
	 * @return a Unicode string.
	 * @throws java.io.EOFException
	 *             if this input stream reaches the end before reading all the bytes.
	 * @throws java.io.IOException
	 *             if an I/O error occurs.
	 * @throws java.io.UTFDataFormatException
	 *             if the bytes do not represent a valid modified UTF-8 encoding of a string.
	 * @see java.io.DataInputStream#readUTF(java.io.DataInput)
	 */
	@Override
	public final String readUTF() throws IOException {
		int charCount = readInt();
		if (charCount == NULL_ARRAY_LENGTH) {
			return null;
		}
		if (charBuffer == null || charCount > charBuffer.length) {
			charBuffer = new char[charCount];
		}
		byte b;
		for (int i = 0; i < charCount; i++) {
			b = readByte();
			if (b < 0) {
				charBuffer[i] = Bits.readUtf8Char(this, b);
			} else {
				charBuffer[i] = (char) b;
			}
		}
		return new String(charBuffer, 0, charCount);
	}

	@Override
	public String[] readUTFArray() throws IOException {
		int len = readInt();
		if (len == NULL_ARRAY_LENGTH) {
			return null;
		}
		if (len > 0) {
			String[] values = new String[len];
			for (int i = 0; i < len; i++) {
				values[i] = readUTF();
			}
			return values;
		}
		return new String[0];
	}

	public String readUTFJNI() throws IOException {
		int charCount = readInt();
		if (charCount == NULL_ARRAY_LENGTH) {
			return null;
		}
		if (charBuffer == null || charCount > charBuffer.length) {
			charBuffer = new char[charCount];
		}

		int read = UTF8Util.utf8BytesToArray(data, charBuffer, pos);

		pos += read;

		return new String(charBuffer, 0, charCount);
	}

	@Override
	public final void reset() {
		pos = mark;
	}

	@Override
	public final long skip(long n) {
		if (n <= 0 || n >= Integer.MAX_VALUE) {
			return 0L;
		}
		return skipBytes((int) n);
	}

	@Override
	public final int skipBytes(final int n) {
		if (n <= 0) {
			return 0;
		}
		int skip = n;
		final int pos = position();
		if (pos + skip > size) {
			skip = size - pos;
		}
		position(pos + skip);
		return skip;
	}

	@Override
	public String toString() {
		return "ByteArrayObjectDataInput{" + "size=" + size + ", pos=" + pos + ", mark=" + mark + '}';
	}
}
