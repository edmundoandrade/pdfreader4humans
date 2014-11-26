// This open source code is distributed without warranties, following the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader.impl;

import java.awt.Color;
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
			private List<GridComponent> list = new ArrayList<GridComponent>();

			public GridComponent[] locateGridComponents() throws IOException {
				int scaling = 4;
				PDRectangle cropBox = pageToDraw.findCropBox();
				BufferedImage image = new BufferedImage((int) cropBox.getWidth() * scaling, (int) cropBox.getHeight() * scaling, BufferedImage.TYPE_INT_ARGB);
				Graphics2D graphics = image.createGraphics();
				graphics.setBackground(Color.WHITE);
				graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
				graphics.scale(scaling, scaling);
				drawPage(graphics, pageToDraw, cropBox.createDimension());
				// ImageIO.write(image, "png", new File("/user/saida.png"));
				graphics.dispose();
				dispose();
				Collections.sort(list);
				return list.toArray(new GridComponent[0]);
			}

			@Override
			protected void processOperator(PDFOperator operator, List<COSBase> arguments) throws IOException {
				processGridOPeration(operator, arguments);
			}

			private void processGridOPeration(PDFOperator operator, List<COSBase> arguments) throws IOException {
				if (isTextOperation(operator.getOperation()))
					return;
				if (operator.getOperation().equals("i")) {
					processSetFlatnessTolerance((COSNumber) arguments.get(0));
					return;
				}
				if (operator.getOperation().equals("l"))
					processLineTo((COSNumber) arguments.get(0), (COSNumber) arguments.get(1));
				else if (operator.getOperation().equals("re"))
					processAppendRectangleToPath((COSNumber) arguments.get(0), (COSNumber) arguments.get(1), (COSNumber) arguments.get(2), (COSNumber) arguments.get(3));
				super.processOperator(operator, arguments);
			}

			private void processLineTo(COSNumber x, COSNumber y) {
				Point2D pos = transformedPoint(x.doubleValue(), y.doubleValue());
				float fromX = (float) getLinePath().getCurrentPoint().getX();
				float fromY = (float) getLinePath().getCurrentPoint().getY();
				float toX = (float) pos.getX();
				float toY = (float) pos.getY();
				list.add(new GridComponent("line", fromX, fromY, toX, toY, getGraphicsState().getLineWidth()));
			}

			private void processAppendRectangleToPath(COSNumber x, COSNumber y, COSNumber w, COSNumber h) {
				Point2D from = transformedPoint(x.doubleValue(), y.doubleValue());
				Point2D to = transformedPoint(w.doubleValue() + x.doubleValue(), h.doubleValue() + y.doubleValue());
				float fromX = (float) from.getX();
				float fromY = (float) from.getY();
				float toX = (float) to.getX();
				float toY = (float) to.getY();
				list.add(new GridComponent("rect", fromX, fromY, toX, toY, getGraphicsState().getLineWidth()));
			}

			private void processSetFlatnessTolerance(COSNumber flatnessTolerance) {
				getGraphicsState().setFlatness(flatnessTolerance.doubleValue());
			}

			private boolean isTextOperation(String operation) {
				return "/BT/ET/T*/Tc/Td/TD/Tf/Tj/TJ/TL/Tm/Tr/Ts/Tw/Tz/'/\"/".contains("/" + operation + "/");
			}
		}.locateGridComponents();
	}
}
