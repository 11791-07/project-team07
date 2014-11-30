package json.gson;

import java.util.Comparator;

public class CompareAbstract implements Comparator<AbstractTextDoc>{

	@Override
	public int compare(AbstractTextDoc o1, AbstractTextDoc o2) {
		if(o1.getScore()>o2.getScore()){
			return -1;
		}
		else if(o1.getScore()<o2.getScore()){
			return 1;
		}
		else{
			return 0;
		}
	}

}
