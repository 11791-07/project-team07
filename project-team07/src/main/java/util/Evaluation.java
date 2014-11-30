package util;

import java.util.*;

import json.gson.Snippet;
import json.gson.TestQuestion;
import json.gson.Triple;

public class Evaluation {
	private List<TestQuestion> processed_questions;
	private HashMap<String, TestQuestion> gold_standard;
	
	
    public Evaluation(List<TestQuestion> processed_questions, HashMap<String, TestQuestion> gold_standard){
	      this.processed_questions = processed_questions;
	      this.gold_standard = gold_standard;
	}
    
    
    public double DocMAP(boolean DEBUG){
    	List<Double> precision = new ArrayList<Double>();
    	List<Double> recall = new ArrayList<Double>();
    	List<Double> average_precision = new ArrayList<Double>();
    	for(TestQuestion question : processed_questions){
    		TestQuestion goldanswer = gold_standard.get(question.getId());
    		List<String> hyp_doc = question.getDocuments();
    		List<String> ref_doc = goldanswer.getDocuments();
    		double[] PR = PresicionRecall(String.class, hyp_doc, ref_doc);
    		double   AP = AveragePrecision(String.class,hyp_doc, ref_doc);
    		precision.add(PR[0]);  
    		recall.add(PR[1]);
    		average_precision.add(AP);
    	}
    	double MAP = 0;
    	for(Double AP : average_precision){
    		MAP = MAP + AP;
    	}
    	MAP = MAP/average_precision.size();
    	if(DEBUG){
    		PrintDebug("document",precision,recall,average_precision);
    	}
    	return MAP;
    }
    
    public double ConceptMAP(boolean DEBUG){
    	List<Double> precision = new ArrayList<Double>();
    	List<Double> recall = new ArrayList<Double>();
    	List<Double> average_precision = new ArrayList<Double>();
    	
    	for(TestQuestion question : processed_questions){
    		TestQuestion goldanswer = gold_standard.get(question.getId());
    		List<String> hyp_concept = question.getConcepts();
    		List<String> ref_concept = goldanswer.getConcepts();
    		double[] PR = PresicionRecall(String.class, hyp_concept, ref_concept);
    		double   AP = AveragePrecision(String.class,hyp_concept, ref_concept);
    		precision.add(PR[0]);  
    		recall.add(PR[1]);
    		average_precision.add(AP);
    	}
    	double MAP = 0;
    	for(Double AP : average_precision){
    		MAP = MAP + AP;
    	}
    	MAP = MAP/average_precision.size();
    	if(DEBUG){
    		PrintDebug("concpet",precision,recall,average_precision);
    	}
    	return MAP;
    }

    public double TripleMAP(boolean DEBUG){
    	
    	List<Double> precision = new ArrayList<Double>();
    	List<Double> recall = new ArrayList<Double>();
    	List<Double> average_precision = new ArrayList<Double>();
    	int counter = 0;
    	for(TestQuestion question : processed_questions){
    		
    		TestQuestion goldanswer = gold_standard.get(question.getId());
    		List<Triple> hyp_triple = question.getTriples();
    		List<Triple> ref_triple = goldanswer.getTriples();
    		if(ref_triple==null || ref_triple.size()==0 || hyp_triple==null){
    			counter++;
    			precision.add(0.0);  
        		recall.add(0.0);
        		average_precision.add(0.0);
        		continue;
    		}
    		double[] PR = PresicionRecall(Triple.class, hyp_triple, ref_triple);
    		double   AP = AveragePrecision(Triple.class,hyp_triple, ref_triple);
    		precision.add(PR[0]);  
    		recall.add(PR[1]);
    		average_precision.add(AP);
    	}
    	
    	double MAP = 0;
    	for(Double AP : average_precision){
    		MAP = MAP + AP;
    	}
    	MAP = MAP/(average_precision.size()-counter);
    	if(DEBUG){
    		PrintDebug("triple",precision,recall,average_precision);
    	}
    	return MAP;
    }
    
