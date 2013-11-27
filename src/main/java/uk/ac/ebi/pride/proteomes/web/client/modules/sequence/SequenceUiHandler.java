package uk.ac.ebi.pride.proteomes.web.client.modules.sequence;

import uk.ac.ebi.pride.proteomes.web.client.modules.UiHandler;
import uk.ac.ebi.pride.widgets.client.sequence.events.ProteinRegionHighlightedEvent;
import uk.ac.ebi.pride.widgets.client.sequence.events.ProteinRegionSelectionEvent;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 27/11/13
 *         Time: 10:58
 */
public interface SequenceUiHandler extends UiHandler {
    void onRegionSelected(ProteinRegionSelectionEvent e);

    void onRegionHighlighted(ProteinRegionHighlightedEvent e);
}
