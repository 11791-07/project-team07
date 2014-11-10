package edu.cmu.lti.oaqa.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import json.gson.QuestionType;
import json.gson.Snippet;
import json.gson.TestFactoidQuestion;
import json.gson.TestQuestion;
import json.gson.TestSet;
import json.gson.Triple;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Concept;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import util.TypeUtil;

public class CASConsumer extends CasConsumer_ImplBase {
    
	
	List<TestQuestion> processed_questions;
	private String processed_file;
	
	public void initialize() throws ResourceInitializationException {
		processed_questions = new ArrayList<TestQuestion>();
		processed_file = "src/main/resources/Phase1_output.json";
		//processed_file = "src/main/resources/Phase1_output_single.json";
	}
	
	@Override
	public void processCas(CAS aJcas) throws ResourceProcessException {
		// TODO Auto-generated method stub
		JCas jcas;
		Question question;
	    try {
			jcas = aJcas.getJCas();
			//Get basic question information
			question = TypeUtil.getQuestion(jcas);
			String id = question.getId();
			String body = question.getText();
			QuestionType type = convertString2Type(question.getQuestionType());
			
			//Get documents retrieved in AE
			List<String> documents = new ArrayList<String>();
			for(Document doc : TypeUtil.getRankedDocuments(jcas)){
		        documents.add(doc.getUri());	
		    }
			
			//Get snippets retrieved in AE. Just make it empty, I did not extract snippets in dummy AE 
			List<Snippet> snippets = new ArrayList<Snippet>();
			
			//Get concepts retrieved in AE
			List<String> concepts = new ArrayList<String>();
			for(Concept concept : TypeUtil.getConcept(jcas)){
				concepts.add(concept.getUris().getNthElement(0));
			}
			
			//Get triples retrieved in AE
			List<Triple> jason_triples = new ArrayList<Triple>();
			for(edu.cmu.lti.oaqa.type.kb.Triple triple : TypeUtil.getTriple(jcas)){
				Triple jason_triple = new Triple(triple.getObject(),triple.getPredicate(),triple.getSubject());
			    jason_triples.add(jason_triple);
			}
			
			//Construct processed question object from information above then put it into list.
			//I changed the constructor of TestQuestion from protected to public so we can initialize it here. 
			TestQuestion processed_question = new TestQuestion(id, body, type, documents, snippets, concepts, jason_triples,"");
			processed_questions.add(processed_question);
			
		} catch (CASException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
	}
	
	@Override
	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {
       	super.collectionProcessComplete(arg0);
		String output = TestSet.dump(processed_questions);
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(processed_file))); 
		writer.write(output);
		writer.flush();
		writer.close();
	}
	
	
	//Auxiliary function 
	public  QuestionType convertString2Type(String type){
		switch (type){
		case "FACTOID":
			return QuestionType.factoid;
		case "LIST":
			return QuestionType.list;
		case "OPINION":
			return QuestionType.summary;
		case "YES_NO":
			return QuestionType.yesno;
		default:
			return QuestionType.factoid;
		}
	}

}
