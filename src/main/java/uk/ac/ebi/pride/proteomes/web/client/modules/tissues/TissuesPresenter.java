package uk.ac.ebi.pride.proteomes.web.client.modules.tissues;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.UserAction;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
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

import java.util.*;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 22/11/13
 *         Time: 15:23
 */
public class TissuesPresenter implements Presenter, ListUiHandler<String>,
                                         ValidStateEvent.Handler,
                                         ProteinUpdateEvent.Handler,
                                         ProteinRequestEvent.Handler,
                                         PeptideUpdateEvent.Handler,
                                         TissueUpdateEvent.Handler {
    private final EventBus eventBus;
    private final ListView<String> view;
    private final ListDataProvider<String> dataProvider = new
            ListDataProvider<String>();
    private final ListSorter<String> dataSorter = new
                                        ListSorter<String>(new ArrayList<String>());

    private boolean groups = true;
    private List<String> selectedTissues = Collections.emptyList();
    private List<Peptide> selectedPeptides = Collections.emptyList();

    public TissuesPresenter(EventBus eventBus, ListView<String> view) {
        this.eventBus = eventBus;
        this.view = view;
        List<Column<String, ?>> columns = TissueColumnProvider.getSortingColumns(dataSorter);
        List<String> columnTitles = TissueColumnProvider.getColumnTitles();
        List<String> columnWidths = TissueColumnProvider.getColumnWidths();

        view.addDataProvider(dataProvider);
        view.addColumns(columns, columnTitles, columnWidths);
        view.addColumnSortHandler(dataSorter);
        view.addUiHandler(this);
        view.asWidget().setVisible(false);
        view.hideContent();

        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(ProteinUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinRequestEvent.getType(), this);
        eventBus.addHandler(PeptideUpdateEvent.getType(), this);
        eventBus.addHandler(TissueUpdateEvent.getType(), this);

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
        if(!groups && event.getProteins().size() > 0) {
            updateList(event.getProteins().get(0).getTissues());
            view.loadList();
        }
    }

    @Override
    public void onProteinRequestEvent(ProteinRequestEvent event) {
        // We should display that the list is being loaded
        if(!groups) {
            view.loadLoadingMessage();
        }
    }

    @Override
    public void onPeptideUpdateEvent(PeptideUpdateEvent event) {
        if(event.getPeptides().isEmpty()) {
            selectedPeptides = Collections.emptyList();
        }
        else {
            selectedPeptides = event.getPeptides().get(0).getPeptideList();
        }
    }

    @Override
    public void onTissueUpdateEvent(TissueUpdateEvent event) {
        selectedTissues = new ArrayList<String>();
        for(String item : event.getTissues()) {
            selectItem(item);
            selectedTissues.add(item);
        }
    }

    @Override
    public void onSelectionChanged(Collection<String> items) {
        StateChanger changer;
        UserAction action;
        List<String> filteredPeptides;

        // an empty selection is represented by a list with a null item,
        // we represent that with an empty list, so we have to add an
        // additional check for that.
        if((items.containsAll(selectedTissues) && selectedTissues.containsAll(items)) ||
                (items.contains(null) && selectedTissues.isEmpty())) {
            return;
        } else if(items.contains(null)) {
            items = Collections.emptyList();
        }

        filteredPeptides = new ArrayList<String>();
        for(Peptide pep : selectedPeptides) {
            // If the collections are disjoint means the peptide doesn't have
            // any tissue in items. If this happens, we filter it out.
            if(!Collections.disjoint(pep.getTissues(), items) || items.isEmpty()) {
                filteredPeptides.add(pep.getSequence());
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

        for(String tissue : selectedTissues) {
            selectItem(tissue);
        }
    }

    private void selectItem(String tissue) {
        view.selectItemOn(dataProvider.getList().indexOf(tissue));
    }

    private void deselectItem(String tissue) {
        view.deselectItemOn(dataProvider.getList().indexOf(tissue));
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
