package libzen.grammar;

import zen.ast.ZNode;
import zen.ast.ZWhileNode;
import zen.deps.Var;
import zen.deps.ZMatchFunction;
import zen.parser.ZTokenContext;

public class WhilePattern extends ZMatchFunction {

	@Override public ZNode Invoke(ZNode ParentNode, ZTokenContext TokenContext, ZNode LeftNode) {
		@Var ZNode WhileNode = new ZWhileNode(ParentNode);
		WhileNode = TokenContext.MatchToken(WhileNode, "while", ZTokenContext.Required);
		WhileNode = TokenContext.MatchToken(WhileNode, "(", ZTokenContext.Required);
		WhileNode = TokenContext.MatchPattern(WhileNode, ZWhileNode._Cond, "$Expression$", ZTokenContext.Required, ZTokenContext.AllowSkipIndent);
		WhileNode = TokenContext.MatchToken(WhileNode, ")", ZTokenContext.Required);
		WhileNode = TokenContext.MatchPattern(WhileNode, ZWhileNode._Block, "$Block$", ZTokenContext.Required);
		return WhileNode;
	}

}
