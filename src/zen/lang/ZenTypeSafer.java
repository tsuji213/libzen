// ***************************************************************************
// Copyright (c) 2013-2014, Konoha project authors. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// *  Redistributions of source code must retain the above copyright notice,
//    this list of conditions and the following disclaimer.
// *  Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
// TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
// OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
// OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// **************************************************************************

//ifdef JAVA

package zen.lang;

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
import zen.ast.ZErrorNode;
import zen.ast.ZFieldNode;
import zen.ast.ZFloatNode;
import zen.ast.ZFuncCallNode;
import zen.ast.ZFunctionNode;
import zen.ast.ZGetIndexNode;
import zen.ast.ZGetNameNode;
import zen.ast.ZGetterNode;
import zen.ast.ZGroupNode;
import zen.ast.ZIfNode;
import zen.ast.ZInstanceOfNode;
import zen.ast.ZIntNode;
import zen.ast.ZLetNode;
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
import zen.ast.ZSetNameNode;
import zen.ast.ZSetterNode;
import zen.ast.ZStringNode;
import zen.ast.ZSymbolNode;
import zen.ast.ZThrowNode;
import zen.ast.ZTryNode;
import zen.ast.ZUnaryNode;
import zen.ast.ZVarDeclNode;
import zen.ast.ZWhileNode;
import zen.deps.Field;
import zen.deps.LibZen;
import zen.deps.Nullable;
import zen.deps.Var;
import zen.parser.ZGenerator;
import zen.parser.ZNameSpace;
import zen.parser.ZUtils;
import zen.parser.ZVariable;
import zen.type.ZFuncType;
import zen.type.ZGreekType;
import zen.type.ZType;
import zen.type.ZTypeChecker;
import zen.type.ZTypeFlag;
import zen.type.ZTypePool;
import zen.type.ZVarScope;
import zen.type.ZVarType;

public class ZenTypeSafer extends ZTypeChecker {

	@Field private ZFunctionNode CurrentFunctionNode = null;

	public ZenTypeSafer(ZGenerator Generator) {
		super(Generator);
	}

	public final boolean IsTopLevel() {
		return (this.CurrentFunctionNode == null);
	}

	public final boolean InFunctionScope() {
		return (this.CurrentFunctionNode != null);
	}

	@Override public void VisitNullNode(ZNullNode Node) {
		ZType Type = this.GetContextType();
		this.TypedNode(Node, Type);
	}

	@Override public void VisitBooleanNode(ZBooleanNode Node) {
		this.TypedNode(Node, ZType.BooleanType);
	}

	@Override public void VisitIntNode(ZIntNode Node) {
		this.TypedNode(Node, ZType.IntType);
	}

	@Override public void VisitFloatNode(ZFloatNode Node) {
		this.TypedNode(Node, ZType.FloatType);
	}

	@Override public void VisitStringNode(ZStringNode Node) {
		this.TypedNode(Node, ZType.StringType);
	}

	@Override public void VisitArrayLiteralNode(ZArrayLiteralNode Node) {
		//		@Var ZNameSpace NameSpace = Node.GetNameSpace();
		@Var ZType ArrayType = this.GetContextType();
		@Var ZType ElementType = ZType.VarType;
		if(ArrayType.IsArrayType()) {
			ElementType = ArrayType.GetParamType(0);
		}
		@Var boolean AllTyped = true;
		@Var int i = 0;
		while(i < Node.NodeList.size()) {
			ZNode SubNode = Node.NodeList.get(i);
			SubNode = this.CheckType(SubNode, ElementType);
			Node.NodeList.set(i, SubNode);
			if(SubNode.IsVarType()) {
				AllTyped = false;
				ArrayType = ZType.VarType;
			}
			else {
				if(ElementType.IsVarType()) {
					ElementType = SubNode.Type;
				}
			}
			i = i + 1;
		}
		if( AllTyped && !ElementType.IsVarType()) {
			ArrayType = ZTypePool.GetGenericType1(ZType.ArrayType, ElementType);
		}
		this.TypedNode(Node, ArrayType);
	}

