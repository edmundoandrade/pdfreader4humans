// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans;

public class GridComponent extends Component {
	private double lineWidth;

	public GridComponent(String type, float fromX, float fromY, float toX, float toY, double lineWidth) {
		super(type, fromX, fromY, toX, toY);
		this.lineWidth = lineWidth;
	}

	public double getLineWidth() {
		return lineWidth;
	}

	@Override
	public String output(String template) {
		return fillTemplate(super.output(template), "lineWidth", getLineWidth());
	}

	@Override
	public String toString() {
		return super.toString() + ", " + lineWidth + "pt";
	}
}
