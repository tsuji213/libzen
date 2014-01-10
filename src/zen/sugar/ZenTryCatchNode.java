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

package zen.sugar;

import java.util.ArrayList;

import zen.ast.ZenCatchNode;
import zen.ast.ZenNode;

final public class ZenTryCatchNode extends ZenNode {
	/*field*/public ZenNode	TryNode;
	/*field*/public ArrayList<ZenNode> 	CatchList;
	/*field*/public ZenNode	FinallyNode;
	public ZenTryCatchNode/*constructor*/() {
		super();
		this.TryNode = null;
		this.FinallyNode = null;
		this.CatchList = new ArrayList<ZenNode>();
	}
	@Override public void Append(ZenNode Node) {
		this.SetChild(Node);
		if(Node instanceof ZenCatchNode) {
			this.CatchList.add(Node);
		}
		else if(this.TryNode == null) {
			this.TryNode = Node;
		}
		else {
			this.FinallyNode = Node;
		}
	}
	//	@Override public void Accept(ZenVisitor Visitor) {
	//		Visitor.VisitTryNode(this);
	//	}
}