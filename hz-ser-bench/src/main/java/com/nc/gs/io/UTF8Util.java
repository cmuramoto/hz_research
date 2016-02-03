package com.nc.gs.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class UTF8Util {

	/**
	 * Unpacks bytes from a byte buffer to a char array, using SSE instructions
	 * to bulk unpack 128-bits (16 ASCII chars) at a time. <br/>
	 * <br/>
	 * This method incurs JNI overhead, however it's worth it if we have more
	 * than {@link Bits#JNI_UTF_VECTOR_THRESHOLD} characters.
	 *
	 * @param src
	 *            - a byte array with characters encoded with utf8
	 * @param off
	 *            - the start position on src array
	 * @param dst
	 *            - the target array
	 * 
	 * @return the amount of bytes read from src.
	 */
	public static native int utf8BytesToArray(byte[] src, char[] dst, int off);

	static {
		loadLibrary("native/libgs-native-SSE3.so");
	}

	public static void loadLibrary(String resource) {
		URL url = Objects.requireNonNull(UTF8Util.class.getClassLoader().getResource(resource));

		String file = url.getFile();

		if (Files.exists(Paths.get(file))) {
			System.load(file);
		} else {
			int ix = file.lastIndexOf('/');
			file = ix > 0 ? file.substring(ix + 1, file.length()) : file;
			try (InputStream is = url.openStream()) {
				Path p = Paths.get(System.getProperty("user.home"), file);
				Files.deleteIfExists(p);
				Files.copy(is, p);
				System.load(p.toAbsolutePath().toString());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

	}

}