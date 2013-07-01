package gizmoe.capabilities;

public class TestCapability extends CapabilityBase{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void body() {
		System.out.println("From TestCapability: thread"+this.hashCode());
	}

}
