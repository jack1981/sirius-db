/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package sirius.db.mongo

import sirius.db.KeyGenerator
import sirius.kernel.BaseSpecification
import sirius.kernel.di.std.Part

class MongoSpec extends BaseSpecification {

    @Part
    private static Mongo mongo

    @Part
    private static KeyGenerator keyGen

    def "basic read / write works"() {
        given:
        def testString = String.valueOf(System.currentTimeMillis())
        when:
        def result = mongo.insert().set("test", testString).set("id", keyGen.generateId()).into("test")
        then:
        mongo.find().
                where("id", result.getString("id")).
                singleIn("test").
                map({ d -> d.getString("test") }).
                orElse(null) == testString
    }

}