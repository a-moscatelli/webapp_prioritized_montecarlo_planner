package g4hab

import groovy.json.JsonOutput
import org.yaml.snakeyaml.Yaml

// http://gorm.grails.org/latest/mongodb/manual/

class MemberController {
	
	static Webix webix = new Webix()
	def persistService
	def utilService

	def index() {
		println "LOG_98f:index:session.code:"+session.code
		Map wsl = [:]
		List areas = []
		if(session.code) wsl = persistService.prop_R(session.code,true)
		if(session.code) areas = persistService.entity_L("area",session.code,[:],true)
		//println areas
		//println "wsl is " + wsl
		render(view: "account", encoding: "UTF-8", model: [
			html_head_webix: webix.html_head_webix,
			html_head_style: webix.html_head_style,
			html_body_session_exists: session.code != null,
			html_body_session_comment: "your session code is: " + session.code,
			html_body_prop_rule: wsl.when,
			html_body_prop_start: wsl.start,
			html_body_prop_end: wsl.last,
			any_user_data: areas.size() > 0
			])
	}
	
	
	def ucheck() {				//	FOR TEST
		persistService.admin_data_b()
		render(text: '{"rc":200,"text":"please check logs"}', contentType: 'text/json', encoding: "UTF-8", status:202)

	}
	
	def vcheck() {				//	FOR TEST
		String vm = "N/A"
		try { vm = System.getenv('MG_MONGO_ATLAS').split("@")[1].replaceAll("\"","\'")} catch(e) {}
		render(text: '{"rc":200,"text":"'+vm+'"}', contentType: 'text/json', encoding: "UTF-8", status:202)

	}
	
	
	def signup() {
		String session_code = utilService.session_c()
		persistService.user_c(session_code)
		session.code = session_code
		redirect(action: "index")		
	}
	
	
	def login() {	// intended to create new session with a given old session code
		int rc=204
		if(request.post && params.account && session.code == null) {
			String account = params.account.trim()
			if(account.size()==utilService.sessionLen()) {
				//session.invalidate() // new				
				log.info "LOG_HUL:session code login:" + account
				if(persistService.user_u(account)) {
					session.code = account
					rc=200					
				} else {
					log.error "LOG_NUL:no account found"
				}
				//log.info "LOG_SUL:session:" + ret
				/*if(ret) {
					session.session_dob = ret.session_dob
					session.session_last = ret.session_last
				}*/
			} else {
				log.error "LOG_EUL:session code login:" + account
			}
		}
		/*
		if(request.post && params.account) {
			session.invalidate() // new
			Map ret = graphService.user_u(params.account.trim())
			if(ret) {
				session.code = params.account.trim()
				log.info "session code sz:" + session.code.size()
				session.session_dob = ret.session_dob
				session.session_last = ret.session_last
			}
		} */
		redirect(action: "index", model:[rc:rc]) //, model: [session_code:session.code, session_dob: session.session_dob, session_last: session.session_last]) // params: [id: "b_n"]
	}
	
	def logout() {
		// src: http://docs.grails.org/3.1.1/ref/Servlet%20API/session.html
		//log.info "User agent: " + request.getHeader("User-Agent")
        //log.info "User agent: " + request.getHeader("User-Agent")
		session.invalidate()
        redirect(action: "index")
    }
	
	def plan() {
		
		if(session.code != null) {
			Map dtt = utilService.get_datefilters()
			render(view: "spa", encoding: "UTF-8", model: [
				html_head_webix: webix.html_head_webix,
				html_head_style: webix.html_head_style,
				html_body_today: dtt.today,
				html_body_tomorrow: dtt.tomorrow
			])
		} else {
			redirect(action: "index")
		}
	}
	
	
	def area_u() {
		if(request.post && session.code) {
			println " area_u -> " + persistService.entity_U("area",session.code, params, params.id=="0")
		}
		render(text: '{"rc":200}', contentType: 'text/json', encoding: "UTF-8", status:202)
	}
	
	def activity_u() {
		if(request.post && session.code) {
			println " activity_u -> " + persistService.entity_U("activity",session.code, params, params.id=="0")
		}
		render(text: '{"rc":200}', contentType: 'text/json', encoding: "UTF-8", status:202)
	}
	
	def area_d() {
		if(request.post && session.code) {
			println " area_d -> " + persistService.entity_D("area",session.code, params)
		}
		render(text: '{"rc":200}', contentType: 'text/json', encoding: "UTF-8", status:202)
	}
	
	def activity_d() {
		if(request.post && session.code) {
			println " area_d -> " + persistService.entity_D("activity",session.code, params)
		}
		render(text: '{"rc":200}', contentType: 'text/json', encoding: "UTF-8", status:202)
	}

	
	def area_L() {
		List areas = []
		boolean enrich_with_stats_from_plan = true
		if(session.code) areas = persistService.entity_L("area",session.code,[:],false)
		if(enrich_with_stats_from_plan) {
			Map stats = persistService.hour_area_stats(session.code)
			areas.each { // add columns
				it.put("ahourscount", stats[it.area] ? stats[it.area].ahourscount : "N/A")
				it.put("ahourspctcnt",stats[it.area] ? stats[it.area].ahourspctcnt : "N/A")
			}
		}
		render(text: JsonOutput.toJson(areas), contentType: 'text/json', encoding: "UTF-8")
	}
	
