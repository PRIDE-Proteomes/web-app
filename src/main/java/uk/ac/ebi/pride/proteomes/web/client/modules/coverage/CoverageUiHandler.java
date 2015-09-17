package uk.ac.ebi.pride.proteomes.web.client.modules.coverage;

import uk.ac.ebi.pride.proteomes.web.client.modules.UiHandler;
import uk.ac.ebi.pride.widgets.client.feature.events.FeatureAreaHighlightEvent;
import uk.ac.ebi.pride.widgets.client.feature.events.FeatureAreaSelectionEvent;
import uk.ac.ebi.pride.widgets.client.feature.events.FeatureRegionHighlightEvent;
import uk.ac.ebi.pride.widgets.client.feature.events.FeatureRegionSelectionEvent;
import uk.ac.ebi.pride.widgets.client.protein.events.*;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 11/11/13
 *         Time: 12:18
 */
public interface CoverageUiHandler extends UiHandler {

    //Protein
    void onRegionClickSelected(ProteinRegionSelectionEvent event);
    void onRegionClickHighlighted(ProteinRegionHighlightEvent event);

    void onRegionDragSelected(ProteinAreaSelectedEvent event);
    void onRegionDragHighlighted(ProteinAreaHighlightEvent event);

    void onPeptideSelected(PeptideSelectedEvent event);

    void onModificationSelected(ModificationSelectedEvent event);

    //Features
    void onRegionClickSelected(FeatureRegionSelectionEvent event);
    void onRegionClickHighlighted(FeatureRegionHighlightEvent event);

    void onRegionDragSelected(FeatureAreaSelectionEvent event);
    void onRegionDragHighlighted(FeatureAreaHighlightEvent event);
}