// This open source code is distributed without warranties, following the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader;

import java.io.IOException;

public interface PDFGridReader {
	GridComponent[] locateGridComponents() throws IOException;
}
