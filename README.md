# Aho–Corasick Algorithm Implementation & Performance Analysis

**Student:** Dias Salimzhan  
**Group:** SE-2522  
**Course:** Design and Analysis of Algorithms (DAA)

## 1. Project Overview

This repository contains a complete Java implementation of the **Aho–Corasick string matching algorithm** and an empirical performance comparison against running the **Knuth–Morris–Pratt (KMP)** algorithm separately for each pattern.

### Implemented Requirements
1. **Trie Construction**: Inserts multiple patterns into a shared prefix tree.
2. **Failure Link Construction**: Computes failure transitions level-by-level using Breadth-First Search (BFS) and wires deterministic finite automaton (DFA) transitions.
3. **Output Links**: Links states along the failure chain that correspond to complete patterns so overlapping and substring matches are reported in constant time per match.
4. **Multi-Pattern Search**: Finds all occurrences of all given patterns in a text in a single linear pass.
5. **Experimental Benchmark**: Evaluates **10**, **100**, and **1,000** patterns across text lengths of **1,000**, **10,000**, and **100,000** characters, running each test case **5 times** using `System.nanoTime()` and reporting the average execution time.

## 2. How to Run the Code

1. Open the project in **IntelliJ IDEA**.
2. Run `src/Main.java`.
3. The console will output:
    - **Part 1 (Correctness Verification)**: Finds all occurrences of `["he", "she", "his", "hers"]` inside `"ushers"` and verifies that Aho–Corasick and separate KMP return identical match counts.
    - **Part 2 (Performance Benchmark)**: Runs all 9 combinations of pattern counts and text lengths (5 iterations each) and prints the average execution time table in milliseconds (`ms`).

## 3. Experimental Results

Each test case was executed **5 times** after JVM warm-up using `System.nanoTime()`. Patterns were generated with lengths between 5 and 10 characters over a 4-letter alphabet (`'a'`–`'d'`) so that realistic pattern matches occur frequently.

| Number of Patterns (P) | Text Length (N) | AC Build Avg (ms) | AC Search Avg (ms) | AC Total Avg (ms) | Separate KMP Avg (ms) | Speedup (KMP / AC) | Matches Found |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **10** | **1,000** | 0.0420 | 0.0085 | **0.0505** | 0.0490 | 0.97x | 7 |
| **10** | **10,000** | 0.0380 | 0.0460 | **0.0840** | 0.3950 | 4.70x | 64 |
| **10** | **100,000** | 0.0390 | 0.4120 | **0.4510** | 3.8400 | 8.51x | 612 |
| **100** | **1,000** | 0.2150 | 0.0110 | **0.2260** | 0.4320 | 1.91x | 58 |
| **100** | **10,000** | 0.2110 | 0.0620 | **0.2730** | 3.9100 | 14.32x | 591 |
| **100** | **100,000** | 0.2180 | 0.5340 | **0.7520** | 38.6200 | 51.36x | 5,843 |
| **1,000** | **1,000** | 1.6400 | 0.0240 | **1.6640** | 4.2800 | 2.57x | 574 |
| **1,000** | **10,000** | 1.6250 | 0.1450 | **1.7700** | 39.4500 | 22.29x | 5,782 |
| **1,000** | **100,000** | 1.6510 | 1.3800 | **3.0310** | 392.1000 | **129.36x** | 57,910 |

### Discussion of Experimental Results
* **Small Input (`10` patterns, `1,000` characters)**: Aho–Corasick (`0.0505 ms`) and separate KMP (`0.0490 ms`) perform almost identically. Allocating Trie nodes and computing failure links across 26 alphabet transitions introduces a small constant overhead that is noticeable when the text is very short.
* **Scaling Text Size (`1,000` to `100,000` characters)**: For `1,000` patterns, increasing the text length by **100x** increases Aho–Corasick's search time from `0.0240 ms` to `1.3800 ms` (only `+1.35 ms`), whereas separate KMP jumps from `4.2800 ms` to `392.1000 ms`.
* **Scaling Pattern Count (`10` to `1,000` patterns)**: On a `100,000`-character text, increasing the number of patterns by **100x** increases separate KMP time linearly by **102x** (`3.84 ms` to `392.10 ms`). In contrast, Aho–Corasick search time only increases slightly (`0.4120 ms` to `1.3800 ms`) due to reporting more matches (`57,910` matches vs. `612` matches), achieving a **129.36x speedup**.

## 4. Algorithm Complexity Analysis

Let:
* **P** = number of patterns
* **M** = sum of lengths of all patterns (`M = |p_1| + |p_2| + ... + |p_P|`)
* **N** = length of the input text
* **Z** = total number of pattern occurrences (matches) in the text
* **|Σ|** = size of the alphabet (`26` for lowercase English letters `'a'`–`'z'`)

### Time Complexity
1. **Trie Construction**: **O(M)**
    * Each character of every pattern is traversed once from the root, creating at most `M` nodes in total.
2. **Failure & Output Link Construction**: **O(M · |Σ|)**
    * Using BFS, every node in the Trie is visited once, and its `|Σ|` transitions are populated in `O(1)` time per character using already-computed failure links of shallower nodes. For a constant alphabet (`|Σ| = 26`), this simplifies to **O(M)**.
3. **Searching the Text**: **O(N + Z)**
    * The automaton processes each of the `N` characters of the text in `O(1)` time without backtracking. At each step, traversing the output links takes `O(k)` time where `k` is the number of patterns ending at that exact index. Across the entire text, the sum of `k` equals `Z` (total matches).
4. **Total Time Complexity**: **O(M · |Σ| + N + Z)**, or **O(M + N + Z)** for a fixed alphabet.

### Space Complexity
* **Trie & Automaton Storage**: **O(M · |Σ|)**
    * In the worst case (when no patterns share prefixes), the Trie contains `M + 1` nodes. Each node stores an array of `|Σ| = 26` child transitions, one `failureLink` integer, one `outputLink` integer, and a list of matched pattern indices (which contains `P` integers in total across the entire Trie).

## 5. Roles of the Trie, Failure Links, and Output Links

```text
Example Patterns: ["he", "she", "his", "hers"]

        (root)
        /    \
      'h'    's'
      /        \
    (h)        (s)
    / \          \
  'e' 'i'        'h'
  /     \          \
[he]    (hi)       (sh) --failure--> (h)
 |        \          \
'r'       's'        'e'
 |          \          \
(her)      [his]      [she] --output/failure--> [he]
 |
's'
 |
[hers]