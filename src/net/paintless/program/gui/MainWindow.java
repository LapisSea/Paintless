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
	
	Button button;
	
	public MainWindow(){
		
	}
	
	@Override
	public void start(Stage main) throws Exception{
		main.setTitle("your");
		button=new Button("mum");
		StackPane layout=new StackPane();
		layout.getChildren().add(button);
		Scene scene=new Scene(layout, 600, 400);
		main.setScene(scene);
		main.show();
		LogUtil.println(System.currentTimeMillis()-Paintless.start);
	}
	
}
