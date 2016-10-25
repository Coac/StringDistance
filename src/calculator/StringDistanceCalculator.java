package calculator;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Calculator of optimal string distances.
 * 
 * This calculator can be used to estimate the minimum transformation cost
 * to turn a string to another. Authorized transformations are :
 * - removing
 * - deleting
 * - replacing
 * only on the starting character of the string.
 * 
 * @author Victor Le <victor.le@ensimag.grenoble-inp.fr>
 * @author Quentin de Longraye <quentin@dldl.fr>
 */
public class StringDistanceCalculator {

	static final double COST_ADD = 1;
	static final double COST_REMOVE = 1;
	static final double COST_CHANGE = 3d/2d;

	/**
	 * Cache system used to store the best cost of a couple of strings.
	 */
	private Map<Entry<String, String>, Double> cache;

	/**
	 * Create a new StringDistanceCalculator with an empty cache.
	 */
	public StringDistanceCalculator() {
		this.cache = new HashMap<>();
	}

	private boolean areEmptyString(String str1, String str2) {
		return str1.length() == 0 && str2.length() == 0;
	}

	private Entry<String, String> getEntry(String str1, String str2) {
		return new AbstractMap.SimpleEntry<String, String>(str1, str2);
	}

	/**
	 * Evaluate the distance between two strings using an naive recursive
	 * method.
	 * 
	 * Given X and Y are two random strings and a and b two characters, then :
	 * 
	 * d(aX, aY) = d(X, Y)
	 * d(aX, bY) = min(addCost + d(aX, Y), removeCost + d(X, bY), changeCost + d(X, Y))
	 * d(ϵ, ϵ) = 0
	 * 
	 * This algorithm is naively applying these formulas.
	 * 
	 * @param str1 First string to compare.
	 * @param str2 Second string to compare.
	 * 
	 * @return the calculated distance.
	 */
	public double evaluate(String str1, String str2) {
		// If strings are empty, cost is null
		if (this.areEmptyString(str1, str2)) {
			return 0.0;
		}

		// If the first string is empty, return the cost to add the remaining
		// characters
		if (str1.length() == 0) {
			return (double) str2.length() * COST_ADD;
		}

		// If the second string is empty, return the cost to remove the
		// remaining characters
		if (str2.length() == 0) {
			return (double) str1.length() * COST_REMOVE;
		}

		// If the two first characters are equals, ignore it and calculate the
		// cost for the remaining ones
		if (str1.charAt(0) == str2.charAt(0)) {
			return this.evaluate(str1.substring(1), str2.substring(1));
		}

		double costAdd = COST_ADD + this.evaluate(str1, str2.substring(1));
		double costRemove = COST_REMOVE + this.evaluate(str1.substring(1), str2);
		double costChange = COST_CHANGE + this.evaluate(str1.substring(1), str2.substring(1));

		// Return the minimum cost between adding, removing or changing the
		// current characters if different
		return Math.min(costAdd, Math.min(costRemove, costChange));
	}

	/**
	 * Evaluate the distance between two strings using an cached recursive
	 * method.
	 * 
	 * This method is faster than the naive evaluation method {@link evaluate}
	 * as it stores the best computed cost for a specific couple of strings. When this
	 * couple appears again later in the execution, the cached value is instantly retrieved
	 * from the cache system without re-calculating all the values.
	 * 
	 * @param str1 First string to compare.
	 * @param str2 Second string to compare.
	 * 
	 * @return the calculated distance.
	 */
	public double evaluateWithCache(String str1, String str2) {

		if (this.areEmptyString(str1, str2)) {
			return 0.0;
		}

		if (str1.length() == 0) {
			return (double) str2.length() * COST_ADD;
		}

		if (str2.length() == 0) {
			return (double) str1.length() * COST_REMOVE;
		}

		// Get the couple of strings used as a cached key
		Map.Entry<String, String> cacheEntry = this.getEntry(str1, str2);

		if (this.cache.containsKey(cacheEntry)) {
			return this.cache.get(cacheEntry);
		}

		if (str1.charAt(0) == str2.charAt(0)) {
			return this.evaluateWithCache(str1.substring(1), str2.substring(1));
		}

		double costAdd = COST_ADD + this.evaluateWithCache(str1, str2.substring(1));
		double costRemove = COST_REMOVE + this.evaluateWithCache(str1.substring(1), str2);
		double costChange = COST_CHANGE + this.evaluateWithCache(str1.substring(1), str2.substring(1));

		double cost = Math.min(costAdd, Math.min(costRemove, costChange));

		// Cache the best cost
		this.cache.put(cacheEntry, cost);

		return cost;
	}

