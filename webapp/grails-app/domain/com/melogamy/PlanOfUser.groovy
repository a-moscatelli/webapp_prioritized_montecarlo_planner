package com.melogamy
import org.bson.types.ObjectId

class PlanOfUser {
	ObjectId id
	List hours
	Map config
	Date dateCreated		// grails si incarica automaticamente di impostarlo, ma va nominato qui esplicitamente!
	Date lastUpdated		// grails si incarica automaticamente di impostarlo, ma va nominato qui esplicitamente!
	static belongsTo = [user: User]
	static mapping = {
        user index:true
    }
    //static constraints = {}
}
