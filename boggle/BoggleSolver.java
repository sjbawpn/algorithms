import java.util.ArrayList;
import java.util.HashSet;

public class BoggleSolver {
    // Initializes the data structure using the
    // given array of strings as the dictionary.
    // (You can assume each word in the dictionary
    // contains only the uppercase letters A through Z.)
    private TrieSETp trieSet;

    public BoggleSolver(String[] dictionary) {
        trieSet = new TrieSETp();
        for (String st : dictionary) {
            trieSet.add(st);
        }

    }
    
    private int pos(int row, int col, int w, int h) {
        return row * w + col; 
    }

    private int col(int pos, int w) {
         return pos % w;
    }

    private int row(int pos, int w) {
        return pos / w;
    }
    private Iterable<Integer> adj(int pos, int w, int h) {
        ArrayList<Integer> adjList = new ArrayList<Integer>();

        int row = row(pos, w);
        int col = col(pos, w);
        if (row > 0) adjList.add(pos(row - 1, col, w, h));
        if (col > 0) adjList.add(pos(row, col - 1, w, h));
        if (row > 0 && col > 0) adjList.add(pos(row - 1, col - 1, w, h));
        if (row > 0 && col < w - 1) adjList.add(pos(row - 1, col + 1, w, h));

        if (row < h - 1) adjList.add(pos(row + 1, col, w, h));
        if (col < w - 1) adjList.add(pos(row, col + 1, w, h));
        if (row < h - 1  && col > 0) adjList.add(pos(row + 1, col - 1, w, h));
        if (row < h - 1  && col < w - 1) adjList.add(pos(row + 1, col + 1, w, h));

        
        return adjList;
    }

    // Returns the set of all valid words in the given Boggle board, as an Iterable.
    public Iterable<String> getAllValidWords(BoggleBoard board) {
        int rows = board.rows();
        int cols = board.cols();

        HashSet<String> solutions = new HashSet<String>();
        
        for (int i = 0; i  < rows; i++) {
            for (int j = 0; j < cols; j++) {
                HashSet<Integer> chain = new HashSet<Integer>();
                //System.out.print(board.getLetter(i, j));
                int p = pos(i, j, cols, rows);
                chain.add(p);
                String str = getLetter(board.getLetter(i, j));
                dfs(str, board, p, solutions, chain);
            
            }
        }
        return solutions;
    }

    private void dfs(String prefix, BoggleBoard board, int p,
                        HashSet<String> solutions, HashSet<Integer> chain) {
        int rows = board.rows();
        int cols = board.cols();
        //if (!trieSet.prefixMatch(str).iterator().hasNext()) return;
        
        for (int a : adj(p, cols, rows)) {
            if (!chain.contains(a)) {
                char c = board.getLetter(row(a, cols), col(a, cols));
                String curr = prefix + getLetter(c);
                // System.out.println("num=" + a +" Current="+curr);
                if (!solutions.contains(curr) && curr.length() > 2
                    && trieSet.contains(curr)) solutions.add(curr);

                boolean search = false; 
                /*
                if (!prefixList.containsKey(curr)) {
                    HashSet<String> list = new HashSet<String>();
                    for (String str : trieSet.prefixMatch(curr)) {
                        list.add(str);
                    }
                    prefixList.put(curr, list);
                } 

                if (prefixList.get(curr).size() == 0) search = false;
                for (String str: trieSet.prefixMatch(curr)) {
                    search = true;
                    break;
                }
                */
                search = trieSet.hasMore(prefix);
                if (search) {
                    chain.add(a);
                    dfs(curr, board, a, solutions, chain);
                    chain.remove(a);
                }

            }

        }
    }

    private String getLetter(char c) {
        StringBuilder b = new StringBuilder();
        b.append(c);
        if (c == 'Q' || c == 'q') b.append('U');
        return b.toString();
    }

    // Returns the score of the given word if it is in the dictionary,
    //  zero otherwise.
    // (You can assume the word contains only the uppercase letters A through Z.)
    public int scoreOf(String word) {
        if (!trieSet.contains(word) || word.length() < 3) return 0;
        if (word.length() < 5) return 1;
        if (word.length() == 5) return 2;
        if (word.length() == 6) return 3;
        if (word.length() == 7) return 5;
        return 11;
    }

    public static void main(String[] args) {
        In in = new In(args[0]);
        String[] dictionary = in.readAllStrings();
        BoggleSolver solver = new BoggleSolver(dictionary);
        BoggleBoard board = new BoggleBoard(args[1]);
        int score = 0;
        for (String word : solver.getAllValidWords(board)) {
            StdOut.println(word);
            score += solver.scoreOf(word);
        }
        StdOut.println("Score = " + score);
    }
}
