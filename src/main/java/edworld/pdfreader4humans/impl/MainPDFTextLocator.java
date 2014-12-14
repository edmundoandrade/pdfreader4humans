// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

import edworld.pdfreader4humans.Component;
import edworld.pdfreader4humans.GridComponent;
import edworld.pdfreader4humans.PDFGridLocator;
import edworld.pdfreader4humans.PDFTextLocator;
import edworld.pdfreader4humans.TextComponent;

public class MainPDFTextLocator implements PDFTextLocator {
	private PDFGridLocator gridLocator;

	public MainPDFTextLocator(PDFGridLocator gridLocator) {
		this.gridLocator = gridLocator;
	}

	public List<TextComponent> locateTextComponents(final PDPage page) throws IOException {
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
				horizontalComponents = Component.horizontal(gridLocator.locateGridComponents(page));
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
					if (findOverlappingHorizontalShape(textPosition) != null && fusible(character, "-"))
						character = fusion(character, "-");
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
