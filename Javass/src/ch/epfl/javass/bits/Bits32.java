package ch.epfl.javass.bits;

import static ch.epfl.javass.Preconditions.checkArgument;

/**
 * Class which allows working with 32 bit vectors (integers)
 * 
 * @author Julian Blackwell (289803)
 * @author Aman Bansal (297535)
 */
public final class Bits32 {

	private Bits32() {
	}

	/**
	 * Function used to return an integer whose bits of index from start to start +
	 * size are 1
	 * 
	 * @param start (int) : the starting index
	 * @param size  (int) : the size
	 * @return an integer whose bits of index from start to start + size are 1
	 * @throws IllegalArgumentException : if start and size do not designate a valid
	 *                                  range of bits (0 to 32 included)
	 */
	public static int mask(int start, int size) throws IllegalArgumentException {

		checkArgument(start >= 0 && size >= 0 && start + size <= Integer.SIZE);

		return size == 0 ? 0 : (-1 >>> (Integer.SIZE - size)) << start;
	}

	/**
	 * Function used to return an integer whose size least significant bits are
	 * equal to those of @param bits going from start to start + size
	 * 
	 * @param bits  (int) : an integer to extract bits from
	 * @param start (int) : the starting index of the bits to extract
	 * @param size  (int) : the size of the bit string to be extracted
	 * @return an integer whose size least significant bits are equal to those
	 *         of @param bits going from start to start + size
	 * @throws IllegalArgumentException : if start and size do not designate a valid
	 *                                  range of bits (0 to 32 included)
	 */
	public static int extract(int bits, int start, int size) throws IllegalArgumentException {

		checkArgument(start >= 0 && size >= 0 && start + size <= Integer.SIZE);

		return (mask(start, size) & bits) >>> start;
	}

	/**
	 * Packs two values into one integer
	 * 
	 * @param v1 (int) : the first value to be packed
	 * @param v2 (int) : the second value to be packed
	 * @param s1 (int) : the size of the bit string of the first value
	 * @param s2 (int) : the size of the bit string of the second value
	 * @return (int) : an integer with v1 stored in the s1 least significant bits
	 *         and v2 in the next s2 bits
	 * @throws IllegalArgumentException : if one of the sizes isn't included between
	 *                                  1 and 32 (both included), if one of the
	 *                                  values occupies more bits than its size, or
	 *                                  if the sum of the sizes is greater than 32
	 */
	public static int pack(int v1, int s1, int v2, int s2) throws IllegalArgumentException {

		checkArgument(s1 > 0 && s2 > 0 && s1 + s2 <= Integer.SIZE);
		checkValueSize(v1, s1);
		checkValueSize(v2, s2);

		return (v2 << s1) + v1;
	}

	/**
	 * Overload of the pack method which allows to pack three values
	 * 
	 * @param v1, v2, v3 (int) : the values to be packed
	 * @param s1, s2, s3 (int) : the bit string lengths of the corresponding values
	 * @return (int) : an integer with all the values packed
	 * @throws IllegalArgumentException : if one of the sizes isn't included between
	 *                                  1 and 32 (both included), if one of the
	 *                                  values occupies more bits than its size, or
	 *                                  if the sum of the sizes is greater than 32
	 */
	public static int pack(int v1, int s1, int v2, int s2, int v3, int s3) throws IllegalArgumentException {

		checkArgument(s3 > 0 && s1 + s2 + s3 <= Integer.SIZE);
		checkValueSize(v3, s3);

		return (v3 << (s2 + s1)) + pack(v1, s1, v2, s2);
	}

	/**
	 * Overload of the pack method which allows to pack seven values
	 * 
	 * @param v1, v2, v3, v4, v5, v6, v7 (int) : the values to be packed
	 * @param s1, s2, s3, s4, s5, s6, s7 (int) : the sizes of the bit strings of the
	 *            corresponding values
	 * @return (int) : an integer with all the values packed
	 * @throws IllegalArgumentException : if one of the sizes isn't included between
	 *                                  1 and 32 (both included), if one of the
	 *                                  values occupies more bits than its size, or
	 *                                  if the sum of the sizes is greater than 32
	 */
	public static int pack(int v1, int s1, int v2, int s2, int v3, int s3, int v4, int s4, int v5, int s5, int v6,
			int s6, int v7, int s7) throws IllegalArgumentException {

		checkArgument(s1 > 0 && s1 + s2 + s3 + s4 + s5 + s6 + s7 <= Integer.SIZE);
		checkValueSize(v1, s1);

		int n = pack(v5, s5, v6, s6, v7, s7);
		n = n << (s2 + s3 + s4);
		n += pack(v2, s2, v3, s3, v4, s4);

		n = n << s1;
		n += v1;

		return n;
	}

	private static void checkValueSize(int v, int s) {
		checkArgument(s == Integer.SIZE - 1 || v < (1 << s));
	}
}