	@Override public void VisitMapLiteralNode(ZMapLiteralNode Node) {
		//		@Var ZNameSpace NameSpace = Node.GetNameSpace();
		//		@Var ZType ContextType = this.GetContextType();
		//		@Var int i = 0;
		//		while(i < Node.NodeList.size()) {
		//			@Var ZNode SubNode = Node.NodeList.get(i);
		//			if(SubNode instanceof ZGetNameNode) {
		//				SubNode = ((ZGetNameNode)SubNode).ToStringNode();
		//			}
		//			SubNode = this.CheckType(SubNode, NameSpace, ZType.StringType);
		//			Node.NodeList.set(i, SubNode);
		//			i = i + 2;
		//		}
		//		@Var boolean AllTyped = true;
		//		if(ContextType instanceof ZenClassType) {
		//			@Var ZenClassType ClassType = (ZenClassType)ContextType;
		//			i = 0;
		//			while(i < Node.NodeList.size()) {
		//				@Var ZStringNode KeyNode = (ZStringNode)Node.NodeList.get(i);
		//				@Var ZType FieldType = ClassType.GetFieldType(KeyNode.StringValue, null);
		//				if(FieldType == null) {
		//					this.Return(ZenError.UndefinedName(KeyNode, KeyNode.StringValue));
		//				}
		//				else {
		//					@Var ZNode SubNode = Node.NodeList.get(i+1);
		//					SubNode = this.CheckType(SubNode, NameSpace, FieldType);
		//					Node.NodeList.set(i+1, SubNode);
		//					if(SubNode.IsVarType()) {
		//						AllTyped = false;
		//						ContextType = ZType.VarType;
		//					}
		//				}
		//				i = i + 2;
		//			}
		//		}
		//		else if(ContextType.IsMapType()) {
		//			@Var ZType ElementType = ContextType.GetParamType(0);
		//			i = 1;
		//			while(i < Node.NodeList.size()) {
		//				@Var ZNode SubNode = Node.NodeList.get(i);
		//				SubNode = this.CheckType(SubNode, NameSpace, ElementType);
		//				Node.NodeList.set(i, SubNode);
		//				if(SubNode.IsVarType()) {
		//					AllTyped = false;
		//					ContextType = ZType.VarType;
		//				}
		//				i = i + 2;
		//			}
		//		}
		//		else {
		//			ContextType = ZType.VarType;
		//			AllTyped = false;
		//		}
		//		this.TypedNode(Node, ContextType);
	}

	@Override public void VisitNewArrayNode(ZNewArrayNode Node) {
		this.Todo(Node);
	}

	@Override public void VisitSymbolNode(ZSymbolNode Node) {

	}

	@Override public void VisitGetNameNode(ZGetNameNode Node) {
		@Var ZNameSpace NameSpace = Node.GetNameSpace();
		@Var ZVariable VarInfo = NameSpace.GetLocalVariable(Node.VarName);
		if(VarInfo != null) {
			Node.VarName = VarInfo.VarName;
			Node.VarIndex = VarInfo.VarUniqueIndex;
			Node.IsCaptured = VarInfo.IsCaptured(this.CurrentFunctionNode);
			this.TypedNode(Node, VarInfo.VarType);
		}
		else {
			@Var ZNode ConstNode = NameSpace.GetSymbolNode(Node.VarName);
			if(ConstNode != null) {
				this.Return(ConstNode);
			}
			else {
				this.Logger.ReportWarning(Node.SourceToken, "undefined variable: " + Node.VarName);
				this.TypedNode(Node, ZType.VarType);
			}
		}
	}

	@Override public void VisitSetNameNode(ZSetNameNode Node) {
		ZNameSpace NameSpace = Node.GetNameSpace();
		ZVariable VarInfo = NameSpace.GetLocalVariable(Node.VarName);
		if(VarInfo == null) {
			this.Return(ZenError.UndefinedName(Node, Node.VarName));
		}
		else {
			Node.VarName = VarInfo.VarName;
			Node.VarIndex = VarInfo.VarUniqueIndex;
			Node.IsCaptured = VarInfo.IsCaptured(this.CurrentFunctionNode);
			if(Node.IsCaptured) {
				this.Return(ZenError.ReadOnlyLocalVariable(Node, Node.VarName));
			}
			else {
				Node.ValueNode = this.CheckType(Node.ValueNode, VarInfo.VarType);
				this.TypedNodeIf(Node, ZType.VoidType, Node.ValueNode);
			}
		}
	}

	@Override public void VisitGetIndexNode(ZGetIndexNode Node) {
		ZNameSpace NameSpace = Node.GetNameSpace();
		Node.RecvNode = this.CheckType(Node.RecvNode, ZType.VarType);
		Node.IndexNode = this.CheckType(Node.IndexNode, ZenGamma.GetIndexType(NameSpace, Node.RecvNode.Type));
		this.TypedNodeIf2(Node, ZenGamma.GetElementType(NameSpace, Node.RecvNode.Type), Node.RecvNode, Node.IndexNode);
	}

