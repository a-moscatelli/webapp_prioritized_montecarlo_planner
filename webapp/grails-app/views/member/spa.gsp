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
	if(true) webix.ui.fullScreen();
	
	var html_body_today = "${html_body_today}";
	var html_body_tomorrow = "${html_body_tomorrow}";
	
	
	
	// from : https://docs.webix.com/samples/20_multiview/02_navigation.html
	
	
	
	
	

	
	//function dodel(id,n) {}
	var kdel = "&#10008;";
	var kdots = "&#10159;"; // "&hellip;"		//	&rarrhk;	&#10555;  &#10548;  &#10138; &#10150;  &#10159;		// https://www.toptal.com/designers/htmlarrows/arrows/
	var kinfo = "&#9658;"; // "<b>&#8811;</b>"; // "&raquo;"; // "&#10067"; // "&nearr;"; // "&#10067";	// 10068 //	"&rarrhk;";	// "&#10548;"; // 	// "&#10150;";	// &#10162;	&rarrhk;
	var g_editaction = "click";
	
	
	var gdb = false;
	//var kdel = "&#10008;";

	function delete_upon_confirmation(params) {
		// title,text,form_id,id
		var promise1 = webix.confirm({	title:params.q, ok:"Yes", cancel:"No",	text: params.label});
		// HEY! if user press ESC, the Cancel is the way.
		//promise.then(function(){});
		// $$(form_id).getValues()
		if(params.uc==1) promise1.then(function(){
				var promise2 = webix.ajax().post("/member/area_d",{"id":params.id});
				promise2.then(function(){
					if(params.id!="0") $$("vm21_areas").remove(params.id);
				});
		});
		if(params.uc==2) promise1.then(function(){
				var promise2 = webix.ajax().post("/member/activity_d",{"id":params.id});
				promise2.then(function(){
					if(params.id!="0") $$("vm22_activs").remove(params.id);
				});
		});
			
	}


	function dodel(id,uc) {
		//webix.message("deleting"+id+" - "+uc);
		if(uc==1) delete_upon_confirmation({"q":"delete this area?","label":$$("vm21_areas").getItem(id).area,"uc":uc,"id":id});
		if(uc==2) delete_upon_confirmation({"q":"delete this activity?","label":$$("vm22_activs").getItem(id).activity,"uc":uc,"id":id});
		//if(uc==2) delete_upon_confirmation("delete this account?",$$("vm22_activs").getItem(id).vm221nm,"vm22_add_act",id);
	}
	
	var v21_areas_table = {
		view:"datatable", select: "cell", 
		//scroll:"xy", 
		minWidth:500, // minWidth:400, 
		//minWidth:236, maxWidth:300,
		id:"vm21_areas",
		url: "/member/area_L",
		editable:true, editaction:g_editaction,	/*wrt_editability*/
		columns: [	// adjust: data,header,true
			{ id: "area",  width:100, header: "Area", adjust:true, editor:"text", sort:"string"}, // css:"a_center", template:"<div onClick='dodel(#id#,1)'>"+kdel+"</div>" }, // delete #id#</div>" }
			{ id: "alimit",  width:100, header: "Rule", adjust:true, editor:"text" }, // css:"g_editable_sleep"
			{ id: "aweight", width:100, header: "Weight", sort:"string", editor:"text" }, // , editor:"date", map:"(date)#vm211dt#" },
			//{ id: "acomment",  fillspace:true, header: "Comment", editor:"text", template:"#id#" }, // css:"g_editable_sleep"
			//{ id:"", template:"<input class='delbtn' type='button' value='Delete'>",	css:"padding_less", width:100 },
			
			//{ id: "aactivities",  width:100, header: "Activities" }, // css:"g_editable_sleep"
			
			
			//{ id:"", template:"<input class='addbtn' type='button' onclick=showact('#id#') value='Activities &hellip;'>",		css:"padding_less", width:100 },
			{ id: "id",  width:100, header: "Activities", adjust:"header", css:"a_center", template:"<div onClick=showact('#id#')>#aactivities# "+kinfo+"</div>" }, // delete #id#</div>" }
			{ id: "ahourscount",adjust:true, header: "Hours", css:"a_right", sort:"int" },
			{ id: "ahourspctcnt",adjust:true, header: "", sort:"int", css:"a_right", template:"#ahourspctcnt#%" },
			{ id: "acomment",  width:400, /*fillspace:true,*/ header: "Comment", editor:"text" /*, template:"id #id#"*/ }, // css:"g_editable_sleep"
			{ id: "id",  width:100, header: "Delete", adjust:"header", css:"a_center", template:"<div onClick=dodel('#id#',1)>"+kdel+"</div>" } // delete #id#</div>" }
			//onContext:{}
			// fillspace:true,
			//{ id: "vm211FX",header: "FX data status", adjust: "header" },
		],
		on: {
			onDataUpdate: function(id, data, old){
					// qui si arriva solo se c'e' stato un vero update. non serve verificare.
					if(gdb) console.log("EEE onDataUpdate Cell value was changed -- "+
					"data is "+JSON.stringify(data)+
					"--"+
					"id is "+JSON.stringify(id)+
					"--"+
					"old is "+JSON.stringify(old)
					);

					// eg EEE onDataUpdate Cell value was changed -- 
					// data is {"id":"5fd0ced40f9aab7860c7a0de","area":"e","aweight":".00","alimit":"D < 1"}--
					// id is "5fd0ced40f9aab7860c7a0de"--
					// old is {"id":"5fd0ced40f9aab7860c7a0de","area":"","aweight":".00","alimit":"D < 1"}
					
					var promise1 = webix.ajax().post("/member/area_u",data);
					promise1.then(function(data){ $$("vm21_areas").clearAll(); $$("vm21_areas").load($$("vm21_areas").config.url); });
					promise1.fail(function(err){ webix.message({ text:error_msg, type:"error"});});
					promise1.finally(function() { });
				}
		},
		
		//on: {			}
		/*
		on:{
					"onItemClick":function(id, e, trg){
						//id.column - column id
						//id.row - row id
						webix.message("Click on row: " + id.row+", column: " + id.column);
					}
				},
		*/
		
		ready(){
				//webix.ui(context_menu_rt).attachTo(this);
			}
	};
	// view-source:https://docs.webix.com/samples/15_datatable/09_columns/05_index_column.html
	var v22_activities_table = {
		view:"datatable", select: "cell", 
		//scroll:"xy", 
		minWidth:500, // minWidth:400, 
		//minWidth:236, maxWidth:300,
		id:"vm22_activs",
		url: "/member/activity_L",
		editable:true, editaction:g_editaction,	/*wrt_editability*/
		columns: [	// adjust: data,header,true
			//{ id: "index", header:"", width:50 },
			{ id: "seqno", header:"seqno", width:50, adjust:true, editor:"text", sort:"int" },
			{ id: "activity",  width:100, header: "Activity", adjust:true, editor:"text", sort:"string"}, // css:"a_center", template:"<div onClick='dodel(#id#,1)'>"+kdel+"</div>" }, // delete #id#</div>" }
			{ id: "etc",  width:100, header: "ETC", adjust:true, editor:"text" }, // css:"g_editable_sleep"
			//{ id: "areaid",  width:200, header: "AreaID", adjust:true, editor:"text" }, // css:"g_editable_sleep"
			{ id: "completion",adjust:"header", header: "Completion", sort:"string" },
			{ id: "acomment",  width:400, /*fillspace:true,*/ header: "Comment", editor:"text"}, // , template:"id #id#" } // css:"g_editable_sleep"
			{ id: "id",  width:100, header: "Delete", adjust:"header", css:"a_center", template:"<div onClick=dodel('#id#',2)>"+kdel+"</div>" } // delete #id#</div>" }
			
			//onContext:{}
			// fillspace:true,
			//{ id: "vm211FX",header: "FX data status", adjust: "header" },
		],
		/*
		on:{
					"data->onStoreUpdated":function(){
						this.data.each(function(obj, i){
							obj.index = i+1;
						})
					}
				},
		*/		
		on: {
			onDataUpdate: function(id, data, old){
					// qui si arriva solo se c'e' stato un vero update. non serve verificare.
					if(gdb) console.log("EEE onDataUpdate Cell value was changed -- "+
					"data is "+JSON.stringify(data)+
					"--"+
					"id is "+JSON.stringify(id)+
					"--"+
					"old is "+JSON.stringify(old)
					);

					// eg EEE onDataUpdate Cell value was changed -- 
					// data is {"id":"5fd0ced40f9aab7860c7a0de","area":"e","aweight":".00","alimit":"D < 1"}--
					// id is "5fd0ced40f9aab7860c7a0de"--
					// old is {"id":"5fd0ced40f9aab7860c7a0de","area":"","aweight":".00","alimit":"D < 1"}
					
					var promise1 = webix.ajax().post("/member/activity_u",data);
					promise1.then(function(data){ 
									$$("vm22_activs").clearAll(); $$("vm22_activs").load($$("vm22_activs").config.url);
									//webix.message("activity_L");
					});
					promise1.fail(function(err){ webix.message({ text:error_msg, type:"error"});});
					promise1.finally(function() { });
				}
		},		
		//drag:true,
		ready(){
				//webix.ui(context_menu_rt).attachTo(this);
			}
	};
	
	
	var vm33_hours_table_in = {
		view:"datatable", select: "cell", 
		//scroll:"xy", 
		minWidth:500, // minWidth:400, 
		//minWidth:236, maxWidth:300,
		id:"vm33_hours",
		url: "/member/hour_L",
		
//		SK	DT	DOW	H24	stream	activity	done
//		0	12/11/2020	Gio	7	carriera /architetto IASA	solution stampa1 passata1	0%

		// adjust: data,header,true
		editable:false, // editaction:g_editaction,	/*wrt_editability*/
		columns: [	// adjust: data,header,true
			{ id:"id",	
				header:[ "SN", { content:"textFilter" } ], // header:"SEQ", 
				adjust:true, sort:"string"},
			{ id:"DT", 
				// header:"Date",
				header:[ "Date", { content:"textFilter" } ],
				//header:[ "Date",{ content:"dateFilter"}], format:webix.i18n.dateFormatStr
				// https://docs.webix.com/datatable__filtering.html#date
				adjust:true
			},
			{ id:"DOW", 
				header:[ "DayOfWeek", { content:"textFilter" } ], // header:"DayOfWeek", 
				adjust:true },
			{ id:"H24", 
				header:[ "Hour", { content:"textFilter" } ], // header:"Hour", 
				adjust:true },
			{ id: "activity",
				header:[ "Activity", { content:"textFilter" } ],
				//header: "Activity", 
				adjust:true, sort:"string"}, // css:"a_center", template:"<div onClick='dodel(#id#,1)'>"+kdel+"</div>" }, // delete #id#</div>" }
			{ id: "area",  adjust:true, 
				//header: "Area"
				header:[ "Area", { content:"textFilter" } ],
			}, // css:"a_center", template:"<div onClick='dodel(#id#,1)'>"+kdel+"</div>" }, // delete #id#</div>" }
			{ id: "progress",  adjust:true, 
				header:[ "Done%", { content:"textFilter" } ], // header: "Done" 
			} // css:"g_editable_sleep"
			//{ id: "acomment",  fillspace:true, header: "Comment", editor:"text", template:"#id#" }, // css:"g_editable_sleep"
			
			//onContext:{}
			// fillspace:true,
			//{ id: "vm211FX",header: "FX data status", adjust: "header" },
		],
		ready(){
				//webix.ui(context_menu_rt).attachTo(this);
			}
	};
	
	var vm33_hours_table = { rows: [
		vm33_hours_table_in,
		{ cols:[
			{	view: "button", label: "Export to Excel", click:function(){
						webix.toExcel( $$("vm33_hours"), {filename: "plan" }); //, ignore: { "vm31fx":false }});
					}
			},
			{}
		]}
	]
	};
	

	function showact(areaid) {
		if(db) webix.message("showact:"+areaid);
		$$("vm22_activs").clearAll();
		$$("vm22_activs").load($$("vm22_activs").config.url+"/"+areaid);
		$$("idac").show();
	}
	
	
	function regenhours(){
		$$("idhh").show(); // usu. redundant.
		$$("vm33_hours").clearAll();
		$$("vm33_hours").load($$("vm33_hours").config.url+"/regen").then(function(data){
			$$("vm21_areas").clearAll();  $$("vm21_areas").load($$("vm21_areas").config.url);
			$$("vm22_activs").clearAll(); $$("vm22_activs").load($$("vm22_activs").config.url);
		});
	}
			
	function vshow(uc) {
		if(uc==10) webix.send("/member/index", {},"GET");
		if(uc==11) $$("idar").show();
		if(uc==12) $$("idac").show();
		if(uc==13) $$("idhh").show();
		if(uc==14) {
			// generate
			var dtcount = $$("vm33_hours").count();
			if(dtcount==0) {
				regenhours();
			} else {
				webix.confirm("re-generate the plan?").then(function(result){regenhours()}).fail(function(){});
			}
				//webix.message("Cancel");
			//});
		}
	}
	
	var timesheet_opts = ["Full", "Today", "Tomorrow"];
	var width1 = 94;
	
	var vb1 = { cols:[
                        { view:"button", value:"Account",  click:"vshow(10)", width:width1 },
						{ view:"button", value:"Timetable", click:"vshow(13)", width:width1, align:"center" },
						{}
							//,
                        //{ view:"button", value:"Save",    click:save12,   css:"webix_primary" },
                        //{ view:"button", value:"Delete",  click:remove12, css:"webix_primary" }					
    ]};
	var vb2 = { cols:[
						{ view:"button", value:"Areas", click:"vshow(11)", width:width1, align:"center" },
						{ view:"button", value:"Timetable", click:"vshow(13)", width:width1, align:"center" },
						{}
						//{ view:"button", value:"Back",  	click:cancel12 },
                        //{ view:"button", value:"Save",    click:save12,   css:"webix_primary" },
                        //{ view:"button", value:"Delete",  click:remove12, css:"webix_primary" }					
    ]};
	var vb3 = { cols:[
						{ view:"button", value:"Generate", click:"vshow(14)", width:width1, align:"center", id:"regen_hours" },
						{ view:"button", value:"Areas", 	 click:"vshow(11)", width:width1, align:"center" },
						//{ view:"button", value:"Activities", click:"vshow(12)", width:100, align:"center" },
                        { view:"button", value:"Account",  width:width1, click:"vshow(10)" },
						{ view:"select", options:timesheet_opts, width:width1,
							on:{
								onChange(newVal, oldVal){
									//webix.message("ts:"+newVal);
									//var xxx = newVal==timesheet_opts[0];
									//webix.message("ts:"+timesheet_opts.indexOf(newVal));
									// ...your handler
									if(newVal=="Full") $$("vm33_hours").filter('#DT#',null);
									if(newVal=="Today") $$("vm33_hours").filter('#DT#',html_body_today);
									if(newVal=="Tomorrow") $$("vm33_hours").filter('#DT#',html_body_tomorrow);
								}
							}
						},
						{}
						/*{ view:"button", value:"Cancel",  click:cancel12 },
                        { view:"button", value:"Save",    click:save12,   css:"webix_primary" },
                        { view:"button", value:"Delete",  click:remove12, css:"webix_primary" }		*/			
    ]};
	
	//var filler = {};
	function wrap_vw_form(view_id, row1_title, row1_toolbar, row2_view ) {
		// return a view having id = idx , row1 = hdt label + toolbar, row2 = 
		var row1_view = { view: "template", type: "header", template: row1_title, autoheight:true, width:width1};
		return { id:view_id, rows:[ 
								{ cols: [ row1_view, row1_toolbar, {} ]}, 
								row2_view
								]};
	}
