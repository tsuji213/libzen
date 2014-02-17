package zen.codegen.erlang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import zen.ast.ZBinaryNode;
import zen.ast.ZBlockNode;
import zen.ast.ZBreakNode;
import zen.ast.ZComparatorNode;
import zen.ast.ZEmptyNode;
import zen.ast.ZFuncCallNode;
import zen.ast.ZFuncDeclNode;
import zen.ast.ZGetLocalNode;
import zen.ast.ZIfNode;
import zen.ast.ZNode;
import zen.ast.ZNullNode;
import zen.ast.ZParamNode;
import zen.ast.ZReturnNode;
import zen.ast.ZSetLocalNode;
import zen.ast.ZWhileNode;
import zen.parser.ZSourceGenerator;
@SuppressWarnings("unchecked")
public class ErlSourceCodeGenerator extends ZSourceGenerator {
	public ArrayList <String> Variables;
	//	public ZNode[] NodeList = new ZNode[10];
	//	public String[] NodeName = new String[10];
	//	public int NodeListKey = 0;
	private int WhileNodeNumber = 0;
	private String WhileNodeName;
	private int VarNumber = 0;
	HashMap<String,Object> VarMap = new HashMap<String,Object>();
	//	Stack VarStack = new Stack();
	//HashMap<String,Integer> VarCloneMap = new HashMap<String,Integer>(this.VarMap);
	private int ifdepthlevel = 0;
	private int flag_return = 0;
	ZNode leftnode;
	ZNode rightnode;

	public ErlSourceCodeGenerator/*constructor*/() {
		super("erlang","5.10.4");
	}

	@Override public void VisitFuncDeclNode(ZFuncDeclNode Node){
		this.Variables = new ArrayList<String>();

		this.CurrentBuilder.Append("-module("+Node.FuncName+").");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.Append("-export(["+Node.FuncName+"/"+Node.ArgumentList.size()+"]).");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.Append("main");
		this.CurrentBuilder.Append("(");
		this.ParamList(Node);
		this.CurrentBuilder.Append(")");
		this.CurrentBuilder.Append("->");
		this.CurrentBuilder.AppendLineFeed();

		int size = Node.ArgumentList.size();
		int i=0;
		while(i < size) {
			this.CurrentBuilder.Indent();
			this.CurrentBuilder.AppendIndent();
			ZParamNode param = (ZParamNode)Node.ArgumentList.get(i);
			this.CurrentBuilder.Append(param.Name.toUpperCase()+this.VarNumber);
			this.CurrentBuilder.Append(" = ");
			this.CurrentBuilder.Append(param.Name.toUpperCase());
			this.CurrentBuilder.Append(",");
			this.CurrentBuilder.AppendLineFeed();
			this.CurrentBuilder.UnIndent();
			this.VarMap.put(param.Name,new Stack<Object>());
			((Stack<Object>) this.VarMap.get(param.Name)).push(this.VarNumber);
			this.Variables.add(param.Name);
			i += 1;
		}
		//		this.VarNumber++;

		this.CurrentBuilder.AppendLineFeed();
		this.GenerateCode(Node.BodyNode);

		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.Append(Node.FuncName);
		this.CurrentBuilder.Append("(");
		this.ParamList(Node);
		this.CurrentBuilder.Append(")");
		this.CurrentBuilder.Append("->");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.Indent();
		this.CurrentBuilder.AppendIndent();
		this.GenerateTryNode("main",Node);
		this.CurrentBuilder.Append(".");

		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.Append("whileinfunc(F,Ln,Rn) ->");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.Indent();
		this.CurrentBuilder.AppendIndent();
		this.GenerateTryNode("F","Ln","Rn");
		this.CurrentBuilder.Append(".");

		System.out.println(this.CurrentBuilder);
	}


	private final boolean DoesNodeExist(ZNode Node){
		return Node != null && !(Node instanceof ZEmptyNode);
	}

