package ch.epfl.javass.net;

import java.util.Base64;
import java.util.StringJoiner;

/**
 * Serialize and deserialize values exchanged between the client and server
 * 
 * @author Julian Blackwell (289803)
 * @author Aman Bansal (297535)
 */
public final class StringSerializer {
	private static final int BASE = 16;

	/*
	 * Non instantiable
	 */
	private StringSerializer() {
	}

	/**
	 * Serialize an integer value in its base 16 textual representation
	 * 
	 * @param i (int) : the integer to serialize
	 * @return (String) : base 16 textual representation of the integer
	 */
	public static String serializeInt(int i) {
		return Integer.toUnsignedString(i, BASE);
	}

	/**
	 * Deserialize an integer in its base 16 textual representation
	 * 
	 * @param s (String) : the base 16 textual representation of the integer
	 * @return (int) : the deserialized integer
	 */
	public static int deserializeInt(String s) {
		return Integer.parseUnsignedInt(s, BASE);
	}

	/**
	 * Serialize a long value in its base 16 textual representation
	 * 
	 * @param l (long) : the long to serialize
	 * @return (String) : base 16 textual representation of the long
	 */
	public static String serializeLong(long l) {
		return Long.toUnsignedString(l, BASE);
	}

	/**
	 * Deserialize a long in its base 16 textual representation
	 * 
	 * @param s (String) : the base 16 textual representation of the long
	 * @return (long) : the deserialized long
	 */
	public static long deserializeLong(String s) {
		return Long.parseUnsignedLong(s, BASE);
	}

	/**
	 * Serialize a string by bytes constituting their encoding in UTF-8 by base64
	 * encoding
	 * 
	 * @param s : the string to serialize
	 * @return the serialized string
	 */
	public static String serializeString(String s) {
		return new String(Base64.getEncoder().encode(s.getBytes()));
	}

	/**
	 * Deserialize a string which is in base64 encoding
	 * 
	 * @param s : the string to deserialize
	 * @return the deserialized string
	 * @see StringSerializer#serializeString(String)
	 */
	public static String deserializeString(String s) {
		return new String(Base64.getDecoder().decode(s.getBytes()));
	}

	/**
	 * Combines strings into a single string where they are separated by a given
	 * delimiter
	 * 
	 * @param delimiter : the given delimiter
	 * @param strings   : the list of strings to combine
	 * @return the combined string in which the given strings are separated by the
	 *         given delimiter
	 */
	public static String combine(char delimiter, String... strings) {
		StringJoiner j = new StringJoiner(String.valueOf(delimiter));

		for (String s : strings) {
			j.add(s);
		}

		return j.toString();
	}

	/**
	 * Splits the strings delimited by a given delimiter into a list of strings
	 * 
	 * @param delimiter : the given delimiter
	 * @param s         : the string to split
	 * @return the list of split strings
	 */
	public static String[] split(char delimiter, String s) {
		return s.split(String.valueOf(delimiter));
	}
}