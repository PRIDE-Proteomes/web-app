package uk.ac.ebi.pride.proteomes.web.client.modules.modifications;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.UserAction;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.ModifiedLocation;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.ProteinRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.StateChangingActionEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.ValidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.ModificationUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.PeptideUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.ProteinUpdateEvent;
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
 *         Date: 26/11/13
 *         Time: 10:35
 */
public class ModificationsPresenter implements Presenter,
                                          ValidStateEvent.ValidStateHandler,
                                          ProteinUpdateEvent.ProteinUpdateHandler,
                                          ProteinRequestEvent.ProteinRequestHandler,
                                          PeptideUpdateEvent.PeptideUpdateHandler,
                                          ModificationUpdateEvent.ModificationUpdateHandler,
                                          ListUiHandler<Multiset.Entry<String>>
{
    private final EventBus eventBus;
    private final ListView<Multiset.Entry<String>> view;
    private final ListDataProvider<Multiset.Entry<String>> dataProvider = new
            ListDataProvider<Multiset.Entry<String>>();
    private final ListSorter<Multiset.Entry<String>> dataSorter = new
            ListSorter<Multiset.Entry<String>>(new ArrayList<Multiset.Entry<String>>());

    private boolean groups;
    private List<Multiset.Entry<String>> selectedModifications = new ArrayList<Multiset.Entry<String>>();
    private List<Peptide> selectedPeptides = Collections.emptyList();

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
        view.hideContent();

        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(ProteinUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinRequestEvent.getType(), this);
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
            Multiset<String> mods = HashMultiset.create();
            for(ModifiedLocation loc : event.getProteins().get(0).getModifiedLocations()) {
                mods.add(loc.getModification());
            }
            updateList(mods.entrySet());
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
    public void onSelectionChanged(Collection<Multiset.Entry<String>> items) {
        StateChanger changer;
        List<String> filteredPeptides;

        if((items.containsAll(selectedModifications) &&
            selectedModifications.containsAll(items)) ||
                (items.contains(null) && selectedModifications.isEmpty())) {
            return;
        } else if(items.contains(null)) {
            items = Collections.emptyList();
        }

        List<String> selection = new ArrayList<String>();
        for(Multiset.Entry<String> item : items) {
            selection.add(item.getElement());
        }

        filteredPeptides = new ArrayList<String>();
        for(Peptide pep : selectedPeptides) {
            // If the collections are disjoint means the peptide doesn't have
            // any tissue in items. If this happens, we filter it out.
            if(!Collections.disjoint(pep.getTissues(), items)) {
                filteredPeptides.add(pep.getSequence());
            }
        }

        changer = new StateChanger();
        changer.addModificationChange(selection);
        changer.addPeptideChange(filteredPeptides);
        UserAction action = new UserAction(UserAction.Type.modification,
                                           "Click");
        StateChangingActionEvent.fire(this, changer, action);
    }

    @Override
    public void onModificationUpdateEvent(ModificationUpdateEvent event) {
        for(Multiset.Entry<String> item : selectedModifications) {
            deselectItem(item);
        }

        selectedModifications = new ArrayList<Multiset.Entry<String>>();
        for(String item : event.getModifications()) {
            for(Multiset.Entry<String> entry : dataProvider.getList()) {
                if(entry.getElement().equals(item)) {
                    selectItem(entry);
                    selectedModifications.add(entry);
                    break;
                }
            }

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
     *
     * We clear the list and repopulate it because if we simply reset the
     * data provider and data sorter references to the list it won't work.
     */
    private void setList(final List<Multiset.Entry<String>> list) {
        dataProvider.getList().clear();
        dataProvider.getList().addAll(list);
        dataSorter.repeatSort();
        dataProvider.flush();
    }
}
