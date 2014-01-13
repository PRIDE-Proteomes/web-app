package uk.ac.ebi.pride.proteomes.web.client.modules.peptides;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.view.client.*;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.UserAction;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Region;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.ProteinRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.StateChangingActionEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.ValidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.*;
import uk.ac.ebi.pride.proteomes.web.client.modules.Presenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.history.StateChanger;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListSorter;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListUiHandler;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListView;
import uk.ac.ebi.pride.proteomes.web.client.utils.PeptideUtils;

import java.util.*;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 12/11/13
 *         Time: 09:40
 */
public class PeptidesPresenter extends Presenter<ListView<PeptideMatch>>
                               implements ListUiHandler<PeptideMatch>,
                                          ValidStateEvent.Handler,
                                          ProteinUpdateEvent.Handler,
                                          ProteinRequestEvent.Handler,
                                          RegionUpdateEvent.Handler,
                                          PeptideUpdateEvent.Handler,
                                          TissueUpdateEvent.Handler,
                                          ModificationUpdateEvent.Handler,
                                          VarianceUpdateEvent.Handler {
    private final ListDataProvider<PeptideMatch> dataProvider = new
                                            ListDataProvider<PeptideMatch>();
    private final ListSorter<PeptideMatch> dataSorter = new
                                    ListSorter<PeptideMatch>(new ArrayList<PeptideMatch>());

    private boolean groups = true;
    private Protein currentProtein;
    private Region currentRegion = Region.emptyRegion();
    private Collection<PeptideMatch> selectedPeptidesMatches = Collections.emptyList();
    private List<String> currentTissues = Collections.emptyList();
    private List<String> currentModifications = Collections.emptyList();
    private List<String> selectedVariancesIDs = Collections.emptyList();

    private boolean selectionEventsDisabled = false;

    public PeptidesPresenter(EventBus eventBus, ListView<PeptideMatch> view) {
        super(eventBus, view);
        List<Column<PeptideMatch, ?>> columns = PeptideColumnProvider
                                            .getSortingColumns(dataSorter);
        List<String> columnTitles = PeptideColumnProvider.getColumnTitles();
        List<String> columnWidths = PeptideColumnProvider.getColumnWidths();

        dataSorter.setList(dataProvider.getList());
        view.addDataProvider(dataProvider);
        view.addColumns(columns, columnTitles, columnWidths);
        view.addColumnSortHandler(dataSorter);
        view.addUiHandler(this);
        view.asWidget().setVisible(false);

        // We define how are the items selected here
        final SingleSelectionModel<PeptideMatch> selectionModel = new
                SingleSelectionModel<PeptideMatch>();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if(!selectionEventsDisabled) {
                    Set<PeptideMatch> selection = new HashSet<PeptideMatch>();
                    selection.add(selectionModel.getSelectedObject());
                    for(ListUiHandler<PeptideMatch> handler : getView().getUiHandlers()) {
                        handler.onSelectionChanged(selection);
                    }
                }
            }
        });

        view.setSelectionModel(selectionModel);
        view.setKeyboardSelectionPolicy(PeptideColumnProvider.getKeyboardSelectionPolicy());

        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(ProteinUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinRequestEvent.getType(), this);
        eventBus.addHandler(RegionUpdateEvent.getType(), this);
        eventBus.addHandler(PeptideUpdateEvent.getType(), this);
        eventBus.addHandler(VarianceUpdateEvent.getType(), this);
        eventBus.addHandler(TissueUpdateEvent.getType(), this);
        eventBus.addHandler(ModificationUpdateEvent.getType(), this);
    }

    @Override
    public void onValidStateEvent(ValidStateEvent event) {
        // We should check if we have to stay hidden or not
        if(event.getViewType() == ValidStateEvent.ViewType.Group) {
            groups = true;
            getView().asWidget().setVisible(false);
        }
        else {
            groups = false;
            getView().asWidget().setVisible(true);
        }
    }

    @Override
    public void onProteinUpdateEvent(ProteinUpdateEvent event) {
        // We need to extract a peptide list from the ones in the proteins
        if(!groups && event.getProteins().size() > 0) {
            currentProtein = event.getProteins().get(0);
            currentRegion = Region.emptyRegion();
            // we should reset filters and ordering here
            updateList(currentProtein.getPeptides());
            getView().loadList();
            getView().showContent();
        }
    }

    @Override
    public void onProteinRequestEvent(ProteinRequestEvent event) {
        // We should display that the list is being loaded
        if(!groups) {
            getView().loadLoadingMessage();
        }
    }

    @Override
    public void onRegionUpdateEvent(RegionUpdateEvent event) {
        // we change the peptide list
        if(event.getRegions().size() == 0) {
            currentRegion = Region.emptyRegion();
        }
        else {
            currentRegion = event.getRegions().get(0);
        }

        updateList(PeptideUtils.filterPeptides(currentProtein.getPeptides(),
                currentRegion.getStart(), currentRegion.getEnd(),
                currentTissues, currentModifications));
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

        if(!selectedPeptidesMatches.isEmpty()) {
            // we reselect the peptides only if there are any
            selectPeptides();
        }
    }

    @Override
    public void onVarianceUpdateEvent(VarianceUpdateEvent event) {
        selectedVariancesIDs = Arrays.asList(event.getVarianceIDs());
    }

    @Override
    public void onTissueUpdateEvent(TissueUpdateEvent event) {
        currentTissues = new ArrayList<String>();
        for(String tissue : event.getTissues()) {
            if(!tissue.equals("")) {
                currentTissues.add(tissue);
            }
        }
        updateList(PeptideUtils.filterPeptides(currentProtein.getPeptides(),
                currentRegion.getStart(), currentRegion.getEnd(),
                currentTissues, currentModifications));
    }

    /**
     * If the modification is in a position the modification filter needs to
     * get reset.
     * @param event Event containing the string identifying a type of
     *              modification.
     */
    @Override
    public void onModificationUpdateEvent(ModificationUpdateEvent event) {
        currentModifications = new ArrayList<String>();
        for(String mod : event.getModifications()) {
            if(!mod.equals("")) {
                try {
                    Integer.parseInt(mod);
                    currentModifications.add("");
                }
                catch(NumberFormatException e) {
                    currentModifications.add(mod);
                }
            }
        }
        updateList(PeptideUtils.filterPeptides(currentProtein.getPeptides(),
                currentRegion.getStart(), currentRegion.getEnd(),
                currentTissues, currentModifications));
    }

    @Override
    public void onSelectionChanged(Collection<PeptideMatch> items) {
        StateChanger changer;
        UserAction action;

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
        Set<String> varianceIDs = new HashSet<String>();

        for(PeptideMatch peptide : items) {
            peptideIds.add(peptide.getSequence());
        }

        // we should change the variance selection if it doesn't fit the
        // current peptide selection

        for(String varianceID : selectedVariancesIDs) {
            if(peptideIds.contains(varianceID.split("[|]")[0].substring(1))) {
                varianceIDs.add(varianceID);
            }
        }

        changer = new StateChanger();
        changer.addPeptideChange(peptideIds);
        if(!varianceIDs.containsAll(selectedVariancesIDs)) {
            changer.addVarianceChange(varianceIDs);
        }

        if(items.isEmpty()) {
            action = new UserAction(UserAction.Type.peptide, "Click Reset");
        }
        else {
            action = new UserAction(UserAction.Type.peptide, "Click Set");
        }

        StateChangingActionEvent.fire(this, changer, action);
    }

    private void updateList(List<PeptideMatch> peptideList) {
        for(Peptide peptide : selectedPeptidesMatches) {
            deselectItem(peptide);
        }

        setList(peptideList);

        selectedPeptidesMatches.retainAll(peptideList);
        for(Peptide peptide : selectedPeptidesMatches) {
            selectItem(peptide);
        }
    }

    private void selectItem(Peptide peptide) {
        // search the first occurrence of the peptide, we can only select
        // one because of the selection model
        int peptidePosition = PeptideUtils.firstIndexWithSequence(dataProvider.getList
                (), peptide.getSequence());

        if(peptidePosition > -1) {
            selectionEventsDisabled = true;
            getView().selectItemOn(peptidePosition);
            selectionEventsDisabled = false;
        }
    }

    private void deselectItem(Peptide peptide) {
        // search the first occurrence of the peptide, we can only select
        // one because of the selection model
        int peptidePosition = PeptideUtils.firstIndexWithSequence(dataProvider.getList
                (), peptide.getSequence());

        if(peptidePosition > -1) {
            selectionEventsDisabled = true;
            getView().deselectItemOn(peptidePosition);
            selectionEventsDisabled = false;
        }
    }

    private void selectPeptides() {
        //we reselect the peptides inside the range, to do this,
        // we must search first the first peptide match that has the same
        // sequence as the peptide we want to select.
        // We assume that if a peptide is in the selected list it already
        // passes all the filters
        for(Peptide peptide : selectedPeptidesMatches) {
            selectItem(peptide);
        }
    }

    /**
     * This method is used whenever a new list is set as the model of the view.
     *
     * We clear the list and repopulate it because if we simply reset the
     * data provider and data sorter references to the list it won't work.
     * We also sort the list afterwards so the list maintains the same order
     * as the user has set. We flush the data after updating the list to make
     * sure the view gets the updated list at this instant and everything
     * will be synchronized.
     */
    private void setList(final List<PeptideMatch> peptideList) {
        dataProvider.getList().clear();
        dataProvider.getList().addAll(peptideList);
        dataSorter.repeatSort();
        dataProvider.flush();
    }
}
