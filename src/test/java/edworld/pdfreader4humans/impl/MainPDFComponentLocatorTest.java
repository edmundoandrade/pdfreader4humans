// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edworld.pdfreader4humans.GridComponent;
import edworld.pdfreader4humans.PDFComponentLocator;
import edworld.pdfreader4humans.TextComponent;

public class MainPDFComponentLocatorTest {
	private PDDocument doc;
	private PDFComponentLocator locator;
	private PDPage page1;

	@Before
	public void setUp() throws Exception {
		locator = new MainPDFComponentLocator();
		doc = PDDocument.load(getClass().getResource("/testcase1/input.pdf"));
		page1 = (PDPage) doc.getDocumentCatalog().getAllPages().get(0);
	}

	@Test
	public void locateGridComponents() throws IOException {
		List<GridComponent> components = locator.locateGridComponents(page1);
		assertEquals(172, components.size());
		assertEquals("rect :: 50.406, 34.147, 758.273, 69.08, 0.0pt", components.get(0).toString());
		assertEquals("rect :: 49.518, 75.116, 49.717, 83.987, 0.51pt", components.get(1).toString());
		assertEquals("rect :: 101.254, 75.116, 101.453, 83.987, 0.51pt", components.get(2).toString());
		assertEquals("rect :: 273.328, 75.116, 273.527, 83.987, 0.51pt", components.get(3).toString());
		assertEquals("rect :: 49.518, 83.887, 389.762, 84.086, 0.51pt", components.get(6).toString());
		assertEquals("line :: 49.607, 218.596, 758.265, 218.596, 1.08pt", components.get(46).toString());
		assertEquals("line :: 49.607, 585.267, 517.321, 585.267, 1.08pt", components.get(47).toString());
	}

	@Test
	public void locateTextComponents() throws IOException {
		List<TextComponent> components = locator.locateTextComponents(page1);
		assertEquals(233, components.size());
		assertEquals("text :: 49.348, 56.089787, 217.9233, 62.794617, Times-Roman 9.0 :: Nº 31, quinta-feira, 13 de fevereiro de 2014", components.get(0).toString());
		assertEquals("text :: 506.4658, 56.486153, 513.0536, 62.764587, HNBDHM+OxfordWd 11.0 :: 1", components.get(1).toString());
		assertEquals("text :: 575.9183, 56.603043, 642.8473, 63.208984, Times-Italic 9.0 :: ISSN 1677-7042", components.get(2).toString());
		assertEquals("text :: 705.2703, 53.990482, 711.2479, 62.155884, Times-Italic 11.0 :: 3", components.get(3).toString());
		assertEquals("text :: 102.3502, 77.49142, 208.51189, 82.99078, Times-Roman 7.0 :: Outros instrumentos e aparelhos", components.get(4).toString());
		assertEquals("text :: 294.9229, 77.603, 309.5321, 82.99078, Times-Roman 7.0 :: 15%", components.get(5).toString());
		assertEquals("text :: 353.0895, 77.603, 367.6987, 82.99078, Times-Roman 7.0 :: 10%", components.get(6).toString());
		assertEquals("text :: 434.5236, 77.42765, 452.4563, 82.99078, Times-Roman 7.0 :: 90.30", components.get(7).toString());
		assertEquals("text :: 470.85208, 77.49142, 629.8473, 82.99078, Times-Roman 7.0 :: Osciloscópios, analisadores de espectro e outros", components.get(8).toString());
		assertEquals("text :: 418.10913, 161.3345, 490.97974, 166.72229, Times-Bold 7.0 :: PM = PE x (1 + M),", components.get(30).toString());
		assertEquals("text :: 494.04825, 162.97635, 514.8582, 166.72229, Times-Roman 7.0 :: sendo:", components.get(31).toString());
		assertEquals("text :: 64.63681, 234.26459, 261.34717, 239.85162, Times-Bold 7.0 :: DECRETO Nº 8.195, DE 12 DE FEVEREIRO DE 2014", components.get(49).toString());
		assertEquals("text :: 318.84198, 316.3835, 359.09103, 320.12946, Times-Roman 7.0 :: a) Gabinete;", components.get(74).toString());
		assertEquals("text :: 148.9305, 677.602, 176.10057, 682.9898, Times-Bold 7.0 :: TOTAL", components.get(176).toString());
		assertEquals("text :: 414.0244, 764.2241, 422.40897, 769.7554, Times-Bold 7.0 :: Nº", components.get(193).toString());
		assertEquals(
				"text :: 49.4745, 879.54083, 352.7328, 884.817, Times-Roman 7.0 :: Este documento pode ser verificado no endereço eletrônico http://www.in.gov.br/autenticidade.html,",
				components.get(229).toString());
		assertEquals("text :: 463.2183, 879.0047, 757.81055, 884.3925, Times-Roman 7.0 :: Documento assinado digitalmente conforme MP nº 2.200-2 de 24/08/2001, que institui a",
				components.get(230).toString());
	}

	@Test
	public void locateTextComponentsWithOverlappingShape() throws IOException {
		doc.close();
		doc = PDDocument.load(getClass().getResource("/testcase3/input.pdf"));
		page1 = (PDPage) doc.getDocumentCatalog().getAllPages().get(0);
		String result = "";
		for (TextComponent component : locator.locateTextComponents(page1))
			if (component.getText().contains("289, de 28 de julho de 2015"))
				result = component.toString();
		assertEquals("text :: 531.4999, 665.6678, 640.17206, 671.0556, Times-Roman 7.0 :: Nº 289, de 28 de julho de 2015.", result);
	}

	@After
	public void tearDown() throws IOException {
		doc.close();
	}
}
