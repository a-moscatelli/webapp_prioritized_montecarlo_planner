package g4hab

import grails.gorm.transactions.Transactional
import java.text.SimpleDateFormat
import org.bson.types.ObjectId

import com.melogamy.User
import com.melogamy.ProcessOfUser
import com.melogamy.TaskOfProcessOfUser
import com.melogamy.PlanOfUser

@Transactional
class PersistService {

	def utilService
	
	String uid() {	// not in use
		return new ObjectId().toHexString()
	}
	
	int user_c(String user_id) {
		println "LOG_3x7:user_c:$user_id"
		User uu = new User(code:user_id).save(flush: true, failOnError: true)
		return 1
	}

	boolean user_u(String user_id) {
		println "LOG_387:user_u:$user_id"
		User uu = User.findByCode(user_id)
		return uu != null
	}
	
	
	int admin_data_b() {
		println "Users count:" + User.count()
		println "Areas count:" + ProcessOfUser.count()
		println "Activities count:" + TaskOfProcessOfUser.count()
		println "Plans count:" + PlanOfUser.count()
		//
		User.findAll().each {
			println "="*50
			it.properties.each{entry -> println "$entry.key: $entry.value"}
		}
		return 1
	}	
	
	int user_area_count(String user_id) {
		User uu = User.findByCode(user_id)
		if(!uu) return 0
		Set ua = uu.processOfUser
		return ua.size()
	}
	List entity_L(String entity, String user_id, Map params, boolean without_addendum) {
		
		println "LOG_283:entity_L:$entity"
		Map addendum = [:]
		List ret = []
		User uu = User.findByCode(user_id)
		println "LOG_464:uu:" + uu.id
		if(!uu) return null
		
		if(entity=="area") {
			ret = uu.processOfUser.collect {
				[
				id : it.properties.id.toHexString(),
				area: it.properties.label,
				aweight: it.properties.weight.toString(),
				acomment: it.properties.comment,
				alimit: it.properties.limit,
				aactivities: it.taskOfProcessOfUser.size()
				]
			}
			addendum = [
				id:"0",
				area:"", 						
				aweight:".00" , 
				alimit: "(D>=1 and D<=7) and (H>=0 and H<=23)",
				aactivities: 0
			]
		}
		if(entity=="activity") {
			println "LOG_u83:entity_L:$entity:area_id:" + params.area_id
		
			if(! params.area_id) return []		
			ProcessOfUser thearea = ProcessOfUser.get(new ObjectId(params.area_id))
			if(!thearea) return null // 404
			if(thearea.user != uu ) return null // 403	// verification of ownership. I don't want hackers to update other's areas.
			ret = thearea.taskOfProcessOfUser.collect {
				[
				id: it.properties.id.toHexString(),
				activity: it.properties.label,
				areaid: params.area_id,
				area: thearea.label,
				acomment: it.properties.comment,
				etc: it.properties.etc.toString(),
				seqno: it.properties.seqno.toString()
				]
			}
			addendum = [
				id:"0",
				activity:"",
				areaid: params.area_id,				
				etc:"0" , 
				seqno: "1"
			]
		}
		//println "size ret ($entity):" + ret.size()
		if(!without_addendum) {
			ret.add(addendum)
		}
		return ret
	}

	String entity_C(String entity, def parent, Map params) {
		println "LOG_Dft:entity_C:$entity"
		ObjectId id = new ObjectId()
		switch(entity) {
			
			case "area":
			new ProcessOfUser(
				id: id,
				user:	parent, // a user
				label:	params.area,
				comment: params.acomment,
				weight:	Float.parseFloat(params.aweight as String),	// as String <- because the value loaded via snakeyaml is already a double, parseFloat will fail
				limit:	params.alimit
			).save(flush: true, failOnError: true)
			break
			
			case "activity":
			new TaskOfProcessOfUser(
				id: id,
				processOfUser:parent, // a user's area
				label:params.activity, 
				comment: params.acomment,
				etc: params.etc,
				seqno: Integer.parseInt(params.seqno as String) // as String <- because the value loaded via snakeyaml is already an int, parseInt will fail
			).save(flush: true, failOnError: true)
			break
			
			case "hours":
			new PlanOfUser(
				id: id,
				hours:params.hours,
				config: params.config,
				user:parent // a user
			).save(flush: true, failOnError: true)
			break
			
			default:
				return null
		}
		return id.toHexString()
	}
	
	
	List get_most_recent_plan(String user_id) {
		User uu = User.findByCode(user_id)
		if(!uu) return 403
		Set plans = uu.planOfUser
		PlanOfUser latestPlan = plans.max { it.dateCreated }
		return latestPlan ? latestPlan.hours : []
	}
	
