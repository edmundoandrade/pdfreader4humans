// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans.impl;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

import edworld.pdfreader4humans.Component;
import edworld.pdfreader4humans.GridComponent;
import edworld.pdfreader4humans.PDFComponentLocator;
import edworld.pdfreader4humans.TextComponent;

public class MainPDFComponentLocator implements PDFComponentLocator {
	private Map<PDPage, List<GridComponent>> cachedGridComponents = new HashMap<PDPage, List<GridComponent>>();
	private Map<PDPage, List<TextComponent>> cachedTextComponents = new HashMap<PDPage, List<TextComponent>>();

	public List<GridComponent> locateGridComponents(PDPage page) throws IOException {
		List<GridComponent> gridComponents = cachedGridComponents.get(page);
		if (gridComponents != null)
			return gridComponents;
		locateComponents(page);
		return cachedGridComponents.get(page);
	}

	public List<TextComponent> locateTextComponents(PDPage page) throws IOException {
		List<TextComponent> textComponents = cachedTextComponents.get(page);
		if (textComponents != null)
			return textComponents;
		locateComponents(page);
		return cachedTextComponents.get(page);
	}

	private void locateComponents(PDPage page) throws IOException {
		List<GridComponent> gridComponents = locateAllGridComponents(page);
		List<TextComponent> textComponents = locateAllTextComponents(page, gridComponents);
		cachedGridComponents.put(page, gridComponents);
		cachedTextComponents.put(page, textComponents);
	}

	protected List<GridComponent> locateAllGridComponents(PDPage page) throws IOException {
		final PDPage pageToDraw = page;
		return new PageDrawer() {
			private List<GridComponent> list = new ArrayList<GridComponent>();

			public List<GridComponent> locateGridComponents() throws IOException {
				PDRectangle cropBox = pageToDraw.findCropBox();
				BufferedImage image = new BufferedImage(Math.round(cropBox.getWidth()), Math.round(cropBox.getHeight()), BufferedImage.TYPE_INT_ARGB);
				Graphics2D graphics = image.createGraphics();
				drawPage(graphics, pageToDraw, cropBox.createDimension());
				graphics.dispose();
				dispose();
				Collections.sort(list);
				return list;
			}

			@Override
			protected void processOperator(PDFOperator operator, List<COSBase> arguments) throws IOException {
				processGridOPeration(operator, arguments);
			}

			private void processGridOPeration(PDFOperator operator, List<COSBase> arguments) throws IOException {
				if (isTextOperation(operator.getOperation()))
					return;
				if (operator.getOperation().equals("i")) {
					processSetFlatnessTolerance((COSNumber) arguments.get(0));
					return;
				}
				if (operator.getOperation().equals("l"))
					processLineTo((COSNumber) arguments.get(0), (COSNumber) arguments.get(1));
				else if (operator.getOperation().equals("re"))
					processAppendRectangleToPath((COSNumber) arguments.get(0), (COSNumber) arguments.get(1), (COSNumber) arguments.get(2), (COSNumber) arguments.get(3));
				super.processOperator(operator, arguments);
			}

			private void processSetFlatnessTolerance(COSNumber flatnessTolerance) {
				getGraphicsState().setFlatness(flatnessTolerance.doubleValue());
			}

			private void processLineTo(COSNumber x, COSNumber y) {
				Point2D from = getLinePath().getCurrentPoint();
				Point2D to = transformedPoint(x.doubleValue(), y.doubleValue());
				addGridComponent("line", from, to);
			}

			private void processAppendRectangleToPath(COSNumber x, COSNumber y, COSNumber w, COSNumber h) {
				Point2D from = transformedPoint(x.doubleValue(), y.doubleValue());
				Point2D to = transformedPoint(w.doubleValue() + x.doubleValue(), h.doubleValue() + y.doubleValue());
				addGridComponent("rect", from, to);
			}

			private void addGridComponent(String type, Point2D from, Point2D to) {
				float fromX = (float) Math.min(from.getX(), to.getX());
				float fromY = (float) Math.min(from.getY(), to.getY());
				float toX = (float) Math.max(from.getX(), to.getX());
				float toY = (float) Math.max(from.getY(), to.getY());
				list.add(new GridComponent(type, fromX, fromY, toX, toY, getGraphicsState().getLineWidth()));
			}

			private boolean isTextOperation(String operation) {
				return "/BT/ET/T*/Tc/Td/TD/Tf/Tj/TJ/TL/Tm/Tr/Ts/Tw/Tz/'/\"/".contains("/" + operation + "/");
			}
		}.locateGridComponents();
	}

