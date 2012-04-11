
package dcpu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class DcpuCompiler {
	private static final int WORD_MASK = 0x0000FFFF;
	private static final int A_SHIFT   = 4;
	private static final int B_SHIFT   = 10;
	
	
	private static final Map<String, Integer> OP_CODES;
	static {
		Map<String, Integer> rep = new HashMap<String, Integer>();
		rep.put("SET", 0x1); rep.put("ADD", 0x2);
		rep.put("SUB", 0x3); rep.put("MUL", 0x4);
		rep.put("DIV", 0x5); rep.put("MOD", 0x6);
		rep.put("SHL", 0x7); rep.put("SHR", 0x8);
		rep.put("AND", 0x9); rep.put("BOR", 0xA);
		rep.put("XOR", 0xB); rep.put("IFE", 0xC);
		rep.put("IFN", 0xD); rep.put("IFG", 0xE);
		rep.put("IFB", 0xF);
		OP_CODES = Collections.unmodifiableMap(rep);
	}
	
	private static final Map<String, Integer> VARIABLES;
	static {
		Map<String, Integer> rep = new HashMap<String, Integer>();
		rep.put("A", 0x0); rep.put("B", 0x1);
		rep.put("C", 0x2); rep.put("X", 0x3);
		rep.put("Y", 0x4); rep.put("Z", 0x5);
		rep.put("I", 0x6); rep.put("J", 0x7);
		
		rep.put("[A]", 0x8); rep.put("[B]", 0x9);
		rep.put("[C]", 0xA); rep.put("[X]", 0xB);
		rep.put("[Y]", 0xC); rep.put("[Z]", 0xD);
		rep.put("[I]", 0xE); rep.put("[J]", 0xF);
		
		rep.put("POP",  0x18); rep.put("PEEK", 0x19);
		rep.put("PUSH", 0x1A); rep.put("SP",   0x1B);
		rep.put("PC",   0x1C); rep.put("O",    0x1D);
		
		/**
		 * @todo IMPLEMENT!! :)
		 * 
		 * 0x10-0x17: [next word + register]
		 *      0x1f: next word (literal)
		 * 
		 * @info where is the need for this?!
		 * 0x20-0x3f: literal value 0x00-0x1f (literal)
		 */
		
		VARIABLES = Collections.unmodifiableMap(rep);
	}
	
	public Program compileString (String source) {
		List<Integer> bytecode = new ArrayList<Integer>();
		Map<String, Integer> lables = new HashMap<String, Integer>();
		
		// prepare source
		String[] lines = source.trim()
			.replaceAll(";[^\n]+", "")
			.replaceAll("[\t ]+", " ")
			.replaceAll("(:\\w+)\\s+", "$1 ")
			.split("\n");
		String[] line;
		String lable;
		
		// create bytecode
		int word1;
		Integer word2 = null, word3 = null;
		int pc = 0;
		for (int i = 0; i < lines.length; i += 1) {
			lines[i] = lines[i].trim();
			
			if (lines[i].startsWith(":")) {
				lable = lines[i].split(" ")[0].replace(":", "");
				if (lables.containsKey(lable)) {
					System.out.println("Duplicate lable '"+ lable +"'!");
					System.exit(1);
				}
				lables.put(lable, pc);
				lines[i] = lines[i].replace(":"+ lable +" ", "");
			}
			
			line = lines[i].split(",? ");
			
			if (line.length == 3) {
				if (!OP_CODES.containsKey(line[0])) {
					System.out.println("Unknown operation '"+ line[0] +"'!");
					System.exit(1);
				}
				word1 = OP_CODES.get(line[0]);
				
				if (VARIABLES.containsKey(line[1])) {
					word1 |= VARIABLES.get(line[1]) << A_SHIFT;
				} else if (line[1].startsWith("0x")) {
					word1 &= 0x1e << A_SHIFT;
					word2  = Integer.parseInt(line[1].replace("0x", ""), 16) & WORD_MASK;
				} else {
					System.out.println("A NOT IMPLEMENTET YET! ("+ line[1] +")");
					System.exit(1);
				}
				
				if (VARIABLES.containsKey(line[2])) {
					word1 &= VARIABLES.get(line[2]) << B_SHIFT;
				} else if (line[2].startsWith("0x")) {
					word1 |= 0x1e << B_SHIFT;
					word3  = Integer.parseInt(line[2].replace("0x", ""), 16) & WORD_MASK;
				} else if (lables.containsKey(line[2])) {
					int lblId = 0;
					Set<String> keys = lables.keySet();
					Iterator<String> it = lables.keySet().iterator();
					while (it.hasNext()) {
						if (it.next().equals(line[2])) {
							break;
						}
						lblId += 1;
					}
					word1 |= (0x20 + lblId) << B_SHIFT;
				} else {
					System.out.println("B NOT IMPLEMENTET YET! ("+ line[2] +") "+ lines[i]);
					System.exit(1);
				}
				
				pc += 1;
				bytecode.add(word1);
				if (word2 != null) {
					bytecode.add(word2.intValue());
					word2 = null;
					pc += 1;
				}
				if (word3 != null) {
					bytecode.add(word3.intValue());
					word3 = null;
					pc += 1;
				}
			} else if (line.length == 2) {
				// @todo IMPLEMENT!!! :)
				System.out.println("NOT IMPLEMENTET YET! ("+ lines[i] +")");
				System.exit(1);
			}
		}
		
		int[] bc = new int[bytecode.size()];
		for (int i = 0; i < bytecode.size(); i += 1) {
			bc[i] = bytecode.get(i).intValue();
		}
		return new Program(bc, lables.values());
		//return bc;
	}
	
	public static String readFile (String file) {
		StringBuilder sb = new StringBuilder();
		
		try {
			FileReader input = new FileReader(file);
			BufferedReader bufRead = new BufferedReader(input);
			String line;
			while ((line = bufRead.readLine()) != null){
				if (!line.isEmpty()) {
					sb.append(line);
					sb.append("\n");
				}
			}
			bufRead.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		return sb.toString();
	}
}
