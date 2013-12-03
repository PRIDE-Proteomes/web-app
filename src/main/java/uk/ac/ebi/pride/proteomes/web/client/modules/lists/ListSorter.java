package uk.ac.ebi.pride.proteomes.web.client.modules.lists;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;

import java.util.*;

/**
 * This class is needed to be able to keep lists sorted when they are updated
 * The normal implementation ({@link com.google.gwt.user.cellview
 * .ColumnSortEvent.ListHandler}) doesn't store the properties of the last sort
 * event, we should do that, as well as provide a method to repeat the last
 * ordering.
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 03/12/13
 *         Time: 10:47
 */
public class ListSorter<T> implements ColumnSortEvent.Handler {
    private final Map<Column<?, ?>, Comparator<T>> comparators = new HashMap<Column<?, ?>, Comparator<T>>();
    private List<T> list;

    private Comparator<T> lastSortingComparator = null;
    private boolean wasLastSortAscending;

    public ListSorter(List<T> list) {
        this.list = list;
    }
    /**
     * Returns the comparator that has been set for the specified column, or
     * null if no comparator has been set.
     *
     * @param column the {@link Column}
     */
    public Comparator<T> getComparator(Column<T, ?> column) {
        return comparators.get(column);
    }

    public List<T> getList() {
        return list;
    }

    @Override
    public void onColumnSort(ColumnSortEvent event) {
        // Get the sorted column.
        Column<?, ?> column = event.getColumn();
        if (column == null) {
            return;
        }

        // Get the comparator.
        final Comparator<T> comparator = comparators.get(column);
        if (comparator == null) {
            return;
        }

        // Keep track of what we're sorting and sort using the comparator.
        sort(comparator, event.isSortAscending());

        lastSortingComparator = comparator;
        wasLastSortAscending = event.isSortAscending();
    }

    /**
     * Set the comparator used to sort the specified column in ascending order.
     *
     * @param column the {@link Column}
     * @param comparator the {@link Comparator} to use for the {@link Column}
     */
    public void setComparator(Column<T, ?> column, Comparator<T> comparator) {
        comparators.put(column, comparator);
    }

    public void setList(List<T> list) {
        assert list != null : "list cannot be null";
        this.list = list;
    }

    public void repeatSort() {
        if(lastSortingComparator != null) {
            sort(lastSortingComparator, wasLastSortAscending);
        }
    }

    private void sort(final Comparator<T> comparator, boolean isSortAscending) {
        if (isSortAscending) {
            Collections.sort(list, comparator);
        } else {
            Collections.sort(list, new Comparator<T>() {
                public int compare(T o1, T o2) {
                    return -comparator.compare(o1, o2);
                }
            });
        }
    }
}
