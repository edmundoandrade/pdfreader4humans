// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

public class PDFPage {
	private int index;
	private PDPage page;
	private PDDocument doc;

	public PDFPage(int index, PDPage page, PDDocument doc) {
		this.index = index;
		this.page = page;
		this.doc = doc;
	}

	public int getIndex() {
		return index;
	}

	public PDPage getPage() {
		return page;
	}

	public PDDocument getDoc() {
		return doc;
	}
}
