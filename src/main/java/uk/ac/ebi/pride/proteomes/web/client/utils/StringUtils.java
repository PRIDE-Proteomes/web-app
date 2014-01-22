package uk.ac.ebi.pride.proteomes.web.client.utils;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Group;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 20/11/13
 *         Time: 10:27
 */
public class StringUtils {
    /**
     * It should be glaringly obvious that this function doesn't support
     * localization out of the box.
     * @param baseName
     * @param count
     * @return
     */
    public static String getCount(String baseName, int count) {
        StringBuilder sb = new StringBuilder("(");
        sb.append(count);
        sb.append(" ").append(baseName);
        if(count != 1){
            sb.append("s");
        }
        sb.append(")");
        return sb.toString();
    }

    // As the tables depend on this piece of code, this should be updated
    // accordingly if there's a class which doesn't provide a sane toString
    // method.
    public static String getName(Object o) {
        if(o instanceof Group) {
            return ((Group) o).getId();
        }
        else if(o instanceof Protein) {
            return ((Protein) o).getAccession();
        }
        else if(o instanceof PeptideMatch) {
            return ((PeptideMatch) o).getSequence() + ":" + ((PeptideMatch) o).getPosition().toString();
        }
        else if(o instanceof Peptide) {
            return ((Peptide) o).getId().substring(1, ((Peptide) o).getId().length() - 1);
        }
        else {
            return o.toString();
        }
    }

    public static String getShortName(Class cl) {
        return cl.getName().substring(cl.getName().lastIndexOf(".") + 1);
    }
}
