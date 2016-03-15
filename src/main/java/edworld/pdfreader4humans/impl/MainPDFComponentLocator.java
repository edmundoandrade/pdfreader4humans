// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans.impl;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.rendering.PageDrawerParameters;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import edworld.pdfreader4humans.Component;
import edworld.pdfreader4humans.GridComponent;
import edworld.pdfreader4humans.PDFComponentLocator;
import edworld.pdfreader4humans.PDFPage;
import edworld.pdfreader4humans.TextComponent;

public class MainPDFComponentLocator implements PDFComponentLocator {
	private static final String SPACE = " ";
	private Map<PDFPage, List<GridComponent>> cachedGridComponents = new HashMap<PDFPage, List<GridComponent>>();
	private Map<PDFPage, List<TextComponent>> cachedTextComponents = new HashMap<PDFPage, List<TextComponent>>();

	public List<GridComponent> locateGridComponents(PDFPage page) throws IOException {
		List<GridComponent> gridComponents = cachedGridComponents.get(page);
		if (gridComponents != null)
			return gridComponents;
		locateComponents(page);
		return cachedGridComponents.get(page);
	}

	public List<TextComponent> locateTextComponents(PDFPage page) throws IOException {
		List<TextComponent> textComponents = cachedTextComponents.get(page);
		if (textComponents != null)
			return textComponents;
		locateComponents(page);
		return cachedTextComponents.get(page);
	}

	private void locateComponents(PDFPage page) throws IOException {
		List<GridComponent> gridComponents = locateAllGridComponents(page);
		List<TextComponent> textComponents = locateAllTextComponents(page, gridComponents);
		cachedGridComponents.put(page, gridComponents);
		Collections.sort(textComponents);
		cachedTextComponents.put(page, textComponents);
	}

	protected List<GridComponent> locateAllGridComponents(PDFPage page) throws IOException {
		final List<GridComponent> list = new ArrayList<GridComponent>();
		PDFRenderer renderer = new PDFRenderer(page.getDoc()) {
			protected PageDrawer createPageDrawer(PageDrawerParameters parameters) throws IOException {
				return new PageDrawer(parameters) {
					@Override
					public void processOperator(Operator operator, List<COSBase> arguments) throws IOException {
						String operation = operator.getName();
						if (isTextOperation(operation))
							return;
						if (operation.equals("i")) {
							processSetFlatnessTolerance((COSNumber) arguments.get(0));
							return;
						}
						if (operation.equals("l"))
							processLineTo((COSNumber) arguments.get(0), (COSNumber) arguments.get(1));
						else if (operation.equals("re"))
							processAppendRectangleToPath((COSNumber) arguments.get(0), (COSNumber) arguments.get(1),
									(COSNumber) arguments.get(2), (COSNumber) arguments.get(3));
						super.processOperator(operator, arguments);
					}

					private void processSetFlatnessTolerance(COSNumber flatnessTolerance) {
						getGraphicsState().setFlatness(flatnessTolerance.doubleValue());
					}

					private void processLineTo(COSNumber x, COSNumber y) {
						Point2D from = getLinePath().getCurrentPoint();
						Point2D to = transformedPoint(x.floatValue(), y.floatValue());
						if (to.getX() == from.getX() || to.getY() == from.getY())
							addGridComponent("line", from, to);
					}

					private void processAppendRectangleToPath(COSNumber x, COSNumber y, COSNumber w, COSNumber h) {
						Point2D from = transformedPoint(x.floatValue(), y.floatValue());
						Point2D to = transformedPoint(w.floatValue() + x.floatValue(), h.floatValue() + y.floatValue());
						addGridComponent("rect", from, to);
					}

					private void addGridComponent(String type, Point2D from, Point2D to) {
						float fromX = (float) min(from.getX(), to.getX());
						float fromY = (float) min(adjustY(from.getY()), adjustY(to.getY()));
						float toX = (float) max(from.getX(), to.getX());
						float toY = (float) max(adjustY(from.getY()), adjustY(to.getY()));
						list.add(new GridComponent(type, fromX, fromY, toX, toY, getGraphicsState().getLineWidth()));
					}

					private double adjustY(double y) {
						return getPage().getBBox().getHeight() - y;
					}

					private boolean isTextOperation(String operation) {
						return "/BT/ET/T*/Tc/Td/TD/Tf/Tj/TJ/TL/Tm/Tr/Ts/Tw/Tz/'/\"/".contains("/" + operation + "/");
					}
				};
			}
		};
		renderer.renderImage(page.getIndex());
		return list;
	}

