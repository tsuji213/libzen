//ifdef JAVA
package zen.codegen.erlang;

import java.util.ArrayList;

import zen.ast.ZAndNode;
import zen.ast.ZArrayLiteralNode;
import zen.ast.ZBinaryNode;
import zen.ast.ZBlockNode;
import zen.ast.ZBooleanNode;
import zen.ast.ZBreakNode;
import zen.ast.ZCastNode;
import zen.ast.ZCatchNode;
import zen.ast.ZClassDeclNode;
import zen.ast.ZComparatorNode;
import zen.ast.ZConstPoolNode;
import zen.ast.ZEmptyNode;
import zen.ast.ZErrorNode;
import zen.ast.ZFieldNode;
import zen.ast.ZFloatNode;
import zen.ast.ZFuncCallNode;
import zen.ast.ZFuncDeclNode;
import zen.ast.ZFunctionNode;
import zen.ast.ZGetIndexNode;
import zen.ast.ZGetLocalNode;
import zen.ast.ZGetterNode;
import zen.ast.ZGroupNode;
import zen.ast.ZIfNode;
import zen.ast.ZInstanceOfNode;
import zen.ast.ZIntNode;
import zen.ast.ZMapLiteralNode;
import zen.ast.ZMethodCallNode;
import zen.ast.ZNewArrayNode;
import zen.ast.ZNewObjectNode;
import zen.ast.ZNode;
import zen.ast.ZNotNode;
import zen.ast.ZNullNode;
import zen.ast.ZOrNode;
import zen.ast.ZParamNode;
import zen.ast.ZReturnNode;
import zen.ast.ZSetIndexNode;
import zen.ast.ZSetLocalNode;
import zen.ast.ZSetterNode;
import zen.ast.ZStringNode;
import zen.ast.ZStupidCastNode;
import zen.ast.ZSymbolNode;
import zen.ast.ZThrowNode;
import zen.ast.ZTryNode;
import zen.ast.ZUnaryNode;
import zen.ast.ZVarDeclNode;
import zen.ast.ZWhileNode;
import zen.deps.Field;
import zen.deps.LibNative;
import zen.deps.LibZen;
import zen.deps.Var;
import zen.deps.ZenMap;
import zen.lang.ZType;
import zen.lang.ZenEngine;
import zen.lang.ZenTypeInfer;
import zen.parser.ZSourceBuilder;
import zen.parser.ZSourceGenerator;

//endif VAJA

@SuppressWarnings("unchecked")

public class ErlSourceCodeGenerator extends ZSourceGenerator {
	@Field protected ZSourceBuilder BodyBuilder;
	@Field private int LoopNodeNumber;
	@Field private VariableManager VarMgr;

	public ErlSourceCodeGenerator/*constructor*/() {
		super("erlang","5.10.4");
		this.BodyBuilder = this.NewSourceBuilder();
		this.CurrentBuilder = this.BodyBuilder;
		this.VarMgr = new VariableManager();

		this.HeaderBuilder.Append("-module(generated).");
		this.HeaderBuilder.AppendLineFeed();
	}

	@Override public boolean StartCodeGeneration(ZNode Node,  boolean AllowLazy, boolean IsInteractive) {
		System.out.println("start!!");
		if (AllowLazy && Node.IsVarType()) {
			return false;
		}
		Node.Accept(this);
		this.HeaderBuilder.AppendLineFeed();
		//if(IsInteractive) {
		if (true) {//FIX ME!!
			//String Code = this.HeaderBuilder.toString() + this.BodyBuilder.toString();
			String Code = this.HeaderBuilder.toString();
			LibNative.println(Code);
			this.HeaderBuilder.Clear();
			this.BodyBuilder.Clear();
		}
		return true;
	}

