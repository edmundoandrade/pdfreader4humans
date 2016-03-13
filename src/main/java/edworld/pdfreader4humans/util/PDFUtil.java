// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.pdfbox.pdmodel.PDDocument;

public class PDFUtil {
	public static PDDocument load(URL url) throws IOException {
		InputStream input = url.openStream();
		try {
			return PDDocument.load(input);
		} finally {
			input.close();
		}
	}
}