	private void ParamList(ZFuncDeclNode Node){
		int size = Node.ArgumentList.size();
		int i=0;
		while(i < size) {
			if(i != 0) {
				this.CurrentBuilder.Append(this.Camma);
			}
			ZParamNode param = (ZParamNode)Node.ArgumentList.get(i);
			this.CurrentBuilder.Append(param.Name.toUpperCase());
			i += 1;
		}
	}

	@Override
	public void VisitWhileNode(ZWhileNode Node) {
		//		this.flag_while = 1;
		this.WhileNodeNumber += 1;
		this.WhileNodeName = "WhileFunc_"+Integer.toString(this.WhileNodeNumber);
		this.CurrentBuilder.Append(this.WhileNodeName + " = fun(F,Ln,Rn) when ");
		this.VisitWhileComparatorNode((ZComparatorNode) Node.CondNode);
		//Node.CondNode.Accept(this);
		this.CurrentBuilder.Append(" ->");
		this.CurrentBuilder.AppendLineFeed();

		this.VisitWhileBlockNode((ZBlockNode)Node.BodyNode);

		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append("F(F,Ln_loop,Rn_loop)");
		//Node.BodyNode.Accept(this);
		this.CurrentBuilder.Indent();
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.UnIndent();
		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append("end,");

		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.AppendLineFeed();

		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append("whileinfunc("+this.WhileNodeName+",Ln_in,Rn_in)");
	}

	@Override public void VisitBreakNode(ZBreakNode Node) {
		this.CurrentBuilder.Append("throw(break)");
	}

	@Override public void VisitIfNode(ZIfNode Node) {
		//HashMap<String,Object> hoge = new HashMap<String,Object>();
		//hoge.put("a","a");
		this.ifdepthlevel ++;
		for(String var: this.Variables){
			int varams = (Integer) ((Stack<Object>) this.VarMap.get(var)).peek();
			((Stack<Object>) this.VarMap.get(var)).push(varams);
		}
		//this.VarCloneMap.putAll(this.VarMap);

		this.CurrentBuilder.Append("if");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.AppendIndent();
		Node.CondNode.Accept(this);
		this.CurrentBuilder.Append(" ->");
		this.CurrentBuilder.AppendLineFeed();
		this.VisitPetternBlockNode((ZBlockNode)Node.ThenNode);
		//Node.ThenNode.Accept(this);
		if(this.DoesNodeExist(Node.ElseNode)){
			if(Node.ElseNode instanceof ZIfNode){
				this.VisitElseIfNode((ZIfNode)Node.ElseNode);
			}else if(Node.ElseNode instanceof ZBlockNode){
				this.VisitElseNode((ZBlockNode)Node.ElseNode);
			}
		}else{
			this.CurrentBuilder.AppendIndent();
			this.CurrentBuilder.Append("true -> ok");
			this.CurrentBuilder.AppendLineFeed();
		}
		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append("end");
		//		for(String var: this.Variables){
		//			((Stack<Object>) this.VarMap.get(var)).pop();
		//		}
		this.ifdepthlevel = 0;
	}

	public void VisitElseIfNode(ZIfNode Node) {
		this.ifdepthlevel = 1;
		for(String var: this.Variables){
			((Stack<Object>) this.VarMap.get(var)).pop();
			int varams = (Integer) ((Stack<Object>) this.VarMap.get(var)).peek();
			((Stack<Object>) this.VarMap.get(var)).push(varams);
		}
		//this.VarCloneMap.putAll(this.VarMap);
		this.CurrentBuilder.AppendIndent();
		Node.CondNode.Accept(this);
		this.CurrentBuilder.Append(" ->");
		this.CurrentBuilder.AppendLineFeed();
		//		if(!this.DoesNodeExist(Node.ElseNode)) {
		//			this.flag_case = 0;
		//		}
		this.VisitPetternBlockNode((ZBlockNode)Node.ThenNode);
		//Node.ThenNode.Accept(this);
		if(this.DoesNodeExist(Node.ElseNode)){
			if(Node.ElseNode instanceof ZIfNode){
				this.VisitElseIfNode((ZIfNode)Node.ElseNode);
			}else if(Node.ElseNode instanceof ZBlockNode){
				this.VisitElseNode((ZBlockNode)Node.ElseNode);
			}
		}else{
			this.CurrentBuilder.AppendIndent();
			this.CurrentBuilder.Append("true -> ok");
			this.CurrentBuilder.AppendLineFeed();
		}
		this.ifdepthlevel = 0;
	}

