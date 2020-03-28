package ch.epfl.javass;

/**
 * Class that facilitates the checking of preconditions
 * 
 * @author Aman Bansal, Julian Blackwell
 */
public final class Preconditions {

	/**
	 * Private constructor with no commands so as to rend the class non-instanciable
	 */
	private Preconditions() {
	}

	/**
	 * Function which throws an exception if its argument is false
	 * 
	 * @param b : the condition to be checked
	 * @throws IllegalArgumentException if the condition is not met
	 */
	public static void checkArgument(boolean b) throws IllegalArgumentException {
		if (!b) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Function which throws an exception if the index is negative or >= size
	 * 
	 * @param index : the starting index
	 * @param size  : maximum size
	 * @return index : the index
	 * @throws IndexOutOfBoundsException if the index is negative or >= size
	 */
	public static int checkIndex(int index, int size) throws IndexOutOfBoundsException {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}

		return index;
	}
}
