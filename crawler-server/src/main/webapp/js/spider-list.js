function changeState(name){
	var t=$("#stateBtn").text().trim();
	var method='start';
	if(t=='停止'){
		method='stop';
	}
	$.get(baseUrl+"monitor?name="+name+"&method="+method,function(data){
		if(data=='true'){
			$("#stateBtn").text(method=='start'?'停止':'启动');
			$("#status").text(method=='start'?"running":"stopping...");
		}else{
			alert("请求失败:"+data);
		}
	},"text")
}