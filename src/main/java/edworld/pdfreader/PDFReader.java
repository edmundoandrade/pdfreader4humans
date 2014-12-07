// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

public class PDFReader {
	private List<Component> firstLevel = new ArrayList<Component>();

	public PDFReader(URL url, PDFTextLocator textLocator, PDFGridLocator gridLocator, BoxDetector boxDetector) throws IOException {
		PDDocument doc = PDDocument.load(url);
		try {
			readAllPages(doc, textLocator, gridLocator, boxDetector);
		} finally {
			doc.close();
		}
	}

	private void readAllPages(PDDocument doc, PDFTextLocator textLocator, PDFGridLocator gridLocator, BoxDetector boxDetector) throws IOException {
		for (Object page : doc.getDocumentCatalog().getAllPages())
			readPage((PDPage) page, textLocator, gridLocator, boxDetector);
	}

	private void readPage(PDPage page, PDFTextLocator textLocator, PDFGridLocator gridLocator, BoxDetector boxDetector) throws IOException {
		List<GridComponent> gridComponents = gridLocator.locateGridComponents(page);
		List<BoxComponent> boxes = boxDetector.detectBoxes(gridComponents);
		List<Component> containers = new ArrayList<Component>(gridComponents.size() + boxes.size());
		containers.addAll(gridComponents);
		containers.addAll(boxes);
		firstLevel.addAll(boxes);
		addComponents(gridComponents, boxes);
		addComponents(textLocator.locateTextComponents(page), containers);
	}

	private void addComponents(List<? extends Component> components, List<? extends Component> containers) {
		for (Component component : components) {
			Component container = findContainer(component, containers);
			if (container != null)
				container.addChild(component);
			else
				firstLevel.add(component);
		}
	}

	private Component findContainer(Component component, List<? extends Component> containers) {
		Component container = null;
		float area = Float.POSITIVE_INFINITY;
		for (Component possibleContainer : containers)
			if (possibleContainer.contains(component) && possibleContainer.getArea() < area) {
				container = possibleContainer;
				area = possibleContainer.getArea();
			}
		return container;
	}

	public List<Component> getFirstLevelComponents() {
		return firstLevel;
	}
}
