package com.melogamy
import org.bson.types.ObjectId

class ProcessOfUser {
	ObjectId id
	String label
	static hasMany = [taskOfProcessOfUser: TaskOfProcessOfUser]
	Date dateCreated		// grails si incarica automaticamente di impostarlo, ma va nominato qui esplicitamente!
	Date lastUpdated		// grails si incarica automaticamente di impostarlo, ma va nominato qui esplicitamente!
	//static hasOne = [processes: ProcessOfUser]
	String limit
	float weight
	String comment
	static belongsTo = [user: User]
	// hence when an User instance is deleted so are all its associated ProcessOfUser instances.

	static mapping = {
        user index:true
    }
    static constraints = {
		label nullable: false, blank: false, unique: false
		comment nullable: true
    }
}
