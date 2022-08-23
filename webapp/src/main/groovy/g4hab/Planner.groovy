
package g4hab

import org.apache.commons.jexl2.* // JexlEngine v2 Expression JexlContext MapContext
import java.text.SimpleDateFormat

class Planner {
	
	Map ymlfile = [:]
	Map timings = [:]
	int debug = 0 // 0:just the output 1,2,3
	Calendar cal
	Date lastd
	JexlEngine jexl
	Map When_expr_cache = [:]
	Map When_expr_cachez = [:]
	Map ETC_expr_cache = [:]
	//final List DOW = ["XXX","Lun","Mar","Mer","Gio","Ven","SAB","DOM"]	//IT
	final List DOW = ["XXX","Mon","Tue","Wed","Thu","Fri","SAT","SUN"]		// EN

	
	
	private void enrich_yaml_with_hidden_variables() {
		ymlfile.streams.each { ss ->
			if(ss.containsKey("sequence")) {
				ss.sequence.each { sp ->
					if(sp.containsKey("ETC")) {
						sp.ETC_initial = eval_ETC_cached(sp.ETC as String) as Integer
						sp.ETC_running = eval_ETC_cached(sp.ETC as String) as Integer
					}
				}
			}
		}		
	}
	
	
	private void print_incomplete_projects() {
		println "==================="
		println "incomplete_projects"
		println "==================="
		ymlfile.streams.each { ss ->
			if(ss.containsKey("sequence")) {
				ss.sequence.each { sp ->
					if(sp.containsKey("ETC")) {
						if(sp.ETC_running>0) {
							println sp
						}
					}
				}
			}
		}		
		println "==================="
	}
	
	private boolean isAllowedInPrinciple(int D, int H) {
		boolean rt = isAllowed(D,H,ymlfile.config.when)
		if(debug>2) println "D $D\tH\t$H\t" + "isAllowedInPrinciple=$rt"
		return rt
	}

	private boolean isAllowed(int D, int H, String logic) {
		long ta = new Date().getTime()
		boolean ret = eval_WHEN_cached(D,H,logic)
		long tz = new Date().getTime()
		take_timing("isAllowed",ta,tz)
		return ret 
	}
	

	int eval_ETC_cached(String jexlExpr) {
		if(!jexlExpr) return 1
		// https://commons.apache.org/proper/commons-jexl/javadocs/apidocs-2.1.1/index.html
		jexlExpr = jexlExpr.toLowerCase()
		if(! ETC_expr_cache.containsKey(jexlExpr)) {
			Expression je = jexl.createExpression( jexlExpr ) // JexlEngine ( "AND" not accepted, "and" OK )
			JexlContext jc = new MapContext() // JexlEngine
			def jr = je.evaluate(jc)
			ETC_expr_cache[jexlExpr] = Math.round(jr) as Integer // JexlEngine
		}
		return ETC_expr_cache[jexlExpr]
	}
	
	boolean eval_WHEN_cached(int D, int H, String jexlExpr) {
		jexlExpr = jexlExpr.toLowerCase()
		String keyDH = "$D/$H/$jexlExpr"
		String key = jexlExpr
		
		if(! When_expr_cachez.containsKey(key)) {
			long ta = new Date().getTime()
			When_expr_cachez[key] = jexl.createExpression( jexlExpr ) as Expression
			long tz = new Date().getTime()
			take_timing("eval_WHEN_cached/createExpression",ta,tz)
		}
		
		if(! When_expr_cache.containsKey(keyDH)) {
			Expression je = When_expr_cachez[key] // jexl.createExpression( jexlExpr ) // JexlEngine ( "AND" not accepted, "and" OK )
			JexlContext jc = new MapContext([d:D,h:H]) // JexlEngine
			When_expr_cache[keyDH] = je.evaluate(jc) as Boolean
		}
		return When_expr_cache[keyDH]
	}
	
