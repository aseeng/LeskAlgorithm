package LeskAlgorithm;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;

public class Algorithm {

	public ArrayList<String> context = new ArrayList<>();
	public ArrayList<String> word = new ArrayList<>();
	public ArrayList<IndexWord> senses = new ArrayList<>();
	
	public Algorithm() {

		try {

			read("asset\\sentences.txt");
			JWNL.initialize(new FileInputStream("asset\\file-properties.xml"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// legge il file con le frasi e le inserisce in una lista
	public void read(String path) throws IOException {

		BufferedReader buf;

		try {

			buf = new BufferedReader(new FileReader(path));
			String lineJustFetched = null;

			while (true) {
				lineJustFetched = buf.readLine();
				if (lineJustFetched == null)
					break;
				else if (lineJustFetched.contains("-")) {
					String line = lineJustFetched.replace("- ", "");
					context.add(line);
				}
			}
			buf.close();

			Pattern pattern = Pattern.compile("[*]{2}(.*?)[*]{2}");

			for (String string : context) {
				Matcher matcher = pattern.matcher(string);
				if (matcher.find() && !word.contains(matcher.group(1).toLowerCase()))
					word.add(matcher.group(1).toLowerCase());
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public void findSense() {
		
		try {
			
			List<POS> pos = new ArrayList<POS>();
			pos = POS.getAllPOS();
			
			for (String w : word) {
				for (POS p : pos) {

					IndexWord sense = Dictionary.getInstance().getIndexWord(p, w);
					if(!sense.equals(null))
						senses.add(sense);
				}
			}
		} catch (JWNLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String leskAlgorithm(IndexWord word, String sentence) throws JWNLException {

		String best_sense = word.getSense(1).getGloss();
		int max_overlap = 0;

		String[] context = sentence.replaceAll("[;|,|(|)|\"]", "").split(" ");
		for (Synset sense : word.getSenses()) {

			String[] signature = sense.getGloss().replaceAll("[;|,|(|)|\"|.]", "").split(" ");
			int overlap = ComputeOverlap(signature, context);
			if (overlap > max_overlap) {

				max_overlap = overlap;
				best_sense = sense.getGloss();
			}
		}
		System.out.println(best_sense);
		return best_sense;
	}

	private int ComputeOverlap(String[] signature, String[] context) throws JWNLException {

		int count = 0;

		for (int i = 0; i < context.length; i++) {
			for (int j = 0; j < signature.length; j++) {
				if (signature[j].equalsIgnoreCase(context[i]) && !blacklist(signature[j]))
					count++;
			}
		}
		return count;
	}

	private boolean blacklist(String word) throws JWNLException {

		List<String> blacklist = new ArrayList<>(Arrays.asList("a", "an", "the", "some", "in", "on", "up", "down",
				"left", "right", "into", "any", "ever", "it"));

		return blacklist.contains(word);
	}

	public static void main(String[] args) {

		Algorithm lesk = new Algorithm();
		lesk.findSense();
		// lesk.leskAlgorithm(sense, sentence);

	}

}
