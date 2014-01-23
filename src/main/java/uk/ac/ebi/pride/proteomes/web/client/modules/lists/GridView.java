package uk.ac.ebi.pride.proteomes.web.client.modules.lists;

import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.*;
import uk.ac.ebi.pride.proteomes.web.client.modules.ViewWithUiHandlers;
import uk.ac.ebi.pride.proteomes.web.client.utils.StringUtils;
import uk.ac.ebi.pride.proteomes.web.client.utils.factories.ModuleContainerFactory;
import uk.ac.ebi.pride.widgets.client.disclosure.client.ModuleContainer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 19/11/13
 *         Time: 14:27
 */
public class GridView<T> extends ViewWithUiHandlers<ListUiHandler<T>>
                         implements ListView<T>,
                                    RowCountChangeEvent.Handler,
                                    OpenHandler<DisclosurePanel>,
                                    HasKeyboardSelectionPolicy {

    private DataGrid<T> grid;
    private ModuleContainer frame;
    private Set<T> selection;

    private final String baseType;

    /* This selection manager allows the user to deselect a column just by
       clicking it, instead of having to use the ctrl modifier.
     */
    private DefaultSelectionEventManager<T> selectionManager =
            DefaultSelectionEventManager.createCustomManager(new DefaultSelectionEventManager.EventTranslator<T>(){
        @Override
        public boolean clearCurrentSelection(CellPreviewEvent<T> event) {
            return false;
        }
        @Override
        public DefaultSelectionEventManager.SelectAction translateSelectionEvent(CellPreviewEvent<T> event) {
            NativeEvent nativeEvent = event.getNativeEvent();
            if(BrowserEvents.CLICK.equals(nativeEvent.getType())) {
                return DefaultSelectionEventManager.SelectAction.TOGGLE;
            }
            else {
                return DefaultSelectionEventManager.SelectAction.DEFAULT;
            }
        }
    });

    public GridView(String title, String typeName) {
        this(title, typeName, false);
    }

    public GridView(String title, String typeName, boolean skipRowHoverStyleUpdate) {
        frame = ModuleContainerFactory.getModuleContainer(title);
        grid = new DataGrid<>();
        baseType = typeName;
        selection = new HashSet<>();

        grid.addRowCountChangeHandler(this);
        grid.setEmptyTableWidget(new Label("No " +
                baseType + "s match the selection."));

        grid.setWidth("99%");
        grid.setHeight("150px");
        grid.setSkipRowHoverCheck(skipRowHoverStyleUpdate);

        // This allows for the data grid to show up to 4096 peptides in the
        // view, there should be a way to allow the view to adjust for any
        // number of peptides.
        grid.setPageSize(4096);

        frame.setWidth("100%");
        frame.setContent(grid);

        frame.addOpenHandler(this);
    }

    @Override
    public void selectItemOn(int row) {
        if(row >= 0 && row < grid.getRowCount()) {
            grid.getSelectionModel().setSelected(grid.getVisibleItem(row), true);
            selection.add(grid.getVisibleItem(row));

            frame.setOpen(true);
            StringBuilder sb = new StringBuilder();
            for(T item : selection) {
                sb.append(StringUtils.getName(item)).append(", ");
            }
            frame.setPrimaryMessage(sb.substring(0, sb.length() - 2));
        }
    }

    @Override
    public void deselectItemOn(int row) {
        if(row >= 0 && row < grid.getRowCount()) {
            grid.getSelectionModel().setSelected(grid.getVisibleItem(row), false);
            selection.remove(grid.getVisibleItem(row));

            if(selection.isEmpty()) {
                frame.clearPrimaryMessage();
            }
            else {
                StringBuilder sb = new StringBuilder();
                for(T item : selection) {
                    sb.append(StringUtils.getName(item)).append(", ");
                }
                frame.setPrimaryMessage(sb.substring(0, sb.length() - 2));
            }
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
    public void loadLoadingMessage() {
        frame.setContent(ModuleContainer.getLoadingPanel());
    }

    @Override
    public void loadList() {
        frame.setContent(grid);
        frame.clearPrimaryMessage();
        updateItemCount(grid.getRowCount());
    }

    @Override
    public void showContent() {
        frame.setOpen(true);
        grid.setVisible(true);
    }

    @Override
    public void hideContent() {
        grid.setVisible(false);
        frame.setOpen(false);
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
    public void setVisible(boolean visible) {
        asWidget().setVisible(visible);
    }

    @Override
    public Widget asWidget() {
        return frame;
    }

    @Override
    public void onRowCountChange(RowCountChangeEvent event) {
        updateItemCount(event.getNewRowCount());
    }

    @Override
    public void onOpen(OpenEvent<DisclosurePanel> event) {
        grid.redraw();
    }

    @Override
    public void setSelectionModel(SelectionModel<? super T> selectionModel) {
        grid.setSelectionModel(selectionModel, selectionManager);
    }

    @Override
    public void setKeyboardSelectionHandler(CellPreviewEvent.Handler<T> keyboardSelectionReg) {
        grid.setKeyboardSelectionHandler(keyboardSelectionReg);
    }


    @Override
    public KeyboardSelectionPolicy getKeyboardSelectionPolicy() {
        return grid.getKeyboardSelectionPolicy();
    }

    @Override
    public void setKeyboardSelectionPolicy(KeyboardSelectionPolicy policy) {
        grid.setKeyboardSelectionPolicy(policy);
    }

    private void updateItemCount(int number) {
        frame.setSecondaryMessage(StringUtils.getCount(baseType, number));
    }
}
