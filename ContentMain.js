"use strict";


const 
{
	remote,
	clipboard
}=require('electron'),
fs = require('fs'),
path=require('path');

function getFile(name,onLoad){
	winLoadingSvg.show();
	fs.readFile(path.join(__dirname, name), 'utf8', (err, data)=>{
		if(err)console.log(err);
		else onLoad(data);
		winLoadingSvg.hide();
	});
}

function getWin(){
	return remote.getCurrentWindow();
}
function iconify(){
	getWin().minimize();
}
function setMaximized(flag){
	var win=getWin();
	if(flag)win.maximize();
	else win.restore();
}
function toggleMaximize(){
	setMaximized(!getWin().isMaximized());
}
function exit(){
	getWin().close();
}

!function(){
	var f=(wat,flag)=>{
		var l=document.body.classList;
		if(flag)l.add(wat);
		else l.remove(wat);
	},
	win=getWin(),
	foc=()=>f("Focused",win.isFocused()),
	max=()=>f("Maximized",win.isMaximized()),
	full=()=>f("FullScreen",win.isFullScreen());

	foc();
	win.on("focus",foc).on("blur", foc);
	max();
	win.on("maximize",max).on("unmaximize",max);
	full();
	win.on("enter-full-screen",full).on("leave-full-screen",full).on("enter-html-full-screen",full).on("leave-html-full-screen",full);
	
}();

function nodeScriptReplace(node) {
	if ( nodeScriptIs(node) === true ) {
			node.parentNode.replaceChild( nodeScriptClone(node) , node );
	}
	else {
			var i        = 0;
			var children = node.childNodes;
			while ( i < children.length ) {
					nodeScriptReplace( children[i++] );
			}
	}

	return node;
}
function nodeScriptIs(node) {
	return node.tagName === 'SCRIPT';
}
function nodeScriptClone(node){
	var script  = document.createElement("script");
	script.text = node.innerHTML;
	for( var i = node.attributes.length-1; i >= 0; i-- ) {
			script.setAttribute( node.attributes[i].name, node.attributes[i].value );
	}
	return script;
}

var views={},loadingView=false;
var loaderElement=document.getElementById("Loader");
var viewBtns=document.getElementById("Views");
viewBtns.show=()=>{
	viewBtns.style.height=viewBtns.style.opacity="";
};
viewBtns.hide=()=>{
	viewBtns.style.height="0px";
	viewBtns.style.opacity=0;
};
var content=document.getElementById("Content");
var winTitlebar=document.getElementById("WinTitlebar");
var loadingView=undefined;
var lastView=undefined;
var activeView=undefined;

function backgroundFlush(flag){
	if(flag)document.body.setAttribute("bgFlush","");
	else document.body.removeAttribute("bgFlush");
};

function loadView(btn){
	if(loadingView)return;
	loadingView=true;
	winLoadingSvg.show();
	
	getFile("views/"+btn.innerHTML+".html",data=>{

		loadingView=views[btn.innerHTML]={name:btn.innerHTML};
		loaderElement.innerHTML=data;
		nodeScriptReplace(loaderElement);
		loaderElement.innerHTML="";
		loadingView=undefined;

		winLoadingSvg.hide();
		loadingView=false;

		openView(btn);
	});
}

function openView(btn){
	if(typeof btn==="string"){
		for(var i=0;i<viewBtns.children.length;i++){
			var b=viewBtns.children[i];
			if(b.innerHTML===btn){
				btn=b;
				break;
			}
		}
		if(typeof btn==="string"){
			console.warn("No view with name",btn);
			return;
		}
	}

	var view=views[btn.innerHTML];

	if(view===undefined){
		loadView(btn);
		return;
	}
	if(activeView!==undefined){
		var lv=views[activeView.innerHTML];
		if(lv.onClose)lv.onClose();
		else console.warn('No "onClose" function in:',lv);
		activeView.removeAttribute("active");
	}
	lastView=activeView;
	activeView=btn;

	btn.setAttribute("active","");
	if(view.onOpen)view.onOpen();
	else console.warn('No "onOpen" function in:',view);
}

openView("Home");

function resize(){
	
}

window.shift=false;
window.ctrl=false;
window.alt=false;
window.onkeydown=window.onkeyup=e=>{
	window.shift=e.shiftKey;
	window.ctrl=e.ctrlKey;
	window.alt=e.altKey;
}

