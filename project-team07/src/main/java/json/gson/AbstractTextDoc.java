package json.gson;

import java.util.HashMap;

public class AbstractTextDoc {
    private String pmid;
    private String Text;
    private HashMap<String,Integer> abstract_title;
    private HashMap<String,Integer> abstract_content;
    private int length;
    private int title_length;
    private double score;
    private double title_score;
    public AbstractTextDoc(String PMID, String content, String title){
    	pmid=PMID;
    	Text = content;
    	abstract_title = new HashMap<String,Integer>();
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
    	
    	title = title.replaceAll("[?.,!:;]", " ").toLowerCase();
    	for(String word : title.split(" ")){
			if(abstract_title.containsKey(word)){
				abstract_title.put(word, abstract_title.get(word)+1);
			}
			else{
				abstract_title.put(word, 1);
			}
		}
    	title_length = title.split(" ").length;
    	score = 0.0;
    	title_score = 0.0;
    }
    public void setScore(double score){
    	this.score = score;
    }
    public void setTitleScore(double score){
    	this.title_score = score;
    }
    public HashMap<String,Integer> getContent(){
    	return abstract_content;
    }
    public HashMap<String,Integer> getTitle(){
    	return abstract_title;
    }
    public int getLength(){
    	return length;
    }
    public double getScore(){
    	return score;
    }
    public double getTitleScore(){
    	return title_score;
    }
    public String getPmid(){
    	return pmid;
    }
    public String getText(){
    	return Text;
    }
}