    public double SnippetMAP(boolean DEBUG){
    	
    	List<Double> precision = new ArrayList<Double>();
    	List<Double> recall = new ArrayList<Double>();
    	List<Double> average_precision = new ArrayList<Double>();
    	for(TestQuestion question : processed_questions){
    		TestQuestion goldanswer = gold_standard.get(question.getId());
    		List<Snippet> hyp_snippet = question.getSnippets();
    		List<Snippet> ref_snippet = goldanswer.getSnippets();
    		double[] PR = PresicionRecall(Snippet.class, hyp_snippet, ref_snippet);
    		double   AP = AveragePrecision(Snippet.class,hyp_snippet, ref_snippet);
    		precision.add(PR[0]);  
    		recall.add(PR[1]);
    		average_precision.add(AP);
    	}
    	
    	double MAP = 0;
    	for(Double AP : average_precision){
    		MAP = MAP + AP;
    	}
    	MAP = MAP/average_precision.size();
    	if(DEBUG){
    		PrintDebug("Snippet",precision,recall,average_precision);
    	}
    	return MAP;
    }
    
    
    //String List Precision and Recall
    public double[] PresicionRecall(Class c, List hyp_doc, List ref_doc){
    	int TP = 0;
        if(c==String.class){
        	hyp_doc = (List<String>) hyp_doc;
        	ref_doc = (List<String>) ref_doc;
        	return PrecisionRecallString(hyp_doc,ref_doc);
        }
        else if(c==Triple.class){
        	hyp_doc = (List<Triple>) hyp_doc;
        	ref_doc = (List<Triple>) ref_doc;
        	return PrecisionRecallTriple(hyp_doc,ref_doc);
        }
        else if(c==Snippet.class){
        	hyp_doc = (List<Snippet>) hyp_doc;
        	ref_doc = (List<Snippet>) ref_doc;
        	return PrecisionRecallSnippet(hyp_doc,ref_doc);
        }
        else{
        	System.out.println("EVALUATION TYPE NOT DEFINED");
        	return (new double[2]);
        }
    }
    
    public double[] PrecisionRecallString(List<String> hyp_doc, List<String> ref_doc){
    	int TP = 0;
    	double[] res = new double[2];
    	for(String s : hyp_doc){
    		for(String t : ref_doc){
    			if(s.equals(t)){
    				TP++;
    			}
    		}
    	}
    	
    	res[0] = (TP+0.0)/hyp_doc.size();
    	res[1] = (TP+0.0)/ref_doc.size();
    	return res;
    }
    
    public double[] PrecisionRecallTriple(List<Triple> hyp_triple, List<Triple> ref_triple){
    	int TP = 0;
    	double[] res = new double[2];
    	for(Triple s : hyp_triple){
    		for(Triple t : ref_triple){
    			if(s.getO()==null || s.getP()==null || s.getS()==null){
    				continue;
    			}
    			if(s.getO().equals(t.getO()) && s.getP().equals(t.getP()) && s.getS().equals(t.getS())){
    				TP++;
    			}
    		}
    	}
    	
    	res[0] = (TP+0.0)/hyp_triple.size();
    	res[1] = (TP+0.0)/ref_triple.size();
    	return res;
    }
    
    public double[] PrecisionRecallSnippet(List<Snippet> hyp_snippet, List<Snippet> ref_snippet){
    	double TP = 0;
    	int S = 0;
    	int G = 0;
    	double[] res = new double[2];
    	for(Snippet s : hyp_snippet){
    		String docuri = s.getDocument();
    		String BeginSection = s.getBeginSection();
    		String EndSection = s.getEndSection();
    		int BeginOffset = s.getOffsetInBeginSection();
    		int EndOffset = s.getOffsetInEndSection();
    		S = S + EndOffset - BeginOffset +1;
    		String text =s.getText();
    		for(Snippet t : ref_snippet){
    			if(t.getDocument().equals(docuri) && t.getBeginSection().equals(BeginSection) && t.getEndSection().equals(EndSection)){
    				int t_BeginOffset = t.getOffsetInBeginSection();
    				int t_EndOffset = t.getOffsetInEndSection();
    				TP = TP + Overlap(BeginOffset,EndOffset,t_BeginOffset,t_EndOffset);
    			}
    		}
    	}
    	for(Snippet t: ref_snippet){
    		int BeginOffset = t.getOffsetInBeginSection();
    		int EndOffset = t.getOffsetInEndSection();
    		G = G + EndOffset - BeginOffset +1;
    	}
    	res[0] = TP/S;
    	res[1] = TP/G;
    	return res;
    }
    
    public int Overlap(int i_left, int i_right, int j_left, int j_right){
    	if(i_left>j_left){
    		return Overlap(j_left,j_right,i_left,i_right);
    	}
    	if(j_left>i_right){
    		return 0;
    	}
    	else{
    		return Math.min(i_right,j_right)-j_left+1;
    	}
    }
    
    
    public double AveragePrecision(Class c, List hyp_doc, List ref_doc){
    	int TP = 0;
        if(c==String.class){
        	hyp_doc = (List<String>) hyp_doc;
        	ref_doc = (List<String>) ref_doc;
        	return AveragePrecisionString(hyp_doc,ref_doc);
        }
        else if(c==Triple.class){
        	hyp_doc = (List<Triple>) hyp_doc;
        	ref_doc = (List<Triple>) ref_doc;
        	return AveragePrecisionTriple(hyp_doc,ref_doc);
        }
        else if(c==Snippet.class){
        	hyp_doc = (List<Snippet>) hyp_doc;
        	ref_doc = (List<Snippet>) ref_doc;
        	return AveragePrecisionSnippet(hyp_doc,ref_doc);
        }
        else{
        	System.out.println("EVALUATION TYPE NOT DEFINED");
        	return -1;
        }
    }
    
