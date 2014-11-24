package json.gson;

import java.util.HashMap;

public class AbstractTextDoc {
    private String pmid;
    private String Text;
    private HashMap<String,Integer> abstract_content;
    private int length;
    private double score;
    public AbstractTextDoc(String PMID, String content){
    	pmid=PMID;
    	Text = content;
    	abstract_content=new HashMap<String,Integer>();
    	content = content.replaceAll("[?.,!:;]", " ").toLowerCase();
    	for(String word : content.split(" ")){
			if(abstract_content.containsKey(word)){
				abstract_content.put(word, abstract_content.get(word)+1);
			}
			else{
				abstract_content.put(word, 1);
			}
		}
    	length = content.split(" ").length;
    	score = 0.0;
    }
    public void setScore(double score){
    	this.score = score;
    }
    public HashMap<String,Integer> getContent(){
    	return abstract_content;
    }
    public int getLength(){
    	return length;
    }
    public double getScore(){
    	return score;
    }
    public String getPmid(){
    	return pmid;
    }
    public String getText(){
    	return Text;
    }
}
