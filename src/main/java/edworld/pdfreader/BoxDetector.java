package edworld.pdfreader;

import java.util.List;

public interface BoxDetector {
	List<GridComponent> detectBoxes(List<GridComponent> components);
}
