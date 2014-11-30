package edu.cmu.lti.oaqa.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import json.gson.FullTextDoc;
import json.gson.TestQuestion;
import json.gson.TestSet;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import com.github.julman99.gsonfire.GsonFireBuilder;
import com.google.gson.Gson;

import util.Similarity;
import util.TypeFactory;
import util.TypeUtil;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.retrieval.Passage;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;


public class SnippetAE extends JCasAnnotator_ImplBase {
    private Gson gson;
	private IDictionary dict;
	public void initialize(UimaContext aContext){
		gson = new GsonFireBuilder().createGson();
		String path ="src/main/resources/dict";
		URL url;
		try {
			url = new URL ( "file" , null , path );
			dict =  new Dictionary(url) ;
			dict.open();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		Question question = TypeUtil.getQuestion(jcas);
		String qID = question.getId();
		String qType = question.getQuestionType();
		String qText = question.getText();
	    int num_empty = 0;
	    int num_total = TypeUtil.getRankedDocuments(jcas).size();
	    //*
	    try {
	    	//Document doc : TypeUtil.getRankedDocuments(jcas)
	    	for(Document doc : TypeUtil.getRankedDocuments(jcas)){
	    	    String[] buf = doc.getUri().split("/");
	    		String pmid = buf[buf.length-1];
	    		List<Passage> cur_snippets = new ArrayList<Passage>();
	    		FullTextDoc full_text = GetFullText(pmid);
	    		if(full_text==null){
	    			num_empty++;
	    			System.out.println("WARNING: NO FULL TEXT FOR PMID: "+pmid+" USING ABSTRACT INSTEAD");
	    			//Using abstract to construct "FullText"
	    			List<String> sections = new ArrayList<String>();
	    			sections.add(doc.getText());  //The Text here has been set as abstract in DocumentAE.java
	    			full_text = new FullTextDoc(pmid,sections, "");
	    		}
	    		else{
	    			System.out.println("LOGGING: HAVE ACCESS TO FULL TEXT FOR PMID: "+pmid);
	    		}
	    		int num_section = full_text.getSections().size();
	    		int sec_index = 0;
	    		for(sec_index = 0;sec_index<num_section;sec_index++){
	    			String cur_section = full_text.getSections().get(sec_index);
	    			String[] sentence_set = cur_section.split("[.\n]");
	    			for(String sentence : sentence_set){
	    				if(sentence.length()<10){
	    					continue;
	    				}
	    				ProcessSentence(cur_snippets, sentence, pmid, cur_section, sec_index, question, jcas);
	    			}
	    		}
	    		RankandFilter(cur_snippets);
	    	}
	    	int snippet_hitsize = 50;
	    	Collection<Passage> set = TypeUtil.getScoredPassage(jcas, snippet_hitsize);
	    	for (Passage snippet : TypeUtil.getRankedPassages(jcas)) {
				if(!set.contains(snippet)){
					snippet.removeFromIndexes();
				}
			}
	    	System.out.println("SUMMARY: "+num_empty+" OUT OF "+num_total+" is EMPTY!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//*/
	}
	public void ProcessSentence(List<Passage> cur_snippets, String sentence, String pmid, String cur_section, int sec_index, Question question, JCas jcas){
		
		double score = Similarity.CosineDistExpansion(question.getText(), sentence,dict);
		//double score = Similarity.CosineDist(question.getText(), sentence);
		
		String uri = "http://www.ncbi.nlm.nih.gov/pubmed/" + pmid;;
		String docID = pmid;
		String beginSection = "sections."+Integer.toString(sec_index);
		String endSection = "sections."+Integer.toString(sec_index);
		int offsetInBeginSection = cur_section.indexOf(sentence);
		int offsetInEndSection = offsetInBeginSection + sentence.length();
		Passage snippet = TypeFactory.createPassage(jcas, score, uri, sentence, docID, offsetInBeginSection, offsetInEndSection,  beginSection,  endSection);
		cur_snippets.add(snippet);
	}
	
	public void RankandFilter(List<Passage> cur_snippets){
	  for(Passage snippet : cur_snippets){
			//if(snippet.getScore()>0.05){
    		   snippet.addToIndexes();
    	    //}
       }
	}
	
	public FullTextDoc GetFullText(String pmid){
		InputStream input;
		FullTextDoc cur = null;
		try {
			input = new URL("http://gold.lti.cs.cmu.edu:30002/pmc/"+pmid).openStream();
			cur = gson.fromJson(new InputStreamReader(input), FullTextDoc.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Fail to get Full text for "+pmid);
			return null;
		}
		return cur;
	}

}
