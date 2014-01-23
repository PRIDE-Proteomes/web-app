package uk.ac.ebi.pride.proteomes.web.client.modules.grouppeptides;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListSorter;
import uk.ac.ebi.pride.proteomes.web.client.utils.Pair;
import uk.ac.ebi.pride.proteomes.web.client.utils.factories.HyperlinkFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 06/01/14
 *         Time: 16:01
 */
public class GroupPeptideColumnProvider {
    public static List<Column<Pair<String, List<String>>, ?>> getSortingColumns
            (ListSorter<Pair<String, List<String>>> sorter) {

        List<Column<Pair<String, List<String>>, ?>> columns = new ArrayList<>();

        TextColumn<Pair<String, List<String>>> sequenceColumn = new TextColumn<Pair<String, List<String>>>() {
            @Override
            public String getValue(Pair<String, List<String>> object) {
                return object.getA();
            }
        };

        sequenceColumn.setSortable(true);
        sorter.setComparator(sequenceColumn, new Comparator<Pair<String, List<String>>>() {
            @Override
            public int compare(Pair<String, List<String>> o1, Pair<String, List<String>> o2) {
                return o1.getA().compareTo(o2.getA());
            }
        });

        Column<Pair<String, List<String>>, SafeHtml> proteinsColumn = new Column<Pair<String, List<String>>, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(Pair<String, List<String>> object) {
                FlowPanel proteins = new FlowPanel();
                for(String protein : object.getB()) {
                    proteins.add(HyperlinkFactory.getInlineHyperLink(protein, "protein=" + protein + "&peptide=" + object.getA()));
                    if(!protein.equals(object.getB().get(object.getB().size() - 1))) {
                        proteins.add(new InlineLabel(" "));
                    }
                }
                return SafeHtmlUtils.fromSafeConstant(proteins.toString());
            }
        };

        proteinsColumn.setSortable(true);
        sorter.setComparator(proteinsColumn, new Comparator<Pair<String, List<String>>>() {
            @Override
            public int compare(Pair<String, List<String>> o1, Pair<String, List<String>> o2) {
                Integer size1 = o1.getB().size();
                Integer size2 = o2.getB().size();
                int result = size1.compareTo(size2);
                if (result == 0) {
                    String accs1 = "";
                    for (String s : o1.getB()) {
                        accs1 += s;
                    }
                    String accs2 = "";
                    for (String s : o2.getB()) {
                        accs2 += s;
                    }
                    result = accs1.compareTo(accs2);
                }
                return result;
            }
        });

        columns.add(sequenceColumn);
        columns.add(proteinsColumn);

        return columns;
    }

    public static List<String> getColumnTitles() {
        List<String> titles = new ArrayList<>();
        Collections.addAll(titles, "Sequence", "Proteins");
        return titles;
    }

    public static List<String> getColumnWidths() {
        List<String> widths = new ArrayList<>();
        Collections.addAll(widths, "40%", "60%");
        return widths;
    }

    public static HasKeyboardSelectionPolicy.KeyboardSelectionPolicy getKeyboardSelectionPolicy() {
        return HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED;
    }
}
