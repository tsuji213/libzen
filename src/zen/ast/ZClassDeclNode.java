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

package zen.ast;

import java.util.ArrayList;

import zen.deps.Field;
import zen.lang.ZenClassType;
import zen.parser.ZNameSpace;
import zen.parser.ZVisitor;
import zen.type.ZType;

public final class ZClassDeclNode extends ZNode {
	@Field public String ClassName = null;
	@Field public ZenClassType ClassType = null;
	@Field public ZType SuperType = null;
	@Field public ArrayList<ZFieldNode>  FieldList = new ArrayList<ZFieldNode>();
	public ZClassDeclNode(ZNode ParentNode) {
		super(ParentNode, null);
	}

	@Override public void Append(ZNode Node) {
		if(Node instanceof ZFieldNode) {
			this.FieldList.add((ZFieldNode)this.SetChild(Node));
		}
		else if(Node instanceof ZTypeNode) {
			this.SuperType = Node.Type;
		}
		else if(this.ClassName == null) {
			this.ClassName = Node.SourceToken.ParsedText;
			this.SourceToken = Node.SourceToken;
		}
	}
	@Override public void Accept(ZVisitor Visitor) {
		Visitor.VisitClassDeclNode(this);
	}

	public ZNode CheckClassName(ZNameSpace NameSpace) {
		if(this.ClassType == null || !(this.ClassType instanceof ZenClassType)) {
			return new ZErrorNode(this, this.SourceToken, "" + this.ClassName + " is not a Zen class.");
		}
		if(!this.ClassType.IsOpenType()) {
			return new ZErrorNode(this, this.SourceToken, "" + this.ClassName + " has been defined.");
		}
		if(this.SuperType != null) {
			if(!(this.SuperType instanceof ZenClassType)) {
				return new ZErrorNode(this, this.SourceToken, "" + this.SuperType + " cannot be extended.");
			}
			if(this.SuperType.IsOpenType()) {
				NameSpace.Generator.Logger.ReportWarning(this.SourceToken, "" + this.SuperType + " is not defined with class.");
				//				return new ZenErrorNode(this.SourceToken, "" + this.SuperType + " is not defined with class.");
			}
			this.ClassType.ResetSuperType((ZenClassType)this.SuperType);
		}
		return null;
	}

}