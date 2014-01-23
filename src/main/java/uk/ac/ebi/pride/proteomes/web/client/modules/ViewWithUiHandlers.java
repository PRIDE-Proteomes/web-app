package uk.ac.ebi.pride.proteomes.web.client.modules;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 17/12/13
 *         Time: 11:21
 */
public class ViewWithUiHandlers<H extends UiHandler> {
    private Collection<H> uiHandlers = new ArrayList<>();

    public Collection<H> getUiHandlers() {
        return uiHandlers;
    }

    public void addUiHandler(H handler) {
        getUiHandlers().add(handler);
    }
}