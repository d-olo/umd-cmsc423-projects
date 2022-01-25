import java.util.ArrayList;

/**
 * Stores a query.
 * @author Dionel Olo
 *
 */
public class QueryRecord {
	
	// The genome and SA are the same for all queries.
	private static String genome, method;
	private static Integer[] sa;

	public static void setGenome(String str) {
		genome = str;
	}
	
	public static void setSA(Integer[] arr) {
		sa = arr;
	}
	
	public static void setMethod(String str) {
		method = str;
	}
	
	// Instance variables
	private String queryName;
	private String query;
	private ArrayList<Integer> matches = new ArrayList<Integer>();
	
	public QueryRecord(String name, String query) {
		this.queryName = name;
		this.query = query;
	}
	
	@Override
	/**
	 * Returns the string to be written to the output file.
	 */
	public String toString() {
		StringBuilder str = new StringBuilder(queryName + "\t" + matches.size() + "\t");
		for(Integer match : matches) {
			str.append(match + "\t");
		}
		return str.toString().strip();
	}
	
	/**
	 * Finds the matches for this query.
	 * @param method The solving method.
	 */
	public void solve() {
		if(method.equals("naive")) solveNaive();
		else solveAccelerated();
	}
	
	/**
	 * Character-based comparison between query and genome.
	 * @param offset The offset to search in the genome.
	 * @return 0 if equal, and the difference between the first non-matching characters if not.
	 */
	private int quickCompare(int offset) {
		return quickCompare(offset, 0);
	}
	
	/**
	 * Character-based comparison between query and genome.
	 * @param offset The offset to search in the genome.
	 * @param lcp The least common prefix between the query and genome at offset.
	 * @return 0 if equal, and the difference between the first non-matching characters if not.
	 */
	private int quickCompare(int offset, int lcp) {
		for(int i = lcp; i < Math.min(query.length(), genome.length() - offset); i++) {
			if (query.charAt(i) != genome.charAt(i + offset)) 
				return query.charAt(i) - genome.charAt(i + offset);
		}
		return 0;
	}
	
	/**
	 * Returns the length of the least common prefix between the query and genome at some offset.
	 * @param offset The offset to search.
	 * @return Length of the LCP
	 */
	private int quickLCP(int offset) {
		for(int i = 0; i < Math.min(query.length(), genome.length() - offset); i++) {
			if (query.charAt(i) != genome.charAt(i + offset)) return i;
		}
		return Math.min(query.length(), genome.length() - offset);
	}
	
	/**
	 * Finds matches using a naive binary search.
	 */
	public void solveNaive() {
		int left = 0, right = sa.length, mid = -1;
		while(left < right) {
			mid = (left + right) / 2;
			if (quickCompare(sa[mid]) > 0) {
				left = mid + 1;
			}
			else {
				right = mid;
			}
		}
		// Roll down until no longer matching.
		while(quickCompare(sa[left]) == 0) {
			matches.add(sa[left]);
			left++;
		}
	}
	
	/**
	 * Finds matches using a binary search, skipping some iterations using LCP.
	 */
	public void solveAccelerated() {
		int left = 0, right = sa.length - 1, mid = -1;
		int lcpLQ = quickLCP(sa[left]);
		int lcpRQ = quickLCP(sa[right]);
		while(left < right) {
			mid = (left + right) / 2;
			if (quickCompare(sa[mid], Math.min(lcpLQ, lcpRQ)) > 0) {
				left = mid + 1;
				lcpLQ = quickLCP(sa[left]);
			}
			else {
				right = mid;
				lcpRQ = quickLCP(sa[right]);
			}
		}
		// Roll down until no longer matching.
		while(quickCompare(sa[left]) == 0) {
			matches.add(sa[left]);
			left++;
		}
	}

}
