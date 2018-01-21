package enigma.engine.sorting;

public class MoveHistoryEntry {
	public int fromIndex;
	public int toIndex;
	//public boolean aiMadeMove;
	
	public MoveHistoryEntry(int fromIdx, int toIdx /*, boolean aiMadeMove */) {
		fromIndex = fromIdx;
		toIndex = toIdx;
		//this.aiMadeMove = aiMadeMove;
	}
}
