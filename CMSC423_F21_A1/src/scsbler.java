import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;

public class scsbler {

	private static int minOverlap;

	public static void main(String[] args) throws IOException {
		String input = args[0];
		minOverlap = Integer.parseInt(args[1]);
		String output = args[2];
		
		// Contains the strings themselves.
		ArrayList<String> reads = new ArrayList<String>();
		// A priority queue to find the best merge to make.
		PriorityQueue<OverlapRank> queue = new PriorityQueue<OverlapRank>();
		
		try {
			// Read in the input.
			BufferedReader br = new BufferedReader(new FileReader(input));

			for(String line; (line = br.readLine()) != null; ) {
				if(line.startsWith(">")) {
					// Add the first line after the sequence marker as a new entry.
					line = br.readLine();
					reads.add(line);
				}
				else {
					// This is not a new entry, so add it to the last existing line.
					String lastLine = reads.get(reads.size() - 1);
					reads.set(reads.size() - 1, lastLine + line);
				}
			}
			// Close the input file after reading.
			br.close();
		} catch (IOException e) {
			System.out.println("Error reading " + input);
		}
		ArrayList<String> origReads = new ArrayList<String>();
		for(String read : reads) {
			origReads.add(read);
		}
		

		// Strings that are not matched to any other string.
		HashSet<Integer> unmatched = new HashSet<Integer>();
		
		// Contains the version and parent data.
		int[] currVersion = new int[reads.size()];
		int[] parent = new int[reads.size()];
		for(int i = 0; i < parent.length; i++) {
			parent[i] = i;
			unmatched.add(i);
		}
		
		
		// For all slices of length minOverlap, note which strings contain them.
		HashMap<String, Set<Integer>> sliceMap = new HashMap<String, Set<Integer>>();
		for(int i = 0; i < reads.size(); i++) {
			String read = reads.get(i);
			for(int j = 0; j < read.length() - minOverlap + 1; j++) {
				String slice = read.substring(j, j + minOverlap);
				if(sliceMap.get(slice) == null) {
					sliceMap.put(slice, new HashSet<Integer>());
					sliceMap.get(slice).add(i);
				}
				else {
					sliceMap.get(slice).add(i);
				}
			}
		}
		
		// Only compare reads to other reads containing the suffix of the first read.
		for(int i = 0; i < reads.size(); i++) {
			String read = reads.get(i);
			if (read.length() < minOverlap) continue;
			else {
				String suffix = read.substring(read.length() - minOverlap);
				if(sliceMap.get(suffix) != null) {
					Iterator<Integer> slices = sliceMap.get(suffix).iterator();
					while(slices.hasNext()) {
						int index = slices.next();
						int overlap = getOverlap(read, reads.get(index));
						if(i != index && overlap != -1) {
							queue.add(new OverlapRank(-overlap, i, index, 0, 0));
							unmatched.remove(i);
							unmatched.remove(index);
						}
					}
				}
			}
		}
		
		OverlapRank or;
		while(queue.peek() != null) {
			or = queue.poll();
			//System.out.println(or);
			if(or.rankL == or.rankR) continue;
			// If the stored versions are equal to the current version, update the strings.
			if(currVersion[or.rankL] == or.verL && currVersion[or.rankR] == or.verR /*&& reads.get(or.rankR).length() >= -or.score*/) {
				// If they're already equal, then there's no need to update the version.
				if(!reads.get(or.rankL).equals(reads.get(or.rankR))) {
					reads.set(or.rankL, reads.get(or.rankL) + reads.get(or.rankR).substring(-or.score));
					currVersion[or.rankL] = currVersion[or.rankL] + 1;
				}
				reads.set(or.rankR, "");
				currVersion[or.rankR] = -1;
				parent[or.rankR] = or.rankL;
				
				// Check if the unmatched strings match the new read, and if so put them in the queue.
				for(int index : unmatched) {
					int overlapL = getOverlap(reads.get(or.rankL), reads.get(index));
					int overlapR = getOverlap(reads.get(index), reads.get(or.rankL));
					if(overlapL != -1) {
						queue.add(new OverlapRank(-overlapL, or.rankL, index, currVersion[or.rankL], 0));
						unmatched.remove(index);
					}
					if(overlapR != -1) {
						queue.add(new OverlapRank(-overlapR, index, or.rankL, currVersion[or.rankL], 0));
						unmatched.remove(index);
					}
				}
			}
			else {
				// Find the highest level parents of the strings.
				int parentL = or.rankL;
				while(parent[parentL] != parentL) {
					parentL = parent[parentL];
				}
				int parentR = or.rankR;
				while(parent[parentR] != parentR) {
					parentR = parent[parentR];
				}
				// Recalculate overlap for the parents, if it's at least as good as the recorded score, merge.
				if (parentL == parentR) continue;
				int overlap = getOverlap(reads.get(parentL), reads.get(parentR));
				if (-overlap <= or.score/* && reads.get(parentR).length() >= overlap*/) {
					if(!reads.get(parentL).equals(reads.get(parentR))) {
						reads.set(parentL, reads.get(parentL) + reads.get(parentR).substring(overlap));
						currVersion[parentL] = currVersion[parentL] + 1;
					}
					reads.set(parentR, "");
					currVersion[parentR] = -1;
					parent[parentR] = parentL;
					
					for(int index : unmatched) {
						int overlapL = getOverlap(reads.get(parentL), reads.get(index));
						int overlapR = getOverlap(reads.get(index), reads.get(parentL));
						if(overlapL != -1) {
							queue.add(new OverlapRank(-overlapL, parentL, index, currVersion[parentL], 0));
							unmatched.remove(index);
						}
						if(overlapR != -1) {
							queue.add(new OverlapRank(-overlapR, index, parentL, currVersion[parentL], 0));
							unmatched.remove(index);
						}
					}
				}
				else {
					//If the overlap is large enough to be considered, re-add it to the queue.
					if (overlap >= minOverlap) {
						queue.add(new OverlapRank(-overlap, parentL, parentR, currVersion[parentL], currVersion[parentR]));
					}
				}
			}
		}
		
		for(int i = 0; i < reads.size(); i++) {
			//if(unmatched.contains(i)) 
				//System.out.println("Read " + i + " (" + reads.get(i) + ") was not visited");
		}
		
		//TODO orig reads, test if all orig reads are in final reads, other way around too
		for(String origRead : origReads) {
			boolean isContained = false;
			for(String read : reads) {
				if (read.contains(origRead)) isContained = true;
			}
			if(isContained) {
				//System.out.println("Read " + origRead + " is included in a final read.");
			}
			else {
				System.out.println("Read " + origRead + " is not included in a final read.");
			}
		}
		
		//some reads are never checked because they don't initially match.
		//how can we add them to the queue? how can we check if they match a new string without rechecking everything?
		//we can check against the final strings easily, but can we check after each string is updated?
		//build a list of unmatched reads. after a merge, check if the unmatched reads would match the new string.
		/*int id2 = 0;
		for(int i = 0; i < reads.size(); i++) {
			if(!reads.get(i).equals("") && minOverlap == 40) {
				System.out.println(">" + id2 + ":" + reads.get(i).length());
				System.out.println(reads.get(i));
				id2++;
			}
		}*/
		
		/*for(int i = 0; i < origReads.size(); i++) {
			System.out.println(">" + i + ":" + origReads.get(i).length());
			System.out.println(origReads.get(i));
		}*/
		
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(output));
			int id = 0;
			for(int i = 0; i < reads.size(); i++) {
				if(!reads.get(i).equals("")) {
					bw.write(">" + id + ":" + reads.get(i).length() + "\n");
					bw.write(reads.get(i) + "\n");
					id++;
				}
			}
			bw.close();
		} catch (IOException e) {
			System.out.println("Error writing to " + output);
		}
	}
		
	/**
	 * Checks the suffix of the left string against the prefix of the right string.
	 * @param left The string whose suffix to compare.
	 * @param right The string whose prefix to compare.
	 * @param minOverlap The required minimum overlap.
	 * @return The number of overlapping characters, if it is at least equal to the minimum, and -1 otherwise.
	 */
	private static int getOverlap(String left, String right) {
		int overlap = -1;
		for(int i = left.length() - minOverlap; i > -1; i--) {
			if(right.startsWith(left.substring(i))) {
				overlap = left.substring(i).length();
			}
		}
		return overlap;
	}
	
}
