package json.gson;

import java.util.List;

public class FullTextDoc {
	private String pmid;
	private List<String> sections;
	private String title;
	
	public FullTextDoc(String pmid, List<String> sections, String title){
		this.pmid = pmid;
		this.sections = sections;
		this.title = title;
	}
     
	@Override
	  public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((pmid == null) ? 0 : pmid.hashCode());
	    result = prime * result + ((sections == null) ? 0 : sections.hashCode());
	    result = prime * result + ((title == null) ? 0 : title.hashCode());
	    return result;
	  }
	
	@Override
	  public boolean equals(Object obj) {
	    if (this == obj)
	      return true;
	    if (obj == null)
	      return false;
	    if (getClass() != obj.getClass())
	      return false;
	    FullTextDoc other = (FullTextDoc) obj;
	    if (pmid == null) {
	      if (other.pmid != null)
	        return false;
	    } else if (!pmid.equals(other.pmid))
	      return false;
	    if (sections == null) {
	      if (other.sections != null)
	        return false;
	    } else if (!sections.equals(other.sections))
	      return false;
	    if (title == null) {
	      if (other.title != null)
	        return false;
	    } else if (!title.equals(other.title))
	      return false;
	    return true;
	  }
	
	public String getPMID(){
		return this.pmid;
	}
	public List<String> getSections(){
		return this.sections;
	}
	public String getTitle(){
		return this.title;
	}
}
