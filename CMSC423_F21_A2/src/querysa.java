import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class querysa {

	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		String saFile = args[0];
		String queryFile = args[1];
		String queryMode = args[2];
		String outputFile = args[3];
		
		// Read in SA and genome
		ObjectInputStream is = new ObjectInputStream(new FileInputStream(saFile));
		String genome = (String) is.readObject();
		Integer[] sa = (Integer[]) is.readObject();
		is.close();
		
		QueryRecord.setGenome(genome);
		QueryRecord.setSA(sa);
		QueryRecord.setMethod(queryMode);
		
		BufferedReader br = new BufferedReader(new FileReader(queryFile));
		ArrayList<String> queryNames = new ArrayList<String>();
		ArrayList<String> queryText = new ArrayList<String>();
		
		for(String line; (line = br.readLine()) != null; ) {
			if(line.startsWith(">")) {
				queryNames.add(line.substring(1));
				queryText.add(br.readLine());
			}
			else queryText.set(queryText.size() - 1, queryText.get(queryText.size() - 1) + line);
		}
		// Close the input file after reading.
		br.close();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			for(int i = 0; i < queryNames.size(); i++) {
			// Instantiate a QueryRecord
			QueryRecord qr = new QueryRecord(queryNames.get(i), queryText.get(i));
			// Solve it
			qr.solve();
			// Write its string to the file.
			bw.write(qr.toString() + "\n");
		}
		bw.close();
	}

}
