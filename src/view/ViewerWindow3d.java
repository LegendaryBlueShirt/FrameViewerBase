package view;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;
import view.interfaces.FramePresenter;
import view.interfaces.ViewerWindow;
import view.model.Data;
import view.model.Hitbox;

public class ViewerWindow3d extends Group implements ViewerWindow {
	List<Surface> availableSurfaces = new ArrayList<Surface>();
	Canvas dataView;
	Color background = Color.MAGENTA;
	private FramePresenter presenter;
	
	int currentFrame = 0;
	int currentX = 400;
	int currentY = 450;
	public BooleanProperty showEffects = new SimpleBooleanProperty(false);

	
	public void setDataView(Canvas canvas) {
		dataView = canvas;
	}
	
	@Override
    public boolean isResizable() {
      return true;
    }
 
	
	
	public void render() {
		GraphicsContext g = dataView.getGraphicsContext2D();
		g.clearRect(0, 0, dataView.getWidth(), dataView.getHeight());
		
		recycleSurfaces();
		
		if(presenter != null) {
			presenter.renderCurrentFrame(this);
			renderSprites();
			renderBoxes(g);
			renderData(g);
		}
	}
	
	int nUsedSurfaces = 0;
	public Surface getSurface() {
		Surface surface = null;
		if(availableSurfaces.size() <= nUsedSurfaces) {
			availableSurfaces.add(new Surface(this));
		}
		surface = availableSurfaces.get(nUsedSurfaces++);
		return surface;
	}
	
	public void recycleSurfaces() {
		for(int n = 0;n < nUsedSurfaces;n++) {
			availableSurfaces.get(n).remove();
		}
		nUsedSurfaces = 0;
	}
	
	private void renderSprites() {
		for(int n = 0;n < nUsedSurfaces; n++) {
			getChildren().add(availableSurfaces.get(n).getRect());
		}
	}
	
	private void renderBoxes(GraphicsContext g) {
		g.save();
		g.translate(getAxisX(), getAxisY());
		
		List<Hitbox> boxes = presenter.getCurrentHitboxes();
		for(Hitbox box: boxes) {
			g.setStroke(box.getColor());
			g.strokeRect(box.getX(), box.getY(), box.getWidth(), box.getHeight());
		}
		g.restore();
	}
	
	private void renderData(GraphicsContext g) {
		g.save();
		g.setFill(new Color(1,1,1,1));
		g.translate(16, 16);
		
		List<Data> currentData = presenter.getCurrentData();
		
		int yoffLeft = 0;
		int yoffRight = 0;
		for(Data data: currentData) {
			if(data.isExtra) {
				g.fillText(data.data, dataView.getWidth()-120, yoffRight);
				yoffRight+=16;
			} else {
				g.fillText(data.data, 0, yoffLeft);
				yoffLeft+=16;
			}
		}
		g.restore();
	}
	
	public EventHandler<WindowEvent> getWindowCloseHandler() {
		return new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//e.getWindow().dispose();
			}
			};
	}

	@Override
	public int getAxisX() {
		if(dragging) {
			return (int)(displaceX - clickOriginX) + currentX;
		} else {
			return currentX;
		}
	}
	
	@Override
	public int getAxisY() {
		if(dragging) {
			return (int)(displaceY - clickOriginY) + currentY;
		} else {
			return currentY;
		}
	}
	
	public void resetPosition() {
		currentX = (int) (dataView.getWidth()/2);
		currentY = (int) (dataView.getHeight()/2 + 150);
	}
	
	boolean dragging = false;
	double clickOriginX = 0;
	double clickOriginY = 0;
	double displaceX = 0;
	double displaceY = 0;
	public void onClick(MouseEvent event) {
		if(event.getButton() == MouseButton.PRIMARY) {
			dragging = true;
			clickOriginX = event.getScreenX();
			clickOriginY = event.getScreenY();
			displaceX = clickOriginX;
			displaceY = clickOriginY;
		}
	}
	
	public void onDrag(MouseEvent event) {
		if(dragging) {
			displaceX = event.getScreenX();
			displaceY = event.getScreenY();
		}
	}
	
	public void onRelease(MouseEvent event) {
		if(dragging) {
			if(event.getButton() == MouseButton.PRIMARY) {
				currentX = getAxisX();
				currentY = getAxisY();
				dragging = false;
			}
		}
	}

	@Override
	public void setPresenter(FramePresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public boolean showEffects() {
		return showEffects.get();
	}
}
