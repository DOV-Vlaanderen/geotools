package org.geotools.filter.function;

import static org.geotools.filter.capability.FunctionNameImpl.parameter;

import java.util.Collection;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.opengis.filter.capability.FunctionName;

public class FilterFunction_size extends FunctionExpressionImpl {

    public static FunctionName NAME =
            new FunctionNameImpl(
                    "size",
                    parameter("result", Integer.class),
                    parameter("source", Collection.class));

    public FilterFunction_size() {
        super(NAME);
    }

    @Override
    public Object evaluate(Object feature) {
        Collection<?> source = getExpression(0).evaluate(feature, Collection.class);
        if (source == null) {
            return null;
        }

        return source.size();
    }
}
