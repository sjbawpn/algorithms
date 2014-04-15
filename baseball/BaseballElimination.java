import java.util.HashMap;
import java.util.ArrayList;
public class BaseballElimination {

    private HashMap<String, Integer> teamNum = new HashMap<String, Integer>();
    private HashMap<String, ArrayList<String>> elimList
                                = new HashMap<String, ArrayList<String>>();
    
    private String[] names;
    private int[] wins;
    private int[] losses;
    private int[] left;
    private int[] gamesLeft;
    private int n;
    // get the a unique number out of 2; 

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        In f = new In(filename);
        n = f.readInt();
        int pairCount = ((n-1) * (1 + n-1)) / 2;
        // 3 + 2 + 1 
        names = new String[n];
        wins = new int[n];
        losses = new int[n];
        left = new int[n];
        gamesLeft = new int[pairCount];

        for (int i = 0; i < n; i++) {
            names[i] = f.readString();
            teamNum.put(names[i], i);
            wins[i] = f.readInt();
            losses[i] = f.readInt();
            left[i] = f.readInt();
            for (int j = 0; j < n; j++) {
                int gameLeft = f.readInt();
                if (i < j) {
                    int idx = encode(i, j, n);
                    gamesLeft[idx] = gameLeft;

                }
            }
        }

    }
    // number of teams
    public int numberOfTeams() { 
        return n;
    }

    // all teams
    public Iterable<String> teams() {
        return teamNum.keySet();
    }
    
    // number of wins for given team
    public int wins(String team) {
        checkTeamIsValid(team);
        return wins[teamNum.get(team)];
    }
    // number of losses for given team
    public int losses(String team) {
        checkTeamIsValid(team);
        return losses[teamNum.get(team)];
    }
    
    // number of remaining games for given team
    public int remaining(String team) {
        checkTeamIsValid(team);
        return left[teamNum.get(team)];
    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        checkTeamIsValid(team1);
        checkTeamIsValid(team2);
        if (team1.equals(team2)) return 0;
        int idx = encode(teamNum.get(team1), teamNum.get(team2), n);

        return gamesLeft[idx];
    }
    
    // is given team eliminated?
    public boolean isEliminated(String team) {
        checkTeamIsValid(team);
        // number of vertices is number of pairs excluding
        // the pairs involving the team in question
        // + number of teams exlusing the team in question 
        // + 2 for s and t

        ArrayList<String> eliminators = new ArrayList<String>();
        int nNorm = n-1;
        int pairCountNorm = ((nNorm-1) * (1 + nNorm-1)) / 2;
        int V = pairCountNorm + (nNorm) + 2;
        int s = 0;
        int t = V-1;

        FlowNetwork fn = new FlowNetwork(V);
        int num = teamNum.get(team);

        for (int i = 0; i < n; i++) {
            if (i == num) continue;
            int iNorm = i;
            if (i >= num) iNorm = i - 1;
            if (wins[num] + left[num] < wins[i]) {
                eliminators.add(names[i]);
                elimList.put(team, eliminators);
                return true;
            }
            for (int j = i + 1; j < n; j++) {
                if (j == num) continue;
                int jNorm = j;
                if (j >= num) jNorm = j - 1;
                // game left nodes go from 1 to t - nodes - 1
                // the encoding calculation includes the nodes to be excluded.
                // to offset that, we need to deduct the number of games.
                int g = encode(iNorm, jNorm, nNorm) + 1;
                int gameId = encode(i, j, n);
                FlowEdge sg = new FlowEdge(s, g, gamesLeft[gameId]);
                fn.addEdge(sg);
                
                int first = iNorm + 1 + (pairCountNorm);
                
                int second = jNorm + 1 + (pairCountNorm);
                FlowEdge g0 = new FlowEdge(g, first, Double.POSITIVE_INFINITY); 
                FlowEdge g1 = new FlowEdge(g, second, Double.POSITIVE_INFINITY); 
                fn.addEdge(g0);
                fn.addEdge(g1);
            }
            int node = iNorm + 1 + pairCountNorm;
            int cap = wins[num] + left[num] - wins[i];
            FlowEdge et = new FlowEdge(node, t, cap);
            fn.addEdge(et);

        }
        
        FordFulkerson ff = new FordFulkerson(fn, s, t);
        boolean[] marked = new boolean[n];

        for (int i = 0; i < n; i++) {
            if (i == num) continue;
            int iNorm = i;
            if (i >= num) iNorm = i - 1;
            for (int j = i + 1; j < n; j++) {
                if (j == num) continue;
                int jNorm = j;
                if (j >= num) jNorm = j - 1;
                int g = encode(iNorm, jNorm, nNorm) + 1;
                if (ff.inCut(g)) {
                    if (!marked[i]) {
                        eliminators.add(names[i]);
                        marked[i] = true;
                    }
                    if (!marked[j]) {
                        eliminators.add(names[j]);
                        marked[j] = true;
                    }
                }
            }
        }

        if (eliminators.size() != 0) {
            elimList.put(team, eliminators);
        } else 
            elimList.put(team, null);

        return eliminators.size() > 0;
    }


    // subset R of teams that eliminates given team; null if not eliminat
    public Iterable<String> certificateOfElimination(String team) {
        checkTeamIsValid(team);
        if (elimList.get(team) == null)
            isEliminated(team);
        return elimList.get(team);
    }

    private void checkTeamIsValid(String team) {
        if (teamNum.get(team) == null) {
            throw new IllegalArgumentException(team + " is not a valid team");
        }
    }

    private int encode(int a, int b, int base) {
        // to handle permulation of 2 numbers
        // take the min and max of the inputs
        int max = Math.max(a, b);
        int min = Math.min(a, b);
        // give a result that's the max allowed number x
        // the smallest number + the biggets number
        // In order to ensure uniquness.
        int result = (base - 1) * min + max;
        int c = min;
        int num = base - min;
        int offset = (c * (num + base - 1)) / 2;
        int diff = max - min; 
        result = offset + diff;

        // In order to insure a growth of + 1, and since only
        // half maximum allowed numbers will be used,
        // (Since we 0-1 and 1-0 should give the same encoding)
        // subtract the offset so the larger numbers occupy
        // the space of the unused numbers.
        // --------- IMPROVE----
        // int offset = min * (base - 1);
        // result = result - offset - 1;
        
        // numbers from 0 to n+2 are reserved. (teams + s + t)
        // so encode the result to start after n;
        return result - 1;

    }

    private int[] decode(int enc, int base) {
        int c = base - 1;
        int result = enc + 1;
        int min = 0;
        int current = result - c;
        while (current > 0) {
            result = current;
            c = c - 1;
            current = current - c;
            min++;
        }

        int max = result + min;
        return new int[]{min, max};

    }
    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);

        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team))
                    StdOut.print(t + " ");
                    StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }
    
    }


}
