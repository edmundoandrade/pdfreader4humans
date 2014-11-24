// This open source code is distributed without warranties, following the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader;

public class TextComponent {
	private float x;
	private float y;
	private String text;
	private String fontName;
	private float fontSize;

	public TextComponent(float x, float y, String text, String fontName, float fontSize) {
		this.x = x;
		this.y = y;
		this.text = text;
		this.fontName = fontName;
		this.fontSize = fontSize;
	}

	@Override
	public String toString() {
		return text + " (" + x + ", " + y + ", " + fontName + " " + fontSize + ")";
	}
}
