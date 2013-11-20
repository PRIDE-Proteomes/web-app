package uk.ac.ebi.pride.proteomes.web.client.modules.peptides;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.view.client.ListDataProvider;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Region;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.ProteinRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.StateChangingActionEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.GroupUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.PeptideUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.ProteinUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.RegionUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.modules.Presenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.history.StateChanger;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListUiHandler;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListView;
import uk.ac.ebi.pride.proteomes.web.client.utils.PeptideUtils;

import java.util.*;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 12/11/13
 *         Time: 09:40
 */
public class PeptidesPresenter implements Presenter,
                                          GroupUpdateEvent.GroupUpdateHandler,
                                          ProteinUpdateEvent.ProteinUpdateHandler,
                                          ProteinRequestEvent.ProteinRequestHandler,
                                          RegionUpdateEvent.RegionUpdateHandler,
                                          PeptideUpdateEvent.PeptideUpdateHandler,
                                          ListUiHandler<PeptideMatch>
{
    private final EventBus eventBus;
    private final ListView<PeptideMatch> view;
    private Protein currentProtein;

    private final ListDataProvider<PeptideMatch> dataProvider = new
                                            ListDataProvider<PeptideMatch>();
    private final ColumnSortEvent.ListHandler<PeptideMatch> dataSorter = new
                                            ColumnSortEvent.ListHandler<PeptideMatch>(new ArrayList<PeptideMatch>());
    private boolean groups = true;
    private Collection<Peptide> selectedPeptides;
    private Protein loadedProtein;

    public PeptidesPresenter(EventBus eventBus, ListView<PeptideMatch> view) {
        this.eventBus = eventBus;
        this.view = view;
        List<Column<PeptideMatch, ?>> columns = PeptideColumnProvider
                                            .getSortingColumns(dataSorter);

        view.addDataProvider(dataProvider);
        view.addColumns(columns);
        view.addColumnSortHandler(dataSorter);

        eventBus.addHandler(GroupUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinRequestEvent.getType(), this);
        eventBus.addHandler(RegionUpdateEvent.getType(), this);
        eventBus.addHandler(PeptideUpdateEvent.getType(), this);
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
    public void onGroupUpdateEvent(GroupUpdateEvent event) {
        // We should check if we have to stay hidden or not
        if(event.getGroups().size() > 0) {
            groups = true;
            view.asWidget().setVisible(false);
        }
        else {
            groups = false;
        }
    }

    @Override
    public void onProteinUpdateEvent(ProteinUpdateEvent event) {
        // We need to extract a peptide list from the ones in the proteins
        if(!groups && event.getProteins().size() > 0) {
            if(event.getProteins().get(0) != currentProtein) {
                currentProtein = event.getProteins().get(0);
                // we should reset filters and ordering here
                setList(currentProtein.getPeptides());
                view.showList();
            }
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
    public void onRegionUpdateEvent(RegionUpdateEvent event) {
        // todo We need to filter the peptides contained in the region

        if(event.getRegions().size() == 0) {
            updateList(loadedProtein.getPeptides());
        }
        else {
            Region region = event.getRegions().get(0);

            updateList(PeptideUtils.filterPeptidesNotIn(loadedProtein.getPeptides(),
                    region.getStart(), region.getEnd()));
        }
    }

    @Override
    public void onPeptideUpdateEvent(PeptideUpdateEvent event) {
        // todo We should (de)select peptides

        for(Peptide peptide : selectedPeptides) {
            deselectPeptide(peptide);
        }

        if(event.getPeptides().size() > 0) {
            selectedPeptides = event.getPeptides();
        }
        else {
            selectedPeptides = Collections.emptyList();
        }
    }

    @Override
    public void onSelectionChanged(Collection<PeptideMatch> items) {
        StateChanger changer;

        if(items.equals(selectedPeptides)) {
            return;
        }

        changer = new StateChanger();

        Set<String> peptideIds = new HashSet<String>();

        for(PeptideMatch peptide : items) {
            peptideIds.add(peptide.getSequence());
        }

        changer.addPeptideChange(peptideIds);
        StateChangingActionEvent.fire(this, changer);
    }

    private void updateList(List<PeptideMatch> peptideList) {
        for(Peptide peptide : selectedPeptides) {
            deselectPeptide(peptide);
        }

        setList(peptideList);

        for(Peptide peptide : selectedPeptides) {
            selectPeptide(peptide);
        }
    }

    private void selectPeptide(Peptide peptide) {
        // todo
        int peptidePosition = dataProvider.getList().indexOf(peptide);

        if(peptidePosition > -1) {
            view.selectItemOn(peptidePosition);
        }
    }

    private void deselectPeptide(Peptide peptide) {
        // todo
        int peptidePosition = dataProvider.getList().indexOf(peptide);

        if(peptidePosition > -1) {
            view.deselectItemOn(peptidePosition);
        }
    }

    /**
     * This method is used whenever a new list is set as the model of the view.
     * We have to update the list reference in the provider and the sorter,
     * otherwise the sorting would not work anymore.
     *
     * Alternatively we could also empty the list and repopulate it with the
     * new data, but that would make it slower.
     */
    private void setList(final List<PeptideMatch> peptideList) {
        dataProvider.setList(peptideList);
        dataSorter.setList(peptideList);
    }
}
