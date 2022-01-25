import java.util.ArrayList;
import java.util.HashSet;

public class ProblemRecord{
	private static String Y;
	private String problemName, X;
	private int maxScore = Integer.MIN_VALUE, gapPenalty, mismatchPenalty;
	private ArrayList<CigarPair> cigar = new ArrayList<CigarPair>();
	private int[][] opts;
	private HashSet<Integer> seeds = new HashSet<Integer>();
	private ArrayList<Alignment> alignments = new ArrayList<Alignment>();
	
	private class CigarPair {
		int count;
		char operation;
		
		private CigarPair(int count, char op) {
			this.count = count;
			this.operation = op;
		}
	}
	
	private class Alignment{
		int index;
		int score;
		String cigar;
		
		private Alignment(int index, int score, String cigar) {
			this.index = index;
			this.score = score;
			this.cigar = cigar;
		}
		
		public boolean equals(Object o) {
			Alignment oa = (Alignment) o;
			return (index == oa.index && score == oa.score);
		}
		
	}
	
	public static void setRefString(String y) {
		Y = y;
	}
	
	protected ProblemRecord(String problemName, String X, int gapPenalty, int mismatchPenalty) {
		this.problemName = problemName;
		this.X = X;
		this.gapPenalty = gapPenalty;
		this.mismatchPenalty = mismatchPenalty;
	}
	
	public String toString() {
		String str =  problemName + "\t" + alignments.size() + "\n";
		for(int i = 0; i < alignments.size(); i++) {
			str += alignments.get(i).index + "\t" + alignments.get(i).score + "\t" + alignments.get(i).cigar + "\n";
		}
		return str;
	}
	
	/**
	 * Character-based comparison between a string and the genome.
	 * @param offset The offset to search in Y.
	 * @return 0 if equal, and the difference between the first non-matching characters if not.
	 */
	private int quickCompare(String str, int offset) {
		int i;
		for(i = 0; i < Math.min(str.length(), Y.length() - offset); i++) {
			if (str.charAt(i) != Y.charAt(i + offset)) 
				return str.charAt(i) - Y.charAt(i + offset);
		}
		return 0;
	}
	
	
	/**
	 * Gets candidate seeds provided a suffix array of string Y
	 * @param sa The suffix array of the index string
	 * @return The best matching candidate.
	 */
	private void genSeeds(int[] sa) {
		//We know reads are of length 100.
		String[] strs = new String[5];
		strs[0] = X.substring(0,20);
		strs[1] = X.substring(20,40);
		strs[2] = X.substring(40,60);
		strs[3] = X.substring(60,80);
		strs[4] = X.substring(80,100);
		
		// For each substring of the read, search separately, and modify its index to account for distance.
		for(int i = 0; i < 5; i++){
			int left = 0, right = sa.length, mid = -1;
			while(left < right) {
				mid = (left + right) / 2;
				if (quickCompare(strs[i], sa[mid]) > 0) {
					left = mid + 1;
				}
				else {
					right = mid;
				}
			}
			// Always add at least one match, in case it's not exact.
			seeds.add(sa[left] - i * 20);
			// If the match is exact, add all exact matches.
			while(left < sa.length && quickCompare(strs[i], sa[left]) == 0) {
				seeds.add(sa[left] - i * 20);
				left++;
			}
		}
	}
	
	private String genFittingMatrix(int start) {
		String Ysub = "";
		if (start < 15) {
			Ysub = Y.substring(0, start + X.length() + 15);
		}
		else if (start + X.length() + 15 > Y.length()) {
			Ysub = Y.substring(start - 15, Y.length());
		}
		else {
			Ysub = Y.substring(start - 15, start + X.length() + 15);
		}
		//Build the matrix.
		opts = new int[Ysub.length() + 1][X.length() + 1];
		
		//Base cases
		for (int i = 0; i < X.length() + 1; i++) {
			opts[0][i] = i * gapPenalty;
		}
		//With X as the sliding string, we want to allow gaps before X.
		for (int j = 1; j < Ysub.length() + 1; j++) {
			opts[j][0] = 0;
		}
		
		for (int j = 1; j < Ysub.length() + 1; j++) {
			for (int i = 1; i < X.length() + 1; i++) {
				//Recursive case
				//get the three cases
				int match = 0, gapX = 0, gapY = 0;
					
				if(X.charAt(i - 1) == Ysub.charAt(j - 1)) {
					match = opts[j - 1][i - 1];
				}
				else {
					match = mismatchPenalty +  opts[j - 1][i - 1];
				}
					
				gapX = gapPenalty + opts[j - 1][i];
				gapY = gapPenalty + opts[j][i - 1];
					
				// Maximize the score by minimizing the penalties.
				opts[j][i] = Math.max(Math.max(gapX, gapY), match);
			}
		}
		return Ysub;
	}
	