	@Override public void VisitStmtList(ArrayList<ZNode> StmtList) {
		@Var int i = 0;
		@Var int size = StmtList.size();
		while (i < size) {
			@Var ZNode SubNode = StmtList.get(i);
			this.CurrentBuilder.AppendLineFeed();
			this.CurrentBuilder.AppendIndent();
			this.GenerateCode(SubNode);
			this.CurrentBuilder.Append(",");
			i = i + 1;
		}
	}
	public void VisitStmtList(ArrayList<ZNode> StmtList, String last) {
		@Var int i = 0;
		@Var int size = StmtList.size();
		while (i < size) {
			@Var ZNode SubNode = StmtList.get(i);
			this.CurrentBuilder.AppendLineFeed();
			this.CurrentBuilder.AppendIndent();
			this.GenerateCode(SubNode);
			if (i == size - 1) {
				this.CurrentBuilder.Append(last);
			}
			else {
				this.CurrentBuilder.Append(",");
			}
			i = i + 1;
		}
	}

	@Override public void VisitBlockNode(ZBlockNode Node) {
		this.CurrentBuilder.Indent();
		this.VisitStmtList(Node.StmtList, ".");
		this.CurrentBuilder.UnIndent();
	}

	public void VisitBlockNode(ZBlockNode Node, String last) {
		this.VarMgr.PushScope();
		this.CurrentBuilder.Indent();
		this.VisitStmtList(Node.StmtList);
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.IndentAndAppend("__Arguments__ = " + this.VarMgr.GenVarTupleOnlyUsed(false));
		this.CurrentBuilder.Append(last);
		this.CurrentBuilder.UnIndent();
		this.VarMgr.PopScope();
	}

	// @Override
	// public void VisitEmptyNode(ZEmptyNode Node) {
	// }

	// @Override
	// public void VisitNullNode(ZNullNode Node) {
	// 	this.CurrentBuilder.Append(this.NullLiteral);
	// }

	// @Override
	// public void VisitBooleanNode(ZBooleanNode Node) {
	// 	if (Node.BooleanValue) {
	// 		this.CurrentBuilder.Append(this.TrueLiteral);
	// 	} else {
	// 		this.CurrentBuilder.Append(this.FalseLiteral);
	// 	}
	// }

	// @Override
	// public void VisitIntNode(ZIntNode Node) {
	// 	this.CurrentBuilder.Append("" + Node.IntValue);
	// }

	// @Override
	// public void VisitFloatNode(ZFloatNode Node) {
	// 	this.CurrentBuilder.Append("" + Node.FloatValue);
	// }

	// @Override
	// public void VisitStringNode(ZStringNode Node) {
	// 	this.CurrentBuilder.Append(LibZen.QuoteString(Node.StringValue));
	// }

	// @Override
	// public void VisitConstPoolNode(ZConstPoolNode Node) {
	// 	// TODO Auto-generated method stub
	// }

	// @Override
	// public void VisitGroupNode(ZGroupNode Node) {
	// 	this.CurrentBuilder.Append("(");
	// 	this.GenerateCode(Node.RecvNode);
	// 	this.CurrentBuilder.Append(")");
	// }

	// @Override
	// public void VisitGetIndexNode(ZGetIndexNode Node) {
	// 	this.GenerateCode(Node.RecvNode);
	// 	this.CurrentBuilder.Append("[");
	// 	this.GenerateCode(Node.IndexNode);
	// 	this.CurrentBuilder.Append("]");
	// }

	// @Override
	// public void VisitSetIndexNode(ZSetIndexNode Node) {
	// 	this.GenerateCode(Node.RecvNode);
	// 	this.CurrentBuilder.Append("[");
	// 	this.GenerateCode(Node.IndexNode);
	// 	this.CurrentBuilder.Append("]");
	// 	this.CurrentBuilder.AppendToken("=");
	// 	this.GenerateCode(Node.ValueNode);
	// }

	// @Override public void VisitSymbolNode(ZSymbolNode Node) {
	// 	//		if(Node.GivenName.equals(Node.ReferenceName) && Node.Type.IsFuncType()) {
	// 	//			Node.ReferenceName = Node.Type.StringfySignature(Node.GivenName); // FIXME
	// 	//		}
	// 	this.CurrentBuilder.Append(Node.ReferenceName);
	// }

