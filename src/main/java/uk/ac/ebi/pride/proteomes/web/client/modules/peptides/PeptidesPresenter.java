package uk.ac.ebi.pride.proteomes.web.client.modules.peptides;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.view.client.ListDataProvider;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.ProteinRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.GroupUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.PeptideUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.ProteinUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.RegionUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.modules.Presenter;

import java.util.ArrayList;
import java.util.List;

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
                                          PeptideUpdateEvent.PeptideUpdateHandler
{
    public interface View extends uk.ac.ebi.pride.proteomes.web.client.modules.View {
        void selectItemOn(int row);
        void deselectItemOn(int row);

        // Used to scroll to a row in the list
        void focusItemOn(int row);

        void showLoadingMessage();
        void showList();

        // Used to inject the list displayed in the view,
        // this frees the view from the list manipulation, e.g. sorting.
        void bindDataProvider(ListDataProvider<PeptideMatch> dataProvider);
        void addColumns(List<Column<PeptideMatch, ?>> columns);
        void addColumnSortHandler(ColumnSortEvent.ListHandler<PeptideMatch> sorter);
    }
    private final EventBus eventBus;
    private final View view;
    private Protein currentProtein;

    private final ListDataProvider<PeptideMatch> dataProvider;
    private final ColumnSortEvent.ListHandler<PeptideMatch> dataSorter;
    private boolean groups = true;

    public PeptidesPresenter(EventBus eventBus, View view) {
        this.eventBus = eventBus;
        this.view = view;
        dataProvider = new ListDataProvider<PeptideMatch>();
        dataSorter = new ColumnSortEvent.ListHandler<PeptideMatch>(new
                ArrayList<PeptideMatch>());
        List<Column<PeptideMatch, ?>> columns = PeptideColumnProvider
                                            .getSortingColumns(dataSorter);

        view.bindDataProvider(dataProvider);
        view.addColumns(columns);
        view.addColumnSortHandler(dataSorter);

        eventBus.addHandler(GroupUpdateEvent.getType(), this);
        eventBus.addHandler(ProteinUpdateEvent.getType(), this);
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
                updateList(currentProtein.getPeptides());
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
        // We need to filter the proteins contained in the region
    }

    @Override
    public void onPeptideUpdateEvent(PeptideUpdateEvent event) {
        // We should select a peptide
    }

    /**
     * This method is used whenever a new list is set as the model of the view,
     * this is because the class that reacts to sorting events is bound to
     * the list, not the data provider, this means that we have to empty the
     * list and then fill it again in order to be able to order any loaded list.
     *
     * If the presenter had a reference to the sortEventHandler, setList
     * could be used in the dataProvider and the sortEventHandler
     * but then the presenter-view interface gets bigger and the order of
     * creation of the presenter and the view has to be designed very
     * carefully. Just kidding, we have now the reference to the sorter,
     * motherfuckers :D
     *
     * We also have to make sure the peptide list cannot be modified,
     * as this may change the list contained inside Proteins
     */

    private void updateList(final List<PeptideMatch> peptideList) {
        dataProvider.getList().clear();
        dataProvider.getList().addAll(peptideList);
    }
}
