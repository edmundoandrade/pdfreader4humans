// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

public class PDFReader {
	private static final String UTF_8 = "UTF-8";
	private URL url;
	private List<List<Component>> firstLevel = new ArrayList<List<Component>>();
	private Map<String, String> templateMap = new HashMap<String, String>();

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
		this.url = url;
		PDDocument doc = PDDocument.load(url);
		try {
			readAllPages(doc, componentLocator, boxDetector, marginDetector);
		} finally {
			doc.close();
		}
	}

	public List<Component> getFirstLevelComponents(int pageNumber) {
		return firstLevel.get(pageNumber - 1);
	}

	public String toXML() {
		String output = template("pdfreader4humans.xml");
		String content = "";
		for (int pageIndex = 0; pageIndex < firstLevel.size(); pageIndex++)
			content += pageToXML(pageIndex + 1, firstLevel.get(pageIndex));
		return output.replaceAll("\\$\\{content\\}", Matcher.quoteReplacement(content));
	}

	private String pageToXML(int pageNumber, List<Component> pageFirstLevelComponents) {
		String pageTemplate = template("pdfreader4humans-page.xml");
		String content = "";
		for (Component component : pageFirstLevelComponents)
			content += output(component);
		return pageTemplate.replaceAll("\\$\\{pageNumber\\}", String.valueOf(pageNumber)).replaceAll("\\$\\{content\\}", Matcher.quoteReplacement(content));
	}

	private String output(Component component) {
		String content = "";
		for (Component child : component.getChildren())
			content += child.output(template("pdfreader4humans-" + child.getType() + ".xml"));
		return component.output(template("pdfreader4humans-" + component.getType() + ".xml")).replaceAll("\\$\\{content\\}", Matcher.quoteReplacement(content));
	}

	public RenderedImage createPageImage(int pageNumber, int scaling, Color inkColor, Color backgroundColor, boolean showStructure) throws IOException {
		Map<String, Font> fonts = new HashMap<String, Font>();
		PDRectangle cropBox = getPageCropBox(pageNumber);
		BufferedImage image = new BufferedImage(Math.round(cropBox.getWidth() * scaling), Math.round(cropBox.getHeight() * scaling), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(inkColor);
		graphics.setBackground(backgroundColor);
		graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
		graphics.scale(scaling, scaling);
		for (Component component : getFirstLevelComponents(pageNumber))
			draw(component, graphics, inkColor, backgroundColor, showStructure, fonts);
		graphics.dispose();
		return image;
	}

	private void readAllPages(PDDocument doc, PDFComponentLocator componentLocator, BoxDetector boxDetector, MarginDetector marginDetector) throws IOException {
		for (Object page : doc.getDocumentCatalog().getAllPages())
			firstLevel.add(readPage((PDPage) page, componentLocator, boxDetector, marginDetector));
	}

	private List<Component> readPage(PDPage page, PDFComponentLocator componentLocator, BoxDetector boxDetector, MarginDetector marginDetector) throws IOException {
		List<Component> firstLevelComponents = new ArrayList<Component>();
		List<GridComponent> gridComponents = componentLocator.locateGridComponents(page);
		List<TextComponent> textComponents = componentLocator.locateTextComponents(page);
		List<BoxComponent> boxes = boxDetector.detectBoxes(gridComponents);
		List<Component> containers = new ArrayList<Component>();
		containers.addAll(gridComponents);
		containers.addAll(boxes);
		List<Component> groups = groupConnectedComponents(containers);
		containers.addAll(groups);
		firstLevelComponents.addAll(groups);
		addComponents(boxes, firstLevelComponents, groups);
		addComponents(gridComponents, firstLevelComponents, containers);
		List<MarginComponent> margins = marginDetector.detectMargins(group(textComponents, firstLevelComponents));
		containers.addAll(margins);
		firstLevelComponents.addAll(margins);
		addComponents(textComponents, firstLevelComponents, containers);
		Collections.sort(firstLevelComponents);
		return firstLevelComponents;
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
		return new GroupComponent(fromX, fromY, toX, toY);
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

	private String template(String templateFileName) {
		String template = templateMap.get(templateFileName);
		if (template != null)
			return template;
		InputStream input = getClass().getResourceAsStream("/templates/" + templateFileName);
		try {
			try {
				template = IOUtils.toString(input, UTF_8);
				templateMap.put(templateFileName, template);
				return template;
			} finally {
				input.close();
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private void draw(Component component, Graphics2D graphics, Color inkColor, Color backgroundColor, boolean showStructure, Map<String, Font> fonts) {
		for (Component child : component.getChildren())
			draw(child, graphics, inkColor, backgroundColor, showStructure, fonts);
		if (component instanceof BoxComponent && showStructure) {
			graphics.setColor(boxColor(backgroundColor));
			graphics.drawRect((int) component.getFromX(), (int) component.getFromY(), (int) component.getWidth(), (int) component.getHeight());
			graphics.setColor(inkColor);
		} else if (component instanceof GroupComponent && showStructure) {
			graphics.setColor(groupColor(backgroundColor));
			graphics.drawRect(Math.round(component.getFromX()), Math.round(component.getFromY()), Math.round(component.getWidth()), Math.round(component.getHeight()));
			graphics.setColor(inkColor);
		} else if (component instanceof MarginComponent && showStructure) {
			graphics.setColor(marginColor(backgroundColor));
			graphics.drawRect(Math.round(component.getFromX()), Math.round(component.getFromY()), Math.round(component.getWidth()), Math.round(component.getHeight()));
			graphics.setColor(inkColor);
		} else if (component.getType().equals("line"))
			graphics.drawLine(Math.round(component.getFromX()), Math.round(component.getFromY()), Math.round(component.getToX()), Math.round(component.getToY()));
		else if (component.getType().equals("rect"))
			graphics.drawRect(Math.round(component.getFromX()), Math.round(component.getFromY()), Math.round(component.getWidth()), Math.round(component.getHeight()));
		else if (component instanceof TextComponent) {
			graphics.setFont(font((TextComponent) component, fonts));
			graphics.drawString(((TextComponent) component).getText(), component.getFromX(), component.getToY());
		}
	}

	private Color boxColor(Color backgroundColor) {
		return new Color(Color.GRAY.getRGB() ^ backgroundColor.getRGB());
	}

	private Color groupColor(Color inkColor) {
		return new Color(Color.CYAN.getRGB() ^ inkColor.getRGB());
	}

	private Color marginColor(Color inkColor) {
		return new Color(Color.YELLOW.getRGB() ^ inkColor.getRGB());
	}

	private Font font(TextComponent component, Map<String, Font> fonts) {
		String key = component.getFontName() + ":" + component.getFontSize();
		Font font = fonts.get(key);
		if (font == null) {
			String name = component.getFontName().contains("Times") ? "TimesRoman" : "Dialog";
			int style = component.getFontName().contains("Bold") ? Font.BOLD : (component.getFontName().contains("Italic") ? Font.ITALIC : Font.PLAIN);
			font = new Font(name, style, (int) component.getFontSize());
			fonts.put(key, font);
		}
		return font;
	}

	private PDRectangle getPageCropBox(int pageIndex) throws IOException {
		PDDocument doc = PDDocument.load(url);
		try {
			return ((PDPage) doc.getDocumentCatalog().getAllPages().get(pageIndex - 1)).findCropBox();
		} finally {
			doc.close();
		}
	}
}
