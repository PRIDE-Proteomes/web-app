package uk.ac.ebi.pride.proteomes.web.client.modules.variances;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.UserAction;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.EmptyPeptideList;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideList;
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
public class VariancesPresenter implements Presenter,
                                           ValidStateEvent.ValidStateHandler,
                                           PeptideUpdateEvent.PeptideUpdateHandler,
                                           VarianceUpdateEvent.VarianceUpdateHandler,
                                           ListUiHandler<Peptide>
{
    private final EventBus eventBus;
    private final ListView<Peptide> view;
    private final ListDataProvider<Peptide> dataProvider = new
                                        ListDataProvider<Peptide>();
    private final ListSorter<Peptide> dataSorter = new
                                       ListSorter<Peptide>(new ArrayList<Peptide>());

    private boolean groups = true;
    private PeptideList currentPeptide;
    private Collection<Peptide> selectedVariances = Collections.emptyList();

    public VariancesPresenter(EventBus eventBus, ListView<Peptide> view) {
        this.eventBus = eventBus;
        this.view = view;

        List<Column<Peptide, ?>> columns = VarianceColumnProvider
                                            .getSortingColumns(dataSorter);
        List<String> columnTitles = VarianceColumnProvider.getColumnTitles();
        List<String> columnWidths = VarianceColumnProvider.getColumnWidths();

        view.addDataProvider(dataProvider);
        view.addColumns(columns, columnTitles, columnWidths);
        view.addColumnSortHandler(dataSorter);
        view.addUiHandler(this);
        view.asWidget().setVisible(false);

        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(PeptideUpdateEvent.getType(), this);
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
    public void onPeptideUpdateEvent(PeptideUpdateEvent event) {
        if(!groups) {
            if(event.getPeptides().size() > 0) {
                currentPeptide = event.getPeptides().get(0);
            }
            else {
                currentPeptide = new EmptyPeptideList();
            }
            updateList(currentPeptide.getPeptideList());
            view.showList();
        }
    }

    @Override
    public void onVarianceUpdateEvent(VarianceUpdateEvent event) {
        for(Peptide peptide : selectedVariances) {
            deselectItem(peptide);
        }

        selectedVariances = new ArrayList<Peptide>();
        for(String Id : event.getVarianceIDs()) {
            int peptidePosition = PeptideUtils.firstIndexWithId(dataProvider.getList(), Id);
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

        Set<String> peptideIds = new HashSet<String>();

        for(Peptide peptide : items) {
            peptideIds.add(peptide.getId());
        }

        changer = new StateChanger();
        changer.addVarianceChange(peptideIds);

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
            view.selectItemOn(peptidePosition);
        }
    }

    private void deselectItem(Peptide peptide) {
        // search the first occurrence of the peptide, we can only select
        // one because of the selection model
        int peptidePosition = PeptideUtils.firstIndexWithId(dataProvider.getList(),
                peptide.getId());
        if(peptidePosition > -1) {
            view.deselectItemOn(peptidePosition);
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
