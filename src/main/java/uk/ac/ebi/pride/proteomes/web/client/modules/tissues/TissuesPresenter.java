package uk.ac.ebi.pride.proteomes.web.client.modules.tissues;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.ProteinRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.StateChangingActionEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.ValidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.ModificationUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.ProteinUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.TissueUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.modules.Presenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.history.StateChanger;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListUiHandler;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListView;

import java.util.*;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 22/11/13
 *         Time: 15:23
 */
public class TissuesPresenter implements Presenter,
                                         ValidStateEvent.ValidStateHandler,
                                         ProteinUpdateEvent.ProteinUpdateHandler,
                                         ProteinRequestEvent.ProteinRequestHandler,
                                         TissueUpdateEvent.TissueUpdateHandler,
                                         ListUiHandler<String>
{
    private final EventBus eventBus;
    private final ListView<String> view;
    private final ListDataProvider<String> dataProvider = new
            ListDataProvider<String>();
    private final ColumnSortEvent.ListHandler<String> dataSorter = new
            ColumnSortEvent.ListHandler<String>(new ArrayList<String>());

    private boolean groups;
    private List<String> selectedTissues = new ArrayList<String>();

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

        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(ProteinUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinRequestEvent.getType(), this);
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
    public void onTissueUpdateEvent(TissueUpdateEvent event) {
        for(String item : selectedTissues) {
            deselectItem(item);
        }

        selectedTissues = new ArrayList<String>();
        for(String item : event.getTissues()) {
            selectItem(item);
            selectedTissues.add(item);
        }
    }

    @Override
    public void onSelectionChanged(Collection<String> items) {
        StateChanger changer;

        if(items.equals(selectedTissues)) {
            return;
        } else if(items.contains(null)) {
            items = Collections.emptyList();
        }

        changer = new StateChanger();

        changer.addTissueChange(items);
        StateChangingActionEvent.fire(this, changer);
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
     * We have to update the list reference in the provider and the sorter,
     * otherwise the sorting would not work anymore.
     *
     * Alternatively we could also empty the list and repopulate it with the
     * new data, but that would make it slower.
     */
    private void setList(final List<String> list) {
        dataProvider.setList(list);
        dataSorter.setList(list);
    }
}
