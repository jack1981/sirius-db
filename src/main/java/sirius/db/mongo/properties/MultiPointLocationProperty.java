/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package sirius.db.mongo.properties;

import org.bson.Document;
import sirius.db.mixing.AccessPath;
import sirius.db.mixing.EntityDescriptor;
import sirius.db.mixing.Mixing;
import sirius.db.mixing.Property;
import sirius.db.mixing.PropertyFactory;
import sirius.db.mongo.MongoEntity;
import sirius.db.mongo.types.MultiPointLocation;
import sirius.kernel.commons.Lambdas;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.commons.Value;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Permits to store a {@link MultiPointLocation} as location aware "MultiPoint" in MongoDB.
 */
public class MultiPointLocationProperty extends Property {

    /**
     * Factory for generating properties based on their field type
     */
    @Register
    public static class Factory implements PropertyFactory {

        @Override
        public boolean accepts(Field field) {
            return MongoEntity.class.isAssignableFrom(field.getDeclaringClass()) && MultiPointLocation.class.equals(
                    field.getType());
        }

        @Override
        public void create(EntityDescriptor descriptor,
                           AccessPath accessPath,
                           Field field,
                           Consumer<Property> propertyConsumer) {
            if (!Modifier.isFinal(field.getModifiers())) {
                Mixing.LOG.WARN("Field %s in %s is not final! This will probably result in errors.",
                                field.getName(),
                                field.getDeclaringClass().getName());
            }

            propertyConsumer.accept(new MultiPointLocationProperty(descriptor, accessPath, field));
        }
    }

    MultiPointLocationProperty(EntityDescriptor descriptor, AccessPath accessPath, Field field) {
        super(descriptor, accessPath, field);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object transformToDatasource(Object object) {
        List<?> locationList = ((List<Tuple<Double, Double>>) object).stream().map(latLong -> {
            List<Object> coordinates = new ArrayList<>();
            coordinates.add(latLong.getFirst());
            coordinates.add(latLong.getSecond());
            return coordinates;
        }).collect(Collectors.toList());

        return new Document().append("type", "MultiPoint").append("coordinates", locationList);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object transformFromDatasource(Value object) {
        List<Tuple<Double, Double>> locations = new ArrayList<>();

        Object coordiantes = ((Document) object.get()).get("coordinates");
        if (coordiantes instanceof List<?>) {
            ((List<List<Double>>) coordiantes).stream().map(entry -> {
                return Tuple.create(entry.get(0), entry.get(1));
            }).collect(Lambdas.into(locations));
        }

        return locations;
    }

    @Override
    public Object transformValue(Value value) {
        if (value.isEmptyString()) {
            return Collections.emptyList();
        }

        List<Tuple<Double, Double>> result = new ArrayList<>();
        for (String location : value.asString().split("\n")) {
            Tuple<String, String> latLong = Strings.split(location, ",");
            try {
                Tuple<Double, Double> coordinate =
                        Tuple.create(Double.parseDouble(latLong.getFirst()), Double.parseDouble(latLong.getSecond()));
                result.add(coordinate);
            } catch (NumberFormatException e) {
                Exceptions.ignore(e);
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void setValueToField(Object value, Object target) {
        ((MultiPointLocation) super.getValueFromField(target)).setData((List<Tuple<Double, Double>>) value);
    }

    @Override
    protected Object getValueFromField(Object target) {
        return ((MultiPointLocation) super.getValueFromField(target)).data();
    }

    @Override
    public Object getValueAsCopy(Object entity) {
        return ((MultiPointLocation) super.getValueFromField(entity)).copyList();
    }
}