    public double AveragePrecisionString(List<String> hyp_doc, List<String> ref_doc){
    	double[] P_r = new double[hyp_doc.size()];
    	int[] Rel_r = new int[hyp_doc.size()];
    	
    	for(int i=0;i<hyp_doc.size();i++){
    		String target = hyp_doc.get(i);
    		for(String ans : ref_doc){
    			if(target.equals(ans)){
    				Rel_r[i] = 1;
    				break;
    			}
    		}
    		P_r[i] = Rel_r[i] + (i==0? 0 : P_r[i-1]);
    	}
    	double AP = 0;
    	for(int i=0;i<hyp_doc.size();i++){
    		P_r[i] = P_r[i]/(i+1);
    		AP = AP + P_r[i]*Rel_r[i];
    	}
    	AP = AP/ref_doc.size();
    	return AP;
    }
    
    public double AveragePrecisionTriple(List<Triple> hyp_triple, List<Triple> ref_triple){
    	double[] P_r = new double[hyp_triple.size()];
    	if(ref_triple==null || ref_triple.size()==0){
    		return 0;
    	}
    	int[] Rel_r = new int[hyp_triple.size()];
    	for(int i=0;i<hyp_triple.size();i++){
    		Triple s = hyp_triple.get(i);
    		for(Triple t : hyp_triple){
    			if(s.getO()==null || s.getP()==null || s.getS()==null){
    				continue;
    			}
    			if(s.getO().equals(t.getO()) && s.getP().equals(t.getP()) && s.getS().equals(t.getS())){
    				Rel_r[i] = 1;
    				break;
    			}
    		}
    		P_r[i] = Rel_r[i] + (i==0? 0 : P_r[i-1]);
    	}
    	double AP = 0;
    	for(int i=0;i<hyp_triple.size();i++){
    		P_r[i] = P_r[i]/(i+1);
    		AP = AP + P_r[i]*Rel_r[i];
    	}
    	AP = AP/ref_triple.size();
    	return AP;
    }
    
    public double AveragePrecisionSnippet(List<Snippet> hyp_snippet, List<Snippet> ref_snippet){
    	double[] P_r = new double[hyp_snippet.size()];
    	double[] S_r = new double[hyp_snippet.size()];
    	int G = 0;
    	int[] Rel_r = new int[hyp_snippet.size()];
    	for(int i=0;i<hyp_snippet.size();i++){
    		Snippet s = hyp_snippet.get(i);
    		for(Snippet t : ref_snippet){
    			if(s.getDocument().equals(t.getDocument()) && s.getBeginSection().equals(t.getBeginSection()) && s.getEndSection().equals(t.getEndSection())){
    				Rel_r[i] = Rel_r[i] + Overlap(s.getOffsetInBeginSection(),s.getOffsetInEndSection(),t.getOffsetInBeginSection(),t.getOffsetInEndSection());
    			}
    		}
    		P_r[i] = Rel_r[i] + (i==0? 0 : P_r[i-1]);
    		S_r[i] = s.getOffsetInEndSection()-s.getOffsetInBeginSection()+1 + (i==0? 0 : S_r[i-1]);
    	}
    	for(Snippet t : ref_snippet){
    		G = G + t.getOffsetInEndSection()-t.getOffsetInEndSection()+1;
    	}
    	double AP = 0;
    	/*
    	for(int i=0;i<hyp_snippet.size();i++){
    		System.out.print(P_r[i]+"    "+S_r[i]);
    	}
    	*/
    	for(int i=0;i<hyp_snippet.size();i++){
    		P_r[i] = P_r[i]/S_r[i];
    		AP = AP + P_r[i]*(Rel_r[i]==0? 0 :1);
    	}
    	AP = AP/G;
    	return AP;
    }
    
    public void PrintDebug(String type, List<Double> precision, List<Double> recall, List<Double> average_precision ){
    	System.out.println("*********"+type+" Retrieval Summary For Each Question**************");
    	System.out.println("Precision: ");
    	System.out.print("[");
    	for(int i=0; i < precision.size()-1;i++){
    		System.out.print(precision.get(i)+",  ");
    	}
    	System.out.print(precision.get(precision.size()-1)+"]");
    	System.out.println();
    	
    	
    	System.out.println("Recall: ");
    	System.out.print("[");
    	for(int i=0; i < recall.size()-1;i++){
    		System.out.print(recall.get(i)+",  ");
    	}
    	System.out.print(recall.get(recall.size()-1)+"]");
    	System.out.println();
    	
    	System.out.println("Average Precision: ");
    	System.out.print("[");
    	for(int i=0; i < average_precision.size()-1;i++){
    		System.out.print(average_precision.get(i)+",  ");
    	}
    	System.out.print(average_precision.get(average_precision.size()-1)+"]");
    	System.out.println();
    }
	//

}
