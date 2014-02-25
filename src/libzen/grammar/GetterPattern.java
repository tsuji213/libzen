package libzen.grammar;

import zen.ast.ZGetterNode;
import zen.ast.ZNode;
import zen.deps.Var;
import zen.deps.ZMatchFunction;
import zen.parser.ZTokenContext;

public class GetterPattern extends ZMatchFunction {

	@Override public ZNode Invoke(ZNode ParentNode, ZTokenContext TokenContext, ZNode LeftNode) {
		@Var ZNode GetterNode = new ZGetterNode(ParentNode, LeftNode);
		GetterNode = TokenContext.MatchToken(GetterNode, ".", ZTokenContext._Required);
		GetterNode = TokenContext.MatchPattern(GetterNode, ZNode._NameInfo, "$Name$", ZTokenContext._Required);
		return GetterNode;
	}

}
