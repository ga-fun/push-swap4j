package com.fathzer.pushswap;

import java.util.Arrays;
import java.util.BitSet;

/** A Circular <a href="https://en.wikipedia.org/wiki/Longest_increasing_subsequence">Longest Increasing Subsequence (LIS)</a> finder that works on arrays of integers with no duplicates and all values in [0, n-1] (normalized).
 * <br>
 * It is based on the <a href="https://en.wikipedia.org/wiki/Segment_tree">Segment Tree</a>.
 */
public class CircularLIS {

    private static class SegmentTree {
        private int[] tree;
        private int size;
        
        public SegmentTree(int n) {
            this.size = n;
            this.tree = new int[4 * n];
        }
        
        // Met à jour la position idx avec la valeur val (si val est plus grand)
        public void update(int node, int start, int end, int idx, int val) {
            if (start == end) {
                tree[node] = Math.max(tree[node], val);
                return;
            }
            
            int mid = (start + end) / 2;
            if (idx <= mid) {
                update(2 * node, start, mid, idx, val);
            } else {
                update(2 * node + 1, mid + 1, end, idx, val);
            }
            tree[node] = Math.max(tree[2 * node], tree[2 * node + 1]);
        }
        
        public void update(int idx, int val) {
            update(1, 0, size - 1, idx, val);
        }
        
        // Requête pour obtenir le maximum dans [l, r]
        public int query(int node, int start, int end, int l, int r) {
            if (r < start || end < l) {
                return 0;
            }
            if (l <= start && end <= r) {
                return tree[node];
            }
            
            int mid = (start + end) / 2;
            int leftMax = query(2 * node, start, mid, l, r);
            int rightMax = query(2 * node + 1, mid + 1, end, l, r);
            return Math.max(leftMax, rightMax);
        }
        
        public int query(int l, int r) {
            if (l > r) return 0;
            return query(1, 0, size - 1, l, r);
        }
        
        public void reset() {
            for (int i = 0; i < tree.length; i++) {
                tree[i] = 0;
            }
        }
    }

    private int maxLen;
    private int lastValue;
    private int[] dp;
    private int[] parent;
    private int[] indexInSeq;
    private SegmentTree segTree;
    
    private CircularLIS(int n) {
        this.maxLen = 0;
        this.lastValue = -1;
        this.dp = new int[n];
        this.parent = new int[n];
        this.indexInSeq = new int[n];
        this.segTree = new SegmentTree(n);
    }

    private void reset() {
        Arrays.fill(parent, -1);
        Arrays.fill(indexInSeq, -1);
        maxLen = 0;
        lastValue = -1;
        segTree.reset();        
    }
    
    private void processCircularLoop(int[] arr, int start) {
        // Reset arrays for this start position
        reset();
        
        int n = arr.length;
        // Parcourir la liste circulairement à partir de 'start'
        for (int i = 0; i < n; i++) {
            int idx = (start + i) % n;
            int value = arr[idx];
            
            // Trouver la plus longue sous-séquence croissante se terminant avant 'value'
            int maxBefore = segTree.query(0, value - 1);
            
            // La LIS se terminant en 'value' est maxBefore + 1
            int currentLIS = maxBefore + 1;
            
            dp[value] = currentLIS;
            indexInSeq[value] = idx;
            
            // Trouver le parent (la valeur précédente dans la LIS)
            if (maxBefore > 0) {
                for (int v = value - 1; v >= 0; v--) {
                    if (dp[v] == maxBefore) {
                        parent[value] = v;
                        break;
                    }
                }
            }
            
            // Mettre à jour le segment tree
            segTree.update(value, currentLIS);
            
            if (currentLIS > maxLen) {
                maxLen = currentLIS;
                lastValue = value;
            }
        }
    }

    private void setResult(BitSet result) {
        result.clear();
        int current = lastValue;
        for (int i = maxLen - 1; i >= 0; i--) {
            result.set(current);
            current = parent[current];
        }
    }
    
    /**
     * Returns the values of the longest increasing subsequence in a circular array.
     * @param arr the array to search (It should contain no duplicates and, if n is the length of the array, all values should be in range [0, n[).
     * <br>If not, the behavior is undefined.
     * @return a BitSet containing the values of the longest increasing subsequence
     */
    public static BitSet get(int[] arr) {
        BitSet result = new BitSet(arr.length);
        int n = arr.length;
        if (n == 0) return result;
        if (n <= 2) {
            // All values are in the LIS
            result.flip(0, n);
            return result;
        }
        
        int maxLISLength = 0;
        CircularLIS instance = new CircularLIS(n);
        
        // On DOIT essayer chaque position de départ possible
        // car la meilleure LIS peut commencer n'importe où
        for (int start = 0; start < n; start++) {
            instance.processCircularLoop(arr, start);
            
            // Reconstruire la séquence si c'est la meilleure
            if (instance.maxLen > maxLISLength) {
                maxLISLength = instance.maxLen;
                instance.setResult(result);
            }
        }
        
        return result;
    }
}