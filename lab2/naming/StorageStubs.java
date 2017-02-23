package naming;

import storage.Command;
import storage.Storage;

/**
 * 
 * @author feichao (feqian @ ucsd.edu)
 *
 */

public class StorageStubs {
	private Storage client_stub;
	private Command cmd_stub;
	
	public StorageStubs(Storage client, Command cmd) {
		this.setClient_stub(client);
		this.setCMD_stub(cmd);
	}

	public Storage getClient_stub() {
		return client_stub;
	}

	public void setClient_stub(Storage client_stub) {
		this.client_stub = client_stub;
	}

	public Command getCMD_stub() {
		return cmd_stub;
	}

	public void setCMD_stub(Command storage_stub) {
		this.cmd_stub = storage_stub;
	}
}
