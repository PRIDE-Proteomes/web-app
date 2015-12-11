package uk.ac.ebi.pride.proteomes.web.client.modules.modifications;

import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.OrderedMultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.UserAction;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.ModifiedLocation;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.ProteinRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.StateChangingActionEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.ValidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.ModificationUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.PeptideUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.ProteinUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.TissueUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.modules.Presenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.history.StateChanger;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListSorter;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListUiHandler;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListView;
import uk.ac.ebi.pride.proteomes.web.client.utils.PeptideUtils;

import java.util.*;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 26/11/13
 *         Time: 10:35
 */
public class ModificationsPresenter extends Presenter<ListView<Multiset.Entry<String>>>
        implements ListUiHandler<Multiset.Entry<String>>,
        ValidStateEvent.Handler,
        ProteinUpdateEvent.Handler,
        ProteinRequestEvent.Handler,
        PeptideUpdateEvent.Handler,
        TissueUpdateEvent.Handler,
        ModificationUpdateEvent.Handler {

    private final ListDataProvider<Multiset.Entry<String>> dataProvider = new ListDataProvider<>();
    private final ListSorter<Multiset.Entry<String>> dataSorter = new ListSorter<>(new ArrayList<Multiset.Entry<String>>());

    private boolean groups = true;
    private boolean selectionEventsDisabled = false;


    private Protein currentProtein;
    private Collection<Multiset.Entry<String>> selectedModifications = Collections.emptyList();
    private List<String> selectedTissues = Collections.emptyList();
    private Collection<? extends PeptideMatch> selectedPeptides = Collections.emptyList();

    //Biological modifications extraced from the peptides. It takes into account the number of times that a modification appear
    private Multiset<String> peptidesMods = TreeMultiset.create();

    public ModificationsPresenter(EventBus eventBus, ListView<Multiset.Entry<String>> view) {
        super(eventBus, view);
        List<Column<Multiset.Entry<String>, ?>> columns = ModificationColumnProvider.getSortingColumns(dataSorter);
        List<String> columnTitles = ModificationColumnProvider.getColumnTitles();
        List<String> columnWidths = ModificationColumnProvider.getColumnWidths();

        view.addDataProvider(dataProvider);
        view.addColumns(columns, columnTitles, columnWidths);
        view.addColumnSortHandler(dataSorter);
        view.addUiHandler(this);
        view.asWidget().setVisible(false);

        final OrderedMultiSelectionModel<Multiset.Entry<String>> selectionModel = new OrderedMultiSelectionModel<>();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (!selectionEventsDisabled) {
                    for (ListUiHandler<Multiset.Entry<String>> handler : getView().getUiHandlers()) {
                        handler.onSelectionChanged(selectionModel.getSelectedSet());
                    }
                }
            }
        });

        view.setSelectionModel(selectionModel);
        view.setKeyboardSelectionPolicy(ModificationColumnProvider.getKeyboardSelectionPolicy());

        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(ProteinUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinRequestEvent.getType(), this);
        eventBus.addHandler(PeptideUpdateEvent.getType(), this);
        eventBus.addHandler(TissueUpdateEvent.getType(), this);
        eventBus.addHandler(ModificationUpdateEvent.getType(), this);

    }

    @Override
    public void onValidStateEvent(ValidStateEvent event) {
        // We should check if we have to stay hidden or not
        if (event.getViewType() == ValidStateEvent.ViewType.Group) {
            groups = true;
            getView().asWidget().setVisible(false);
        } else {
            groups = false;
            getView().asWidget().setVisible(true);
        }
    }

    @Override
    public void onProteinUpdateEvent(ProteinUpdateEvent event) {
        if (!groups && event.getProteins().size() > 0) {
            currentProtein = event.getProteins().get(0);

            Multiset<String> proteinMods = TreeMultiset.create();
            for (ModifiedLocation loc : currentProtein.getModifiedLocations()) {
                proteinMods.add(loc.getModification());
            }

            //This code could be remove and replace by updateList() as soon as the
            // peptides don't contain non biological modifications or a flag no filter them.
            final List<PeptideMatch> peptides = currentProtein.getPeptides();
            if (!peptides.isEmpty()) {
                for (PeptideMatch peptide : peptides) {
                    for (ModifiedLocation item : peptide.getModifiedLocations()) {
                        peptidesMods.add(item.getModification());
                    }
                }
                // We calculate the set to remove non biological modifications
                // present in the peptides but not in the protein
                peptidesMods.retainAll(proteinMods);
            }

            setList(peptidesMods.entrySet());
            selectModifications(peptidesMods);

            getView().loadList();
            getView().showContent();
        }
    }

    @Override
    public void onProteinRequestEvent(ProteinRequestEvent event) {
        // We should display that the list is being loaded
        if (!groups) {
            getView().loadLoadingMessage();
        }
    }

    @Override
    public void onPeptideUpdateEvent(PeptideUpdateEvent event) {
        if (event.getPeptides().isEmpty()) {
            selectedPeptides = Collections.emptyList();
        } else {
            selectedPeptides = event.getPeptides();
        }
    }

    @Override
    public void onModificationUpdateEvent(ModificationUpdateEvent event) {
        // Reset selection
        for (Multiset.Entry<String> mod : selectedModifications) {
            deselectItem(mod);
        }

        //Update the selection if it is possible
        selectedModifications = new ArrayList<>();
        for (String item : event.getModifications()) {
            for (Multiset.Entry<String> entry : dataProvider.getList()) {
                if (entry.getElement().equals(item)) {
                    selectedModifications.add(entry);
                    break;
                }
            }
        }

        // We collect only the possible moodifications available in the peptides after filtering by selected tissue and selected mods
        updateList(PeptideUtils.filterPeptideMatches(currentProtein.getPeptides(),
                selectedTissues, event.getModifications()));
    }

    @Override
    public void onTissueUpdateEvent(TissueUpdateEvent event) {

        selectedTissues = event.getTissues();

        for (Multiset.Entry<String> selectedModification : selectedModifications) {
            deselectItem(selectedModification);
        }


        //We collect only the possible moodifications available in the peptides after filtering by tissue
        updateList(PeptideUtils.filterPeptideMatches(currentProtein.getPeptides(),
                selectedTissues, extractModifications(selectedModifications)));
    }


    @Override
    public void onSelectionChanged(Collection<Multiset.Entry<String>> items) {
        StateChanger changer;
        UserAction action;
        List<PeptideMatch> filteredPeptides;

        // an empty selection is represented by a list with a null item,
        // we represent that with an empty list, so we have to add an
        // additional check for that.
        if ((items.containsAll(selectedModifications) &&
                selectedModifications.containsAll(items)) ||
                (items.contains(null) && selectedModifications.isEmpty())) {
            return;
        } else if (items.contains(null)) {
            items = Collections.emptyList();
        }

        Collection<String> selection = extractModifications(items);

        filteredPeptides = new ArrayList<>();
        for (PeptideMatch pep : selectedPeptides) {
            // If the collections are disjoint means the peptide doesn't have
            // any modifications in items. If this happens, we filter it out.
            if (!Collections.disjoint(PeptideUtils.extractModifications(pep), selection) || selection.isEmpty()) {
                filteredPeptides.add(pep);
            }
        }

        changer = new StateChanger();
        changer.addModificationChange(selection);
        if (filteredPeptides.size() < selectedPeptides.size()) {
            changer.addPeptideChange(filteredPeptides);
        }
        if (selection.isEmpty()) {
            action = new UserAction(UserAction.Type.modification, "Click Reset");
        } else {
            action = new UserAction(UserAction.Type.modification, "Click Set");
        }
        StateChangingActionEvent.fire(this, changer, action);
    }

    private List<String> extractModifications(Collection<Multiset.Entry<String>> selectedModifications) {
        Set<String> result = new TreeSet<>();
        for (Multiset.Entry<String> selectedModification : selectedModifications) {
            result.add(selectedModification.getElement());
        }

        return  Lists.newArrayList(result);
    }

    private void updateList(Collection<PeptideMatch> peptides) {
        if (!peptides.isEmpty()) {
            Multiset<String> filteredModifications = TreeMultiset.create();

            for (PeptideMatch peptide : peptides) {
                filteredModifications.addAll(PeptideUtils.extractModifications(peptide));
            }
            // We calculate the set to remove non biological modifications
            // present in the peptides but not in the protein
            filteredModifications.retainAll(peptidesMods);
            setList(filteredModifications.entrySet());
            selectModifications(filteredModifications);
        }
    }

    private void selectModifications(Multiset<String> modifications) {
        for (Multiset.Entry<String> item : modifications.entrySet()) {
            for (Multiset.Entry<String> entry : selectedModifications) {
                if (entry.getElement().equals(item.getElement())) {
                    selectItem(entry);
                    break;
                }
            }
        }
    }


    private void selectItem(Multiset.Entry<String> mod) {
        int modPosition = -1;
        for (int i = 0; i < dataProvider.getList().size(); i++) {
            Multiset.Entry<String> entry = dataProvider.getList().get(i);
            if (entry.getElement().equals(mod.getElement())) {
                modPosition = i;
                break;
            }
        }

        if (modPosition > -1) {
            selectionEventsDisabled = true;
            getView().selectItemOn(modPosition);
            getView().focusItemOn(modPosition);
            selectionEventsDisabled = false;
        }
    }

    private void deselectItem(Multiset.Entry<String> mod) {
        int modPosition = -1;
        for (int i = 0; i < dataProvider.getList().size(); i++) {
            Multiset.Entry<String> entry = dataProvider.getList().get(i);
            if (entry.getElement().equals(mod.getElement())) {
                modPosition = i;
                break;
            }
        }

        if (modPosition > -1) {
            selectionEventsDisabled = true;
            getView().deselectItemOn(modPosition);
            selectionEventsDisabled = false;
        }
    }

    /**
     * This method is used whenever a new list is set as the model of the view.
     *
     * We clear the list and repopulate it because if we simply reset the
     * data provider and data sorter references to the list it won't work.
     */
    private void setList(final Collection<Multiset.Entry<String>> list) {
        dataProvider.getList().clear();
        dataProvider.getList().addAll(list);
        dataSorter.repeatSort();
        dataProvider.flush();
        getView().loadList();
    }
}
