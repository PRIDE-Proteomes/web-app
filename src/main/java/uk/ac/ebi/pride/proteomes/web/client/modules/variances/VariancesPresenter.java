package uk.ac.ebi.pride.proteomes.web.client.modules.variances;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideList;
import uk.ac.ebi.pride.proteomes.web.client.events.state.ValidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.PeptideUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.modules.Presenter;
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
                                           ListUiHandler<Peptide>
{
    private final EventBus eventBus;
    private final ListView<Peptide> view;
    private final ListDataProvider<Peptide> dataProvider = new
                                        ListDataProvider<Peptide>();
    private final ColumnSortEvent.ListHandler<Peptide> dataSorter = new
                                        ColumnSortEvent.ListHandler<Peptide>(new ArrayList<Peptide>());

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
        if(!groups && event.getPeptides().size() > 0) {
            currentPeptide = event.getPeptides().get(0);

            updateList(currentPeptide.getPeptideList());
            view.showList();
        }
    }

    @Override
    public void onSelectionChanged(Collection<Peptide> items) {
        // For now there's no way we can rely the id of the peptide variance
        // to the rest of the application, so we don't do anything at all.
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
        int peptidePosition = PeptideUtils.firstIndexOf(dataProvider.getList
                (), peptide.getSequence());

        if(peptidePosition > -1) {
            view.selectItemOn(peptidePosition);
        }
    }

    private void deselectItem(Peptide peptide) {
        // search the first occurrence of the peptide, we can only select
        // one because of the selection model
        int peptidePosition = PeptideUtils.firstIndexOf(dataProvider.getList
                (), peptide.getSequence());

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
    private void setList(final List<Peptide> peptideList) {
        dataProvider.setList(peptideList);
        dataSorter.setList(peptideList);
    }
}
