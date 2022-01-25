
public class OverlapRank implements Comparable<OverlapRank> {

	protected int score, rankL, rankR, verL, verR;
	
	public OverlapRank(int score, int rankL, int rankR, int verL, int verR) {
		this.score = score;
		this.rankL = rankL;
		this.rankR = rankR;
		this.verL = verL;
		this.verR = verR;
	}
	
	@Override
	public int compareTo(OverlapRank o) {
		if(score != o.score) return score - o.score;
		else if (rankL != o.rankL) return rankL - o.rankL;
		else return rankR - o.rankR;
	}
	
	public String toString() {
		return "{" + score + ", " + rankL + ", " + rankR + ", " + verL + ", " + verR + "}";
	}

}
