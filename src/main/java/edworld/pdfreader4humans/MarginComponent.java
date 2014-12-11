// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans;

public class MarginComponent extends Component {
	public MarginComponent(float fromX, float fromY, float toX, float toY) {
		super(fromX, fromY, toX, toY);
	}

	@Override
	public String toString() {
		return "margin (" + fromX + ", " + fromY + ", " + toX + ", " + toY + ")";
	}
}
