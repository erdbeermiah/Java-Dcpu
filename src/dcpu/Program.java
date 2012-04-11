

package dcpu;

import java.util.Collection;
import java.util.Iterator;

public class Program {
	private static final int MEMORY_SIZE   = 65536;
	private static final int NUM_ITERALS   = 32;
	
	public int[] lit = new int[NUM_ITERALS];
	public int[] memory = new int[MEMORY_SIZE];
	
	public Program (int[] prog, Collection<Integer> literals) {
		for (int i = 0; i < prog.length; i += 1) {
			memory[i] = prog[i];
		}
		
		Iterator<Integer> it = literals.iterator();
		for (int i = 0; i < literals.size(); i += 1) {
			lit[i] = it.next();
		}
	}
}
