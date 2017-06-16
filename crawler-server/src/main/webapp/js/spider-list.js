function changeState(name){
	var t=$("#stateBtn").text();
	var method='start';
	if(t=='停止'){
		method='stop';
	}
	$.get(baseUrl+"monitor?name="+name+"&method="+method,function(data){
		if(data=='true'){
			$("stateBtn").text(method=='start'?'停止':'启动');
		}else{
			alert("请求失败:"+data);
		}
	},"text")
}