	@Override public void VisitGetLocalNode(ZGetLocalNode Node) {
		String VarName = this.VarMgr.GenVariableName(Node.VarName);
		this.CurrentBuilder.Append(VarName);
	}

	@Override public void VisitSetLocalNode(ZSetLocalNode Node) {
		int mark = this.GetLazyMark();

		this.GenerateCode(Node.ValueNode);

		String VarName = Node.VarName;
		this.VarMgr.IncrementVariableNumber(VarName);
		this.AppendLazy(mark, this.VarMgr.GenVariableName(VarName) + " = ");
	}


	// @Override public void VisitGetterNode(ZGetterNode Node) {
	// 	this.GenerateSurroundCode(Node.RecvNode);
	// 	this.CurrentBuilder.Append(".");
	// 	this.CurrentBuilder.Append(Node.FieldName);
	// }

	// @Override
	// public void VisitSetterNode(ZSetterNode Node) {
	// 	this.GenerateSurroundCode(Node.RecvNode);
	// 	this.CurrentBuilder.Append(".");
	// 	this.CurrentBuilder.Append(Node.FieldName);
	// 	this.CurrentBuilder.AppendToken("=");
	// 	this.GenerateCode(Node.ValueNode);
	// }

	// @Override
	// public void VisitMethodCallNode(ZMethodCallNode Node) {
	// 	this.GenerateSurroundCode(Node.RecvNode);
	// 	this.CurrentBuilder.Append(".");
	// 	this.CurrentBuilder.Append(Node.MethodName);
	// 	this.VisitParamList("(", Node.ParamList, ")");
	// }

	// @Override
	// public void VisitFuncCallNode(ZFuncCallNode Node) {
	// 	this.GenerateCode(Node.FuncNode);
	// 	this.VisitParamList("(", Node.ParamList, ")");
	// }

	// @Override
	// public void VisitUnaryNode(ZUnaryNode Node) {
	// 	this.CurrentBuilder.Append(Node.SourceToken.ParsedText);
	// 	this.GenerateCode(Node.RecvNode);
	// }

	// @Override
	// public void VisitNotNode(ZNotNode Node) {
	// 	this.CurrentBuilder.Append(this.NotOperator);
	// 	this.GenerateSurroundCode(Node.RecvNode);
	// }

	// @Override
	// public void VisitCastNode(ZCastNode Node) {
	// 	this.CurrentBuilder.Append("(");
	// 	if(Node instanceof ZStupidCastNode) {
	// 		this.CurrentBuilder.AppendBlockComment("stupid");
	// 	}
	// 	this.VisitType(Node.Type);
	// 	this.CurrentBuilder.Append(")");
	// 	this.GenerateSurroundCode(Node.ExprNode);
	// }

	// @Override public void VisitInstanceOfNode(ZInstanceOfNode Node) {
	// 	this.GenerateCode(Node.LeftNode);
	// 	this.CurrentBuilder.AppendToken("instanceof");
	// 	this.VisitType(Node.RightNode.Type);
	// }

	// @Override public void VisitBinaryNode(ZBinaryNode Node) {
	// 	if (Node.ParentNode instanceof ZBinaryNode) {
	// 		this.CurrentBuilder.Append("(");
	// 	}
	// 	this.GenerateCode(Node.LeftNode);
	// 	this.CurrentBuilder.AppendToken(Node.SourceToken.ParsedText);
	// 	this.GenerateCode(Node.RightNode);
	// 	if (Node.ParentNode instanceof ZBinaryNode) {
	// 		this.CurrentBuilder.Append(")");
	// 	}
	// }

	// @Override public void VisitComparatorNode(ZComparatorNode Node) {
	// 	this.GenerateCode(Node.LeftNode);
	// 	this.CurrentBuilder.AppendToken(Node.SourceToken.ParsedText);
	// 	this.GenerateCode(Node.RightNode);
	// }

