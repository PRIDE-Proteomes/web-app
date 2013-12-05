package uk.ac.ebi.pride.proteomes.web.client.modules.lists;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.*;
import uk.ac.ebi.pride.proteomes.web.client.utils.StringUtils;
import uk.ac.ebi.pride.proteomes.web.client.utils.factories.ModuleContainerFactory;
import uk.ac.ebi.pride.widgets.client.disclosure.client.ModuleContainer;

import java.util.*;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 19/11/13
 *         Time: 14:27
 */
public class GridView<H extends ListUiHandler<T>, T> implements ListView<T>,
                                                     RowCountChangeEvent.Handler
{
    private List<ListUiHandler<T>> handlers = new ArrayList<ListUiHandler<T>>();
    private DataGrid<T> grid;
    private ModuleContainer frame;

    private String baseType;
    private boolean selectionEventsDisabled = false;

    public GridView(String title, String typeName) {
        frame = ModuleContainerFactory.getModuleContainer(title);
        grid = new DataGrid<T>();
        baseType = typeName;

        grid.addRowCountChangeHandler(this);

        grid.setSelectionModel(getSelectionModel());
        grid.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);

        grid.setEmptyTableWidget(new Label("No " +
                baseType + "s match the selection."));

        grid.setWidth("99%");
        grid.setHeight("150px");

        // This allows for the data grid to show up to 4096 peptides in the
        // view, there should be a way to allow the view to adjust for any
        // number of peptides.
        grid.setPageSize(4096);

        frame.setWidth("100%");
        frame.setContent(grid);
    }
    @Override
    public void selectItemOn(int row) {
        if(row >= 0 && row < grid.getRowCount()) {
            grid.getSelectionModel().setSelected(grid.getVisibleItem(row), true);
            frame.setOpen(true);
            frame.setPrimaryMessage(StringUtils.getName(grid.getVisibleItem(row)));
        }
    }

    @Override
    public void deselectItemOn(int row) {
        if(row >= 0 && row < grid.getRowCount()) {
            selectionEventsDisabled = true;
            grid.getSelectionModel().setSelected(grid.getVisibleItem(row), false);
            frame.clearPrimaryMessage();
        }
    }

    @Override
    public void focusItemOn(int row) {
        //scroll to the first column of the required row,
        // this way we ensure the horizontal scroll is at its leftmost.
        if(row >= 0 && row < grid.getRowCount()) {
            grid.getRowElement(row).getCells().getItem(0).scrollIntoView();
        }
    }

    @Override
    public void showLoadingMessage() {
        frame.setContent(ModuleContainer.getLoadingPanel());
    }

    @Override
    public void showList() {
        frame.setContent(grid);
        frame.clearPrimaryMessage();
        frame.setOpen(true);
        updateItemCount(grid.getRowCount());
    }

    @Override
    public void addDataProvider(ListDataProvider<T> dataProvider) {
        dataProvider.addDataDisplay(grid);
    }

    @Override
    public void addColumns(List<Column<T, ?>> columns, List<String> titles, List<String> widths) {
        for(int i = 0; i < columns.size(); i ++) {
            grid.addColumn(columns.get(i), titles.get(i));
            grid.setColumnWidth(columns.get(i), widths.get(i));
        }
    }

    @Override
    public void addColumnSortHandler(ListSorter<T> sorter) {
        grid.addColumnSortHandler(sorter);
    }

    @Override
    public void bindToContainer(AcceptsOneWidget container) {
        container.setWidget(frame);
    }

    @Override
    public void addUiHandler(ListUiHandler<T> handler) {
        handlers.add(handler);
    }

    @Override
    public Collection<ListUiHandler<T>> getUiHandlers() {
        return handlers;
    }

    @Override
    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    @Override
    public Widget asWidget() {
        return frame;
    }

    @Override
    public void onRowCountChange(RowCountChangeEvent event) {
        updateItemCount(event.getNewRowCount());
    }

    private void updateItemCount(int number) {
        frame.setSecondaryMessage(StringUtils.getCount(baseType, number));
    }

    private SelectionModel<T> getSelectionModel() {
        final SingleSelectionModel<T> selectionModel = new
                SingleSelectionModel<T>();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                if(!selectionEventsDisabled) {
                    Set<T> selection = new HashSet<T>();
                    selection.add(selectionModel.getSelectedObject());
                    for(ListUiHandler<T> handler : getUiHandlers()) {
                        handler.onSelectionChanged(selection);
                    }
                }
                else {
                    selectionEventsDisabled = false;
                }
            }
        });
        return selectionModel;
    }
}
