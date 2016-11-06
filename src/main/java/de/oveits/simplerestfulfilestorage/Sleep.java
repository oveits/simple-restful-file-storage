package de.oveits.simplerestfulfilestorage;

import org.apache.camel.Header;

public class Sleep {		
	public final void perform(@Header("sleep") final Integer milliseconds) throws InterruptedException  {
		Thread.sleep(milliseconds);
	    }
}
