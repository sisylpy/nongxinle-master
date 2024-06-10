$(function () {
    $("#jqGrid").jqGrid({
        url: '../sys/generator/list',
        datatype: "json",
        colModel: [			
			{ label: '表名', name: 'tableName', width: 100, key: true },
			{ label: 'Engine', name: 'engine', width: 70},
			{ label: '表备注', name: 'tableComment', width: 100 },
			{ label: '创建时间', name: 'createTime', width: 100 }
        ],
		viewrecords: true,
        height: 400,
        rowNum: 10,
		rowList : [10,30,50,100,200],
        rownumbers: true, 
        rownumWidth: 25, 
        autowidth:true,
        multiselect: true,
        pager: "#jqGridPager",
        jsonReader : {
            root: "page.list",
            page: "page.currPage",
            total: "page.totalPage",
            records: "page.totalCount"
        },
        prmNames : {
            page:"page", 
            rows:"limit", 
            order: "order"
        },
        gridComplete:function(){
        	//隐藏grid底部滚动条
        	$("#jqGrid").closest(".ui-jqgrid-bdiv").css({ "overflow-x" : "hidden" }); 
        }
    });
});

var vm = new Vue({
	el:'#rrapp',
	data:{
		tableName: null
	},
	methods: {
		query: function () {
			$("#jqGrid").jqGrid('setGridParam',{
                postData:{'tableName': vm.tableName},
                page:1
            }).trigger("reloadGrid");
		},
		generator: function() {
		    console.log("===000000-----------")
			var tableNames = getSelectedRows();
            console.log(tableNames);
            console.log("menuIds.....")

			if(tableNames == null){
				return ;
			}
           ;
            // location.href = "../sys/generator/code?tables=" + JSON.stringify(tableNames);
            var url = "../sys/generator/code";
            $.ajax({
                type: "POST",
                url: url,
                data:JSON.stringify(tableNames),
                success: function(r){
                    if(r.code === 0){
                        alert('操作成功', function(index){
                            vm.back();
                        });
                    }else{
                        alert(r.msg);
                    }
                }
            });

		}
	}
});

