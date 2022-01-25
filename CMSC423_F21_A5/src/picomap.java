import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;

public class picomap {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		String indexFile = args[0];
		String readFile = args[1];
		int mismatchPenalty = -Integer.parseInt(args[2]);
		int gapPenalty = -Integer.parseInt(args[3]);
		String outputFile = args[4];
		HashSet<String> solved = new HashSet<String>();
		
		//read the index
		//deserialize the string
		ObjectInputStream reader = new ObjectInputStream(new FileInputStream(indexFile));
		String genome  = (String) reader.readObject();
		int[] sa = (int[]) reader.readObject();

		// Get reads, populate the problem records.
		BufferedReader br = new BufferedReader(new FileReader(readFile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		ProblemRecord.setRefString(genome);
		
		String probName = br.readLine();
		String x = br.readLine();
			
		while (probName != null) {
			if(!solved.contains(probName)) {
				ProblemRecord prob = new ProblemRecord(probName.substring(1), x.toString(), gapPenalty, mismatchPenalty);
				prob.solve(sa);
				bw.write(prob.toString());
				prob = null;
				solved.add(probName);
			}
			probName =  br.readLine();
			x = br.readLine();
		}

					
		br.close();
		bw.close();
		reader.close();
		
	}

}
