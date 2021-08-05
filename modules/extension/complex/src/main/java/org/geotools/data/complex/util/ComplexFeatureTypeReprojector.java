package org.geotools.data.complex.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.geotools.data.complex.feature.type.ComplexTypeProxy;
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
     * @param descr the descriptor
     * @return reprojected descriptor
     */
    public AttributeDescriptor reprojectAttribute(AttributeDescriptor descr) {
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
        reprojectSubstitutionGroup(ad);

        return ad;
    }

    protected void reprojectSubstitutionGroup(AttributeDescriptor ad) {
        if (ad.getUserData().containsKey("substitutionGroup")) {
            ArrayList<AttributeDescriptor> newSubstitutionGroup = new ArrayList<>();
            ArrayList<?> substitutionGroup =
                    (ArrayList<?>) ad.getUserData().get("substitutionGroup");
            for (Object o : substitutionGroup) {
                if (o instanceof GeometryDescriptor) {
                    newSubstitutionGroup.add(reprojectGeometryAttribute((GeometryDescriptor) o));
                } else {
                    newSubstitutionGroup.add(reprojectAttribute((AttributeDescriptor) o));
                }
            }
            ad.getUserData().put("substitutionGroup", newSubstitutionGroup);
        }
    }

    /**
     * Reproject compatible geometries.
     *
     * @param type the type
     * @return reprojected type
     */
    public AttributeType reprojectType(AttributeType type) {
        if (!(type instanceof ComplexType)) {
            if (type instanceof GeometryType) {
                return reprojectGeometryType((GeometryType) type);
            } else {
                return type;
            }
        }
        if (processingTypes.contains(type.getName())) {
            if (type instanceof FeatureType) {
                return new FeatureTypeProxy(type.getName(), types);
            } else {
                return new ComplexTypeProxy(type.getName(), types);
            }
        }
        processingTypes.add(type.getName());
        ComplexType complexType = (ComplexType) type;
        GeometryDescriptor defaultGeom = null;
        GeometryDescriptor reprojectedDefaultGeom = null;
        if (type instanceof FeatureType) {
            defaultGeom = ((FeatureType) type).getGeometryDescriptor();
            if (defaultGeom != null) {
                reprojectedDefaultGeom = reprojectGeometryAttribute(defaultGeom);
            }
        }
        Collection<PropertyDescriptor> schema = new ArrayList<>();
        for (PropertyDescriptor descr : complexType.getDescriptors()) {
            if (descr.equals(defaultGeom)) {
                schema.add(reprojectedDefaultGeom);
            } else if (descr instanceof GeometryDescriptor) {
                schema.add(reprojectGeometryAttribute((GeometryDescriptor) descr));
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
                            reprojectedDefaultGeom,
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
        types.put(newType.getName(), newType);
        return newType;
    }

    protected GeometryDescriptor reprojectGeometryAttribute(GeometryDescriptor descr) {
        if (!CRS.isCompatible(crs, descr.getCoordinateReferenceSystem())) {
            return descr;
        }

        GeometryDescriptor gd =
                ftf.createGeometryDescriptor(
                        reprojectGeometryType(descr.getType()),
                        descr.getName(),
                        descr.getMinOccurs(),
                        descr.getMaxOccurs(),
                        descr.isNillable(),
                        descr.getDefaultValue());
        gd.getUserData().putAll(descr.getUserData());
        reprojectSubstitutionGroup(gd);
        return gd;
    }

    protected GeometryType reprojectGeometryType(GeometryType type) {
        if (type.getCoordinateReferenceSystem() != null
                && !CRS.isCompatible(crs, type.getCoordinateReferenceSystem())) {
            return type;
        }

        GeometryType newType =
                ftf.createGeometryType(
                        type.getName(),
                        type.getBinding(),
                        crs,
                        type.isIdentified(),
                        type.isAbstract(),
                        type.getRestrictions(),
                        type.getSuper(),
                        type.getDescription());
        newType.getUserData().putAll(type.getUserData());
        return newType;
    }
}