	def activity_L() {
		List activities = []
		boolean enrich_with_stats_from_plan = true
		if(params.id) session.area = params.id
		println "LOG_345:activity_L:" + session.area
		if(session.code) activities = persistService.entity_L("activity", session.code,[area_id:session.area],false)
		if(enrich_with_stats_from_plan) {
			Map acstats = persistService.hour_activity_stats(session.code)
			activities.each {	// add columns
				String thekey = it.activity+"@"+it.area
				it.put("completion",acstats[thekey] ? acstats[thekey].completion : "N/A")
			}
		}
		render(text: JsonOutput.toJson(activities), contentType: 'text/json', encoding: "UTF-8")
	}
	
	def prop_U() {
	
		//println "prop_U:" + params
		// prop_U:[genrule:H>=7 AND H<23, date_a:2020-12-18 00:00:00, date_z:, controller:member, format:null, action:prop_U]
		if(session.code) {
			persistService.prop_U(session.code,params)
		}
		render(text: '{"rc":200}', contentType: 'text/json', encoding: "UTF-8", status:202)
	}
	
	def export() {
		boolean yaml_not_json = true
		if(session.code) {
			List streams = persistService.get_streams_for_planner(session.code)			
			Map config = persistService.prop_R(session.code,true) // utilService.get_default_plan_config()	// TBC not consistent !!!
			Map output = [streams:streams, config:config]
			if(yaml_not_json) {
				Yaml yaml = new Yaml()
				StringWriter writer = new StringWriter()
				yaml.dump(output, writer)
				// https://stackoverflow.com/questions/332129/yaml-media-type
				// render(file: new File(absolutePath), fileName: "balanced_planner_exported.yaml")
				//render(text: writer.toString(), contentType: 'text/x-yaml', encoding: "UTF-8") // , fileName: "balanced_planner_exported.yaml")
				String rts = writer.toString()
				InputStream rtstream = new ByteArrayInputStream(rts.getBytes("UTF-8"))	// https://stackoverflow.com/questions/782178/how-do-i-convert-a-string-to-an-inputstream-in-java
				render(file:rtstream, contentType: 'text/x-yaml', fileName: "balanced_planner_exported.yaml") 	// https://blog.jdriven.com/2013/09/grails-goodness-render-binary-output-with-the-file-attribute/
				//render(file:rtstream, contentType: 'text/x-yaml', encoding: "UTF-8", fileName: "balanced_planner_exported.yaml")				
			} else {
				render(text: JsonOutput.toJson(output), contentType: 'text/json', encoding: "UTF-8")
			}
		}
	}


	def upload() {
		boolean using_the_builtin_example = params.id=="example"
		boolean yaml_not_json = true

		boolean user_is_authenticated = true
		boolean user_has_no_data = true
		if(!session.code) user_is_authenticated=false
		
		if(user_is_authenticated) {
			if(persistService.user_area_count(session.code) > 0) user_has_no_data=false
		}
		if(user_is_authenticated && user_has_no_data && yaml_not_json) {
			String contentyaml
			if(using_the_builtin_example) {
				contentyaml = utilService.get_sample_yaml() // sample_yaml
				//println "contentyaml size = " + contentyaml.size()
			} else {
				def fyaml = request.getFile('upload')
				InputStream isyaml = fyaml.getInputStream()
				contentyaml = isyaml.getText("UTF-8")
			}
			Yaml parser = new Yaml()
			Map ymlfile = parser.load(contentyaml)
			
			println "persist_file:" + persistService.persist_file(session.code, ymlfile)
		} else {
			log.error "LOG_3ry:user has got data already so upload is not performed"
		}
		println "user_is_authenticated: $user_is_authenticated user_has_no_data:$user_has_no_data"
		if(using_the_builtin_example) {
			redirect(action: "plan")
		} else {
			render(text: '{"rc":200}', contentType: 'text/json', encoding: "UTF-8", status:202)
		}
	}
	
	
	def hour_L() {
		boolean regenerate = params.id=="regen"
		List hours = []
		if(session.code) {
			if(regenerate) {
				List streams = persistService.get_streams_for_planner(session.code)
				//println "new Planner:"
				Map plan_config = persistService.prop_R(session.code,false) // utilService.get_default_plan_config()
				println "new Planning: stored plan_config is " + plan_config
				// if(! plan_config.genrule) plan_config = utilService.get_default_plan_config()
				Planner planner = new Planner(streams,plan_config)
				//println "new Planner done"
				hours = planner.get_timetable(true)
				persistService.entity_U("hours",session.code,[config:plan_config, hours:hours],true)
			}
			hours = persistService.get_most_recent_plan(session.code)
			//println "new Planner ret is " + hours.size()
		}
		render(text: JsonOutput.toJson(hours), contentType: 'text/json', encoding: "UTF-8")
	}
}
