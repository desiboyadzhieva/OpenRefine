package com.google.gridworks.gel.controls;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONWriter;

import com.google.gridworks.expr.EvalError;
import com.google.gridworks.expr.Evaluable;
import com.google.gridworks.expr.ExpressionUtils;
import com.google.gridworks.gel.Control;
import com.google.gridworks.gel.ControlFunctionRegistry;
import com.google.gridworks.gel.ast.VariableExpr;

public class ForRange implements Control {
    public String checkArguments(Evaluable[] args) {
        if (args.length != 5) {
            return ControlFunctionRegistry.getControlName(this) + " expects 5 arguments";
        } else if (!(args[3] instanceof VariableExpr)) {
            return ControlFunctionRegistry.getControlName(this) + 
                " expects third argument to be the element's variable name";
        }
        return null;
    }

    public Object call(Properties bindings, Evaluable[] args) {
        Object fromO = args[0].evaluate(bindings);
        Object toO = args[1].evaluate(bindings);
        Object stepO = args[2].evaluate(bindings);
        
        if (ExpressionUtils.isError(fromO)) {
            return fromO;
        } else if (ExpressionUtils.isError(toO)) {
            return toO;
        } else if (ExpressionUtils.isError(stepO)) {
            return stepO;
        } else if (!(fromO instanceof Number) || !(toO instanceof Number) || !(stepO instanceof Number)) {
            return new EvalError("First, second, and third arguments of forRange must all be numbers");
        }
        
        String indexName = ((VariableExpr) args[3]).getName();
        Object oldIndexValue = bindings.get(indexName);

        try {
            List<Object> results = new ArrayList<Object>();
            
            if (isIntegral((Number) fromO) && isIntegral((Number) stepO)) {
                long from = ((Number) fromO).longValue();
                long step = ((Number) stepO).longValue();
                double to = ((Number) toO).doubleValue();
                
                while (from < to) {
                	bindings.put(indexName, from);
                    
                    Object r = args[4].evaluate(bindings);
                    
                    results.add(r);
                    
                    from += step;
                }
            } else {
                double from = ((Number) fromO).longValue();
                double step = ((Number) stepO).longValue();
                double to = ((Number) toO).doubleValue();
                
                while (from < to) {
                	bindings.put(indexName, from);
                    
                    Object r = args[4].evaluate(bindings);
                    
                    results.add(r);
                    
                    from += step;
                }
            }
            return results.toArray(); 
        } finally {
            /*
             *  Restore the old values bound to the variables, if any.
             */
            if (oldIndexValue != null) {
                bindings.put(indexName, oldIndexValue);
            } else {
                bindings.remove(indexName);
            }
        }
    }
    
    static private boolean isIntegral(Number o) {
    	if (o instanceof Integer || o instanceof Long) {
    		return true;
    	} else {
    		return (o.doubleValue() - o.longValue()) == 0;
    	}
    }
    
    public void write(JSONWriter writer, Properties options)
        throws JSONException {
    
        writer.object();
        writer.key("description"); writer.value(
            "Iterates over the variable v starting at \"from\", incrementing by \"step\" each time while less than \"to\". At each iteration, evaluates expression e, and pushes the result onto the result array."
        );
        writer.key("params"); writer.value("number from, number to, number step, variable v, expression e");
        writer.key("returns"); writer.value("array");
        writer.endObject();
    }
}