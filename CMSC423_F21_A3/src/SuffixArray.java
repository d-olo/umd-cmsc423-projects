import java.util.Comparator;
import java.util.PriorityQueue;

public class SuffixArray {
	
	
	public static int[] buildSA(String genome) {
		
		PriorityQueue<Integer> saQueue = new PriorityQueue<Integer>(new SAComparator(genome));
		for(int i = 0; i < genome.length(); i++) {
			saQueue.add(i);
		}
		
		int[] sa = new int[saQueue.size()];
		int i = 0;
		while(saQueue.peek() != null) {
			sa[i] = saQueue.poll();
			i++;
		}
		
		return sa;
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


