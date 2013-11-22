package uk.ac.ebi.pride.proteomes.web.client.modules.history;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.Region;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.InconsistentStateException;

import java.util.Collection;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 21/10/13
 *         Time: 10:42
 */
final class State {
    private  final String[] selectedGroupIds;
    private  final String[] selectedProteinIds;
    private  final String[] selectedRegionIds;
    private  final String[] selectedPeptideIds;
    private  final String[] selectedVarianceIds;
    private  final String[] selectedModificationIds;
    private  final String[] selectedTissueIds;

    private final String historyToken;

    static final String sepTypes = "&";
    static final String sepMaps = "=";
    static final String sepValues = ",";

    /**
     *
     * @param groupIds String representing several groups,
     *                 separated with sepValues.
     * @param proteinIds String representing several proteins,
     *                 separated with sepValues.
     * @param regionIds String representing several regions,
     *                 separated with sepValues.
     * @param peptideIds String representing several peptides,
     *                 separated with sepValues.
     * @param varianceIds String representing several peptide variances,
     *                 separated with sepValues.
     * @param modificationIds String representing several modifications,
     *                 separated with sepValues.
     * @param tissueIds String representing several tissues,
     *                 separated with sepValues.
     * @throws InconsistentStateException if the state that's being created
     * doesn't follow the data model hierarchy this exception is thrown
     * instead of creating the new state. Note that there isn't enough
     * information to check whether the state is semantically correct,
     * so another check using all the data pointed in here must be made.
     */
    State(String groupIds, String proteinIds, String regionIds,
          String peptideIds, String varianceIds, String modificationIds,
          String tissueIds)
             throws InconsistentStateException {

        if(!isValid(groupIds, proteinIds, regionIds, peptideIds, varianceIds,
                    modificationIds, tissueIds)) {
            throw new InconsistentStateException();
        }

        StringBuilder sBuild = new StringBuilder();

        if(!groupIds.isEmpty()) {
            sBuild.append("group" + sepMaps).append(groupIds).append(sepTypes);
        }
        if(!proteinIds.isEmpty()) {
            sBuild.append("protein" + sepMaps).append(proteinIds).append(sepTypes);
        }
        if(!regionIds.isEmpty()) {
            sBuild.append("region" + sepMaps).append(regionIds).append(sepTypes);
        }
        if(!peptideIds.isEmpty()) {
            sBuild.append("peptide" + sepMaps).append(peptideIds).append(sepTypes);
        }
        if(!varianceIds.isEmpty()) {
            sBuild.append("variance" + sepMaps).append(varianceIds).append(sepTypes);
        }
        if(!modificationIds.isEmpty()) {
            sBuild.append("modification" + sepMaps).append(modificationIds).append(sepTypes);
        }
        if(!tissueIds.isEmpty()) {
            sBuild.append("tissue" + sepMaps).append(tissueIds).append(sepTypes);
        }

        // Should we clean the history token before saving it? (using the
        // split Ids)
        if(sBuild.length() > 0) {
            historyToken = sBuild.substring(0, sBuild.length() - 1);
        }
        else {
            historyToken = sBuild.toString();
        }

        // We split the strings into arrays or use an empty array if they're
        // empty.

        selectedGroupIds = groupIds.isEmpty() ? new String[0] : groupIds.split(sepValues);
        selectedProteinIds = proteinIds.isEmpty() ? new String[0] : proteinIds.split(sepValues);
        selectedRegionIds = regionIds.isEmpty() ? new String[0] : regionIds.split(sepValues);
        selectedPeptideIds = peptideIds.isEmpty() ? new String[0] : peptideIds.split(sepValues);
        selectedVarianceIds = varianceIds.isEmpty() ? new String[0] : varianceIds.split(sepValues);
        selectedModificationIds = modificationIds.isEmpty() ? new String[0] : modificationIds.split(sepValues);
        selectedTissueIds = tissueIds.isEmpty() ? new String[0] : tissueIds.split(sepValues);
    }

