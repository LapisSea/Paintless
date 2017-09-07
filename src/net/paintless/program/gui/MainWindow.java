package net.paintless.program.gui;

import static com.lapissea.util.UtilL.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import com.lapissea.util.LogUtil;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.paintless.Paintless;

public class MainWindow extends Application{
	
	class Vec2D{
		
		double	x;
		double	y;
		
		public void rotate(double angle, double axisX, double axisY){
			double tY=y-axisX,tX=x-axisY;
			double cosa=Math.cos(angle);
			double sina=Math.sin(angle);
			x=tX*cosa+tY*sina+axisX;
			y=-tX*sina+tY*cosa+axisY;
		}
	}
	
	static boolean loading=true;
	
	private static final Color	winActiveBord	=new Color(0.09375, 0.51171875, 0.83984375, 1);
	private static final Color	winUnactiveBord	=new Color(0.5, 0.5, 0.5, 0.6640625);
	
	public static void start() throws Exception{
		launch(MainWindow.class);
	}
	
	public static boolean loading(){
		return loading;
	}
	
	Stage		window;
	BorderPane	contentPane;
	ImageView	unmax	=null;
	ImageView	max		=null;
	
	boolean	dragged,doubleClick,imgDrag;
	double	xDrag,yDrag,wantedZoom=1,wantedRotation=0,wantedTopAnim=40;
	
	Vec2D lastImg=new Vec2D();
	
	SimpleBooleanProperty	ctrlDown=new SimpleBooleanProperty(false);
	SimpleBooleanProperty	altDown	=new SimpleBooleanProperty(false);
	SimpleDoubleProperty	zoom	=new SimpleDoubleProperty(1);
	SimpleDoubleProperty	rotation=new SimpleDoubleProperty(0);
	SimpleDoubleProperty	topAnim=new SimpleDoubleProperty(40);
	
	BorderPane	mainGui;
	StackPane	fileGui;
	Canvas		drawCanvas;
	
	@Override
	public void start(Stage win) throws Exception{
		window=win;
		window.setTitle("Paintless");
		window.initStyle(StageStyle.TRANSPARENT);
		initBase();
		window.show();
		
		new AnimationTimer(){
			
			@Override
			public void handle(long now){
				updateDraw();
			}
		}.start();
		
		initMainGui();
		
		fileGui=new StackPane();
		fileGui.setBackground(solidBg(Color.WHITE));
		setGui(mainGui);
		loading=false;
		LogUtil.println(System.currentTimeMillis()-Paintless.start);
		
	}
	
	interface a{
		int a();
	}
	
