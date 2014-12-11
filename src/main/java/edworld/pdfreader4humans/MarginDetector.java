// This open source code is distributed without warranties according to the license published at http://www.apache.org/licenses/LICENSE-2.0
package edworld.pdfreader4humans;

import java.util.List;

public interface MarginDetector {
	List<MarginComponent> detectMargins(List<? extends Component> components);
}
