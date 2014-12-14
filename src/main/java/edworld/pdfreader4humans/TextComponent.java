// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans;

public class TextComponent extends Component {
	private String text;

	private String fontName;

	private float fontSize;

	public TextComponent(String text, float fromX, float fromY, float toX, float toY, String fontName, float fontSize) {
		super("text", fromX, fromY, toX, toY);
		this.text = text;
		this.fontName = fontName;
		this.fontSize = fontSize;
	}

	public String getText() {
		return text;
	}

	public String getFontName() {
		return fontName;
	}

	public float getFontSize() {
		return fontSize;
	}

	@Override
	public String output(String template) {
		String output = super.output(template);
		output = fillTemplate(output, "text", getText());
		output = fillTemplate(output, "fontName", getFontName());
		output = fillTemplate(output, "fontSize", getFontSize());
		return output;
	}

	@Override
	public String toString() {
		return super.toString() + ", " + fontName + " " + fontSize + " :: " + text;
	}
}
