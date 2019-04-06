package LeskAlgorithm;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
			IndexWord sense = Dictionary.getInstance().getIndexWord(POS.NOUN, "bank");
			String sentence = "the bank can guarantee deposits will eventually cover future tuition costs because it invests in adjustablerate mortgage securities";
			new Algorithm().leskAlgorithm(sense, sentence);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Algorithm() {
		// TODO Auto-generated constructor stub
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
		
		int count=0;
		
		for (int i = 0; i < context.length; i++) {
			for (int j = 0; j < signature.length; j++) {
				if(signature[j].equalsIgnoreCase(context[i]) && getPos(signature[j]) != "undefined")
					count++;
			}
		}
		return count;
	}
	
	 private String getPos(String word) throws JWNLException {
		 
		 
		 List<String> article= new ArrayList<>(Arrays.asList("a","an", "the", "some", "in", "on", "up", "down","left", "right","into", "any", "ever" ,"it"));
		 
		 if(article.contains(word))
			 return "undefined";
		 
		    int[] polysemies = new int[4];
		    int max = 0;
		    int index = 0;
		    String[] pos = {"NOUN", "VERB", "ADJECTIVE", "ADVERB"};
		    Dictionary d = Dictionary.getInstance();
		    
		    IndexWord noun_form = d.getIndexWord(POS.NOUN, word);
		    polysemies[0] = (noun_form==null)?0:noun_form.getSenses().length;
		    IndexWord verb_form = d.getIndexWord(POS.VERB, word);
		    polysemies[1] = (verb_form==null)?0:verb_form.getSenses().length;
		    IndexWord adj_form = d.getIndexWord(POS.ADJECTIVE, word);
		    polysemies[2] = (adj_form==null)?0:adj_form.getSenses().length;
		    IndexWord adv_form = d.getIndexWord(POS.ADVERB, word);
		    polysemies[3] = (adv_form==null)?0:adv_form.getSenses().length;
		    
		    for (int i = 0; i < 4; i++) {
				if(polysemies[i] > max) {
					index = i;
					max = polysemies[i];
				}
			}
		    return pos[index];
		  }
}
