package util;

import java.util.ArrayList;

public class EvaluationObject {

  private ArrayList<Double> AP;

  private double MAP;

  private double GMAP;

  private ArrayList<Double> precision;

  private ArrayList<Double> recall;

  private ArrayList<Double> F;

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

}