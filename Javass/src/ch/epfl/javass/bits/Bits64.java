package ch.epfl.javass.bits;

import static ch.epfl.javass.Preconditions.checkArgument;

/**
 * Class which allows working with 32 bit vectors (integers)
 * 
 * @author Julian Blackwell (289803)
 * @author Aman Bansal (297535)
 */
public final class Bits64 {

	private Bits64() {
	}

	/**
	 * Function used to return a long whose bits of index from start to start + size
	 * are 1
	 * 
	 * @param start (int) : the starting index
	 * @param size  (int) : the size
	 * @return a long whose bits of index from start to start + size are 1
	 * @throws IllegalArgumentException : if start and size do not designate a valid
	 *                                  range of bits (0 to 64 included)
	 */
	public static long mask(int start, int size) throws IllegalArgumentException {

		checkArgument(start >= 0 && size >= 0 && start + size <= Long.SIZE);

		return size == 0 ? 0 : (-1L >>> (Long.SIZE - size)) << start;
	}

	/**
	 * Function used to return a long whose size least significant bits are equal to
	 * those of @param bits going from start to start + size
	 * 
	 * @param bits  (long) : a long to extract bits from
	 * @param start (int) : the starting index of the bits to extract
	 * @param size  (int) : the size of the bit string to be extracted
	 * @return a long whose size least significant bits are equal to those of @param
	 *         bits going from start to start + size
	 * @throws IllegalArgumentException : if start and size do not designate a valid
	 *                                  range of bits (0 to 64 included)
	 */
	public static long extract(long bits, int start, int size) throws IllegalArgumentException {

		checkArgument(start >= 0 && size >= 0 && start + size <= Long.SIZE);

		return (mask(start, size) & bits) >>> start;
	}

	/**
	 * Function used to pack two values into one integer
	 * 
	 * @param v1 (long) : the first value to be packed
	 * @param v2 (long) : the second value to be packed
	 * @param s1 (int) : the size of the bit string of the first value to be packed
	 * @param s2 (int) : the size of the bit string of the second value to be packed
	 * @return (long) : an integer with v1 stored in the s1 least significant bits
	 *         and v2 in the next s2 bits
	 * @throws IllegalArgumentException : if one of the sizes isn't included between
	 *                                  1 and 64 (both included), if one of the
	 *                                  values occupies more bits than its size, or
	 *                                  if the sum of the sizes is greater than 64
	 */
	public static long pack(long v1, int s1, long v2, int s2) throws IllegalArgumentException {
		checkArgument(s1 > 0 && s2 > 0 && s1 + s2 <= Long.SIZE);
		checkValueSize(v1, s1);
		checkValueSize(v2, s2);

		return (v2 << s1) | v1;
	}

	private static void checkValueSize(long v, int s) {
		checkArgument(s == Long.SIZE - 1 || v < (1L << s));
	}
}