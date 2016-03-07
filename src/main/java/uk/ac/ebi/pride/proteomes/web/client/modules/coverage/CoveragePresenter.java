package uk.ac.ebi.pride.proteomes.web.client.modules.coverage;

import com.google.common.collect.Lists;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.UserAction;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.ModificationWithPosition;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.PeptideWithPeptiforms;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Region;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ModificationAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.PeptideAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ProteinAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
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
import uk.ac.ebi.pride.widgets.client.common.handler.ProteinModificationHandler;
import uk.ac.ebi.pride.widgets.client.feature.events.FeatureAreaHighlightEvent;
import uk.ac.ebi.pride.widgets.client.feature.events.FeatureAreaSelectionEvent;
import uk.ac.ebi.pride.widgets.client.feature.events.FeatureRegionHighlightEvent;
import uk.ac.ebi.pride.widgets.client.feature.events.FeatureRegionSelectionEvent;
import uk.ac.ebi.pride.widgets.client.protein.events.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

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
                                          ModificationWithPositionUpdateEvent.Handler,
                                          PeptiformUpdateEvent.Handler,
                                          TissueUpdateEvent.Handler {


    public interface ThisView extends View, HasUiHandlers<CoverageUiHandler> {
        void updateProtein(ProteinAdapter protein);
        void updateRegionSelection(int start, int end);
        void resetRegionSelection();
        void updatePeptideSelection(List<PeptideAdapter> peptideSelection);
        void resetPeptideSelection();
        void updateModificationHighlight(ModificationAdapter mod);
        void updateModificationHighlight(int start, int end);
        void resetModificationHighlight();
        void resetModWithPosSelection();

        void updateModWithPosSelection(Integer position);

        void updatePeptideHighlight(List<PeptideAdapter> peptideSelection);
        void resetPeptideHighlight();
        void displayLoadingMessage();
    }

    private static Logger logger = Logger.getLogger(CoveragePresenter.class.getName());


    /* TODO optimize the events that are doing the same to avoid triggering more that one the same event */
    private boolean hiding = true;
    private boolean justHighlighted = false;
    private Protein currentProtein;
    private Region currentRegion = Region.emptyRegion();
    private List<String> currentModifications = Collections.emptyList();
    private ModificationWithPosition currentModWithPos;
    private List<String> currentTissues = Collections.emptyList();

    private List<PeptideWithPeptiforms> peptideMatchSelection = Collections.emptyList();
    private List<Peptide> selectedPeptiforms = Collections.emptyList();

    //Needed to maintain temporary state while doing a selection
    private List<PeptideWithPeptiforms> tempPeptides = Collections.emptyList();

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
        eventBus.addHandler(ModificationWithPositionUpdateEvent.getType(), this);
        eventBus.addHandler(TissueUpdateEvent.getType(), this);
        eventBus.addHandler(PeptiformUpdateEvent.getType(), this);
    }

    // Callbacks that handle event bus events

    @Override
    public void onValidStateEvent(ValidStateEvent event) {
        if (event.getViewType() == ValidStateEvent.ViewType.Protein) {
            hiding = false;
            getView().asWidget().setVisible(true);
        } else {
            hiding = true;
            getView().asWidget().setVisible(false);
        }
    }

    @Override
    public void onProteinUpdateEvent(ProteinUpdateEvent event) {
        if (!hiding && event.getProteins().size() > 0) {
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

        if (event.getRegions().size() > 0) {
            region = event.getRegions().get(0);
            currentRegion = region;
            getView().updateRegionSelection(currentRegion.getStart(), currentRegion.getEnd());
        } else {
            currentRegion = Region.emptyRegion();
            getView().resetRegionSelection();
        }

        // Apparently when the region gets modified the peptide selection
        // gets reset, we must set it again manually.

        if (!peptideMatchSelection.isEmpty()) {
            selectionAdapters = new ArrayList<>();
            for (PeptideMatch match : peptideMatchSelection) {
                selectionAdapters.add(new PeptideAdapter(match));
            }
            getView().updatePeptideSelection(selectionAdapters);
        }
    }

    /**
     * This function selects all the peptide matches in the coverage view that
     * have the same sequence as the first peptide of the list that it's
     * received.
     *
     * @param event the event containing a list of peptides that got updated,
     *              we want to select them in the coverage view
     */
    @Override
    public void onPeptideUpdateEvent(PeptideUpdateEvent event) {
        List<PeptideAdapter> selectionAdapters;

        // we make a copy because if we don't it somehow it sometimes gets reset
        peptideMatchSelection = new ArrayList<>(event.getPeptides());
        tempPeptides = new ArrayList<>(event.getPeptides());

        if (event.getPeptides().size() > 0) {
            selectionAdapters = new ArrayList<>();

            for (PeptideMatch match : event.getPeptides()) {
                selectionAdapters.add(new PeptideAdapter(match));
            }

            getView().updatePeptideSelection(selectionAdapters);
        } else {
            getView().resetPeptideSelection();
        }


        // Apparently when the peptides gets modified the region, mods selection get reset
        // we must set it again manually.

        if (!currentRegion.isEmpty()) {
            getView().updateRegionSelection(currentRegion.getStart(), currentRegion.getEnd());
        }

        if (currentModWithPos!=null) {
            getView().updateModWithPosSelection(currentModWithPos.getPosition());
        }
    }

    @Override
    public void onPeptiformUpdateEvent(PeptiformUpdateEvent event) {
        selectedPeptiforms = event.getPeptiforms();
    }

    /**
     * This function only take into account that a modification is a type pf modification
     *
     * @param event event containing the string identifying a modification.
     */
    @Override
    public void onModificationUpdateEvent(ModificationUpdateEvent event) {
        currentModifications = event.getModifications();
        //This bit updates the modification triangles in the proteinCoverage part
        updatePeptideHighlight();

        if (!currentRegion.isEmpty()) {
            getView().updateRegionSelection(currentRegion.getStart(), currentRegion.getEnd());
        }
    }

    @Override
    public void onModificationUpdateEvent(ModificationWithPositionUpdateEvent event) {
        List<PeptideAdapter> selectionAdapters;

        if (event.getModifications().size() > 0) {
            currentModWithPos = event.getModifications().get(0);

        } else {
            currentModWithPos = null;
        }

        // Apparently when the modification gets modified the peptide selection
        // gets reset, we must set it again manually.

        if (!peptideMatchSelection.isEmpty()) {
            selectionAdapters = new ArrayList<>();
            for (PeptideMatch match : peptideMatchSelection) {
                selectionAdapters.add(new PeptideAdapter(match));
            }
            getView().updatePeptideSelection(selectionAdapters);
        }
    }

    @Override
    public void onTissueUpdateEvent(TissueUpdateEvent event) {
        currentTissues = event.getTissues();
        updatePeptideHighlight();
    }

    // Callbacks that handle user events from the view.
    @Override
    public void onRegionClickSelected(ProteinRegionSelectionEvent event) {
        onRegionClickSelected(event.getStart(), event.getLength());
    }

    @Override
    public void onRegionClickSelected(FeatureRegionSelectionEvent event) {
        onRegionClickSelected(event.getStart(), event.getLength());
    }

    @Override
    public void onRegionClickHighlighted(ProteinRegionHighlightEvent event) {
        // we don't do anything for the moment
    }

    @Override
    public void onRegionClickHighlighted(FeatureRegionHighlightEvent event) {
        // we don't do anything for the moment
    }
    @Override
    public void onRegionDragSelected(ProteinAreaSelectedEvent event) {
        onRegionDragSelected(event.isResetObjectSelection(), event.getStart(), event.getEnd());
    }

    @Override
    public void onRegionDragSelected(FeatureAreaSelectionEvent event) {
        onRegionDragSelected(event.isResetObjectSelection() ,event.getStart(), event.getEnd());
    }


    @Override
    public void onRegionDragHighlighted(ProteinAreaHighlightEvent event) {
        onRegionDragHighlighted(event.getStart(), event.getEnd());
    }

    @Override
    public void onRegionDragHighlighted(FeatureAreaHighlightEvent event) {
        onRegionDragHighlighted(event.getStart(), event.getEnd());
    }

    @Override
    public void onPeptideSelected(PeptideSelectedEvent event) {
        StateChanger changer = new StateChanger();
        List<Region> regions = new ArrayList<>();
        List<PeptideMatch> peptides = new ArrayList<>();
        List<ModificationWithPosition> auxModWithPos = new ArrayList<>();
        UserAction action;

        if (event.getPeptide() != null) {

            //Peptides
            PeptideMatch peptide = (PeptideMatch) event.getPeptide();
            peptides.add(peptide);

            //Regions
            if (!PeptideUtils.inRange(peptide, currentRegion.getStart(), currentRegion.getEnd())) {
                changer.addRegionChange(regions);
            }

            //Peptiforms
            List<Peptide> peptiforms = new ArrayList<>();
            for (Peptide peptiform : selectedPeptiforms) {
                if (peptide.getSequence().equals(peptiform.getSequence())) {
                    peptiforms.add(peptiform);
                }
            }

            if (!peptiforms.containsAll(selectedPeptiforms)) {
                changer.addPeptiformChange(peptiforms);
            }

            //Modifications
            List<String> modifications = PeptideUtils.extractModificationTypes(peptide);
            modifications.retainAll(currentModifications);
            if (!modifications.containsAll(currentModifications)) {
                changer.addModificationChange(modifications);
            }

            //We need to translate the modifications to protein coordinates
            //TODO Review
            List<ModificationWithPosition> modWithPos = PeptideUtils.extractModifications(peptide, currentProtein.getSequence().length());
            auxModWithPos.add(currentModWithPos);
            modWithPos.retainAll(auxModWithPos);
            if (!modWithPos.containsAll(auxModWithPos)) {
                changer.addModificationWithPositionChange(modWithPos);
            }

            //Tissues
            List<String> tissues = new ArrayList<>(peptide.getTissues());
            //We copy the list to avoid modifies the original ones
            tissues.retainAll(currentTissues);
            if (!tissues.containsAll(currentTissues)) {
                changer.addTissueChange(tissues);
            }

            action = new UserAction(UserAction.Type.peptide, "Click Set");
        } else {
            action = new UserAction(UserAction.Type.peptide, "Click Reset");
        }

        changer.addPeptideChange(peptides);
        StateChangingActionEvent.fire(this, changer, action);
    }

    @Override
    public void onModificationSelected(ModificationSelectedEvent event) {
        StateChanger changer = new StateChanger();
        List<Region> regions = new ArrayList<>();
        UserAction action;

        List<ModificationWithPosition> modWithPos = new ArrayList<>();

        //Regions
        //With the selection we reset the region if the modification is outside of the current one
        if(!currentRegion.isEmpty()) {
            if (event.getSite() < currentRegion.getStart() || event.getSite() > currentRegion.getEnd()) {
                changer.addRegionChange(regions);
            }
        }

        // Terminal modifications are ignored for now
        if (event.getSite() > 0 && event.getSite() < currentProtein.getSequence().length() + 1) {
            if(peptideMatchSelection.size() > 0) {
                List<PeptideMatch>  peptides = PeptideUtils.filterPeptideMatches(peptideMatchSelection,
                        event.getSite(), event.getSite(),
                        currentTissues, currentModifications);

                changer.addPeptideChange(peptides);
            }


            // We add the corresponding modification in this position because we can
            // use them to update the tissues filter gridView
            modWithPos.addAll(extractModifications(event.getModificationList()));

            //We can only select one modification with position at a time
            changer.addModificationWithPositionChange(modWithPos);
        }
        if(modWithPos.isEmpty()) {
            action = new UserAction(UserAction.Type.modificationWithPos, "Click Reset");
            StateChangingActionEvent.fire(this, changer, action);
        }
        else {
            action = new UserAction(UserAction.Type.modificationWithPos, "Click Set");
            StateChangingActionEvent.fire(this, changer, action);
        }
    }

    private List<ModificationWithPosition> extractModifications(List<ProteinModificationHandler> modifications) {
        List<ModificationWithPosition> result = new ArrayList<>();
        for (final ProteinModificationHandler modification : modifications) {
            result.add(new ModificationWithPosition(modification.getPrideModification().getName(), modification.getSite()));

        }

        return  Lists.newArrayList(result);
    }

    private void onRegionClickSelected(int eventRegionStart, int evenRegionLength){

        StateChanger changer = new StateChanger();
        List<Region> region = new ArrayList<>();
        List<PeptideMatch> peptides;

        // if the selection is done right to left then start > end
        int start = eventRegionStart;
        int end = eventRegionStart + evenRegionLength - 1;

        try {
            region.add(new Region(start, end));
            changer.addRegionChange(region);

            peptides = PeptideUtils.filterPeptideMatchesNotIn(peptideMatchSelection,
                    start, end);

            if (peptides.size() != peptideMatchSelection.size()) {
                changer.addPeptideChange(peptides);
            }

            UserAction action = new UserAction(UserAction.Type.region, "Click Set");
            StateChangingActionEvent.fire(this, changer, action);
        } catch (IllegalRegionValueException e) {
            // This is probably because of an empty selection,
            // we don't send any event
        }
    }

    //TODO Review
    private void onRegionDragSelected(boolean resetObjectSelection, int eventRegionStart, int evenRegionEnd) {
        StateChanger changer = new StateChanger();
        List<Region> regions = new ArrayList<>();
        UserAction action = UserAction.emptyAction();
        List<PeptideMatch> peptides;
        Region region;


        if(resetObjectSelection){
            action = new UserAction(UserAction.Type.region, "Drag Reset");
            region = Region.emptyRegion();
            changer.addModificationWithPositionChange(Collections.<ModificationWithPosition>emptyList());
            changer.addPeptideChange(Collections.<PeptideMatch>emptyList());
            regions.add(region);
            changer.addRegionChange(regions);
            StateChangingActionEvent.fire(this, changer, action);
        }
        else {
            // if the selection is done right to left then start > end
            int start = eventRegionStart < evenRegionEnd ? eventRegionStart : evenRegionEnd;
            int end = eventRegionStart + evenRegionEnd - start;

            try {
                region = new Region(start, end);
                // We have to check if a highlight has been done just before to
                // know if the user wants to reset or want to select a 1-site region
                if (region.getLength() == 0 && !justHighlighted) {
                    //we don't want to select a single aminoacid,
                    // we want to reset the selection
                    action = new UserAction(UserAction.Type.region, "Drag Reset");
                    region = Region.emptyRegion();
                    changer.addModificationWithPositionChange(Collections.<ModificationWithPosition>emptyList());

                } else {
                    action = new UserAction(UserAction.Type.region, "Drag Set");
                    justHighlighted = false;
                }


                peptides = PeptideUtils.filterPeptideMatchesNotIn(peptideMatchSelection, start, end);

                if (peptides.size() != peptideMatchSelection.size()) {
                    changer.addPeptideChange(peptides);
                }


            } catch (IllegalRegionValueException e) {
                action = new UserAction(UserAction.Type.region, "Drag Reset");
                region = Region.emptyRegion();
                regions.add(region);
            } finally {
                changer.addRegionChange(regions);
                StateChangingActionEvent.fire(this, changer, action);
            }
        }
    }

    private void onRegionDragHighlighted(int eventRegionStart, int evenRegionEnd) {
        List<Region> regions = new ArrayList<>();

        // if the selection is done right to left then start > end
        int start = eventRegionStart < evenRegionEnd ? eventRegionStart : evenRegionEnd;
        int end = eventRegionStart + evenRegionEnd - start;

        try {
            regions.add(new Region(start, end));
            justHighlighted = true;
        } catch (IllegalRegionValueException e) {
            regions.add(Region.emptyRegion());
        } finally {
            RegionUpdateEvent.fire(this, regions);

            // We have to send an update list of peptides that are selected with
            // the new list otherwise other widget might get desyncronized,
            // that's not good, this can be detected when the filtered list
            // has a different the size compared with the last temporary list
            // we stored.

            if (tempPeptides.size() !=
                    PeptideUtils.filterPeptideWithPeptiformsNotIn(peptideMatchSelection, start, end).size()) {
                tempPeptides = PeptideUtils.filterPeptideWithPeptiformsNotIn(peptideMatchSelection, start, end);

                PeptideUpdateEvent.fire(this, tempPeptides);

                // We should restore the peptiform IDs too in case they need to be reselected
                if (tempPeptides.size() == peptideMatchSelection.size()) {
                    PeptiformUpdateEvent.fire(this, selectedPeptiforms);
                }
            }
        }
    }

    private void updatePeptideHighlight() {
        List<PeptideMatch> peptides;
        List<PeptideAdapter> selectionAdapters;

        getView().resetPeptideHighlight();
        getView().resetModificationHighlight();

        if (!currentTissues.isEmpty() || !currentModifications.isEmpty()) {
            peptides = PeptideUtils.filterPeptideMatches(currentProtein.getPeptides(),
                    currentRegion.getStart(),currentRegion.getEnd(),
                    currentTissues, currentModifications);

            if (peptides.size() > 0) {
                selectionAdapters = new ArrayList<>();

                for (PeptideMatch match : peptides) {
                    selectionAdapters.add(new PeptideAdapter(match));
                }
                getView().updatePeptideHighlight(selectionAdapters);

                if (!currentModifications.isEmpty()) {
                    for (String currentModification : currentModifications) {
                        getView().updateModificationHighlight(new ModificationAdapter(currentModification));
                    }
                }
            }
        }
    }
}
