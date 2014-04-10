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
        
		//final rule BDD is a conjunction of all the individual rules.
		rules = factory.one();

		//Iterate through each field on the board and apply horizontal, vertical and diagonal rules.
        for (int i = 0; i < this.n * this.n; i++) {
            rules.andWith(constructIndividualRule(i));
        }

		//add the one queen per row/column rule to the 'rules' BDD
        rules.andWith(constructExactlyNQueensRule());
    }

    public int[][] getGameBoard() {
        return board;
    }

    public boolean insertQueen(int column, int row) {

		//If queen has already been placed on the field or the field is illegal then do nothing.
        if (board[column][row] == -1 || board[column][row] == 1) {
            return true;
        }

		//If this is reached the user has clicked on a valid field. Add a queen to that field
        addQueen(column, row);
		
		//Print the number of solutions still available
        System.out.println(rules.pathCount());

		//Update the board to show invalid fields
        for (int r = 0; r < this.n; r++) {
            for (int c = 0; c < this.n; c++) {
				//If there is a queen already. Do nothing
                if (board[c][r] == 1)
                    continue;

				//If the newest insertion of a queen eliminates this field as a candidate then set this field on the board to -1 (invalid)
                if (rules.restrict(factory.ithVar((r * this.n) + c)).isZero()) {
                    board[c][r] = -1;
                }
            }
        }

		//If there is only one possible solution left. Automatically fill out the rest of the board.
        if (rules.pathCount() == 1)
            addRemainingQueens();

        return true;
    }

	//Construct the rule that a queen must exist for each row/column
    private BDD constructExactlyNQueensRule() {
		//A conjunction of rules hence we are initializing the BDD with one()
        BDD queensRule = factory.one();

        for (int col = 0; col < this.n; col++) {
			//Add single row queens rule
            queensRule.andWith(constructIndividualQueensRowRule(col));
        }

        return queensRule;
    }

	//Single row queens rule generation
    private BDD constructIndividualQueensRowRule(int col) {
        //As this rule is a disjunction (a1 | b1 | c1 | .... | h1) we use zero() as initializer. Using one() would make it true always.
		BDD singleQueenRule = factory.zero();

        for (int row = 0; row < this.n; row++) {
            singleQueenRule.orWith(factory.ithVar((row * this.n) + col));
        }

        return singleQueenRule;
    }

	//Construct and return the rules that apply to a single field on the board.
    private BDD constructIndividualRule(int pos) {
		//Conjunction
        BDD posRule = factory.one();

		//As board is made up of integers we get all the fields that are to be excluded from this current field.
        List<Integer> excluded = getExcluded(pos);

		//for each field in the excluded list add the excluded field to the expression of the field.
        for (int e : excluded) {
            posRule.andWith(factory.nithVar(e));
        }

		//Imply that assigning this field to true, all its excluded must be false
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

	//Exclude all vertically to the right of this field
    private List<Integer> excludeVertical(int pos) {
        List<Integer> excluded = new ArrayList<Integer>();
        int row = pos / this.n;

        // r=1 in order to exclude the position itself
        for (int r = 1; r < this.n - row; r++) {
            excluded.add(pos + (r * this.n));
        }
        return excluded;
    }

	// exclude all fields diagonally to the right of this field
    private List<Integer> excludeDiagonal(int pos) {
        List<Integer> excluded = new ArrayList<Integer>();
        int col = pos % this.n;
        int row = pos / this.n;

        // Directions {row,col} upper-right and down-right
        int[][] directions = { { 1, 1 }, { -1, 1 } };

		//Iterate directions and add index of excluded fields.
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

	//Add a queen to the user specified position.
    private void addQueen(int col, int row) {
        BDD addQueen = factory.one();
		//Add the restriction to the global rule that this specified field (index) has been set to true and reassign the global rule to this new restricted BDD.
        rules = rules.restrict(addQueen.andWith(factory.ithVar(row * this.n
                + col)));
		//Assign the board col row to one to indicate that a queen has been placed
        board[col][row] = 1;
    }

	//Add the remaining queens to the board if only one possible solution exists.
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