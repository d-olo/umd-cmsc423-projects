import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class saligner {

	public static void main(String[] args) {
		
		if (args.length != 5) {
			System.out.println("Improper input.");
			return;
		}
		
		String inputFile = args[0];
		String method = args[1];
		int mismatchPenalty = -Integer.parseInt(args[2]);
		int gapPenalty = -Integer.parseInt(args[3]);
		String outputFile = args[4];
		
		// read in each of the problem statements
		// calculate the solution
		// write to output
		
		// Read in the problem records into a list.
		ArrayList<ProblemRecord> probs = new ArrayList<ProblemRecord>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			String prob = br.readLine(), x = br.readLine(), y = br.readLine();
			
			while (prob != null) {
				probs.add(new ProblemRecord(prob, x, y));
				prob = br.readLine();
				x = br.readLine();
				y = br.readLine();
			}
			
			for (int i = 0; i < probs.size(); i++) {
				probs.get(i).solve(method, gapPenalty, mismatchPenalty);
				bw.write(probs.get(i).toString());
				bw.write("\n");
			}
			
			br.close();
			bw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}


/***
 * Stores an alignment problem.
 * @author Dionel Olo
 *
 */
class ProblemRecord {
	private String problemName;
	private String X;
	private String Y;
	private int score = 0;
	private int yStart = 0;
	private int yEnd = 0;
	private ArrayList<CigarPair> cigar = new ArrayList<CigarPair>();
	private String cigarStr = "";
	
	private class CigarPair {
		int count;
		char operation;
		
		private CigarPair(int count, char op) {
			this.count = count;
			this.operation = op;
		}
	}
	
	protected ProblemRecord(String problemName, String X, String Y) {
		this.problemName = problemName;
		this.X = X;
		this.Y = Y;
	}
	
	public String toString() {
		return problemName + "\n" + X + "\n" + Y + "\n" + score + "\t" + yStart + "\t" + yEnd + "\t" + cigarStr;
	}
	
	public void solve(String method, int gapPenalty, int mismatchPenalty) {
		if (method.equals("global")) {
			yEnd = Y.length();
			//Build the matrix.
			int[][] opts = new int[Y.length() + 1][X.length() + 1];
			
			//Base cases
			for (int i = 0; i < X.length() + 1; i++) {
				opts[0][i] = i * gapPenalty;
			}
			for (int j = 0; j < Y.length() + 1; j++) {
				opts[j][0] = j * gapPenalty;
			}
			
			
			for (int j = 1; j < Y.length() + 1; j++) {
				for (int i = 1; i < X.length() + 1; i++) {
					//Recursive case
					//get the three cases
					int match = 0, gapX = 0, gapY = 0;
						
					if(X.charAt(i - 1) == Y.charAt(j - 1)) {
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
			
			//Now that we have the matrix, find the optimal alignment, starting at the score.
			score = opts[Y.length()][X.length()];
			int i = X.length(), j = Y.length();
			while (i > 0 && j > 0) {
				if (opts[j][i] == opts[j - 1][i] + gapPenalty) {
					//Gap in X
					if (cigar.size() > 0 && cigar.get(0).operation == 'D')
						cigar.get(0).count++;
					else
						cigar.add(0, new CigarPair(1, 'D'));
					j--;
				} 
				else if (opts[j][i] == opts[j][i - 1] + gapPenalty) {
					//Gap in Y
					if (cigar.size() > 0 && cigar.get(0).operation == 'I')
						cigar.get(0).count++;
					else
						cigar.add(0, new CigarPair(1, 'I'));
					i--;
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
			
			//Whichever of i or j is not 0, reduce it to 0.
			if(i > 0) {
				while (i > 0) {
					if (cigar.size() > 0 && cigar.get(0).operation == 'I')
						cigar.get(0).count++;
					else
						cigar.add(0, new CigarPair(1, 'I'));
					i--;
				}
			}
			else if (j > 0) {
				while (j > 0) {
					if (cigar.size() > 0 && cigar.get(0).operation == 'D')
						cigar.get(0).count++;
					else
						cigar.add(0, new CigarPair(1, 'D'));
					j--;
				}
			}

			for (int k = 0; k < cigar.size(); k++) {
				cigarStr += Integer.toString(cigar.get(k).count) + cigar.get(k).operation;
			}
		}
		
		
		else if (method.equals("fitting")) {
			
			//Build the matrix.
			int[][] opts = new int[Y.length() + 1][X.length() + 1];
			
			//Base cases
			for (int i = 0; i < X.length() + 1; i++) {
				opts[0][i] = i * gapPenalty;
			}
			//With X as the sliding string, we want to allow gaps before X.
			for (int j = 1; j < Y.length() + 1; j++) {
				opts[j][0] = 0;
			}
			
			for (int j = 1; j < Y.length() + 1; j++) {
				for (int i = 1; i < X.length() + 1; i++) {
					//Recursive case
					//get the three cases
					int match = 0, gapX = 0, gapY = 0;
						
					if(X.charAt(i - 1) == Y.charAt(j - 1)) {
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

			// Search for the best score among all positions on the last column.
			int i = X.length(), j = 0, maxVal = Integer.MIN_VALUE;
			for(int k = 0; k < Y.length() + 1; k++) {
				if (opts[k][i] > maxVal) {
					maxVal = opts[k][i];
					score = maxVal;
					j = k;
				}
			}
			
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
			
			yStart = j;
			//Calculate the endpoint.
			yEnd = yStart;
			
			for (int k = 0; k < cigar.size(); k++) {
				//The length of X is the number of all non-insertion ops.
				if(cigar.get(k).operation != 'I')
					yEnd += cigar.get(k).count;
				cigarStr += Integer.toString(cigar.get(k).count) + cigar.get(k).operation;
			}
		}
	}
}
