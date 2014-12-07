package edworld.pdfreader;

import java.util.List;

public interface BoxDetector {
	List<BoxComponent> detectBoxes(List<GridComponent> components);
}
