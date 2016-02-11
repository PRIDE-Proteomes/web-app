package uk.ac.ebi.pride.proteomes.web.client.utils;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.ModificationWithPosition;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.IllegalModificationPositionException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 16:32
 */
public class ModificationUtils {
    public static List<ModificationWithPosition> tokenize(List<String> regionIds) throws
                                                    IllegalModificationPositionException,
                                                    NumberFormatException,
                                                    IndexOutOfBoundsException {
        List<ModificationWithPosition> modList = new ArrayList<>();

        for(String regionId : regionIds) {
            modList.add(ModificationWithPosition.tokenize(regionId));
        }
        return modList;
    }
}
