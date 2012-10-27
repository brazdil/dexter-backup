package uk.ac.cam.db538.dexter.dex.type;

public class UnknownTypeException extends Exception {

	private static final long serialVersionUID = 4123412329982683006L;

	public UnknownTypeException(String typeDescriptor) {
		super("Unknown type: " + typeDescriptor);
	}
	
}
