package uk.ac.ebi.pride.proteomes.web.client.modules.history;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.InconsistentStateException;
import uk.ac.ebi.pride.proteomes.web.client.utils.DefaultHashMap;

import java.util.*;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 18/10/13
 *         Time: 12:14
 */
public class StateChanger {
    private final class Change {
        private String key;
        private String value;

        public Change(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    Queue<Change> orderedChanges = new LinkedList<Change>();

    public void addGroupChange(Collection<String> selectedGroup) {
        orderedChanges.add(new Change("group", State.getToken(selectedGroup)));
    }

    /**
     * A new state is created from old one using the modified fields
     * @param oldState the old state used to create the new one
     * @return a new state with the new properties applied
     */
    public State change(State oldState) throws InconsistentStateException {
        State changedState;

        if(orderedChanges.size() == 0) {
            changedState = oldState;
        }
        else {
            DefaultHashMap<String, String> changesToApply = new DefaultHashMap<String, String>();
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
            String oldModifications = State.getToken(oldState.getSelectedRegions());
            String oldTissues = State.getToken(oldState.getSelectedRegions());

            changedState = new State(changesToApply.get("group", oldGroups),
                                     changesToApply.get("protein", oldProteins),
                                     changesToApply.get("peptide", oldPeptides),
                                     changesToApply.get("variance", oldVariances),
                                     changesToApply.get("region", oldRegions),
                                     changesToApply.get("modification", oldModifications),
                                     changesToApply.get("tissue", oldTissues));
        }
        return changedState;
    }
}