	Map hour_area_stats(String user_id) {	//	for each area: compute the count and count% of hours clocked
		Map area_stat_ret = [:]
		if(user_id) {
			List hours = get_most_recent_plan(user_id)
			float tot_hours = hours.size()
			Map area_stat_m = hours.groupBy { it.area }.collectEntries { k,v -> [ (k): v.size()] } // .sort({k1, k2 -> k1 <=> k2} as Comparator)
			// eg. [area1:area1_count,area2:area2_count]
			area_stat_m.each {
				area_stat_ret.put(it.key, [
					ahourscount: it.value,
					ahourspctcnt: Math.round(100.0 * (it.value / tot_hours))
				])
			}
		}
		return area_stat_ret
	}

	Map hour_activity_stats(String user_id) {	//	for each activity: find the last date = completion date
		Map activity_statm_ret = [:]
		if(user_id) {
			List hours = get_most_recent_plan(user_id)
			Map area_stat_m = hours.groupBy { it.activity+"@"+it.area }.collectEntries { k,v -> [ (k): v.max {it.DT}] } // .sort({k1, k2 -> k1 <=> k2} as Comparator)
			// eg. [area1:area1_count,area2:area2_count]
			area_stat_m.each {
				activity_statm_ret.put(it.key, [
					completion: it.value.DT
				])
			}
		}
		return activity_statm_ret
	}

	
	String entity_U(String entity, String user_id, Map params, boolean creating) {
		User uu = User.findByCode(user_id)
		if(!uu) return null // "403"
		if(creating) {
			def parent
			if(entity=="activity") {
				parent = ProcessOfUser.get(new ObjectId(params.areaid))
				if(!parent) return null // 403
				if(parent.user != uu ) return null // 403	// ownership check
			}
			if(entity=="area") {
				parent=uu
			}
			if(entity=="hours") {
				parent=uu
			}
			return entity_C(entity,parent,params)
		} else {
			println "params.acomment:" + params.acomment
			if(entity=="area") {
				ProcessOfUser thearea = ProcessOfUser.get(new ObjectId(params.id))
				thearea.label =  params.area
				thearea.weight = Float.parseFloat(params.aweight as String) // as String <- because the value loaded via snakeyaml is already a double, parseFloat will fail
				thearea.limit =  params.alimit
				thearea.comment = params.acomment
				thearea.save(flush: true, failOnError: true)
				return params.id // 200
			}
			if(entity=="activity") {
				TaskOfProcessOfUser theactivity = TaskOfProcessOfUser.get(new ObjectId(params.id))
				if(!theactivity) return 404
				if(theactivity.processOfUser.user != uu ) return 403	// verification of ownership. I don't want hackers to update other's areas.
				if(theactivity) {
					theactivity.label =  params.activity
					theactivity.seqno = Integer.parseInt(params.seqno as String) // as String <- because the value loaded via snakeyaml is already an int, parseInt will fail
					theactivity.etc =  params.etc
					theactivity.comment = params.acomment
					theactivity.save(flush: true, failOnError: true)
				}
				return params.id // 200
			}
		}
		return null
	}
	

	
	int entity_D(String entity, String user_id, Map params) {
		User uu = User.findByCode(user_id)
		if(!uu) return 403
		switch(entity) {
			
			case "area":
			String area_id = params.id // maybe "0"
			println "LOG_dy6:area_d:area_id:"+area_id
			if(area_id=="0") return 204
			ProcessOfUser thearea = ProcessOfUser.get(new ObjectId(area_id))
			if(!thearea) return 404
			if(thearea.user != uu ) return 403	// verification of ownership. I don't want hackers to update other's areas.
			thearea.delete(flush: true)
			return 200
			break
			
			case "activity":
			String activity_id = params.id // maybe "0"
			println "LOG_dy6:activity_d:activity_id:"+activity_id
			if(activity_id=="0") return 204
			TaskOfProcessOfUser theactivity = TaskOfProcessOfUser.get(new ObjectId(activity_id))
			if(!theactivity) return 404
			if(theactivity.processOfUser.user != uu ) return 403	// verification of ownership. I don't want hackers to update other's areas.
			println "@"*50
			println theactivity
			println "@"*50
			theactivity.delete(flush: true)
			return 200
			break
			
			default:
			return 500
		}
	}
	
	
	int persist_file(String user_id, Map yaml) {
		
		User uu = User.findByCode(user_id)
		if(!uu) return null // "403"
		
		yaml.streams.each { ss ->
			ObjectId id = new ObjectId()
			println "adding YML area " + ss.stream
			ProcessOfUser pu = new ProcessOfUser(
				id: id,
				user:	uu,
				label:	ss.stream, 
				weight:	Float.parseFloat(ss.weight as String),	// as String <- because the value loaded via snakeyaml is already a double, parseFloat will fail
				limit:	ss.when
			).save(flush: true, failOnError: true)
			
			int seqno=0
			int seqnos
			ss.sequence.each { sq ->
				seqno += 10
				seqnos = Integer.parseInt(seqno as String)
				//yaml.streams[ss].sequence[sq] = seqno
				sq.put("seqno",seqno)
				ObjectId id2 = new ObjectId()
				//also available: addToTaskOfProcessOfUser
				println "adding activity " + sq.project + " to area " + ss.stream + " with seqno " + seqnos
				new TaskOfProcessOfUser(
					id: id2,
					processOfUser:pu, // a user's area
					label:sq.project, 
					etc: sq.ETC,
					seqno: sq.seqno // seqnos // as String <- because the value loaded via snakeyaml is already an int, parseInt will fail
				).save(flush: true, failOnError: true)
			
			}
		}
		return 0
	}
	
