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
import json.gson.TestQuestion;
import json.gson.TestSet;
import json.gson.Triple;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import util.TypeUtil;
import util.EvaluationObject;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Concept;
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

  public void initialize() throws ResourceInitializationException {
    processed_questions = new ArrayList<TestQuestion>();
    // processed_file = "src/main/resources/Phase1_output.json";
    processed_file = "src/main/resources/Phase1_output_single.json";
    // gold_standard = "src/main/resources/BioASQ-SampleData1B.json";
    String gold_standard_string = "src/main/resources/BioASQ-SampleData1B_single.json";
    gold_standard = readJSON(gold_standard_string);
    e_concept = new EvaluationObject();
    e_document = new EvaluationObject();
    e_triple = new EvaluationObject();
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
      for (Document doc : TypeUtil.getRankedDocuments(jcas)) {
        documents.add(doc.getUri());
      }

      // Get snippets retrieved in AE. Just make it empty, I did not extract snippets in dummy AE
      List<Snippet> snippets = new ArrayList<Snippet>();
      /*
       * for (Snippet snip : TypeUtil.gegetRankedPassages(jcas)) { snippets.add(snip); }
       */

      // Get concepts retrieved in AE
      List<String> concepts = new ArrayList<String>();
      for (ConceptSearchResult concept : TypeUtil.getRankedConceptSearchResults(jcas)) {
        concepts.add(concept.getUri());
      }

      // Get triples retrieved in AE
      List<Triple> jason_triples = new ArrayList<Triple>();
      for (TripleSearchResult triple : TypeUtil.getRankedTripleSearchResults(jcas)) {
        Triple jason_triple = new Triple(triple.getTriple().getObject(), triple.getTriple()
                .getPredicate(), triple.getTriple().getSubject());
        jason_triples.add(jason_triple);
      }

      // Construct processed question object from information above then put it into list.
      // I changed the constructor of TestQuestion from protected to public so we can initialize it
      // here.
      TestQuestion processed_question = new TestQuestion(id, body, type, documents, snippets,
              concepts, jason_triples, "");
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
    }
    double MAP_concept = MAP(e_concept.getAP());
    double MAP_document = MAP(e_document.getAP());
    double MAP_triple = MAP(e_triple.getAP());
    System.out.println("Concept MAP: " + MAP_concept);
    System.out.println("Document MAP: " + MAP_document);
    System.out.println("Triple MAP: " + MAP_triple);
    
    String output = TestSet.dump(processed_questions);
    BufferedWriter writer = new BufferedWriter(new FileWriter(new File(processed_file)));
    writer.write(output);
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

  /*
   * public double precision(double tp, double fp) { return tp / (tp + fp); }
   */

  public double precision(List<String> predictions, List<String> gold) {
    int TP = 0, FP = 0;
    for (String p : predictions) {
      if (gold.contains(p)) {
        TP++;
      } else {
        FP++;
      }
    }
    return TP / (TP + FP);
  }

  public double triplePrecision(List<Triple> predictions, List<Triple> gold) {
    int TP = 0, FP = 0;
    boolean found = false;
    for (Triple t : predictions) {
      for (Triple g : gold) {
        if (g.getO() == t.getO() && g.getP() == t.getP() && g.getS() == t.getS()) {
          TP++;
          found = true;
          break;
        }
        if (!found) {
          FP++;
        }
        found = false;
      }
    }
    return TP / (TP + FP);
  }

  public double recall(List<String> predictions, List<String> gold) {
    int TP = 0;
    for (String p : predictions) {
      if (gold.contains(p)) {
        TP++;
      }
    }
    int FN = gold.size() - TP;
    return TP / (TP + FN);
  }

  public double tripleRecall(List<Triple> predictions, List<Triple> gold) {
    int TP = 0;
    for (Triple t : predictions) {
      for (Triple g : gold) {
        if (g.getO() == t.getO() && g.getP() == t.getP() && g.getS() == t.getS()) {
          TP++;
        }
      }
    }
    int FN = gold.size() - TP;
    return TP / (TP + FN);
  }

  public double fMeasure(double P, double R) {
    return (2 * P * R) / (P + R);
  }

  public double AP(List<String> predictions, List<String> gold) {
    double total = 0.0;
    int rel_total = 0;
    for (int r = 0; r < predictions.size(); r++) {
      List<String> rList = predictions.subList(0, r);
      int rel = 0;
      if (gold.contains(predictions.get(r))) {
        rel = 1;
        rel_total++;
      }
      total += precision(rList, gold) * rel;
    }
    total /= rel_total;
    return total;

  }

  public double tripleAP(List<Triple> predictions, List<Triple> gold) {
    double total = 0.0;
    int rel_total = 0;
    for (int r = 0; r < predictions.size(); r++) {
      List<Triple> rList = predictions.subList(0, r);
      int rel = 0;
      Triple t = predictions.get(r);
      for (Triple g : gold) {
        if (g.getO() == t.getO() && g.getP() == t.getP() && g.getS() == t.getS()) {
          rel = 1;
          rel_total++;
        }
      }
      total += triplePrecision(rList, gold) * rel;
    }
    total /= rel_total;
    return total;

  }

  public double MAP(ArrayList<Double> AP) {
    double answer = 0.0;
    for (int i = 0; i < AP.size(); i++) {
      answer += AP.get(i);
    }
    answer /= AP.size();
    return answer;
  }

  public double GMAP(double[] AP, double epsilon) {
    //TODO: Fix this
    double answer = 1.0;
    for (int i = 0; i < AP.length; i++) {
      answer *= (AP[i] + epsilon);
    }
    return Math.pow(answer, (1 / AP.length));
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
    int TP = 0;
    int FP = 0;
    if (type == "concept") {
      List<String> docs = next.getConcepts();
      List<String> goldList = gold.getConcepts();
      for (String c : docs) {
        if (goldList.contains(c)) {
          TP++;
        } else {
          FP++;
        }
      }
      e_concept.addTP(TP);
      e_concept.addFP(FP);
      int FN = goldList.size() - TP;
      e_concept.addFN(FN);
      double p = precision(docs, goldList);
      e_concept.addPrecision(p); // precision
      double r = recall(docs, goldList);
      e_concept.addRecall(r); // recall
      e_concept.addF(fMeasure(p, r)); // f-measure
      e_concept.addAP(AP(docs, goldList));

    } else if (type == "document") {

      List<String> docs = next.getDocuments();
      List<String> goldList = gold.getDocuments();
      for (String c : docs) {
        if (goldList.contains(c)) {
          TP++;
        } else {
          FP++;
        }
      }
      e_document.addTP(TP);
      e_document.addFP(FP);
      int FN = goldList.size() - TP;
      e_document.addFN(FN);
      double p = precision(docs, goldList);
      e_document.addPrecision(p); // precision
      double r = recall(docs, goldList);
      e_document.addRecall(r); // recall
      e_document.addF(fMeasure(p, r)); // f-measure
      e_document.addAP(AP(docs, goldList));

    } else if (type == "triple") {

      List<Triple> docs = next.getTriples();
      List<Triple> goldList = gold.getTriples();
      for (String c : next.getConcepts()) {
        if (gold.getConcepts().contains(c)) {
          TP++;
        } else {
          FP++;
        }
      }
      e_concept.addTP(TP);
      e_concept.addFP(FP);
      int FN = goldList.size() - TP;
      e_concept.addFN(FN);
      double p = triplePrecision(docs, goldList);
      e_concept.addPrecision(p); // precision
      double r = tripleRecall(docs, goldList);
      e_concept.addRecall(r); // recall
      e_concept.addF(fMeasure(p, r)); // f-measure
      e_concept.addAP(tripleAP(docs, goldList));
    }
  }

}
