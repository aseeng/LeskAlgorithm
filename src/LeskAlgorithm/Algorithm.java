package LeskAlgorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
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
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

public class Algorithm {

	public ArrayList<String> context = new ArrayList<>();
	public ArrayList<String> words = new ArrayList<>();
	public int finalOverlap;

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

			InputStream posModelIn = new FileInputStream("asset" + File.separator + "en-pos-maxent.bin");
			POSModel posModel = new POSModel(posModelIn);
			POSTaggerME posTagger = new POSTaggerME(posModel);

			InputStream is = new FileInputStream(new File("asset\\en-lemmatizer.dict"));
			DictionaryLemmatizer lemmatizer = new DictionaryLemmatizer(is);

			Pattern pattern = Pattern.compile("[*]{2}(.*?)[*]{2}");

			for (String string : context) {
				Matcher matcher = pattern.matcher(string);
				if (matcher.find()) {

					String[] word = new String[1];
					word[0] = matcher.group(1).toLowerCase();
					String pos[] = posTagger.tag(word);
					String[] lemma = lemmatizer.lemmatize(word, pos);
					words.add(lemma[0]);
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Long> findIndex() {

		ArrayList<Long> synsets = new ArrayList<Long>();
		try {

			List<POS> pos = POS.getAllPOS();

			for (int i = 0; i < words.size(); i++) {

				finalOverlap = -1;
				int tmpOverlap = -1;
				Synset synset = null;

				for (POS tag : pos) {

					IndexWord sense = Dictionary.getInstance().getIndexWord(tag, words.get(i));

					if (sense != null) {

//						System.out.println(sense + " " + context.get(i));
						Synset tmpSynset = leskAlgorithm(sense, context.get(i));
						// System.out.println(" parola " + words.get(i) + " tag " + tag + " overlap " + finalOverlap + " synsetID " + tmpSynset + " frase " + context.get(i));

						if (finalOverlap > tmpOverlap) {
							synset = tmpSynset;
							tmpOverlap = finalOverlap;
						}
					}
				}
				System.out.println(synset.getGloss());
				//synsets.add(synset);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return synsets;
	}

	public Synset leskAlgorithm(IndexWord word, String sentence) throws JWNLException {

		Synset best_sense = word.getSense(1);
		String gloss = word.getSense(1).getGloss();
		int max_overlap = 0;

		String[] context = sentence.replaceAll("[;|,|(|)|\"]", "").split(" ");
		for (Synset sense : word.getSenses()) {
			System.out.println();

			String[] signature = sense.getGloss().replaceAll("[;|,|(|)|\"|.]", "").split(" ");
			int overlap = ComputeOverlap(signature, context);
			if (overlap > max_overlap) {

				max_overlap = overlap;
				best_sense = sense;
				gloss = sense.getGloss();
			}
		}
		if (max_overlap > finalOverlap) {
			finalOverlap = max_overlap;
			return best_sense;
		}
		return null;
	}

	private int ComputeOverlap(String[] signature, String[] context) throws JWNLException {

		int count = 0;

		for (int i = 0; i < context.length; i++) {
			for (int j = 0; j < signature.length; j++) {
				if (signature[j].equalsIgnoreCase(context[i]) && !blacklist(signature[j])) {
					count++;
				}
			}
		}
		return count;
	}

	private boolean blacklist(String word) throws JWNLException {

		List<String> blacklist = new ArrayList<>(
				Arrays.asList("a", "an", "the", "some", "in", "on", "to", "up", "down", "into", "any", "ever", "it"));

		return blacklist.contains(word);
	}

	public static void main(String[] args) throws JWNLException {

		Algorithm lesk = new Algorithm();
//		lesk.leskAlgorithm(Dictionary.getInstance().getIndexWord(POS.NOUN, "arm"), "**Arms** bend at the elbow");
//		lesk.leskAlgorithm(Dictionary.getInstance().getIndexWord(POS.NOUN, "arm"),"Germany sells **arms** to Saudi Arabia.");
//		lesk.leskAlgorithm(Dictionary.getInstance().getIndexWord(POS.NOUN, "key"), "The **key** broke in the lock.");
//		lesk.leskAlgorithm(Dictionary.getInstance().getIndexWord(POS.ADJECTIVE, "key"), "The **key** problem was not one of quality but of quantity.");
//		lesk.leskAlgorithm(Dictionary.getInstance().getIndexWord(POS.NOUN, "solution"), "Work out the **solution** in your head.");
//		lesk.leskAlgorithm(Dictionary.getInstance().getIndexWord(POS.NOUN, "solution"), "Heat the **solution** to 75° Celsius. ");
//		lesk.leskAlgorithm(Dictionary.getInstance().getIndexWord(POS.NOUN, "ash"), "The house was burnt to **ashes** while the owner returned.");
//		lesk.leskAlgorithm(Dictionary.getInstance().getIndexWord(POS.NOUN, "ash"), "This table is made of **ash** wood.");
//		lesk.leskAlgorithm(Dictionary.getInstance().getIndexWord(POS.NOUN, "lunch"), "The **lunch** with her boss took longer than she expected. ");
//		lesk.leskAlgorithm(Dictionary.getInstance().getIndexWord(POS.NOUN, "lunch"), "She packed her **lunch** in her purse.");
//		lesk.leskAlgorithm(Dictionary.getInstance().getIndexWord(POS.NOUN, "classification"), " The **classification** of the genetic data took two years.");
//		lesk.leskAlgorithm(Dictionary.getInstance().getIndexWord(POS.NOUN, "classification"), "The journal Science published the **classification** this month."); //VEDERE POS POSSIBILI
//		lesk.leskAlgorithm(Dictionary.getInstance().getIndexWord(POS.NOUN, "wood"), "His cottage is near a small **wood**."); //VEDERE POS POSSIBILI
//		lesk.leskAlgorithm(Dictionary.getInstance().getIndexWord(POS.NOUN, "wood"), "The statue was made out of a block of **wood**.");
		ArrayList<Long> l = lesk.findIndex();
		// System.out.println(l);
	}

}