	@Override public void VisitSetIndexNode(ZSetIndexNode Node) {
		ZNameSpace NameSpace = Node.GetNameSpace();
		Node.RecvNode = this.CheckType(Node.RecvNode, ZType.VarType);
		Node.IndexNode = this.CheckType(Node.IndexNode, ZenGamma.GetIndexType(NameSpace, Node.RecvNode.Type));
		Node.ValueNode = this.CheckType(Node.ValueNode, ZenGamma.GetElementType(NameSpace, Node.RecvNode.Type));
		this.TypedNodeIf3(Node, ZType.VoidType,  Node.RecvNode, Node.IndexNode, Node.ValueNode);
	}

	@Override public void VisitGroupNode(ZGroupNode Node) {
		Node.RecvNode.Accept(this); // this is shortcut
		this.TypedNode(Node, Node.RecvNode.Type);
	}

	@Override public void VisitGetterNode(ZGetterNode Node) {
		@Var ZNameSpace NameSpace = Node.GetNameSpace();
		//		@Var ZType ContextType = this.GetContextType();
		Node.RecvNode = this.CheckType(Node.RecvNode, ZType.VarType);
		ZType FieldType = ZenGamma.GetFieldType(NameSpace, Node.RecvNode.Type, Node.FieldName);
		//		if(FieldType.IsVarType() && ContextType.IsInferrableType()) {
		//			ZenGamma.InferFieldType(NameSpace, Node.RecvNode.Type, Node.FieldName, ContextType, Node.SourceToken);
		//		}
		if(FieldType.IsVoidType() && !Node.RecvNode.IsVarType()) {
			this.Return(ZenError.UndefinedName(Node, Node.RecvNode.Type.StringfyClassMember(Node.FieldName)));
			return;
		}
		this.TypedNodeIf(Node, FieldType, Node.RecvNode);
	}

	@Override public void VisitSetterNode(ZSetterNode Node) {
		@Var ZNameSpace NameSpace = Node.GetNameSpace();
		Node.RecvNode = this.CheckType(Node.RecvNode, ZType.VarType);
		@Var ZType FieldType = ZenGamma.GetSetterType(NameSpace, Node.RecvNode.Type, Node.FieldName);
		if(FieldType.IsVoidType()) {
			this.Return(ZenError.ReadOnlyName(Node, Node.RecvNode.Type, Node.FieldName));
			return;
		}
		Node.ValueNode = this.CheckType(Node.ValueNode, FieldType);
		//		if(FieldType.IsVarType()) {
		//			ZenGamma.InferFieldType(NameSpace, Node.RecvNode.Type, Node.FieldName, Node.ValueNode.Type, Node.SourceToken);
		//		}
		this.TypedNodeIf2(Node, ZType.VoidType, Node.RecvNode, Node.ValueNode);
	}

	private ZFuncType GuessFuncTypeFromContext(ZType ReturnType, @Nullable ZType RecvType, ArrayList<ZNode> ParamList) {
		if(ReturnType.IsVoidType()) {
			ReturnType = ZType.VarType;
		}
		ArrayList<ZType> TypeList = new ArrayList<ZType>();
		TypeList.add(ReturnType.GetRealType());
		if(RecvType != null) {
			TypeList.add(RecvType.GetRealType());
		}
		@Var int i = 0;
		while(i < ParamList.size()) {
			ZNode SubNode = ParamList.get(i);
			ZType ParamType = SubNode.Type.GetRealType();
			TypeList.add(ParamType);
			i = i + 1;
		}
		return ZTypePool.LookupFuncType(TypeList);
	}

	private void TypeCheckFuncCall(ZFuncCallNode FuncNode, ZFuncType FuncType) {
		@Var int i = 0;
		@Var boolean IsAllTyped = true;
		@Var ZType[] Greek = ZGreekType.NewGreekTypes(null);
		while(i < FuncNode.ParamList.size()) {
			@Var ZNode SubNode = FuncNode.ParamList.get(i);
			@Var ZType ParamType =  FuncType.GetParamType(i+1);
			SubNode = this.TryType(SubNode, ParamType);
			if(SubNode.IsUntyped()) {
				IsAllTyped = false;
			}
			if(!ParamType.AcceptValueType(SubNode.Type, false, Greek)) {
				SubNode = ZenError.FuncCallTypeError(ParamType.GetRealType(Greek), FuncNode, i+1, SubNode.Type);
			}
			FuncNode.ParamList.set(i, SubNode);
			i = i + 1;
		}
		if(!FuncType.IsVarType() && IsAllTyped) {
			this.TypedNode(FuncNode, FuncType.GetReturnType().GetRealType(Greek));
		}
		else {
			this.TypedNode(FuncNode, ZType.VarType);
		}
	}