	private void initMainGui(){
		mainGui=cls(new BorderPane(), "gui");
		
		HBox top=cls(new HBox(), "bar");
		mainGui.setTop(top);
		
		topAnim.addListener(e->{
			top.setMaxHeight(topAnim.get());
		});
		
		Label zoomlab=new Label("  Zoom: 100%, Rotation: 0°");
		zoomlab.prefHeight(40);
		Runnable updateLabel=()->{
			zoomlab.setText("Zoom: "+Math.round(zoom.get()*100_00)/100D+"% Rotation: "+Math.round(rotation.get()*100)/100D+"°");
		};
		zoom.addListener(e->updateLabel.run());
		rotation.addListener(e->updateLabel.run());
		top.getChildren().addAll(
				btn("File", e->setGui(fileGui), "btn", "w60"),
				btn("Home", e->setGui(fileGui), "btn", "w60"),
				btn("Edit", e->setGui(fileGui), "btn", "w60"),
				btn("Effect", e->setGui(fileGui), "btn", "w60"),
				btn("Animate", e->setGui(fileGui), "btn", "w60"), zoomlab);
		
		ScrollPane scroll=new ScrollPane();
		mainGui.setCenter(scroll);
		scroll.getStyleClass().add("scroll-bar");
		scroll.setFitToHeight(true);
		scroll.setFitToWidth(true);
		
		Pane pane=new Pane(drawCanvas=new Canvas());
		pane.setBorder(new Border(new BorderStroke(new Color(0.7734375, 0.7734375, 0.7734375, 1), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
		pane.setPadding(new Insets(1));
		
		Vec2D xy1=new Vec2D();
		Vec2D xy2=new Vec2D();
		
		StackPane actualSize=new StackPane(pane);
		Runnable updateBg=()->{
			double w=drawCanvas.getWidth()*zoom.get();
			double h=drawCanvas.getHeight()*zoom.get();
			pane.setMinSize(w, h);
			pane.setMaxSize(w, h);
			pane.getTransforms().clear();
			double rot=rotation.get();
			w/=2;
			h/=2;
			pane.getTransforms().add(new Rotate(rot, w, h));
			rot=Math.toRadians(rot);
//			rot=Math.abs(rot)%180;
//			if(rot>90)rot=90-rot;
			xy1.x=w;
			xy1.y=h;
			xy2.x=w;
			xy2.y=-h;
			xy1.rotate(rot, 0, 0);
			xy2.rotate(rot, 0, 0);
			
			long vert=Math.round(Math.max(Math.abs(xy1.x), Math.abs(xy2.x)));
			long hor=Math.round(Math.max(Math.abs(xy1.y), Math.abs(xy2.y)));
			actualSize.setMinSize(vert*2, hor*2);
			actualSize.setMaxSize(vert*2, hor*2);
		};
		
		drawCanvas.heightProperty().addListener(e->updateBg.run());
		drawCanvas.widthProperty().addListener(e->updateBg.run());
		drawCanvas.setWidth(100);
		drawCanvas.setHeight(100);
//		actualSize.setBackground(solidBg(Color.LIME));
		StackPane s=new StackPane(actualSize);
		s.setFocusTraversable(true);
		mainGui.setOnMouseMoved(e->{
			wantedTopAnim=e.getY()<top.getPrefHeight()*1.2?top.getPrefHeight():0;
		});
		mainGui.setOnKeyPressed(e->{
			ctrlDown.set(e.isControlDown());
			altDown.set(e.isAltDown());
		});
		mainGui.setOnKeyReleased(e->{
			ctrlDown.set(e.isControlDown());
			altDown.set(e.isAltDown());
		});
		mainGui.setOnScroll(e->{
			if(!ctrlDown.get()) return;
			
			double d=e.getDeltaX()+e.getDeltaY();
			
			if(altDown.get()){
				wantedRotation+=d/5;
			}else{
				wantedZoom=Math.max(wantedZoom+wantedZoom/d*10, 0.01);
				if(wantedZoom>20) wantedZoom=20;
			}
		});
		scroll.setContent(s);
		pane.setBackground(solidBg(Color.WHITE));
		pane.setEffect(new DropShadow(10, 0, 2, new Color(0, 0, 0, 0.1)));
		
		loadImg("tile.png", 16, 16, img->{
			pane.setBackground(new Background(new BackgroundImage(img, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
		});
		pane.setCache(true);
		
//		GraphicsContext ctx=drawCanvas.getGraphicsContext2D();
//		ctx.setFontSmoothingType(FontSmoothingType.GRAY);
//
//		ctx.setFill(Color.WHITE);
//		ctx.fillRect(0, 0, drawCanvas.getWidth(), drawCanvas.getHeight());
//		ctx.setFill(Color.BLACK);
		
		scroll.setBackground(solidBg(new Color(0.8984375, 0.8984375, 0.8984375, 1)));
		
		drawCanvas.setOnMousePressed(e->{
			lastImg.x=e.getX();
			lastImg.y=e.getY();
			applyTool(e);
		});
		drawCanvas.setOnMouseDragged(e->applyTool(e));
		rotation.addListener(e->{
			updateBg.run();
		});
		zoom.addListener(e->{
			ObservableList<Transform> t=drawCanvas.getTransforms();
			t.clear();
			t.add(new Scale(zoom.get(), zoom.get(), 0, 0));
			updateBg.run();
		});
		
	}
	
	private void applyTool(MouseEvent e){
		GraphicsContext ctx=drawCanvas.getGraphicsContext2D();
		double radius=6;
		double xDist=lastImg.x-e.getX(),yDist=lastImg.y-e.getY(),dist=Math.sqrt(xDist*xDist+yDist*yDist);
		
		for(double i=0;i<dist;i+=radius/3){
			double percent=i/dist;
			ctx.setFill(Color.BLACK);
			ctx.fillOval(e.getX()-radius/2+xDist*percent, e.getY()-radius/2+yDist*percent, radius, radius);
		}
		ctx.setFill(Color.BLACK);
		ctx.fillOval(e.getX()-radius/2, e.getY()-radius/2, radius, radius);
		lastImg.x=e.getX();
		lastImg.y=e.getY();
	}
	
	private Background solidBg(Color color){
		return new Background(new BackgroundFill(color, new CornerRadii(0), Insets.EMPTY));
	}
	
	private void updateDraw(){
		if(Math.abs(zoom.get()-wantedZoom)>0.001) zoom.set((zoom.get()*2+wantedZoom)/3);
		if(wantedRotation>=360){
			wantedRotation-=360;
			rotation.set(rotation.get()-360);
		}else if(wantedRotation<=-360){
			wantedRotation+=360;
			rotation.set(rotation.get()+360);
		}
		
		if(Math.abs(rotation.get()-wantedRotation)>0.001) rotation.set((rotation.get()*2+wantedRotation)/3);
		if(Math.abs(topAnim.get()-wantedTopAnim)>0.001) topAnim.set((topAnim.get()*2+wantedTopAnim)/3);
	}
	
	public void setGui(Node gui){
		contentPane.setCenter(gui);
	}
	
	private void initBase(){
		
		int shadowSize=20;
		
		StackPane root=new StackPane();
		root.setEffect(new DropShadow(shadowSize, new Color(0, 0, 0, 0.5)));
		root.setPadding(new Insets(shadowSize));
		root.setBackground(Background.EMPTY);
		contentPane=new BorderPane();
		root.getChildren().add(contentPane);
		contentPane.setBorder(border(winUnactiveBord, 1));
		
		Scene scene=new Scene(root, 1200, 750, false, SceneAntialiasing.BALANCED);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		HBox titleBar=cls(new HBox(), "bar");
		
		Pane spacer=new Pane();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		Label title=new Label(window.getTitle());
		title.setCache(true);
		title.setPrefHeight(40);
		title.setPadding(new Insets(0, 0, 0, 10));
		spacer.maxHeight(1);
		
		titleBar.setOnMousePressed(e->{
			dragged=false;
			if(doubleClick=e.getClickCount()==2){
				window.setMaximized(true);
				return;
			}
			xDrag=e.getScreenX()-window.getX();
			yDrag=e.getScreenY()-window.getY();
		});
		titleBar.setOnMouseDragged(e->{
			if(window.isFullScreen()||doubleClick) return;
			if(window.isMaximized()){
				double percentX=e.getSceneX()/(window.getWidth()-120);
				yDrag=e.getScreenY()+shadowSize;
				window.setMaximized(false);
				xDrag=(window.getWidth()-122+shadowSize)*percentX+shadowSize;
				return;
			}
			window.setX(e.getScreenX()-xDrag);
			window.setY(e.getScreenY()-yDrag);
			dragged=true;
		});
		titleBar.setOnMouseReleased(e->{
			if(!dragged) return;
			if(e.getScreenY()==0){
				new Thread(()->{
					sleep(30);
					Platform.runLater(()->window.setMaximized(true));
				}).start();
			}
		});
		
//		new Image(src("min.png")) new Image(src("x.png"))
		Button minMax,min,x;
		
		titleBar.getChildren().addAll(
				title,
				spacer,
				min=btn("", e->window.setIconified(true), "btn", "w40"),
				minMax=btn("", e->window.setMaximized(!window.isMaximized()), "btn", "w40"),
				x=btn("", e->{
					window.hide();
					System.exit(0);
				}, "btn", "w40"));
		
		loadImg("max.png", i->{
			max=new ImageView(i);
			minMax.setGraphic(max);
		});
		loadImg("unmax.png", i->{
			unmax=new ImageView(i);
			if(!window.isMaximized()) minMax.setGraphic(unmax);
		});
		loadImg("min.png", i->min.setGraphic(new ImageView(i)));
		loadImg("x.png", i->x.setGraphic(new ImageView(i)));
		
		contentPane.setTop(titleBar);
		
		scene.setFill(Color.TRANSPARENT);
		
		window.setScene(scene);
		ResizeHelper.addResizeListener(shadowSize+1, window);
		
		window.setMinHeight(42+shadowSize*2);
		window.setMinWidth(106+shadowSize*2);
		
		window.focusedProperty().addListener(e->updateBorder((BorderPane)window.getScene().getRoot().getChildrenUnmodifiable().get(0)));
		window.maximizedProperty().addListener(e->{
			StackPane root0=(StackPane)window.getScene().getRoot();
			if(window.isMaximized()){
				minMax.setGraphic(unmax);
				root0.setPadding(new Insets(0));
			}else{
				root0.setPadding(new Insets(shadowSize));
				minMax.setGraphic(max);
			}
			updateBorder((BorderPane)root0.getChildrenUnmodifiable().get(0));
		});
		window.widthProperty().addListener(e->{
			if(window.getWidth()<window.getMinWidth()) window.setWidth(window.getMinWidth());
		});
		window.heightProperty().addListener(e->{
			if(window.getHeight()<window.getMinHeight()) window.setHeight(window.getMinHeight());
		});
	}
	
	private Border border(Color color, int width){
		BorderWidths wid;
		switch(width){
		case 0:
			wid=BorderWidths.EMPTY;
			break;
		case 1:
			wid=BorderWidths.DEFAULT;
			break;
		default:
			wid=new BorderWidths(width);
			break;
		}
		return new Border(new BorderStroke(color, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, wid));
	}
	
	private void loadImg(String img, Consumer<Image> onload){
		loadImg(img, 0, 0, onload);
	}
	
	private void loadImg(String img, int w, int h, Consumer<Image> onload){
		Paintless.LOADING_POOL.execute(()->Platform.runLater(()->{
			try(InputStream s=src(img)){
				onload.accept(new Image(s, w, h, true, false));
			}catch(IOException e){
				e.printStackTrace();
			}
		}));
	}
	
	private void updateBorder(BorderPane borderPane0){
		borderPane0.setBorder(window.isMaximized()?null:border(window.isFocused()?winActiveBord:winUnactiveBord, 1));
	}
	
	private InputStream src(String path){
		return getClass().getResourceAsStream(path);
	}
	
	private Button btn(String txt, EventHandler<ActionEvent> action, String...cls){
		return btn(new Button(txt), action, cls);
	}
	
	private Button btn(Button t, EventHandler<ActionEvent> action, String...cls){
		t.setOnAction(action);
		return cls(t, cls);
	}
	
	private <T extends Node> T cls(T t, String...cls){
		t.getStyleClass().addAll(cls);
		return t;
	}
	
	public void exitApp0(){
		System.exit(0);
	}
}
