# StringDistance
Calculator of optimal string distances.

This calculator can be used to estimate the minimum transformation cost to turn a string to another.  
Authorized transformations are :
- removing
- deleting
- replacing
These transformations are used only on the starting character of the string.


## Formal statement
Given X and Y are two random strings and a and b two characters, then :

	 d(aX, aY) = d(X, Y)
	 d(aX, bY) = min(addCost + d(aX, Y), removeCost + d(X, bY), changeCost + d(X, Y))
	 d(ϵ, ϵ) = 0


## Algorithms
The program implements 3 differents methods to calculate the distance :
- naive algorithm
- dynamic programming algorithm 
- iterative algorithm

### Naive algorithm
This algorithm is naively applying these formulas.

### Dynamic programming algorithm 
This method is faster than the naive evaluation method `evaluate` as it stores the best computed cost for a specific couple of strings. When this couple appears again later in the execution, the cached value is instantly retrieved from the cache system without re-calculating all the values.

### Iteraive programming algorithm 
This algorithm is using a matrix storing the different successive costs.
- The cost for removing a value can be computed using the above one cost and adding the ``COST_REMOVE``.
- The cost for adding a value can be computed using the left one cost and adding the ``COST_ADD``.
- The cost for replacing a value can computed using the top left one cost and adding the ``COST_CHANGE``.
 
The matrix is initialized following the same rule (considering ``COST_REMOVE = COST_ADD = COST_CHANGE = 1``),
so it will be for example :
```
  CAT  
 0123  
D1000
O2000
G3000
```
The cell [1, 1] is comparing the letter "C" with "D", [2, 1] is comparing "CA" with "D" and so on. Consequently,
the last cell [3, 3] will compare the cost for "CAT" and "DOG".

The result matrix will be :
```
  CAT
 0123
D1123
O2223
G3333
```
This allows to use the consecutive calculated costs iteratively and brings to a faster algorithm.
