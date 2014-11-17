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
   
  class ValueComparator implements Comparator<Concept> {

     Map<Concept, Double> base;
     public ValueComparator(HashMap<Concept,Double> concepts) {
         this.base = concepts;
     }

     // Note: this comparator imposes orderings that are inconsistent with equals.    
     public int compare(Concept a, Concept b) {
         if (base.get(a) >= base.get(b)) {
             return -1;
         } else {
             return 1;
         } // returning 0 would merge keys
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
        
		HashMap<Concept, Double> concepts = new HashMap<Concept, Double>(); 
		ValueComparator bvc =  new ValueComparator(concepts);
    TreeMap<Concept, Double> sorted_concepts = new TreeMap<Concept, Double>(bvc);
    
    /* 
     * Retrieve related Concepts
     */
    try {
      int max_concept = 1; // Just to make dummy output compact, no more than three outputs.
      int counter;
      String concept_keyword = qText;
      Concept concept;
      
      OntologyServiceResponse.Result diseaseOntologyResult = service.findDiseaseOntologyEntitiesPaged(concept_keyword, 0);
      counter = 0;
      System.out.println("Disease ontology: " + diseaseOntologyResult.getFindings().size());
      for (OntologyServiceResponse.Finding finding : diseaseOntologyResult.getFindings()) {
        System.out.println("Score: " + finding.getScore());
        concept = TypeFactory.createConcept(jcas, finding.getConcept().getLabel(), finding.getConcept().getUri());
        concepts.put(concept, finding.getScore()); //concept.addToIndexes();
        if (++counter >= max_concept)
          break;
      }
      
      OntologyServiceResponse.Result geneOntologyResult = service.findGeneOntologyEntitiesPaged(concept_keyword, 0, 10);
      counter = 0;
      System.out.println("Gene ontology: " + geneOntologyResult.getFindings().size());
      for (OntologyServiceResponse.Finding finding : geneOntologyResult.getFindings()) {
        System.out.println("Score: " + finding.getScore());
        concept = TypeFactory.createConcept(jcas, finding.getConcept().getLabel(), finding.getConcept().getUri());
        concepts.put(concept, finding.getScore()); //concept.addToIndexes();
        if (++counter >= max_concept)
          break;
      }


      OntologyServiceResponse.Result jochemResult = service.findJochemEntitiesPaged(concept_keyword, 0);
      counter = 0;
      System.out.println("Jochem: " + jochemResult.getFindings().size());
      for (OntologyServiceResponse.Finding finding : jochemResult.getFindings()) {
        System.out.println("Score: " + finding.getScore());
        concept = TypeFactory.createConcept(jcas, finding.getConcept().getLabel(), finding.getConcept().getUri());
        concepts.put(concept, finding.getScore()); //concept.addToIndexes();
        if (++counter >= max_concept)
          break;
      }      
      
      OntologyServiceResponse.Result meshResult = service.findMeshEntitiesPaged(concept_keyword, 0);
      counter = 0;
      System.out.println("MeSH: " + meshResult.getFindings().size());
      for (OntologyServiceResponse.Finding finding : meshResult.getFindings()) {
        System.out.println("Score: " + finding.getScore());
        concept = TypeFactory.createConcept(jcas, finding.getConcept().getLabel(), finding.getConcept().getUri());
        concepts.put(concept, finding.getScore()); //concept.addToIndexes();
        if (++counter >= max_concept)
          break;
      }


      OntologyServiceResponse.Result uniprotResult = service.findUniprotEntitiesPaged(concept_keyword, 0);
      counter = 0;
      System.out.println("UniProt: " + uniprotResult.getFindings().size());
      for (OntologyServiceResponse.Finding finding : uniprotResult.getFindings()) {
        System.out.println("Score: " + finding.getScore());
        concept = TypeFactory.createConcept(jcas, finding.getConcept().getLabel(), finding.getConcept().getUri());
        concepts.put(concept, finding.getScore()); //concept.addToIndexes();
        if (++counter >= max_concept)
          break;
      }
    } catch (Exception e) {
      System.out.println("Failed to extract concepts");
    }

    sorted_concepts.putAll(concepts);

    System.out.println("results: " + sorted_concepts);
    
    for (Map.Entry<Concept, Double> entry : sorted_concepts.entrySet()) {
        Concept key = entry.getKey();
        Double value = entry.getValue();

        key.addToIndexes();     
    }
	}

}
