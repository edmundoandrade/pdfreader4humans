// This open source code is distributed without warranties, following the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edworld.pdfreader.PDFTextReader;
import edworld.pdfreader.TextComponent;

public class PDFTextReaderImplTest {
	private PDDocument doc;
	private PDFTextReader reader;

	@Before
	public void setUp() throws Exception {
		doc = PDDocument.load(getClass().getResource("/testcase1/input.pdf"));
		PDPage page1 = (PDPage) doc.getDocumentCatalog().getAllPages().get(0);
		reader = new PDFTextReaderImpl(page1);
	}

	@Test
	public void locateTextComponents() throws IOException {
		TextComponent[] components = reader.locateTextComponents();
		assertEquals(387, components.length);
		assertEquals("Nº 31, quinta-feira, 13 de fevereiro de 2014 (49.348, 62.794617, Times-Roman 9.0)", components[0].toString());
		assertEquals("1 (506.4658, 62.764587, HNBDHM+OxfordWd 11.0)", components[1].toString());
		assertEquals("ISSN 1677-7042 (575.9183, 63.208984, Times-Italic 9.0)", components[2].toString());
		assertEquals("3 (705.2703, 62.155884, Times-Italic 11.0)", components[3].toString());
		assertEquals("Outros instrumentos e aparelhos (102.3502, 82.99078, Times-Roman 7.0)", components[4].toString());
		assertEquals("15% (294.9229, 82.99078, Times-Roman 7.0)", components[5].toString());
		assertEquals("10% (353.0895, 82.99078, Times-Roman 7.0)", components[6].toString());
		assertEquals("90.30 (434.5236, 82.99078, Times-Roman 7.0)", components[7].toString());
		assertEquals("Osciloscópios, analisadores de espectro e outros (470.85208, 82.99078, Times-Roman 7.0)", components[8].toString());
		assertEquals("15% (663.42487, 82.99078, Times-Roman 7.0)", components[9].toString());
		assertEquals("10% (721.59143, 82.99078, Times-Roman 7.0)", components[10].toString());
		assertEquals("Contadores de gases, líquidos ou de eletricidade, incluídos os aparelhos para sua aferição, baseados (50.6142, 93.47052, Times-Roman 7.0)",
				components[11].toString());
		assertEquals("instrumentos e aparelhos para medida ou controle (470.8521, 90.96088, Times-Roman 7.0)", components[12].toString());
		assertEquals("em técnicas digitais e outros contadores baseados em técnicas digitais. (50.6142, 101.44061, Times-Roman 7.0)", components[13].toString());
	}

	@After
	public void tearDown() throws IOException {
		doc.close();
	}
}
