import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;

public class inspectfm {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		String fmFile = args[0];
		int sampleRate = Integer.parseInt(args[1]);
		String outputFile = args[2];
		
		// Read in the FM-index
		ObjectInputStream is = new ObjectInputStream(new FileInputStream(fmFile));
		int[] sa = (int[]) is.readObject();
		String bwt = (String) is.readObject();
		int[] fmCol = (int[]) is.readObject();
		int[][] tally = (int[][]) is.readObject();
		is.close();
	
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		bw.write(fmCol[0] + "\t" + fmCol[1] + "\t" + fmCol[2] + "\t" + fmCol[3] + "\t" + fmCol[4] + "\n");
		bw.write(bwt + "\n");
		
		StringBuilder tallyA = new StringBuilder();
		StringBuilder tallyC = new StringBuilder();
		StringBuilder tallyG = new StringBuilder();
		StringBuilder tallyT = new StringBuilder();
		for(int i = 0; i < bwt.length(); i += sampleRate) {
			tallyA.append(tally[0][i] + "\t");
			tallyC.append(tally[1][i] + "\t");
			tallyG.append(tally[2][i] + "\t");
			tallyT.append(tally[3][i] + "\t");
		}
		bw.write(tallyA.toString().trim() + "\n");
		bw.write(tallyC.toString().trim() + "\n");
		bw.write(tallyG.toString().trim() + "\n");
		bw.write(tallyT.toString().trim() + "\n");
		
		bw.close();

	}

}
