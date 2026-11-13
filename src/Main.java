import java.util.*;

public class Main {

    public static class AhoCorasick {
        private static final int ALPHABET_SIZE = 26;

        public static class Node {
            int[] children = new int[ALPHABET_SIZE];
            int[] go = new int[ALPHABET_SIZE];
            int failureLink = 0;
            int outputLink = 0;
            List<Integer> matchedPatternIndices = new ArrayList<>();

            public Node() {
                Arrays.fill(children, -1);
                Arrays.fill(go, -1);
            }
        }

        public static class Match {
            public final String pattern;
            public final int startIndex;
            public final int endIndex;

            public Match(String pattern, int startIndex, int endIndex) {
                this.pattern = pattern;
                this.startIndex = startIndex;
                this.endIndex = endIndex;
            }

            @Override
            public String toString() {
                return "Pattern \"" + pattern + "\" found at index " + startIndex + ".." + endIndex;
            }
        }

        private final List<Node> trie;
        private final List<String> patterns;

        public AhoCorasick() {
            trie = new ArrayList<>();
            trie.add(new Node());
            patterns = new ArrayList<>();
        }

        // Build Trie for multiple patterns
        public void insert(String pattern) {
            if (pattern == null || pattern.isEmpty()) {
                return;
            }
            int currentNode = 0;
            int patternIndex = patterns.size();
            patterns.add(pattern);

            for (int i = 0; i < pattern.length(); i++) {
                int charIndex = pattern.charAt(i) - 'a';
                if (trie.get(currentNode).children[charIndex] == -1) {
                    trie.get(currentNode).children[charIndex] = trie.size();
                    trie.add(new Node());
                }
                currentNode = trie.get(currentNode).children[charIndex];
            }
            trie.get(currentNode).matchedPatternIndices.add(patternIndex);
        }

        // Build failure links and output links using BFS
        public void buildFailureAndOutputLinks() {
            Queue<Integer> queue = new ArrayDeque<>();
            Node root = trie.get(0);

            for (int c = 0; c < ALPHABET_SIZE; c++) {
                int child = root.children[c];
                if (child != -1) {
                    root.go[c] = child;
                    trie.get(child).failureLink = 0;
                    trie.get(child).outputLink = 0;
                    queue.add(child);
                } else {
                    root.go[c] = 0;
                }
            }

            while (!queue.isEmpty()) {
                int u = queue.poll();
                Node current = trie.get(u);

                for (int c = 0; c < ALPHABET_SIZE; c++) {
                    int v = current.children[c];
                    if (v != -1) {
                        current.go[c] = v;

                        int failState = trie.get(current.failureLink).go[c];
                        Node childNode = trie.get(v);
                        childNode.failureLink = failState;

                        Node failNode = trie.get(failState);
                        if (!failNode.matchedPatternIndices.isEmpty()) {
                            childNode.outputLink = failState;
                        } else {
                            childNode.outputLink = failNode.outputLink;
                        }

                        queue.add(v);
                    } else {
                        current.go[c] = trie.get(current.failureLink).go[c];
                    }
                }
            }
        }

        // Search for all occurrences of the patterns in text
        public int search(String text, List<Match> recordedMatches) {
            int currentState = 0;
            int totalMatches = 0;

            for (int i = 0; i < text.length(); i++) {
                int charIndex = text.charAt(i) - 'a';
                currentState = trie.get(currentState).go[charIndex];

                int tempState = currentState;
                while (tempState != 0) {
                    Node node = trie.get(tempState);
                    if (!node.matchedPatternIndices.isEmpty()) {
                        for (int patIdx : node.matchedPatternIndices) {
                            totalMatches++;
                            if (recordedMatches != null) {
                                String pat = patterns.get(patIdx);
                                int startPos = i - pat.length() + 1;
                                recordedMatches.add(new Match(pat, startPos, i));
                            }
                        }
                    }
                    tempState = node.outputLink;
                }
            }
            return totalMatches;
        }

        public int getNodeCount() {
            return trie.size();
        }
    }

    public static class KMP {

        private static int[] buildLPS(String pattern) {
            int m = pattern.length();
            int[] lps = new int[m];
            int len = 0;
            int i = 1;

            while (i < m) {
                if (pattern.charAt(i) == pattern.charAt(len)) {
                    len++;
                    lps[i] = len;
                    i++;
                } else {
                    if (len != 0) {
                        len = lps[len - 1];
                    } else {
                        lps[i] = 0;
                        i++;
                    }
                }
            }
            return lps;
        }

        public static int searchSinglePattern(String text, String pattern) {
            int n = text.length();
            int m = pattern.length();
            int[] lps = buildLPS(pattern);
            int i = 0;
            int j = 0;
            int count = 0;

            while (i < n) {
                if (pattern.charAt(j) == text.charAt(i)) {
                    i++;
                    j++;
                }
                if (j == m) {
                    count++;
                    j = lps[j - 1];
                } else if (i < n && pattern.charAt(j) != text.charAt(i)) {
                    if (j != 0) {
                        j = lps[j - 1];
                    } else {
                        i++;
                    }
                }
            }
            return count;
        }

