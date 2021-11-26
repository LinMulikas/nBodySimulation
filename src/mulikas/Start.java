package mulikas;

import com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import javax.swing.*;

public class Start extends Application{
	
	public static void main(String[] args){
		try{
			UIManager.setLookAndFeel(new WindowsClassicLookAndFeel());
		} catch(UnsupportedLookAndFeelException e){
			e.printStackTrace();
		}
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception{
		primaryStage.setTitle("Universe");
		Pane pane = new Pane();
		Circle circle = new Circle();
		circle.setCenterX(100);
		circle.setCenterY(100);
		circle.setRadius(10);
		circle.setStroke(Color.BLACK);
		circle.setFill(Color.BLACK);
		pane.getChildren().addAll(circle);
		Scene scene = new Scene(pane, 1000, 1000);
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		Timeline timeline = new Timeline();
		primaryStage.show();
		
		
		
	}
}
