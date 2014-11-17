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
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse.Result;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Triple;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.retrieval.TripleSearchResult;

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
   // String qID = question.getId();
   // String qType = question.getQuestionType();
    String qText = question.getText();

    /*
     * Retrieve related Documents
     */
    try {
      //TODO: create stopword list
      String[] words = qText.toLowerCase().split(" ");
      for (String word : words) {
        Result meshResult = service.findPubMedCitations(word, 0);
        for (edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse.Document finding : meshResult.getDocuments()) {
            Document doc = TypeFactory.createDocument(jcas, "http://www.ncbi.nlm.nih.gov/pubmed/" + finding.getPmid(), finding.getPmid());
            doc.addToIndexes();          
        }
      }
        
      /*//working code dummy code
        for (String word : words) {
        OntologyServiceResponse.Result meshResult = service.findMeshEntitiesPaged(word, 0);
        for (OntologyServiceResponse.Finding finding : meshResult.getFindings()) {
          if (finding.getScore() > 0.5) {
            Document doc = TypeFactory.createDocument(jcas, finding.getConcept().getUri());
            doc.addToIndexes();
          }
        }
      }*/   
      
      

    } catch (Exception e) {
      System.out.println("Failed to extract documents");
    }

  }

}
