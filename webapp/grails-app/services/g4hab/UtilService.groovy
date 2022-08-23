package g4hab

import com.melogamy.MgSimpleUtils
import java.text.SimpleDateFormat
import grails.gorm.transactions.Transactional


@Transactional
class UtilService {
	
	static MgSimpleUtils simpleutils = new MgSimpleUtils()
	
    String session_c() {
		long da = simpleutils.epoch_ms()
		String ss = simpleutils.getRandomAlphanum(sessionLen()-2)	// 18 alphanums: 1.8E32 - 24 alphanums: 1.0E43
		//ss = ss.substring(0,6) + "_" + ss.substring(6,12) + "_" + ss.substring(12,18) // + "_" + ss.substring(18,24)
		long dz = simpleutils.epoch_ms()
		ss = "A" + ss + "Z"
		log.info "LOG_G7y:session_c(): $ss : took ${dz-da} ms"
		return ss
    }
	
	int sessionLen() {
		return 18+2
	}
	
	String get_sample_yaml() {
		// M:\DEV\GRAILS\grails-4.0.3\g4hab\grails-app\conf\g4hab
		// thanks to https://www.damirscorner.com/blog/posts/20160313-AccessingApplicationFilesFromCodeInGrails.html
		def resource = this.class.classLoader.getResource('g4hab/example1.yaml')
		def path = resource.file // absolute file path
		String rt = resource.openStream().getText("UTF-8") // input stream for the file
		return rt
	}

	Map get_datefilters() {
		Date dtstart = new Date()
		Calendar cc = Calendar.getInstance()
		Calendar cq = Calendar.getInstance()
		cc.setTime(dtstart)
		cq.setTime(dtstart)
		cc.add(Calendar.DATE, 1)
		cq.add(Calendar.MONTH, 3)
		Date dtstop = cc.getTime()
		Date dtq = cq.getTime()
		SimpleDateFormat formatter = new SimpleDateFormat('yyyy-MM-dd')
		Map config = [
			today: formatter.format(dtstart), // "2020-11-12" //# 2020-11-08
			tomorrow: formatter.format(dtstop), // "2021-12-31"
			quarter: formatter.format(dtq)
		]
		return config
	}
	
	Map get_default_plan_config() {
		int ndays = 30 * 6 // 6 months
		Calendar cc = Calendar.getInstance()
		Date dtstart = new Date()
		cc.setTime(dtstart)
		cc.add(Calendar.DATE, ndays)
		Date dtstop = cc.getTime()
		Map config = [
			when: "H>=7 AND H<23",
			start: dtstart, // "2020-11-12" //# 2020-11-08
			last:  dtstop // "2021-12-31"		
		]
		return config
	}
	
}
