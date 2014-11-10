package edu.cmu.lti.oaqa.model;

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
		// TODO Auto-generated method stub
		
		/*
		 * Extract Question information
		 */
		Question question = TypeUtil.getQuestion(jcas);
		String qID = question.getId();
		String qType = question.getQuestionType();
		String qText = question.getText();
        
        /* 
         * Retrieve related Concepts
         */
        try{
	        String concept_keyword = qText;
	        OntologyServiceResponse.Result diseaseOntologyResult = service
	                .findDiseaseOntologyEntitiesPaged(concept_keyword, 0);
	        int max_concept = 3;  //Just to make dummy output compact, no more than three outputs. 
	        int counter =0;
	        for (OntologyServiceResponse.Finding finding : diseaseOntologyResult.getFindings()) {
	            Concept concept = TypeFactory.createConcept(jcas, finding.getConcept().getLabel(),finding.getConcept().getUri());
	        	concept.addToIndexes();
	        	counter++;
	        	if(counter==max_concept){
	        		break;
	        	}
	        }
        } catch(Exception e){
        	System.out.println("Failed to extract concepts");
        }
        

	}

}
