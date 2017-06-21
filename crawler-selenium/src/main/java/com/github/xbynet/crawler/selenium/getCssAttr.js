function getStyle(obj, attr) {
    if (obj.currentStyle) {
        return obj.currentStyle[attr];
    } else {
        return document.defaultView.getComputedStyle(obj, null)[attr];
    }
}
function getCssAttr(sel,attr){
	var tmp=document.querySelector(sel);
	var res=getStyle(tmp,attr);
	return res;
}
return getCssAttr(arguments[0],arguments[1]);