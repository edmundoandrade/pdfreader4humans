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

	public PDFReader(URL url, PDFTextLocator textLocator, PDFGridLocator gridLocator) throws IOException {
		PDDocument doc = PDDocument.load(url);
		try {
			readAllPages(doc, textLocator, gridLocator);
		} finally {
			doc.close();
		}
	}

	private void readAllPages(PDDocument doc, PDFTextLocator textLocator, PDFGridLocator gridLocator) throws IOException {
		for (Object page : doc.getDocumentCatalog().getAllPages())
			readPage((PDPage) page, textLocator, gridLocator);
	}

	private void readPage(PDPage page, PDFTextLocator textLocator, PDFGridLocator gridLocator) throws IOException {
		GridComponent[] gridComponents = gridLocator.locateGridComponents(page);
		firstLevel.addAll(group(gridComponents));
		addComponents(gridComponents);
		addComponents(textLocator.locateTextComponents(page));
	}

	private void addComponents(Component[] components) {
		for (Component component : components) {
			Component container = findContainer(component);
			if (container != null)
				container.addChild(component);
			else
				firstLevel.add(component);
		}
	}

	private Component findContainer(Component component) {
		Component container = null;
		float area = Float.MAX_VALUE;
		for (Component possibleContainer : firstLevel)
			if (possibleContainer != component && possibleContainer.contains(component) && possibleContainer.getArea() < area) {
				container = possibleContainer;
				area = possibleContainer.getArea();
			}
		if (container != null) {
			System.out.println("> " + container.toString() + " " + container.getArea());
			System.out.println(" CONTAINS " + component.toString() + " " + component.getArea());
		}
		return container;
	}

	private List<Component> group(GridComponent[] gridComponents) {
		List<Component> list = new ArrayList<Component>();
		for (GridComponent component1 : gridComponents) {
			if (component1.getWidth() > component1.getHeight()) {
				List<GridComponent> crossedComponents = new ArrayList<GridComponent>();
				for (GridComponent component2 : gridComponents)
					if (component2.getHeight() > component2.getWidth() && component1.intersects(component2))
						crossedComponents.add(component2);
				float top = crossedComponents.size() > 0 ? crossedComponents.get(0).getFromY() : 0;
				float bottom = crossedComponents.size() > 0 ? crossedComponents.get(0).getToY() : 0;
				for (GridComponent component2 : crossedComponents) {
					if (component2.getFromY() == top && top < component1.getFromY()) {
						list.add(new GridComponent("group", component1.getFromX(), top, component2.getFromX(), component1.getFromY(), component1.getLineWidth()));
					}
					if (component2.getToY() == bottom && bottom > component1.getToY()) {
						list.add(new GridComponent("group", component1.getFromX(), component1.getFromY(), component2.getFromX(), bottom, component1.getLineWidth()));
					}
					top = component2.getFromY();
					bottom = component2.getToY();
				}
			}
		}
		return list;
	}

	public List<Component> getFirstLevelComponents() {
		return firstLevel;
	}
}
