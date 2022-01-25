import java.util.ArrayList;

/**
 * Stores a query.
 * @author Dionel Olo
 *
 */
public class QueryRecord {
	
	// The genome and SA are the same for all queries.
	private static String bwt, method;
	private static int[] sa, fmCol;
	private static int[][] tally;
	
	public static void setBWT(String str) {
		bwt = str;
	}
	
	public static void setMethod(String str) {
		method = str;
	}
	
	public static void setSA(int[] arr) {
		sa = arr;
	}
	
	public static void setFMCol(int[] arr) {
		fmCol = arr;
	}
	
	public static void setTally(int[][] arr) {
		tally = arr;
	}
	
	
	// Instance variables
	private String queryName;
	private String query;
	private ArrayList<Integer> matches = new ArrayList<Integer>();
	private int matchLength;
	
	public QueryRecord(String name, String query) {
		this.queryName = name;
		this.query = query;
	}
	
	@Override
	/**
	 * Returns the string to be written to the output file.
	 */
	public String toString() {
		StringBuilder str = new StringBuilder(queryName + "\t" + matchLength + "\t" + matches.size() + "\t");
		for(Integer match : matches) {
			str.append(match + "\t");
		}
		return str.toString().strip();
	}
	
	/**
	 * Takes a character and returns its corresponding tally index.
	 * @param nucleotide
	 * @return
	 */
	private int charToIndex(char nucleotide) {
		switch(nucleotide) {
		case 'A': return 0;
		case 'C': return 1;
		case 'G': return 2;
		case 'T': return 3;
		default: return -1;
		}
			
	}
	
	/**
	 * Finds the matches for this query.
	 * @param method The solving method.
	 */
	public void solve() {
		int left = -1, right = -1;
		//Find the initial range of occurrences of the last char of the query.
		//left and right are both inclusive.
		char queryEnd = query.charAt(query.length() - 1);
		if(queryEnd == 'A') {
			left = fmCol[0];
			right = left + fmCol[1] - 1;
		}
		else if(queryEnd == 'C') {
			left = fmCol[0] + fmCol[1];
			right = left + fmCol[2] - 1;
		}
		else if(queryEnd == 'G') {
			left = fmCol[0] + fmCol[1] + fmCol[2];
			right = left + fmCol[3] - 1;
		}
		else if(queryEnd == 'T') {
			left = fmCol[0] + fmCol[1] + fmCol[2] + fmCol[3];
			right = left + fmCol[4] - 1;
		}

		matchLength = 1;
		// After the initial range is set, narrow the range by searching for the previous character in the query.
		for(int i = query.length() - 2; i >= 0; i--) {
			int tallyIndex = charToIndex(query.charAt(i));
			int probe = left; // Don't update left until we've checked if a match exists.
			// Instead, test where the match is, then update left afterwards.
			
			//Find the first and last occurrences of the character within the range, and shrink the range.
			while(tally[tallyIndex][probe - 1] == tally[tallyIndex][probe]) probe++;
			
			// If left ever passes right, then it was unable to find a match within the range.
			if(probe > right) {
				// If looking for complete searches, update left so no match will be added.
				// If not, leave left where it is.
				if(method.equals("complete")) {
					matchLength = 0;
					left = probe;
				}
				break;
			}
			else left = probe;
			
			while(tally[tallyIndex][right - 1] == tally[tallyIndex][right]) right--;
			
			// Now, we have to find the corresponding characters in fmCol.
			// fmCol is sorted, so we can skip over all preceding characters.
			int tallyLeft = tally[tallyIndex][left] - 1;
			int tallyRight = tally[tallyIndex][right] - 1;
			
			if(tallyIndex == 0) {
				left = fmCol[0] + tallyLeft;
				right = fmCol[0] + tallyRight;
			}
			else if(tallyIndex == 1) {
				left = fmCol[0] + fmCol[1] + tallyLeft;
				right = fmCol[0] + fmCol[1] + tallyRight;
			}
			else if(tallyIndex == 2) {
				left = fmCol[0] + fmCol[1] + fmCol[2] + tallyLeft;
				right = fmCol[0] + fmCol[1] + fmCol[2] + tallyRight;
			}
			else if(tallyIndex == 3) {
				left = fmCol[0] + fmCol[1] + fmCol[2] + fmCol[3] + tallyLeft;
				right = fmCol[0] + fmCol[1] + fmCol[2] + fmCol[3] + tallyRight;
			}
			
			matchLength++;
		}
		
		for(int i = left; i <= right; i++) {
			matches.add(sa[i]);
		}
	}
	
}
