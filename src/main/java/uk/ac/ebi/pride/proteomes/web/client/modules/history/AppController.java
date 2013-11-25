package uk.ac.ebi.pride.proteomes.web.client.modules.history;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.History;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Region;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.*;
import uk.ac.ebi.pride.proteomes.web.client.events.state.EmptyViewEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.GroupRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.PeptideRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.ProteinRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.InvalidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.StateChangingActionEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.ValidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.*;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.IllegalRegionValueException;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.InconsistentStateException;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.DataServer;
import uk.ac.ebi.pride.proteomes.web.client.utils.RegionUtils;

import java.util.*;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 22/10/13
 *         Time: 10:38
 */
public class AppController implements
                           StateChangingActionEvent.StateChangingActionHandler,
                           ValueChangeHandler<String>,
                           HasHandlers,
                           DataServer.DataClient
{
    private final EventBus eventBus;
    private final DataServer server;
    private State appState;
    private Queue<State> stateQueue;
    private boolean queueBeingProcessed = false;

    public AppController(EventBus eventBus, DataServer server) {
        this.eventBus = eventBus;
        this.server = server;

        stateQueue = new LinkedList<State>();
        try {
            appState = State.tokenize("");
        } catch (InconsistentStateException e) {/**/}

        eventBus.addHandler(StateChangingActionEvent.getType(), this);
        History.addValueChangeHandler(this);
    }

    @Override
    public void onStateChangingActionEvent(StateChangingActionEvent event) {
        State freshState;

        try {
            freshState = event.getChanger().change(appState);
        }
        catch(InconsistentStateException e) {
            // we tried to create an inconsistent state, that's bad,
            // we should act upon it. (show a popup with an error?)
            InvalidStateEvent.fire(this, "The address cannot be " +
                    "displayed. Please check that is is correct and " +
                    "change it, or go back. If you didn't type the " +
                    "address contact the PRIDE team about the error and " +
                    "explained them what you were doing before this " +
                    "message");
            return;
        }
        requestData(freshState);
    }

    /**
     * This function gets called whenever the browser's url gets changed
     * @param event the event that carries the new URL
     */
    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        if(event.getValue().isEmpty()) {
            // We're in the empty landing page!
            try {
                appState = State.tokenize(event.getValue());
            } catch (InconsistentStateException e){
                InvalidStateEvent.fire(this, "Application Error, " +
                                             "please contact the PRIDE team.");
                return;
            }

            EmptyViewEvent.fire(this, "Please, select a protein or group of " +
                                      "proteins to show its data.");
        }
        else {
            State freshState;
            try {
                freshState = State.tokenize(event.getValue());
            }
            catch(InconsistentStateException e) {
                // we tried to create an inconsistent state, that's bad,
                // we should act upon it. (show a popup with an error?)
                InvalidStateEvent.fire(this, "The address cannot be " +
                        "displayed. Please check that is is correct and " +
                        "change it, or go back. If you didn't type the " +
                        "address contact the PRIDE team about the error and " +
                        "explained them what you were doing before this " +
                        "message");
                return;
            }
            requestData(freshState);
        }
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEventFromSource(event, this);
    }

    @Override
    public void onGroupsRetrieved(Collection<Group> groups) {
        processStateQueue();
    }

    @Override
    public void onProteinsRetrieved(Collection<Protein> proteins) {
        processStateQueue();
    }

    @Override
    public void onPeptideVarianceListsRetrieved(Collection<PeptideList>  peptides) {
        processStateQueue();
    }

    @Override
    public void onRetrievalError(String message) {
        ErrorOnUpdateEvent.fire(this, message);
    }

    private void requestData(State state) {
        // Check what information is already cached or already requested to
        // notify the application that some data may take some time to be
        // retrieved, we don't want the users to think that the web app is
        // unresponsive.

        boolean areGroupsCached = true;
        boolean areProteinsCached = true;
        boolean arePeptidesCached = true;

        if(!State.getToken(state.getSelectedGroups()).equals(
                State.getToken(appState.getSelectedGroups()))) {
            for(String id : state.getSelectedGroups()) {
                if(!server.isGroupCached(id)) {
                    areGroupsCached = false;
                    break;
                }
            }
        }

        if(!State.getToken(state.getSelectedProteins()).equals(
                State.getToken(appState.getSelectedProteins()))) {
            for(String accession : state.getSelectedProteins()) {
                if(!server.isProteinCached(accession)) {
                    areProteinsCached = false;
                    break;
                }
            }
        }

        if(!State.getToken(state.getSelectedPeptides()).equals(
                State.getToken(appState.getSelectedPeptides()))) {
            for(String sequence : state.getSelectedPeptides()) {
                if(!server.isPeptideCached(sequence)) {
                    arePeptidesCached = false;
                    break;
                }
            }
        }

        //update the queue with the new state that has to be processed
        stateQueue.add(state);

        if(!areGroupsCached) {
            GroupRequestEvent.fire(this);
        }
        if(!areProteinsCached) {
            ProteinRequestEvent.fire(this);
        }
        if(!arePeptidesCached) {
            PeptideRequestEvent.fire(this);
        }

        // If everything is cached we go straight to update the views
        if(areGroupsCached && areProteinsCached && arePeptidesCached) {
            processStateQueue();
        }
        else {
            // It's time to retrieve data and wait for the callback
            if(!areGroupsCached) {
                server.requestGroups(state.getSelectedGroups());
            }
            if(!areProteinsCached) {
                server.requestProteins(state.getSelectedProteins());
            }
            if(!arePeptidesCached) {
                server.requestPeptideVariances(state.getSelectedPeptides());
            }
        }
    }

    private void processStateQueue() {
        // Do a poor man's atomic region, we have to ensure that we reset the
        // variable queueBeingProcessed to false whenever we exit the function
        if(queueBeingProcessed) {
            return;
        }
        else {
            queueBeingProcessed = true;
        }

        // Check if the queue should be processed
        if(stateQueue.isEmpty() || !isDataReady(stateQueue.element())) {
            queueBeingProcessed = false;
            return;
        }
        try {
            if(!isStateDataValid(stateQueue.peek())) {
                throw new InconsistentStateException();
            }
            goTo(stateQueue.remove());
        }
        catch(InconsistentStateException e) {
            stateQueue.remove();
            InvalidStateEvent.fire(this, "The address cannot be " +
                    "displayed. Please check that is is correct and " +
                    "change it, or go back. If you didn't type the " +
                    "address contact the PRIDE team about the error and " +
                    "explained them what you were doing before this " +
                    "message");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            queueBeingProcessed = false;
        }

        //the first state in the queue right now might be ready, who knows?
        processStateQueue();
    }

    /**
     * This function checks whether the data in a state is cached in the
     * server or not yet
     * @param state The state which data needs to be checked.
     * @return Whether the data in the state is all cached already in the
     * provider
     */
    private boolean isDataReady(State state) {
        if(state == null) {
            return false;
        }
        for(String id : state.getSelectedGroups()) {
            if(!server.isGroupCached(id)) {
                return false;
            }
        }
        for(String accession : state.getSelectedProteins()) {
            if(!server.isProteinCached(accession)) {
                return false;
            }
        }
        for(String sequence : state.getSelectedPeptides()) {
            if(!server.isPeptideCached(sequence)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This function checks whether an application state contains consistent
     * data, i.e. the state can be displayed without inconsistencies.
     * @param state the state that needs to be checked.
     * @return Whether the data in the state is consistent or not.
     */
    private boolean isStateDataValid(State state) {
        if(state == null) {
            return false;
        }

        boolean isCorrect = true;

        // for each peptide in the state check if they belong in all the
        // selected groups and proteins.
        for(String sequence : state.getSelectedPeptides()) {
            for(String id : state.getSelectedGroups()) {
                if(!server.getGroup(id).getMemberProteins().contains
                        (sequence)) {
                    isCorrect = false;
                    break;
                }
            }

            if(!isCorrect) {
                break;
            }
            for(String accession : state.getSelectedProteins()) {
                //check if the peptide in the state are actually in the
                // protein, as well as inside the regions selected.
                boolean isContained = false;
                for(PeptideMatch match : server.getProtein(accession).getPeptides()) {
                    if(sequence.equals(match.getSequence())) {
                        // Check if the peptide match is inside any region,
                        // the lack of region means it's contained,
                        // because all the protein is relevant.
                        if(state.getSelectedRegions().length == 0) {
                            isContained = true;
                        }
                        else {
                            for(String regionId : state.getSelectedRegions()) {
                                try {
                                    Region region = Region.tokenize(regionId);
                                    if(region.getStart() <= match.getPosition() &&
                                       region.getEnd() >= match.getPosition() + match
                                               .getSequence().length()) {
                                        isContained = true;
                                        break;
                                    }
                                } catch (IllegalRegionValueException e) {
                                    isCorrect = false;
                                }
                            }
                        }
                        if(isContained) {
                            break;
                        }
                    }
                }
                if(!isContained || !isCorrect) {
                    isCorrect = false;
                    break;
                }
            }
            if(!isCorrect) {
                break;
            }
        }

        return isCorrect;
    }

    /**
     * This method signals the rest of the application the data that has been
     * updated to reflect the new state
     * @param newState the state that the application has to show.
     */
    private void goTo(State newState) {
        // we assume the code here when it gets interrupted it cannot be
        // executed again, otherwise we might run into data inconsistencies
        // (the caller should guarantee this, like processStateQueue() does)

        if(newState.getSelectedGroups().length > 0) {
            ValidStateEvent.fire(this, ValidStateEvent.ViewType.Group);
        }
        else {
            ValidStateEvent.fire(this, ValidStateEvent.ViewType.Protein);
        }

        History.newItem(newState.getHistoryToken(), false);

        if(!Arrays.equals(newState.getSelectedGroups(),
                          appState.getSelectedGroups())) {
            GroupUpdateEvent.fire(this, server.getGroups(newState.getSelectedGroups()));
        }
        if(!Arrays.equals(newState.getSelectedProteins(),
                          appState.getSelectedProteins())) {
            ProteinUpdateEvent.fire(this, server.getProteins(newState.getSelectedProteins()));
        }
        if(!Arrays.equals(newState.getSelectedRegions(),
                          appState.getSelectedRegions())) {
            try {
                RegionUpdateEvent.fire(this, RegionUtils.tokenize(newState.getSelectedRegions()));
            } catch (IllegalRegionValueException e) {
                // this should never happen (we checked before!)
                ErrorOnUpdateEvent.fire(this, "Application Error, please " +
                                              "contact the PRIDE team.");
            }
        }
        if(!Arrays.equals(newState.getSelectedPeptides(),
                          appState.getSelectedPeptides())) {
            // This should group the peptides if there are several proteins
            // selected. Since the group view doesn't allow for this at the
            // moment there's no need to implement it at the moment.

            PeptideUpdateEvent.fire(this, server.getPeptideVarianceLists(newState.getSelectedPeptides()));
        }
        if(!Arrays.equals(newState.getSelectedVariances(),
                          appState.getSelectedVariances())) {
            VarianceUpdateEvent.fire(this, newState.getSelectedVariances());
        }
        if(!Arrays.equals(newState.getSelectedModifications(),
                          appState.getSelectedModifications())) {
            ModificationUpdateEvent.fire(this, newState.getSelectedModifications());
        }
        if(!Arrays.equals(newState.getSelectedTissues(),
                          appState.getSelectedTissues())) {
            TissueUpdateEvent.fire(this, newState.getSelectedTissues());
        }

        appState = newState;
    }
}