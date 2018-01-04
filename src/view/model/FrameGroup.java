package view.model;

import java.util.Locale;

import view.util.NameOverride;

public abstract class FrameGroup {
	private final static String formatString = "%d %s";
	@Override
	public final String toString() {
		String properName = NameOverride.getNameOverride(getId());
		if(properName != null) {
			return String.format(Locale.US, formatString, getId(), properName);
		} else {
			return String.format(Locale.US, formatString, getId(), getName());
		}
	}
	
	public abstract int getId();
	public abstract String getName();
}