	public void VisitElseNode(ZBlockNode Node) {
		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append("true ->");
		this.CurrentBuilder.AppendLineFeed();
		this.VisitFinishPetternBlockNode(Node);
		//Node.Accept(this);
	}

	//		@Override public void VisitConstNode(ZConstNode Node) {
	//	//		this.CurrentBuilder.Append(Node.Token.ParsedText);
	//		}

	@Override public void VisitNullNode(ZNullNode Node) {
		this.CurrentBuilder.Append(this.NullLiteral);
	}

	@Override public void VisitBinaryNode(ZBinaryNode Node) {
		Node.LeftNode.Accept(this);
		this.CurrentBuilder.Append(Node.SourceToken.ParsedText);
		Node.RightNode.Accept(this);
	}


	@Override public void VisitReturnNode(ZReturnNode Node) {
		this.flag_return = 1;
		this.CurrentBuilder.Append("throw(");
		this.GenerateCode(Node.ValueNode);
		this.CurrentBuilder.Append(")");
		this.flag_return = 0;
	}

	public void VisitWhileComparatorNode(ZComparatorNode Node) {
		this.leftnode = Node.LeftNode;
		//this.CurrentBuilder.Append(this.leftnode.VarName.toUpperCase());
		this.rightnode = Node.RightNode;
		//this.CurrentBuilder.Append(this.leftnode.VarName.toUpperCase());
		//		this.GenerateCode(Node.LeftNode);
		this.CurrentBuilder.Append("Ln");
		this.CurrentBuilder.AppendToken(Node.SourceToken.ParsedText);
		this.CurrentBuilder.Append("Rn");
		//		this.GenerateCode(Node.RightNode);
	}

	//	@Override public void VisitComparatorNode(ZComparatorNode Node) {
	//		this.leftnode = Node.LeftNode;
	//		//this.CurrentBuilder.Append(this.leftnode.VarName.toUpperCase());
	//		this.rightnode = Node.RightNode;
	//		//this.CurrentBuilder.Append(this.leftnode.VarName.toUpperCase());
	//		//		this.GenerateCode(Node.LeftNode);
	//		this.CurrentBuilder.Append("Ln");
	//		this.CurrentBuilder.AppendToken(Node.SourceToken.ParsedText);
	//		this.CurrentBuilder.Append("Rn");
	//		//		this.GenerateCode(Node.RightNode);
	//	}

	@Override
	public void VisitGetLocalNode(ZGetLocalNode Node) {
		if(Node.ParentNode instanceof ZFuncCallNode){
			this.CurrentBuilder.Append(Node.VarName);
			return;
		}
		if(this.ifdepthlevel > 0){
			if(this.flag_return == 1){
				this.CurrentBuilder.Append(Node.VarName.toUpperCase()+((Stack<Object>) this.VarMap.get(Node.VarName)).peek());
			}else{
				this.CurrentBuilder.Append(Node.VarName.toUpperCase()+((Stack<Object>) this.VarMap.get(Node.VarName)).peek());
				//this.CurrentBuilder.Append(Node.VarName.toUpperCase()+this.VarCloneMap.get(Node.VarName));
				//				this.VarNumber = this.VarCloneMap.get(Node.VarName) + 1;
				//			this.VarMap.put(Node.VarName,);
			}
		}else{
			this.CurrentBuilder.Append(Node.VarName.toUpperCase()+((Stack<Object>) this.VarMap.get(Node.VarName)).peek());
		}
	}

