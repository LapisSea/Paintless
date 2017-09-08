Brush={

	run:function(ctx,x,y,prevX,prevY){
		var color;
		if(mouseBtns.left)color="black";
		else if(mouseBtns.right)color="white"
		else return;

		if(ctx.globalCompositeOperation!="source-over")ctx.globalCompositeOperation="source-over";
		
		var brushSize=20*homeGui.force;
		var angle=Math.atan2(-(x-prevX),y-prevY);

		ctx.fillStyle=color;
		
		ctx.beginPath();
		ctx.arc(x,y,brushSize/2,angle,Math.PI+angle);
		ctx.arc(prevX,prevY,brushSize/2,Math.PI+angle,angle);
		ctx.fill();
	}

};
Eracer={

	run:function(ctx,x,y,prevX,prevY){
		var color;
		if(mouseBtns.left)color="black";
		else if(mouseBtns.right)color="white"
		else return;

		if(ctx.globalCompositeOperation!="destination-out")ctx.globalCompositeOperation="destination-out";
		
		var brushSize=20*homeGui.force;
		var angle=Math.atan2(-(x-prevX),y-prevY);
		
		ctx.beginPath();
		ctx.arc(x,y,brushSize/2,angle,Math.PI+angle);
		ctx.arc(prevX,prevY,brushSize/2,Math.PI+angle,angle);
		ctx.fill();
	}

};

!function(){
	var btn=document.createElement("button");
	btn.style.backgroundImage="url(assets/brush.svg)";
	btn.className="basic";
	homeGui.addTool(btn,Brush);
	btn.onclick();

	btn=document.createElement("button");
	btn.style.backgroundImage="url(assets/eracer.svg)";
	btn.className="basic";
	homeGui.addTool(btn,Eracer);
}();