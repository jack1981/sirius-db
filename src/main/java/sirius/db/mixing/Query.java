/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package sirius.db.mixing;

public abstract class Query<Q, E extends BaseEntity<?>> extends BaseQuery<Q, E> {

    protected Query(EntityDescriptor descriptor) {
        super(descriptor);
    }

    /**
     * Adds a condition which determines which documents should be selected.
     *
     * @param key   the name of the field to filter on
     * @param value the value to filter on
     * @return the query itself for fluent method calls
     */
    public abstract Q eq(Mapping key, Object value);

    /**
     * Adds a condition which determines which documents should be selected, if the value is non-null.
     * <p>
     * If the given value is <tt>null</tt>, the constraint is skipped.
     *
     * @param field the field to check
     * @param value the value to filter on
     * @return the query itself for fluent method calls
     */
    public abstract Q eqIgnoreNull(Mapping field, Object value);

    /**
     * Adds a sort constraint to order by the given field ascending.
     *
     * @param field the field to order by.
     * @return the builder itself for fluent method calls
     */
    public abstract Q orderAsc(Mapping field);

    /**
     * Adds a sort constraint to order by the given field descending.
     *
     * @param field the field to order by.
     * @return the builder itself for fluent method calls
     */
    public abstract Q orderDesc(Mapping field);

    /**
     * Executes the query and counts the number of results.
     *
     * @return the number of matched result entries
     */
    public abstract long count();

    /**
     * Determines if the query would have at least one matching entity.
     *
     * @return <tt>true</tt> if at least one entity matches the query, <tt>false</tt> otherwise.
     */
    public abstract boolean exists();
}