	@Override
	public void VisitSetLocalNode(ZSetLocalNode Node) {
		this.VarNumber = (Integer) ((Stack<?>) this.VarMap.get(Node.VarName)).peek();
		this.VarNumber++;
		this.CurrentBuilder.Append(Node.VarName.toUpperCase()+this.VarNumber);
		this.CurrentBuilder.AppendToken("=");
		this.GenerateCode(Node.ValueNode);
		if(!this.VarMap.containsKey(Node.VarName)){
			this.VarNumber = 0;
			this.VarMap.put(Node.VarName,new Stack<Object>());
			//((Stack<Object>) this.VarMap.get(Node.VarName)).push(this.VarNumber);
		}else{
			this.VarNumber = (Integer) ((Stack<?>) this.VarMap.get(Node.VarName)).pop();
			this.VarNumber++;
			//((Stack<Object>) this.VarMap.get(Node.VarName)).push(this.VarNumber);
			//			this.VarNumber = this.VarMap.get(Node.VarName);
			//			this.VarNumber++;
		}
		((Stack<Object>) this.VarMap.get(Node.VarName)).push(this.VarNumber);
		//this.VarMap.put(Node.VarName,this.VarNumber);
	}

	@Override
	public void VisitBlockNode(ZBlockNode Node) {
		this.CurrentBuilder.Indent();
		int limit = Node.StmtList.size();
		for (int i = 0; i < limit; i++) {
			ZNode SubNode = Node.StmtList.get(i);
			this.CurrentBuilder.AppendIndent();
			this.GenerateCode(SubNode);
			if(i == limit-1){
				this.CurrentBuilder.Append(".");
				this.CurrentBuilder.AppendLineFeed();
			}else{
				this.CurrentBuilder.Append(",");
				this.CurrentBuilder.AppendLineFeed();
			}
		}
		this.CurrentBuilder.UnIndent();
	}

	public void VisitPetternBlockNode(ZBlockNode Node) {
		this.CurrentBuilder.Indent();
		int limit = Node.StmtList.size();
		for (int i = 0; i < limit; i++) {
			ZNode SubNode = Node.StmtList.get(i);
			this.CurrentBuilder.AppendIndent();
			this.GenerateCode(SubNode);
			if(i == limit-1){
				this.CurrentBuilder.Append(";");
				this.CurrentBuilder.AppendLineFeed();
			}else{
				this.CurrentBuilder.Append(",");
				this.CurrentBuilder.AppendLineFeed();
			}
		}
		this.CurrentBuilder.UnIndent();
	}

	public void VisitFinishPetternBlockNode(ZBlockNode Node) {
		this.CurrentBuilder.Indent();
		int limit = Node.StmtList.size();
		for (int i = 0; i < limit; i++) {
			ZNode SubNode = Node.StmtList.get(i);
			this.CurrentBuilder.AppendIndent();
			this.GenerateCode(SubNode);
			if(i == limit-1){
				this.CurrentBuilder.AppendLineFeed();
			}else{
				this.CurrentBuilder.Append(",");
				this.CurrentBuilder.AppendLineFeed();
			}
		}
		this.CurrentBuilder.UnIndent();
	}

	public void VisitWhileBlockNode(ZBlockNode Node) {
		this.CurrentBuilder.Indent();
		int limit = Node.StmtList.size();
		for (int i = 0; i < limit; i++) {
			ZNode SubNode = Node.StmtList.get(i);
			this.CurrentBuilder.AppendIndent();
			this.GenerateCode(SubNode);
			this.CurrentBuilder.Append(",");
			this.CurrentBuilder.AppendLineFeed();

		}
		this.CurrentBuilder.UnIndent();
	}

