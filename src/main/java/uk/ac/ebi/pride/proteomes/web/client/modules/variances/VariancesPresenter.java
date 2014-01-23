package uk.ac.ebi.pride.proteomes.web.client.modules.variances;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.UserAction;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.PeptideWithVariances;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.events.state.StateChangingActionEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.ValidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.PeptideUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.VarianceUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.modules.Presenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.history.StateChanger;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListSorter;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListUiHandler;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListView;
import uk.ac.ebi.pride.proteomes.web.client.utils.PeptideUtils;

import java.util.*;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 29/11/13
 *         Time: 14:46
 */
public class VariancesPresenter extends Presenter<ListView<Peptide>>
                                implements ListUiHandler<Peptide>,
                                           ValidStateEvent.Handler,
                                           PeptideUpdateEvent.Handler,
                                           VarianceUpdateEvent.Handler {
    private final ListDataProvider<Peptide> dataProvider = new
                                        ListDataProvider<>();
    private final ListSorter<Peptide> dataSorter = new
                                       ListSorter<>(new ArrayList<Peptide>());

    private boolean groups = true;
    private Collection<Peptide> selectedVariances = Collections.emptyList();
    private boolean selectionEventsDisabled = false;

    public VariancesPresenter(EventBus eventBus, ListView<Peptide> view) {
        super(eventBus, view);

        List<Column<Peptide, ?>> columns = VarianceColumnProvider
                                            .getSortingColumns(dataSorter);
        List<String> columnTitles = VarianceColumnProvider.getColumnTitles();
        List<String> columnWidths = VarianceColumnProvider.getColumnWidths();

        dataSorter.setList(dataProvider.getList());
        view.addDataProvider(dataProvider);
        view.addColumns(columns, columnTitles, columnWidths);
        view.addColumnSortHandler(dataSorter);
        view.addUiHandler(this);
        view.asWidget().setVisible(false);

        // We define how are the items selected here
        final SingleSelectionModel<Peptide> selectionModel = new
                SingleSelectionModel<>();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                if(!selectionEventsDisabled) {
                    Set<Peptide> selection = new HashSet<>();
                    selection.add(selectionModel.getSelectedObject());
                    for(ListUiHandler<Peptide> handler : getView().getUiHandlers()) {
                        handler.onSelectionChanged(selection);
                    }
                }
            }
        });

        view.setSelectionModel(selectionModel);
        view.setKeyboardSelectionPolicy(VarianceColumnProvider.getKeyboardSelectionPolicy());

        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(PeptideUpdateEvent.getType(), this);
        eventBus.addHandler(VarianceUpdateEvent.getType(), this);
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
    public void onPeptideUpdateEvent(PeptideUpdateEvent event) {
        if(!groups) {
            PeptideWithVariances currentPeptide;
            if(event.getPeptides().size() > 0) {
                currentPeptide = event.getPeptides().get(0);
                getView().showContent();
            }
            else {
                currentPeptide = PeptideWithVariances.emptyPeptideWithVariances();
            }
            updateList(currentPeptide.getPeptideList());
            selectedVariances = new ArrayList<>();
            getView().loadList();
        }
    }

    @Override
    public void onVarianceUpdateEvent(VarianceUpdateEvent event) {
        for(Peptide peptide : selectedVariances) {
            deselectItem(peptide);
        }

        selectedVariances = new ArrayList<>();
        for(Peptide variance : event.getVariances()) {
            int peptidePosition = PeptideUtils.firstIndexWithId(dataProvider.getList(), variance.getId());
            if(peptidePosition > -1) {
                selectedVariances.add(dataProvider.getList().get(peptidePosition));
            }
        }

        if(!selectedVariances.isEmpty()) {
            // we reselect the peptides only if there are any
            selectVariances();
        }
    }

    @Override
    public void onSelectionChanged(Collection<Peptide> items) {
        StateChanger changer;
        UserAction action;
        // an empty selection is represented by a list with a null items,
        // we represent that with an empty list, so we have to add an
        // additional check for that.
        if((items.containsAll(selectedVariances) &&
            selectedVariances.containsAll(items)) ||
                (items.contains(null) && selectedVariances.isEmpty())) {
            return;
        } else if(items.contains(null)) {
            items = Collections.emptyList();
        }

        Set<Peptide> variances = new HashSet<>();

        for(Peptide variance : items) {
            variances.add(variance);
        }

        changer = new StateChanger();
        changer.addVarianceChange(variances);

        if(items.isEmpty()) {
            action = new UserAction(UserAction.Type.variance, "Click Reset");
        }
        else {
            action = new UserAction(UserAction.Type.variance, "Click Set");
        }
        StateChangingActionEvent.fire(this, changer, action);
    }

    private void updateList(List<Peptide> peptideList) {
        for(Peptide peptide : selectedVariances) {
            deselectItem(peptide);
        }

        setList(peptideList);

        selectedVariances.retainAll(peptideList);
        for(Peptide peptide : selectedVariances) {
            selectItem(peptide);
        }
    }

    private void selectItem(Peptide peptide) {
        // search the first occurrence of the peptide, we can only select
        // one because of the selection model
        int peptidePosition = PeptideUtils.firstIndexWithId(dataProvider.getList(),
                peptide.getId());
        if(peptidePosition > -1) {
            selectionEventsDisabled = true;
            getView().selectItemOn(peptidePosition);
            selectionEventsDisabled = false;
        }
    }

    private void deselectItem(Peptide peptide) {
        // search the first occurrence of the peptide, we can only select
        // one because of the selection model
        int peptidePosition = PeptideUtils.firstIndexWithId(dataProvider.getList(),
                peptide.getId());
        if(peptidePosition > -1) {
            selectionEventsDisabled = true;
            getView().deselectItemOn(peptidePosition);
            selectionEventsDisabled = false;
        }
    }

    private void selectVariances() {
        for(Peptide peptide : selectedVariances) {
            selectItem(peptide);
        }
    }

    /**
     * This method is used whenever a new list is set as the model of the view.
     *
     * We clear the list and repopulate it because if we simply reset the
     * data provider and data sorter references to the list it won't work.
     */
    private void setList(final List<Peptide> peptideList) {
        dataProvider.getList().clear();
        dataProvider.getList().addAll(peptideList);
        dataSorter.repeatSort();
        dataProvider.flush();
    }
}
