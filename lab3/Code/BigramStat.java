import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.io.FileWriter;
/**
 *  @author  Feichao (feqian@ucsd.edu)
 *  <p>
 *  At the conclusion of its exectution, it should output 
 *  three pieces of information, one per line: 
 *  (1) the total number of bigrams, 
 *  (2) the most common bigram, and 
 *  (3) the number of bigrams required to add up to 10% of all bigrams.
 */

public class BigramStat {
	public static void main(String[] args) {
		String file = "../Data/result.txt";	
		int total = 0;
	    Path path = Paths.get(file);
	    List<String> lines = null;
	    try {
			 lines = Files.readAllLines(path, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<Bigrams> bgm = new ArrayList<Bigrams>();
		Bigrams tmp = null;
		for (String s : lines) {
			tmp = new Bigrams(s);
			bgm.add(tmp);
			total += tmp.count;
		}
		Collections.sort(bgm);
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			for(Bigrams b: bgm) {
			  writer.write(b.to_string() + "\n");
			}
			writer.close();		
		} catch (IOException e) {
			e.printStackTrace();
		}

		Bigrams most = bgm.get(0);
		System.out.println("The total number of bigrams: " + total);
		System.out.println("The most common bigram: (" + most.first + ", " + most.second + ")");
		System.out.println("The number of bigrams required to add up to 10% of all bigrams: " + getAdd(bgm, total));
	}	
	
	public static int getAdd(List<Bigrams> bgm, int total) {
		int res = 0, count = 0;
		for (Bigrams b : bgm) {
			count += b.count;
			++res;
			if (10 * count >= total) {
				break;
			}
		}
		return res;
	}
	public static class Bigrams implements Comparable<Bigrams>{
		int count;
		String first;
		String second;
		private Scanner sc;
		public Bigrams(String line) {
			sc = new Scanner(line);
			this.first = sc.next();
			this.second = sc.next();
			this.count = sc.nextInt();
		}
		public int compareTo(Bigrams tmp) {
			//descending order
			return tmp.count - this.count;
		}
		public String to_string() {
			return first + " " + second + " " + count;
		}
	}
}