	protected List<TextComponent> locateAllTextComponents(final PDPage page, final List<GridComponent> gridComponents) throws IOException {
		return new PDFTextStripper() {
			private Map<String, String> fusions;
			List<Component> horizontalComponents;
			private ArrayList<TextComponent> list;
			{
				fusions = new HashMap<String, String>();
				fusions.put("o-", "º");
				fusions.put("a-", "ª");
			}

			public List<TextComponent> locateTextComponents() throws IOException {
				horizontalComponents = Component.horizontal(gridComponents);
				list = new ArrayList<TextComponent>();
				PDStream contents = page.getContents();
				setStartPage(getCurrentPageNo());
				setEndPage(getCurrentPageNo());
				setSortByPosition(false);
				if (contents != null) {
					output = new StringWriter();
					processPage(page, contents.getStream());
				}
				Collections.sort(list);
				return list;
			}

			@Override
			protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
				float fromX = Float.POSITIVE_INFINITY;
				float fromY = Float.POSITIVE_INFINITY;
				float toX = Float.NEGATIVE_INFINITY;
				float toY = Float.NEGATIVE_INFINITY;
				float fontSize = -1;
				String fontName = "";
				List<TextPosition> partialList = new ArrayList<TextPosition>();
				String partialText = "";
				float lastLeft = Float.NEGATIVE_INFINITY;
				float lastRight = Float.NEGATIVE_INFINITY;
				for (TextPosition textPosition : textPositions) {
					String character = textPosition.getCharacter();
					Component overlappingShape = findOverlappingHorizontalShape(textPosition);
					if (overlappingShape != null && fusible(character, "-")) {
						character = fusion(character, "-");
						removeOverlappingShape(overlappingShape);
					}
					float x1 = textPosition.getX();
					float y1 = textPosition.getY();
					if (x1 < lastLeft) {
						list.add(new TextComponent(partialText, fromX, fromY, toX, toY, fontName, fontSize));
						writeString(text.substring(partialText.length()), textPositions.subList(partialList.size(), textPositions.size()));
						return;
					} else if (x1 < lastRight && fusible(partialText, character)) {
						partialText = fusion(partialText, character);
					} else {
						if (x1 < fromX) {
							fromX = x1;
							fromY = y1 - textPosition.getHeight();
							fontName = textPosition.getFont().getBaseFont();
							fontSize = textPosition.getFontSizeInPt();
						}
						partialList.add(textPosition);
						partialText += character;
					}
					toX = Math.max(x1 + textPosition.getWidth(), toX);
					toY = Math.max(y1, toY);
					lastLeft = x1;
					lastRight = x1 + textPosition.getWidth();
				}
				list.add(new TextComponent(partialText, fromX, fromY, toX, toY, fontName, fontSize));
			}

			private Component findOverlappingHorizontalShape(TextPosition textPosition) {
				GridComponent component = new GridComponent("rect", textPosition.getX(), textPosition.getY() - textPosition.getHeight(), textPosition.getX()
						+ textPosition.getWidth(), textPosition.getY(), 1);
				for (Component candidate : horizontalComponents)
					if (candidate.intersects(component) && Math.abs(candidate.getWidth() - component.getWidth()) < 0.1)
						return candidate;
				return null;
			}

			private void removeOverlappingShape(Component overlappingShape) throws IOException {
				gridComponents.remove(overlappingShape);
			}

			private boolean fusible(String partialText, String character) {
				return fusions.containsKey(fusionPair(partialText, character));
			}

			private String fusion(String partialText, String character) {
				return partialText.substring(0, partialText.length() - 1) + fusions.get(fusionPair(partialText, character));
			}

			private String fusionPair(String partialText, String character) {
				return partialText.substring(partialText.length() - 1) + character.charAt(0);
			}
		}.locateTextComponents();
	}
}
