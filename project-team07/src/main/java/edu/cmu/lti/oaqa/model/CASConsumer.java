package edu.cmu.lti.oaqa.model;

import static java.util.stream.Collectors.toList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import json.gson.QuestionType;
import json.gson.Snippet;
//import json.gson.Snippet;
import json.gson.TestQuestion;
import json.gson.TestSet;
//import json.gson.Triple;


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

import util.TypeUtil;
import util.EvaluationObject;
import edu.cmu.lti.oaqa.type.kb.Concept;
import edu.cmu.lti.oaqa.type.kb.Triple;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.retrieval.Passage;
import edu.cmu.lti.oaqa.type.retrieval.TripleSearchResult;

public class CASConsumer extends CasConsumer_ImplBase {

  List<TestQuestion> processed_questions;

  private String processed_file;

  private HashMap<String, TestQuestion> gold_standard;

  private EvaluationObject e_concept;

  private EvaluationObject e_document;

  private EvaluationObject e_triple;

  private EvaluationObject e_snippet;

  private static double EPSILON = 0.1;

  public void initialize() throws ResourceInitializationException {
    processed_questions = new ArrayList<TestQuestion>();
    // processed_file = "src/main/resources/Phase1_output.json";
    processed_file = "src/main/resources/Phase1_output_single.json";
    // String gold_standard_string = "src/main/resources/BioASQ-SampleData1B.json";
    String gold_standard_string = "src/main/resources/BioASQ-SampleData1B_single.json";
    gold_standard = readJSON(gold_standard_string);
    e_concept = new EvaluationObject();
    e_document = new EvaluationObject();
    e_triple = new EvaluationObject();
    e_snippet = new EvaluationObject();
  }

  @Override
  public void processCas(CAS aJcas) throws ResourceProcessException {
    JCas jcas;
    Question question;

    try {
      jcas = aJcas.getJCas();
      // Get basic question information
      question = TypeUtil.getQuestion(jcas);
      String id = question.getId();
      String body = question.getText();
      QuestionType type = convertString2Type(question.getQuestionType());

      // Get documents retrieved in AE
      List<String> documents = new ArrayList<String>();
      for (Object doc : getList(Document.type, jcas)) {
        Document d = (Document)doc;
        documents.add(d.getUri());
      }

      List<String> concepts = new ArrayList<String>();
      for (Object doc : getList(Concept.type, jcas)) {
        Concept c = (Concept)doc;
        concepts.add(c.getUris().getNthElement(0)); //get top element
      }

      List<json.gson.Triple> triples = new ArrayList<json.gson.Triple>();
      for (Object doc : getList(Triple.type, jcas)) {
        Triple trip = (Triple)doc;
        json.gson.Triple json_triple = new json.gson.Triple(trip.getObject(), trip.getPredicate(),trip.getSubject());
        triples.add(json_triple);
      }

      List<Snippet> snippets = new ArrayList<Snippet>();
      for (Object doc : getList(Passage.type, jcas)) {
        Snippet s = (Snippet)doc;
        snippets.add(s);
      }

      TestQuestion processed_question = new TestQuestion(id, body, type, documents, snippets, concepts, triples, "");
      processed_questions.add(processed_question);

    } catch (CASException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
          IOException {
    super.collectionProcessComplete(arg0);
    for (int i = 0; i < processed_questions.size(); i++) {
      TestQuestion next = processed_questions.get(i);
      String id = next.getId();
      TestQuestion gold = gold_standard.get(id);
      solveIt(gold, next, "concept");
      solveIt(gold, next, "document");
      solveIt(gold, next, "triple");
      solveIt(gold, next, "snippet");
    }
    e_concept.MAP();
    e_document.MAP();
    e_triple.MAP();
    e_concept.GMAP(EPSILON);
    e_document.GMAP(EPSILON);
    e_triple.GMAP(EPSILON);

    String output = TestSet.dump(processed_questions);
    BufferedWriter writer = new BufferedWriter(new FileWriter(new File(processed_file)));
    writer.write(output);
    System.out.println("\nConcept MAP: " + e_concept.getMAP() + "\nDocument MAP: "
            + e_document.getMAP() + "\nTriple MAP: " + e_triple.getMAP() + "\nConcept GMAP: "
            + e_concept.getGMAP() + "\nDocument GMAP: " + e_document.getGMAP() + "\nTriple GMAP: "
            + e_triple.getGMAP());
    writer.flush();
    writer.close();

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
      question_list = (HashMap<String, TestQuestion>) TestSet.loadMap(fis);
      // TestSet.loadMap(fis);
    } else if (String[].class.isAssignableFrom(value.getClass())) {
      question_list = (HashMap<String, TestQuestion>) Arrays
              .stream(String[].class.cast(value))
              .flatMap(
                      path -> ((Collection<TestQuestion>) TestSet.loadMap(getClass()
                              .getResourceAsStream(path))).stream()).collect(toList());
    }

    return question_list;
  }

  public void solveIt(TestQuestion gold, TestQuestion next, String type) {
    if (type == "concept") {
      List<String> docs = next.getConcepts();
      List<String> goldList = gold.getConcepts();
      if (docs.isEmpty())
        return;
      e_concept.runEvaluation(docs, goldList);
    } else if (type == "document") {
      List<String> docs = next.getDocuments();
      List<String> goldList = gold.getDocuments();
      if (docs.isEmpty())
        return;
      e_document.runEvaluation(docs, goldList);
    } else if (type == "triple") {
      List<json.gson.Triple> docs = next.getTriples();
      List<json.gson.Triple> goldList = gold.getTriples();
      if (docs.isEmpty())
        return;
      e_triple.runEvaluation(docs, goldList);
    } else if (type == "snippet") {
      List<Snippet> docs = next.getSnippets();
      List<Snippet> goldList = gold.getSnippets();
      if (docs.isEmpty())
        return;
      e_snippet.runEvaluation(docs, goldList);
    }

  }

  public List<Object> getList(int type, JCas jcas) {
    List<Object> l = new ArrayList<Object>();
    FSIterator fsi = jcas.getJFSIndexRepository().getAllIndexedFS(type);
    while (fsi.hasNext()) {
      l.add(fsi.next());
    }
    return l;
  }

}
