// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edworld.pdfreader.Component;
import edworld.pdfreader.PDFGridLocator;

public class PDFGridLocatorImplTest {
	private PDDocument doc;
	private PDFGridLocator locator;

	@Before
	public void setUp() throws Exception {
		doc = PDDocument.load(getClass().getResource("/testcase1/input.pdf"));
		locator = new PDFGridLocatorImpl();
	}

	@Test
	public void locateGridComponents() throws IOException {
		PDPage page1 = (PDPage) doc.getDocumentCatalog().getAllPages().get(0);
		Component[] components = locator.locateGridComponents(page1);
		assertEquals(173, components.length);
		assertEquals("rect (50.406, 34.147, 758.273, 69.08, 0.0pt)", components[0].toString());
		assertEquals("rect (49.518, 75.116, 49.717, 83.987, 0.51pt)", components[1].toString());
		assertEquals("rect (101.254, 75.116, 101.453, 83.987, 0.51pt)", components[2].toString());
		assertEquals("rect (273.328, 75.116, 273.527, 83.987, 0.51pt)", components[3].toString());
		assertEquals("line (49.607, 218.596, 758.265, 218.596, 1.08pt)", components[46].toString());
		assertEquals("line (49.607, 585.267, 517.321, 585.267, 1.08pt)", components[47].toString());
		assertEquals("line (49.607, 873.311, 758.273, 873.311, 0.51pt)", components[172].toString());
	}

	@After
	public void tearDown() throws IOException {
		doc.close();
	}
}
