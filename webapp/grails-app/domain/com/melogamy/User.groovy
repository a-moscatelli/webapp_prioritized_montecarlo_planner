package com.melogamy
import org.bson.types.ObjectId

class User {

	ObjectId id
	String code
	static hasMany = [processOfUser: ProcessOfUser, planOfUser: PlanOfUser]
	Date dateCreated		// grails si incarica automaticamente di impostarlo, ma va nominato qui esplicitamente!
	Date lastUpdated		// grails si incarica automaticamente di impostarlo, ma va nominato qui esplicitamente!
	String comment
	Map uconfig
	//List myprocesses
	//String mytasks
	//String myhours
    static constraints = {
		code nullable: false, blank: false, unique: true
		comment nullable: true
    }

}
