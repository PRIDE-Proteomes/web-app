package uk.ac.ebi.pride.proteomes.web.client.modules.peptides;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.UserAction;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.ModificationWithPosition;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.PeptideWithPeptiforms;
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
import java.util.logging.Logger;

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
                                          ModificationWithPositionUpdateEvent.Handler,
                                          PeptiformUpdateEvent.Handler {

    private static Logger logger = Logger.getLogger(PeptidesPresenter.class.getName());

    private final ListDataProvider<PeptideMatch> dataProvider = new ListDataProvider<>();
    private final ListSorter<PeptideMatch> dataSorter = new ListSorter<>(new ArrayList<PeptideMatch>());

    private boolean groups = true;
    private Protein currentProtein;
    private Region currentRegion = Region.emptyRegion();
    private List<PeptideWithPeptiforms> selectedPeptidesMatches = Collections.emptyList();
    private List<String> currentTissues = Collections.emptyList();
    private List<String> currentModifications = Collections.emptyList();
    private List<ModificationWithPosition> currentModWithPos = Collections.emptyList();
    private List<Peptide> selectedPeptiforms = Collections.emptyList();

    private boolean selectionEventsDisabled = false;

    public PeptidesPresenter(EventBus eventBus, ListView<PeptideMatch> view) {
        super(eventBus, view);
        List<Column<PeptideMatch, ?>> columns = PeptideColumnProvider.getSortingColumns(dataSorter);
        List<String> columnTitles = PeptideColumnProvider.getColumnTitles();
        List<String> columnWidths = PeptideColumnProvider.getColumnWidths();

        dataSorter.setList(dataProvider.getList());
        view.addDataProvider(dataProvider);
        view.addColumns(columns, columnTitles, columnWidths);
        view.addColumnSortHandler(dataSorter);
        view.addUiHandler(this);
        view.setHeight("150px");
        view.asWidget().setVisible(false);

        // We define how are the items selected here
        final SingleSelectionModel<PeptideMatch> selectionModel = new SingleSelectionModel<>();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                for(ListUiHandler<PeptideMatch> handler : getView().getUiHandlers()) {
                    handler.onSelectionChanged(selectionModel.getSelectedObject());
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
        eventBus.addHandler(PeptiformUpdateEvent.getType(), this);
        eventBus.addHandler(TissueUpdateEvent.getType(), this);
        eventBus.addHandler(ModificationUpdateEvent.getType(), this);
        eventBus.addHandler(ModificationWithPositionUpdateEvent.getType(), this);
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

            for(PeptideMatch peptide : selectedPeptidesMatches) {
                deselectItem(peptide);
            }

            // we should reset filters and ordering here
            updateList(new ArrayList<>(currentProtein.getPeptides()));
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

        for(PeptideMatch peptide : selectedPeptidesMatches) {
            deselectItem(peptide);
        }

        // we change the peptide list
        if(event.getRegions().size() == 0) {
            currentRegion = Region.emptyRegion();
        }
        else {
            currentRegion = event.getRegions().get(0);
        }

        updateList(PeptideUtils.filterPeptideMatches(currentProtein.getPeptides(),
                currentRegion.getStart(), currentRegion.getEnd(),
                currentTissues, currentModifications, currentModWithPos, currentProtein.getSequence().length()));
    }

    @Override
    public void onPeptideUpdateEvent(PeptideUpdateEvent event) {

        for(PeptideMatch peptide : selectedPeptidesMatches) {
            deselectItem(peptide);
        }

        selectedPeptidesMatches = new ArrayList<>(event.getPeptides());

        if(!selectedPeptidesMatches.isEmpty()){ //TODO Why do we need to check if it is empty?
            updateList(PeptideUtils.filterPeptideMatches(currentProtein.getPeptides(),
                    currentRegion.getStart(), currentRegion.getEnd(),
                    currentTissues, currentModifications, currentModWithPos, currentProtein.getSequence().length()));
        }
    }

    @Override
    public void onPeptiformUpdateEvent(PeptiformUpdateEvent event) {
        selectedPeptiforms = event.getPeptiforms();
    }

    @Override
    public void onTissueUpdateEvent(TissueUpdateEvent event) {

        for(PeptideMatch peptide : selectedPeptidesMatches) {
            deselectItem(peptide);
        }

        currentTissues = event.getTissues();

        updateList(PeptideUtils.filterPeptideMatches(currentProtein.getPeptides(),
                currentRegion.getStart(), currentRegion.getEnd(),
                currentTissues, currentModifications, currentModWithPos, currentProtein.getSequence().length()));
    }

    /**
     * If the modification is in a position the modification filter needs to
     * get reset.
     * @param event Event containing the string identifying a type of
     *              modification.
     */
    @Override
    public void onModificationUpdateEvent(ModificationUpdateEvent event) {

        for(PeptideMatch peptide : selectedPeptidesMatches) {
            deselectItem(peptide);
        }

        currentModifications = event.getModifications();

        updateList(PeptideUtils.filterPeptideMatches(currentProtein.getPeptides(),
                currentRegion.getStart(), currentRegion.getEnd(),
                currentTissues, currentModifications, currentModWithPos, currentProtein.getSequence().length()));

    }

    @Override
    public void onModificationUpdateEvent(ModificationWithPositionUpdateEvent event) {

        for(PeptideMatch peptide : selectedPeptidesMatches) {
            deselectItem(peptide);
        }

        currentModWithPos = event.getModifications();

        updateList(PeptideUtils.filterPeptideMatches(currentProtein.getPeptides(),
                currentRegion.getStart(), currentRegion.getEnd(),
                currentTissues, currentModifications, currentModWithPos, currentProtein.getSequence().length()));
    }

    @Override
    public void onSelectionChanged(PeptideMatch peptide) {
        StateChanger changer = new StateChanger();
        List<Region> regions = new ArrayList<>();
        List<PeptideMatch> peptides = new ArrayList<>();
        UserAction action;

        if (selectionEventsDisabled)
            return;

        if (peptide != null) {

            //Peptides
            peptides.add(peptide);
            changer.addPeptideChange(peptides);

            //Regions
            //With the selection we reset the region to avoid confunsing to the user clicking outside of the region and moving it
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
            List<ModificationWithPosition> modWithPos = PeptideUtils.extractModifications(peptide, currentProtein.getSequence().length());
            modWithPos.retainAll(currentModWithPos);
            if (!modWithPos.containsAll(currentModWithPos)) {
                changer.addModificationWithPositionChange(modWithPos);
            }

            //Tissues
            List<String> tissues = new ArrayList<>(peptide.getTissues());
            tissues.retainAll(currentTissues);
            if (!tissues.containsAll(currentTissues)) {
                changer.addTissueChange(tissues);
            }

            // We use HashSets because we don't want duplicated and want a fast
            // lookup
            Set<Peptide> variances = new HashSet<>();

            // we should change the peptiform selection if it doesn't fit the
            // current peptide selection
            for (Peptide variance : selectedPeptiforms) {
                if (peptide.getSequence().equalsIgnoreCase((variance.getSequence()))) {
                    variances.add(variance);
                }
            }
            if (!variances.containsAll(selectedPeptiforms)) {
                changer.addPeptiformChange(variances);
            }

            action = new UserAction(UserAction.Type.peptide, "Click Set");
        } else {
            action = new UserAction(UserAction.Type.peptide, "Click Reset");
        }

        changer.addPeptideChange(peptides);
        StateChangingActionEvent.fire(this, changer, action);
    }

    @Override
    public void onSelectionChanged(Collection<PeptideMatch> items) {
        //Multiple selection is not allow in this case
    }

    private void updateList(List<? extends PeptideMatch> peptideList) {
        setList(peptideList);

        for(PeptideMatch peptide : selectedPeptidesMatches) {
            selectItem(peptide);
        }
    }

    private void selectItem(PeptideMatch peptide) {
        int peptidePosition = -1;
        for(int i = 0; i < dataProvider.getList().size(); i++) {
            PeptideMatch match = dataProvider.getList().get(i);
            if(peptide.getSequence().equals(match.getSequence())
                && peptide.getPosition().equals(match.getPosition())) {
                peptidePosition = i;
                break;
            }
        }

        if(peptidePosition > -1) {
            selectionEventsDisabled = true;
            getView().selectItemOn(peptidePosition);
            getView().focusItemOn(peptidePosition);
            selectionEventsDisabled = false;
        }
    }

    private void deselectItem(PeptideMatch peptide) {
        int peptidePosition = -1;
        for(int i = 0; i < dataProvider.getList().size(); i++) {
            PeptideMatch match = dataProvider.getList().get(i);
            if(peptide.getSequence().equals(match.getSequence())
                    && peptide.getPosition().equals(match.getPosition())) {
                peptidePosition = i;
                break;
            }
        }

        if(peptidePosition > -1) {
            selectionEventsDisabled = true;
            getView().deselectItemOn(peptidePosition);
            selectionEventsDisabled = false;
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
    private void setList(final List<? extends PeptideMatch> peptideList) {
        dataProvider.getList().clear();
        dataProvider.getList().addAll(peptideList);
        dataSorter.repeatSort();
        dataProvider.flush();
        getView().loadList();
        getView().showContent();
    }
}
