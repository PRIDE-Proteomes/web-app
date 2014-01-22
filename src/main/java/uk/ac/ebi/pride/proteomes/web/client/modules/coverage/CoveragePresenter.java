package uk.ac.ebi.pride.proteomes.web.client.modules.coverage;

import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.UserAction;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.PeptideWithVariances;
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
import uk.ac.ebi.pride.widgets.client.protein.events.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 08/11/13
 *         Time: 14:43
 */
public class CoveragePresenter extends Presenter<CoveragePresenter.ThisView>
                               implements CoverageUiHandler,
                                          ValidStateEvent.Handler,
                                          ProteinUpdateEvent.Handler,
                                          ProteinRequestEvent.Handler,
                                          RegionUpdateEvent.Handler,
                                          PeptideUpdateEvent.Handler,
                                          ModificationUpdateEvent.Handler,
                                          VarianceUpdateEvent.Handler {

    public interface ThisView extends View, HasUiHandlers<CoverageUiHandler> {
        public void updateProtein(ProteinAdapter protein);
        public void updateRegionSelection(int start, int end);
        public void resetRegionSelection();
        public void updatePeptideSelection(List<PeptideAdapter> peptideSelection);
        public void resetPeptideSelection();
        public void updateModificationHighlight(ModificationAdapter mod);
        public void updateModificationHighlight(int start, int end);
        public void resetModificationHighlight();
        public void displayLoadingMessage();
    }

    private boolean hiding = true;
    private boolean justHighlighted = false;
    private Protein currentProtein;
    private Region currentRegion = Region.emptyRegion();

    //We need to keep both peptide lists updated to be able to send the matches
    // to the widget and send the peptides to the eventBus when
    // we drag the area highlight without affecting the url
    private PeptideWithVariances currentPeptide = PeptideWithVariances.emptyPeptideWithVariances();
    private List<PeptideMatch> currentPeptideMatches = Collections.emptyList();
    private List<String> selectedVarianceIDs = Collections.emptyList();

    //Needed to maintain temporary state while doing a selection
    private List<PeptideMatch> tempPeptides = Collections.emptyList();

    public CoveragePresenter(EventBus eventBus, ThisView view) {
        super(eventBus, view);

        view.addUiHandler(this);
        view.asWidget().setVisible(false);

        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(ProteinUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinRequestEvent.getType(), this);
        eventBus.addHandler(RegionUpdateEvent.getType(), this);
        eventBus.addHandler(PeptideUpdateEvent.getType(), this);
        eventBus.addHandler(ModificationUpdateEvent.getType(), this);
        eventBus.addHandler(VarianceUpdateEvent.getType(), this);
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
        List<PeptideAdapter> selectionAdapters;

        if(event.getSource() == this) {
            return;
        }

        if(event.getRegions().size() > 0) {
            region = event.getRegions().get(0);
            currentRegion = region;
            getView().updateRegionSelection(region.getStart(), region.getEnd());
            if(region.getLength() == 1) {
                getView().updateModificationHighlight(region.getStart(), region.getEnd());
            }
        }
        else {
            currentRegion = Region.emptyRegion();
            getView().resetRegionSelection();
        }

        // Apparently when the region gets modified the peptide selection
        // gets reset, we must set it again manually.

        if(!currentPeptideMatches.isEmpty()) {
            selectionAdapters = new ArrayList<PeptideAdapter>();
            for(PeptideMatch match : currentPeptideMatches) {
                selectionAdapters.add(new PeptideAdapter(match));
            }
            getView().updatePeptideSelection(selectionAdapters);
        }
    }

    /**
     * This function selects all the peptide matches in the coverage view that
     * have the same sequence as the first peptide of the list that it's
     * received.
     * @param event the event containing a list of peptides that got updated,
     *              we want to select them in the coverage view
     */
    @Override
    public void onPeptideUpdateEvent(PeptideUpdateEvent event) {
        List<PeptideAdapter> selectionAdapters;
        List<PeptideMatch> selection;

        if(event.getSource() == this) {
            return;
        }

        if(event.getPeptides().size() > 0) {
            selectionAdapters = new ArrayList<PeptideAdapter>();
            selection = new ArrayList<PeptideMatch>();

            for(PeptideMatch match : currentProtein.getPeptides()) {
                if(match.getSequence().equals(event.getPeptides().get(0).getSequence())) {
                    selectionAdapters.add(new PeptideAdapter(match));
                    selection.add(match);
                }
            }
            currentPeptide = event.getPeptides().get(0);
            currentPeptideMatches = selection;
            tempPeptides = selection;
            getView().updatePeptideSelection(selectionAdapters);
        }
        else {
            getView().resetPeptideSelection();
            currentPeptide = PeptideWithVariances.emptyPeptideWithVariances();
            currentPeptideMatches = Collections.emptyList();
            tempPeptides = Collections.emptyList();
        }
    }

    @Override
    public void onVarianceUpdateEvent(VarianceUpdateEvent event) {
        selectedVarianceIDs = event.getVarianceIDs();
    }

    /**
     * This function has to take into account that a modification can be a
     * position or a type of modification
     * @param event event containing the string identifying a modification.
     */
    @Override
    public void onModificationUpdateEvent(ModificationUpdateEvent event) {
        if(!event.getModifications().isEmpty()) {
            String currentModification = event.getModifications().get(0);
            try {
                int position = Integer.parseInt(currentModification);

                getView().updateModificationHighlight(position, position);
            }
            catch (NumberFormatException e) {
                getView().updateModificationHighlight(new ModificationAdapter(currentModification));
            }
        }
        else {
            getView().resetModificationHighlight();
        }
    }

    // Callbacks that handle user events from the view.

    @Override
    public void onRegionClickSelected(ProteinRegionSelectionEvent event) {
        StateChanger changer = new StateChanger();
        List<Region> region = new ArrayList<Region>();
        Set<PeptideMatch> peptides;

        try {
            region.add(new Region(event.getStart(), event.getStart() + event
                    .getLength() - 1));
            changer.addRegionChange(region);

            peptides = new HashSet<PeptideMatch>();
            for(PeptideMatch peptide : currentPeptideMatches) {
                if(PeptideUtils.inRange(peptide, event.getStart(),
                        event.getStart() + event.getLength() - 1)) {
                    peptides.add(peptide);
                }
            }
            changer.addPeptideChange(peptides);

            UserAction action = new UserAction(UserAction.Type.region,
                                               "Click Coverage Set");
            StateChangingActionEvent.fire(this, changer, action);
        } catch (IllegalRegionValueException e) {
            // This is probably because of an empty selection,
            // we don't send any event
        }
    }

    @Override
    public void onRegionDragSelected(ProteinAreaSelectedEvent event) {
        StateChanger changer = new StateChanger();
        List<Region> regions = new ArrayList<Region>();
        UserAction action = UserAction.emptyAction();
        Set<PeptideMatch> peptides;
        Region region;

        // if the selection is done right to left then start > end
        int start = event.getStart() < event.getEnd() ? event.getStart() : event.getEnd();
        int end = event.getStart() + event.getEnd() - start;

        try {
            region = new Region(start, end);
            // We have to check if a highlight has been done just before to
            // know if the user wants to reset or want to select a 1-site region
            if(region.getLength() == 0 && !justHighlighted) {
                //we don't want to select a single aminoacid,
                // we want to reset the selection
                action = new UserAction(UserAction.Type.region,
                        "Drag Coverage Reset");
                region = Region.emptyRegion();
            }
            else {
                action = new UserAction(UserAction.Type.region,
                        "Drag Coverage Set");
                justHighlighted = false;
            }

            regions.add(region);

            peptides = new HashSet<PeptideMatch>();
            if(!region.isEmpty() && !region.equals(currentRegion)) {
                for(PeptideMatch peptide : currentPeptideMatches) {
                    if(PeptideUtils.inRange(peptide, start, end)) {
                        peptides.add(peptide);
                    }
                }
            }
            // if the region is empty then an empty set will get sent, we reset
            // the peptide selection.
            changer.addPeptideChange(peptides);

        } catch (IllegalRegionValueException e) {
            action = new UserAction(UserAction.Type.region,
                    "Drag Coverage Reset");
            region = Region.emptyRegion();
            regions.add(region);
        } finally {
            changer.addRegionChange(regions);
            StateChangingActionEvent.fire(this, changer, action);
        }
    }

    @Override
    public void onRegionClickHighlighted(ProteinRegionHighlightEvent event) {
        // we don't do anything for the moment
    }

    @Override
    public void onRegionDragHighlighted(ProteinAreaHighlightEvent event) {
        List<Region> regions = new ArrayList<Region>();

        // if the selection is done right to left then start > end
        int start = event.getStart() < event.getEnd() ? event.getStart() : event.getEnd();
        int end = event.getStart() + event.getEnd() - start;

        try {
            regions.add(new Region(start, end));
            justHighlighted = true;
        } catch (IllegalRegionValueException e) {
            regions.add(Region.emptyRegion());
        } finally {
            RegionUpdateEvent.fire(this, regions);

            // We have to send an update list of peptides that are selected with
            // the new list otherwise other widget might get desyncronized,
            // that's not good.

            //Check if the region changes the number of peptides selected
            if(tempPeptides.size() !=
                    PeptideUtils.filterPeptideMatchesNotIn(currentPeptideMatches, start, end).size()) {
                tempPeptides = PeptideUtils.filterPeptideMatchesNotIn(currentPeptideMatches, start, end);

                // We have to do dirty things to able to give the peptide list to the
                // other widgets :D
                List<PeptideWithVariances> peptideList = new ArrayList<PeptideWithVariances>();

                if(!tempPeptides.isEmpty()) {
                    peptideList.add(currentPeptide);
                }

                PeptideUpdateEvent.fire(this, peptideList);

                // We should restore the variance IDs too in case they need to be reselected
                if(tempPeptides.size() == currentPeptideMatches.size()) {
                    VarianceUpdateEvent.fire(this, selectedVarianceIDs);
                }
            }
        }
    }

    @Override
    public void onPeptideSelected(PeptideSelectedEvent event) {
        StateChanger changer = new StateChanger();
        List<Region> regions = new ArrayList<Region>();
        List<PeptideMatch> peptides = new ArrayList<PeptideMatch>();
        UserAction action = new UserAction(UserAction.Type.peptide,
                                           "Click Set");

        PeptideAdapter peptide = (PeptideAdapter) event.getPeptide();

        // If the peptide doesn't fit the region that's selected we ought to
        // change the region too.
        if(!currentRegion.isEmpty() &&
                !PeptideUtils.inRange(peptide, currentRegion.getStart(),
                currentRegion.getEnd())) {
            try {
                regions.add(new Region(peptide.getSite(), peptide.getEnd()));
                changer.addRegionChange(regions);
            } catch (IllegalRegionValueException e) {
                // this shouldn't happen, unless the peptide is somehow empty.
                e.printStackTrace();
            }
        }

        List<String> varianceIDs = new ArrayList<String>();
        for(String varianceID : selectedVarianceIDs) {
            if(peptide.getSequence().equals(varianceID.split("[|]")[0].substring(1))) {
                varianceIDs.add(varianceID);
            }
        }

        if(!varianceIDs.containsAll(selectedVarianceIDs)) {
            changer.addVarianceChange(varianceIDs);
        }
        peptides.add(peptide);
        changer.addPeptideChange(peptides);

        StateChangingActionEvent.fire(this, changer, action);
    }

    @Override
    public void onModificationSelected(ModificationSelectedEvent event) {
        StateChanger changer = new StateChanger();
        List<Region> regions = new ArrayList<Region>();
        List<String> modifications = new ArrayList<String>();
        UserAction action = new UserAction(UserAction.Type.modification,
                                           "Click Set");

        // Terminal modifications are ignored for now
        if(event.getSite() > 0 && event.getSite() < currentProtein
                .getSequence().length() + 1) {
            if(event.getSite() < currentRegion.getStart() ||
               event.getSite() > currentRegion.getEnd()) {
                try {
                    regions.add(new Region(event.getSite(), event.getSite()));
                    changer.addRegionChange(regions);
                } catch (IllegalRegionValueException e) {
                    // this shouldn't happen, at all.
                    e.printStackTrace();
                }
            }
            modifications.add(event.getSite().toString());
            changer.addModificationChange(modifications);
            StateChangingActionEvent.fire(this, changer, action);
        }
    }
}
