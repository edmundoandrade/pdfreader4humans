// This open source code is distributed without warranties, following the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edworld.pdfreader.GridComponent;
import edworld.pdfreader.PDFGridReader;

public class PDFGridReaderImplTest {
	private PDDocument doc;
	private PDFGridReader reader;

	@Before
	public void setUp() throws Exception {
		doc = PDDocument.load(getClass().getResource("/testcase1/input.pdf"));
		PDPage page1 = (PDPage) doc.getDocumentCatalog().getAllPages().get(0);
		reader = new PDFGridReaderImpl(page1);
	}

	@Test
	public void locateGridComponents() throws IOException {
		GridComponent[] components = reader.locateGridComponents();
		assertEquals(173, components.length);
		assertEquals("rect (50.406, 69.08, 758.273, 34.147)", components[0].toString());
		assertEquals("rect (49.518, 75.116, 49.717, 83.987)", components[1].toString());
		assertEquals("rect (101.254, 75.116, 101.453, 83.987)", components[2].toString());
		assertEquals("rect (273.328, 75.116, 273.527, 83.987)", components[3].toString());
		assertEquals("line (49.607, 218.596, 758.265, 218.596)", components[46].toString());
		assertEquals("line (49.607, 585.267, 517.321, 585.267)", components[47].toString());
		assertEquals("line (49.607, 873.311, 758.273, 873.311)", components[172].toString());
	}

	@After
	public void tearDown() throws IOException {
		doc.close();
	}
}
