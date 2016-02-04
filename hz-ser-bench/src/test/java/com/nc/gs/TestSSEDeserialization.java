package com.nc.gs;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteOrder;

import org.junit.Test;

import com.nc.io.ByteArrayObjectDataInput;
import com.nc.io.ByteArrayObjectDataOutput;

public class TestSSEDeserialization {

	private static long[] jniLoop(String s, int itrs, String title) throws IOException {
		try (ByteArrayObjectDataOutput out = new ByteArrayObjectDataOutput(256 * 1024, null, ByteOrder.nativeOrder())) {
			out.writeUTF(s);
			int len = s.length();
			char[] dst = new char[len];
			long[] rv = new long[2];
			try (ByteArrayObjectDataInput in = new ByteArrayObjectDataInput(out.bytes(), null, ByteOrder.nativeOrder())) {
				msg("Starting jni %s\n", title);

				long start = System.currentTimeMillis();
				boolean ok = true;
				for (int i = 0; i < itrs; i++) {
					String r = in.readUTFJNI();

					if (r.length() != len) {
						ok = false;
						break;
					}
					in.reset();
				}

				long elapsed = System.currentTimeMillis() - start;
				rv[0] = elapsed;

				msg("JNI[%s]: Itrs: %d. Elapsed: %d\n", title, itrs, elapsed);

				if (!ok) {
					System.err.println("Failed");
				}

				System.gc();
				System.gc();
				System.gc();
				System.gc();

				start = System.currentTimeMillis();
				for (int i = 0; i < itrs; i++) {
					in.inflateUTFJNI(dst);
					in.reset();
				}
				elapsed = System.currentTimeMillis() - start;
				rv[1] = elapsed;

				msg("JNI(Inflate)[%s]: Itrs: %d. Elapsed: %d\n", title, itrs, elapsed);

				return rv;
			}
		}
	}

	private static String loadTextSource(String str) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(TestSSEDeserialization.class.getClassLoader().getResourceAsStream("txt/" + str)))) {
			String l;
			while ((l = br.readLine()) != null) {
				sb.append(l);
			}
		}

		return sb.toString();
	}

	private static void loop(String resource) throws IOException {
		msg("-----------------");
		String s = loadTextSource(resource);

		msg("Length %d\n", s.length());

		regularLoop(s, 100, "Warm Up");
		long[] rl = regularLoop(s, 100000, "Bench");

		jniLoop(s, 100, "Warm Up");
		long[] jl = jniLoop(s, 100000, "Bench");

		msg("-----------------");

		System.err.printf("\nString len: %d. Regular (elapsed): %d. JNI (elapsed): %d. Regular (elapsed/inflate-only): %d. JNI (elapsed/inflate-only): %d. \nRegular/JNI: %.2f.\nRegular/JNI (Inflate only): %.2f\n", s.length(), rl[0], jl[0], rl[1], jl[1], ((double) rl[0]) / jl[0], ((double) rl[1]) / jl[1]);
	}

	private static void msg(String fmt, Object... args) {
		// System.out.printf(fmt, args);
	}

	private static long[] regularLoop(String s, int itrs, String title) throws IOException {
		try (ByteArrayObjectDataOutput out = new ByteArrayObjectDataOutput(256 * 1024, null, ByteOrder.nativeOrder())) {
			out.writeUTF(s);
			int len = s.length();
			char[] dst = new char[len];
			long[] rv = new long[2];
			try (ByteArrayObjectDataInput in = new ByteArrayObjectDataInput(out.bytes(), null, ByteOrder.nativeOrder())) {
				msg("Starting Regular %s\n", title);

				long start = System.currentTimeMillis();
				boolean ok = true;
				for (int i = 0; i < itrs; i++) {
					String r = in.readUTF();

					if (r.length() != len) {
						ok = false;
						break;
					}
					in.reset();
				}

				long elapsed = System.currentTimeMillis() - start;
				rv[0] = elapsed;

				msg("Regular[%s]: Itrs: %d. Elapsed: %d\n", title, itrs, elapsed);

				if (!ok) {
					System.out.println("Failed");
				}

				System.gc();
				System.gc();
				System.gc();
				System.gc();

				start = System.currentTimeMillis();
				for (int i = 0; i < itrs; i++) {
					in.inflateUTF(dst);
					in.reset();
				}
				elapsed = System.currentTimeMillis() - start;
				rv[1] = elapsed;
				msg("Regular(Inflate)[%s]: Itrs: %d. Elapsed: %d\n", title, itrs, elapsed);

				return rv;
			}
		}

	}

	@Test
	public void asciiLoop() throws IOException {
		loop("pure_ascii.txt");
	}

	@Test
	public void danteLoop() throws IOException {
		loop("paradiseXXXIII.txt");
	}

	@SuppressWarnings("resource")
	@Test
	public void sample() throws IOException {
		ByteArrayObjectDataOutput out = new ByteArrayObjectDataOutput(256 * 1024, null, ByteOrder.nativeOrder());

		String left = "OneÃº¹²£¢¬←ŋðßæ»©“”n¢ŧ®ħŋħŋµ“đ©»«ðßð/€²³£ŧ←ħ̉ øµ”“³©»";
		out.writeUTF(left);
		String right = "Two{¬¢£đŋ↓¬ħ←ŋŧđ®ð€ĸ→ħ←ŋŧ€³®đ¢→ĸøłþ[ĸ¬ħŋŧđ®ð€ß?ß®£ŧ¢ŋ←¬{→[ĸøþ´łøĸ→ħŋđ";
		out.writeUTF(right);

		ByteArrayObjectDataInput in = new ByteArrayObjectDataInput(out.toByteArray(), null, ByteOrder.nativeOrder());

		assertEquals(left, in.readUTFJNI());
		assertEquals(right, in.readUTFJNI());
	}

}