	private void GenerateTryNode(String FunctionName,String LeftNode,String RightNode){
		this.CurrentBuilder.Append("try "+ FunctionName + "("+FunctionName+","+LeftNode+","+RightNode+") of");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.Indent();
		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append("_ -> ok");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.UnIndent();
		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append("catch");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.Indent();
		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append("throw:break -> break;");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append("throw:X -> X");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.UnIndent();
		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append("end");
	}

	private void GenerateTryNode(String FunctionName,ZFuncDeclNode Node){
		this.CurrentBuilder.Append("try "+ FunctionName + "(");
		this.ParamList(Node);
		this.CurrentBuilder.Append(") of");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.Indent();
		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append("_ -> ok");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.UnIndent();
		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append("catch");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.Indent();
		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append("throw:break -> break;");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append("throw:X -> X");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.UnIndent();
		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append("end");
	}

	//	@Override public void VisitAssignNode(ZAssignNode Node) {
	//		Node.LeftNode.Evaluate(this);
	//		this.CurrentBuilder.SpaceAppendSpace("=");
	//		Node.RightNode.Evaluate(this);
	//		//this.CurrentBuilder.Append(",");
	//		//if(!this.IsInForExpr(Node)) this.CurrentBuilder.Append("");
	//	}
	//	@Override public void VisitVarDeclNode(ZVarDeclNode Node) {
	//		String VarName = Node.Token.ParsedText;
	//		this.CurrentBuilder.SpaceAppendSpace(VarName.toUpperCase());
	//		if(Node.InitNode.Token.ParsedText != VarName){
	//			this.CurrentBuilder.SpaceAppendSpace("=");
	//			Node.InitNode.Evaluate(this);
	//		}
	//		this.CurrentBuilder.Append(".");
	//this.VisitIndentBlock(Node.BlockNode);
	//	}

	//	@Override public void VisitIfNode(ZIfNode Node) {
	//		this.flag_if = 1;
	//		this.CurrentBuilder.Append("(fun() when ");
	//		Node.CondNode.Accept(this);
	//		this.CurrentBuilder.Append(" ->");
	//		this.CurrentBuilder.AppendLineFeed();
	//		if(this.DoesNodeExist(Node.ElseNode)) {
	//			this.flag_case = 1;
	//		}
	//		Node.ThenNode.Accept(this);
	//		if(this.DoesNodeExist(Node.ElseNode)){
	//			if(Node.ElseNode instanceof ZIfNode){
	//				this.VisitElseIfNode((ZIfNode)Node.ElseNode);
	//			}else if(Node.ElseNode instanceof ZBlockNode){
	//				this.VisitElseNode((ZBlockNode)Node.ElseNode);
	//			}
	//		}
	//		this.CurrentBuilder.AppendIndent();
	//		this.CurrentBuilder.Append("end)()");
	//		this.flag_if = 0;
	//		this.flag_period += 1;
	//	}
	//
	//	public void VisitElseIfNode(ZIfNode Node) {
	//		this.CurrentBuilder.AppendIndent();
	//		this.CurrentBuilder.Append("() when ");
	//		Node.CondNode.Accept(this);
	//		this.CurrentBuilder.Append(" ->");
	//		this.CurrentBuilder.AppendLineFeed();
	//		if(!this.DoesNodeExist(Node.ElseNode)) {
	//			this.flag_case = 0;
	//		}
	//		Node.ThenNode.Accept(this);
	//		if(this.DoesNodeExist(Node.ElseNode)){
	//			if(Node.ElseNode instanceof ZIfNode){
	//				this.VisitElseIfNode((ZIfNode)Node.ElseNode);
	//			}else if(Node.ElseNode instanceof ZBlockNode){
	//				this.VisitElseNode((ZBlockNode)Node.ElseNode);
	//			}
	//		}
	//	}

	//	private final void DebugAppendNode(ZNode Node){
	//		this.CurrentBuilder.Append("/* ");
	//		this.CurrentBuilder.Append(Node.getClass().getSimpleName());
	//		this.CurrentBuilder.Append(" */");
	//	}

