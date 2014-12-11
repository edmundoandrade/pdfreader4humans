// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans;

public class BoxComponent extends Component {
	private boolean borderLeft, borderTop, borderRight, borderBottom;
	private double lineWidth;

	public BoxComponent(float fromX, float fromY, float toX, float toY, double lineWidth, boolean borderLeft, boolean borderTop, boolean borderRight, boolean borderBottom) {
		super(fromX, fromY, toX, toY);
		this.lineWidth = lineWidth;
		this.borderLeft = borderLeft;
		this.borderTop = borderTop;
		this.borderRight = borderRight;
		this.borderBottom = borderBottom;
	}

	public double getLineWidth() {
		return lineWidth;
	}

	@Override
	public String toString() {
		return "box (" + fromX + ", " + fromY + ", " + toX + ", " + toY + ", " + lineWidth + "pt, " + borderToString() + ")";
	}

	protected String borderToString() {
		String border = "";
		if (borderLeft)
			border += ":left";
		if (borderTop)
			border += ":top";
		if (borderRight)
			border += ":right";
		if (borderBottom)
			border += ":bottom";
		return "border" + (border.isEmpty() ? ":no" : border);
	}
}
