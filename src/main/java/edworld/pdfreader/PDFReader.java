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
		List<GridComponent> containers = groupIntoBoxes(extendConnectedComponents(gridLocator.locateGridComponents(page)));
		firstLevel.addAll(containers);
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

	private List<GridComponent> groupIntoBoxes(List<GridComponent> gridComponents) {
		List<GridComponent> list = new ArrayList<GridComponent>();
		for (GridComponent component1 : gridComponents) {
			if (component1.getWidth() > component1.getHeight()) {
				List<GridComponent> crossedComponents = new ArrayList<GridComponent>();
				for (GridComponent component2 : gridComponents)
					if (component2.getHeight() > component2.getWidth() && component1.intersects(component2))
						crossedComponents.add(component2);
				float leftUp = component1.getFromX();
				float top = Float.MAX_VALUE;
				for (GridComponent component2 : crossedComponents) {
					if (component2.getFromY() < component1.getFromY()) {
						float nextTop = boxTop(component2, component1.getFromY(), gridComponents);
						if (top < component1.getFromY() && nextTop == top) {
							addBox(leftUp, top, component2.getFromX(), component1.getFromY(), component1, component2, list);
							leftUp = component2.getToX();
						}
						top = nextTop;
					}
				}
				float leftDown = component1.getFromX();
				float bottom = Float.MIN_VALUE;
				for (GridComponent component2 : crossedComponents) {
					if (component2.getFromY() >= component1.getFromY()) {
						float nextBottom = boxBottom(component2, component1.getToY(), gridComponents);
						if (bottom > component1.getToY() && nextBottom == bottom) {
							addBox(leftDown, component1.getToY(), component2.getFromX(), bottom, component1, component2, list);
							leftDown = component2.getToX();
						}
						bottom = nextBottom;
					}
				}
			}
		}
		list.addAll(gridComponents);
		return list;
	}

	private float boxTop(GridComponent component, float bottom, List<GridComponent> gridComponents) {
		float top = component.getFromY();
		for (GridComponent crossComponent : gridComponents) {
			if (crossComponent.getWidth() > crossComponent.getHeight() && crossComponent.intersects(component) && crossComponent.getFromX() < component.getFromX()
					&& crossComponent.getToY() < bottom && crossComponent.getToY() > top) {
				top = crossComponent.getToY();
			}
		}
		return top;
	}

	private float boxBottom(GridComponent component, float top, List<GridComponent> gridComponents) {
		float bottom = component.getToY();
		for (GridComponent crossComponent : gridComponents) {
			if (crossComponent.getWidth() > crossComponent.getHeight() && crossComponent.intersects(component) && crossComponent.getFromX() < component.getFromX()
					&& crossComponent.getFromY() > top && crossComponent.getFromY() < bottom) {
				bottom = crossComponent.getFromY();
			}
		}
		return bottom;
	}

	private void addBox(float fromX, float fromY, float toX, float toY, GridComponent component1, GridComponent component2, List<GridComponent> list) {
		list.add(new GridComponent("box", fromX, fromY, toX, toY, component1.getLineWidth()));
	}

	public List<Component> getFirstLevelComponents() {
		return firstLevel;
	}
}