	int prop_U(String user_id,Map params) {
		println "storing plan_config " + params
		// prop_U:[genrule:H>=7 AND H<23, date_a:2020-12-18 00:00:00, date_z:, controller:member, format:null, action:prop_U]
		User uu = User.findByCode(user_id)
		if(!uu) return 403 // 403
		uu.uconfig = [ genrule:params.genrule, date_a:params.date_a?.take(4+1+2+1+2), date_z:params.date_z?.take(4+1+2+1+2) ]
		uu.save(flush: true, failOnError: true)
		return 200
	}

	Map prop_R(String user_id, boolean as_text) {
		// prop_U:[genrule:H>=7 AND H<23, date_a:2020-12-18 00:00:00, date_z:, controller:member, format:null, action:prop_U]
		User uu = User.findByCode(user_id)
		if(!uu) return null // 403
		
		Map dtx = utilService.get_datefilters()
		println "dtx get_datefilters is " + dtx
		println "uu.uconfig is " + uu.uconfig
		SimpleDateFormat formatter = new SimpleDateFormat('yyyy-MM-dd')
		String gr = uu.uconfig?.genrule ? uu.uconfig.genrule : "H>=7 AND H<23"
		Date d1 = uu.uconfig?.date_a ? formatter.parse(uu.uconfig.date_a): formatter.parse(dtx.today)
		Date d2 = uu.uconfig?.date_z ? formatter.parse(uu.uconfig.date_z): formatter.parse(dtx.quarter)
		String t1 = uu.uconfig?.date_a ? uu.uconfig.date_a: dtx.today
		String t2 = uu.uconfig?.date_z ? uu.uconfig.date_z: dtx.quarter
		Map rd = [
			when: gr,
			start: d1, // "2020-11-12" //# 2020-11-08
			last:  d2 //
		]
		Map rt = [
			when: gr,
			start: t1, // "2020-11-12" //# 2020-11-08
			last:  t2 //
		]
		
		
		
		//println "rd will be " + rd
		//println "rt will be " + rt
		return as_text ? rt : rd //= [ genrule:params.genrule, date_a:params.date_a, date_z:params.date_z ]
	}

	
	List get_streams_for_planner(String user_id) {
		User uu = User.findByCode(user_id)
		if(!uu) return null // 403
		
		List areas = uu.processOfUser.collect {
			[
			stream : it.properties.label,
			weight: it.properties.weight,
			when: it.properties.limit,
			sequence: it.taskOfProcessOfUser.collect { [ 
				project: it.properties.label,
				ETC: it.properties.etc,
				seqno: it.properties.seqno
				] }
			]
		}
		return areas
	}
	
	

}
