import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class inspectsa {
	
	private static int quickLCP(String genome, int offsetA, int offsetB) {
		for(int i = 0; i < Math.min(genome.length() - offsetA, genome.length() - offsetB); i++) {
			if(genome.charAt(i + offsetA) != genome.charAt(i + offsetB)) return i;
		}
		return Math.min(genome.length() - offsetA, genome.length() - offsetB);
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		String saFile = args[0];
		int sampleRate = Integer.parseInt(args[1]);
		String outputFile = args[2];
		
		// Read in SA and genome.
		ObjectInputStream is = new ObjectInputStream(new FileInputStream(saFile));
		String genome = (String) is.readObject();
		Integer[] sa = (Integer[]) is.readObject();
		is.close();
	
		// Create the LCP array, and store the sum and maximum.
		ArrayList<Integer> lcp1 = new ArrayList<Integer>();
		StringBuilder spotCheck = new StringBuilder();
		double sum = 0, median = 0;
		int max = 0;
		
		// While looping, get LCPs, sum them up, and add to spot check.
		for(int i = 0; i < sa.length; i++) {
			if(i > 0) {
				int lcp = quickLCP(genome, sa[i - 1], sa[i]);
				sum += lcp;
				if (lcp > max) max = lcp;
				lcp1.add(lcp);
			}
			
			if(i % sampleRate == 0) spotCheck.append(sa[i] + "\t");
		}
		
		double mean = sum / (double) lcp1.size();
		lcp1.sort(null);
		if(lcp1.size() % 2 == 1) 
			median = lcp1.get(lcp1.size() / 2);
		else 
			median = (lcp1.get(lcp1.size() / 2 - 1) + lcp1.get(lcp1.size() / 2))/2.0f;
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		bw.write(mean + "\n");
		bw.write(median + "\n");
		bw.write(max + "\n");
		bw.write(spotCheck.toString());
		bw.close();
	}
}
