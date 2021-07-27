package org.geotools.data.complex.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.geotools.data.complex.feature.type.FeatureTypeProxy;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.referencing.CRS;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.FeatureTypeFactory;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class ComplexFeatureTypeReprojector {

    private FeatureTypeFactory ftf = CommonFactoryFinder.getFeatureTypeFactory(null);
    
    private Map<Name, AttributeType> types = new HashMap<>();
    
    private Set<Name> processingTypes = new HashSet<>();

    private CoordinateReferenceSystem crs;

    public ComplexFeatureTypeReprojector(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    /**
     * Reproject compatible geometries.
     *
     * @param descr the descriptor of the feature
     * @param geometryPath the path of the geometry that needs to be reprojected (or null if n/a)
     * @param reprojectDefaultDescriptor whether default geometry must be reprojected
     * @return reprojected feature type
     */
    public AttributeDescriptor reprojectAttribute(AttributeDescriptor descr) {
        if (!(descr.getType() instanceof ComplexType)) {
            return descr;
        }
        AttributeType newType = reprojectType(descr.getType());

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

    private AttributeType reprojectType(AttributeType type) {
        if (!(type instanceof ComplexType)) {
            return type;
        }
        if (processingTypes.contains(type.getName())) {
            return new FeatureTypeProxy(type.getName(), types);
        }
        processingTypes.add(type.getName());
        ComplexType complexType = (ComplexType) type;
        GeometryDescriptor defaultGeom = null;
        GeometryDescriptor reprojectedDefaultGeom = null;
        if (type instanceof FeatureType) {
            defaultGeom = ((FeatureType) type).getGeometryDescriptor();
            if (CRS.isCompatible(crs, defaultGeom.getCoordinateReferenceSystem())) {
                reprojectedDefaultGeom = reprojectGeometry(defaultGeom);
            } else {
                reprojectedDefaultGeom = defaultGeom;
            }
        }
        Collection<PropertyDescriptor> schema = new ArrayList<>();
        for (PropertyDescriptor descr : complexType.getDescriptors()) {
            if (descr.equals(defaultGeom)) {
                schema.add(reprojectedDefaultGeom);
            } else if (descr instanceof GeometryDescriptor
                    && CRS.isCompatible(
                            crs, ((GeometryDescriptor) descr).getCoordinateReferenceSystem())) {
                schema.add(reprojectGeometry((GeometryDescriptor) descr));
            } else if (descr instanceof AttributeDescriptor) {
                schema.add(reprojectAttribute((AttributeDescriptor) descr));
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
                            reprojectType(type.getSuper()),
                            type.getDescription());
        }
        newType.getUserData().putAll(type.getUserData());
        types.put(newType.getName(), type);
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