    /**
     *
     * @param value a url in the app that is to be tokenized to retrieve state
     * @return the application state that the url represents
     * @throws InconsistentStateException the state wasn't created because it
     * represents an invalid state
     */
    public static State tokenize(String value) throws
            InconsistentStateException {
        String groupIds = "", proteinIds = "",  peptideIds = "",
                varianceIds = "", regionIds = "", modificationIds = "",
                tissueIds = "";

        String[] tokens, dict;

        tokens = value.split(sepTypes);

        for(String token : tokens) {
            dict = token.split(sepMaps);

            if(dict.length < 2) {
                continue;
            }

            // TODO use switch statement, leads to less boilerplate
            // for gwt 2.6, it supports java 7 with string switches
            if(dict[0].compareToIgnoreCase("group") == 0) {
                groupIds = dict[1];
            }
            else if(dict[0].compareToIgnoreCase("protein") == 0) {
                proteinIds = dict[1];
            }
            else if(dict[0].compareToIgnoreCase("region") == 0) {
                regionIds = dict[1];
            }
            else if(dict[0].compareToIgnoreCase("peptide") == 0) {
                peptideIds = dict[1];
            }
            else if(dict[0].compareToIgnoreCase("variance") == 0) {
                varianceIds = dict[1];
            }
            else if(dict[0].compareToIgnoreCase("modification") == 0) {
                modificationIds = dict[1];
            }
            else if(dict[0].compareToIgnoreCase("tissue") == 0) {
                tissueIds = dict[1];
            }
        }

        return new State(groupIds, proteinIds, regionIds, peptideIds,
                varianceIds, modificationIds, tissueIds);
    }

    public String[] getSelectedGroups() {
        return selectedGroupIds;
    }

    public String[] getSelectedProteins() {
        return selectedProteinIds;
    }

    public String[] getSelectedRegions() {
        return selectedRegionIds;
    }

    public String[] getSelectedPeptides() {
        return selectedPeptideIds;
    }

    public String[] getSelectedVariances() {
        return selectedVarianceIds;
    }

    public String[] getSelectedModifications() {
        return selectedModificationIds;
    }

    public String[] getSelectedTissues() {
        return selectedTissueIds;
    }

    public String getHistoryToken() {
        return historyToken;
    }

    /**
     * Used to produce a URL token from a collection of ids
     * @param ids the collection that is needed to convert to a string
     * @return section of URL that represents the collection
     */
    static String getToken(Collection<String> ids) {
        StringBuilder builder = new StringBuilder();
        for(String id : ids) {
            builder.append(id);
            builder.append(sepValues);
        }
        return builder.substring(0, builder.length() == 0 ? builder.length():
                                                            builder.length() - 1);
    }

    /**
     * Used to produce a URL token from a collection of ids
     * @param ids the collection that is needed to convert to a string
     * @return section of URL that represents the collection
     */
    static String getToken(String[] ids) {
        StringBuilder builder = new StringBuilder();
        for(String id : ids) {
            builder.append(id);
            builder.append(sepValues);
        }
        return builder.substring(0, builder.length() == 0 ? builder.length():
                                                            builder.length() - 1);
    }

    boolean isValid(String groups,
                    String proteins,
                    String regions,
                    String peptides,
                    String variances,
                    String modifications,
                    String tissues) {
        boolean isValid = true;

        if(proteins.isEmpty() &&
           (!peptides.isEmpty() || !variances.isEmpty() || !regions.isEmpty())) {
            isValid = false;
        }
        else if((groups.isEmpty() && proteins.isEmpty()) &&
                (!modifications.isEmpty() || !tissues.isEmpty())) {
            isValid = false;
        }
        else if(peptides.isEmpty() && !variances.isEmpty()) {
            isValid = false;
        }
        else {
            for(String regionId : regions.split(sepValues)) {
                if(!regionId.isEmpty()) {
                    try {
                        Region.tokenize(regionId);
                    }
                    catch(Exception e) {
                        isValid = false;
                    }
                }
            }
        }

        return isValid;
    }
}