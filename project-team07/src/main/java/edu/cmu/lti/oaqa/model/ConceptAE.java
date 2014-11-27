package edu.cmu.lti.oaqa.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import util.TypeFactory;
import util.TypeUtil;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Concept;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;


public class ConceptAE extends JCasAnnotator_ImplBase {
    
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
        
		Question question = TypeUtil.getQuestion(jcas);
		String qID = question.getId();
		String qType = question.getQuestionType();
		String qText = question.getText();
	    /*
		try {
	      int max_concept = 1; // Just to make dummy output compact, no more than three outputs.
	      int counter;
	      String concept_keyword = qText.replaceAll("[?.,!:;]", " ");
	      Concept concept;
	      ConceptSearchResult concept_search;
	      
	      OntologyServiceResponse.Result diseaseOntologyResult = service.findDiseaseOntologyEntitiesPaged(concept_keyword, 0);
	      for (OntologyServiceResponse.Finding finding : diseaseOntologyResult.getFindings()) {
	        concept = TypeFactory.createConcept(jcas, finding.getConcept().getLabel(), finding.getConcept().getUri());
	        concept_search = TypeFactory.createConceptSearchResult(jcas, concept, finding.getConcept().getUri(), finding.getScore(), "", "");
	        concept_search.addToIndexes();
	      }
	      
	      OntologyServiceResponse.Result geneOntologyResult = service.findGeneOntologyEntitiesPaged(concept_keyword, 0, 10);
	      for (OntologyServiceResponse.Finding finding : geneOntologyResult.getFindings()) {
	        concept = TypeFactory.createConcept(jcas, finding.getConcept().getLabel(), finding.getConcept().getUri());
	        concept_search = TypeFactory.createConceptSearchResult(jcas, concept, finding.getConcept().getUri(), finding.getScore(), "", "");
	        concept_search.addToIndexes();
	      }
	
	
	      OntologyServiceResponse.Result jochemResult = service.findJochemEntitiesPaged(concept_keyword, 0);
	      for (OntologyServiceResponse.Finding finding : jochemResult.getFindings()) {
	    	  concept = TypeFactory.createConcept(jcas, finding.getConcept().getLabel(), finding.getConcept().getUri());
		      concept_search = TypeFactory.createConceptSearchResult(jcas, concept, finding.getConcept().getUri(), finding.getScore(), "", "");
		      concept_search.addToIndexes();
	      }      
	      
	      OntologyServiceResponse.Result meshResult = service.findMeshEntitiesPaged(concept_keyword, 0);
	      for (OntologyServiceResponse.Finding finding : meshResult.getFindings()) {
	    	  concept = TypeFactory.createConcept(jcas, finding.getConcept().getLabel(), finding.getConcept().getUri());
		      concept_search = TypeFactory.createConceptSearchResult(jcas, concept, finding.getConcept().getUri(), finding.getScore(), "", "");
		      concept_search.addToIndexes();
	      }
	
	
	      OntologyServiceResponse.Result uniprotResult = service.findUniprotEntitiesPaged(concept_keyword, 0);
	      for (OntologyServiceResponse.Finding finding : uniprotResult.getFindings()) {
	    	  concept = TypeFactory.createConcept(jcas, finding.getConcept().getLabel(), finding.getConcept().getUri());
		      concept_search = TypeFactory.createConceptSearchResult(jcas, concept, finding.getConcept().getUri(), finding.getScore(), "", "");
		      concept_search.addToIndexes();
	      }
	      
	    } catch (Exception e) {
	      System.out.println("Failed to extract concepts");
	    }
	    */
	}
	

}
