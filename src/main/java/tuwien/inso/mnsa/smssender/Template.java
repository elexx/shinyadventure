package tuwien.inso.mnsa.smssender;

public class Template {
	private final String destinationNumber, text;
	
	public Template(String destinationNumber, String text) {
		this.destinationNumber = destinationNumber;
		this.text = text;
	}
	
	public String getDestinationNumber() {
		return destinationNumber;
	}
	
	public String getText() {
		return text;
	}
}
