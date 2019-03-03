package org.geotools.filter.function;

import static org.geotools.filter.capability.FunctionNameImpl.parameter;

import java.util.ArrayList;
import java.util.List;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.opengis.filter.capability.FunctionName;

public class FilterFunction_lapply extends FunctionExpressionImpl {

    public static FunctionName NAME =
            new FunctionNameImpl(
                    "lapply",
                    parameter("result", List.class),
                    parameter("source", List.class),
                    parameter("expression", Object.class));

    public FilterFunction_lapply() {
        super(NAME);
    }

    @Override
    public Object evaluate(Object feature) {
        List<?> source = getExpression(0).evaluate(feature, List.class);
        if (source == null) {
            return null;
        }

        List<Object> result = new ArrayList<Object>();
        for (Object item : source) {
            result.add(getExpression(1).evaluate(item));
        }

        return result;
    }
}