	// @Override public void VisitAndNode(ZAndNode Node) {
	// 	this.GenerateCode(Node.LeftNode);
	// 	this.CurrentBuilder.AppendToken(this.AndOperator);
	// 	this.GenerateCode(Node.RightNode);
	// }

	// @Override
	// public void VisitOrNode(ZOrNode Node) {
	// 	this.GenerateCode(Node.LeftNode);
	// 	this.CurrentBuilder.AppendToken(this.OrOperator);
	// 	this.GenerateCode(Node.RightNode);
	// }

	@Override public void VisitIfNode(ZIfNode Node) {
		int mark = this.GetLazyMark();

		this.CurrentBuilder.Append("if");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.AppendIndent();
		this.GenerateCode(Node.CondNode);
		this.CurrentBuilder.Append(" ->");
		this.VisitBlockNode((ZBlockNode)Node.ThenNode, ";");
		if (Node.ElseNode != null) {
			this.CurrentBuilder.AppendLineFeed();
			this.CurrentBuilder.IndentAndAppend("true ->");
			this.VisitBlockNode((ZBlockNode)Node.ElseNode, "");
		}
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.IndentAndAppend("end");

		this.AppendLazy(mark, this.VarMgr.GenVarTupleOnlyUsed(true) + " = ");
	}

	@Override public void VisitReturnNode(ZReturnNode Node) {
		this.CurrentBuilder.Append("throw(");
		this.GenerateCode(Node.ValueNode);
		this.CurrentBuilder.Append(")");
	}

	@Override public void VisitWhileNode(ZWhileNode Node) {
		this.LoopNodeNumber += 1;
		String WhileNodeName = "Loop" + Integer.toString(this.LoopNodeNumber);

		int mark1 = this.GetLazyMark();

		this.VarMgr.StartUsingFilter(false);
		this.VisitBlockNode((ZBlockNode)Node.BodyNode, ",");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.Indent();
		this.CurrentBuilder.IndentAndAppend(WhileNodeName + "(" + WhileNodeName + ", __Arguments__);");
		this.CurrentBuilder.UnIndent();

		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.IndentAndAppend("(_, Args) ->");
		this.CurrentBuilder.Indent();
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.IndentAndAppend("Args");
		this.CurrentBuilder.UnIndent();

		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.IndentAndAppend("end,");

		this.VarMgr.StopUsingFilter();
		this.VarMgr.ContinueUsingFilter(true);
		ZSourceBuilder LazyBuilder = this.NewSourceBuilder();
		this.CurrentBuilder = LazyBuilder;
		this.GenerateCode(Node.CondNode);
		this.CurrentBuilder = this.BodyBuilder;
		this.AppendLazy(mark1, ""
						+ WhileNodeName
						+ " = fun(" + WhileNodeName + ", "
						+ this.VarMgr.GenVarTupleOnlyUsedByChildScope(false)
						+ ") when "
						+ LazyBuilder.toString()
						+ " -> ");

		this.VarMgr.FinishUsingFilter();
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.AppendIndent();
		int mark2 = this.GetLazyMark();
		this.CurrentBuilder.Append(" = " + WhileNodeName + "(" + WhileNodeName + ", ");
		this.CurrentBuilder.Append(this.VarMgr.GenVarTupleOnlyUsedByChildScope(false) + ")");
		this.AppendLazy(mark2, this.VarMgr.GenVarTupleOnlyUsedByChildScope(true));
	}

	// @Override public void VisitBreakNode(ZBreakNode Node) {
	// 	this.CurrentBuilder.Append("break");
	// }

	// @Override public void VisitThrowNode(ZThrowNode Node) {
	// 	this.CurrentBuilder.Append("throw");
	// 	this.CurrentBuilder.AppendWhiteSpace();
	// 	this.GenerateCode(Node.ValueNode);
	// }

