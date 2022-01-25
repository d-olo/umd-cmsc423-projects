import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class randsim {

	public static void main(String[] args) {
		int readLength = Integer.parseInt(args[0]);
		double targetDepth = Double.parseDouble(args[1]);
		double theta = Double.parseDouble(args[2]);
		String input = args[3];
		String outputStem = args[4];
		
		StringBuffer genomeBuffer = new StringBuffer();
		
		try {
			// Open the input file and read the genome into a buffer.
			BufferedReader br = new BufferedReader(new FileReader(input));
			for(String line; (line = br.readLine()) != null; ) {
				if (!line.startsWith(">")) {
					genomeBuffer.append(line.toUpperCase().strip());
				}
			}
			// Close the input file after reading.
			br.close();
		} catch (IOException e) {
			System.out.println("Error reading " + input);
		}	
			
		String genome = genomeBuffer.toString();
		//Based on the length of the genome, calculate the number of reads.
		int numReads = (int) (targetDepth * genome.length() / readLength);
		Random rd = new Random(24601);
		// Take the number of reads needed to get the correct depth.
		int[] depth = new int[genome.length()];
		ArrayList<Integer> positions = new ArrayList<Integer>();
		try {
			BufferedWriter bw_fa = new BufferedWriter(new FileWriter(outputStem + ".fa"));
			for(int i = 0; i < numReads; i++) {
				int pos = rd.nextInt(genome.length() - readLength);
				positions.add(pos);
				String read = genome.substring(pos, pos + readLength);
				for(int j = pos; j < pos + readLength; j++) depth[j]++;
				bw_fa.write(">" + i + ":" + pos + ":" + readLength + "\n");
				bw_fa.write(read + "\n");
			} 
			bw_fa.close();
		} catch (IOException e) {
			System.out.println("Error writing to " + outputStem+".fa");
		}
		// Perform stat calculations
		double avgDepth = (numReads * readLength / (double) genome.length());
		int basesCovered = 0;
		double sumSquareDiffs = 0;
		for(int i = 0; i < depth.length; i++) {
			// Test if this base is covered
			if(depth[i] > 0) basesCovered++;
			// Get the squared difference between the depth and mean depth.
			sumSquareDiffs += Math.pow(depth[i] - avgDepth, 2);
		}
		double varDepth = sumSquareDiffs / (genome.length() - 1);

		int islandDist = (int) (readLength * theta);
		int numIslands = 1;
		positions.sort(null);
		for(int i = 1; i < positions.size(); i++) {
			int posL = positions.get(i - 1);
			int posR = positions.get(i);
			//If the end of the left read doesn't reach the right read, then this is an island.
			if(posL + readLength < posR) numIslands++;
			//If there is not sufficient overlap, there is an island.
			else if (posL + readLength - posR < islandDist) numIslands++;
		}
		
		try {
			BufferedWriter bw_st = new BufferedWriter(new FileWriter(outputStem + ".stats"));
			
			bw_st.write("num_reads\t" + numReads + "\n");
			bw_st.write("bases_covered\t" + basesCovered + "\n");
			bw_st.write("avg_depth\t" + avgDepth + "\n");
			bw_st.write("var_depth\t" + varDepth + "\n");
			bw_st.write("num_islands\t" + numIslands + "\n");
			
			bw_st.close();
		} catch (IOException e) {
			System.out.println("Error writing to " + outputStem+".stats");
		}

	}

}
