package uk.ac.ebi.pride.proteomes.web.client.modules;

import java.util.Collection;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 17/12/13
 *         Time: 11:50
 */
public interface HasUiHandlers<H extends UiHandler> {
    Collection<H> getUiHandlers();
    public void addUiHandler(H handler);
}
