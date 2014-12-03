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
		List<GridComponent> extendedComponents = extendConnectedComponents(gridLocator.locateGridComponents(page));
		List<GridComponent> boxes = boxDetector.detectBoxes(extendedComponents);
		List<GridComponent> containers = new ArrayList<GridComponent>(boxes);
		containers.addAll(extendedComponents);
		firstLevel.addAll(boxes);
		addComponents(extendedComponents, boxes);
		addComponents(textLocator.locateTextComponents(page), containers);
	}

	private void addComponents(List<? extends Component> components, List<GridComponent> containers) {
		for (Component component : components) {
			Component container = findContainer(component, containers);
			if (container != null)
				container.addChild(component);
			else
				firstLevel.add(component);
		}
	}

	private Component findContainer(Component component, List<GridComponent> containers) {
		Component container = null;
		float area = Float.MAX_VALUE;
		for (Component possibleContainer : containers)
			if (possibleContainer.contains(component) && possibleContainer.getArea() < area) {
				container = possibleContainer;
				area = possibleContainer.getArea();
			}
		if (container != null) {
			System.out.println("> " + container.toString() + " " + container.getArea());
			System.out.println(" CONTAINS " + component.toString() + " " + component.getArea());
		}
		return container;
	}

	private List<GridComponent> extendConnectedComponents(List<GridComponent> gridComponents) {
		List<GridComponent> listNotExtended = new ArrayList<GridComponent>(gridComponents);
		List<GridComponent> list = new ArrayList<GridComponent>();
		for (GridComponent component1 : gridComponents) {
			GridComponent next = component1;
			List<GridComponent> horizontalExtension = new ArrayList<GridComponent>();
			for (GridComponent component2 : gridComponents) {
				if (component1 != component2 && next.intersects(component2) && component1.getFromY() == component2.getFromY() && component1.getToY() == component2.getToY()) {
					horizontalExtension.add(component2);
					next = component2;
				}
			}
			addExtendedComponent(component1, horizontalExtension, list, listNotExtended);
			next = component1;
			List<GridComponent> verticalExtension = new ArrayList<GridComponent>();
			for (GridComponent component2 : gridComponents) {
				if (component1 != component2 && next.intersects(component2) && component1.getFromX() == component2.getFromX() && component1.getToX() == component2.getToX()) {
					verticalExtension.add(component2);
					next = component2;
				}
			}
			addExtendedComponent(component1, verticalExtension, list, listNotExtended);
		}
		list.addAll(listNotExtended);
		return list;
	}

	private void addExtendedComponent(GridComponent component1, List<GridComponent> extension, List<GridComponent> list, List<GridComponent> listNotExtended) {
		if (extension.size() == 0 || !listNotExtended.contains(component1))
			return;
		float fromX = component1.getFromX();
		float fromY = component1.getFromY();
		float toX = component1.getToX();
		float toY = component1.getToY();
		for (GridComponent component2 : extension) {
			if (!listNotExtended.contains(component2))
				return;
			fromX = Math.min(component2.getFromX(), fromX);
			fromY = Math.min(component2.getFromY(), fromY);
			toX = Math.max(component2.getToX(), toX);
			toY = Math.max(component2.getToY(), toY);
		}
		GridComponent extendedComponent = new GridComponent("extension", fromX, fromY, toX, toY, component1.getLineWidth());
		extendedComponent.addChild(component1);
		listNotExtended.remove(component1);
		for (GridComponent component2 : extension) {
			extendedComponent.addChild(component2);
			listNotExtended.remove(component2);
		}
		list.add(extendedComponent);
	}

	public List<Component> getFirstLevelComponents() {
		return firstLevel;
	}
}
