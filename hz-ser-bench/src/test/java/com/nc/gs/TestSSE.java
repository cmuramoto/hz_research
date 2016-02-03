package com.nc.gs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteOrder;

import com.nc.io.ByteArrayObjectDataInput;
import com.nc.io.ByteArrayObjectDataOutput;

public class TestSSE {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		ByteArrayObjectDataOutput out = new ByteArrayObjectDataOutput(256 * 1024, null, ByteOrder.nativeOrder());

		out.writeUTF("Poop");
		out.writeUTF("Tart");

		ByteArrayObjectDataInput in = new ByteArrayObjectDataInput(out.toByteArray(), null, ByteOrder.nativeOrder());

		System.out.println(in.readUTFJNI());
		System.out.println(in.readUTFJNI());

		System.out.println("-----------------");
		
		danteLoop();
		
		System.out.println("-----------------");
		
		asciiLoop();
	}

	public static void danteLoop() throws IOException {
		loop("paradiseXXXIII.txt");
	}

	public static void asciiLoop() throws IOException {
		loop("pure_ascii.txt");
	}
	
	private static void loop(String resource) throws IOException {
		String s = loadTextSource(resource);

		System.out.println("Length " + s.length());

		regularLoop(s, 100, "Warm Up");
		regularLoop(s, 100000, "Bench");

		jniLoop(s, 100, "Warm Up");
		jniLoop(s, 100000, "Bench");
	}

	private static void jniLoop(String s, int itrs, String title) throws IOException {
		try (ByteArrayObjectDataOutput out = new ByteArrayObjectDataOutput(256 * 1024, null, ByteOrder.nativeOrder())) {
			out.writeUTF(s);
			int len = s.length();
			char[] dst = new char[len];
			try (ByteArrayObjectDataInput in = new ByteArrayObjectDataInput(out.bytes(), null,
					ByteOrder.nativeOrder())) {
				System.out.println("Starting jni " + title);

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

				System.out.printf("JNI[%s]: Itrs: %d. Elapsed: %d\n", title, itrs, elapsed);

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

				System.out.printf("JNI(Inflate)[%s]: Itrs: %d. Elapsed: %d\n", title, itrs, elapsed);
			}
		}
	}

	private static void regularLoop(String s, int itrs, String title) throws IOException {
		try (ByteArrayObjectDataOutput out = new ByteArrayObjectDataOutput(256 * 1024, null, ByteOrder.nativeOrder())) {
			out.writeUTF(s);
			int len = s.length();
			char[] dst = new char[len];
			try (ByteArrayObjectDataInput in = new ByteArrayObjectDataInput(out.bytes(), null,
					ByteOrder.nativeOrder())) {
				System.out.println("Starting Regular " + title);

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

				System.out.printf("Regular[%s]: Itrs: %d. Elapsed: %d\n", title, itrs, elapsed);

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

				System.out.printf("Regular(Inflate)[%s]: Itrs: %d. Elapsed: %d\n", title, itrs, elapsed);

			}
		}

	}

	private static String loadTextSource(String str) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(TestSSE.class.getClassLoader().getResourceAsStream("txt/" + str)))) {
			String l;
			while ((l = br.readLine()) != null) {
				sb.append(l);
			}
		}

		return sb.toString();
	}

}
