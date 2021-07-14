package org.geotools.data.complex.util;

import java.util.ArrayList;
import java.util.Collection;
import org.geotools.data.complex.util.XPathUtil.StepList;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.type.Types;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.FeatureTypeFactory;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class ComplexFeatureTypeReprojector {

    private FeatureTypeFactory ftf = CommonFactoryFinder.getFeatureTypeFactory(null);

    private CoordinateReferenceSystem crs;

    public ComplexFeatureTypeReprojector(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    /**
     * Reproject specific geometry by path and/or default geometry
     *
     * @param descr the descriptor of the feature
     * @param geometryPath the path of the geometry that needs to be reprojected (or null if n/a)
     * @param reprojectDefaultDescriptor whether default geometry must be reprojected
     * @return reprojected feature type
     */
    public AttributeDescriptor reprojectAttribute(
            AttributeDescriptor descr,
            XPathUtil.StepList geometryPath,
            boolean reprojectDefaultDescriptor) {
        if (!(descr.getType() instanceof ComplexType)) {
            return descr;
        }
        AttributeType newType =
                reprojectType(descr.getType(), geometryPath, reprojectDefaultDescriptor);

        AttributeDescriptor ad =
                ftf.createAttributeDescriptor(
                        newType,
                        descr.getName(),
                        descr.getMinOccurs(),
                        descr.getMaxOccurs(),
                        descr.isNillable(),
                        descr.getDefaultValue());
        ad.getUserData().putAll(descr.getUserData());

        return ad;
    }

    private AttributeType reprojectType(
            AttributeType type,
            XPathUtil.StepList geometryPath,
            boolean reprojectDefaultDescriptor) {
        if (!(type instanceof ComplexType)) {
            return type;
        }
        ComplexType complexType = (ComplexType) type;
        GeometryDescriptor defaultGeom = null;
        GeometryDescriptor reprojectedDefaultGeom = null;
        if (type instanceof FeatureType) {
            defaultGeom = ((FeatureType) type).getGeometryDescriptor();
            if (reprojectDefaultDescriptor && defaultGeom != null) {
                reprojectedDefaultGeom = reprojectGeometry(defaultGeom);
            }
        }
        Collection<PropertyDescriptor> schema = new ArrayList<>();
        for (PropertyDescriptor descr : complexType.getDescriptors()) {
            if (reprojectDefaultDescriptor && descr.equals(defaultGeom)) {
                schema.add(reprojectedDefaultGeom);
            } else if (geometryPath != null
                    && !geometryPath.isEmpty()
                    && Types.equals(descr.getName(), geometryPath.get(0).getName())) {
                if (geometryPath.size() > 1) {
                    StepList subPath = new StepList(geometryPath);
                    subPath.remove(0);
                    schema.add(reprojectAttribute((AttributeDescriptor) descr, subPath, false));
                } else {
                    schema.add(reprojectGeometry((GeometryDescriptor) descr));
                }
            } else {
                schema.add(descr);
            }
        }

        ComplexType newType;
        if (type instanceof FeatureType) {
            FeatureType featType = (FeatureType) type;
            newType =
                    ftf.createFeatureType(
                            featType.getName(),
                            schema,
                            featType.getGeometryDescriptor(),
                            featType.isAbstract(),
                            featType.getRestrictions(),
                            featType.getSuper(),
                            featType.getDescription());
        } else {
            newType =
                    ftf.createComplexType(
                            type.getName(),
                            schema,
                            type.isIdentified(),
                            type.isAbstract(),
                            type.getRestrictions(),
                            reprojectType(type.getSuper(), geometryPath, false),
                            type.getDescription());
        }
        newType.getUserData().putAll(type.getUserData());
        return newType;
    }

    private GeometryDescriptor reprojectGeometry(GeometryDescriptor descr) {
        GeometryType type =
                ftf.createGeometryType(
                        descr.getType().getName(),
                        descr.getType().getBinding(),
                        crs,
                        descr.getType().isIdentified(),
                        descr.getType().isAbstract(),
                        descr.getType().getRestrictions(),
                        descr.getType().getSuper(),
                        descr.getType().getDescription());
        type.getUserData().putAll(descr.getType().getUserData());
        GeometryDescriptor gd =
                ftf.createGeometryDescriptor(
                        type,
                        descr.getName(),
                        descr.getMinOccurs(),
                        descr.getMaxOccurs(),
                        descr.isNillable(),
                        descr.getDefaultValue());
        gd.getUserData().putAll(descr.getUserData());
        return gd;
    }
}
