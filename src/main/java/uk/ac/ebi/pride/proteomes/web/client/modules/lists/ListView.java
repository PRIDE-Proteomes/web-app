package uk.ac.ebi.pride.proteomes.web.client.modules.lists;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.view.client.ListDataProvider;
import uk.ac.ebi.pride.proteomes.web.client.modules.View;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 19/11/13
 *         Time: 14:22
 */
public interface ListView<T> extends View<ListUiHandler<T>> {

    void selectItemOn(int row);
    void deselectItemOn(int row);

    // Used to scroll to a row in the list
    void focusItemOn(int row);

    void loadLoadingMessage();
    void loadList();

    void showContent();
    void hideContent();

    // Used to inject the list displayed in the view,
    // this frees the view from the list manipulation, e.g. sorting.
    void addDataProvider(ListDataProvider<T> dataProvider);
    void addColumns(List<Column<T, ?>> columns, List<String> titles, List<String> widths);
    void addColumnSortHandler(ListSorter<T> sorter);
}