	/**
	 * Evaluate the distance between two strings using an iterative
	 * distance algorithm.
	 * 
	 * This algorithm is using a matrix storing the different successive costs.
	 * 
	 * - The cost for removing a value can be computed using the above one cost and adding the {@link COST_REMOVE}.
	 * - The cost for adding a value can be computed using the left one cost and adding the {@link COST_ADD}.
	 * - The cost for replacing a value can computed using the top left one cost and adding the {@link COST_CHANGE}.
	 * 
	 * The matrix is initialized following the same rule (considering COST_REMOVE = COST_ADD = COST_CHANGE = 1),
	 * so it will be for example :
	 * 
	 *   CAT
	 *  0123
	 * D1000
	 * O2000
	 * G3000
	 * 
	 * The cell [1, 1] is comparing the letter "C" with "D", [2, 1] is comparing "CA" with "D" and so on. Consequently,
	 * the last cell [3, 3] will compare the cost for "CAT" and "DOG".
	 * 
	 * The result matrix will be :
	 * 
	 *   CAT
	 *  0123
	 * D1123
	 * O2223
	 * G3333
	 * 
	 * This allows to use the consecutive calculated costs iteratively and brings to a faster algorithm.
	 * 
	 * @param str1 First string to compare.
	 * @param str2 Second string to compare.
	 * 
	 * @return the calculated distance.
	 */
	public double evaluateIterative(String str1, String str2) {
		double[][] costs = new double[str2.length() + 1][str1.length() + 1];

		double cost;

		for (int i = 0; i < str2.length() + 1; ++i) {
			costs[i][0] = i * COST_REMOVE;
		}

		for (int j = 0; j < str1.length() + 1; ++j) {
			costs[0][j] = j * COST_ADD;
		}

		for (int y = 1; y < str2.length() + 1; ++y) {
			for (int x = 1; x < str1.length() + 1; ++x) {
				if (str1.charAt(x - 1) == str2.charAt(y - 1)) {
					cost = 0;
				} else {
					cost = COST_CHANGE;
				}

				costs[y][x] = Math.min(costs[y - 1][x] + COST_REMOVE,
						Math.min(costs[y][x - 1] + COST_ADD, costs[y - 1][x - 1] + cost));
			}
		}

		return costs[str2.length()][str1.length()];
	}

	public static void main(String[] args) {
		StringDistanceCalculator calculator = new StringDistanceCalculator();

		StringDistanceCalculator.test(calculator, "algorithme", "gorilles");
	}

	public static void test(StringDistanceCalculator calculator, String str1, String str2) {

		long time = System.nanoTime();
		double distance = calculator.evaluate(str1, str2);
		System.out.println("[RECURSIVE][FOUND] Distance " + distance + " found in "
				+ (System.nanoTime() - time) / 1000000000.0 + " seconds");

		time = System.nanoTime();
		distance = calculator.evaluateWithCache(str1, str2);
		System.out.println("[CACHED RECURSIVE][FOUND] Distance " + distance + " found in "
				+ (System.nanoTime() - time) / 1000000000.0 + " seconds");

		time = System.nanoTime();
		distance = calculator.evaluateIterative(str1, str2);
		System.out.println("[ITERATIVE][FOUND] Distance " + distance + " found in "
				+ (System.nanoTime() - time) / 1000000000.0 + " seconds");
	}
}
