package util;

import java.util.ArrayList;
import java.util.List;

import json.gson.Snippet;

public class EvaluationObject {

  private ArrayList<Double> AP;

  private double MAP;

  private double GMAP;

  private ArrayList<Double> precision;

  private ArrayList<Double> softPrecision;

  private ArrayList<Double> recall;

  private ArrayList<Double> softRecall;

  private ArrayList<Double> F;

  private ArrayList<Double> softF;

  private ArrayList<Integer> TP;

  private ArrayList<Integer> FP;

  private ArrayList<Integer> FN;

  public EvaluationObject(ArrayList<Double> AP, double MAP, double GMAP,
          ArrayList<Double> precision, ArrayList<Double> recall, ArrayList<Double> F,
          ArrayList<Integer> TP, ArrayList<Integer> FP, ArrayList<Integer> FN) {
    super();
    this.AP = AP;
    this.MAP = MAP;
    this.GMAP = GMAP;
    this.precision = precision;
    this.recall = recall;
    this.F = F;
    this.TP = TP;
    this.FP = FP;
    this.FN = FN;
  }

  public EvaluationObject() {
    super();
    this.AP = new ArrayList<Double>();
    this.MAP = 0.0;
    this.GMAP = 0.0;
    this.precision = new ArrayList<Double>();
    this.recall = new ArrayList<Double>();
    this.F = new ArrayList<Double>();
    this.TP = new ArrayList<Integer>();
    this.FP = new ArrayList<Integer>();
    this.FN = new ArrayList<Integer>();
  }

  public ArrayList<Double> getAP() {
    return AP;
  }

  public void addAP(double AP) {
    this.AP.add(AP);
  }

  public double getMAP() {
    return MAP;
  }

  public void addMAP(double MAP) {
    this.MAP = MAP;
  }

  public double getGMAP() {
    return GMAP;
  }

  public void addGMAP(double GMAP) {
    this.GMAP = GMAP;
  }

  public ArrayList<Double> getPrecision() {
    return precision;
  }

  public void addPrecision(double precision) {
    this.precision.add(precision);
  }

  public ArrayList<Double> getRecall() {
    return recall;
  }

  public void addRecall(double recall) {
    this.recall.add(recall);
  }

  public ArrayList<Double> getF() {
    return F;
  }

  public void addF(double F) {
    this.F.add(F);
  }

  public ArrayList<Integer> getTP() {
    return TP;
  }

  public void addTP(int TP) {
    this.TP.add(TP);
  }

  public ArrayList<Integer> getFN() {
    return FN;
  }

  public void addFN(int FN) {
    this.FN.add(FN);
  }

  public ArrayList<Integer> getFP() {
    return FP;
  }

  public void addFP(int FP) {
    this.FP.add(FP);
  }

  public void runEvaluation(List<? extends Object> predictions, List<? extends Object> gold) {
    int TP = 0, FP = 0, FN = 0;
    for (Object p : predictions) {
      for (Object g : gold) {
        if (g.equals(p)) { // hard metric
          TP++;
        }
        //else if (){//TODO soft metric
        //}
        else {
          FP++;
        }
      }
    }
    addFN(gold.size() - TP);
    addFP(FP);
    addTP(TP);
    double P = precision(TP, FP);
    double R = recall(TP, FN);
    fMeasure(P, R);
    AP(predictions, gold);
  }

  public double precision(double TP, double FP) {
    double pre = 0.0;
    if (TP + FP != 0) {
      pre = (double) TP / (TP + FP);
    }
    this.addPrecision(pre);
    return pre;
  }

  public double precision(List<? extends Object> predictions, List<? extends Object> gold) {
    int TP = 0, FP = 0;
    for (Object p : predictions) {
      for (Object g : gold) {
        if (g.equals(p)) { // hard metric
          TP++;
        }
        // else if (){//TODO soft metric
        // }
        else {
          FP++;
        }
      }
    }
    if (TP + FP == 0) {
      return 0.0;
    }
    return (double) TP / (TP + FP);

  }

  public double recall(double TP, double FN) {
    double re = 0.0;
    if (TP + FN != 0) {
      re = (double) TP / (TP + FN);
    }
    this.addRecall(re);
    return re;
  }

  public void fMeasure(double P, double R) {
    if (P + R == 0) {
      this.addF(0.0);
    } else {
      this.addF((2 * P * R) / (P + R));
    }
  }

  public void AP(List<? extends Object> predictions, List<? extends Object> gold) {
    double total = 0.0;
    int rel_total = 0;
    for (int r = 0; r < predictions.size(); r++) {
      List<Object> rList = (List<Object>) predictions.subList(0, r + 1);
      int rel = 0;
      Object temp1 = predictions.get(r);
      for (Object g : gold) {
        if (g.equals(temp1)) {
          rel = 1;
          rel_total++;
        }
        /*
         * else if (){//soft metric
         * 
         * }
         */
      }
      double temp = precision(rList, gold);
      total += temp * (double) rel;
    }
    if (rel_total != 0) {
      total /= rel_total;
    } else {
      total = 0.0;
    }
    this.addAP(total);
  }
  
  
  public void SnippetAP(List<Snippet> predictions, List<Snippet> gold) {
    double total = 0.0;
    int rel_total = 0;
    for (int r = 0; r < predictions.size(); r++) {
      List<Snippet> rList = (List<Snippet>) predictions.subList(0, r + 1);
      int rel = 0;
      Snippet temp1 = predictions.get(r);
      int begin = temp1.getOffsetInBeginSection();
      int end = temp1.getOffsetInEndSection();
      for (Snippet g : gold) {
        int g_begin = g.getOffsetInBeginSection();
        int g_end = g.getOffsetInEndSection();
        if (g_begin > begin || end > g_begin){
          rel = 1;
          rel_total++;
        }
        /*
         * else if (){//soft metric
         * 
         * }
         */
      }
      double temp = precision(rList, gold); //TODO fix this to be Snippet precision
      total += temp * (double) rel;
    }
    if (rel_total != 0) {
      total /= rel_total;
    } else {
      total = 0.0;
    }
    this.addAP(total);
  }

  public void MAP() {
    for (int i = 0; i < this.AP.size(); i++) {
      this.MAP += this.AP.get(i);
    }
    if (this.AP.size() == 0) {
      this.MAP = 0.0;
    } else {
      this.MAP /= this.AP.size();
    }
  }

  public void GMAP(double epsilon) {
    double answer = 1.0;
    for (int i = 0; i < this.AP.size(); i++) {
      answer *= (this.AP.get(i) + epsilon);
    }
    if (this.AP.size() == 0) {
      GMAP = 0.0;
    } else {
      GMAP = Math.pow(answer, (1.0 / this.AP.size()));
    }
  }

  public void runSnippetEvaluation(List<Snippet> S, List<Snippet> G) {
    int TP = 0;
    for (Object s : S) {
      for (Object g : G) {
        if (g.equals(s)) { // hard metric
          TP++;
        }
        //else if (){//TODO soft metric
        //}
      }
    }
    double P = (double)TP/S.size();
    double R = (double)TP/G.size();
    double F = (2*P*R)/(P+R);
    this.addPrecision(P);
    this.addRecall(R);
    this.addF(F);
    SnippetAP(S, G);
  }

}