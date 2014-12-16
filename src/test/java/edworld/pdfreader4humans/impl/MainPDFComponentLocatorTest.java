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
		doc = PDDocument.load(getClass().getResource("/testcase1/input.pdf"));
		locator = new MainPDFComponentLocator();
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
		assertEquals(392, components.size());
		assertEquals("text :: 49.348, 56.089787, 217.9233, 62.794617, Times-Roman 9.0 :: Nº 31, quinta-feira, 13 de fevereiro de 2014", components.get(0).toString());
		assertEquals("text :: 506.4658, 56.486153, 513.0536, 62.764587, HNBDHM+OxfordWd 11.0 :: 1", components.get(1).toString());
		assertEquals("text :: 575.9183, 56.603043, 642.8473, 63.208984, Times-Italic 9.0 :: ISSN 1677-7042", components.get(2).toString());
		assertEquals("text :: 705.2703, 53.990482, 711.2479, 62.155884, Times-Italic 11.0 :: 3", components.get(3).toString());
		assertEquals("text :: 102.3502, 77.49142, 208.51189, 82.99078, Times-Roman 7.0 :: Outros instrumentos e aparelhos", components.get(4).toString());
		assertEquals("text :: 294.9229, 77.603, 309.5321, 82.99078, Times-Roman 7.0 :: 15%", components.get(5).toString());
		assertEquals("text :: 353.0895, 77.603, 367.6987, 82.99078, Times-Roman 7.0 :: 10%", components.get(6).toString());
		assertEquals("text :: 434.5236, 77.42765, 452.4563, 82.99078, Times-Roman 7.0 :: 90.30", components.get(7).toString());
		assertEquals("text :: 470.85208, 77.49142, 629.8473, 82.99078, Times-Roman 7.0 :: Osciloscópios, analisadores de espectro e outros", components.get(8).toString());
		assertEquals("text :: 663.42487, 77.603, 678.03406, 82.99078, Times-Roman 7.0 :: 15%", components.get(9).toString());
		assertEquals("text :: 721.59143, 77.603, 736.2006, 82.99078, Times-Roman 7.0 :: 10%", components.get(10).toString());
		assertEquals("text :: 64.63681, 234.46262, 115.43865, 239.8504, Times-Bold 7.0 :: DECRETO Nº", components.get(49).toString());
		assertEquals("text :: 318.84198, 316.3835, 359.09103, 320.12946, Times-Roman 7.0 :: a) Gabinete;", components.get(98).toString());
		assertEquals("text :: 304.69586, 435.96918, 517.3223, 440.66357, Times-Roman 7.0 :: tados e dos demais atos normativos a ser uniformemente seguida", components.get(206)
				.toString());
		assertEquals("text :: 414.0244, 764.2241, 422.40897, 769.7554, Times-Bold 7.0 :: Nº", components.get(351).toString());
		assertEquals("text :: 463.2183, 879.116, 631.8032, 884.3922, Times-Roman 7.0 :: Documento assinado digitalmente conforme MP nº", components.get(388).toString());
	}

	@After
	public void tearDown() throws IOException {
		doc.close();
	}
}
