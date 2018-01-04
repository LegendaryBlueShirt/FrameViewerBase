package view;
import java.io.IOException;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.DepthTest;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import view.interfaces.FramePresenter;
import view.interfaces.FramePresenter.LoadCallback;
import view.interfaces.ViewerWindow;
import view.model.CharacterDef;
import view.model.FrameGroup;

public abstract class FrameViewer extends Application implements LoadCallback {
	protected FramePresenter presenter;
	ComboBox<CharacterDef> characterSelect;
	boolean animating = false;
	int sequenceTime = 0;
	//static int currentSequence = 0;
	MenuBar menu;
	ComboBox<FrameGroup> sequenceSelect;
	
	protected void setCharacterList(List<CharacterDef> characters) {
		sequenceSelect.getItems().clear();
		characterSelect.getItems().clear();
		characterSelect.getItems().addAll(characters);
		characterSelect.setDisable(true);
		sequenceSelect.setDisable(true);
		characterSelect.getSelectionModel().select(0);
		
		presenter.load(characters.get(0), this);
	}
	
	@Override
	public void onLoadComplete() {
		sequenceSelect.getItems().clear();
		sequenceSelect.getItems().addAll(presenter.getFrameGroups());
		characterSelect.setDisable(false);
		sequenceSelect.setDisable(false);
		sequenceSelect.getSelectionModel().select(0);
	}
	
	EventHandler<KeyEvent> keyListener = new EventHandler<KeyEvent>(){
		@Override
		public void handle(KeyEvent event) {
			if(presenter == null) {
				return;
			}
			int currentIndex = sequenceSelect.getSelectionModel().getSelectedIndex();
		    switch(event.getCode()) {
			    case UP:
			    	try{
			    		sequenceSelect.getSelectionModel().select(currentIndex-1);
			    	} catch(Exception e) {
			    		sequenceSelect.getSelectionModel().select(currentIndex);
			    	}
			    	event.consume();
			    	break;
			    case DOWN:
			    	try{
			    		sequenceSelect.getSelectionModel().select(currentIndex+1);
			    	} catch(Exception e) {
			    		sequenceSelect.getSelectionModel().select(currentIndex);
			    	}
			    	event.consume();
			    	break;
			    case LEFT:
			    	presenter.retreatFrame();
			    	animating = false;
			    	event.consume();
			    	break;
			    case RIGHT:
			    	presenter.advanceFrame();
			    	animating = false;
			    	event.consume();
			    	break;
			    case SPACE:
			    	animating = !animating;
			    	event.consume();
			    	break;
			    default:
		    }
		}
	};
	
	void createMenu(final ViewerWindow3d view) {
		menu = new MenuBar();
		
		Menu fileMenu = new Menu("File");
		MenuItem loadDirectory = new MenuItem("Load Directory");
		loadDirectory.setOnAction(event -> showDirectoryChooser());
		fileMenu.getItems().add(loadDirectory);
		
		Menu viewMenu = new Menu("View");
		MenuItem resetPosition = new MenuItem("Reset Position");
		resetPosition.setOnAction(event -> view.resetPosition());
		CheckMenuItem effectToggle = new CheckMenuItem("Effects Enabled");
		view.showEffects.bind(effectToggle.selectedProperty());
		
		viewMenu.getItems().add(resetPosition);
		viewMenu.getItems().add(effectToggle);
		
		menu.getMenus().addAll(fileMenu, viewMenu);
		
		characterSelect = new ComboBox<CharacterDef>();
		characterSelect.valueProperty().addListener(new ChangeListener<CharacterDef>() {
			@Override
			public void changed(ObservableValue<? extends CharacterDef> observable, CharacterDef oldValue,
					CharacterDef newValue) {
				if(presenter == null)
					return;
				if(newValue == null)
					return;
				if(characterSelect.isDisabled())
					return;
				presenter.load(newValue, FrameViewer.this);
			}});
		
		sequenceSelect = new ComboBox<FrameGroup>();
		sequenceSelect.valueProperty().addListener(new ChangeListener<FrameGroup>() {
			@Override
			public void changed(ObservableValue<? extends FrameGroup> observable, FrameGroup oldValue, FrameGroup newValue) {
				if(presenter == null)
					return;
				if(newValue == null)
					return;
				if(sequenceSelect.isDisabled())
					return;
				presenter.setFrameGroup(newValue);
				sequenceTime = 0;
			}});
		
		characterSelect.setDisable(true);
		sequenceSelect.setDisable(true);
	}
	
