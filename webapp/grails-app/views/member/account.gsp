<!DOCTYPE HTML>
<html>

	<%-- SECTION 1of4 - HEAD AND COMMON STYLE --%>
	
    <head>
		${raw(html_head_webix)}
		<title>Balanced Planner</title>
		<style>${raw(html_head_style)}</style>
    </head>
	<body>
	
	<script type="text/javascript" charset="utf-8">
	var html_body_session_comment = "${html_body_session_comment}";
	
	var html_body_prop_rule = "${raw(html_body_prop_rule)}";
	var html_body_prop_start = "${raw(html_body_prop_start)}";
	var html_body_prop_end = "${raw(html_body_prop_end)}";
	var any_user_data = ${any_user_data};
	
	var gdb=false;
	
	var vm31_balances_exportform = {
		view: "form",
		minWidth:180, maxWidth:300,	
		elements: [
			//{	view: "label", label: "Export CSV", width: 100},
			{	view: "button", label: "Export YAML", click:function(){
						webix.send("/member/export", {},"GET");
						//webix.toCSV( $$("vm31a"), {filename: "balances", ignore: { "vm31fx":false }});
					}
			},
			{
				view: "uploader", id:"csvuploader", value: "Upload YAML",	multiple:false, name:"files",
				css:"webix_secondary",
				// https://docs.webix.com/desktop__styling.html
				//accept:"text/x-yaml", // "text/csv",
				//accept:"text/yaml;text/yml", // "text/csv",
				//accept:"application/yaml;application/yml", // "text/csv",
				link:"mylist",  upload:"/member/upload",
				on: {
					/*onViewShow: function() {
						var or1 = $$("vm21_dates").count() >0
						var or2 = $$("vm22_accounts").count() >0
						if(or1 || or2) {$$("csvuploader").disable();}
						if(gdb) webix.message("csvuploader onViewShow");
					}, */
					onFileUpload: function(file, response){
						if(gdb) webix.message("done");
						$$("csvuploader").disable();
						//$$("vm21_dates").load("/member/date");
						//$$("vm22_accounts").load("/member/account");
						//$$("vm31a").load("/member/balance");
						//refreshreports();
					},
					onFileUploadError:function(file, response){
						//console.log(response.totinvalid);
						//console.log(response.errortype);
						webix.message({
							text:"error",
							//text:"error at line "+response.firstline + " ("+response.errortype+")",
							type:"error"});
					}
				}
			},
			{
				view:"list",  id:"mylist", type:"uploader",
				autoheight:true, borderless:true	
			},
			{
				view:"button", id:"populatesample", value:"Populate example", click:function(id,e){
					webix.send("/member/upload/example", {},"GET");
				}
			},
			/*,
			{
				view: "button", label: "Get value", click: function() {
					var text = this.getParentView().getValues();
					text = JSON.stringify(text, "\n");
					webix.message("<pre>"+text+"</pre>");
				}
			}*/
			//{},{}
		]
	}
	
	
	
	
	
	
	
	
	var vm10_session_Y = { 	maxWidth:300,
				rows:[
				
				{ view:"form", // width:vm10_user_W, /*adjust:true, */ 
					elements:[
					
						//{ template:"current session", type:"section"},
						{ view:"accordion", multi:true, rows:[
							{ header:"reveal current session code", collapsed:true, 
							  body: { height:60, template: html_body_session_comment }
							}
							]
						},
						//{ template:"plan", type:"section"},
						
						//{ template:"reset session", type:"section"},
						{
							view:"button", value:"logout", click:function(id,e){
								webix.send("/member/logout", {},"POST");
							}
						},
						{
							view:"button", value:"EDIT PLAN", 
							css:"webix_primary",
							click:function(id,e){
								webix.send("/member/plan", {},"GET");
							}
						}
					]
				},
			
				vm31_balances_exportform,
				
				{	view:"property", id:"properties", autoheight:true, // 120, //width:300,
					elements:[
						{ label:"properties", type:"label"},
						{ label:"general rule", type:"text", id:"genrule", value:html_body_prop_rule},
							// https://docs.webix.com/datatable__formatting.html
						{ label:"Start date", type:"date", id:"date_a", format:webix.Date.dateToStr("%Y-%m-%d"), value:html_body_prop_start},
						{ label:"End date", type:"date", id:"date_z", format:webix.Date.dateToStr("%Y-%m-%d"), value:html_body_prop_end}
					],
					on: {
						onAfterEditStop: function(state, editor, ignoreUpdate){
								if(state.value != state.old){
									//webix.message("Cell value was changed");
									//webix.message(JSON.stringify($$("properties").getValues()));
									var promise2 = webix.ajax().post("/member/prop_U",$$("properties").getValues());
									promise2.then(function(){
										//if(params.id!="0") $$("vm21_areas").remove(params.id);
									});
									
									
									//webix.message("editor is "+JSON.stringify(editor));
									//editor is {"popupType":"date","$inline":true,"config":{"label":"End date","type":"date","id":"date_z","value":"
									
							}
						}
					}
				}
				
				//when: "H>=7 AND H<23",
				//start: dtstart, // "2020-11-12" //# 2020-11-08
				//last:  dtstop // "2021-12-31"		
				
			//{ view:"label", label: "<font color=darkgrey>"+session_code+"</font>"},	//,	labelWidth:300},
			//{ view:"label", label: session_stats_c},	//,	labelWidth:300},
			//{ view:"label", label: session_stats_a}	// labelWidth:300}
			]
	};
	var vm10_user_LW = 150;
	var vm10_session_N = { view:"form", /*autowidth:true, */
			maxWidth:300,
			// width:vm10_user_W,
			id:"vm10_switchuser", 
				elementsConfig:{labelPosition:"top"}, elements:[
			{ template:"switch to session", type:"section"},
			//{ label:"session code", type:"password", name:"account", required:true, labelWidth:vm10_user_LW},
			{ view:"text", type:"password", label:"session code", required:true, name:"account", labelWidth:vm10_user_LW},
			{ view:"button", minWidth:180, maxWidth:300, 
				hotkey: "enter+ctrl", css:"webix_primary", value:"login", click:function(id,e){
					webix.send("/member/login",
					$$("vm10_switchuser").getValues(),"POST");
				}
			},
			{ view:"button", minWidth:180, maxWidth:300, 
				hotkey: "enter+ctrl", value:"signup (auto)", click:function(id,e){
					webix.send("/member/signup", {},"POST");
				}
			}
			]
	};
	</script>
	
	<g:if test="${html_body_session_exists}">
	<script>
	webix.ui.fullScreen();
	var v0 = {rows:[{},{cols:[{},vm10_session_Y,{}]},{}]};
	webix.ui(v0);
	if(any_user_data) {
		$$("populatesample").disable();
		$$("csvuploader").disable();
	}
	</script>
	</g:if>
	<g:else>
	<script>
	webix.ui.fullScreen();
	var v0 = {rows:[{},{cols:[{},vm10_session_N,{}]},{}]};
	webix.ui(v0);
	</script>
	</g:else>
	
	</body>
</html>

