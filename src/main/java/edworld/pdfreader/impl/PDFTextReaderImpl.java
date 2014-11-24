// This open source code is distributed without warranties, following the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

import edworld.pdfreader.PDFTextReader;
import edworld.pdfreader.TextComponent;

public class PDFTextReaderImpl implements PDFTextReader {
	private PDPage page;

	public PDFTextReaderImpl(PDPage page) {
		this.page = page;
	}

	public TextComponent[] locateTextComponents() throws IOException {
		return new PDFTextStripper() {
			private ArrayList<TextComponent> list = new ArrayList<TextComponent>();

			public TextComponent[] locateTextComponents() throws IOException {
				PDStream contents = page.getContents();
				setStartPage(getCurrentPageNo());
				setEndPage(getCurrentPageNo());
				setSortByPosition(true);
				if (contents != null) {
					output = new StringWriter();
					processPage(page, contents.getStream());
				}
				return list.toArray(new TextComponent[0]);
			}

			@Override
			protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
				float x = Float.MAX_VALUE, y = -1, fontSize = -1;
				String fontName = "";
				for (TextPosition textPosition : textPositions) {
					if (textPosition.getX() <= x) {
						x = textPosition.getX();
						y = textPosition.getY();
						fontName = textPosition.getFont().getBaseFont();
						fontSize = textPosition.getFontSizeInPt();
					}
				}
				list.add(new TextComponent(x, y, text, fontName, fontSize));
			}
		}.locateTextComponents();
	}
}
