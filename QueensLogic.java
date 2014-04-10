/**
 * N-Queens solved using BDD logic.
 *
 * @author Peter Skovgaard
 *
 */
import java.util.*;

import net.sf.javabdd.*;

public class QueensLogic {
    private int n = 0;
    private int[][] board;

    // Suggested number of nodes and cache from the problem description. Cache = nodes/10
    private int nodes = 2000000;
    private int cache = nodes / 10;

    private BDDFactory factory = JFactory.init(nodes, cache);
    private BDD rules;

    int queensPlacedCounter = 0;

    public QueensLogic() {

    }

    public void initializeGame(int size) {
        this.n = size;
        this.board = new int[n][n];
        int numFields = this.n * this.n;
        factory.setVarNum(numFields);
        rules = factory.one();

        for (int i = 0; i < this.n * this.n; i++) {
            rules.andWith(constructIndividualRule(i));
        }

        rules.andWith(constructExactlyNQueensRule());
    }

    public int[][] getGameBoard() {
        return board;
    }

    public boolean insertQueen(int column, int row) {

        if (board[column][row] == -1 || board[column][row] == 1) {
            return true;
        }

        addQueen(column, row);
        System.out.println(rules.pathCount());

        for (int r = 0; r < this.n; r++) {
            for (int c = 0; c < this.n; c++) {
                if (board[c][r] == 1)
                    continue;

                if (rules.restrict(factory.ithVar((r * this.n) + c)).isZero()) {
                    board[c][r] = -1;
                }
            }
        }

        if (rules.pathCount() == 1)
            addRemainingQueens();

        return true;
    }

    private BDD constructExactlyNQueensRule() {
        BDD queensRule = factory.one();

        for (int col = 0; col < this.n; col++) {
            queensRule.andWith(constructIndividualQueensRowRule(col));
        }

        return queensRule;
    }

    private BDD constructIndividualQueensRowRule(int col) {
        BDD singleQueenRule = factory.zero();

        for (int row = 0; row < this.n; row++) {
            singleQueenRule.orWith(factory.ithVar((row * this.n) + col));
        }

        return singleQueenRule;
    }

    private BDD constructIndividualRule(int pos) {
        BDD posRule = factory.one();

        List<Integer> excluded = getExcluded(pos);

        for (int e : excluded) {
            posRule.andWith(factory.nithVar(e));
        }

        BDD posImpliesRule = factory.ithVar(pos).imp(posRule);

        return posImpliesRule;
    }

    private List<Integer> getExcluded(int pos) {
        // Exclude horizontal
        List<Integer> excluded = excludeHorizontal(pos);

        // Exclude vertical
        excluded.addAll(excludeVertical(pos));

        // Exclude diagonal
        excluded.addAll(excludeDiagonal(pos));

        return excluded;
    }

    // Exclude all below this. All that is above has been excluded by previous node.
    private List<Integer> excludeHorizontal(int pos) {
        List<Integer> excluded = new ArrayList<Integer>();
        int col = pos % this.n;

        // c=1 in order to exclude the position itself
        for (int c = 1; c < this.n - col; c++) {
            int excludePos = c + pos;
            excluded.add(excludePos);
        }
        return excluded;
    }

    private List<Integer> excludeVertical(int pos) {
        List<Integer> excluded = new ArrayList<Integer>();
        int row = pos / this.n;

        // r=1 in order to exclude the position itself
        for (int r = 1; r < this.n - row; r++) {
            excluded.add(pos + (r * this.n));
        }
        return excluded;
    }

    private List<Integer> excludeDiagonal(int pos) {
        List<Integer> excluded = new ArrayList<Integer>();
        int col = pos % this.n;
        int row = pos / this.n;

        // Directions {row,col} upper-right and down-right
        int[][] directions = { { 1, 1 }, { -1, 1 } };

        for (int direction[] : directions) {
            int nextRow = row + direction[0];
            int nextCol = col + direction[1];
            while (nextRow < this.n && nextRow >= 0 && nextCol < this.n) {
                excluded.add((this.n * nextRow) + nextCol);
                nextRow += direction[0];
                nextCol += direction[1];
            }
        }

        return excluded;
    }

    private void addQueen(int col, int row) {
        BDD addQueen = factory.one();
        rules = rules.restrict(addQueen.andWith(factory.ithVar(row * this.n + col)));
        board[col][row] = 1;
    }

    private void addRemainingQueens() {
        for (int r = 0; r < this.n; r++) {
            for (int c = 0; c < this.n; c++) {
                if (board[c][r] == 0) {
                    addQueen(c, r);
                }
            }
        }
    }
}