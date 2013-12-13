package uk.ac.ebi.pride.proteomes.web.client.modules.coverage;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.UserAction;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Region;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ModificationAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ProteinAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.PeptideAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.ProteinRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.StateChangingActionEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.ValidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.*;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.IllegalRegionValueException;
import uk.ac.ebi.pride.proteomes.web.client.modules.Presenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.history.StateChanger;
import uk.ac.ebi.pride.proteomes.web.client.utils.PeptideUtils;
import uk.ac.ebi.pride.widgets.client.protein.events.*;

import java.util.*;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 08/11/13
 *         Time: 14:43
 */
public class CoveragePresenter implements Presenter,
                                          ValidStateEvent.ValidStateHandler,
                                          ProteinUpdateEvent.ProteinUpdateHandler,
                                          ProteinRequestEvent.ProteinRequestHandler,
                                          RegionUpdateEvent.RegionUpdateHandler,
                                          PeptideUpdateEvent.PeptideUpdateHandler,
                                          ModificationUpdateEvent.ModificationUpdateHandler,
                                          CoverageUiHandler, VarianceUpdateEvent.VarianceUpdateHandler {
    public interface View extends uk.ac.ebi.pride.proteomes.web.client.modules.View<CoverageUiHandler> {
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

    private final EventBus eventBus;
    private final View view;

    private boolean hiding = true;
    private boolean justHighlighted = false;
    private Protein currentProtein;
    private Region currentRegion = Region.emptyRegion();
    private List<PeptideMatch> currentPeptides = Collections.emptyList();
    private Collection<String> selectedVarianceIDs = Collections.emptyList();
    private String currentModification = "";

    public CoveragePresenter(EventBus eventBus, View view) {
        this.eventBus = eventBus;
        this.view = view;

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

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEventFromSource(event, this);
    }

    @Override
    public void bindToContainer(AcceptsOneWidget container) {
        view.bindToContainer(container);
    }

    // Callbacks that handle event bus events

    @Override
    public void onValidStateEvent(ValidStateEvent event) {
        if(event.getViewType() == ValidStateEvent.ViewType.Protein) {
            hiding = false;
            view.asWidget().setVisible(true);
        }
        else {
            hiding = true;
            view.asWidget().setVisible(false);
        }
    }

    @Override
    public void onProteinUpdateEvent(ProteinUpdateEvent event) {
        if(!hiding && event.getProteins().size() > 0) {
            currentProtein = event.getProteins().get(0);
            view.updateProtein(new ProteinAdapter(currentProtein));
        }
    }

    @Override
    public void onProteinRequestEvent(ProteinRequestEvent event) {
        view.displayLoadingMessage();
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
            view.updateRegionSelection(region.getStart(), region.getEnd());
            if(region.getLength() == 1) {
                view.updateModificationHighlight(region.getStart(), region.getEnd());
            }
        }
        else {
            currentRegion = Region.emptyRegion();
            view.resetRegionSelection();
        }

        // Apparently when the region gets modified the peptide selection
        // gets reset, we must set it again manually.

        if(!currentPeptides.isEmpty()) {
            selectionAdapters = new ArrayList<PeptideAdapter>();
            for(PeptideMatch match : currentPeptides) {
                selectionAdapters.add(new PeptideAdapter(match));
            }
            view.updatePeptideSelection(selectionAdapters);
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
        List<Peptide> eventPeptides;

        if(event.getPeptides().size() > 0 && event.getPeptides().size() > 0) {
            selectionAdapters = new ArrayList<PeptideAdapter>();
            selection = new ArrayList<PeptideMatch>();

            eventPeptides = event.getPeptides().get(0).getPeptideList();
            for(PeptideMatch match : currentProtein.getPeptides()) {
                if(match.getSequence().equals(eventPeptides.get(0).getSequence())) {
                    selectionAdapters.add(new PeptideAdapter(match));
                    selection.add(match);
                }
            }
            currentPeptides = selection;
            view.updatePeptideSelection(selectionAdapters);
        }
        else {
            view.resetPeptideSelection();
            currentPeptides = Collections.emptyList();
        }
    }

    @Override
    public void onVarianceUpdateEvent(VarianceUpdateEvent event) {
        selectedVarianceIDs = Arrays.asList(event.getVarianceIDs());
    }

    /**
     * This function has to take into account that a modification can be a
     * position or a type of modification
     * @param event
     */
    @Override
    public void onModificationUpdateEvent(ModificationUpdateEvent event) {
        if(event.getModifications().length > 0) {
            currentModification = event.getModifications()[0];
            try {
                int position = Integer.parseInt(currentModification);

                view.updateModificationHighlight(position, position);
            }
            catch (NumberFormatException e) {
                view.updateModificationHighlight(new ModificationAdapter(currentModification));
            }
        }
        else {
            view.resetModificationHighlight();
            currentModification = null;
        }
    }

    // Callbacks that handle user events from the view.

    @Override
    public void onRegionClickSelected(ProteinRegionSelectionEvent event) {
        StateChanger changer = new StateChanger();
        List<String> region = new ArrayList<String>();
        Set<String> peptides;

        try {
            region.add(new Region(event.getStart(), event.getStart() + event
                    .getLength() - 1).toString());
            changer.addRegionChange(region);

            peptides = new HashSet<String>();
            for(PeptideMatch peptide : currentPeptides) {
                if(PeptideUtils.inRange(peptide, event.getStart(),
                        event.getStart() + event.getLength() - 1)) {
                    peptides.add(peptide.getSequence());
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
        List<String> regions = new ArrayList<String>();
        UserAction action = UserAction.emptyAction();
        Set<String> peptides;
        Region region;

        // if the selection is done right to left then start > end
        int start = event.getStart() < event.getEnd() ? event.getStart() : event.getEnd();
        int end = event.getStart() + event.getEnd() - start;

        try {
            region = new Region(start, end);
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

            regions.add(region.toString());

            if(!region.isEmpty()) {
                peptides = new HashSet<String>();
                for(PeptideMatch peptide : currentPeptides) {
                    if(PeptideUtils.inRange(peptide, start, end)) {
                        peptides.add(peptide.getSequence());
                    }
                }
                changer.addPeptideChange(peptides);
            }
        } catch (IllegalRegionValueException e) {
            action = new UserAction(UserAction.Type.region,
                    "Drag Coverage Reset");
            region = Region.emptyRegion();
            regions.add(region.toString());
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
        }
    }

    @Override
    public void onPeptideSelected(PeptideSelectedEvent event) {
        StateChanger changer = new StateChanger();
        List<String> regions = new ArrayList<String>();
        List<String> peptides = new ArrayList<String>();
        UserAction action = new UserAction(UserAction.Type.peptide,
                                           "Click Set");

        PeptideAdapter peptide = (PeptideAdapter) event.getPeptide();

        // If the peptide doesn't fit the region that's selected we ought to
        // change the region too.
        if(!currentRegion.isEmpty() &&
                !PeptideUtils.inRange(peptide, currentRegion.getStart(),
                currentRegion.getEnd())) {
            try {
                regions.add(new Region(peptide.getSite(), peptide.getEnd()).toString());
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
        peptides.add(peptide.getSequence());
        changer.addPeptideChange(peptides);

        StateChangingActionEvent.fire(this, changer, action);
    }

    @Override
    public void onModificationSelected(ModificationSelectedEvent event) {
        StateChanger changer = new StateChanger();
        List<String> regions = new ArrayList<String>();
        List<String> modifications = new ArrayList<String>();
        UserAction action = new UserAction(UserAction.Type.modification,
                                           "Click Set");

        // Terminal modifications are ignored for now
        if(event.getSite() > 0 && event.getSite() < currentProtein
                .getSequence().length() + 1) {
            // The region only gets changed if the modification doesn't fit
            // in it.
            if(event.getSite() < currentRegion.getStart() ||
               event.getSite() > currentRegion.getEnd()) {
                try {
                    regions.add(new Region(event.getSite(), event.getSite()).toString());
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