	List getFirstAllowedProjectPerStream(int D, int H) {	//bugs
		long ta = new Date().getTime()
		List pp_allowed_for_DH = []
		if( isAllowedInPrinciple(D,H)) {
			ymlfile.streams.eachWithIndex { ss, si ->
			//for(int si=0; si < ymlfile.streams.size(); si++) {	Map ss = ymlfile.streams[si]
				boolean one_incompleted_project_for_the_stream_is_found = false
				//for(int pi=0; pi < ss.sequence.size(); pi++) {	Map pp = ss.sequence[pi]
				boolean seq_case1_noseq = ! ss.containsKey("sequence")
				boolean seq_case2_emptyseq = ss.containsKey("sequence") && ss.sequence.size()==0
				// prima era solo seq_case1 
				
				//
				boolean stream_allowed = isAllowed(D,H,ss.when)
				if(stream_allowed) {
					
					List eligible_pp = ss.sequence.findAll { pp ->
							// pp may be an empty project
							pp.containsKey("ETC_running") && pp.ETC_running > 0
					}
					Map project 
					if(eligible_pp.size()==0) {
						ss.put("sequence",[[project:ss.stream]])	// maybe overwriting
						project = 	[
										si:si,
										pi:0,	// was pi
										area_weight:ss.weight,
										area_name:ss.stream,
										task_name:ss.stream												
										]
					} else {
						//eligible_pp.each { println it }
						Map first_pp = eligible_pp.min { it.task_seqno }
						project = 	[
										si:si,
										pi:first_pp.task_seqno,
										area_weight:ss.weight,
										area_name:ss.stream,
										task_name:first_pp.project											
									]
					}
					pp_allowed_for_DH.add(project)
				}
				
				
			}
		}
		long tz = new Date().getTime()
		take_timing("getFirstAllowedProjectPerStream",ta,tz)
		return pp_allowed_for_DH
	}
	
	void take_timing(String key, long ta, long tz) {
		if(! timings.containsKey(key)) {
				timings[key] = 0
				timings[key+"_count"] = 0
		}
		timings[key] += (tz - ta)
		timings[key+"_count"] ++
	}
	
	void print_time_stats() {
		//if(debug==0) return
		println "==================="
		println "timings cumul [ms]"
		timings.eachWithIndex { key, value, i ->
			println "$key\t$value"
		}
		println "==================="
	}
	
	Map getTheBestProject(List pp_allowed_for_DH) {
		// the allowed projects are sorted by decreasing weight: [.9 .9 .5 .3 .1];
		// totalwg is computed = 2.7;
		// random(0 to 2.7) = 2.0;
		// -> the 3rd is chosen.
		long ta = new Date().getTime()
		Map ret = null
		List pp_allowed_for_DH_OrderByDecrWg = pp_allowed_for_DH.sort { it.area_weight }.reverse()
		if(pp_allowed_for_DH_OrderByDecrWg.size()>0) {
			double totwg = pp_allowed_for_DH_OrderByDecrWg.collect {it.area_weight }.sum()
			double randomcut = Math.random() * totwg
			//if(debug>2) println "D $D\tH\t$H\t" + pp_allowed_for_DH_OrderByDecrWg + " total weight: " + totwg
			boolean allocated = false
			double cumul = 0.0
			// [ 0.95 0.45 0.15 0.05 ] wgs
			// [ 0.95 1.40 1.55 1.60 ] wgs cumul
			// sum = 1.60
			// if randomcut = 0.90 pick the first, if 1.20 pick the second
			
			pp_allowed_for_DH_OrderByDecrWg.each {
				cumul += it.area_weight
				if(allocated==false && randomcut <= cumul) {
					allocated=true
					ret = it
				}
			}
			//if(debug>2) println "D $D\tH\t$H\t" +"best:\t" + ret + "\t(randomcut:$randomcut)"
		}
		long tz = new Date().getTime()
		take_timing("getTheBestProject",ta,tz)
		return ret
	}
	
	void updateETC(Map project) {
		// println "updateETC for project " + project
		long ta = new Date().getTime()
		if(project) {
			// pi may be 0 or task_seqno
			Map ptr = ymlfile.streams[project.si].sequence.find { it.task_seqno == project.pi} // [project.pi]
			if(ptr && ptr.containsKey("ETC")) {
					ptr.ETC_running--
			}
		}
		long tz = new Date().getTime()
		take_timing("updateETC",ta,tz)
	}
	
	Map getDAY_OF_WEEK() {
		int ret
		int sun1 = cal.get(Calendar.DAY_OF_WEEK) // 1 (Sunday) to 7 (Saturday)
		assert Calendar.SUNDAY==1
		assert Calendar.SATURDAY==7
		if(sun1==Calendar.SUNDAY) ret=7 else ret=sun1-1
		assert ret>=1 || ret<=7
		
		return [D:ret, DDD:DOW[ret]]
	}
	
	
	Date getdate() {
		return cal.getTime()
	}
	
	String getdatestr() {
		SimpleDateFormat formatter = new SimpleDateFormat('yyyy-MM-dd')
		return formatter.format(cal.getTime())
	}
	String getdatestrhh(int hh) {
		assert hh>=0 && hh<24
		SimpleDateFormat formatter = new SimpleDateFormat('yyyyMMdd')
		String rtd = formatter.format(cal.getTime())
		String rth = hh<10 ? "0"+hh : ""+hh
		return rtd + rth
	}
	
