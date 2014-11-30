package util;

import java.util.*;

public class PosDict {
	public  HashMap<String, Set<String>> POSfilterword;
	public  HashSet<String> keep;
	public  HashSet<String> discard;
	public PosDict(){
		POSfilterword= new HashMap<String,Set<String>>();
		keep = new  HashSet<String>();
		discard = new  HashSet<String>();
		POSfilterword.put(".",new HashSet<String>());
		POSfilterword.put("CC",new HashSet<String>());
		POSfilterword.put("CS",new HashSet<String>());
		POSfilterword.put("CS+",new HashSet<String>());
		POSfilterword.put("DD",new HashSet<String>());
		POSfilterword.put("EX",new HashSet<String>());
		POSfilterword.put("II",new HashSet<String>());
		POSfilterword.put("JJ",new HashSet<String>());
		POSfilterword.put("MC",new HashSet<String>());
		POSfilterword.put("NN",new HashSet<String>());
		POSfilterword.put("NNP",new HashSet<String>());
		POSfilterword.put("NNS",new HashSet<String>());
		POSfilterword.put("PN",new HashSet<String>());
		POSfilterword.put("PNG",new HashSet<String>());
		POSfilterword.put("PNR",new HashSet<String>());
		POSfilterword.put("RR",new HashSet<String>());
		POSfilterword.put("RRT",new HashSet<String>());
		POSfilterword.put("TO",new HashSet<String>());
		POSfilterword.put("VBB",new HashSet<String>());
		POSfilterword.put("VBZ",new HashSet<String>());
		POSfilterword.put("VHB",new HashSet<String>());
		POSfilterword.put("VM",new HashSet<String>());
		POSfilterword.put("VVB",new HashSet<String>());
		POSfilterword.put("VVGN",new HashSet<String>());
		POSfilterword.put("VVI",new HashSet<String>());
		POSfilterword.put("VVN",new HashSet<String>());
		POSfilterword.put("VVNJ",new HashSet<String>());
		
		String[] yes = {"JJ","NN","NNP","NNS","RR","VVNJ"};
		String[] no = {"CC","CS","CS+","DD","EX","II","MC","PNG","PN","PNR","RRT","TO","VBB","VBZ","VHB","VM","VVB","VVGN","VVI","VVN"};
		POSfilterword.get("JJ").add("do");POSfilterword.get("JJ").add("which");POSfilterword.get("JJ").add("extra");
		POSfilterword.get("NNP").add("can");POSfilterword.get("NNS").add("does");
		POSfilterword.get("RR").add("how");POSfilterword.get("RR").add("do");POSfilterword.get("RR").add("classically");POSfilterword.get("RR").add("strongly");
		for(String pos : yes){
			keep.add(pos);
		}
		for(String pos : no){
			discard.add(pos);
		}
	}
	public boolean Accept(String word, String Pos){
		if(keep.contains(Pos) && !POSfilterword.get(Pos).contains(word)){
			return true;
		}
		else{
			return false;
		}
	}
}
