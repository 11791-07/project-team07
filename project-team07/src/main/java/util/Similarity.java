package util;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import json.gson.AbstractTextDoc;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;


public class Similarity {
	
	
	public static double CosineDistExpansion(String question, String sentence,IDictionary dict){
		double res = 0;
		String[] query = question.toLowerCase().replace("[(),?-:]", " ")
				.split(" ");
		String[] sentence_word = sentence.toLowerCase()
				.replace("[(),?-:]", " ").split(" ");
		HashMap<String, Integer> doc_text = new HashMap<String, Integer>();
		HashMap<String, Integer> query_text = new HashMap<String, Integer>();
		HashMap<String, Set<String>> doc_text_expansion = new HashMap<String, Set<String>>();
		HashMap<String, Set<String>> query_text_expansion = new HashMap<String, Set<String>>();
		for (String word : sentence_word) {
			if (doc_text.containsKey(word)) {
				doc_text.put(word, doc_text.get(word) + 1);
			} else {
				doc_text.put(word, 1);
			}
			if (!doc_text_expansion.containsKey(word)) {
				doc_text_expansion.put(word, WordNetExpansion(word, dict));
			}
		}
		for (String word : query) {
			if (query_text.containsKey(word)) {
				query_text.put(word, query_text.get(word) + 1);
			} else {
				query_text.put(word, 1);
			}
			if (!query_text_expansion.containsKey(word)) {
				query_text_expansion.put(word, WordNetExpansion(word, dict));
			}
		}
		double query_len = 0;
		double doc_len = 0;
		for (String word : query_text.keySet()) {
			query_len = query_len + Math.pow(query_text.get(word), 2);
			String index = null;
			match(word, index, doc_text_expansion, query_text_expansion);
			if (index != null) {
				res = res + query_text.get(word) * doc_text.get(index);
			}
		}
		for (String word : doc_text.keySet()) {
			doc_len = doc_len + Math.pow(doc_text.get(word), 2);
		}
		return res / (Math.sqrt(doc_len) * Math.sqrt(query_len));
	}
	
	public static void match(String word, String index, HashMap<String, Set<String>> doc_text_expansion, HashMap<String, Set<String>> query_text_expansion){
		index = null;
		for(String query_expan : query_text_expansion.get(word)){
			for(String possible : doc_text_expansion.keySet()){
				Set<String> expansion = doc_text_expansion.get(possible);
				if(expansion.contains(query_expan)){
					index = possible;
					return;
				}
			}
		}
		for(String possible : doc_text_expansion.keySet()){
			Set<String> expansion = doc_text_expansion.get(possible);
			if(expansion.contains(word)){
				index = possible;
				return;
			}
		}
		return ;
	}
	
	public static Set<String> WordNetExpansion(String input, IDictionary dict){
		HashSet<String> res = new HashSet<String>();
		res.add(input);
		IIndexWord idxWord;
		try{
		    idxWord = dict.getIndexWord(input, POS.NOUN);
		}catch (Exception e){
			System.out.println("NO EXPANSION");
			return res;
		}
		if(idxWord!=null && !idxWord.getWordIDs().isEmpty()){
			System.out.println("EXPANSION SUCCEED");
			IWordID wordID = idxWord.getWordIDs().get(0);  // 1 st meaning
			IWord word = dict.getWord(wordID);
			ISynset synset = word.getSynset () ;
			word.getRelatedWords();
			// iterate over words associated with the synset
			for ( IWord w : synset.getWords()){
			    String[] temp = w.getLemma().toLowerCase().split(" ");
			    for(String s : temp){
			    	if(!res.contains(s)){
			    		res.add(s);
			    	}
			    }
			}
		}
		
		return res;
	}
	
	public static double CosineDist(String question, String sentence){
		HashMap<String, Integer> doc_text = new HashMap<String, Integer>();
		HashMap<String, Integer> query_text = new HashMap<String, Integer>();
		String[] query = question.toLowerCase().replace("[(),?-:]", " ").split(" ");
        String[] sentence_word = sentence.toLowerCase().replace("[(),?-:]", " ").split(" ");
		for(String word : sentence_word){
			if(doc_text.containsKey(word)){
				doc_text.put(word, doc_text.get(word)+1);
			}
			else{
				doc_text.put(word, 1);
			}
		}
		for(String word : query){
			if(query_text.containsKey(word)){
				query_text.put(word, query_text.get(word)+1);
			}
			else{
				query_text.put(word, 1);
			}
		}
		return CosineDist(query_text,doc_text);
	}
	
	public static double CosineDist(HashMap<String, Integer> query_text, HashMap<String, Integer> doc_text){
		double res = 0;
		double query_len = 0;
		double doc_len = 0;
		for(String word : query_text.keySet()){
			query_len = query_len + Math.pow(query_text.get(word),2);
			if(doc_text.containsKey(word)){
				res = res + query_text.get(word)*doc_text.get(word);
			}
		}
		for(String word : doc_text.keySet()){
			doc_len = doc_len + Math.pow(doc_text.get(word),2);
		}
		return res/(Math.sqrt(doc_len) * Math.sqrt(query_len));
	}
    
	public static void CosineDist(HashMap<String, Integer> query_text, List<AbstractTextDoc> doc_list){
		for(AbstractTextDoc doc : doc_list){
			doc.setScore(CosineDist(query_text,doc.getContent()));
			doc.setTitleScore(CosineDist(query_text,doc.getTitle()));
		}
	}
	
	public static void BM25Dist(String query_text, List<AbstractTextDoc> doc_list){
		double k1 = 1.5;
		double b = 0.75;
		String[] words = query_text.split(" ");
		int N = doc_list.size();
		double avgdl = 0;
		for(AbstractTextDoc doc : doc_list){
			avgdl = avgdl + doc.getLength();
		}
		avgdl = avgdl/N;
		double[] IDF = new double[words.length];
		for(int i=0;i<words.length;i++){
			String word = words[i];
			int counter = 0;
			for(AbstractTextDoc doc : doc_list){
				if(doc.getContent().containsKey(word)){
					counter++;
				}
			}
			IDF[i] = Math.log((N-counter+0.5)/(counter+0.5));
		}
		for(AbstractTextDoc doc : doc_list){
			double score = 0;
			double D = doc.getLength();
			HashMap<String, Integer> doc_text = doc.getContent();
			for(int i=0;i<words.length;i++){
				String word = words[i];
				double TF = doc_text.containsKey(word)? doc_text.get(word)/D : 0;
				score = score + IDF[i]*(TF*(k1+1))/(TF+k1*(1-b+b*D/avgdl));
			}
			doc.setScore(score);
		}
	}
	
}
