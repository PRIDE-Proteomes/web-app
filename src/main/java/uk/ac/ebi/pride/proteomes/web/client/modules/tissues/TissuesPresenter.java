package uk.ac.ebi.pride.proteomes.web.client.modules.tissues;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.UserAction;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.ProteinRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.StateChangingActionEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.ValidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.PeptideUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.ProteinUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.TissueUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.modules.Presenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.history.StateChanger;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListSorter;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListUiHandler;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 22/11/13
 *         Time: 15:23
 */
public class TissuesPresenter extends Presenter<ListView<String>>
                              implements ListUiHandler<String>,
                                         ValidStateEvent.Handler,
                                         ProteinUpdateEvent.Handler,
                                         ProteinRequestEvent.Handler,
                                         PeptideUpdateEvent.Handler,
                                         TissueUpdateEvent.Handler {

    private final ListDataProvider<String> dataProvider = new ListDataProvider<>();
    private final ListSorter<String> dataSorter = new
                                        ListSorter<>(new ArrayList<String>());

    private boolean groups = true;
    private Collection<String> selectedTissues = Collections.emptyList();
    private Collection<? extends PeptideMatch> selectedPeptides = Collections.emptyList();
    private boolean selectionEventsDisabled = false;

    public TissuesPresenter(EventBus eventBus, ListView<String> view) {
        super(eventBus, view);

        List<Column<String, ?>> columns = TissueColumnProvider.getSortingColumns(dataSorter);
        List<String> columnTitles = TissueColumnProvider.getColumnTitles();
        List<String> columnWidths = TissueColumnProvider.getColumnWidths();

        view.addDataProvider(dataProvider);
        view.addColumns(columns, columnTitles, columnWidths);
        view.addColumnSortHandler(dataSorter);
        view.addUiHandler(this);
        view.asWidget().setVisible(false);
        view.hideContent();

        final MultiSelectionModel<String> selectionModel = new MultiSelectionModel<>();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if(!selectionEventsDisabled) {
                    for(ListUiHandler<String> handler : getView().getUiHandlers()) {
                        handler.onSelectionChanged(selectionModel.getSelectedSet());
                    }
                }
            }
        });

        view.setSelectionModel(selectionModel);
        view.setKeyboardSelectionPolicy(TissueColumnProvider.getKeyboardSelectionPolicy());

        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(ProteinUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinRequestEvent.getType(), this);
        eventBus.addHandler(PeptideUpdateEvent.getType(), this);
        eventBus.addHandler(TissueUpdateEvent.getType(), this);

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
        if(!groups && event.getProteins().size() > 0) {
            updateList(event.getProteins().get(0).getTissues());
            getView().loadList();
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
    public void onPeptideUpdateEvent(PeptideUpdateEvent event) {
        if(event.getPeptides().isEmpty()) {
            selectedPeptides = Collections.emptyList();
        }
        else {
            selectedPeptides = event.getPeptides();
        }
    }

    @Override
    public void onTissueUpdateEvent(TissueUpdateEvent event) {
        for(String tissue : selectedTissues) {
            deselectItem(tissue);
        }

        selectedTissues = new ArrayList<>();
        for(String item : event.getTissues()) {
            selectItem(item);
            selectedTissues.add(item);
        }
    }

    @Override
    public void onSelectionChanged(Collection<String> items) {
        StateChanger changer;
        UserAction action;
        List<PeptideMatch> filteredPeptides;

        // an empty selection is represented by a list with a null item,
        // we represent that with an empty list, so we have to add an
        // additional check for that.
        if((items.containsAll(selectedTissues) && selectedTissues.containsAll(items)) ||
                (items.contains(null) && selectedTissues.isEmpty())) {
            return;
        } else if(items.contains(null)) {
            items = Collections.emptyList();
        }

        filteredPeptides = new ArrayList<>();

        for(PeptideMatch pep : selectedPeptides) {
            // If the collections are disjoint means the peptide doesn't have
            // any tissue in items. If this happens, we filter it out.
            if(!Collections.disjoint(pep.getTissues(), items) || items.isEmpty()) {
                filteredPeptides.add(pep);
            }
        }

        changer = new StateChanger();
        changer.addTissueChange(items);
        if(filteredPeptides.size() < selectedPeptides.size()) {
            changer.addPeptideChange(filteredPeptides);
        }

        if(items.isEmpty()) {
            action = new UserAction(UserAction.Type.tissue, "Click Reset");
        }
        else {
            action = new UserAction(UserAction.Type.tissue, "Click Set");
        }
        StateChangingActionEvent.fire(this, changer, action);
    }

    private void updateList(List<String> tissues) {
        for(String tissue : selectedTissues) {
            deselectItem(tissue);
        }

        setList(tissues);

        selectedTissues.retainAll(tissues);
        for(String tissue : selectedTissues) {
            selectItem(tissue);
        }
    }

    private void selectItem(String tissue) {
        selectionEventsDisabled = true;
        getView().selectItemOn(dataProvider.getList().indexOf(tissue));
        selectionEventsDisabled = false;
    }

    private void deselectItem(String tissue) {
        selectionEventsDisabled = true;
        getView().deselectItemOn(dataProvider.getList().indexOf(tissue));
        selectionEventsDisabled = false;
    }

    /**
     * This method is used whenever a new list is set as the model of the view.
     *
     * We clear the list and repopulate it because if we simply reset the
     * data provider and data sorter references to the list it won't work.
     */
    private void setList(final List<String> list) {
        dataProvider.getList().clear();
        dataProvider.getList().addAll(list);
        dataSorter.repeatSort();
        dataProvider.flush();
    }
}
