package uk.ac.ebi.pride.proteomes.web.client.modules.coverage;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Region;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ModificationAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ProteinAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.PeptideAdapter;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
                                          CoverageUiHandler
{
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
    private Protein currentProtein;
    private Region currentRegion = Region.emptyRegion();
    private List<PeptideMatch> currentPeptides = Collections.emptyList();
    private String currentModification = "";

    public CoveragePresenter(EventBus eventBus, View view) {
        this.eventBus = eventBus;
        this.view = view;

        view.addUiHandler(this);

        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(ProteinUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinRequestEvent.getType(), this);
        eventBus.addHandler(RegionUpdateEvent.getType(), this);
        eventBus.addHandler(PeptideUpdateEvent.getType(), this);
        eventBus.addHandler(ModificationUpdateEvent.getType(), this);
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
            view.setVisible(true);
        }
    }

    @Override
    public void onProteinRequestEvent(ProteinRequestEvent event) {
        view.displayLoadingMessage();
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

        if(event.getPeptides().size() > 0 && event.getPeptides().size() > 0) {
            selectionAdapters = new ArrayList<PeptideAdapter>();
            selection = new ArrayList<PeptideMatch>();

            for(PeptideMatch match : currentProtein.getPeptides()) {
                if(match.getSequence().equals(event.getPeptides().get(0)
                        .getVariances().get(0).getSequence())) {
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
    public void onRegionUpdateEvent(RegionUpdateEvent event) {
        Region region;

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
    }

    @Override
    public void onModificationUpdateEvent(ModificationUpdateEvent event) {
        if(event.getModifications().length > 0) {
            currentModification = event.getModifications()[0];
            view.updateModificationHighlight(
                    new ModificationAdapter(event.getModifications()[0]));
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

        try {
            region.add(new Region(event.getStart(), event.getStart() + event
                    .getLength() - 1).toString());
            changer.addRegionChange(region);

            StateChangingActionEvent.fire(this, changer);
        } catch (IllegalRegionValueException e) {
            // This is probably because of an empty selection,
            // we don't send any event
        }
    }

    @Override
    public void onRegionDragSelected(ProteinAreaSelectedEvent event) {
        StateChanger changer = new StateChanger();
        List<String> regions = new ArrayList<String>();
        // if the selection is done right to left then start > end
        int start = event.getStart() < event.getEnd() ? event.getStart() : event.getEnd();
        int end = event.getStart() + event.getEnd() - start;

        try {
            regions.add(new Region(start, end).toString());
            changer.addRegionChange(regions);

            StateChangingActionEvent.fire(this, changer);
        } catch (IllegalRegionValueException e) {
            // Empty selection, we don't send any event
        }
    }

    @Override
    public void onRegionClickHighlighted(ProteinRegionHighlightEvent event) {
        // todo We don't do anything right now, we should work on getting the
        // application to a functional state first
    }

    @Override
    public void onRegionDragHighlighted(ProteinAreaHighlightEvent event) {
        // todo We don't do anything right now, we should work on getting the
        // application to a functional state first
    }

    @Override
    public void onPeptideSelected(PeptideSelectedEvent event) {
        StateChanger changer = new StateChanger();
        List<String> regions = new ArrayList<String>();
        List<String> peptides = new ArrayList<String>();

        PeptideAdapter peptide = (PeptideAdapter) event.getPeptide();

        // If the peptide doesn't fit the region that's selected we ought to
        // change the region too.
        if(!PeptideUtils.inRange(peptide, currentRegion.getStart(),
                currentRegion.getEnd())) {
            try {
                regions.add(new Region(peptide.getSite(), peptide.getEnd()).toString());
                changer.addRegionChange(regions);
            } catch (IllegalRegionValueException e) {
                // this shouldn't happen
            }
        }
        peptides.add(peptide.getSequence());
        changer.addPeptideChange(peptides);

        StateChangingActionEvent.fire(this, changer);
    }

    @Override
    public void onModificationSelected(ModificationSelectedEvent event) {
        StateChanger changer = new StateChanger();
        List<String> regions = new ArrayList<String>();
        List<String> modifications = new ArrayList<String>();

        // Terminal modifications are ignored for now
        if(event.getSite() > 0 && event.getSite() < currentProtein
                .getSequence().length() + 1) {
        try {

            regions.add(new Region(event.getSite(), event.getSite() + 1).toString());
            changer.addRegionChange(regions);
        } catch (IllegalRegionValueException e) {
            // this shouldn't happen
        }

        modifications.add(event.getSite().toString());
        changer.addModificationChange(modifications);
        StateChangingActionEvent.fire(this, changer);

        }
    }
}
