// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans.impl;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edworld.pdfreader4humans.GridComponent;
import edworld.pdfreader4humans.PDFComponentLocator;
import edworld.pdfreader4humans.PDFPage;
import edworld.pdfreader4humans.TextComponent;
import edworld.pdfreader4humans.util.PDFUtil;

public class MainPDFComponentLocatorTest {
	private PDDocument doc;
	private PDFComponentLocator locator;
	private PDFPage page1;

	@Before
	public void setUp() throws Exception {
		locator = new MainPDFComponentLocator();
		doc = null;
	}

	@Test
	public void locateGridComponents() throws IOException {
		doc = PDFUtil.load(getClass().getResource("/testcase1/input.pdf"));
		page1 = new PDFPage(0, doc.getPages().get(0), doc);
		List<GridComponent> components = locator.locateGridComponents(page1);
		assertEquals(172, components.size());
		List<String> items = new ArrayList<>();
		for (GridComponent component : components)
			items.add(component.toString());
		assertThat(items, hasItem("rect :: 50.406, 34.147034, 758.273, 69.08002, 1.0pt"));
		assertThat(items, hasItem("rect :: 49.518, 75.11603, 49.717003, 83.987, 0.51pt"));
		assertThat(items, hasItem("rect :: 49.518, 83.887024, 389.762, 84.086, 0.51pt"));
		assertThat(items, hasItem("rect :: 101.254, 75.11603, 101.452995, 83.987, 0.51pt"));
		assertThat(items, hasItem("rect :: 273.328, 75.11603, 273.527, 83.987, 0.51pt"));
		assertThat(items, not(hasItem("line :: 49.607, 321.733, 517.321, 585.26697, 1.08pt")));
	}

	@Test
	public void locateGridComponentsWithMassiveDrawingElements() throws IOException {
		doc = PDFUtil.load(getClass().getResource("/testcase5/input.pdf"));
		page1 = new PDFPage(0, doc.getPages().get(0), doc);
		List<GridComponent> components = locator.locateGridComponents(page1);
		assertEquals(422, components.size());
		List<String> items = new ArrayList<>();
		for (GridComponent component : components)
			items.add(component.toString());
		assertThat(items, hasItem("line :: 49.607, 873.311, 758.273, 873.311, 0.51pt"));
		assertThat(items, hasItem("line :: 713.179, 797.3233, 713.179, 807.12506, 0.257pt"));
	}

	@Test
	public void locateTextComponents() throws IOException {
		doc = PDFUtil.load(getClass().getResource("/testcase1/input.pdf"));
		page1 = new PDFPage(0, doc.getPages().get(0), doc);
		List<TextComponent> components = locator.locateTextComponents(page1);
		assertEquals(233, components.size());
		assertEquals(
				"text :: 49.348, 57.235485, 217.9233, 62.794617, Times-Roman 9.0 :: Nº 31, quinta-feira, 13 de fevereiro de 2014",
				components.get(0).toString());
		assertEquals("text :: 506.4658, 56.486153, 513.0536, 62.764587, HNBDHM+OxfordWd 11.0 :: 1",
				components.get(1).toString());
		assertEquals("text :: 575.9183, 57.645023, 642.8473, 63.208984, Times-Italic 9.0 :: ISSN 1677-7042",
				components.get(2).toString());
		assertEquals("text :: 705.2703, 55.580524, 711.2479, 62.155884, Times-Italic 11.0 :: 3",
				components.get(3).toString());
		assertEquals(
				"text :: 102.3502, 78.543465, 208.51189, 82.99078, Times-Roman 7.0 :: Outros instrumentos e aparelhos",
				components.get(4).toString());
		assertEquals("text :: 294.9229, 78.543465, 309.5321, 82.99078, Times-Roman 7.0 :: 15%",
				components.get(5).toString());
		assertEquals("text :: 353.0895, 78.543465, 367.6987, 82.99078, Times-Roman 7.0 :: 10%",
				components.get(6).toString());
		assertEquals("text :: 434.5236, 78.543465, 452.4563, 82.99078, Times-Roman 7.0 :: 90.30",
				components.get(7).toString());
		assertEquals(
				"text :: 470.85208, 78.543465, 629.8473, 82.99078, Times-Roman 7.0 :: Osciloscópios, analisadores de espectro e outros",
				components.get(8).toString());
		assertEquals("text :: 418.10913, 162.12753, 490.97974, 166.72229, Times-Bold 7.0 :: PM = PE x (1 + M),",
				components.get(30).toString());
		assertEquals("text :: 494.04825, 162.27498, 514.8582, 166.72229, Times-Roman 7.0 :: sendo:",
				components.get(31).toString());
		assertEquals(
				"text :: 64.63681, 235.25565, 261.34717, 239.85162, Times-Bold 7.0 :: DECRETO Nº 8.195, DE 12 DE FEVEREIRO DE 2014",
				components.get(49).toString());
		assertEquals("text :: 318.84198, 315.68213, 359.09103, 320.12946, Times-Roman 7.0 :: a) Gabinete;",
				components.get(73).toString());
		assertEquals("text :: 148.9305, 678.395, 176.10057, 682.9898, Times-Bold 7.0 :: TOTAL",
				components.get(176).toString());
		assertEquals("text :: 414.0244, 765.1606, 422.40897, 769.7554, Times-Bold 7.0 :: Nº",
				components.get(193).toString());
		assertEquals(
				"text :: 49.4745, 880.3697, 352.7328, 884.817, Times-Roman 7.0 :: Este documento pode ser verificado no endereço eletrônico http://www.in.gov.br/autenticidade.html,",
				components.get(229).toString());
		assertEquals(
				"text :: 463.2183, 879.9449, 757.81055, 884.3925, Times-Roman 7.0 :: Documento assinado digitalmente conforme MP nº 2.200-2 de 24/08/2001, que institui a",
				components.get(230).toString());
	}

	@Test
	public void locateTextComponentsWithOverlappingShape() throws IOException {
		doc = PDFUtil.load(getClass().getResource("/testcase3/input.pdf"));
		page1 = new PDFPage(0, doc.getPages().get(0), doc);
		String result = "";
		for (TextComponent component : locator.locateTextComponents(page1))
			if (component.getText().contains("289, de 28 de julho de 2015"))
				result = component.toString();
		assertEquals(
				"text :: 531.4999, 666.6083, 640.17206, 671.0556, Times-Roman 7.0 :: Nº 289, de 28 de julho de 2015.",
				result);
	}

	@Test
	public void locateTextComponentsWithCorrectFontSize() throws IOException {
		doc = PDFUtil.load(getClass().getResource("/testcase6/input.pdf"));
		page1 = new PDFPage(0, doc.getPages().get(0), doc);
		String result = "";
		for (TextComponent component : locator.locateTextComponents(page1))
			if (component.getText().contains("Coordenação de Apoio ao Plenário"))
				result = component.toString();
		assertEquals(
				"text :: 161.3, 723.07477, 431.73688, 730.56, Arial,Bold 15.0 :: Coordenação de Apoio ao Plenário ",
				result);
	}

	@After
	public void tearDown() throws IOException {
		if (doc != null) {
			doc.close();
			doc = null;
		}
	}
}
