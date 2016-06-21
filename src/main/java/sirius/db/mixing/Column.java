/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package sirius.db.mixing;

import javax.annotation.Nullable;

/**
 * Represents a column (property) name which is used in queries.
 * <p>
 * For each field, a <tt>Column</tt> with the same name must be defined. This column is used to reference the
 * field (or its property) in queries. This adds syntactic checks and permits refactorings (renaming etc.).
 * <p>
 * An example for a field with its column would be:
 * <pre>
 * {@code
 *    private int age;
 *    public static final Column AGE = Column.named("age");
 * }
 * </pre>
 */
public class Column {

    /**
     * Used to join several field names (e.g. for composites or mixins).
     */
    public static final String SUBFIELD_SEPARATOR = "_";

    /**
     * Contains the name of the represented field
     */
    private final String name;

    /**
     * Contains the parent in case this field resides in a composite or mixin
     */
    private final Column parent;

    /*
     * Creates a new column. Use named(String) to create a new constant within a class. Use inner(Column) or
     * join(Column) to access composites, mixins or referenced entities.
     */
    private Column(String name, Column parent) {
        this.name = name;
        this.parent = parent;
    }

    /**
     * Creates a new <tt>Column</tt>. This should be used to create a <tt>public static final</tt> constant
     * in the class where the field is defined.
     *
     * @param name the name of the represented field
     * @return a column representing the field with the given name
     */
    public static Column named(String name) {
        return new Column(name, null);
    }

    /**
     * Creates a new <tt>Column></tt> for a mixin class.
     *
     * @param mixinType the class which defines the mixin
     * @return a column representing the mixin
     */
    public static Column mixin(Class<?> mixinType) {
        return new Column(mixinType.getSimpleName(), null);
    }

    /**
     * References an inner field of a composite represented by this column.
     *
     * @param inner the inner field of the composite represented by this column
     * @return a column representing the combined path of this column and inner field
     */
    public Column inner(Column inner) {
        return new Column(name + SUBFIELD_SEPARATOR + inner.name, null);
    }

    /**
     * References a mixin for an inner composite or referenced type.
     *
     * @param mixinType t the class which defines the mixin
     * @return a column representing the combined path of this column and the given mixin
     */
    public Column inMixin(Class<?> mixinType) {
        return new Column(name + SUBFIELD_SEPARATOR + mixinType.getSimpleName(), null);
    }

    /**
     * Joins the referenced field described by <tt>joinColumn</tt>.
     * <p>
     * Note that this column needs to represent an <tt>EntityRef</tt> field.
     *
     * @param joinColumn the column of the referenced entity to join
     * @return a column which joins the entity represented by this column and accesses the given <tt>joinColumn</tt>
     */
    public Column join(Column joinColumn) {
        return new Column(joinColumn.name, this);
    }

    /**
     * Returns the field name for which this column was created.
     * <p>
     * Note that this is not necessarily the property name as for properties in mixins or compounds, the
     * parent fields are appended separated by {@link #SUBFIELD_SEPARATOR}.
     *
     * @return the field name for which this column was created
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the parent field.
     *
     * @return the parent field (the composite or mixin which contains this column). Returns <tt>null</tt> if this is
     * a top-level field of an entity
     */
    @Nullable
    public Column getParent() {
        return parent;
    }

    @Override
    public String toString() {
        if (parent != null) {
            return parent.toString() + "." + name;
        } else {
            return name;
        }
    }
}