	private void TypeCheckNativeMethodCall(ZNode Node, ZType RecvType, String MethodName, ArrayList<ZNode> ParamList) {
		ZFuncType FuncType = this.Generator.GetMethodFuncType(RecvType, MethodName, ParamList);
		if(FuncType != null) {
			@Var int i = 0;
			@Var boolean IsAllTyped = true;
			@Var int StaticShift = FuncType.GetParamSize() - ParamList.size();
			while(i < ParamList.size()) {
				@Var ZNode SubNode = ParamList.get(i);
				SubNode = this.CheckType(SubNode, FuncType.GetParamType(i+StaticShift));
				ParamList.set(i, SubNode);
				if(SubNode.IsUntyped()) {
					IsAllTyped = false;
				}
				i = i + 1;
			}
			if(IsAllTyped) {
				this.TypedNode(Node, FuncType.GetReturnType());
				return;
			}
		}
		this.Return(Node);
	}

	@Override public void VisitMethodCallNode(ZMethodCallNode Node) {
		@Var ZNameSpace NameSpace = Node.GetNameSpace();
		Node.RecvNode = this.CheckType(Node.RecvNode, ZType.VarType);
		if(!Node.RecvNode.IsVarType()) {
			ZType FieldType = ZenGamma.GetFieldType(NameSpace, Node.RecvNode.Type, Node.MethodName);
			if(FieldType.IsFuncType()) {
				ZFuncCallNode FuncCall = Node.ToGetterFuncCall();
				this.TypeCheckFuncCall(FuncCall, (ZFuncType)FieldType);
				return;
			}
		}
		@Var int FuncParamSize = Node.ParamList.size() + 1;
		@Var ZFunc Func = ZenGamma.LookupFunc(NameSpace, Node.MethodName, Node.RecvNode.Type, FuncParamSize);
		if(Func != null) {
			ZFuncCallNode FuncCall = Node.ToStaticFuncCall(Func);
			this.TypeCheckFuncCall(FuncCall, Func.GetFuncType());
			return;
		}
		this.TypeCheckNodeList(Node.ParamList);
		this.TypeCheckNativeMethodCall(Node, Node.RecvNode.Type, Node.MethodName, Node.ParamList);
	}

	@Override public void VisitNewObjectNode(ZNewObjectNode Node) {
		@Var ZNameSpace NameSpace = Node.GetNameSpace();
		@Var ZType ContextType = this.GetContextType();
		this.TypeCheckNodeList(Node.ParamList);
		if(Node.Type.IsVarType()) {
			if(ContextType.IsVarType()) {
				this.Return(Node);
				return;
			}
			Node.Type = ContextType;
		}
		@Var int FuncParamSize = Node.ParamList.size() + 1;
		@Var ZFunc Func = ZenGamma.LookupFunc(NameSpace, Node.Type.GetUniqueName(), Node.Type, FuncParamSize);
		if(Func != null) {
			ZFuncCallNode FuncCall = Node.ToStaticFuncCall(Func);
			this.TypeCheckFuncCall(FuncCall, Func.GetFuncType());
			return;
		}
		this.TypeCheckNativeMethodCall(Node, Node.Type, null, Node.ParamList);
	}

