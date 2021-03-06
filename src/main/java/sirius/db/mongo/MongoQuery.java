/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package sirius.db.mongo;

import sirius.db.mixing.EntityDescriptor;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.Mixing;
import sirius.db.mixing.query.Query;
import sirius.db.mixing.query.constraints.FilterFactory;
import sirius.db.mongo.constraints.MongoConstraint;
import sirius.db.mongo.facets.MongoFacet;
import sirius.kernel.di.std.Part;
import sirius.kernel.health.Exceptions;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Creates a new query against MongoDB.
 *
 * @param <E> the type of entities being queried.
 */
public class MongoQuery<E extends MongoEntity> extends Query<MongoQuery<E>, E, MongoConstraint> {

    private final Finder finder;

    private List<MongoFacet> facets;

    @Part
    private static Mango mango;

    @Part
    private static Mongo mongo;

    /**
     * Creates a new query for the given descriptor.
     *
     * @param descriptor the descriptor of the entities being queried
     */
    protected MongoQuery(EntityDescriptor descriptor) {
        super(descriptor);
        this.finder = mongo.find(descriptor.getRealm());
    }

    /**
     * Limits the fields being returned to the given list.
     *
     * @param fieldsToReturn specified the list of fields to return
     * @return the builder itself for fluent method calls
     */
    public MongoQuery<E> fields(Mapping... fieldsToReturn) {
        finder.selectFields(fieldsToReturn);

        return this;
    }

    @Override
    public MongoQuery<E> eq(Mapping key, Object value) {
        finder.where(key.toString(), value);
        return this;
    }

    @Override
    public MongoQuery<E> eqIgnoreNull(Mapping field, Object value) {
        if (value != null) {
            return eq(field, value);
        } else {
            return this;
        }
    }

    @Override
    public MongoQuery<E> where(MongoConstraint constraint) {
        if (constraint != null) {
            finder.where(constraint);
        }

        return this;
    }

    @Override
    public MongoQuery<E> orderAsc(Mapping field) {
        finder.orderByAsc(field.toString());
        return this;
    }

    @Override
    public MongoQuery<E> orderDesc(Mapping field) {
        finder.orderByDesc(field.toString());
        return this;
    }

    /**
     * Adds a limit to the query.
     *
     * @param skip  the number of items to skip (used for pagination).
     * @param limit the max. number of items to return (exluding those who have been skipped).
     * @return the builder itself for fluent method calls
     */
    public MongoQuery<E> limit(int skip, int limit) {
        this.skip = skip;
        this.limit = limit;
        finder.limit(skip, limit);

        return this;
    }

    @Override
    public MongoQuery<E> skip(int skip) {
        this.skip = skip;
        finder.skip(skip);
        return this;
    }

    @Override
    public MongoQuery<E> limit(int limit) {
        this.limit = limit;
        finder.limit(limit);
        return this;
    }

    @Override
    public void iterate(Predicate<E> resultHandler) {
        finder.eachIn(descriptor.getRelationName(), doc -> resultHandler.test(Mango.make(descriptor, doc)));
    }

    @Override
    public long count() {
        return finder.countIn(descriptor.getRelationName());
    }

    @Override
    public boolean exists() {
        return finder.copyFilters().selectFields(MongoEntity.ID).singleIn(descriptor.getRelationName()).isPresent();
    }

    /**
     * Returns a list of all items in the result, in a random order.
     * <p>
     * Internally, this uses a <tt>$sample</tt> aggregation with size equal to {@link #limit}.
     * Note that large results should be processed using {@link #iterate(Predicate)} or
     * {@link #iterateAll(Consumer)} as they are more memory efficient.
     *
     * @return a list of items in the query or an empty list if the query did not match any items
     */
    public List<E> randomList() {
        List<E> result = new ArrayList<>();

        // Ensure a sane limit...
        if (limit <= 0 || limit > MAX_LIST_SIZE) {
            throw Exceptions.handle()
                            .to(Mixing.LOG)
                            .withSystemErrorMessage("When using 'randomList' a limit (below %s) has to be provided. "
                                                    + "Query: %s", MAX_LIST_SIZE, this)
                            .handle();
        }

        finder.sample(descriptor.getRelationName(), doc -> {
            result.add(Mango.make(descriptor, doc));
            failOnOverflow(result);
            return true;
        });

        return result;
    }

    @Override
    public void delete(@Nullable Consumer<E> entityCallback) {
        iterateAll(entity -> {
            if (entityCallback != null) {
                entityCallback.accept(entity);
            }

            mango.delete(entity);
        });
    }

    @Override
    public void truncate() {
        Deleter deleter = mongo.delete();
        finder.transferFilters(deleter);
        deleter.manyFrom(descriptor.getRelationName());
    }

    @Override
    public FilterFactory<MongoConstraint> filters() {
        return QueryBuilder.FILTERS;
    }

    /**
     * Adds a facet to be later executed using {@link #executeFacets()}.
     *
     * @param facet the facet to add
     * @return the query itself for fluent method calls
     */
    public MongoQuery<E> addFacet(MongoFacet facet) {
        if (facets == null) {
            facets = new ArrayList<>();
        }

        facets.add(facet);
        return this;
    }

    /**
     * Executes all previously attached facets in one go.
     */
    public void executeFacets() {
        finder.executeFacets(descriptor, facets);
    }

    @Override
    public String toString() {
        return descriptor.getType() + ": " + finder.toString();
    }
}
