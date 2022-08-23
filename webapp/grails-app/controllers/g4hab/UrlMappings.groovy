package g4hab

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

		"/"(controller: "member", action: "index")	
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
