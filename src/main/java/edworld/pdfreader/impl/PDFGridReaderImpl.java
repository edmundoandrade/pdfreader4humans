// This open source code is distributed without warranties, following the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader.impl;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.PDFOperator;

import edworld.pdfreader.GridComponent;
import edworld.pdfreader.PDFGridReader;

public class PDFGridReaderImpl implements PDFGridReader {
	private PDPage page;

	public PDFGridReaderImpl(PDPage page) {
		this.page = page;
	}

	public GridComponent[] locateGridComponents() throws IOException {
		final PDPage pageToDraw = page;
		return new PageDrawer() {
			private ArrayList<GridComponent> list = new ArrayList<GridComponent>();

			public GridComponent[] locateGridComponents() throws IOException {
				PDRectangle cropBox = pageToDraw.findCropBox();
				BufferedImage image = new BufferedImage((int) cropBox.getWidth(), (int) cropBox.getHeight(), BufferedImage.TYPE_INT_ARGB);
				Graphics2D graphics = image.createGraphics();
				drawPage(graphics, pageToDraw, cropBox.createDimension());
				graphics.dispose();
				dispose();
				Collections.sort(list);
				return list.toArray(new GridComponent[0]);
			}

			@Override
			protected void processOperator(PDFOperator operator, List<COSBase> arguments) throws IOException {
				if (operator.getOperation().equals("l")) {
					processLineTo((COSNumber) arguments.get(0), (COSNumber) arguments.get(1));
				}
				super.processOperator(operator, arguments);
			};

			private void processLineTo(COSNumber x, COSNumber y) {
				Point2D pos = transformedPoint(x.doubleValue(), y.doubleValue());
				float fromX = (float) getLinePath().getCurrentPoint().getX();
				float fromY = (float) getLinePath().getCurrentPoint().getY();
				float toX = (float) pos.getX();
				float toY = (float) pos.getY();
				list.add(new GridComponent("line", fromX, fromY, toX, toY));
			}
		}.locateGridComponents();
	}
}