	// @Override public void VisitTryNode(ZTryNode Node) {
	// 	this.CurrentBuilder.Append("try");
	// 	this.GenerateCode(Node.TryNode);
	// 	if(Node.CatchNode != null) {
	// 		this.GenerateCode(Node.CatchNode);
	// 	}
	// 	if (Node.FinallyNode != null) {
	// 		this.CurrentBuilder.Append("finally");
	// 		this.GenerateCode(Node.FinallyNode);
	// 	}
	// }

	// @Override public void VisitCatchNode(ZCatchNode Node) {
	// 	this.CurrentBuilder.Append("catch (");
	// 	this.CurrentBuilder.Append(Node.ExceptionName);
	// 	this.VisitTypeAnnotation(Node.ExceptionType);
	// 	this.CurrentBuilder.Append(")");
	// 	this.GenerateCode(Node.BodyNode);
	// }

	// @Override public void VisitVarDeclNode(ZVarDeclNode Node) {
	// 	this.CurrentBuilder.Append("var");
	// 	this.CurrentBuilder.AppendWhiteSpace();
	// 	this.CurrentBuilder.Append(Node.NativeName);
	// 	this.VisitTypeAnnotation(Node.DeclType);
	// 	this.CurrentBuilder.AppendToken("=");
	// 	this.GenerateCode(Node.InitNode);
	// 	this.CurrentBuilder.Append(this.SemiColon);

	// 	// copied from VisitBlock(ZenBlockNode)
	// 	this.CurrentBuilder.Append("{");
	// 	this.CurrentBuilder.Indent();
	// 	this.VisitStmtList(Node.StmtList);
	// 	if(Node.StmtList.size() > 0) {
	// 		this.CurrentBuilder.Append(this.SemiColon);
	// 	}
	// 	this.CurrentBuilder.UnIndent();
	// 	this.CurrentBuilder.AppendLineFeed();
	// 	this.CurrentBuilder.AppendIndent();
	// 	this.CurrentBuilder.Append("}");
	// }

	// protected void VisitTypeAnnotation(ZType Type) {
	// 	this.CurrentBuilder.Append(": ");
	// 	this.VisitType(Type);
	// }

	@Override public void VisitParamNode(ZParamNode Node) {
		this.CurrentBuilder.Append(Node.Name.toUpperCase());
	}

	// @Override public void VisitFunctionNode(ZFunctionNode Node) {
	// 	this.CurrentBuilder.Append("function");
	// 	this.CurrentBuilder.AppendWhiteSpace();
	// 	this.VisitParamList("(", Node.ArgumentList, ")");
	// 	this.VisitTypeAnnotation(Node.ReturnType);
	// 	this.GenerateCode(Node.BodyNode);
	// }

	@Override public void VisitFuncDeclNode(ZFuncDeclNode Node) {
		int size = Node.ArgumentList.size();
		int i = 0;
		while (i < size) {
			ZParamNode Param = (ZParamNode)Node.ArgumentList.get(i);
			this.VarMgr.CreateVariable(Param.Name);
			i += 1;
		}

		this.HeaderBuilder.Append("-export([" + Node.FuncName + "/" + Node.ArgumentList.size() + "]).");
		this.HeaderBuilder.AppendLineFeed();

		this.CurrentBuilder.Append(Node.FuncName + "_inner");
		this.VisitParamList("(", Node.ArgumentList, ")");
		this.CurrentBuilder.Append("->");
		if (Node.BodyNode == null) {
			this.CurrentBuilder.AppendIndent();
			this.CurrentBuilder.Append("pass.");
		} else {
			this.GenerateCode(Node.BodyNode);
		}

		this.CurrentBuilder.AppendLineFeed();
		this.AppendWrapperFuncDecl(Node);
		this.CurrentBuilder.AppendLineFeed();
	}

