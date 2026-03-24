[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/ga-fun/push-swap4j)

# push-swap4j
A java implementation of the push-swap problem

## Some algorithms implemented in this project

### Turk
The most basic algorithm:
1. It pushes all the elements of A but 3 to B
2. Then sort the three remaining elements of A
3. In a second phase, in a loop while B stack is not empty, it searches for the cheapest element to push to A, keeping A sorted
4. Finally, it rotates A to position the smallest element at the top

#### Results:
- 100 elements: 570 operations
- 500 elements: 5157 operations

### LisTurk
An improved version of Turk that uses the [longest increasing subsequence](https://en.wikipedia.org/wiki/Longest_increasing_subsequence) to push elements to B (replace the 1 and 2 steps of Turk).

#### Results:
- 100 elements: 534 operations
- 500 elements: 5015 operations

### Butterfly

Here is the description of the algorithm:
1. It pushes all elements of A to B except the 3 largest ones. It uses a sliding window that defines which elements are "eligible" to be pushed to B at any given moment. Starting with the smallest values, it slides progressively through the range. Elements smaller than the window are pushed to the bottom of B (pb, rb), elements inside the window are pushed to the top of B (pb), and elements larger than the window wait in A and the next element is processed. Every time an element is pushed to B, the window is slid by one position.  
This implementation has an extra optimization that switches head of B when an element was pushed from inside the window and is smaller than the current head of B. The idea is to have the top of B almost reverse sorted. 
2. Then sort the remaining elements of A
3. In a second phase, in a loop while B stack is not empty, push back the highest element from B to A, keeping A sorted.

#### Results:
- 100 elements: 516 operations
- 500 elements: 4658 operations

These results are very sensitive to the window size in the Butterfly algorithm, the value used by this implementation is empirical and may not be optimal.

These results can be improved by keeping more elements in A during the first phase. The A sorting phase uses a [BFS](https://en.wikipedia.org/wiki/Breadth-first_search) approach to find the optimal sequence of operations.
With a 7 elements kept in A, the results are:  
- 100 elements: 508 operations
- 500 elements: 4640 operations

### LisButterfly
A variant of Butterfly that pushes all the elements but the [longest increasing subsequence](https://en.wikipedia.org/wiki/Longest_increasing_subsequence) to B (replace the 1 and 2 steps of Butterfly).  
The benefit is this LIS is usually far larger than the number of elements we can raisonnably sort in an optimal way in point 2 of the Butterfly algorithm.

This implementation also includes various local optimizations to improve performance:
- when n is the highest element of B and n-1 is at the top of A and n is not at bottom of B, we first push n-1 back to A, then n and then, we perform a sa.
- when we are rotating B (with rb) to reach n (the element to push) and n-1 is somewhere near the bottom of B, the last rb is replaced by a sb. This replaces all "rb pa rrb" sequences by "sb pa".
- this sorter makes its best effort to use grouped operations when possible (e.g. ss instead of sa sb).

#### Results:
- 100 elements: 463 operations
- 500 elements: 4350 operations