package org.geotools.filter.function;

import static org.geotools.filter.capability.FunctionNameImpl.parameter;

import java.util.ArrayList;
import java.util.List;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.opengis.filter.capability.FunctionName;

public class FilterFunction_literate extends FunctionExpressionImpl {

    public static FunctionName NAME =
            new FunctionNameImpl(
                    "literate",
                    parameter("result", List.class),
                    parameter("times", Integer.class),
                    parameter("expression", Object.class));

    public FilterFunction_literate() {
        super(NAME);
    }

    @Override
    public Object evaluate(Object feature) {
        Integer size = getExpression(0).evaluate(feature, Integer.class);
        if (size == null) {
            return null;
        }

        List<Object> result = new ArrayList<Object>();
        for (int i = 0; i < size; i++) {
            result.add(getExpression(1).evaluate(i));
        }

        return result;
    }
}
