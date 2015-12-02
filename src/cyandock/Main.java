/*
 * 
 * Name: Cyan Dock 
 * Author: William Starkovich
 * 
 * -Description-
 * This launcher dock is created in Java FX and uses a config file to set up
 * Applications that you can then launch via the dock. You're also able to
 * Assign an icon to each launch button. It's styled with CSS.
 * 
 * Version: 1.01
 * 
 * Last update: 12/2/2015
 * 
 * -Change Log-
 * 12/2/2015  - Added error messages.
 * 12/1/2015  - Added Drag and Drop.
 * 11/28/2015 - Made it so if the Icon field of the App is a .exe it fetches the App's Icon.
 */

package cyandock;
	
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sun.awt.shell.ShellFolder;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;


public class Main extends Application {
	private Button[] btns;
	private String[] AppCommands;
	private String[] AppIconNames;
	
	private Button quitBtn;
	
	private float btnSize = 48;
	private float btnSpacing = 16;
	private int numApps = 0;
	
	Stage myStage;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			myStage = primaryStage;
			
			BufferedReader input = null;
			
			try {
				input =  new BufferedReader(new FileReader("./data/settings.cfg"));
				String line = null;
				line = input.readLine();
				numApps = Integer.parseInt(line.split(":")[1]);
				
				AppCommands = new String[numApps];
				AppIconNames = new String[numApps];
				
				for(int i = 0; i < numApps; i++){
					line = input.readLine();
					AppCommands[i] = line.split("=")[1];
				
					line = input.readLine();
					AppIconNames[i] = line.split("=")[1];
					
					System.out.println("DEBUG: " + AppCommands[i] + " " + AppIconNames[i]);
				}
			
			
				primaryStage.initStyle(StageStyle.TRANSPARENT);
				primaryStage.setTitle("CyanLauncher");
				
				btns = new Button[numApps];
				quitBtn = new Button();
				
				
				
				
				Pane  root = new Pane();
				root.getChildren().add(createBar());
				
				for(int i = 0; i < numApps; i++){
					btns[i] = createLaunchButton(i, AppIconNames[i], AppCommands[i]);
					root.getChildren().add(btns[i]);
				}
				
				quitBtn = createLaunchButton(numApps, "exit.png", "EXIT");
					root.getChildren().add(quitBtn);
					
					
					
					primaryStage.setScene(createScene(root, primaryStage));
					primaryStage.show();
				}
				
				catch(FileNotFoundException e){
					primaryStage.setScene(buildErrorDialog("Could not find settings.cfg! [Press this to exit]"));
					primaryStage.show();
				}
				
				catch(NullPointerException e){
					primaryStage.setScene(buildErrorDialog("Number of apps wrong in settings.cfg! [Press this to exit]"));
					primaryStage.show();
				}
				
				catch(ArrayIndexOutOfBoundsException e){
					primaryStage.setScene(buildErrorDialog("Malformed app string in settings.cfg! [Press this to exit]"));
						primaryStage.show();
				}
				
				
				catch(Exception e){
					primaryStage.setScene(buildErrorDialog("An error has occurred! [Press this to exit]"));
					primaryStage.show();
				}
			
