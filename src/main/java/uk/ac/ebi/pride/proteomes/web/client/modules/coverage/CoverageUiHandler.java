package uk.ac.ebi.pride.proteomes.web.client.modules.coverage;

import uk.ac.ebi.pride.widgets.client.protein.events.*;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 11/11/13
 *         Time: 12:18
 */
public interface CoverageUiHandler {
    void onRegionClickSelected(ProteinRegionSelectionEvent event);
    void onRegionDragSelected(ProteinAreaSelectedEvent event);
    void onRegionClickHighlighted(ProteinRegionHighlightEvent event);
    void onRegionDragHighlighted(ProteinAreaHighlightEvent event);

    void onPeptideSelected(PeptideSelectedEvent event);

    void onModificationSelected(ModificationSelectedEvent event);
    void onModificationHighlighted(ModificationHighlightedEvent event);
}
