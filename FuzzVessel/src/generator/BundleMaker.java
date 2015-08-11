package generator;

//TODO: Insert here a map or something - a bundle needs to store some values.
public class BundleMaker {

	public BundleMaker(){
		this(10);
	}
	
	public BundleMaker(int contents){
		for(int i=0; i<contents; ++i)
			i++;
	}
}