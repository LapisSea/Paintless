package net.paintless.program.gui;

import com.lapissea.util.LogUtil;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.paintless.Paintless;

public class MainWindow extends Application{
	
	static void start() throws Exception{
		launch(MainWindow.class);
	}
	
	Stage	window;
	Scene	mainScene,configScene;
	
	@Override
	public void start(Stage win) throws Exception{
		window=win;
		
		window.setTitle("Paintless");
		
		initMainScene();
		initConfigScene();
		
		window.setScene(mainScene);
		
		window.show();
		
		LogUtil.println(System.currentTimeMillis()-Paintless.start);
	}
	
	private void initMainScene(){
		Button button=new Button("open config");
		
		button.setOnAction(e->{
			window.setScene(configScene);
			LogUtil.println(e);
		});
		
		StackPane layout=new StackPane();
		layout.getChildren().add(button);
		
		mainScene=new Scene(layout, 600, 400);
		
	}
	private void initConfigScene(){
		Button button=new Button("open main");
		
		button.setOnAction(e->{
			window.setScene(mainScene);
			LogUtil.println(e);
		});
		
		StackPane layout=new StackPane();
		layout.getChildren().add(button);
		
		configScene=new Scene(layout, 600, 400);
		
	}
	
}
