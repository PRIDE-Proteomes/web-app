package uk.ac.ebi.pride.proteomes.web.client.modules.history;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.Region;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Group;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.InconsistentStateException;
import uk.ac.ebi.pride.proteomes.web.client.utils.DefaultHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This class tries to decouple the modules using the event bus from the
 * whole state of the application. Using the state changer they can only use
 * the methods of the data in which they are interested whenever they want to
 * change the state of the application.
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 18/10/13
 *         Time: 12:14
 */
public class StateChanger {
    private final class Change {
        private final Type key;
        private final String value;

        public Change(Type key, String value) {
            this.key = key;
            this.value = value;
        }

        public Type getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    private enum Type {
        Group, Protein, Region, Peptide, Variance, Modification, Tissue
    }

    private Queue<Change> orderedChanges = new LinkedList<>();

    public void addGroupChange(Collection<Group> groupSelection) {
        Collection<String> groupIds = new ArrayList<>();
        for(Group group : groupSelection) {
            groupIds.add(group.getId());
        }
        orderedChanges.add(new Change(Type.Group,
                                      State.getToken(groupIds)));
    }

    public void addProteinChange(Collection<Protein> proteinSelection) {
        Collection<String> proteinIds = new ArrayList<>();
        for(Protein prot : proteinSelection) {
            proteinIds.add(prot.getAccession());
        }
        orderedChanges.add(new Change(Type.Protein,
                                      State.getToken(proteinIds)));
    }

    public void addRegionChange(Collection<Region> regionSelection) {
        Collection<String> regionIds = new ArrayList<>();
        for(Region reg : regionSelection) {
            regionIds.add(reg.toString());
        }
        orderedChanges.add(new Change(Type.Region,
                                      State.getToken(regionIds)));
    }

    public void addPeptideChange(Collection<PeptideMatch> peptideSelection) {
        Collection<String> peptideIds = new ArrayList<>();
        for(PeptideMatch match : peptideSelection) {
            peptideIds.add(match.getSequence() + State.sepFields + match.getPosition());
        }
        orderedChanges.add(new Change(Type.Peptide,
                                      State.getToken(peptideIds)));
    }

    public void addVarianceChange(Collection<Peptide> varianceSelection) {
        Collection<String> varianceIDs = new ArrayList<>();
        for(Peptide variance : varianceSelection) {
            varianceIDs.add(variance.getId());
        }
        orderedChanges.add(new Change(Type.Variance,
                                      State.getToken(varianceIDs)));
    }

    public void addModificationChange(Collection<String> modificationSelection) {
        orderedChanges.add(new Change(Type.Modification,
                State.getToken(modificationSelection)));
    }

    public void addTissueChange(Collection<String> tissueSelection) {
        orderedChanges.add(new Change(Type.Tissue,
                State.getToken(tissueSelection)));
    }

    /**
     * A new state is created from old one using the modified fields
     * @param oldState the old state used to create the new one
     * @return a new state with the new properties applied
     */
    State change(State oldState) throws InconsistentStateException {
        State changedState;

        if(orderedChanges.isEmpty()) {
            changedState = oldState;
        }
        else {
            DefaultHashMap<Type, String> changesToApply = new DefaultHashMap<>();
            for(Change change : orderedChanges) {
                changesToApply.put(change.getKey(), change.getValue());
            }

            // We create the strings from the old state and use them as
            // fallback values when there is no change for them.
            String oldGroups = State.getToken(oldState.getSelectedGroups());
            String oldProteins = State.getToken(oldState.getSelectedProteins());
            String oldPeptides = State.getToken(oldState.getSelectedPeptides());
            String oldVariances = State.getToken(oldState.getSelectedVariances());
            String oldRegions = State.getToken(oldState.getSelectedRegions());
            String oldModifications = State.getToken(oldState.getSelectedModifications());
            String oldTissues = State.getToken(oldState.getSelectedTissues());

            changedState = State.simplifyState(
                    changesToApply.get(Type.Group, oldGroups),
                    changesToApply.get(Type.Protein, oldProteins),
                    changesToApply.get(Type.Region, oldRegions),
                    changesToApply.get(Type.Peptide, oldPeptides),
                    changesToApply.get(Type.Variance, oldVariances),
                    changesToApply.get(Type.Modification, oldModifications),
                    changesToApply.get(Type.Tissue, oldTissues));
        }
        return changedState;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        DefaultHashMap<Type, String> changesToApply = new DefaultHashMap<>();
        for(Change change : orderedChanges) {
            changesToApply.put(change.getKey(), change.getValue());
        }
        sb.append(changesToApply.containsKey(Type.Group) ?
                    "group=" + changesToApply.get(Type.Group) + State.sepTypes:
                    "");
        sb.append(changesToApply.containsKey(Type.Protein) ?
                    "protein=" + changesToApply.get(Type.Protein) + State.sepTypes:
                    "");
        sb.append(changesToApply.containsKey(Type.Region) ?
                    "region=" + changesToApply.get(Type.Region) + State.sepTypes:
                    "");
        sb.append(changesToApply.containsKey(Type.Peptide) ?
                    "peptide=" + changesToApply.get(Type.Peptide) + State.sepTypes:
                    "");
        sb.append(changesToApply.containsKey(Type.Variance) ?
                    "variance=" + changesToApply.get(Type.Variance) + State.sepTypes:
                    "");
        sb.append(changesToApply.containsKey(Type.Modification) ?
                    "modification=" + changesToApply.get(Type.Modification) + State.sepTypes:
                    "");
        sb.append(changesToApply.containsKey(Type.Tissue) ?
                    "tissue=" + changesToApply.get(Type.Tissue) + State.sepTypes:
                    "");
        return sb.substring(0, sb.length() > 0 ? sb.length() - 1 : 0);
    }
}
