package g4hab

class PublicController {

    def index() { 
		render(text: '{"rc":200,"text":"OK"}', contentType: 'text/json', encoding: "UTF-8") 
	}
}
