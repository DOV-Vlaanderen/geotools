/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2017, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.filter.function.color;

import static org.geotools.filter.capability.FunctionNameImpl.parameter;

import java.awt.Color;
import org.geotools.filter.FunctionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.opengis.filter.capability.FunctionName;

/**
 * Tint lesscss.org color function. Takes one colors and mixes it with white based on a weight (and
 * their eventual alpha)
 *
 * @author Andrea Aime - GeoSolutions
 */
public class GrayscaleFunction extends FunctionImpl {

    public static FunctionName NAME =
            new FunctionNameImpl(
                    "grayscale", parameter("result", Color.class), parameter("color", Color.class));

    public GrayscaleFunction() {
        this.functionName = NAME;
    }

    @Override
    public Object evaluate(Object object) {
        Color color = (Color) getParameterValue(object, 0);

        HSLColor hsl = new HSLColor(color);
        hsl.setSaturation(0);
        return hsl.toRGB();
    }
}
