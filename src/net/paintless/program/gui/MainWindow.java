package net.paintless.program.gui;

import static com.lapissea.util.UtilL.*;

import java.io.InputStream;
import java.util.function.Consumer;

import com.lapissea.util.LogUtil;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
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
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.paintless.Paintless;

public class MainWindow extends Application{
	
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
	
	boolean	dragged,doubleClick;
	double	xDrag,yDrag;
	
	BorderPane	mainGui;
	StackPane	fileGui;
	Canvas		drawCanvas;
	
	@Override
	public void start(Stage win) throws Exception{
		window=win;
		window.setTitle("Paintless");
		window.initStyle(StageStyle.TRANSPARENT);
		initBase();
		
		mainGui=new BorderPane();
		
		HBox top=cls(new HBox(), "bar");
		mainGui.setTop(top);
		
		top.getChildren().add(btn("File", e->setGui(fileGui), "btn", "w60"));
		top.getChildren().add(btn("Home", e->setGui(fileGui), "btn", "w60"));
		top.getChildren().add(btn("Edit", e->setGui(fileGui), "btn", "w60"));
		top.getChildren().add(btn("Effect", e->setGui(fileGui), "btn", "w60"));
		top.getChildren().add(btn("Animate", e->setGui(fileGui), "btn", "w60"));
		
		ScrollPane scroll=new ScrollPane();
		mainGui.setCenter(scroll);
		scroll.getStyleClass().add("scroll-bar");
		
		StackPane stack=new StackPane();
		scroll.setContent(stack);
		drawCanvas=new Canvas(500,500);
		stack.getChildren().add(drawCanvas);
		
		scroll.setBackground(new Background(new BackgroundFill(new Color(0.8984375, 0.8984375, 0.8984375, 1), new CornerRadii(0), Insets.EMPTY)));
		
		//////////////
		
		fileGui=new StackPane();
		
		setGui(mainGui);
		
		new AnimationTimer(){
			
			@Override
			public void handle(long now){
				updateDraw();
			}
		}.start();
		window.show();
		loading=false;
		LogUtil.println(System.currentTimeMillis()-Paintless.start);
	}
	
	private void updateDraw(){
		GraphicsContext ctx=drawCanvas.getGraphicsContext2D();
		ctx.clearRect(0,0,drawCanvas.getWidth(),drawCanvas.getHeight());
		ctx.setFill(Color.BLACK);
		ctx.setStroke(Color.BLACK);
		ctx.strokeRect(0,0,drawCanvas.getWidth(),drawCanvas.getHeight());
		ctx.beginPath();
		ctx.moveTo(0, 0);
		ctx.lineTo(drawCanvas.getWidth()+(Math.random()-0.5)*50,drawCanvas.getHeight()+(Math.random()-0.5)*50);
		ctx.stroke();
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
		contentPane.setBorder(new Border(new BorderStroke(winUnactiveBord, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
		
		Scene scene=new Scene(root, 600, 400);
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
			max=i;
			minMax.setGraphic(max);
		});
		loadImg("unmax.png", i->{
			unmax=i;
			if(!window.isMaximized()) minMax.setGraphic(unmax);
		});
		loadImg("min.png", min::setGraphic);
		loadImg("x.png", x::setGraphic);
		
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
	
	private void loadImg(String img, Consumer<ImageView> onload){
		Paintless.LOADING_POOL.execute(()->Platform.runLater(()->onload.accept(new ImageView(new Image(src(img))))));
	}
	
	private void updateBorder(BorderPane borderPane0){
		if(window.isMaximized()) borderPane0.setBorder(null);
		else if(window.isFocused()) borderPane0.setBorder(new Border(new BorderStroke(winActiveBord, BorderStrokeStyle.SOLID, new CornerRadii(0), BorderWidths.DEFAULT)));
		else borderPane0.setBorder(new Border(new BorderStroke(winUnactiveBord, BorderStrokeStyle.SOLID, new CornerRadii(0), BorderWidths.DEFAULT)));
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
