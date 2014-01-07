package uk.ac.ebi.pride.proteomes.web.client.modules.grouppeptides;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.events.state.ValidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.GroupUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.modules.Presenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListSorter;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListView;
import uk.ac.ebi.pride.proteomes.web.client.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 06/01/14
 *         Time: 15:36
 */
public class GroupPeptidePresenter extends Presenter<ListView<Pair<String, List<String>>>>
                                   implements ValidStateEvent.Handler,
                                              GroupUpdateEvent.Handler {
    private final ListDataProvider<Pair<String, List<String>>> dataProvider = new
            ListDataProvider<Pair<String, List<String>>>();
    private final ListSorter<Pair<String, List<String>>> dataSorter = new
            ListSorter<Pair<String, List<String>>>(new ArrayList<Pair<String, List<String>>>());

    private boolean groups = true;

    public GroupPeptidePresenter(EventBus eventBus, ListView<Pair<String, List<String>>> view) {
        super(eventBus, view);

        List<Column<Pair<String, List<String>>, ?>> columns = GroupPeptideColumnProvider
                .getSortingColumns(dataSorter);
        List<String> columnTitles = GroupPeptideColumnProvider.getColumnTitles();
        List<String> columnWidths = GroupPeptideColumnProvider.getColumnWidths();

        dataSorter.setList((dataProvider.getList()));
        view.addDataProvider(dataProvider);
        view.addColumns(columns, columnTitles, columnWidths);
        view.addColumnSortHandler(dataSorter);
        view.asWidget().setVisible(false);

        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(GroupUpdateEvent.getType(), this);
    }

    @Override
    public void onValidStateEvent(ValidStateEvent event) {
        if(event.getViewType() != ValidStateEvent.ViewType.Group) {
            groups = false;
            getView().asWidget().setVisible(false);
        }
        else {
            groups = true;
            getView().asWidget().setVisible(true);
        }
    }

    @Override
    public void onGroupUpdateEvent(GroupUpdateEvent event) {
        if(groups && event.getGroups().size() > 0) {
            updateList(event.getGroups().get(0).getUniquePeptides());
            getView().loadList();
            getView().showContent();
        }
    }

    private void updateList(Map<String, List<String>> peptideMap) {
        List<Pair<String, List<String>>> newList = new ArrayList<Pair<String, List<String>>>();
        for(Map.Entry<String, List<String>> entry : peptideMap.entrySet()) {
            newList.add(new Pair<String, List<String>>(entry.getKey(), entry.getValue()));
        }
        setList(newList);
    }
    private void setList(final List<Pair<String, List<String>>> pairList) {
        dataProvider.getList().clear();
        dataProvider.getList().addAll(pairList);
        dataSorter.repeatSort();
        dataProvider.flush();
    }
}
