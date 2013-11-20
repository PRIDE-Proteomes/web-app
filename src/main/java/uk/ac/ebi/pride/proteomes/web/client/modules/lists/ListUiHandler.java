package uk.ac.ebi.pride.proteomes.web.client.modules.lists;

import uk.ac.ebi.pride.proteomes.web.client.modules.UiHandler;

import java.util.Collection;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 19/11/13
 *         Time: 16:47
 */
public interface ListUiHandler<T> extends UiHandler {
    void onSelectionChanged(Collection<T> items);
}
