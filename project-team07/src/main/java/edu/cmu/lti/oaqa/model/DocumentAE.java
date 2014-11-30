package edu.cmu.lti.oaqa.model;
import java.io.*;
import java.util.*;

import json.gson.AbstractTextDoc;
import json.gson.CompareAbstract;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Streams;

import util.PosDict;
import util.Similarity;
import util.Stemmer;
import util.TypeFactory;
import util.TypeUtil;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse.Result;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations;




public class DocumentAE extends JCasAnnotator_ImplBase {

  private GoPubMedService service;
  public Chunker chunker = null;
  public File modelFile = null;
  public TokenizerFactory TOKENIZER_FACTORY;
  public HmmDecoder decoder;
  public PosDict posdict ;
  
  public void initialize(UimaContext aContext) {
    String property_file = "project.properties";
    String POSmodel_file = (String) aContext.getConfigParameterValue("Param_ModelFilePOS");
    try {
      TOKENIZER_FACTORY	= new RegExTokenizerFactory("(-|'|\\d|\\p{L})+|\\S"); 	
      service = new GoPubMedService(property_file);
	  chunker = (Chunker) AbstractExternalizable.readResourceObject(DocumentAE.class, (String) aContext.getConfigParameterValue("Param_ModelFile"));
	  FileInputStream fileIn = new FileInputStream(POSmodel_file);
      ObjectInputStream objIn = new ObjectInputStream(fileIn);
      HiddenMarkovModel hmm = (HiddenMarkovModel) objIn.readObject();
      Streams.closeQuietly(objIn);
      decoder = new HmmDecoder(hmm);
      posdict = new PosDict();
    } catch (Exception e){
    	System.out.println("initialization failed");
    	e.printStackTrace();
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
    char[] cs = qText.toCharArray();
    qText = qText.replaceAll("[?.,!:;()]", " ").toLowerCase();
    
    Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(cs,0,cs.length);
    String[] tokens = tokenizer.tokenize();
    List<String> tokenList = Arrays.asList(tokens);
    Tagging<String> tagging = decoder.tag(tokenList);
    StringBuffer POSquerybuf = new StringBuffer();
    for (int i = 0; i < tagging.size(); ++i){
    	String POS = tagging.tag(i);
    	String word = tagging.token(i).toLowerCase();
    	if(posdict.Accept(word, POS)){
    		POSquerybuf.append(word+" ");
    	}
    }
    String POSquery = POSquerybuf.toString();    
    
    HashMap<String,Integer> query_text = new HashMap<String,Integer>();
    for(String word : qText.split(" ")){
		if(query_text.containsKey(word)){
			query_text.put(word, query_text.get(word)+1);
		}
		else{
			query_text.put(word, 1);
		}
	}
    Chunking chunkingHmm = chunker.chunk(qText);
    
    
    /*
     * Retrieve related Documents
     */
    //*
    try{
    	List<AbstractTextDoc> doc_list = new ArrayList<AbstractTextDoc>();
    	String[] words = qText.split(" ");
    	Set<String> visited = new HashSet<String>();
    	int concept_max = 5;
    	int concept_counter = 0;
    	Set<String> concept_set = new HashSet<String>();
    	String concept_query = "";
    	for (ConceptSearchResult concept_search : TypeUtil.getRankedConceptSearchResults(jcas)){
    		if(concept_counter>=concept_max){
    			break;
    		}
    		String concept = concept_search.getConcept().getName();//+concept_search.getSearchId();
    		String[] buf = concept.split(" ");
    		for(String s : buf){
    			if(!concept_set.contains(s) && qText.indexOf(s)!=-1){
    				concept_set.add(s);
    				concept_query = concept_query+" "+s;
    			}
    		}
	    	concept_counter++;
    	}
    	
		for(Chunk ner : chunkingHmm.chunkSet()){
			String NER = qText.substring(ner.start(),ner.end());
	    	PubMedSearchServiceResponse.Result pubmedResult = service.findPubMedCitations(NER, 0);
	    	for(PubMedSearchServiceResponse.Document finding : pubmedResult.getDocuments()){
	    		AbstractTextDoc doc = new AbstractTextDoc(finding.getPmid(),finding.getDocumentAbstract(),finding.getTitle());
	    		if(!visited.contains(finding.getPmid())){
	    			visited.add(finding.getPmid());
	    			doc_list.add(doc);
	    		}
	    	}
    	}

		PubMedSearchServiceResponse.Result pubmedResult;
		pubmedResult = service.findPubMedCitations(POSquery, 0);
		for(PubMedSearchServiceResponse.Document finding : pubmedResult.getDocuments()){
    		AbstractTextDoc doc = new AbstractTextDoc(finding.getPmid(),finding.getDocumentAbstract(),finding.getTitle());
    		if(!visited.contains(finding.getPmid())){
    			visited.add(finding.getPmid());
    			doc_list.add(doc);
    		}
    	}

    	pubmedResult = service.findPubMedCitations(qText, 0);
    	for(PubMedSearchServiceResponse.Document finding : pubmedResult.getDocuments()){
    		AbstractTextDoc doc = new AbstractTextDoc(finding.getPmid(),finding.getDocumentAbstract(),finding.getTitle());
    		if(!visited.contains(finding.getPmid())){
    			visited.add(finding.getPmid());
    			doc_list.add(doc);
    		}
    	}
  
    	pubmedResult = service.findPubMedCitations(concept_query, 0);
    	for(PubMedSearchServiceResponse.Document finding : pubmedResult.getDocuments()){
    		AbstractTextDoc doc = new AbstractTextDoc(finding.getPmid(),finding.getDocumentAbstract(),finding.getTitle());
    		if(!visited.contains(finding.getPmid())){
    			visited.add(finding.getPmid());
    			doc_list.add(doc);
    		}
    	}
    	Similarity.CosineDist(query_text, doc_list);
    	//Similarity.BM25Dist(qText, doc_list);
    	for(AbstractTextDoc doc : doc_list){
    		double alpha = 0.5;
    		double score = alpha*doc.getTitleScore() + (1-alpha)*doc.getScore();
    		Document document = TypeFactory.createDocument(jcas, "http://www.ncbi.nlm.nih.gov/pubmed/" + doc.getPmid(), doc.getPmid(),score,doc.getText());
    	    document.addToIndexes();
    	}
    	int doc_hitsize = 100;
    	Collection<Document> set = TypeUtil.getScoredDocument(jcas,doc_hitsize);
		for (Document doc : TypeUtil.getRankedDocuments(jcas)) {
			if(!set.contains(doc)){
				doc.removeFromIndexes();
			}
		}
    } catch (Exception e) {
      System.out.println("Failed to extract documents");
    } 
    //*/
    //System.out.println(qText);
    //System.out.println(POSquery);
    
  }
}
