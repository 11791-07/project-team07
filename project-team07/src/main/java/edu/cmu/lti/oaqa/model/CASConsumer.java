package edu.cmu.lti.oaqa.model;

import static java.util.stream.Collectors.toList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import json.gson.QuestionType;
import json.gson.Snippet;
import json.gson.TestQuestion;
import json.gson.TestSet;
import json.gson.Triple;
import lemurproject.indri.QueryEnvironment;
import lemurproject.indri.RMExpander;
import lemurproject.indri.ScoredExtentResult;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import util.Evaluation;
import util.TypeUtil;
import util.EvaluationObject;
import edu.cmu.lti.oaqa.type.kb.Concept;
import edu.cmu.lti.oaqa.type.answer.Answer;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.retrieval.Passage;
import edu.cmu.lti.oaqa.type.retrieval.TripleSearchResult;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;

public class CASConsumer extends CasConsumer_ImplBase {
	List<TestQuestion> processed_questions;
	private String processed_file;
	private HashMap<String, TestQuestion> gold_standard;
	private int NonDocQuery;
	

	public void initialize() throws ResourceInitializationException {
		processed_questions = new ArrayList<TestQuestion>();
		String gold_standard_string ="src/main/resources/BioASQ-SampleData1B.json";
		gold_standard = readJSON(gold_standard_string); 
		NonDocQuery = 0;
	}

	@Override
	public void processCas(CAS aJcas) throws ResourceProcessException {
		JCas jcas;
		Question question;
		
		try {
			jcas = aJcas.getJCas();
			question = TypeUtil.getQuestion(jcas);
			String id = question.getId();
			String body = question.getText();
			QuestionType type = convertString2Type(question.getQuestionType());
			
			
			List<String> concepts = new ArrayList<String>();
			for(ConceptSearchResult concept_search : TypeUtil.getRankedConceptSearchResults(jcas)){
				concepts.add(concept_search.getUri());
			}
			List<Triple> json_triples = new ArrayList<Triple>();
			for(TripleSearchResult triple_search : TypeUtil.getRankedTripleSearchResults(jcas)){
				Triple triple = new Triple(triple_search.getTriple().getSubject(),triple_search.getTriple().getPredicate(),triple_search.getTriple().getObject());
				json_triples.add(triple);
			}
			
			List<Snippet> snippets = new ArrayList<Snippet>();
			int snippet_hitsize = 50;
			for(Passage snippet : TypeUtil.getRankedPassages(jcas)){
			  Snippet jason_snippet = new Snippet(snippet.getUri(),snippet.getText(),snippet.getOffsetInBeginSection(),snippet.getOffsetInEndSection(),snippet.getBeginSection(),snippet.getEndSection());
			  snippets.add(jason_snippet);
			}
			
			List<String> documents = new ArrayList<String>();
			for (Document doc : TypeUtil.getRankedDocuments(jcas)) {
					documents.add(doc.getUri());
			}
			Answer ans = (Answer) TypeUtil.getAnswers(jcas).toArray()[0];
			TestQuestion processed_question = new TestQuestion(id, body, type,
					documents, snippets, concepts, json_triples, ans.getText());
			processed_questions.add(processed_question);
		} catch (CASException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {
		super.collectionProcessComplete(arg0);
		Evaluation eval = new Evaluation(processed_questions, gold_standard);
		System.out.println("CONCEPT MAP: "+eval.ConceptMAP(true));
	    System.out.println("DOC MAP:  "+eval.DocMAP(true));
		System.out.println("TRIPLE MAP:  "+eval.TripleMAP(true));
		System.out.println("SNIPPET MAP : "+eval.SnippetMAP(true));
	}

	// Auxiliary function
	public QuestionType convertString2Type(String type) {
		switch (type) {
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

	public HashMap<String, TestQuestion> readJSON(String filePath) {
		HashMap<String, TestQuestion> question_list = null;
		FileInputStream fis = null;
		Object value = filePath;
		try {
			fis = new FileInputStream(filePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (String.class.isAssignableFrom(value.getClass())) {
			question_list = (HashMap<String, TestQuestion>) TestSet
					.loadMap(fis);
			// TestSet.loadMap(fis);
		} else if (String[].class.isAssignableFrom(value.getClass())) {
			question_list = (HashMap<String, TestQuestion>) Arrays
					.stream(String[].class.cast(value))
					.flatMap(
							path -> ((Collection<TestQuestion>) TestSet
									.loadMap(getClass().getResourceAsStream(
											path))).stream()).collect(toList());
		}
		return question_list;
	}
}