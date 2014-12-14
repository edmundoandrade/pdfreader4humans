// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

public class PDFReader {
	private List<Component> firstLevel = new ArrayList<Component>();

	/**
	 * Class responsible for reading PDF contents in the same order a human would read them.
	 * 
	 * @param url
	 *            the PDF's location
	 * @param componentLocator
	 *            an instance of a PDFComponentLocator subclass such as MainPDFComponentLocator
	 * @param boxDetector
	 *            an instance of a BoxDetector subclass such as MainBoxDetector
	 * @param marginDetector
	 *            an instance of a MarginDetector subclass such as MainMarginDetector
	 * @throws IOException
	 */
	public PDFReader(URL url, PDFComponentLocator componentLocator, BoxDetector boxDetector, MarginDetector marginDetector) throws IOException {
		PDDocument doc = PDDocument.load(url);
		try {
			readAllPages(doc, componentLocator, boxDetector, marginDetector);
		} finally {
			doc.close();
		}
	}

	private void readAllPages(PDDocument doc, PDFComponentLocator componentLocator, BoxDetector boxDetector, MarginDetector marginDetector) throws IOException {
		for (Object page : doc.getDocumentCatalog().getAllPages())
			readPage((PDPage) page, componentLocator, boxDetector, marginDetector);
	}

	private void readPage(PDPage page, PDFComponentLocator componentLocator, BoxDetector boxDetector, MarginDetector marginDetector) throws IOException {
		List<GridComponent> gridComponents = componentLocator.locateGridComponents(page);
		List<TextComponent> textComponents = componentLocator.locateTextComponents(page);
		List<BoxComponent> boxes = boxDetector.detectBoxes(gridComponents);
		List<Component> containers = new ArrayList<Component>();
		containers.addAll(gridComponents);
		containers.addAll(boxes);
		List<Component> groups = groupConnectedComponents(containers);
		containers.addAll(groups);
		firstLevel.addAll(groups);
		addComponents(boxes, firstLevel, groups);
		addComponents(gridComponents, firstLevel, containers);
		List<MarginComponent> margins = marginDetector.detectMargins(group(textComponents, firstLevel));
		containers.addAll(margins);
		firstLevel.addAll(margins);
		addComponents(textComponents, firstLevel, containers);
		Collections.sort(firstLevel);
	}

	private List<? extends Component> group(List<TextComponent> textComponents, List<Component> layoutComponents) {
		List<Component> list = new ArrayList<Component>();
		list.addAll(layoutComponents);
		for (Component component : textComponents)
			if (findContainer(component, layoutComponents) == null)
				list.add(component);
		return list;
	}

	private List<Component> groupConnectedComponents(List<Component> components) {
		Map<Component, Integer> groupMap = new HashMap<Component, Integer>();
		int lastGroupIndex = buildGroupMap(components, groupMap);
		List<Component> groups = new ArrayList<Component>(lastGroupIndex);
		for (int groupIndex = 1; groupIndex <= lastGroupIndex; groupIndex++)
			groups.add(createGroup(groupIndex, groupMap));
		return groups;
	}

	private Component createGroup(int groupIndex, Map<Component, Integer> groupMap) {
		float fromX = Float.POSITIVE_INFINITY;
		float fromY = Float.POSITIVE_INFINITY;
		float toX = Float.NEGATIVE_INFINITY;
		float toY = Float.NEGATIVE_INFINITY;
		for (Component component : groupMap.keySet())
			if (groupMap.get(component) == groupIndex) {
				fromX = Math.min(component.getFromX(), fromX);
				fromY = Math.min(component.getFromY(), fromY);
				toX = Math.max(component.getToX(), toX);
				toY = Math.max(component.getToY(), toY);
			}
		return new GridComponent("group", fromX, fromY, toX, toY, 0);
	}

	private int buildGroupMap(List<Component> components, Map<Component, Integer> groupMap) {
		int lastGroupIndex = 0;
		for (Component component1 : components)
			for (Component component2 : components)
				if (component1.intersects(component2))
					lastGroupIndex = mapToSameGroupIndex(component1, component2, lastGroupIndex, groupMap);
		return lastGroupIndex;
	}

	private int mapToSameGroupIndex(Component component1, Component component2, int lastGroupIndex, Map<Component, Integer> groupMap) {
		Integer groupIndex1 = groupMap.get(component1);
		Integer groupIndex2 = groupMap.get(component2);
		if (groupIndex1 == null && groupIndex2 == null) {
			lastGroupIndex++;
			groupMap.put(component1, lastGroupIndex);
			groupMap.put(component2, lastGroupIndex);
		} else if (groupIndex1 != null && groupIndex2 == null) {
			groupMap.put(component2, groupIndex1);
		} else if (groupIndex1 == null && groupIndex2 != null) {
			groupMap.put(component1, groupIndex2);
		} else if (groupIndex1 != groupIndex2) {
			joinGroups(groupIndex1, groupIndex2, groupMap);
		}
		return lastGroupIndex;
	}

	private void joinGroups(Integer groupIndex1, Integer groupIndex2, Map<Component, Integer> groupMap) {
		for (Component component : groupMap.keySet())
			if (groupMap.get(component) == groupIndex2)
				groupMap.put(component, groupIndex1);
	}

	private void addComponents(List<? extends Component> components, List<Component> targetList, List<? extends Component> containers) {
		for (Component component : components) {
			Component container = findContainer(component, containers);
			if (container != null)
				container.addChild(component);
			else
				targetList.add(component);
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
