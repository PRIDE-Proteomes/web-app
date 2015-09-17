package uk.ac.ebi.pride.proteomes.web.client.modules.lists;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * This class allows to access to the scrollPanel in the dataGrid.
 * The scrollPanel is not espoused in the default implementation.
 *
 * @author ntoro
 * @since 21/07/15 13:55
 */
public class DataGridWithScroll<T>  extends DataGrid<T> {

    public ScrollPanel getScrollPanel() {
        HeaderPanel header = (HeaderPanel) getWidget();
        return (ScrollPanel) header.getContentWidget();
    }
}
