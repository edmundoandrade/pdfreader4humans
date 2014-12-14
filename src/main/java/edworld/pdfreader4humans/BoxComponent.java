// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans;

public class BoxComponent extends Component {
	private boolean borderLeft, borderTop, borderRight, borderBottom;
	private double lineWidth;

	public BoxComponent(float fromX, float fromY, float toX, float toY, double lineWidth, boolean borderLeft, boolean borderTop, boolean borderRight, boolean borderBottom) {
		super("box", fromX, fromY, toX, toY);
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
	public String output(String template) {
		String output = super.output(template);
		output = fillTemplate(output, "lineWidth", getLineWidth());
		output = fillTemplate(output, "borders", borderToString());
		return output;
	}

	@Override
	public String toString() {
		return super.toString() + ", " + lineWidth + "pt, borders:" + borderToString();
	}

	protected String borderToString() {
		String borders = "";
		if (borderLeft)
			borders += ",left";
		if (borderTop)
			borders += ",top";
		if (borderRight)
			borders += ",right";
		if (borderBottom)
			borders += ",bottom";
		return borders.isEmpty() ? "no" : borders.substring(1);
	}
}
