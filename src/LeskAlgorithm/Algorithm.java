package LeskAlgorithm;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
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

	public static void main(String[] args) {

		try {
			JWNL.initialize(new FileInputStream("asset\\file-properties.xml"));
			BufferedReader buf = new BufferedReader(new FileReader("asset\\sentences.txt"));
			String lineJustFetched = null;
			ArrayList<String> words = new ArrayList<>();

			while (true) {
				lineJustFetched = buf.readLine();
				if (lineJustFetched == null)
					break;
				else if (lineJustFetched.contains("-")) {
					String line = lineJustFetched.replace("- ", "");
					words.add(line);
				}
			}
			buf.close();

			IndexWord sense = Dictionary.getInstance().getIndexWord(POS.NOUN, "bank");
			ArrayList<String> word = new ArrayList<String>();

			Pattern pattern = Pattern.compile("[*]{2}(.*?)[*]{2}");

			for (String string : words) {
				Matcher matcher = pattern.matcher(string);

				if (matcher.find() && !word.contains(matcher.group(1).toLowerCase())) {				
						word.add(matcher.group(1).toLowerCase());
				}
			}

			for (String string : word) {
				System.out.println(string);
			}

			// new Algorithm().leskAlgorithm(sense, sentence);
		} catch (Exception e) {
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

}
