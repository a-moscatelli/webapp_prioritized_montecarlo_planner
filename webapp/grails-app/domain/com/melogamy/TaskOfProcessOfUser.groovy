package com.melogamy
import org.bson.types.ObjectId

class TaskOfProcessOfUser {
	ObjectId id
	String label
	Date dateCreated		// grails si incarica automaticamente di impostarlo, ma va nominato qui esplicitamente!
	Date lastUpdated		// grails si incarica automaticamente di impostarlo, ma va nominato qui esplicitamente!
	String etc
	int seqno
	String comment
	
	static belongsTo = [processOfUser: ProcessOfUser]
	// hence when an ProcessOfUser instance is deleted so are all its associated TaskOfProcessOfUser instances.
	static mapping = {
        processOfUser index:true
    }
    static constraints = {
		label nullable: false, blank: false, unique: false
		comment nullable: true
    }
}
