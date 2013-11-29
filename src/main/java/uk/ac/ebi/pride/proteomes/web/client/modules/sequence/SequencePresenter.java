package uk.ac.ebi.pride.proteomes.web.client.modules.sequence;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Region;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ModificationAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.PeptideAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.adapters.ProteinAdapter;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.ProteinRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.StateChangingActionEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.ValidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.ModificationUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.PeptideUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.ProteinUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.RegionUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.IllegalRegionValueException;
import uk.ac.ebi.pride.proteomes.web.client.modules.Presenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.history.StateChanger;
import uk.ac.ebi.pride.proteomes.web.client.utils.PeptideUtils;
import uk.ac.ebi.pride.widgets.client.sequence.events.ProteinRegionHighlightedEvent;
import uk.ac.ebi.pride.widgets.client.sequence.events.ProteinRegionSelectionEvent;

import java.util.*;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 27/11/13
 *         Time: 10:57
 */
public class SequencePresenter implements Presenter,
                                          ValidStateEvent.ValidStateHandler,
                                          ProteinUpdateEvent.ProteinUpdateHandler,
                                          ProteinRequestEvent.ProteinRequestHandler,
                                          RegionUpdateEvent.RegionUpdateHandler,
                                          PeptideUpdateEvent.PeptideUpdateHandler,
                                          ModificationUpdateEvent.ModificationUpdateHandler,
                                          SequenceUiHandler
{
    private boolean hiding = true;
    private Protein currentProtein;
    private Region currentRegion = Region.emptyRegion();
    private List<PeptideMatch> currentPeptides = Collections.emptyList();
    private String currentModification = "";

    public interface View extends uk.ac.ebi.pride.proteomes.web.client.modules.View<SequenceUiHandler> {
        void updateProtein(ProteinAdapter proteinAdapter);
        void updateRegionSelection(int start, int end);
        void resetRegionSelection();
        void updatePeptideSelection(List<PeptideAdapter> peptideSelection);
        void resetPeptideSelection();
        void updateModificationHighlight(ModificationAdapter mod);
        void resetModificationHighlight();
        void displayLoadingMessage();
    }

    private final EventBus eventBus;
    private final View view;

    public SequencePresenter(EventBus eventBus, View view) {
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
    }
    @Override
    public void bindToContainer(AcceptsOneWidget container) {
        view.bindToContainer(container);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEventFromSource(event, this);
    }

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
    public void onRegionHighlighted(ProteinRegionHighlightedEvent event) {
        List<Region> regions = new ArrayList<Region>();

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

    @Override
    public void onRegionUpdateEvent(RegionUpdateEvent event) {
        Region region;

        if(event.getRegions().size() > 0) {
            region = event.getRegions().get(0);
            currentRegion = region;
            view.updateRegionSelection(region.getStart(), region.getEnd());
        }
        else {
            currentRegion = Region.emptyRegion();
            view.resetRegionSelection();
        }
    }

    @Override
    public void onPeptideUpdateEvent(PeptideUpdateEvent event) {
        List<PeptideAdapter> selectionAdapters;
        List<PeptideMatch> selection;

        if(event.getPeptides().size() > 0 && event.getPeptides().size() > 0) {
            selectionAdapters = new ArrayList<PeptideAdapter>();
            selection = new ArrayList<PeptideMatch>();

            for(PeptideMatch match : currentProtein.getPeptides()) {
                if(match.getSequence().equals(event.getPeptides().get(0)
                        .getPeptideList().get(0).getSequence())) {
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

    @Override
    public void onRegionSelected(ProteinRegionSelectionEvent event) {
        StateChanger changer = new StateChanger();
        List<String> regions = new ArrayList<String>();
        Set<String> peptides;

        // if the selection is done right to left then start > end
        int start = event.getStart() < event.getEnd() ? event.getStart() : event.getEnd();
        int end = event.getStart() + event.getEnd() - start;

        try {
            regions.add(new Region(start, end).toString());
            changer.addRegionChange(regions);

            peptides = new HashSet<String>();
            for(PeptideMatch peptide : currentPeptides) {
                if(PeptideUtils.inRange(peptide, start, end)) {
                    peptides.add(peptide.getSequence());
                }
            }
            changer.addPeptideChange(peptides);
            StateChangingActionEvent.fire(this, changer);

        } catch (IllegalRegionValueException e) {
            regions.add("");
            changer.addRegionChange(regions);
            StateChangingActionEvent.fire(this, changer);
        }
    }
}
