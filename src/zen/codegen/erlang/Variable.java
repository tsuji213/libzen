//ifdef JAVA

package zen.codegen.erlang;

import zen.deps.Field;

//endif VAJA

public class Variable {
	@Field public int Read;
	@Field public int Next;
	@Field public boolean IsUsed;
	public Variable/*constructor*/() {
		this.Read = -1;
		this.Next = 0;
		this.IsUsed = false;
	}
}
