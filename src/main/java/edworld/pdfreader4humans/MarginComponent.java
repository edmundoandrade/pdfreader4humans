// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans;

public class MarginComponent extends Component {
	public MarginComponent(float fromX, float fromY, float toX, float toY) {
		super("margin", fromX, fromY, toX, toY);
	}

	public MarginComponent extended(Component component) {
		float fromX = Math.min(getFromX(), component.getFromX());
		float fromY = Math.min(getFromY(), component.getFromY());
		float toX = Math.max(getToX(), component.getToX());
		float toY = Math.max(getToY(), component.getToY());
		return new MarginComponent(fromX, fromY, toX, toY);
	}
}
