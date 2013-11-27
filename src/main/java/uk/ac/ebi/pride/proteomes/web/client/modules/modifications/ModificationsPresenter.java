package uk.ac.ebi.pride.proteomes.web.client.modules.modifications;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.ModifiedLocation;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.ProteinRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.StateChangingActionEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.ValidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.ProteinUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.modules.Presenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.history.StateChanger;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListUiHandler;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 26/11/13
 *         Time: 10:35
 */
public class ModificationsPresenter implements Presenter,
                                          ValidStateEvent.ValidStateHandler,
                                          ProteinUpdateEvent.ProteinUpdateHandler,
                                          ProteinRequestEvent.ProteinRequestHandler,
                                          ListUiHandler<Multiset.Entry<String>>
{
    private final EventBus eventBus;
    private final ListView<Multiset.Entry<String>> view;
    private final ListDataProvider<Multiset.Entry<String>> dataProvider = new
            ListDataProvider<Multiset.Entry<String>>();
    private final ColumnSortEvent.ListHandler<Multiset.Entry<String>> dataSorter = new
            ColumnSortEvent.ListHandler<Multiset.Entry<String>>(new ArrayList<Multiset.Entry<String>>());

    private boolean groups;
    private List<Multiset.Entry<String>> selectedModifications = new ArrayList<Multiset.Entry<String>>();

    public ModificationsPresenter(EventBus eventBus, ListView<Multiset.Entry<String>> view) {
        this.eventBus = eventBus;
        this.view = view;
        List<Column<Multiset.Entry<String>, ?>> columns = ModificationColumnProvider.getSortingColumns(dataSorter);
        List<String> columnTitles = ModificationColumnProvider.getColumnTitles();
        List<String> columnWidths = ModificationColumnProvider.getColumnWidths();

        view.addDataProvider(dataProvider);
        view.addColumns(columns, columnTitles, columnWidths);
        view.addColumnSortHandler(dataSorter);
        view.addUiHandler(this);
        view.asWidget().setVisible(false);

        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(ProteinUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinRequestEvent.getType(), this);
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
    public void onSelectionChanged(Collection<Multiset.Entry<String>> items) {

        StateChanger changer;

        if(items.equals(selectedModifications)) {
            return;
        } else if(items.contains(null)) {
            items = Collections.emptyList();
        }

        changer = new StateChanger();

        List<String> selection = new ArrayList<String>();
        for(Multiset.Entry<String> item : items) {
            selection.add(item.getElement());
        }

        changer.addModificationChange(selection);
        StateChangingActionEvent.fire(this, changer);
    }

    @Override
    public void onProteinRequestEvent(ProteinRequestEvent event) {
        // We should display that the list is being loaded
        if(!groups) {
            view.showLoadingMessage();
        }
    }

    @Override
    public void onProteinUpdateEvent(ProteinUpdateEvent event) {
        if(!groups && event.getProteins().size() > 0) {
            Multiset<String> mods = HashMultiset.create();
            for(ModifiedLocation loc : event.getProteins().get(0).getModifiedLocations()) {
                mods.add(loc.getModification());
            }

            updateList(mods.entrySet());
            view.showList();
        }
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

    private void updateList(Collection<Multiset.Entry<String>> mods) {
        for(Multiset.Entry<String> tissue : selectedModifications) {
            deselectItem(tissue);
        }

        setList(new ArrayList<Multiset.Entry<String>>(mods));

        for(Multiset.Entry<String> tissue : selectedModifications) {
            selectItem(tissue);
        }
    }

    private void selectItem(Multiset.Entry<String> mod) {
        view.selectItemOn(dataProvider.getList().indexOf(mod));
    }

    private void deselectItem(Multiset.Entry<String> tissue) {
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
    private void setList(final List<Multiset.Entry<String>> list) {
        dataProvider.setList(list);
        dataSorter.setList(list);
    }
}