	//		@Override public void VisitUnaryNode(ZUnaryNode Node) {
	//			this.CurrentBuilder.Append(Node.Token.ParsedText);
	//			Node.Expr.Evaluate(this);
	//		}

	//	@Override
	//	public void VisitBlockNode(ZBlockNode Node) {
	//		this.CurrentBuilder.Indent();
	//		int limit = Node.StmtList.size();
	//		for (int i = 0; i < limit; i++) {
	//			ZNode SubNode = Node.StmtList.get(i);
	//			this.CurrentBuilder.AppendIndent();
	//			this.GenerateCode(SubNode);
	//			if(i == limit-1){
	//				this.flag_period -= 1;
	//				if(this.flag_if == 1){
	//					if(this.flag_case == 1){
	//						this.CurrentBuilder.Append(";");
	//						this.CurrentBuilder.AppendLineFeed();
	//					}else{
	//						this.CurrentBuilder.AppendLineFeed();
	//					}
	//				}else if(this.flag_while  == 1){
	//					this.CurrentBuilder.Append(",");
	//					this.CurrentBuilder.AppendLineFeed();
	//				}else{
	//					this.CurrentBuilder.Append(".");
	//					this.CurrentBuilder.AppendLineFeed();
	//				}
	//			}else{
	//				this.CurrentBuilder.Append(",");
	//				this.CurrentBuilder.AppendLineFeed();
	//			}
	//		}
	//		this.CurrentBuilder.UnIndent();
	//	}

	//	private void VisitBlockWithoutIndent(ZNode Node) {
	//		/*local*/ZNode CurrentNode = Node;
	//		while(CurrentNode != null) {
	//			if(!this.IsEmptyBlock(CurrentNode)) {
	//				this.CurrentBuilder.AppendIndent();
	//				CurrentNode.Evaluate(this);
	//				this.CurrentBuilder.Append(",");
	//			}
	//			CurrentNode = CurrentNode.NextNode;
	//		}
	//	}

