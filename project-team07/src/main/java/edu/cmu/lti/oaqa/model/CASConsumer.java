package edu.cmu.lti.oaqa.model;

import static java.util.stream.Collectors.toList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.json.*;

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

import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Concept;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import util.TypeUtil;

public class CASConsumer extends CasConsumer_ImplBase {

  List<TestQuestion> processed_questions;

  private String processed_file;

  private List<TestQuestion> gold_standard;

  public void initialize() throws ResourceInitializationException {
    processed_questions = new ArrayList<TestQuestion>();
    // processed_file = "src/main/resources/Phase1_output.json";
    processed_file = "src/main/resources/Phase1_output_single.json";
    // gold_standard = "src/main/resources/BioASQ-SampleData1B.json";
    String gold_standard_string = "src/main/resources/BioASQ-SampleData1B_single.json";
    gold_standard = readJSON(gold_standard_string);
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

      // Get concepts retrieved in AE
      List<String> concepts = new ArrayList<String>();
      for (Concept concept : TypeUtil.getConcept(jcas)) {
        concepts.add(concept.getUris().getNthElement(0));
      }

      // Get triples retrieved in AE
      List<Triple> jason_triples = new ArrayList<Triple>();
      for (edu.cmu.lti.oaqa.type.kb.Triple triple : TypeUtil.getTriple(jcas)) {
        Triple jason_triple = new Triple(triple.getObject(), triple.getPredicate(),
                triple.getSubject());
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

  public double precision(double tp, double fp) {
    return tp / (tp + fp);
  }

  public double recall(double tp, double fn) {
    return tp / (tp + fn);
  }

  public double fMeasure(double P, double R) {
    return (2 * P * R) / (P + R);
  }

  public double AP(double[] P, int[] rel, int Lr) {
    double total = 0.0;
    for (int r = 0; r < P.length; r++) {
      total += P[r] * rel[r];
    }
    total /= Lr;
    return total;

  }

  public double MAP(double[] AP) {
    double answer = 0.0;
    for (int i = 0; i < AP.length; i++) {
      answer += AP[i];
    }
    answer /= AP.length;
    return answer;
  }

  public double GMAP(double[] AP, double epsilon) {
    double answer = 1.0;
    for (int i = 0; i < AP.length; i++) {
      answer *= (AP[i] + epsilon);
    }
    return Math.pow(answer, (1 / AP.length));
  }

  public List<TestQuestion> readJSON(String filePath) {
    List<TestQuestion> question_list = null;
    FileInputStream fis = null;
    Object value = filePath;
    try {
      fis = new FileInputStream(filePath);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    if (String.class.isAssignableFrom(value.getClass())) {
      question_list = (List<TestQuestion>) TestSet.load(fis);
    } else if (String[].class.isAssignableFrom(value.getClass())) {
      question_list = Arrays.stream(String[].class.cast(value))
              .flatMap(path -> TestSet.load(getClass().getResourceAsStream(path)).stream())
              .collect(toList());
    }

    return question_list;
  }
}
