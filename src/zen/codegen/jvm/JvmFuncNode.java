package zen.codegen.jvm;

import zen.ast.ZNode;
import zen.type.ZFuncType;

class JvmFuncNode extends ZNode {
	String FuncName;
	String ReferenceName;
	String ClassName;
	String FieldDesc;
	Class<?> FuncClass;
	JvmFuncNode(ZNode ParentNode, ZFuncType FuncType, String FuncName) {
		super(ParentNode, null);
		this.Type = FuncType;
		this.FuncName = FuncName;
		this.ReferenceName = FuncType.StringfySignature(FuncName);
		this.ClassName = "C" + this.ReferenceName;
		this.FieldDesc = null;
		this.FuncClass = null;
	}

	@Override public final String GetVisitName() {
		return "VisitJvmFuncNode";
	}


}
