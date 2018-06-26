/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package sirius.db.es.constraints;

import com.alibaba.fastjson.JSONObject;
import sirius.db.mixing.query.constraints.Constraint;

/**
 * Defines a constraint which is accepted by {@link sirius.db.es.ElasticQuery} and most probably generated by
 * {@link ElasticFilterFactory}.
 *
 * @see sirius.db.es.Elastic#FILTERS
 */
public class ElasticConstraint extends Constraint {

    private JSONObject constraint;

    /**
     * Creates a new constraint represented as JSON.
     *
     * @param constraint the JSON making up the constraint
     */
    public ElasticConstraint(JSONObject constraint) {
        this.constraint = constraint;
    }

    @Override
    public void asString(StringBuilder builder) {
        builder.append(constraint);
    }

    public JSONObject toJSON() {
        return constraint;
    }
}
