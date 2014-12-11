// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans;

public class GridComponent extends Component {
	private String type;
	private double lineWidth;

	public GridComponent(String type, float fromX, float fromY, float toX, float toY, double lineWidth) {
		super(fromX, fromY, toX, toY);
		this.type = type;
		this.lineWidth = lineWidth;
	}

	public String getType() {
		return type;
	}

	public double getLineWidth() {
		return lineWidth;
	}

	@Override
	public String toString() {
		return type + " (" + fromX + ", " + fromY + ", " + toX + ", " + toY + ", " + lineWidth + "pt)";
	}
}
