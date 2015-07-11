package edworld.pdfreader4humans;

import static org.apache.commons.io.FileUtils.writeLines;
import static org.apache.commons.io.FileUtils.writeStringToFile;

import java.io.File;
import java.io.IOException;

import edworld.pdfreader4humans.impl.MainBoxDetector;
import edworld.pdfreader4humans.impl.MainMarginDetector;
import edworld.pdfreader4humans.impl.MainPDFComponentLocator;

public class MainPDFReader {
	public static void main(String[] args) throws IOException {
		if (args.length < 3) {
			System.out.println("Syntax for saving a PDF's content into a XML file: -toXML sourcePDF targetXML");
			System.out.println("Syntax for saving a PDF's content into a TXT file: -toTXT sourcePDF targetTXT");
			return;
		}
		PDFReader reader = new PDFReader(new File(args[1]).toURI().toURL(), new MainPDFComponentLocator(), new MainBoxDetector(), new MainMarginDetector());
		if (args[0].equals("-toXML"))
			writeStringToFile(new File(args[2]), reader.toXML(), "UTF-8");
		else
			writeLines(new File(args[2]), "UTF-8", reader.toTextLines());
	}
}
