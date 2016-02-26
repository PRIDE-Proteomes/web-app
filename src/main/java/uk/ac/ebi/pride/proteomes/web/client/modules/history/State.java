package uk.ac.ebi.pride.proteomes.web.client.modules.history;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.Region;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.InconsistentStateException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 21/10/13
 *         Time: 10:42
 */
final class State {
    private  final List<String> selectedGroupIds;
    private  final List<String> selectedProteinIds;
    private  final List<String> selectedRegionIds;
    private  final List<String> selectedPeptideIds;
    private  final List<String> selectedPeptiformIDs;
    private  final List<String> selectedModificationIds;
    private  final List<String> selectedModificationWithPosIds;
    private  final List<String> selectedTissueIds;

    private final String historyToken;

    static final String sepTypes = "&";
    static final String sepMaps = "=";
    static final String sepValues = ",";
    static final String sepFields = ";";

    static class EmptyState {
        private static final State INSTANCE = new State();
    }

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
     * @param peptiformIds String representing several peptiforms,
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
    private State(String groupIds, String proteinIds, String regionIds,
          String peptideIds, String peptiformIds, String modificationIds, String modificationWithPosIds,
          String tissueIds)
             throws InconsistentStateException {

        if(!isValid(groupIds, proteinIds, regionIds, peptideIds, peptiformIds,
                    modificationIds, modificationWithPosIds, tissueIds)) {
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
        if(!peptiformIds.isEmpty()) {
            sBuild.append("peptiform" + sepMaps).append(peptiformIds).append(sepTypes);
        }
        if(!modificationIds.isEmpty()) {
            sBuild.append("modification" + sepMaps).append(modificationIds).append(sepTypes);
        }
        if(!modificationWithPosIds.isEmpty()) {
            sBuild.append("modificationWithPos" + sepMaps).append(modificationWithPosIds).append(sepTypes);
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

        selectedGroupIds = groupIds.isEmpty() ? Collections.<String>emptyList() : Arrays.asList(groupIds.split(sepValues));
        selectedProteinIds = proteinIds.isEmpty() ? Collections.<String>emptyList() : Arrays.asList(proteinIds.split(sepValues));
        selectedRegionIds = regionIds.isEmpty() ? Collections.<String>emptyList() : Arrays.asList(regionIds.split(sepValues));
        selectedPeptideIds = peptideIds.isEmpty() ? Collections.<String>emptyList() : Arrays.asList(peptideIds.split(sepValues));
        selectedPeptiformIDs = peptiformIds.isEmpty() ? Collections.<String>emptyList() : Arrays.asList(peptiformIds.split(sepValues));
        selectedModificationIds = modificationIds.isEmpty() ? Collections.<String>emptyList() : Arrays.asList(modificationIds.split(sepValues));
        selectedModificationWithPosIds =  modificationWithPosIds.isEmpty() ? Collections.<String>emptyList() : Arrays.asList(modificationWithPosIds.split(sepValues));
        selectedTissueIds = tissueIds.isEmpty() ? Collections.<String>emptyList() : Arrays.asList(tissueIds.split(sepValues));
    }

    private State() {
        selectedGroupIds = Collections.emptyList();
        selectedProteinIds = Collections.emptyList();
        selectedRegionIds = Collections.emptyList();
        selectedPeptideIds = Collections.emptyList();
        selectedPeptiformIDs = Collections.emptyList();
        selectedModificationIds = Collections.emptyList();
        selectedModificationWithPosIds = Collections.emptyList();
        selectedTissueIds = Collections.emptyList();
        historyToken = "";
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
                peptiformIds = "", regionIds = "", modificationIds = "",
                modificationWithPosIds = "",
                tissueIds = "";

        String[] tokens, dict;

        tokens = value.split(sepTypes);

        for(String token : tokens) {
            dict = token.split(sepMaps);

            if(dict.length < 2) {
                continue;
            }

            String type = dict[0].toLowerCase();

            switch (type) {
                case "group":
                    groupIds = dict[1];
                    break;
                case "protein":
                    proteinIds = dict[1];
                    break;
                case "region":
                    regionIds = dict[1];
                    break;
                case "peptide":
                    peptideIds = dict[1];
                    break;
                case "peptiform":
                    peptiformIds = dict[1];
                    break;
                case "modification":
                    modificationIds = dict[1];
                    break;
                case "modificationWithPost":
                    modificationWithPosIds = dict[1];
                    break;
                case "tissue":
                    tissueIds = dict[1];
                    break;
            }
        }

        return new State(groupIds, proteinIds, regionIds, peptideIds,
                peptiformIds, modificationIds, modificationWithPosIds, tissueIds);
    }

    public List<String> getSelectedGroups() {
        return selectedGroupIds;
    }

    public List<String> getSelectedProteins() {
        return selectedProteinIds;
    }

    public List<String> getSelectedRegions() {
        return selectedRegionIds;
    }

    public List<String> getSelectedPeptides() {
        return selectedPeptideIds;
    }

    public List<String> getSelectedPeptiforms() {
        return selectedPeptiformIDs;
    }

    public List<String> getSelectedModifications() {
        return selectedModificationIds;
    }

    public List<String> getSelectedModificationWithPositions() {
        return selectedModificationWithPosIds;
    }

    public List<String> getSelectedTissues() {
        return selectedTissueIds;
    }

    public String getHistoryToken() {
        return historyToken;
    }

    /**
     * Used to produce a URL token from a collection of identifiers.
     * @param ids the collection that is needed to represent in a URL
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

    boolean isValid(String groups,
                    String proteins,
                    String regions,
                    String peptides,
                    String variances,
                    String modifications,
                    String modificationsWithPos,
                    String tissues) {
        boolean isValid = true;

        if(proteins.isEmpty() &&
           (!peptides.isEmpty() || !variances.isEmpty() || !regions.isEmpty())) {
            isValid = false;
        }
        else if((groups.isEmpty() && proteins.isEmpty()) &&
                (!modifications.isEmpty() || !modificationsWithPos.isEmpty() || !tissues.isEmpty())) {
            isValid = false;
        }
        else if(peptides.isEmpty() && !variances.isEmpty()) {
            isValid = false;
        }
        else {
            // we do more complex checks here.
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
            for(String varianceId : variances.split(sepValues)) {
                if(!varianceId.isEmpty()) {
                    String varSeq = varianceId.split("[|]")[0].substring(1);
                    String[] pepIds = peptides.split(sepValues);
                    boolean isContained = false;
                    for(String pepId : pepIds) {
                        if(pepId.contains(";")) {
                            if(varSeq.equals(pepId.split(sepFields)[0])) {
                                isContained = true;
                                break;
                            }
                        }
                        else if(varSeq.equals(pepId)) {
                            isContained = true;
                            break;
                        }
                    }
                    isValid = isContained;
                }
            }
        }

        return isValid;
    }

    /**
     * Using some string that may create an invalid state,
     * we create a valid one to our best effort without using the data,
     * otherwise an exception is thrown.
     * @param groupIds the url-encoded identifiers of the groups
     * @param proteinIds the url-encoded identifiers of the proteins
     * @param regionIds the url-encoded identifiers of the regions
     * @param peptideIds the url-encoded identifiers of the peptides
     * @param peptiformIds the url-encoded identifiers of the peptiforms
     * @param modificationIds the url-encoded identifiers of the modifications
     * @param modificationWithPosIds the url-encoded identifiers of the modifications with positions
     * @param tissueIds the url-encoded identifiers of the tissues
     * @return a new state with a state with removed properties to make it valid
     * @throws InconsistentStateException
     */
    static State simplifyState(String groupIds, String proteinIds,
                               String regionIds, String peptideIds,
                               String peptiformIds, String modificationIds,
                               String modificationWithPosIds,
                               String tissueIds)
                                       throws InconsistentStateException {
        String newRegionIds = "", newPeptideIds = "", newPeptiformIds = "",
                newModIds = "",  newModWithPosIds = "",
                newTissueIds = "";

        if(!proteinIds.isEmpty()) {
            newRegionIds = regionIds;
            newPeptideIds = peptideIds;
        }

        if(!newPeptideIds.isEmpty()) {
            newPeptiformIds = peptiformIds;
        }

        if(!proteinIds.isEmpty() || !groupIds.isEmpty()) {
            newModIds = modificationIds;
            newModWithPosIds = modificationWithPosIds;
            newTissueIds = tissueIds;
        }

        return new State(groupIds, proteinIds, newRegionIds, newPeptideIds,
                         newPeptiformIds, newModIds, newModWithPosIds, newTissueIds);
    }

    public static State getInvalidState() {
        return EmptyState.INSTANCE;
    }
}