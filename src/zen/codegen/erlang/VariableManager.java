//ifdef JAVA

package zen.codegen.erlang;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import zen.deps.Field;

//endif VAJA

public class VariableManager {
	@Field private HashMap<String, Variable> CurrentMap;
	@Field private ArrayList<HashMap<String, Variable>> MapList;

	public VariableManager/*constructor*/() {
		this.CurrentMap = new HashMap<String, Variable>();
		this.MapList = new ArrayList<HashMap<String, Variable>>();
	}

	Variable SearchVariable(String VarName) {
		int size;
		Variable Var = this.CurrentMap.get(VarName);
		if (Var == null && (size = this.MapList.size()) > 0) {
			int i = size - 1;
			do {
				Var = MapList.get(i).get(VarName);
				i = i - 1;
			} while(i > 0 && Var != null);
		}
		return Var;
	}

	Variable CreateVariable(String VarName) {
		Variable Var = this.CurrentMap.get(VarName);
		if (Var == null) {
			Var = new Variable();
			this.CurrentMap.put(VarName, Var);
		} else {
			//pass
		}
		return Var;
	}

	String GenVariableName(String VarName) {
		Variable Var = this.SearchVariable(VarName);
		String ret;
		if (Var != null) {
			if (Var.Read == -1) {
				ret = VarName.toUpperCase();
			} else {
				ret = VarName.toUpperCase() + Integer.toString(Var.Read);
			}
		} else {
			//Error handling
			ret = VarName;
		}
		return ret;
	}

	void IncrementVariableNumber(String VarName) {
		Variable Var = this.CurrentMap.get(VarName);
		if (Var == null) {
			Var = this.SearchVariable(VarName);
			if (Var == null) {
				//Error handling
			} else {
				Variable NewVar = this.CreateVariable(VarName);
				NewVar.Read = Var.Read;
				NewVar.Next = Var.Next;
				NewVar.IsUsed = true;
				Var = NewVar;
			}
		}
		Var.Read = Var.Next;
		Var.Next += 1;
	}

	void PushScope() {
		HashMap<String, Variable> NewMap = new HashMap<String, Variable>();
		this.MapList.add(this.CurrentMap);
		this.CurrentMap = NewMap;
	}
	void PopScope() {
		int size = this.MapList.size();
		if (size > 0) {
			HashMap<String, Variable> OldMap = this.CurrentMap;
			this.CurrentMap = this.MapList.get(size - 1);
			this.MapList.remove(size - 1);
			for (Map.Entry<String, Variable> KeyValue : OldMap.entrySet()) {
				Variable OldVar = KeyValue.getValue();
				if (OldVar.IsUsed) {
					Variable CurrentVar = this.CreateVariable(KeyValue.getKey());
					CurrentVar.Next = OldVar.Next;
					CurrentVar.IsUsed = true;
				}
			}
		} else {
			System.out.println("too much call PopScope!!");
		}
	}

	String GenVarTupleOnlyUsed(boolean DoIncrement) {
		String VarTuple = "{";
		for (Map.Entry<String, Variable> KeyValue : this.CurrentMap.entrySet()) {
			Variable Var = KeyValue.getValue();
			if (Var.IsUsed) {
				String VarName = KeyValue.getKey();
				if (DoIncrement) this.IncrementVariableNumber(VarName);
				VarTuple += this.GenVariableName(VarName);
				VarTuple += ", ";
			}
		}
		VarTuple += "pad}";
		return VarTuple;
	}
}
