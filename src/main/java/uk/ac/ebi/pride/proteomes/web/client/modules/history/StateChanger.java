package uk.ac.ebi.pride.proteomes.web.client.modules.history;

import uk.ac.ebi.pride.proteomes.web.client.exceptions.InconsistentStateException;

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
            Map<String, String> tempState = new HashMap<String, String>();
            for(Change change : orderedChanges) {
                tempState.put(change.getKey(), change.getValue());
            }
            changedState = new State(tempState.get("group"),
                                     tempState.get("protein"),
                                     tempState.get("peptide"),
                                     tempState.get("variance"),
                                     tempState.get("region"),
                                     tempState.get("modification"),
                                     tempState.get("tissue"));
        }

        return changedState;
    }
}