			finally {
			    if(input != null) input.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public Pane createBar(){
		Pane  bgPane = new Pane();
		bgPane.getStyleClass().add("bar");
		bgPane.setPrefSize((btnSize) + (numApps * (btnSize + btnSpacing)), 64);
		bgPane.setPadding(new Insets(0,0,0,0));
		bgPane.setLayoutX(10);
		bgPane.setLayoutY(7);
		
		return bgPane;
	}
	
	public Button createLaunchButton(int i, String icon, String cmd) throws FileNotFoundException{
		Button btn = new Button();

		ClassLoader loader = getClass().getClassLoader();
		
		//System.out.println(loader.getResource("."));
		//System.out.println(loader.getResource("./../") + icon);
		
		if(icon.endsWith(".exe")){
			File file = new File(icon);
			// Get metadata and create an icon
			ShellFolder sf = ShellFolder.getShellFolder(file);
			Image fxImage = SwingFXUtils.toFXImage((BufferedImage) new ImageIcon(sf.getIcon(true)).getImage(), null);
			btn.setGraphic(new ImageView(fxImage));
		}
	
		else if(icon.endsWith(".png")) btn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("./../data/" + icon))));

		btn.setLayoutX(10 + ((btnSize + btnSpacing) * i));
		btn.setLayoutY(7);
		btn.setPadding(new Insets(0,0,0,0));
		btn.setPrefSize(btnSize, btnSize);
		
		final int cmdNum = new Integer(i).intValue();	

		btn.setOnAction(new EventHandler<ActionEvent>(){
			
			@Override
			public void handle(ActionEvent event){
				try {
					if(!cmd.equals("EXIT")) Runtime.getRuntime().exec(cmd);
					else System.exit(0);
				} catch (IOException e) { e.printStackTrace(); }

			}
			
		});
		
		btn.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent mouseEvent) {
				if(cmd.equals("EXIT")) quitBtn.setLayoutY(0);

				for(int i = 0; i < numApps; i++){
					btns[i].setLayoutY(7);
				}
				
				if(!cmd.equals("EXIT")){
					btns[cmdNum].setLayoutY(0);
					quitBtn.setLayoutY(7);
				}
				
			}
		});
		
		return btn;
	}
	
	public Scene createScene(Pane root, Stage primaryStage){
		Scene scene = new Scene(root,4 /*border*/ + (btnSize + btnSpacing) + (numApps * 64),64 + 7);
		
		scene.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent mouseEvent) {
				for(int i = 0; i < numApps; i++){
					btns[i].setLayoutY(7);
				}
				quitBtn.setLayoutY(7);
			}
		});
		
		final Delta dragDelta = new Delta();
		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent mouseEvent) {
				// record a delta distance for the drag and drop operation.
				dragDelta.x = primaryStage.getX() - mouseEvent.getScreenX();
				dragDelta.y = primaryStage.getY() - mouseEvent.getScreenY();
			}
		});
		
		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent mouseEvent) {
				primaryStage.setX(mouseEvent.getScreenX() + dragDelta.x);
				primaryStage.setY(mouseEvent.getScreenY() + dragDelta.y);
			}
		});
		
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		scene.setFill(null);
		
		scene.setOnDragOver(new EventHandler <DragEvent>() {

			public void handle(DragEvent event) {
				event.acceptTransferModes(TransferMode.ANY);
				event.consume();
			}
		});
		
		scene.setOnDragDropped(new EventHandler <DragEvent>() {
			public void handle(DragEvent event) {
				/* data dropped */
				//System.out.println("onDragDropped");
				/* if there is a string data on dragboard, read it and use it */
					Dragboard db = event.getDragboard();
					boolean success = false;
					if (db.hasFiles()) {
						success = true;
						String filePath = null;
						for (File file:db.getFiles()) {
							filePath = file.getAbsolutePath();
							System.out.println(filePath);
							if(filePath.endsWith(".exe")){
								try {
									addButton(filePath);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							
							else System.out.println("Not a .exe!");
						}
					}
					/* let the source know whether the string was successfully 
					 * transferred and used */
					event.setDropCompleted(success);
					
					event.consume();
			}
		});
		return scene;
	}
	
	public void addButton(String cmd) throws IOException{
		Button[] oldBtns = new Button[numApps];
		for(int i = 0; i < oldBtns.length; i++){
			oldBtns[i] = btns[i];
		}
		
		numApps++;
		
		btns = new Button[numApps];
		for(int i = 0; i < oldBtns.length; i++){
			btns[i] = oldBtns[i];
		}

		btns[oldBtns.length] = createLaunchButton(oldBtns.length, cmd, cmd);

		extendAppVars(cmd);
		
		Pane root = new Pane();
		root.getChildren().add(createBar());
		
		for(int i = 0; i < numApps; i++){
			root.getChildren().add(btns[i]);
		}
		
		BufferedWriter output =  new BufferedWriter(new FileWriter("./data/settings.cfg"));
		try {
			String fileGuts = "";
			fileGuts += "Number of Apps:" + numApps + "\n";
			
			for(int i = 0; i < numApps; i++){
				fileGuts += "App Command=" + AppCommands[i] + "\n";

				fileGuts += "App Icon=" + AppIconNames[i] + "\n";
				
				System.out.println("DEBUG: " + AppCommands[i] + " " + AppIconNames[i]);
			}
			
			output.write(fileGuts);
			
			
		}
		finally {
			output.close();
		}
		
		quitBtn = createLaunchButton(numApps, "exit.png", "EXIT");
		root.getChildren().add(quitBtn);
		
		myStage.setScene(createScene(root, myStage));
		myStage.show();
	}
	
	public void extendAppVars(String cmd){

		String[] oldCmds = new String[AppCommands.length];
		String[] oldIcons = new String[AppIconNames.length];
		for(int i = 0; i < oldCmds.length; i++){
			oldCmds[i] = AppCommands[i];
			oldIcons[i] = AppIconNames[i];
		}
		
		AppCommands = new String[numApps];
		AppIconNames = new String[numApps];
		for(int i = 0; i < oldCmds.length; i++){
			AppCommands[i] = oldCmds[i];
			AppIconNames[i] = oldIcons[i];
		}
		
		
		
		AppCommands[oldCmds.length] = cmd;
		AppIconNames[oldIcons.length] = cmd;
	}

	public Scene buildErrorDialog(String text){
		StackPane root = new StackPane();

		Button btn = new Button();
		btn.setText(text);
		btn.setLayoutX(0);
		btn.setLayoutY(25);
		btn.setPrefSize(340, 20);
		btn.setOnAction(new EventHandler<ActionEvent>(){
			
		@Override
		public void handle(ActionEvent event){
				System.exit(0);
			}
		});

		root.getChildren().add(btn);
		
		Scene myScene = new Scene(root, 350, 50);
		myScene.getStylesheets().add(getClass().getResource("error.css").toExternalForm());
		return myScene;
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}

class Delta { double x, y; } 
