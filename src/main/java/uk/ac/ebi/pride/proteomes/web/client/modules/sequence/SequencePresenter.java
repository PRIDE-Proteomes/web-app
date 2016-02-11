package uk.ac.ebi.pride.proteomes.web.client.modules.sequence;

import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.UserAction;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.ModificationWithPosition;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.PeptideWithPeptiforms;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Region;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ModificationAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.PeptideAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ProteinAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.ProteinRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.StateChangingActionEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.ValidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.*;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.IllegalRegionValueException;
import uk.ac.ebi.pride.proteomes.web.client.modules.HasUiHandlers;
import uk.ac.ebi.pride.proteomes.web.client.modules.Presenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.View;
import uk.ac.ebi.pride.proteomes.web.client.modules.history.StateChanger;
import uk.ac.ebi.pride.proteomes.web.client.utils.PeptideUtils;
import uk.ac.ebi.pride.widgets.client.sequence.events.ProteinRegionHighlightedEvent;
import uk.ac.ebi.pride.widgets.client.sequence.events.ProteinRegionSelectionEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 27/11/13
 *         Time: 10:57
 */
public class SequencePresenter extends Presenter<SequencePresenter.ThisView>
                               implements SequenceUiHandler,
                                          ValidStateEvent.Handler,
                                          ProteinUpdateEvent.Handler,
                                          ProteinRequestEvent.Handler,
                                          RegionUpdateEvent.Handler,
                                          PeptideUpdateEvent.Handler,
                                          ModificationUpdateEvent.Handler,
                                          TissueUpdateEvent.Handler {

    public interface ThisView extends View, HasUiHandlers<SequenceUiHandler> {
        void updateProtein(ProteinAdapter proteinAdapter);
        void updateRegionSelection(int start, int end);
        void resetRegionSelection();
        void updatePeptideSelection(List<PeptideAdapter> peptideSelection);
        void resetPeptideSelection();
        void updateModificationHighlight(ModificationAdapter mod);
        void updateModificationHighlightBetween(ModificationAdapter mod, int start, int end);
        void updateModificationHighlight(int modPosition);
        void highlightModificationsInPeptides(List<ModificationAdapter> modifications, List<PeptideAdapter> peptides);
        void resetModificationHighlight();
        void updatePeptideHighlight(List<PeptideAdapter> peptides);
        void updatePeptideHighlight(PeptideAdapter peptides);
        void resetPeptideHighlight();
        void displayLoadingMessage();
    }

    private static Logger logger = Logger.getLogger(SequencePresenter.class.getName());


    private boolean hiding = true;
    private Protein currentProtein;
    private List<PeptideWithPeptiforms> currentPeptides = Collections.emptyList();
    private List<String> currentTissues = Collections.emptyList();
    private List<PeptideWithPeptiforms> peptideMatchSelection = Collections.emptyList();
    private Region currentRegion = Region.emptyRegion();
    private List<ModificationWithPosition> currentModifications = Collections.emptyList();

    public SequencePresenter(EventBus eventBus, ThisView view) {
        super(eventBus, view);

        view.addUiHandler(this);
        view.asWidget().setVisible(false);

        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(ProteinUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinRequestEvent.getType(), this);
        eventBus.addHandler(RegionUpdateEvent.getType(), this);
        eventBus.addHandler(PeptideUpdateEvent.getType(), this);
        eventBus.addHandler(ModificationUpdateEvent.getType(), this);
        eventBus.addHandler(TissueUpdateEvent.getType(), this);
    }

    // Callbacks that handle event bus events

    @Override
    public void onValidStateEvent(ValidStateEvent event) {
        if(event.getViewType() == ValidStateEvent.ViewType.Protein) {
            hiding = false;
            getView().asWidget().setVisible(true);
        }
        else {
            hiding = true;
            getView().asWidget().setVisible(false);
        }
    }

    @Override
    public void onProteinUpdateEvent(ProteinUpdateEvent event) {
        if(!hiding && event.getProteins().size() > 0) {
            currentProtein = event.getProteins().get(0);
            currentRegion = Region.emptyRegion();
            getView().updateProtein(new ProteinAdapter(currentProtein));
        }
    }

    @Override
    public void onProteinRequestEvent(ProteinRequestEvent event) {
        getView().displayLoadingMessage();
    }

    @Override
    public void onRegionUpdateEvent(RegionUpdateEvent event) {
        Region region;
//        List<PeptideAdapter> selectionAdapters;
//
//        if (event.getSource() == this) {
//            return;
//        }

        if(event.getRegions().size() > 0) {
            currentRegion = event.getRegions().get(0);
            getView().updateRegionSelection(currentRegion.getStart(), currentRegion.getEnd());
            if (currentRegion.getLength() == 1) {
                getView().updateModificationHighlight(currentRegion.getStart());
            }
        }
        else {
            currentRegion = Region.emptyRegion();
            getView().resetRegionSelection();
        }

//        // Apparently when the region gets modified the peptide selection
//        // gets reset, we must set it again manually.
//
//        if (!peptideMatchSelection.isEmpty()) {
//            selectionAdapters = new ArrayList<>();
//            for (PeptideMatch match : peptideMatchSelection) {
//                selectionAdapters.add(new PeptideAdapter(match));
//            }
//            getView().updatePeptideSelection(selectionAdapters);
//        }
        // we change the peptide list
    }

    @Override
    public void onPeptideUpdateEvent(PeptideUpdateEvent event) {
        List<PeptideAdapter> selectionAdapters;

        if(event.getPeptides().size() > 0) {
            selectionAdapters = new ArrayList<>();

            for(PeptideMatch match : event.getPeptides()) {
                selectionAdapters.add(new PeptideAdapter(match));
            }
            currentPeptides = event.getPeptides();
            getView().updatePeptideSelection(selectionAdapters);

        }
        else {
            getView().resetPeptideSelection();
            currentPeptides = Collections.emptyList();
        }
    }

    /**
     * This function has to take into account that a modification can be a
     * position or a type of modification
     *
     * @param event event containing the string identifying a modification.
     */
    @Override
    public void onModificationUpdateEvent(ModificationUpdateEvent event) {
        List<ModificationAdapter> modificationAdapters;
        List<PeptideAdapter> peptideAdapters;

        currentModifications = event.getModifications();
        getView().resetPeptideHighlight();
        getView().resetModificationHighlight();

        if (!currentModifications.isEmpty()) {
            List<PeptideMatch> peptides = updatePeptideHighlight();
            //TODO Review
            if (!peptides.isEmpty()) {
                modificationAdapters = new ArrayList<>();
                for (ModificationWithPosition currentModification : currentModifications) {
                    modificationAdapters.add(
                            new ModificationAdapter(currentModification.getModification(), currentModification.getPosition()));
                }

                peptideAdapters = new ArrayList<>();
                for (PeptideMatch match : peptides) {
                    peptideAdapters.add(new PeptideAdapter(match));
                }

                getView().highlightModificationsInPeptides(modificationAdapters, peptideAdapters);
            }
        }

    }

    @Override
    public void onTissueUpdateEvent(TissueUpdateEvent event) {

        currentTissues = event.getTissues();
        getView().resetPeptideHighlight();
        getView().resetModificationHighlight();

        updatePeptideHighlight();
    }

    @Override
    public void onRegionSelected(ProteinRegionSelectionEvent event) {
        StateChanger changer = new StateChanger();
        List<Region> regions = new ArrayList<>();
        UserAction action = new UserAction(UserAction.Type.region, "Drag Set");
        List<PeptideMatch> peptides;

        // if the selection is done right to left then start > end
        int start = event.getStart() < event.getEnd() ? event.getStart() : event.getEnd();
        int end = event.getStart() + event.getEnd() - start;

        try {
            regions.add(new Region(start, end));

            // We should keep selecting only the peptides that fit in the new
            // region
            if(currentPeptides.size() > 0) {
                peptides = PeptideUtils.filterPeptideMatches(currentPeptides,
                        start, end,
                        currentTissues, currentModifications,currentProtein.getSequence().length());

                changer.addPeptideChange(peptides);
            }

        } catch (IllegalRegionValueException ignore) {

        } finally {
            changer.addRegionChange(regions);
            StateChangingActionEvent.fire(this, changer, action);
        }
    }

    @Override
    public void onRegionHighlighted(ProteinRegionHighlightedEvent event) {
        List<Region> regions = new ArrayList<>();

        // if the selection is done right to left then start > end
        int start = event.getStart() < event.getEnd() ? event.getStart() : event.getEnd();
        int end = event.getStart() + event.getEnd() - start;

        try {
            regions.add(new Region(start, end));
        } catch (IllegalRegionValueException e) {
            regions.add(Region.emptyRegion());
        } finally {
            RegionUpdateEvent.fire(this, regions);
        }
    }

    private List<PeptideMatch> updatePeptideHighlight() {
        List<PeptideMatch> peptides = Collections.emptyList();
        List<PeptideAdapter> selectionAdapters;

        if(!currentTissues.isEmpty() || !currentModifications.isEmpty()) {
            peptides = PeptideUtils.filterPeptideMatches(currentProtein.getPeptides(),
                    currentRegion.getStart(), currentRegion.getEnd(),
                    currentTissues, currentModifications, currentProtein.getSequence().length());

            if (peptides.size() > 0) {
                selectionAdapters = new ArrayList<>();

                for (PeptideMatch match : peptides) {
                    selectionAdapters.add(new PeptideAdapter(match));
                }

                getView().updatePeptideHighlight(selectionAdapters);
            }
        }

        return peptides;
    }
}