	boolean rolldate() {
		cal.add(Calendar.DATE, 1)
		boolean stop = cal.getTime() == lastd
		return !stop
	}
	
	/*
	String getALLOWEDMASK(String logic_fDH) {
		String mask = ""
		byte[] bmask = new byte[7*24]
		for(int D=1;D<=7;D++) {
			for(int H=0;H<24;H++) {
				boolean allowed = eval_WHEN_cached(D,H,logic)
				bmask = allowed ? 1 : 0
				mask = mask + (allowed ? "Y" : "N")
				//} catch(e) {
					//if(debug>0) println "ERROR WITH LOGIC: $logic : $e" 
					// e may be groovy.lang.MissingPropertyException
				//}
			}
		}
		assert mask.size() == 7*24
		return mask
	}*/
	
	/*boolean isAllowedInMASK(String mask, int D, int H) {
		assert mask.size() == 7*24
		return mask.getAt((D-1)*24+H) == "Y" // 7 * 24 = 168
	}*/
	
	Map getStreamProject(Map pp_pick_for_DH) {
		try {
			Map streamz = ymlfile.streams.find { it.stream == pp_pick_for_DH.area_name}
			Map projectz = streamz.sequence.find { it.project == pp_pick_for_DH.task_name}
			return projectz
		} catch(e) {}
		return null
	}
	
	
	Planner(List streams, Map config) {
		//Yaml parser = new Yaml()
		//ymlfile = parser.load((yml_filename as File).text)
		

		
		ymlfile.config = config //[
		jexl = new JexlEngine()
		cal = Calendar.getInstance()
		Date dtcur = ymlfile.config.start
		lastd = ymlfile.config.last
		cal.setTime(dtcur)
		
		ymlfile.streams = streams
		
		enrich_yaml_with_hidden_variables()
	}
	
	
	List get_timetable(boolean include_SK) {
		//println ymlfile
		//Database db = new Database(args[1])
		boolean completion = false
		boolean going=true
		List body = []
		for(int dd=0;going;dd++) {
			Map dw = getDAY_OF_WEEK()
			int D = dw.D
			String DDD = dw.DDD
			String dtcur = getdatestr()	// TBC
			for(int H in 0..23) {
				if(completion) break
				List pp_allowed_for_DH = getFirstAllowedProjectPerStream(D,H)
				//println "pp_allowed_for_DH $D $H :"
				//pp_allowed_for_DH.each { println it }
				Map pp_pick_for_DH = getTheBestProject(pp_allowed_for_DH)
				//println "chosen pp_allowed_for_DH $D $H : " + pp_pick_for_DH
				if(pp_pick_for_DH) {
					int pctz = -1	// dummy value - used just to assure the number of line fields is maintained
					try {
						Map projectz = getStreamProject(pp_pick_for_DH)
						if(projectz.containsKey("ETC")) {
							// ETC_running : from ETC_initial downto 0
							boolean show_PCTDONE_at_the_end_of_the_hour = true // not at the beginning. displayed values will go from 5% to 100%
							int projectz_ETC_running
							if(show_PCTDONE_at_the_end_of_the_hour) {
								projectz_ETC_running = projectz.ETC_running -1
							} else {
								projectz_ETC_running = projectz.ETC_running
							}
							pctz = (int) Math.round( 100 * (projectz.ETC_initial - projectz_ETC_running) / projectz.ETC_initial )
						}
					} catch(e) {
						println "EEEE2 "+e+ " "+pp_pick_for_DH
					}
					String done_disp = pctz<0 ? "..." : "$pctz%"
					if(include_SK) {
						//String SK = dtcur+"-"+H
						String SK = getdatestrhh(H)
						body.add([ id:SK, DT:dtcur, DOW:DDD, H24:H, area:pp_pick_for_DH.area_name, activity:pp_pick_for_DH.task_name, progress:done_disp])					
					} else {
						body.add([        DT:dtcur, DOW:DDD, H24:H, area:pp_pick_for_DH.area_name, activity:pp_pick_for_DH.task_name, progress:done_disp])					
					}
				}
				updateETC(pp_pick_for_DH)
			}
			going = rolldate()
		}
		return body
		//db.writeToNewFile(args[2],["DT","DOW","H24","stream","activity","done"],body,"\t",6,"SK")
		
		//db.print_time_stats()
		//db.print_incomplete_projects()

	}
	
}