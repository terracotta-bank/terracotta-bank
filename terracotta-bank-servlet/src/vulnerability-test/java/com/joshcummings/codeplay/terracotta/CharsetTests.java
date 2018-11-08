/*
 * Copyright 2015-2018 Josh Cummings
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.joshcummings.codeplay.terracotta;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CharsetTests {
	private static void printArray(byte[] b) {
		System.out.println(Arrays.toString(b));
	}
	
	private static void printArray(char[] b) {
		System.out.println(Arrays.toString(b));
	}
	
	public static void main(String[] args) {
		String s = "\r\n\0\0\0嘍嘊";
		printArray(s.getBytes(StandardCharsets.UTF_16));
		printArray(s.getBytes(StandardCharsets.UTF_8));
		printArray(s.getBytes(StandardCharsets.ISO_8859_1));
		char[] c = new char[s.length()];
		s.getChars(0, s.length(), c, 0);
		printArray(c);
		// and then cast each element of c to byte
		byte[] b = new byte[c.length];
		for ( int i = 0; i < c.length; i++ ) {
			b[i] = (byte)c[i];
		}
		printArray(b);
	}
}