	public void solve(int[] sa) {
		// To begin solving, generate the seeds from the read.
		genSeeds(sa);
		
		for(int seed: seeds) {
			
			// If this seed is too close to either end of the string, it cannot be valid.
			if (seed + 100 > Y.length() || seed < 0) continue;
			
			//Check if the read exactly matches the substring starting from this seed.
			//If so, then there were no errors in the read, and we can skip the calculation for this seed.
			if(X.equals(Y.substring(seed, seed + 100))) {
				if(maxScore < 0) alignments.clear();
				Alignment alignment = new Alignment(seed, 0, X.length() + "=");
				if(!alignments.contains(alignment)) alignments.add(alignment);
				maxScore = 0;
				continue;
			}
			
			String Ysub = genFittingMatrix(seed);
			String cigarStr = "";
			cigar.clear();
			
			int seedScore = Integer.MIN_VALUE;
			//Now that we have the matrix, find the optimal alignment, starting at the highest score.
			int i = X.length(), j = 0, maxVal = Integer.MIN_VALUE;
			for(int k = 0; k < Ysub.length() + 1; k++) {
				if (opts[k][i] > maxVal) {
					maxVal = opts[k][i];
					seedScore = maxVal;
					j = k;
				}
			}
			
			if (seedScore < maxScore) continue; // Don't calculate if this score isn't optimal.
			
			while (i > 0 && j > 0) {
				if (opts[j][i] == opts[j][i - 1] + gapPenalty) {
					//Gap in Y
					if (cigar.size() > 0 && cigar.get(0).operation == 'I')
						cigar.get(0).count++;
					else
						cigar.add(0, new CigarPair(1, 'I'));
					i--;
				}
				else if (opts[j][i] == opts[j - 1][i] + gapPenalty) {
					//Gap in X
					if (cigar.size() > 0 && cigar.get(0).operation == 'D')
						cigar.get(0).count++;
					else
						cigar.add(0, new CigarPair(1, 'D'));
					j--;
				} 
				else if (opts[j][i] == opts[j - 1][i - 1] + mismatchPenalty) {
					//Mismatch
					if (cigar.size() > 0 && cigar.get(0).operation == 'X')
						cigar.get(0).count++;
					else
						cigar.add(0, new CigarPair(1, 'X'));
					i--;
					j--;
				}
				else if (opts[j][i] == opts[j - 1][i - 1]) {
					//Match
					if (cigar.size() > 0 && cigar.get(0).operation == '=')
						cigar.get(0).count++;
					else
						cigar.add(0, new CigarPair(1, '='));
					i--;
					j--;
				}
				
			}
			
			//If i is over 0, bring it down to 0.
			if(i > 0) {
				while (i > 0) {
					if (cigar.size() > 0 && cigar.get(0).operation == 'I')
						cigar.get(0).count++;
					else
						cigar.add(0, new CigarPair(1, 'I'));
					i--;
				}
			}
			
			for (int k = 0; k < cigar.size(); k++) {
				cigarStr += Integer.toString(cigar.get(k).count) + cigar.get(k).operation;
			}
			
			// If the score for this seed is greater than the previous optimal score, recalculate.
			if (seedScore > maxScore) {
				maxScore = seedScore;
				// Since this is now the largest score, clear any recorded alignments.
				alignments.clear();
			}
			Alignment alignment = new Alignment(j + seed - 15, seedScore, cigarStr);
			if(!alignments.contains(alignment)) alignments.add(alignment);
		}
		
		// Clear the matrix when finished, because this is using object-oriented methodology, and will take up space otherwise.
		opts = null;
	}
}