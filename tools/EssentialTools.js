function BrushCanvas(onChange){
	this.canvas=createOffscreenCanvas();
	this.dirty=false;
	this.size=0;
	this.onChange=onChange;
	this.get=function(){
		if(this.dirty===true){
			this.dirty=false;
			this.canvas.width=this.canvas.height=this.size;
			if(this.size==0)return;

			var ctx=this.canvas.getContext("2d"),
				canvasData=ctx.createImageData(this.size,this.size);

			this.onChange(this,canvasData.data,this.size);
			ctx.putImageData(canvasData, 0,0);  
		}
		return this.canvas;
	}
}
function FloatSlider(config){
	this.value=config.def||0;
	this.min=config.min||Number.MIN_SAFE_INTEGER;
	this.max=config.max||Number.MAX_SAFE_INTEGER;
	this.cfgFile=config.cfgFile?config.cfgFile+".cfg":undefined;
	this.onchange=config.onchange;
	setTimeout(()=>this.onchange(this.value),1);
}

Brush={
	brushImg:(()=>{
		var b=new BrushCanvas((that,d)=>{
			var siz=that.size;
			console.log("asd")
			for(var i=0;i<d.length;i+=4){

				var x=(i/4)%siz;
				var y=Math.floor((i/4)/siz);

				var rad=siz/2,
					distX=x-rad,
					distY=y-rad;
				var a=Math.max(0,(rad-Math.sqrt(distX*distX+distY*distY))/rad);

				d[i+0]=that.color.r;
				d[i+1]=that.color.g;
				d[i+2]=that.color.b;
				d[i+3]=Math.pow(a,0.05+(1-b.hardnes)*1.95)*that.color.a;
			}
		});
		b.hardnes=1;
		b.color={r:255,g:255,b:255,a:255};
		b.setColor=col=>{
			if(b.color.r===col.r&&b.color.g===col.g&&b.color.b===col.b&&b.color.a===col.a)return;
			b.color.r=col.r;
			b.color.g=col.g;
			b.color.b=col.b;
			b.color.a=col.a;
			b.dirty=true;
		}
		b.setHardnes=hardnes=>{
			if(b.hardnes.r===hardnes)return;
			b.hardnes=hardnes;
			b.dirty=true;
		}
		
		return b;
	})(),
	dragDist:0,
	color1:{r:0,g:255,b:100,a:255},
	color2:{r:20,g:155,b:250,a:100},
	start:function(ctx,x,y){
		this.dragDist=this.brushImg.size/3+0.001;
		run(ctx,x,y,x,y);
	},
	stop:function(ctx,x,y,prevX,prevY){},
	run: function(ctx,x,y,prevX,prevY){
		if(isNaN(prevX))return;

		var color;
		if(mouseBtns.left)color=this.color1;
		else if(mouseBtns.right)color=this.color2;
		else return;

		var distX=x-prevX,
			distY=y-prevY,
			move=Math.sqrt(distX*distX+distY*distY),
			siz3=this.brushImg.size/4;
		this.dragDist+=move;
		if(this.dragDist<siz3)return;


		if(ctx.globalCompositeOperation!="source-over")ctx.globalCompositeOperation="source-over";
		this.brushImg.setColor(color);
		// ctx.fillStyle="red";
		// ctx.fillRect(x,y,10,10);
		// ctx.fillStyle="blue";
		// ctx.fillRect(prevX+5,prevY,10,10);

		//normalize
		distX*=siz3/move;
		distY*=siz3/move;

		var brush=this.brushImg.get();
		
		x=prevX-brush.width/2;
		y=prevY-brush.height/2;

		while(this.dragDist>=siz3){
			this.dragDist-=siz3;
			ctx.drawImage(brush,x,y);
			x+=distX;
			y+=distY;
		}

	},
	attributes:{
		size:new FloatSlider({
			cfgFile:"Brush",
			min:0,
			max:Number.MAX_SAFE_INTEGER,
			def:60,
			onchange:val=>{
				Brush.brushImg.size=Math.ceil(val);
				Brush.brushImg.dirty=true;
			}
		}),
		hardnes:new FloatSlider({
			cfgFile:"Hardnes",
			min:0,
			max:1,
			def:0,
			onchange:val=>Brush.brushImg.setHardnes(val)
		})
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