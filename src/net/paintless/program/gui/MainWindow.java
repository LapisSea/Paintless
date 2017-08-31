package net.paintless.program.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

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
	}
	
}