/*
		var hdvw2 = {view:"toolbar",cols:[
		//hdvw2,
        { view:"button", id:view_id+"LoadBut", value:hdt, width:100, align:"left" },
        { view:"button", value:"Activities", click:"vshow(12)", width:100, align:"center" },
        { view:"button", value:"Back", click:"vshow(13)", width:100, align:"center" },
        { view:"button", value:"Info", width:100, align:"right" },
		{}
		]
		};
*/
		//return { id:idx, rows:[ hdvw, vw, fm ] };
	
	var vv1 = {
        //container: "area_spa",
        //padding:8,
        id:"views1",
		fitBiggest:true,
		//type:"space",
        cells:[
			wrap_vw_form("idar","Areas",				vb1, v21_areas_table),
			wrap_vw_form("idac","Activities of Area",	vb2, v22_activities_table),
			wrap_vw_form("idhh","Timetable",			vb3, vm33_hours_table)
            //{id:"aboutView", template:"<i>Select an item in List to edit it in Form</i>",padding:5}
        ]


    };
	webix.ui(vv1);
	
 	var db=0;
    function save12(){
		if(db)webix.message("save12");
        $$("formView1").save();
        $$("views1").back();
    }
    function cancel12(){	// cancel editing
		if(db)webix.message("cancel12");
        $$("views1").back();
    }
	function remove12(){
		if(db)webix.message("remove12");
        $$("listView1").remove($$("listView1").getSelectedId());
		$$("views1").back();
    }
	
	 function save13(){
		if(db)webix.message("save13");
        $$("formView1").save();
        $$("views1").back();
    }
    function cancel13(){	// cancel editing
		if(db)webix.message("cancel13");
        $$("views1").back();
    }
	function remove13(){
		if(db)webix.message("remove13");
        $$("listView2").remove($$("listView2").getSelectedId());
		$$("views1").back();
    }	 


	webix.ui({
        view:"contextmenu",
	    id:"cmenu1",
        data:["Add","Edit","Delete",{ $template:"Separator" },"Activities"],
        on:{
            onItemClick:function(id){
                var context = this.getContext();
                var list = context.obj;
                var listId = context.id;
                if(db) webix.message("List item: <i>"+listId+"</i> <br/>Context menu item: <i>"+this.getItem(id).value+"</i>");
				if(this.getItem(id).value == "Activities") {
					//$$("idac").show();
					showact(listId);
				}
            }
        }
    });
	
	
	
	webix.ui({
        view:"contextmenu",
	    id:"cmenu2",
        data:["Add","Edit","Delete",{ $template:"Separator" },"Hours",{ $template:"Separator" },"Back"],
        on:{
            onItemClick:function(id){
                var context = this.getContext();
                var list = context.obj;
                var listId = context.id;
                if(db) webix.message("List item: <i>"+listId+"</i> <br/>Context menu item: <i>"+this.getItem(id).value+"</i>");
				if(this.getItem(id).value == "Back") {
					$$("idar").show();
				}
				if(this.getItem(id).value == "Hours") {
					$$("idhh").show();
				}
				
            }
        }
    });
	webix.ui({
        view:"contextmenu",
	    id:"cmenu3",
        data:["Add","Edit","Delete",{ $template:"Separator" },"Back"],
        on:{
            onItemClick:function(id){
                var context = this.getContext();
                var list = context.obj;
                var listId = context.id;
                if(db) webix.message("List item: <i>"+listId+"</i> <br/>Context menu item: <i>"+this.getItem(id).value+"</i>");
				if(this.getItem(id).value == "Back") {
					$$("idac").show();
				}
				
            }
        }
    });

    $$("cmenu1").attachTo($$("vm21_areas"));
    $$("cmenu2").attachTo($$("vm22_activs"));
    // $$("cmenu3").attachTo($$("vm33_hours"));
	
	
	</script>
	
	
	<%-- SECTION 2of4 - COMMON JS --%>
		
		<!-- COMMON JS DYNAMIC 
		<script type="text/javascript" charset="utf-8">
		var session_code="${session_code}";
		var session_stats_c="(created just now)";
		var session_stats_a="";
		var isdev = ${isdev};
		
		<g:if test="${session_dob && session_last}">
		var session_dob=${session_dob};
		var session_last=${session_last};
		var session_stats_c = "created on:<br>" + new Date(session_dob).toString().substr(0,21);  // Wed Oct 14 2020 12:00:02
		var session_stats_a = "last login on:<br>" + new Date(session_last).toString().substr(0,21);
		</g:if>
		</script>
		-->
		<!-- COMMON JS STATIC -->
		
		<!--asset:javascript src="mcaa.js" alt=""/-->
		
	</body>
</html>

