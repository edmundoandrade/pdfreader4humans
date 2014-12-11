// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDPage;

public interface PDFGridLocator {
	List<GridComponent> locateGridComponents(PDPage page) throws IOException;
}