	protected List<TextComponent> locateAllTextComponents(PDFPage page, final List<GridComponent> gridComponents)
			throws IOException {
		final PDPage thePage = page.getPage();
		return new PDFTextStripper() {
			private Map<String, String> fusions;
			List<Component> horizontalComponents;
			List<Component> verticalComponents;
			private ArrayList<TextComponent> list;

			{
				fusions = new HashMap<String, String>();
				fusions.put("o-", "º");
				fusions.put("a-", "ª");
			}

			public List<TextComponent> locateTextComponents() throws IOException {
				horizontalComponents = Component.horizontal(gridComponents);
				verticalComponents = Component.vertical(gridComponents);
				list = new ArrayList<TextComponent>();
				setStartPage(getCurrentPageNo());
				setEndPage(getCurrentPageNo());
				setSortByPosition(false);
				if (thePage.hasContents()) {
					output = new StringWriter();
					processPage(thePage);
				}
				joinConsecutiveTexts(list);
				return list;
			}

			protected void joinConsecutiveTexts(ArrayList<TextComponent> textComponents) {
				for (int i = 0; i < textComponents.size() - 1; i++) {
					TextComponent currentComponent = textComponents.get(i);
					TextComponent nextComponent = textComponents.get(i + 1);
					if (currentComponent.consecutive(nextComponent, false)) {
						boolean mustSeparate = false;
						TextComponent newComponent = joinTextComponents(currentComponent, SPACE, nextComponent);
						for (Component separator : verticalComponents)
							if (separator.intersects(newComponent) && !separator.contains(newComponent)) {
								mustSeparate = true;
								break;
							}
						if (!mustSeparate) {
							textComponents.set(i, newComponent);
							textComponents.remove(i + 1);
							i--;
						}
					}
				}
			}

			protected TextComponent joinTextComponents(TextComponent component1, String separatorCharacter,
					TextComponent component2) {
				return new TextComponent(component1.getText() + separatorCharacter + component2.getText(),
						component1.getFromX(), min(component1.getFromY(), component2.getFromY()), component2.getToX(),
						max(component1.getToY(), component2.getToY()), component1.getFontName(),
						component1.getFontSize());
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
					String character = textPosition.getUnicode();
					Component overlappingShape = findOverlappingHorizontalShape(textPosition);
					if (overlappingShape != null && (character.endsWith(SPACE) || fusible(character, "-"))) {
						character = fusion(character, "-");
						removeOverlappingShape(overlappingShape);
					}
					float x1 = textPosition.getX();
					float y1 = textPosition.getY();
					if (x1 < lastLeft) {
						list.add(new TextComponent(partialText, fromX, fromY, toX, toY, fontName, fontSize));
						writeString(text.substring(partialText.length()),
								textPositions.subList(partialList.size(), textPositions.size()));
						return;
					} else if (x1 < lastRight && fusible(partialText, character) && fontsz(textPosition) < fontSize)
						partialText = fusion(partialText, character);
					else if (x1 + textPosition.getWidth() * 0.2 < lastRight && partialText.endsWith(SPACE))
						partialText = fusion(partialText, character);
					else {
						if (x1 < fromX) {
							fromX = x1;
							fromY = y1 - textPosition.getHeight();
							fontName = textPosition.getFont().getName();
							fontSize = fontsz(textPosition);
						}
						partialList.add(textPosition);
						partialText += character;
					}
					toX = max(x1 + textPosition.getWidth(), toX);
					toY = max(y1, toY);
					lastLeft = x1;
					lastRight = x1 + textPosition.getWidth();
				}
				list.add(new TextComponent(partialText, fromX, fromY, toX, toY, fontName, fontSize));
			}

			private float fontsz(TextPosition textPosition) {
				return textPosition.getFontSize() == 1F ? textPosition.getFontSizeInPt()
						: (int) textPosition.getFontSize();
			}

			private Component findOverlappingHorizontalShape(TextPosition textPosition) {
				GridComponent component = new GridComponent("rect", textPosition.getX(),
						textPosition.getY() - textPosition.getHeight(), textPosition.getX() + textPosition.getWidth(),
						textPosition.getY(), 1);
				for (Component candidate : horizontalComponents)
					if (candidate.underlineOf(component) && abs(candidate.getWidth() - component.getWidth()) < 0.1)
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
				if (character.equals(SPACE))
					return partialText;
				if (partialText.endsWith(SPACE))
					return partialText.substring(0, partialText.length() - 1) + character;
				return partialText.substring(0, partialText.length() - 1)
						+ fusions.get(fusionPair(partialText, character));
			}

			private String fusionPair(String partialText, String character) {
				return partialText.substring(partialText.length() - 1) + character.charAt(0);
			}
		}.locateTextComponents();
	}
}