        public static int searchAllPatterns(String text, List<String> patterns) {
            int totalMatches = 0;
            for (String pattern : patterns) {
                totalMatches += searchSinglePattern(text, pattern);
            }
            return totalMatches;
        }
    }

    private static String generateRandomString(Random rand, int length, int alphabetPool) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + rand.nextInt(alphabetPool)));
        }
        return sb.toString();
    }

    private static List<String> generatePatterns(Random rand, int count, int minLen, int maxLen, int alphabetPool) {
        List<String> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int len = minLen + rand.nextInt(maxLen - minLen + 1);
            list.add(generateRandomString(rand, len, alphabetPool));
        }
        return list;
    }

    public static void main(String[] args) {
        System.out.println("1. Correctness Verification");
        AhoCorasick demoAc = new AhoCorasick();
        List<String> demoPatterns = Arrays.asList("he", "she", "his", "hers");
        for (String p : demoPatterns) {
            demoAc.insert(p);
        }
        demoAc.buildFailureAndOutputLinks();

        String demoText = "ushers";
        List<AhoCorasick.Match> recordedMatches = new ArrayList<>();
        int acDemoCount = demoAc.search(demoText, recordedMatches);
        int kmpDemoCount = KMP.searchAllPatterns(demoText, demoPatterns);

        System.out.println("Text: \"" + demoText + "\"");
        System.out.println("Patterns: " + demoPatterns);
        System.out.println("Trie Nodes: " + demoAc.getNodeCount());
        for (AhoCorasick.Match m : recordedMatches) {
            System.out.println("  " + m);
        }
        System.out.println("Matches (Aho-Corasick): " + acDemoCount);
        System.out.println("Matches (Separate KMP): " + kmpDemoCount);
        System.out.println();

        System.out.println("2. Performance Benchmark (Average of 5 runs)");
        int[] patternCounts = {10, 100, 1000};
        int[] textLengths = {1000, 10000, 100000};
        int runs = 5;
        Random rand = new Random(42);

        System.out.printf("%-10s | %-10s | %-14s | %-14s | %-14s | %-14s | %-10s | %-10s%n",
                "Patterns", "Text Size", "AC Build (ms)", "AC Search (ms)", "AC Total (ms)", "KMP Total (ms)", "Speedup", "Matches");

        for (int pCount : patternCounts) {
            List<String> patterns = generatePatterns(rand, pCount, 5, 10, 4);

            for (int tLen : textLengths) {
                String text = generateRandomString(rand, tLen, 4);

                // Warm-up run
                AhoCorasick warmupAc = new AhoCorasick();
                for (String p : patterns) {
                    warmupAc.insert(p);
                }
                warmupAc.buildFailureAndOutputLinks();
                warmupAc.search(text, null);
                KMP.searchAllPatterns(text, patterns);

                long sumAcBuildNs = 0;
                long sumAcSearchNs = 0;
                long sumKmpTotalNs = 0;
                int verifiedMatches = 0;

                for (int r = 0; r < runs; r++) {
                    long t0 = System.nanoTime();
                    AhoCorasick ac = new AhoCorasick();
                    for (String p : patterns) {
                        ac.insert(p);
                    }
                    ac.buildFailureAndOutputLinks();
                    long t1 = System.nanoTime();

                    long t2 = System.nanoTime();
                    int acMatches = ac.search(text, null);
                    long t3 = System.nanoTime();

                    long t4 = System.nanoTime();
                    int kmpMatches = KMP.searchAllPatterns(text, patterns);
                    long t5 = System.nanoTime();

                    if (acMatches != kmpMatches) {
                        throw new IllegalStateException("Match count mismatch!");
                    }

                    verifiedMatches = acMatches;
                    sumAcBuildNs += (t1 - t0);
                    sumAcSearchNs += (t3 - t2);
                    sumKmpTotalNs += (t5 - t4);
                }

                double avgAcBuildMs = (sumAcBuildNs / (double) runs) / 1_000_000.0;
                double avgAcSearchMs = (sumAcSearchNs / (double) runs) / 1_000_000.0;
                double avgAcTotalMs = avgAcBuildMs + avgAcSearchMs;
                double avgKmpTotalMs = (sumKmpTotalNs / (double) runs) / 1_000_000.0;
                double speedup = avgKmpTotalMs / avgAcTotalMs;

                System.out.printf("%-10d | %-10d | %-14.4f | %-14.4f | %-14.4f | %-14.4f | %-9.2fx | %-10d%n",
                        pCount, tLen, avgAcBuildMs, avgAcSearchMs, avgAcTotalMs, avgKmpTotalMs, speedup, verifiedMatches);
            }
        }
    }
}