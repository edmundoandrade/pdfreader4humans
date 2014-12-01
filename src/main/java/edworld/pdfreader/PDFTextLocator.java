// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDPage;

public interface PDFTextLocator {
	TextComponent[] locateTextComponents(PDPage page) throws IOException;
}