package uk.ac.ebi.pride.proteomes.web.client.modules.variances;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.ModifiedLocation;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListSorter;

import java.util.*;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 29/11/13
 *         Time: 14:46
 */
public class VarianceColumnProvider {

    public interface SimpleCellTemplates extends SafeHtmlTemplates {
      @Template("<a href=\"{0}\" target=\"_blank\">{1}</a>")
      SafeHtml anchor(SafeUri href, String name);

    }
    public interface MultiLinkCellTemplates extends SafeHtmlTemplates {
      @Template(" {0} ")
      SafeHtml anchor(SafeHtml text);

    }

    static final SimpleCellTemplates cell = GWT.create(SimpleCellTemplates.class);
    static final MultiLinkCellTemplates multiCell = GWT.create(MultiLinkCellTemplates.class);


    public static List<Column<Peptide, ?>> getSortingColumns(ListSorter<Peptide> sorter) {

        List<Column<Peptide, ?>> columns = new ArrayList<Column<Peptide, ?>>();

        TextColumn<Peptide> sequenceColumn = new TextColumn<Peptide>() {
            @Override
            public String getValue(Peptide object) {
                return object.getSequence();
            }
        };

        sequenceColumn.setSortable(true);
        sorter.setComparator(sequenceColumn, new Comparator<Peptide>() {
            @Override
            public int compare(Peptide o1, Peptide o2) {
                return o1.getSequence().compareTo(o2.getSequence());
            }
        });

        // Column that shows the tissues the peptide has been seen in.
        TextColumn<Peptide> tissuesColumn = new TextColumn<Peptide>() {
            @Override
            public String getValue(Peptide object) {
                StringBuilder sb = new StringBuilder();
                for(String tissue : object.getTissues()) {
                    sb.append(tissue).append(", ");
                }
                return sb.length() == 0 ? "None" : sb.substring(0, sb.length() - 2);
            }
        };

        tissuesColumn.setSortable(true);
        sorter.setComparator(tissuesColumn, new Comparator<Peptide>() {
            @Override
            public int compare(Peptide o1, Peptide o2) {
                StringBuilder sb1 = new StringBuilder();
                StringBuilder sb2 = new StringBuilder();
                for(String tissue : o1.getTissues()) {
                    sb1.append(tissue);
                }
                for(String tissue : o2.getTissues()) {
                    sb2.append(tissue);
                }
                return sb1.toString().compareTo(sb2.toString());
            }
        });

        // Column that shows the modifications the peptide has.
        TextColumn<Peptide> modsColumn = new TextColumn<Peptide>() {
            @Override
            public String getValue(Peptide object) {
                Set<String> modSet = new HashSet<String>();
                for(ModifiedLocation modLoc : object.getModifiedLocations()) {
                    modSet.add(modLoc.getPosition() + ":" + modLoc.getModification());
                }
                StringBuilder sb = new StringBuilder();
                for(String mod : modSet) {
                    sb.append(mod).append(", ");
                }
                return sb.length() == 0 ? "None" : sb.substring(0, sb.length() - 2);
            }
        };

        modsColumn.setSortable(true);
        sorter.setComparator(modsColumn, new Comparator<Peptide>() {
            @Override
            public int compare(Peptide o1, Peptide o2) {
                if(o1.getModifiedLocations().size() !=
                        o2.getModifiedLocations().size()) {
                    return new Integer(o1.getModifiedLocations().size())
                            .compareTo(o2.getModifiedLocations().size());
                }
                else {
                    StringBuilder sb1 = new StringBuilder();
                    StringBuilder sb2 = new StringBuilder();
                    for(String tissue : o1.getTissues()) {
                        sb1.append(tissue);
                    }
                    for(String tissue : o2.getTissues()) {
                        sb2.append(tissue);
                    }
                    return sb1.toString().compareTo(sb2.toString());
                }
            }
        });




                // Column that shows the tissues the peptide has been seen in.
        TextColumn<Peptide> assaysColumn = new TextColumn<Peptide>() {
            @Override
            public String getValue(Peptide object) {
                StringBuilder sb = new StringBuilder();
                for(String assay : object.getAssays()) {
                    sb.append(assay).append(", ");
                }
                return sb.length() == 0 ? "None" : sb.substring(0, sb.length() - 2);
            }
        };

        assaysColumn.setSortable(true);
        sorter.setComparator(assaysColumn, new Comparator<Peptide>() {
            @Override
            public int compare(Peptide o1, Peptide o2) {
                StringBuilder sb1 = new StringBuilder();
                StringBuilder sb2 = new StringBuilder();
                for(String assay : o1.getAssays()) {
                    sb1.append(assay);
                }
                for(String assay : o2.getAssays()) {
                    sb2.append(assay);
                }
                return sb1.toString().compareTo(sb2.toString());
            }
        });

        Column<Peptide, SafeHtml> assayLinkColumn = new Column<Peptide, SafeHtml>(new SafeHtmlCell()) {
           @Override
           public SafeHtml getValue(Peptide obj)
           {
               String assayId = obj.getAssays().iterator().next();
                   SafeUri href = UriUtils.fromString("http://wwwdev.ebi.ac.uk/pride/archive/assay/" + assayId);
            return cell.anchor(href, assayId);
           }
        };


        Column<Peptide, SafeHtml> multiAssayLinkColumn = new Column<Peptide, SafeHtml>(new SafeHtmlCell()) {
           @Override
           public SafeHtml getValue(Peptide obj)
           {
               StringBuilder sb = new StringBuilder();
               SafeHtmlBuilder builder = new SafeHtmlBuilder();
               for (String assayId : obj.getAssays()) {
                   sb.append("<a href=\"http://wwwdev.ebi.ac.uk/pride/archive/assay/").append(assayId).append("\" target=\"_blank\">").append(assayId).append("</a>&nbsp;");
               }
               builder.appendHtmlConstant(sb.toString());
               return multiCell.anchor(builder.toSafeHtml());
           }
        };



        columns.add(sequenceColumn);
        columns.add(modsColumn);
        columns.add(tissuesColumn);
//        columns.add(assaysColumn);
        columns.add(multiAssayLinkColumn);
        return columns;
    }

    public static List<String> getColumnTitles() {
        List<String> titles = new ArrayList<String>();
        Collections.addAll(titles, "Sequence", "Modifications", "Tissues", "Assays");
        return titles;
    }

    public static List<String> getColumnWidths() {
        List<String> widths = new ArrayList<String>();
        Collections.addAll(widths, "25%", "25%", "25%", "25%");
        return widths;
    }
}
