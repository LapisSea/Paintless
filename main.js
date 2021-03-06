"use strict";

const 
	windowStateKeeper = require('electron-window-state'),
	path=require('path'),
	url=require('url'),
	{
		BrowserWindow,
		app,
		globalShortcut,
		clipboard,
		webFrame
	}=require('electron');

let mainWindow;

function createWin(){
	let winState=windowStateKeeper({defaultWidth: 1000, defaultHeight: 800});
	mainWindow=new BrowserWindow({
		x: winState.x,
		y: winState.y,
		width: winState.width,
		height: winState.height,
		minHeight: 200,
		minWidth: 300,
		frame: false,
		show: false,
		icon: path.join(__dirname, 'assets/PaintlessIco64.png'),
		webPreferences: {'experimentalFeatures': true}
	});
	mainWindow.on('ready-to-show', ()=>{
		mainWindow.show();
		mainWindow.focus();
	});
	
	winState.manage(mainWindow);
	mainWindow.loadURL(url.format({pathname: path.join(__dirname, 'index.html'),protocol: 'file:',slashes: true}));
	mainWindow.on('closed', ()=>mainWindow = null);
	if(process.argv.indexOf("-dev")!==-1){
		globalShortcut.register('F5',()=>mainWindow.reload());
		globalShortcut.register('F6',()=>mainWindow.webContents.toggleDevTools());
	}
}


app.on('ready', createWin);

app.on('window-all-closed', function (){
	if(process.platform!=='darwin')app.quit();
});
app.on('activate', ()=>{
	if(mainWindow===null)createWin();
});