package uk.ac.ebi.pride.proteomes.web.client.modules.peptides;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Region;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.ProteinRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.StateChangingActionEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.ValidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.PeptideUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.ProteinUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.RegionUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.TissueUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.IllegalRegionValueException;
import uk.ac.ebi.pride.proteomes.web.client.modules.Presenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.history.StateChanger;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListUiHandler;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListView;
import uk.ac.ebi.pride.proteomes.web.client.utils.PeptideUtils;

import java.util.*;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 12/11/13
 *         Time: 09:40
 */
public class PeptidesPresenter implements Presenter,
                                          ValidStateEvent.ValidStateHandler,
                                          ProteinUpdateEvent.ProteinUpdateHandler,
                                          ProteinRequestEvent.ProteinRequestHandler,
                                          RegionUpdateEvent.RegionUpdateHandler,
                                          PeptideUpdateEvent.PeptideUpdateHandler,
                                          ListUiHandler<PeptideMatch>,TissueUpdateEvent.TissueUpdateHandler {
    private final EventBus eventBus;
    private final ListView<PeptideMatch> view;
    private final ListDataProvider<PeptideMatch> dataProvider = new
                                            ListDataProvider<PeptideMatch>();
    private final ColumnSortEvent.ListHandler<PeptideMatch> dataSorter = new
                                            ColumnSortEvent.ListHandler<PeptideMatch>(new ArrayList<PeptideMatch>());

    private boolean groups = true;
    private Protein currentProtein;
    private Region currentRegion = Region.emptyRegion();
    private Collection<PeptideMatch> selectedPeptidesMatches = Collections.emptyList();
    private String currentTissue = "";

    public PeptidesPresenter(EventBus eventBus, ListView<PeptideMatch> view) {
        this.eventBus = eventBus;
        this.view = view;
        List<Column<PeptideMatch, ?>> columns = PeptideColumnProvider
                                            .getSortingColumns(dataSorter);
        List<String> columnTitles = PeptideColumnProvider.getColumnTitles();
        List<String> columnWidths = PeptideColumnProvider.getColumnWidths();

        view.addDataProvider(dataProvider);
        view.addColumns(columns, columnTitles, columnWidths);
        view.addColumnSortHandler(dataSorter);
        view.addUiHandler(this);
        view.asWidget().setVisible(false);

        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(ProteinUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinRequestEvent.getType(), this);
        eventBus.addHandler(RegionUpdateEvent.getType(), this);
        eventBus.addHandler(PeptideUpdateEvent.getType(), this);
        eventBus.addHandler(TissueUpdateEvent.getType(), this);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEventFromSource(event, this);
    }

    @Override
    public void bindToContainer(AcceptsOneWidget container) {
        view.bindToContainer(container);
    }

    @Override
    public void onValidStateEvent(ValidStateEvent event) {
        // We should check if we have to stay hidden or not
        if(event.getViewType() == ValidStateEvent.ViewType.Group) {
            groups = true;
            view.asWidget().setVisible(false);
        }
        else {
            groups = false;
            view.asWidget().setVisible(true);
        }
    }

    @Override
    public void onProteinUpdateEvent(ProteinUpdateEvent event) {
        // We need to extract a peptide list from the ones in the proteins
        if(!groups && event.getProteins().size() > 0) {
            currentProtein = event.getProteins().get(0);
            try {
                currentRegion = new Region(1, currentProtein.getSequence()
                        .length());
            } catch (IllegalRegionValueException e) {
            }
            // we should reset filters and ordering here
            updateList(currentProtein.getPeptides());
            view.showList();
        }
    }

    @Override
    public void onProteinRequestEvent(ProteinRequestEvent event) {
        // We should display that the list is being loaded
        if(!groups) {
            view.showLoadingMessage();
        }
    }

    @Override
    public void onRegionUpdateEvent(RegionUpdateEvent event) {
        // we deselect all the peptides, we can select them again.
        for(Peptide peptide : selectedPeptidesMatches) {
            deselectItem(peptide);
        }

        // we change the peptide list
        if(event.getRegions().size() == 0) {
            try {
                currentRegion = new Region(1, currentProtein.getSequence().length());
            } catch (IllegalRegionValueException e) {
            }
            updateList(currentProtein.getPeptides());
        }
        else {
            currentRegion = event.getRegions().get(0);
            updateList(PeptideUtils.filterPeptidesNotInTissue(
                       PeptideUtils.filterPeptidesNotIn(currentProtein.getPeptides(),
                             currentRegion.getStart(), currentRegion.getEnd()),
                       currentTissue));
        }

        // we reselect the peptides, this is because the selected peptides
        // might not change when reselecting the region.
        selectPeptides();
    }

    @Override
    public void onPeptideUpdateEvent(PeptideUpdateEvent event) {
        for(Peptide peptide : selectedPeptidesMatches) {
            deselectItem(peptide);
        }

        // we don't care about all the modifications, so we get rid of them
        // and pick a single variance per list of peptides.
        Collection<Peptide> selectedPepVars = PeptideUtils.getFirstOfEach(event.getPeptides());
        selectedPeptidesMatches = new ArrayList<PeptideMatch>();
        for(Peptide pepVariance : selectedPepVars) {
            for(PeptideMatch pepMatch : currentProtein.getPeptides()) {
                if(pepVariance.getSequence().equals(pepMatch.getSequence())) {
                    selectedPeptidesMatches.add(pepMatch);
                    break;
                }
            }
        }

        if(event.getPeptides().size() > 0) {
            // we reselect the peptides only if there are any
            selectPeptides();
        }
    }

    @Override
    public void onTissueUpdateEvent(TissueUpdateEvent event) {
        // we deselect all the peptides, we can select them again.
        for(Peptide peptide : selectedPeptidesMatches) {
            deselectItem(peptide);
        }

        if(event.getTissues().length > 0 && !event.getTissues()[0].equals("")) {
            currentTissue = event.getTissues()[0];
            updateList(PeptideUtils.filterPeptidesNotInTissue(
                       PeptideUtils.filterPeptidesNotIn(currentProtein.getPeptides(),
                               currentRegion.getStart(),currentRegion.getEnd()),
                        currentTissue));
        }
        else {
            currentTissue = "";
            updateList(PeptideUtils.filterPeptidesNotIn(currentProtein.getPeptides(),
                    currentRegion.getStart(),currentRegion.getEnd()));
        }
    }

    @Override
    public void onSelectionChanged(Collection<PeptideMatch> items) {
        StateChanger changer;

        // an empty selection is represented by a list with a null items,
        // we represent that with an empty list, so we have to add an
        // additional check for that.
        if((items.containsAll(selectedPeptidesMatches) &&
            selectedPeptidesMatches.containsAll(items)) ||
                (items.contains(null) && selectedPeptidesMatches.isEmpty())) {
            return;
        } else if(items.contains(null)) {
            items = Collections.emptyList();
        }

        Set<String> peptideIds = new HashSet<String>();

        for(PeptideMatch peptide : items) {
            peptideIds.add(peptide.getSequence());
        }

        changer = new StateChanger();
        changer.addPeptideChange(peptideIds);
        StateChangingActionEvent.fire(this, changer);
    }

    private void updateList(List<PeptideMatch> peptideList) {
        for(Peptide peptide : selectedPeptidesMatches) {
            deselectItem(peptide);
        }

        setList(peptideList);

        for(Peptide peptide : selectedPeptidesMatches) {
            selectItem(peptide);
        }
    }

    private void selectItem(Peptide peptide) {
        // search the first occurrence of the peptide, we can only select
        // one because of the selection model
        int peptidePosition = PeptideUtils.firstIndexOf(dataProvider.getList
                (), peptide.getSequence());

        if(peptidePosition > -1) {
            view.selectItemOn(peptidePosition);
        }
    }

    private void deselectItem(Peptide peptide) {
        // search the first occurrence of the peptide, we can only select
        // one because of the selection model
        int peptidePosition = PeptideUtils.firstIndexOf(dataProvider.getList
                (), peptide.getSequence());

        if(peptidePosition > -1) {
            view.deselectItemOn(peptidePosition);
        }
    }

    private void selectPeptides() {
        //we reselect the peptides inside the range, to do this,
        // we must search first the first peptide match that has the same
        // sequence as the peptide we want to select.
        for(Peptide peptide : selectedPeptidesMatches) {
            int peptidePosition = PeptideUtils.firstIndexOf(dataProvider.getList(),
                    peptide.getSequence());
            if(peptidePosition != -1) {
            if(PeptideUtils.inRange(dataProvider.getList().get(peptidePosition),
                    currentRegion.getStart(),
                    currentRegion.getEnd())) {
                selectItem(peptide);
            }
            }
        }
    }

    /**
     * This method is used whenever a new list is set as the model of the view.
     * We have to update the list reference in the provider and the sorter,
     * otherwise the sorting would not work anymore.
     *
     * Alternatively we could also empty the list and repopulate it with the
     * new data, but that would make it slower.
     */
    private void setList(final List<PeptideMatch> peptideList) {
        dataProvider.setList(peptideList);
        dataSorter.setList(peptideList);
    }
}
