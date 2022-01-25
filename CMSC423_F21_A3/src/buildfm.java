import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.PriorityQueue;

public class buildfm {
	
	//Contains the suffix array
	private static int[] sa;
	//Contains a total number of each character, because the string is just the chars in order.
	private static int[] fmCol;
	//The Burrows-Wheeler transform.
	private static String bwt;
	//Contains a running tally of A, C, G, and T in 0, 1, 2, and 3.
	private static int[][] tally;
	
	private static void buildFMIndex(String genome) {
		
		fmCol = new int[5];
		tally = new int[4][genome.length()];
		
		// Given the suffix array and genome, we can build the FM-index.
		StringBuilder bwtBuilder = new StringBuilder();
		int[] tallyCounter = new int[4];
		for(int i = 0; i < sa.length; i++) {
			if(sa[i] == 0) bwtBuilder.append(genome.charAt(genome.length() - 1));
			else bwtBuilder.append(genome.charAt(sa[i] - 1));
			
			if(genome.charAt(sa[i]) == 'A') {
				fmCol[1]++;
			}
			else if(genome.charAt(sa[i]) == 'C') {
				fmCol[2]++;
			}
			else if(genome.charAt(sa[i]) == 'G') {
				fmCol[3]++;
			}
			else if(genome.charAt(sa[i]) == 'T') {
				fmCol[4]++;
			}
			else {
				fmCol[0]++;
			}
			if(sa[i] == 0) {
				if(genome.charAt(genome.length() - 1) == 'A') {
					tallyCounter[0]++;
				}
				else if(genome.charAt(genome.length() - 1) == 'C') {
					tallyCounter[1]++;
				}
				else if(genome.charAt(genome.length() - 1) == 'G') {
					tallyCounter[2]++;
				}
				else if(genome.charAt(genome.length() - 1) == 'T') {
					tallyCounter[3]++;
				}
			}
			else {
				if(genome.charAt(sa[i] - 1) == 'A') {
					tallyCounter[0]++;
				}
				else if(genome.charAt(sa[i] - 1) == 'C') {
					tallyCounter[1]++;
				}
				else if(genome.charAt(sa[i] - 1) == 'G') {
					tallyCounter[2]++;
				}
				else if(genome.charAt(sa[i] - 1) == 'T') {
					tallyCounter[3]++;
				}
			}
			
			tally[0][i] = tallyCounter[0];
			tally[1][i] = tallyCounter[1];
			tally[2][i] = tallyCounter[2];
			tally[3][i] = tallyCounter[3];
		}
		
		
		bwt = bwtBuilder.toString();
	}
	
	public static void main(String[] args) throws IOException{
		String input = args[0];
		String output = args[1];

		StringBuilder genome_buf = new StringBuilder();

		BufferedReader br = new BufferedReader(new FileReader(input));
		for(String line; (line = br.readLine()) != null; ) {
			if (!line.startsWith(">")) {
				genome_buf.append(line.toUpperCase().strip());
			}
		}
		br.close();
		
		genome_buf.append("$");
		String genome = genome_buf.toString();
		sa = SuffixArray.buildSA(genome);
		
		//Build the index based on the genome.
		buildFMIndex(genome);

		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(output));
		out.writeObject(sa);
		out.writeObject(bwt);
		out.writeObject(fmCol);
		out.writeObject(tally);
		out.close();
		
	}
}

	
