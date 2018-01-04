package view.interfaces;

import view.Surface;

public interface ViewerWindow {
	public Surface getSurface();
	public int getAxisX();
	public int getAxisY();
	public void setPresenter(FramePresenter presenter);
	public boolean showEffects();
}
