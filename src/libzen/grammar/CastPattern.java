package libzen.grammar;

import zen.ast.ZCastNode;
import zen.ast.ZNode;
import zen.deps.Var;
import zen.deps.ZMatchFunction;
import zen.parser.ZTokenContext;
import zen.type.ZType;

public class CastPattern extends ZMatchFunction {

	@Override public ZNode Invoke(ZNode ParentNode, ZTokenContext TokenContext, ZNode LeftNode) {
		@Var ZNode CastNode = new ZCastNode(ParentNode, ZType.VarType, null);
		CastNode = TokenContext.MatchToken(CastNode, "(", ZTokenContext._Required);
		CastNode = TokenContext.MatchPattern(CastNode, ZNode._TypeInfo, "$Type$", ZTokenContext._Required);
		CastNode = TokenContext.MatchToken(CastNode, ")", ZTokenContext._Required);
		CastNode = TokenContext.MatchPattern(CastNode, ZCastNode._Expr, "$RightExpression$", ZTokenContext._Required);
		return CastNode;
	}

}
