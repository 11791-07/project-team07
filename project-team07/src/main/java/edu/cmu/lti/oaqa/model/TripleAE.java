package edu.cmu.lti.oaqa.model;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import util.TypeFactory;
import util.TypeUtil;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.LinkedLifeDataServiceResponse;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Triple;
import edu.cmu.lti.oaqa.type.retrieval.TripleSearchResult;


public class TripleAE extends JCasAnnotator_ImplBase {
    
   private GoPubMedService service;
	
   public void initialize(UimaContext aContext){
	   String property_file = "project.properties";
	   try {
		service = new GoPubMedService(property_file);
	   } catch (ConfigurationException e) {
		   // TODO Auto-generated catch block
		   e.printStackTrace();
		   System.out.println("Service Initialization Failed");
	   }	
	}
	
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		// TODO Auto-generated method stub
		Question question = TypeUtil.getQuestion(jcas);
		String qID = question.getId();
		String qType = question.getQuestionType();
		String qText = question.getText();
        //*
		try {
	        String triple_keyword = qText;
	        LinkedLifeDataServiceResponse.Result linkedLifeDataResult = service
	                .findLinkedLifeDataEntitiesPaged(triple_keyword, 0);
	        for (LinkedLifeDataServiceResponse.Entity entity : linkedLifeDataResult.getEntities()) {
	          for (LinkedLifeDataServiceResponse.Relation relation : entity.getRelations()) {
	        	  Triple triple = TypeFactory.createTriple(jcas, relation.getSubj(),  relation.getPred(), relation.getObj()); 
	        	  TripleSearchResult tripleresult= TypeFactory.createTripleSearchResult(jcas, triple);         
	        	  tripleresult.addToIndexes();       
	          }
	        }
        } catch (Exception e){
        	System.out.println("Failed to extract triples");
        }
        //*/
	}
}
