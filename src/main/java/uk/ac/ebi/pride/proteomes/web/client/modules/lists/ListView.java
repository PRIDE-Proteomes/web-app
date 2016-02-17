package uk.ac.ebi.pride.proteomes.web.client.modules.lists;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionModel;
import uk.ac.ebi.pride.proteomes.web.client.modules.HasUiHandlers;
import uk.ac.ebi.pride.proteomes.web.client.modules.View;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 19/11/13
 *         Time: 14:22
 */
public interface ListView<T> extends View, HasUiHandlers<ListUiHandler<T>> {

    void selectItemOn(int row);
    void deselectItemOn(int row);

    // Used to scroll to a row in the list
    void focusItemOn(int row);

    void loadLoadingMessage();
    void loadList();
    //For now these is only used by the modifications but allows selection by position, not only by type. Should be more generic
    void loadListWithSelection(List<List<String>> selection);

    void showContent();
    void hideContent();

    void setHeight(String height);

    // Used to inject the list displayed in the view,
    // this frees the view from the list manipulation, e.g. sorting.
    void addDataProvider(ListDataProvider<T> dataProvider);
    void addColumns(List<Column<T, ?>> columns, List<String> titles, List<String> widths);
    void addColumnSortHandler(ListSorter<T> sorter);

    // Used to manage the selection
    void setSelectionModel(SelectionModel<? super T> selectionModel);
    void setKeyboardSelectionHandler(CellPreviewEvent.Handler<T> keyboardSelectionReg);
    void setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy policy);
}
