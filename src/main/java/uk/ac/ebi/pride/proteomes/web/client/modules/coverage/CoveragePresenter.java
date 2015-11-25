package uk.ac.ebi.pride.proteomes.web.client.modules.coverage;

import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.UserAction;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.PeptideWithPeptiforms;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Region;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ModificationAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.PeptideAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ProteinAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.ModifiedLocation;
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
import uk.ac.ebi.pride.widgets.client.feature.events.FeatureAreaHighlightEvent;
import uk.ac.ebi.pride.widgets.client.feature.events.FeatureAreaSelectionEvent;
import uk.ac.ebi.pride.widgets.client.feature.events.FeatureRegionHighlightEvent;
import uk.ac.ebi.pride.widgets.client.feature.events.FeatureRegionSelectionEvent;
import uk.ac.ebi.pride.widgets.client.protein.events.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
                                          PeptiformUpdateEvent.Handler,
                                          TissueUpdateEvent.Handler {


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

    /* TODO optimize the events that are doing the same to avoid triggering more that one the same event */
    private boolean hiding = true;
    private boolean justHighlighted = false;
    private Protein currentProtein;
    private Region currentRegion = Region.emptyRegion();
    private List<String> currentModifications = Collections.emptyList();
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

        if (event.getSource() == this) {
            return;
        }

        if (event.getRegions().size() > 0) {
            region = event.getRegions().get(0);
            currentRegion = region;
            getView().updateRegionSelection(region.getStart(), region.getEnd());
            if (region.getLength() == 1) {
                getView().updateModificationHighlight(region.getStart(), region.getEnd());
            }
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

        if (event.getSource() == this) {
            return;
        }

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
    }

    @Override
    public void onPeptiformUpdateEvent(PeptiformUpdateEvent event) {
        selectedPeptiforms = event.getPeptiforms();
    }

    /**
     * This function has to take into account that a modification can be a
     * position or a type of modification
     *
     * @param event event containing the string identifying a modification.
     */
    @Override
    public void onModificationUpdateEvent(ModificationUpdateEvent event) {
        currentModifications = event.getModifications();
        if (!event.getModifications().isEmpty()) {
            for (String mod : event.getModifications()) {
                try {
                    int position = Integer.parseInt(mod);

                    getView().updateModificationHighlight(position, position);
                } catch (NumberFormatException e) {
                    getView().updateModificationHighlight(new ModificationAdapter(mod));
                }
            }
        } else {
            getView().resetModificationHighlight();
        }
    }

    @Override
    public void onTissueUpdateEvent(TissueUpdateEvent event) {
        currentTissues = event.getTissues();
    }


    // Callbacks that handle user events from the view.

    @Override
    public void onRegionClickSelected(ProteinRegionSelectionEvent event) {
        StateChanger changer = new StateChanger();
        List<Region> region = new ArrayList<>();
        List<PeptideMatch> peptides;

        try {
            region.add(new Region(event.getStart(), event.getStart() + event
                    .getLength() - 1));
            changer.addRegionChange(region);

            peptides = PeptideUtils.filterPeptideMatchesNotIn(peptideMatchSelection,
                    event.getStart(), event.getStart() + event.getLength() - 1);

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

    @Override
    public void onRegionClickSelected(FeatureRegionSelectionEvent event) {
        StateChanger changer = new StateChanger();
        List<Region> region = new ArrayList<>();
        List<PeptideMatch> peptides;

        try {
            region.add(new Region(event.getStart(), event.getStart() + event
                    .getLength() - 1));
            changer.addRegionChange(region);

            peptides = PeptideUtils.filterPeptideMatchesNotIn(peptideMatchSelection,
                    event.getStart(), event.getStart() + event.getLength() - 1);

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
        StateChanger changer = new StateChanger();
        List<Region> regions = new ArrayList<>();
        UserAction action = UserAction.emptyAction();
        List<PeptideMatch> peptides;
        Region region;

        // if the selection is done right to left then start > end
        int start = event.getStart() < event.getEnd() ? event.getStart() : event.getEnd();
        int end = event.getStart() + event.getEnd() - start;

        try {
            region = new Region(start, end);
            // We have to check if a highlight has been done just before to
            // know if the user wants to reset or want to select a 1-site region
            if (region.getLength() == 0 && !justHighlighted) {
                //we don't want to select a single aminoacid,
                // we want to reset the selection
                action = new UserAction(UserAction.Type.region, "Drag Reset");
                region = Region.emptyRegion();
            } else {
                action = new UserAction(UserAction.Type.region, "Drag Set");
                justHighlighted = false;
            }

            regions.add(region);

            peptides = PeptideUtils.filterPeptideMatchesNotIn(peptideMatchSelection,
                    start, end);

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

    @Override
    public void onRegionDragSelected(FeatureAreaSelectionEvent event) {
        StateChanger changer = new StateChanger();
        List<Region> regions = new ArrayList<>();
        UserAction action = UserAction.emptyAction();
        List<PeptideMatch> peptides;
        Region region;

        // if the selection is done right to left then start > end
        int start = event.getStart() < event.getEnd() ? event.getStart() : event.getEnd();
        int end = event.getStart() + event.getEnd() - start;

        try {
            region = new Region(start, end);
            // We have to check if a highlight has been done just before to
            // know if the user wants to reset or want to select a 1-site region
            if (region.getLength() == 0 && !justHighlighted) {
                //we don't want to select a single aminoacid,
                // we want to reset the selection
                action = new UserAction(UserAction.Type.region,
                        "Drag Reset");
                region = Region.emptyRegion();
            } else {
                action = new UserAction(UserAction.Type.region,
                        "Drag Set");
                justHighlighted = false;
            }

            regions.add(region);

            peptides = PeptideUtils.filterPeptideMatchesNotIn(peptideMatchSelection,
                    start, end);

            if (peptides.size() != peptideMatchSelection.size()) {
                changer.addPeptideChange(peptides);
            }


        } catch (IllegalRegionValueException e) {
            action = new UserAction(UserAction.Type.region,
                    "Drag Reset");
            region = Region.emptyRegion();
            regions.add(region);
        } finally {
            changer.addRegionChange(regions);
            StateChangingActionEvent.fire(this, changer, action);
        }
    }


    @Override
    public void onRegionDragHighlighted(ProteinAreaHighlightEvent event) {
        List<Region> regions = new ArrayList<>();

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

    @Override
    public void onRegionDragHighlighted(FeatureAreaHighlightEvent event) {
        List<Region> regions = new ArrayList<>();

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


    @Override
    public void onPeptideSelected(PeptideSelectedEvent event) {
        StateChanger changer = new StateChanger();
        List<Region> regions = new ArrayList<>();
        List<PeptideMatch> peptides = new ArrayList<>();
        UserAction action = new UserAction(UserAction.Type.peptide,
                "Click Set");

        PeptideAdapter peptide = (PeptideAdapter) event.getPeptide();
        peptides.add(peptide);
        changer.addPeptideChange(peptides);

        // If the peptide doesn't fit the region that's selected we ought to
        // change the region too.
        if (!currentRegion.isEmpty() &&
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

        List<Peptide> peptiforms = new ArrayList<>();
        for (Peptide peptiform : selectedPeptiforms) {
            if (peptide.getSequence().equals(peptiform.getSequence())) {
                peptiforms.add(peptiform);
            }
        }

        if (!peptiforms.containsAll(selectedPeptiforms)) {
            changer.addPeptiformChange(peptiforms);
        }

        boolean contained = false;
        for (ModifiedLocation modLoc : peptide.getModifiedLocations()) {
            for (String mod : currentModifications) {
                try {
                    Integer.parseInt(mod);
                } catch (NumberFormatException e) {
                    if (modLoc.getModification().equals(mod)) {
                        contained = true;
                        break;
                    }
                }
            }
        }
        if (!contained) {
            // We need to remove all filtering modifications
            List<String> correctMods = new ArrayList<>();
            for (String mod : currentModifications) {
                try {
                    Integer.parseInt(mod);
                    correctMods.add(mod);

                } catch (NumberFormatException ignore) {
                }
            }

            if (correctMods.size() < currentModifications.size()) {
                changer.addModificationChange(correctMods);
            }
        }
        contained = false;
        for (String pepTissue : peptide.getTissues()) {
            for (String tissue : currentTissues) {
                if (tissue.equals(pepTissue)) {
                    contained = true;
                    break;
                }
            }
        }
        if (!contained) {
            if (!currentTissues.isEmpty()) {
                changer.addTissueChange(new ArrayList<String>());
            }
        }

        StateChangingActionEvent.fire(this, changer, action);
    }

    @Override
    public void onModificationSelected(ModificationSelectedEvent event) {
        StateChanger changer = new StateChanger();
        List<Region> regions = new ArrayList<>();
        List<String> modifications = new ArrayList<>();
        UserAction action = new UserAction(UserAction.Type.modification,
                "Click Set");

        // Terminal modifications are ignored for now
        if (event.getSite() > 0 && event.getSite() < currentProtein
                .getSequence().length() + 1) {
            if (event.getSite() < currentRegion.getStart() ||
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
