package uk.ac.ebi.pride.proteomes.web.client.modules.modifications;

import com.google.common.collect.Lists;
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
import java.util.logging.Logger;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 26/11/13
 *         Time: 10:35
 */
public class ModificationsPresenter extends Presenter<ListView<String>>
        implements ListUiHandler<String>,
        ValidStateEvent.Handler,
        ProteinUpdateEvent.Handler,
        ProteinRequestEvent.Handler,
        PeptideUpdateEvent.Handler,
        TissueUpdateEvent.Handler,
        ModificationUpdateEvent.Handler {

    private static Logger logger = Logger.getLogger(ModificationsPresenter.class.getName());
    private final ListDataProvider<String> dataProvider = new ListDataProvider<>();
    private final ListSorter<String> dataSorter = new ListSorter<>(new ArrayList<String>());

    private boolean groups = true;
    private boolean selectionEventsDisabled = false;


    private Protein currentProtein;
    private List<String> selectedModifications = Collections.emptyList();

    private List<String> selectedTissues = Collections.emptyList();
    private List<? extends PeptideMatch> selectedPeptides = Collections.emptyList();

    // Biological modifications extracted from all the peptides of the protein.
    private Set<String> proteinMods = new TreeSet<>();

    public ModificationsPresenter(EventBus eventBus, ListView<String> view) {
        super(eventBus, view);
        List<Column<String, ?>> columns = ModificationColumnProvider.getSortingColumns(dataSorter);
        List<String> columnTitles = ModificationColumnProvider.getColumnTitles();
        List<String> columnWidths = ModificationColumnProvider.getColumnWidths();

        dataSorter.setList(dataProvider.getList());
        view.addDataProvider(dataProvider);
        view.addColumns(columns, columnTitles, columnWidths);
        view.addColumnSortHandler(dataSorter);
        view.addUiHandler(this);
        view.setHeight("120px");
        view.asWidget().setVisible(false);

        final OrderedMultiSelectionModel<String> selectionModel = new OrderedMultiSelectionModel<>();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (!selectionEventsDisabled) {
                    for (ListUiHandler<String> handler : getView().getUiHandlers()) {
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

            for (ModifiedLocation modifiedLocation : currentProtein.getModifiedLocations()) {
                proteinMods.add(modifiedLocation.getModification());
            }
            // Biological modifications extracted from all the peptides of the protein.
            // It takes into account the number of times that a modification appear

            updateList(proteinMods);
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
        for (String selectedModification : selectedModifications) {
            deselectItem(selectedModification);
        }

        selectedModifications = event.getModifications();

        updateModifications();
    }

    @Override
    public void onTissueUpdateEvent(TissueUpdateEvent event) {
        // Reset selection
        for (String selectedModification : selectedModifications) {
            deselectItem(selectedModification);
        }

        selectedTissues = event.getTissues();
        updateModifications();

    }

    @Override
    public void onSelectionChanged(Collection<String> items) {
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

        filteredPeptides = new ArrayList<>();
        for (PeptideMatch peptide : selectedPeptides) {
            Set<String> filteredModifications = new TreeSet<>();
            filteredModifications.addAll(PeptideUtils.extractModificationTypes(peptide));
            filteredModifications.retainAll(proteinMods);

            // If the collections are disjoint means the peptide doesn't have
            // any modifications in items. If this happens, we filter it out.
            if (!Collections.disjoint(filteredModifications, items) || items.isEmpty()) {
                filteredPeptides.add(peptide);
            }
        }


        changer = new StateChanger();
        changer.addModificationChange(items);
        if (filteredPeptides.size() < selectedPeptides.size()) {
            changer.addPeptideChange(filteredPeptides);
        }
        if (items.isEmpty()) {
            action = new UserAction(UserAction.Type.modification, "Click Reset");
        } else {
            action = new UserAction(UserAction.Type.modification, "Click Set");
        }
        StateChangingActionEvent.fire(this, changer, action);
    }


    @Override
    public void onSelectionChanged(String item) {
        //As this list allows multiselection, this doesn't have anything to do
    }

    private void updateModifications() {
        //We collect only the possible modifications available in the peptides after filtering by selected tissue and selected mods
        List<PeptideMatch> peptides = PeptideUtils.filterPeptideMatches(currentProtein.getPeptides(),
                selectedTissues, selectedModifications);

        if (!peptides.isEmpty()) {
            Set<String> filteredModifications = new TreeSet<>();

            for (PeptideMatch peptide : peptides) {
                filteredModifications.addAll(PeptideUtils.extractModificationTypes(peptide));
            }

            // We calculate the set to remove possible modifications
            // present in the peptides but not in the protein
            filteredModifications.retainAll(proteinMods);

            updateList(Lists.newArrayList(filteredModifications));
        }
    }

    private void updateList(Collection<? extends String> modifications) {

        setList(modifications);
        for (String modification : selectedModifications) {
            selectItem(modification);
        }
    }

    private void selectItem(String modification) {

        selectionEventsDisabled = true;
        getView().focusItemOn(dataProvider.getList().indexOf(modification));
        getView().selectItemOn(dataProvider.getList().indexOf(modification));
        selectionEventsDisabled = false;
    }

    private void deselectItem(String modification) {
        selectionEventsDisabled = true;
        getView().deselectItemOn(dataProvider.getList().indexOf(modification));
        selectionEventsDisabled = false;
    }

    /**
     * This method is used whenever a new list is set as the model of the view.
     * <p/>
     * We clear the list and repopulate it because if we simply reset the
     * data provider and data sorter references to the list it won't work.
     */
    private void setList(final Collection<? extends String> list) {
        dataProvider.getList().clear();
        dataProvider.getList().addAll(list);
        dataProvider.flush();
        getView().loadList();
        getView().showContent();
    }
}
