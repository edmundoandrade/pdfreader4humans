// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans.impl;

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

import edworld.pdfreader4humans.GridComponent;
import edworld.pdfreader4humans.PDFGridLocator;

public class MainPDFGridLocator implements PDFGridLocator {
	public List<GridComponent> locateGridComponents(PDPage page) throws IOException {
		final PDPage pageToDraw = page;
		return new PageDrawer() {
			private List<GridComponent> list = new ArrayList<GridComponent>();

			public List<GridComponent> locateGridComponents() throws IOException {
				int scaling = 4;
				PDRectangle cropBox = pageToDraw.findCropBox();
				BufferedImage image = new BufferedImage((int) cropBox.getWidth() * scaling, (int) cropBox.getHeight() * scaling, BufferedImage.TYPE_INT_ARGB);
				Graphics2D graphics = image.createGraphics();
				graphics.setBackground(Color.WHITE);
				graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
				graphics.scale(scaling, scaling);
				drawPage(graphics, pageToDraw, cropBox.createDimension());
				graphics.dispose();
				dispose();
				Collections.sort(list);
				return list;
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

			private void processSetFlatnessTolerance(COSNumber flatnessTolerance) {
				getGraphicsState().setFlatness(flatnessTolerance.doubleValue());
			}

			private void processLineTo(COSNumber x, COSNumber y) {
				Point2D from = getLinePath().getCurrentPoint();
				Point2D to = transformedPoint(x.doubleValue(), y.doubleValue());
				addGridComponent("line", from, to);
			}

			private void processAppendRectangleToPath(COSNumber x, COSNumber y, COSNumber w, COSNumber h) {
				Point2D from = transformedPoint(x.doubleValue(), y.doubleValue());
				Point2D to = transformedPoint(w.doubleValue() + x.doubleValue(), h.doubleValue() + y.doubleValue());
				addGridComponent("rect", from, to);
			}

			private void addGridComponent(String type, Point2D from, Point2D to) {
				float fromX = (float) Math.min(from.getX(), to.getX());
				float fromY = (float) Math.min(from.getY(), to.getY());
				float toX = (float) Math.max(from.getX(), to.getX());
				float toY = (float) Math.max(from.getY(), to.getY());
				list.add(new GridComponent(type, fromX, fromY, toX, toY, getGraphicsState().getLineWidth()));
			}

			private boolean isTextOperation(String operation) {
				return "/BT/ET/T*/Tc/Td/TD/Tf/Tj/TJ/TL/Tm/Tr/Ts/Tw/Tz/'/\"/".contains("/" + operation + "/");
			}
		}.locateGridComponents();
	}
}
