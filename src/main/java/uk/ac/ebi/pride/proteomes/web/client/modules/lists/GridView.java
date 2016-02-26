package uk.ac.ebi.pride.proteomes.web.client.modules.lists;

import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
import uk.ac.ebi.pride.proteomes.web.client.images.FileImages;
import uk.ac.ebi.pride.proteomes.web.client.modules.ViewWithUiHandlers;
import uk.ac.ebi.pride.proteomes.web.client.utils.StringUtils;
import uk.ac.ebi.pride.proteomes.web.client.utils.factories.ModuleContainerFactory;
import uk.ac.ebi.pride.widgets.client.disclosure.client.ModuleContainer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    Logger logger = Logger.getLogger(GridView.class.getName());

    private DataGridWithScroll<T> grid;
    private ModuleContainer frame;
    private PushButton downloadBtn;
    private FlowPanel flowPanel;
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

    public GridView(final String title, String typeName, boolean skipRowHoverStyleUpdate) {

        baseType = typeName;
        selection = new HashSet<>();

        grid = new DataGridWithScroll<>();
        grid.addRowCountChangeHandler(this);
        grid.setEmptyTableWidget(new Label("No " + baseType + "s match the selection."));

        grid.setWidth("100%");
        grid.setHeight("200px");
        grid.setSkipRowHoverCheck(skipRowHoverStyleUpdate);

        // This allows for the data grid to show up to 4096 peptides in the
        // view, there should be a way to allow the view to adjust for any
        // number of peptides.
        grid.setPageSize(4096);

        downloadBtn =  new PushButton(new Image(FileImages.INSTANCE.tsvPngFile()), new ClickHandler() {
            public void onClick(ClickEvent event) {
                download(createTsvFile(), title + ".tsv");
            }
        });

//        downloadBtn.setStyleName("export-Button");
        downloadBtn.setTitle("Click here to export the data as a .tsv file");

        flowPanel = new FlowPanel();
        flowPanel.add(downloadBtn);
        flowPanel.add(grid);

        frame = ModuleContainerFactory.getModuleContainer(title);
        frame.setWidth("100%");
        frame.setContent(flowPanel);

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
            int offset = grid.getRowElement(row).getOffsetTop();
            grid.getScrollPanel().setVerticalScrollPosition(offset);
            logger.log(Level.INFO, "Scroll active row: " + row);
        }
    }

    @Override
    public void loadLoadingMessage() {
        frame.setContent(ModuleContainer.getLoadingPanel());
    }

    @Override
    public void loadList() {
        frame.setContent(flowPanel);
        frame.clearPrimaryMessage();
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
    public void setHeight(String height) {
        grid.setHeight(height);
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
        grid.setRowCount(number);
    }


    /**
     * Uses files in resources/public to enable download.
     * @param text from grid
     * @param fileName customize for the download
     *
     * The JavaScript for download is FileSaver. This one (sometimes) needs Blob as
     * the standard W3C File API Blob interface is not available in all browsers.
     * Blob.js is a cross-browser Blob implementation that solves this.
     * https://github.com/eligrey/FileSaver.js/blob/master/README.md
     */
    public static native void download(String text, String fileName) /*-{
        $wnd.saveAs(
            new Blob(
                [text]
                , {type: "text/plain;charset=utf-8;"}
            )
            , fileName
        );
    }-*/;

    /**
     * This method transforms the model to plane text to be exported with FileSaver
     *
     * @return the content for the file as string
     */
    private String createTsvFile() {
        StringBuilder stringBuilder = new StringBuilder();

        int numColumns = grid.getColumnCount();
        for (int i = 0; i < numColumns; i++) {
            stringBuilder.append(grid.getHeader(i).getValue());
            if (i < numColumns - 1) {
                stringBuilder.append("\t");
            } else {
                stringBuilder.append("\n");
            }
        }

        int numRows = grid.getRowCount();
        for (int j = 0; j < numRows; j++) {
            for (int i = 0; i < numColumns; i++) {
                stringBuilder.append(grid.getRowElement(j).getCells().getItem(i).getInnerText());
                if (i < numColumns - 1) {
                    stringBuilder.append("\t");
                } else {
                    stringBuilder.append("\n");
                }
            }
        }

        return stringBuilder.toString();
    }
}
