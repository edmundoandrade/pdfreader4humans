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

import edworld.pdfreader4humans.PDFTextLocator;
import edworld.pdfreader4humans.TextComponent;

public class MainPDFTextLocatorTest {
	private PDDocument doc;
	private PDFTextLocator locator;

	@Before
	public void setUp() throws Exception {
		doc = PDDocument.load(getClass().getResource("/testcase1/input.pdf"));
		locator = new MainPDFTextLocator(new MainPDFGridLocator());
	}

	@Test
	public void locateTextComponents() throws IOException {
		PDPage page1 = (PDPage) doc.getDocumentCatalog().getAllPages().get(0);
		List<TextComponent> components = locator.locateTextComponents(page1);
		assertEquals(392, components.size());
		assertEquals("Nº 31, quinta-feira, 13 de fevereiro de 2014 (49.348, 56.089787, 217.9233, 62.794617, Times-Roman 9.0)", components.get(0).toString());
		assertEquals("1 (506.4658, 56.486153, 513.0536, 62.764587, HNBDHM+OxfordWd 11.0)", components.get(1).toString());
		assertEquals("ISSN 1677-7042 (575.9183, 56.603043, 642.8473, 63.208984, Times-Italic 9.0)", components.get(2).toString());
		assertEquals("3 (705.2703, 53.990482, 711.2479, 62.155884, Times-Italic 11.0)", components.get(3).toString());
		assertEquals("Outros instrumentos e aparelhos (102.3502, 77.49142, 208.51189, 82.99078, Times-Roman 7.0)", components.get(4).toString());
		assertEquals("15% (294.9229, 77.603, 309.5321, 82.99078, Times-Roman 7.0)", components.get(5).toString());
		assertEquals("10% (353.0895, 77.603, 367.6987, 82.99078, Times-Roman 7.0)", components.get(6).toString());
		assertEquals("90.30 (434.5236, 77.42765, 452.4563, 82.99078, Times-Roman 7.0)", components.get(7).toString());
		assertEquals("Osciloscópios, analisadores de espectro e outros (470.85208, 77.49142, 629.8473, 82.99078, Times-Roman 7.0)", components.get(8).toString());
		assertEquals("15% (663.42487, 77.603, 678.03406, 82.99078, Times-Roman 7.0)", components.get(9).toString());
		assertEquals("10% (721.59143, 77.603, 736.2006, 82.99078, Times-Roman 7.0)", components.get(10).toString());
		assertEquals("DECRETO Nº (64.63681, 234.46262, 115.43865, 239.8504, Times-Bold 7.0)", components.get(49).toString());
		assertEquals("a) Gabinete; (318.84198, 316.3835, 359.09103, 320.12946, Times-Roman 7.0)", components.get(98).toString());
		assertEquals("tados e dos demais atos normativos a ser uniformemente seguida (304.69586, 435.96918, 517.3223, 440.66357, Times-Roman 7.0)", components.get(206).toString());
		assertEquals("Nº (414.0244, 764.2241, 422.40897, 769.7554, Times-Bold 7.0)", components.get(351).toString());
		assertEquals("Documento assinado digitalmente conforme MP nº (463.2183, 879.116, 631.8032, 884.3922, Times-Roman 7.0)", components.get(388).toString());
	}

	@After
	public void tearDown() throws IOException {
		doc.close();
	}
}