	//	@Override public void OpenClassField(ZSyntaxTree ParsedTree, ZType Type, ZClassField ClassField) {
	//		this.CurrentBuilder = this.NewSourceBuilder();
	//		this.CurrentBuilder.AppendIndent();
	//		this.CurrentBuilder.Append("var  ");
	//		this.CurrentBuilder.Append(Type.ShortName);
	//		this.CurrentBuilder.Append(" = (function(_super) {");
	//		this.CurrentBuilder.Indent();
	//
	//		if(Type.SuperType != null){
	//			this.CurrentBuilder.AppendIndent();
	//			this.CurrentBuilder.Append("__extends(");
	//			this.CurrentBuilder.Append(Type.ShortName);
	//			this.CurrentBuilder.Append(", _super);");
	//		}
	//
	//		this.CurrentBuilder.AppendIndent();
	//		this.CurrentBuilder.Append("function ");
	//		this.CurrentBuilder.Append(Type.ShortName);
	//		this.CurrentBuilder.Append("(");
	//		this.CurrentBuilder.Append(") {");
	//		this.CurrentBuilder.Indent();
	//
	//		/*local*/int i = 0;
	//		/*local*/int size = LibGreenTea.ListSize(ClassField.FieldList);
	//		while(i < size) {
	//			/*local*/ZFieldInfo FieldInfo = ClassField.FieldList.get(i);
	//			/*local*/String InitValue = this.StringifyConstValue(FieldInfo.InitValue);
	//			if(!FieldInfo.Type.IsNativeType()) {
	//				InitValue = this.NullLiteral;
	//			}
	//			this.CurrentBuilder.AppendIndent();
	//			this.CurrentBuilder.Append("this.");
	//			this.CurrentBuilder.Append(FieldInfo.NativeName);
	//			this.CurrentBuilder.Append(" = ");
	//			this.CurrentBuilder.Append(InitValue);
	//			this.CurrentBuilder.Append(this.SemiColon);
	//			i += 1;
	//		}
	//		this.CurrentBuilder.UnIndent();
	//		this.CurrentBuilder.AppendIndent();
	//		this.CurrentBuilder.Append("};");
	//		this.CurrentBuilder.AppendIndent();
	//		this.CurrentBuilder.Append("return  ");
	//		this.CurrentBuilder.Append(Type.ShortName);
	//		this.CurrentBuilder.Append(";");
	//		this.CurrentBuilder.Append("})");
	//		if(Type.SuperType != null){
	//			this.CurrentBuilder.Append("(");
	//			this.CurrentBuilder.Append(Type.SuperType.ShortName);
	//			this.CurrentBuilder.Append(")");
	//		}
	//		this.CurrentBuilder.Append(";");
	//	}
	//	@Override public void VisitEmptyNode(ZEmptyNode EmptyNode) {
	//		LibGreenTea.DebugP("empty node: " + EmptyNode.Token.ParsedText);
	//	}
	//	@Override public void VisitInstanceOfNode(ZInstanceOfNode Node) {
	//		/*extention*/
	//	}
	//	@Override public void VisitSelfAssignNode(ZSelfAssignNode Node) {
	//		Node.LeftNode.Evaluate(this);
	//		this.CurrentBuilder.Append(Node.Token.ParsedText);
	//		Node.RightNode.Evaluate(this);
	//	}
	//	@Override public void VisitTrinaryNode(ZTrinaryNode Node) {
	//		Node.ConditionNode.Evaluate(this);
	//		this.CurrentBuilder.Append(" ? ");
	//		Node.ThenNode.Evaluate(this);
	//		this.CurrentBuilder.Append(" : ");
	//		Node.ElseNode.Evaluate(this);
	//	}
	//	@Override public void VisitExistsNode(ZExistsNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitCastNode(ZCastNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitSliceNode(ZSliceNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitSuffixNode(ZSuffixNode Node) {
	//		this.DebugAppendNode(Node);
	//	}

	//	@Override public void InvokeMainFunc(String MainFuncName) {
	//		this.CurrentBuilder = this.NewSourceBuilder();
	//		this.CurrentBuilder.Append(MainFuncName);
	//		this.CurrentBuilder.Append("();");
	//	}

	//	@Override public void VisitIndexerNode(ZIndexerNode Node) {
	//		this.CurrentBuilder.Append("[");
	//		Node.Expr.Evaluate(this);
	//		this.CurrentBuilder.Append("]");
	//	}
	//	@Override public void VisitArrayNode(ZArrayNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitNewArrayNode(ZNewArrayNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitDoWhileNode(ZDoWhileNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitForNode(ZForNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitForEachNode(ZForEachNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitNewNode(ZNewNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitConstructorNode(ZConstructorNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitGetterNode(ZGetterNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitSetterNode(ZSetterNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitDyGetterNode(ZDyGetterNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitDySetterNode(ZDySetterNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitApplyOverridedMethodNode(ZApplyOverridedMethodNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitApplyFuncNode(ZApplyFuncNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitApplyDynamicFuncNode(ZApplyDynamicFuncNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitApplyDynamicMethodNode(ZApplyDynamicMethodNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitAndNode(ZAndNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitOrNode(ZOrNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitSwitchNode(ZSwitchNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitBreakNode(ZBreakNode Node) {
	//		this.CurrentBuilder.Append("break");
	//	}
	//	@Override public void VisitContinueNode(ZContinueNode Node) {
	//		this.CurrentBuilder.Append("continue");
	//	}
	//	@Override public void VisitTryNode(ZTryNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitThrowNode(ZThrowNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitFunctionNode(ZFunctionNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitErrorNode(ZErrorNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
	//	@Override public void VisitCommandNode(ZCommandNode Node) {
	//		this.DebugAppendNode(Node);
	//	}
}

