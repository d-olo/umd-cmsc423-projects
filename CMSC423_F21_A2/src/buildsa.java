import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.PriorityQueue;

public class buildsa {
	
	
	public static void main(String[] args) throws IOException {
		String input = args[0];
		String output = args[1];
		
		// Read in the genome.
		BufferedReader br = new BufferedReader(new FileReader(input));
		StringBuffer genomeBuffer = new StringBuffer();
		for(String line; (line = br.readLine()) != null; ) {
			if (!line.startsWith(">")) {
				genomeBuffer.append(line.toUpperCase().strip());
			}
		}
		br.close();
		
		genomeBuffer.append("$");
		String genome = genomeBuffer.toString();
		
		PriorityQueue<Integer> saQueue = new PriorityQueue<Integer>(new SAComparator(genome));
		for(int i = 0; i < genome.length(); i++) {
			saQueue.add(i);
		}
		
		Integer[] sa = new Integer[saQueue.size()];
		int i = 0;
		while(saQueue.peek() != null) {
			sa[i] = saQueue.poll();
			i++;
		}
		
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(output));
		os.writeObject(genome);
		os.writeObject(sa);
		os.close();
	
	}

}

class SAComparator implements Comparator<Integer> {
	
	String genome;
	
	SAComparator(String genome) {
		this.genome = genome;
	}
	
	@Override
	public int compare(Integer o1, Integer o2) {
		for(int i = 0; i < Math.min((genome.length() - o2), (genome.length() - o1)); i++) {
			if (genome.charAt(i + o1) != genome.charAt(i + o2)) return genome.charAt(i + o1) - genome.charAt(i + o2);
		}
		return (genome.length() - o2) - (genome.length() - o1);
	}
	
}