	private boolean IsFuncName(ZFuncCallNode FuncCallNode, ZNameSpace NameSpace) {
		if(FuncCallNode.FuncNode instanceof ZGetNameNode && FuncCallNode.FuncNode.IsVarType()) {
			@Var ZGetNameNode Node = (ZGetNameNode)FuncCallNode.FuncNode;
			@Var ZVariable VarInfo = NameSpace.GetLocalVariable(Node.VarName);
			if(VarInfo == null || !VarInfo.VarType.IsVarType() || !VarInfo.VarType.IsFuncType()) {
				FuncCallNode.ResolvedFuncName = Node.VarName;
			}
			if(FuncCallNode.ResolvedFuncName != null) {
				if(FuncCallNode.ResolvedFuncType == null) {
					@Var int FuncParamSize = FuncCallNode.ParamList.size();
					@Var ZType RecvType = FuncCallNode.GetRecvType();
					@Var ZFunc Func = ZenGamma.LookupFunc(NameSpace, FuncCallNode.ResolvedFuncName, RecvType, FuncParamSize);
					if(Func != null) {
						FuncCallNode.ResolvedFuncType = Func.GetFuncType();
					}
					else {
						this.VarScope.FoundUnresolvedSymbol(FuncCallNode.ResolvedFuncName);
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override public void VisitFuncCallNode(ZFuncCallNode Node) {
		@Var ZNameSpace NameSpace = Node.GetNameSpace();
		@Var ZType ContextType = this.GetContextType();
		this.TypeCheckNodeList(Node.ParamList);
		@Var ZFuncType PartialFuncType = this.GuessFuncTypeFromContext(ContextType, null, Node.ParamList);
		if(this.IsFuncName(Node, NameSpace)) {
			if(Node.ResolvedFuncType != null) {
				this.TypeCheckFuncCall(Node, Node.ResolvedFuncType);
				return;
			}
		}
		else {
			Node.FuncNode = this.TryType(Node.FuncNode, PartialFuncType);
			@Var ZType FuncNodeType = Node.FuncNode.Type;
			if(!FuncNodeType.IsFuncType() && !FuncNodeType.IsVarType()) {
				this.Return(new ZErrorNode(Node, "not function: given = " + FuncNodeType));
				return;
			}
			if(FuncNodeType.IsFuncType()) {
				this.TypeCheckFuncCall(Node, (ZFuncType)FuncNodeType);
				return;
			}
		}
		this.TypedNode(Node, ZType.VarType);
	}

	@Override public void VisitUnaryNode(ZUnaryNode Node) {
		Node.RecvNode = this.CheckType(Node.RecvNode, ZType.VarType);
		this.TypedNode(Node, Node.RecvNode.Type);
	}

	@Override public void VisitNotNode(ZNotNode Node) {
		Node.RecvNode = this.CheckType(Node.RecvNode, ZType.BooleanType);
		this.TypedNodeIf(Node, ZType.BooleanType, Node.RecvNode);
	}

	@Override public void VisitCastNode(ZCastNode Node) {
		Node.ExprNode = this.CheckType(Node.ExprNode, Node.Type);
		this.TypedNode(Node, Node.Type);
	}

	@Override public void VisitInstanceOfNode(ZInstanceOfNode Node) {
		Node.LeftNode = this.CheckType(Node.LeftNode, ZType.VarType);
		this.TypedNodeIf(Node, ZType.BooleanType, Node.LeftNode);
	}

	private ZType GetBinaryLeftType(String Op, ZType ContextType) {
		if(LibZen.EqualsString(Op, "|") || LibZen.EqualsString(Op, "&") || LibZen.EqualsString(Op, "<<") || LibZen.EqualsString(Op, ">>") || LibZen.EqualsString(Op,  "^")) {
			return ZType.IntType;
		}
		if(LibZen.EqualsString(Op, "*") || LibZen.EqualsString(Op, "-") || LibZen.EqualsString(Op, "/") || LibZen.EqualsString(Op, "%")) {
			if(ContextType.IsNumberType()) {
				return ContextType;
			}
		}
		return ZType.VarType;
	}

	private ZType GetBinaryRightType(String Op, ZType ContextType) {
		if(LibZen.EqualsString(Op, "|") || LibZen.EqualsString(Op, "&") || LibZen.EqualsString(Op, "<<") || LibZen.EqualsString(Op, ">>") || LibZen.EqualsString(Op,  "^")) {
			return ZType.IntType;
		}
		if(LibZen.EqualsString(Op, "*") || LibZen.EqualsString(Op, "-") || LibZen.EqualsString(Op, "/") || LibZen.EqualsString(Op, "%")) {
			if(ContextType.IsNumberType()) {
				return ContextType;
			}
		}
		return ZType.VarType;
	}

	private void UnifyBinaryNodeType(ZBinaryNode Node, ZType Type) {
		if(Node.LeftNode.Type.Equals(Type)) {
			Node.RightNode = this.CheckType(Node.RightNode, Type);
			return;
		}
		if(Node.RightNode.Type.Equals(Type)) {
			Node.LeftNode = this.CheckType(Node.LeftNode, Type);
		}
	}

	private void UnifyBinaryEnforcedType(ZBinaryNode Node, ZType Type) {
		if(Node.LeftNode.Type.Equals(Type)) {
			Node.RightNode = this.EnforceType(Node.RightNode, Type);
			return;
		}
		if(Node.RightNode.Type.Equals(Type)) {
			Node.LeftNode = this.EnforceType(Node.LeftNode, Type);
		}
	}

	@Override public void VisitBinaryNode(ZBinaryNode Node) {
		ZType ContextType = this.GetContextType();
		ZType LeftType = this.GetBinaryLeftType(Node.SourceToken.ParsedText, ContextType);
		ZType RightType = this.GetBinaryRightType(Node.SourceToken.ParsedText, ContextType);
		//System.err.println("debug left=" + LeftType + ", right=" + RightType);
		Node.LeftNode = this.CheckType(Node.LeftNode, LeftType);
		Node.RightNode = this.CheckType(Node.RightNode, RightType);
		//System.err.println("debug left=" + Node.LeftNode.Type + ", right=" + Node.RightNode.Type);
		if(!Node.LeftNode.Type.Equals(Node.RightNode.Type)) {
			if(Node.SourceToken.EqualsText("+")) {
				this.UnifyBinaryEnforcedType(Node, ZType.StringType);
			}
			this.UnifyBinaryNodeType(Node, ZType.FloatType);
			Node.LeftNode = this.CheckType(Node.LeftNode, Node.RightNode.Type);
		}
		this.TypedNodeIf(Node, Node.LeftNode.Type, Node.RightNode);
	}

	@Override public void VisitComparatorNode(ZComparatorNode Node) {
		Node.LeftNode = this.CheckType(Node.LeftNode, ZType.VarType);
		Node.RightNode = this.TryType(Node.RightNode, Node.LeftNode.Type);
		this.UnifyBinaryNodeType(Node, ZType.FloatType);
		Node.RightNode = this.CheckType(Node.RightNode, Node.LeftNode.Type);
		this.TypedNodeIf2(Node, ZType.BooleanType, Node.LeftNode, Node.RightNode);
	}

	@Override public void VisitAndNode(ZAndNode Node) {
		Node.LeftNode = this.CheckType(Node.LeftNode, ZType.BooleanType);
		Node.RightNode = this.CheckType(Node.RightNode, ZType.BooleanType);
		this.TypedNodeIf2(Node, ZType.BooleanType, Node.LeftNode, Node.RightNode);
	}

	@Override public void VisitOrNode(ZOrNode Node) {
		Node.LeftNode = this.CheckType(Node.LeftNode, ZType.BooleanType);
		Node.RightNode = this.CheckType(Node.RightNode, ZType.BooleanType);
		this.TypedNodeIf2(Node, ZType.BooleanType, Node.LeftNode, Node.RightNode);
	}

	@Override public void VisitBlockNode(ZBlockNode Node) {
		@Var int i = 0;
		ZType BlockType = ZType.VoidType;
		while(i < Node.StmtList.size()) {
			ZNode SubNode = Node.StmtList.get(i).GetStatementNode();  // without annotation
			SubNode = this.CheckType(SubNode, ZType.VoidType);
			Node.StmtList.set(i, SubNode);
			if(SubNode.IsVarType()) {
				BlockType = ZType.VarType;
			}
			if(SubNode.IsBreakingBlock()) {
				break;
			}
			i = i + 1;
		}
		this.TypedNode(Node, BlockType);
	}

	@Override public void VisitVarDeclNode(ZVarDeclNode Node) {
		Node.InitNode = this.CheckType(Node.InitNode, Node.DeclType);
		if(!(Node.DeclType instanceof ZVarType)) {
			Node.DeclType = this.VarScope.NewVarType(Node.DeclType, Node.NativeName, Node.SourceToken);
			Node.NameSpace.SetLocalVariable(this.CurrentFunctionNode, Node.DeclType, Node.NativeName, Node.SourceToken);
		}
		this.VisitBlockNode(Node);
		this.TypedNodeIf2(Node, ZType.VoidType, Node.InitNode, Node);
	}

	@Override public void VisitIfNode(ZIfNode Node) {
		Node.CondNode = this.CheckType(Node.CondNode, ZType.BooleanType);
		Node.ThenNode = this.CheckType(Node.ThenNode, ZType.VoidType);
		if(Node.ElseNode != null) {
			Node.ElseNode = this.CheckType(Node.ElseNode, ZType.VoidType);
			this.TypedNodeIf3(Node, ZType.VoidType, Node.CondNode, Node.ThenNode, Node.ElseNode);
		}
		else {
			this.TypedNodeIf2(Node, ZType.VoidType, Node.CondNode, Node.ThenNode);
		}
	}

	@Override public void VisitReturnNode(ZReturnNode Node) {
		if(!this.InFunctionScope()) {
			this.Return(ZenError.NeedFunctionScope(Node, "return"));
			return;
		}
		@Var ZType ReturnType = this.CurrentFunctionNode.ReturnType;
		if(Node.ValueNode != null && ReturnType.IsVoidType()) {
			Node.ValueNode = null;
		}
		else if(Node.ValueNode == null && !ReturnType.IsVarType() && !ReturnType.IsVoidType()) {
			this.Logger.ReportWarning(Node.SourceToken, "returning default value of " + ReturnType);
			Node.ValueNode = ZenGamma.CreateDefaultValueNode(Node, ReturnType, null);
		}
		if(Node.ValueNode != null) {
			Node.ValueNode = this.CheckType(Node.ValueNode, ReturnType);
			this.TypedNodeIf(Node, ZType.VoidType, Node.ValueNode);
		}
		else {
			if(ReturnType instanceof ZVarType) {
				((ZVarType)ReturnType).Infer(ZType.VoidType, Node.SourceToken);
			}
			this.TypedNode(Node, ZType.VoidType);
		}
	}


	@Override public void VisitWhileNode(ZWhileNode Node) {
		Node.CondNode = this.CheckType(Node.CondNode, ZType.BooleanType);
		Node.BodyNode = this.CheckType(Node.BodyNode, ZType.VoidType);
		this.TypedNodeIf2(Node, ZType.VoidType, Node.CondNode, Node.BodyNode);
	}

	@Override public void VisitBreakNode(ZBreakNode Node) {
		this.TypedNode(Node, ZType.VoidType);
	}

	@Override public void VisitThrowNode(ZThrowNode Node) {
		Node.ValueNode = this.CheckType(Node.ValueNode, ZType.VarType);
		this.TypedNodeIf(Node, ZType.VoidType, Node.ValueNode);
	}

	@Override public void VisitTryNode(ZTryNode Node) {
		Node.TryNode = this.CheckType(Node.TryNode, ZType.VoidType);
		if(Node.CatchNode != null) {
			Node.CatchNode = this.CheckType(Node.CatchNode, ZType.VoidType);
		}
		if(Node.FinallyNode != null) {
			Node.FinallyNode = this.CheckType(Node.FinallyNode, ZType.VoidType);
		}
		this.TypedNode(Node, ZType.VoidType);
	}

	@Override public void VisitCatchNode(ZCatchNode Node) {
		// TODO Auto-generated method stub
		this.Todo(Node);
	}

	@Override public void VisitLetNode(ZLetNode Node) {
		Node.GlobalName = this.Generator.GetGlobalName(Node.Symbol);
		Node.ValueNode = this.CheckType(Node.ValueNode, Node.SymbolType);
		this.TypedNode(Node, ZType.VoidType);
	}

	private boolean HasReturn(ZNode Node) {
		if(Node instanceof ZBlockNode) {
			@Var ZBlockNode BlockNode = (ZBlockNode)Node;
			@Var int i = 0;
			@Var ZNode StmtNode = null;
			while(i < BlockNode.StmtList.size()) {
				StmtNode = BlockNode.StmtList.get(i);
				if(StmtNode instanceof ZReturnNode) {
					return true;
				}
				i = i + 1;
			}
			Node = StmtNode;
		}
		if(Node instanceof ZReturnNode) {
			return true;
		}
		if(Node instanceof ZIfNode) {
			ZIfNode IfNode = (ZIfNode)Node;
			if(IfNode.ElseNode != null) {
				return this.HasReturn(IfNode.ThenNode) && this.HasReturn(IfNode.ElseNode);
			}
			return false;
		}
		if(Node instanceof ZBlockNode) {
			return this.HasReturn(Node);
		}
		return false;
	}

	@Override public void DefineFunction(ZFunctionNode FunctionNode, boolean Enforced) {
		if(FunctionNode.FuncName != null && FunctionNode.GlobalName == null) {
			@Var ZFuncType FuncType = FunctionNode.GetFuncType(null);
			//System.out.println("debug guessing " + FuncType);
			if(Enforced || !FuncType.IsVarType()) {
				@Var ZNameSpace NameSpace = FunctionNode.GetNameSpace();
				@Var ZFunc Func = ZenGamma.GetFunc(NameSpace, FunctionNode.FuncName, FuncType, null);
				if(Func != null) {
					this.Logger.ReportError(FunctionNode.SourceToken, "redefinition of function: " + Func);
				}
				else {
					Func = new ZSignature(0, FunctionNode.FuncName, FuncType, FunctionNode.SourceToken);
					ZenGamma.DefineFunc(NameSpace, Func);
					FunctionNode.GlobalName = Func.GetSignature();
				}
			}
		}
	}

	private void PushFunctionNode(ZNameSpace NameSpace, ZFunctionNode FunctionNode, ZType ContextType) {
		@Var ZFuncType FuncType = null;
		if(ContextType instanceof ZFuncType) {
			FuncType = (ZFuncType)ContextType;
		}
		this.CurrentFunctionNode = FunctionNode.Push(this.CurrentFunctionNode);
		this.VarScope = new ZVarScope(this.VarScope, this.Logger, null);
		@Var int i = 0;
		while(i < FunctionNode.ParamList.size()) {
			@Var ZParamNode ParamNode = FunctionNode.ParamList.get(i);
			ParamNode.Type = this.VarScope.NewVarType(ParamNode.Type, ParamNode.Name, ParamNode.SourceToken);
			if(FuncType != null) {
				ParamNode.Type.Maybe(FuncType.GetParamType(i+1), null);
			}
			NameSpace.SetLocalVariable(this.CurrentFunctionNode, ParamNode.Type, ParamNode.Name, null);
			i = i + 1;
		}
		FunctionNode.ReturnType = this.VarScope.NewVarType(FunctionNode.ReturnType, "return", FunctionNode.SourceToken);
		if(FuncType != null) {
			FunctionNode.Type.Maybe(FuncType.GetParamType(0), null);
		}
	}

	private void PopFunctionNode(ZNameSpace NameSpace) {
		this.CurrentFunctionNode = this.CurrentFunctionNode.Pop();
		this.VarScope = this.VarScope.Parent;
	}

	@Override public void VisitFunctionNode(ZFunctionNode Node) {
		@Var ZNameSpace NameSpace = Node.FuncBlock.GetNameSpace();
		@Var ZType ContextType = this.GetContextType();
		if(!this.HasReturn(Node.FuncBlock)) {
			Node.FuncBlock.Append(new ZReturnNode(Node));
		}
		this.PushFunctionNode(NameSpace, Node, ContextType);
		this.VarScope.TypeCheckFuncBlock(this, Node);
		this.PopFunctionNode(NameSpace);
		@Var ZFuncType FuncType = Node.GetFuncType(ContextType);
		if(this.IsTopLevel() && !FuncType.IsVarType()) {
			this.TypedNode(Node, ZType.VoidType);
		}
		else {
			this.TypedNode(Node, FuncType);
		}
	}

	@Override public void VisitClassDeclNode(ZClassDeclNode Node) {
		@Var ZNameSpace NameSpace = Node.GetNameSpace();
		@Var ZType ClassType = NameSpace.GetType(Node.ClassName, Node.SourceToken);
		if(ClassType instanceof ZenClassType) {
			if(!ClassType.IsOpenType()) {
				this.Return(new ZErrorNode(Node, Node.ClassName + " has been defined."));
				return;
			}
			Node.ClassType = (ZenClassType)ClassType;
		}
		else {
			this.Return(new ZErrorNode(Node, Node.ClassName + " is not a Zen class."));
			return;
		}
		if(Node.SuperType != null) {
			if(Node.SuperType instanceof ZenClassType && !Node.SuperType.IsOpenType()) {
				Node.ClassType.ResetSuperType((ZenClassType)Node.SuperType);
			}
			else {
				this.Return(new ZErrorNode(Node, "" + Node.SuperType + " cannot be extended."));
				return;
			}
		}
		@Var int i = 0;
		while(i < Node.FieldList.size()) {
			@Var ZFieldNode FieldNode = Node.FieldList.get(i);
			FieldNode.ClassType = Node.ClassType;
			if(FieldNode.InitNode == null) {
				FieldNode.InitNode = ZenGamma.CreateDefaultValueNode(FieldNode, FieldNode.DeclType, FieldNode.FieldName);
			}
			FieldNode.InitNode = this.CheckType(FieldNode.InitNode, FieldNode.DeclType);
			if(FieldNode.DeclType.IsVarType()) {
				FieldNode.DeclType = FieldNode.InitNode.Type;
				if(FieldNode.DeclType.IsVarType()) {
					this.Return(new ZErrorNode(FieldNode, "type of " + FieldNode.FieldName + " is unspecific"));
					return;
				}
			}
			Node.ClassType.AppendField(FieldNode.DeclType, FieldNode.FieldName, FieldNode.SourceToken);
			i = i + 1;
		}
		Node.ClassType.TypeFlag = ZUtils.UnsetFlag(Node.ClassType.TypeFlag, ZTypeFlag.OpenType);
		this.Return(Node.ClassType.CheckAllFields(NameSpace));
		this.TypedNode(Node, ZType.VoidType);
	}

	@Override public void VisitErrorNode(ZErrorNode Node) {
		this.Return(Node);
	}

}