	protected abstract void showDirectoryChooser();
	protected abstract String getTitle();
	
	//static Thread looper;
	static AnimationTimer looper;
	public void start() throws IOException, InterruptedException {
		if(looper != null)
			looper.stop();
		//	looper.join();
		
		looper = new AnimationTimer() {
			long lastFrameNanos = 0;
			int framecount = 0;
			int framesSkipped = 0;
			final int FPS = 60;
			final long frameDurationNanos = (long) (1000000000.0/FPS);
			boolean skipFrame = false;
			@Override
			public void handle(long now) {
				framecount++;
				if(animating) {
					sequenceTime++;
					presenter.setTime(sequenceTime);
					//currentFrame = AnimHelper.getFrameForTime(sequenceData, sequenceTime);
				}
				
				long currentFrameNanos = now-lastFrameNanos;
				if(currentFrameNanos > frameDurationNanos) { //We're on time.
					//System.out.println(frameDurationNanos+"       "+currentFrameNanos+"   "+lastFrameNanos);
					skipFrame=true; //We gotta skip a frame.
					//System.out.println("Skipped frames -"+framesSkipped);
				}
				
				if(!skipFrame) {
					//System.out.println("Draw");
					view.render();
				} else {
					framesSkipped++;
					skipFrame = false;
				}
				
				lastFrameNanos = now;
			}};
		looper.start();	
	}
	
	public ViewerWindow getView() {
		return view;
	}
	
	static ViewerWindow3d view;

	@Override
	public void start(Stage primaryStage) throws Exception {
		view = new ViewerWindow3d();
		primaryStage.setOnCloseRequest(view.getWindowCloseHandler());
		primaryStage.setTitle(getTitle());
		
	    Scene theScene = new Scene( new VBox(), 800, 600 );
	    theScene.addEventFilter(KeyEvent.KEY_PRESSED,
                event -> keyListener.handle(event));
	    
		createMenu(view);
		
		menu.prefWidthProperty().bind(primaryStage.widthProperty());
		
		BorderPane border = new BorderPane();
		HBox topMenu = new HBox();
		
		topMenu.getChildren().addAll(characterSelect,sequenceSelect);
		// setup our canvas size and put it into the content of the frame
		border.setTop(topMenu);
		
		Pane pane = new Pane();
		border.setCenter(pane);
		
		//ViewerWindow3d threeDeeGroup = new ViewerWindow3d();
		SubScene threeDeeSpace = new SubScene(view, 400, 300, true, SceneAntialiasing.DISABLED);
		threeDeeSpace.setFill(Color.DIMGRAY);
		threeDeeSpace.setCamera(new PerspectiveCamera());
		threeDeeSpace.setDepthTest(DepthTest.DISABLE);
		
		pane.getChildren().add(threeDeeSpace);
		Canvas data = new Canvas();
		view.setDataView(data);
		pane.getChildren().add(data);
		
		data.widthProperty().bind(primaryStage.widthProperty());
	    data.heightProperty().bind(primaryStage.heightProperty().subtract(topMenu.heightProperty()));
	    threeDeeSpace.widthProperty().bind(primaryStage.widthProperty());
	    threeDeeSpace.heightProperty().bind(primaryStage.heightProperty().subtract(topMenu.heightProperty()));
		
	    theScene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> view.onClick(event));
	    theScene.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> view.onDrag(event));
	    theScene.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> view.onRelease(event));
	    
		((VBox)theScene.getRoot()).getChildren().addAll(menu, border);
		
		/*if(SteamHelper.getUNIELDirectory() != null) {
			setAppMode(AppMode.UNIEL_STEAM);
			unielHome = SteamHelper.getUNIELDirectory();
			start();
		}*/
		
		primaryStage.setScene( theScene );
		primaryStage.show();
	}
}
