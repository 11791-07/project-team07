package edu.cmu.lti.oaqa.model;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import json.JsonCollectionReaderHelper;
import json.gson.Question;
import json.gson.TestSet;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Progress;

public class CollectionReader extends CollectionReader_ImplBase {
    
	private static int question_index = 0;
	private List<Question> question_list;
	
	public void initialize(){
		
		String filePath = "/questions.json";
		//String filePath = "/questions_single2.json";
		//String filePath = "/ZeroQuestion.json";
		Object value = filePath;
		if (String.class.isAssignableFrom(value.getClass())) {
			question_list = TestSet
					.load(getClass().getResourceAsStream(
							String.class.cast(value))).stream()
					.collect(toList());
		} else if (String[].class.isAssignableFrom(value.getClass())) {
			question_list = Arrays
					.stream(String[].class.cast(value))
					.flatMap(
							path -> TestSet.load(
									getClass().getResourceAsStream(path))
									.stream()).collect(toList());
		}
	}

	@Override
	public void getNext(CAS aJcas) throws IOException, CollectionException {
		// TODO Auto-generated method stub
		Question next = question_list.get(question_index);
		question_index++;
		JCas jcas;
	    try {
	      jcas = aJcas.getJCas();
	    } catch (CASException e) {
	      throw new CollectionException(e);
	    }
		JsonCollectionReaderHelper.addQuestionToIndex(next, "",  jcas);
		

	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public Progress[] getProgress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		// TODO Auto-generated method stub
		return question_index<question_list.size();
	}

}