	// @Override public void VisitClassDeclNode(ZClassDeclNode Node) {
	// 	this.CurrentBuilder.Append("class");
	// 	this.CurrentBuilder.AppendWhiteSpace();
	// 	this.CurrentBuilder.Append(Node.ClassName);
	// 	if(Node.SuperType != null) {
	// 		this.CurrentBuilder.AppendToken("extends");
	// 		this.VisitType(Node.SuperType);
	// 	}
	// 	this.CurrentBuilder.AppendWhiteSpace();
	// 	this.CurrentBuilder.Append("{");
	// 	this.CurrentBuilder.Indent();
	// 	@Var int i = 0;
	// 	while (i < Node.FieldList.size()) {
	// 		@Var ZFieldNode FieldNode = Node.FieldList.get(i);
	// 		this.CurrentBuilder.AppendLineFeed();
	// 		this.CurrentBuilder.AppendIndent();
	// 		this.CurrentBuilder.Append("field");
	// 		this.CurrentBuilder.AppendWhiteSpace();
	// 		this.CurrentBuilder.Append(FieldNode.FieldName);
	// 		this.VisitTypeAnnotation(FieldNode.DeclType);
	// 		this.CurrentBuilder.AppendToken("=");
	// 		this.GenerateCode(FieldNode.InitNode);
	// 		this.CurrentBuilder.Append(this.SemiColon);
	// 		i = i + 1;
	// 	}
	// 	this.CurrentBuilder.UnIndent();
	// 	this.CurrentBuilder.AppendLineFeed();
	// 	this.CurrentBuilder.AppendIndent();
	// 	this.CurrentBuilder.Append("}");
	// }


	// @Override public void VisitErrorNode(ZErrorNode Node) {
	// 	this.Logger.ReportError(Node.SourceToken, Node.ErrorMessage);
	// }

	// // Utils
	// protected void VisitType(ZType Type) {
	// 	this.CurrentBuilder.Append(this.GetNativeType(Type.GetRealType()));
	// }

	@Override protected void VisitParamList(String OpenToken, ArrayList<ZNode> ParamList, String CloseToken) {
		this.CurrentBuilder.Append(OpenToken);
		@Var int i = 0;
		while(i < ParamList.size()) {
			@Var ZNode ParamNode = ParamList.get(i);
			if (i > 0) {
				this.CurrentBuilder.Append(", ");
			}
			this.GenerateCode(ParamNode);
			i = i + 1;
		}
		this.CurrentBuilder.Append(CloseToken);
	}

	// @Override
	// public void VisitArrayLiteralNode(ZArrayLiteralNode Node) {
	// 	// TODO Auto-generated method stub
	// }

	// @Override
	// public void VisitMapLiteralNode(ZMapLiteralNode Node) {
	// 	// TODO Auto-generated method stub
	// }

	// @Override
	// public void VisitNewArrayNode(ZNewArrayNode Node) {
	// 	// TODO Auto-generated method stub
	// }

	// @Override
	// public void VisitNewObjectNode(ZNewObjectNode Node) {
	// 	// TODO Auto-generated method stub
	// }

	private void AppendWrapperFuncDecl(ZFuncDeclNode Node){
		this.CurrentBuilder.Append(Node.FuncName);
		this.VisitParamList("(", Node.ArgumentList, ")");
		this.CurrentBuilder.Append(" ->");

		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.Indent();
		this.CurrentBuilder.IndentAndAppend("try "+ Node.FuncName + "_inner");
		this.VisitParamList("(", Node.ArgumentList, ")");
		this.CurrentBuilder.Append(" of");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.Indent();
		this.CurrentBuilder.IndentAndAppend("_ -> void");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.UnIndent();
		this.CurrentBuilder.IndentAndAppend("catch");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.Indent();
		this.CurrentBuilder.IndentAndAppend("throw:X -> X");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.UnIndent();
		this.CurrentBuilder.IndentAndAppend("end.");

		this.CurrentBuilder.UnIndent();
	}


	private int GetLazyMark() {
		this.CurrentBuilder.Append(null);
		return this.CurrentBuilder.SourceList.size() - 1;
	}
	private void AppendLazy(int mark, String Code) {
		this.CurrentBuilder.SourceList.set(mark, Code);
	}
}
