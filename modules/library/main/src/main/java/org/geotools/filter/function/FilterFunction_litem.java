package org.geotools.filter.function;

import static org.geotools.filter.capability.FunctionNameImpl.parameter;

import java.util.List;

import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.opengis.filter.capability.FunctionName;

public class FilterFunction_litem extends FunctionExpressionImpl {

    public static FunctionName NAME =
            new FunctionNameImpl(
                    "litem",
                    parameter("result", Object.class),
                    parameter("source", List.class),
                    parameter("index", Integer.class));

    public FilterFunction_litem() {
        super(NAME);
    }

    @Override
    public Object evaluate(Object feature) {
        List<?> source = getExpression(0).evaluate(feature, List.class);
        if (source == null) {
            return null;
        }
        Integer index = getExpression(1).evaluate(feature, Integer.class);

        return source.get(index);
    }
}
