package uk.ac.ebi.pride.proteomes.web.client.modules.history;

import uk.ac.ebi.pride.proteomes.web.client.exceptions.InconsistentStateException;
import uk.ac.ebi.pride.proteomes.web.client.utils.DefaultHashMap;

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
        private Type key;
        private String value;

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

    Queue<Change> orderedChanges = new LinkedList<Change>();

    public void addGroupChange(Collection<String> groupSelection) {
        orderedChanges.add(new Change(Type.Group,
                                      State.getToken(groupSelection)));
    }

    public void addProteinChange(Collection<String> proteinSelection) {
        orderedChanges.add(new Change(Type.Protein,
                                      State.getToken(proteinSelection)));
    }

    public void addRegionChange(Collection<String> regionSelection) {
        orderedChanges.add(new Change(Type.Region,
                                      State.getToken(regionSelection)));
    }

    public void addPeptideChange(Collection<String> peptideSelection) {
        orderedChanges.add(new Change(Type.Peptide,
                                      State.getToken(peptideSelection)));
    }

    public void addVarianceChange(Collection<String> varianceSelection) {
        orderedChanges.add(new Change(Type.Variance,
                                      State.getToken(varianceSelection)));
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

        if(orderedChanges.size() == 0) {
            changedState = oldState;
        }
        else {
            DefaultHashMap<Type, String> changesToApply = new DefaultHashMap<Type, String>();
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

            changedState = new State(changesToApply.get(Type.Group, oldGroups),
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

        DefaultHashMap<Type, String> changesToApply = new DefaultHashMap<Type, String>();
        for(Change change : orderedChanges) {
            changesToApply.put(change.getKey(), change.getValue());
        }
        sb.append(changesToApply.containsKey(Type.Group) ?
                    "Group: " + changesToApply.get(Type.Group) + "; ":
                    "");
        sb.append(changesToApply.containsKey(Type.Protein) ?
                    "Protein: " + changesToApply.get(Type.Protein) + "; ":
                    "");
        sb.append(changesToApply.containsKey(Type.Region) ?
                    "Region: " + changesToApply.get(Type.Region) + "; ":
                    "");
        sb.append(changesToApply.containsKey(Type.Peptide) ?
                    "Peptide: " + changesToApply.get(Type.Peptide) + "; ":
                    "");
        sb.append(changesToApply.containsKey(Type.Variance) ?
                    "Variance: " + changesToApply.get(Type.Variance) + "; ":
                    "");
        sb.append(changesToApply.containsKey(Type.Modification) ?
                    "Modification: " + changesToApply.get(Type.Modification) + "; ":
                    "");
        sb.append(changesToApply.containsKey(Type.Tissue) ?
                    "Group: " + changesToApply.get(Type.Tissue) + "; ":
                    "");
        return sb.substring(0, sb.length() - 2);
    }
}
