package uk.ac.ebi.pride.proteomes.web.client.utils.factories;

import com.google.gwt.user.client.ui.Hyperlink;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 05/12/13
 *         Time: 15:57
 */
public class HyperlinkFactory {
    public static Hyperlink getHyperLink(String newLocation, String name) {
        return new Hyperlink(newLocation, name);
    }
}
