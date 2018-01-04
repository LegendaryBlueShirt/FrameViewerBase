package view.interfaces;

import java.io.File;
import java.util.List;

import view.model.CharacterDef;
import view.model.Data;
import view.model.FrameGroup;
import view.model.Hitbox;

public interface FramePresenter {
	public void setRootDir(File root);
	public <L extends CharacterDef> List<L> getCharacters();
	public void load(CharacterDef character, LoadCallback loader);
	public <T extends FrameGroup> List<T> getFrameGroups();
	public void setFrameGroup(FrameGroup group);
	
	public void setTime(int sequenceTime);
	public void advanceFrame();
	public void retreatFrame();
	
	public void renderCurrentFrame(ViewerWindow window);
	
	public List<Hitbox> getCurrentHitboxes();
	public List<Data> getCurrentData();
	
	interface LoadCallback {
		void onLoadComplete();
	}
}