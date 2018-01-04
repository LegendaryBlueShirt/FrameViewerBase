package view;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import view.interfaces.ViewerWindow;

public class Surface {
	private final ViewerWindow anchor;
	private Rectangle plane;
	private Translate position = new Translate();
	private Rotate rotation = new Rotate(0,0,0);
	private Scale scale = new Scale();
	
	public Surface(ViewerWindow anchor) {
		this.anchor = anchor;
	}
	
	public void setSprite(Image sprite, int xAxis, int yAxis) {
		remove();
		plane = new Rectangle(xAxis, yAxis, sprite.getWidth(), sprite.getHeight());
		plane.getTransforms().addAll(position, rotation, scale);
		plane.setFill(new ImagePattern(sprite, 0, 0, 1, 1, true));
	}
	
	public void remove() {
		if(plane == null) {
			return;
		}
		Node parent = plane.getParent();
		if(parent == null) {
			return;
		}
		if(parent instanceof Group) {
			((Group) parent).getChildren().remove(plane);
		}
	}
	
	public Rectangle getRect() {
		return plane;
	}
	
	public void setPosition(int x, int y, int z) {
		position.setX(anchor.getAxisX() + x);
		position.setY(anchor.getAxisY() + y);
		position.setZ(z);
	}
	
	public void setBlendMode(int mode) {
		switch(mode) {
			case 10: plane.setBlendMode(BlendMode.OVERLAY);
			default: plane.setBlendMode(null);
		}
	}
	
//	public void setRotation(float x, float y, float z) {
//		xRot.setAngle(360*x);
//		yRot.setAngle(360*y);
//		zRot.setAngle(360*z);
//	}
	
	public void matrixRotate(double yaw, double pitch, double roll){
		roll*= (-2.0*Math.PI);
		pitch*= (2.0*Math.PI);
		yaw*= (2.0*Math.PI);
	    double A11=Math.cos(roll)*Math.cos(yaw);
	    double A12=Math.cos(pitch)*Math.sin(roll)+Math.cos(roll)*Math.sin(pitch)*Math.sin(yaw);
	    double A13=Math.sin(roll)*Math.sin(pitch)-Math.cos(roll)*Math.cos(pitch)*Math.sin(yaw);
	    double A21=-Math.cos(yaw)*Math.sin(roll);
	    double A22=Math.cos(roll)*Math.cos(pitch)-Math.sin(roll)*Math.sin(pitch)*Math.sin(yaw);
	    double A23=Math.cos(roll)*Math.sin(pitch)+Math.cos(pitch)*Math.sin(roll)*Math.sin(yaw);
	    double A31=Math.sin(yaw);
	    double A32=-Math.cos(yaw)*Math.sin(pitch);
	    double A33=Math.cos(pitch)*Math.cos(yaw);

	    double d = Math.acos((A11+A22+A33-1d)/2d);
	    if(d!=0d){
	        double den=2d*Math.sin(d);
	        Point3D p= new Point3D((A32-A23)/den,(A13-A31)/den,(A21-A12)/den);
	        rotation.setAxis(p);
	        rotation.setAngle(Math.toDegrees(d));                    
	    }
	}
	
	public void setScale(float x, float y, float z) {
		scale.setX(x);
		scale.setY(y);
		scale.setZ(z);
	}
}
