package uk.ac.ebi.pride.proteomes.web.client.utils.factories;

import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.InlineHyperlink;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 05/12/13
 *         Time: 15:57
 */
public class HyperlinkFactory {
    public static Hyperlink getInlineHyperLink(String newLocation, String name) {
        return new InlineHyperlink(newLocation, name);
    }
}
