package edu.cmu.lti.oaqa.model;
import java.util.*;

import json.gson.AbstractTextDoc;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import util.Similarity;
import util.Stemmer;
import util.TypeFactory;
import util.TypeUtil;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse.Result;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.Document;

public class DocumentAE extends JCasAnnotator_ImplBase {

  private GoPubMedService service;

  public void initialize(UimaContext aContext) {
    String property_file = "project.properties";
    try {
      service = new GoPubMedService(property_file);
    } catch (ConfigurationException e) {
      e.printStackTrace();
      System.out.println("Service Initialization Failed");
    }
  }

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    /*
     * Extract Question information
     */
    Question question = TypeUtil.getQuestion(jcas);
    String qID = question.getId();
    String qType = question.getQuestionType();
    String qText = question.getText();
    qText = qText.replaceAll("[?.,!:;]", " ").toLowerCase();
    HashMap<String,Integer> query_text = new HashMap<String,Integer>();
    for(String word : qText.split(" ")){
		if(query_text.containsKey(word)){
			query_text.put(word, query_text.get(word)+1);
		}
		else{
			query_text.put(word, 1);
		}
	}
    
    /*
     * Retrieve related Documents
     */
    try{
    	List<AbstractTextDoc> doc_list = new ArrayList<AbstractTextDoc>();
    	String[] words = qText.split(" ");
    	PubMedSearchServiceResponse.Result pubmedResult = service.findPubMedCitations(qText, 0);
    	for(PubMedSearchServiceResponse.Document finding : pubmedResult.getDocuments()){
    		AbstractTextDoc doc = new AbstractTextDoc(finding.getPmid(),finding.getDocumentAbstract());
    		doc_list.add(doc);
    	}
    	Similarity.CosineDist(query_text, doc_list);
    	//Similarity.BM25Dist(qText, doc_list);
    	for(AbstractTextDoc doc : doc_list){
    		Document document = TypeFactory.createDocument(jcas, "http://www.ncbi.nlm.nih.gov/pubmed/" + doc.getPmid(), doc.getPmid(),doc.getScore(),doc.getText());
    	    document.addToIndexes();
    	}
    } catch (Exception e) {
      System.out.println("Failed to extract documents");
    }  
  }
}
