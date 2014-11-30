package edu.cmu.lti.oaqa.model;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import util.TypeFactory;
import util.TypeUtil;
import edu.cmu.lti.oaqa.type.answer.Answer;
import edu.cmu.lti.oaqa.type.input.Question;

public class AnswerAE extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		// TODO Auto-generated method stub
		Question question = TypeUtil.getQuestion(jcas);
		String qID = question.getId();
		String qType = question.getQuestionType();
		String qText = question.getText();
		switch (qType){
			case "yes_no":
			  process_YESNO(jcas);
			  break;
			default:
				 process_YESNO(jcas);
				 break;
		}
	}
    public void process_YESNO(JCas jcas){
    	Answer ans = TypeFactory.createAnswer(jcas, "yes");
    	ans.addToIndexes();
    }
}
