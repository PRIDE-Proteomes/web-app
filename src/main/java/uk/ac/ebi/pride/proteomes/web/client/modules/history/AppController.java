package uk.ac.ebi.pride.proteomes.web.client.modules.history;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.History;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Region;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Group;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Protein;
import uk.ac.ebi.pride.proteomes.web.client.events.state.EmptyViewEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.GroupRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.PeptideRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.ProteinRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.InvalidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.StateChangingActionEvent;
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

    public AppController(EventBus eventBus, DataServer server) {
        this.eventBus = eventBus;
        this.server = server;

        stateQueue = new LinkedList<State>();

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
            return;
        }

        stateQueue.add(freshState);
        requestData(freshState);
    }

    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        if(event.getValue().isEmpty()) {
            // We're in the empty landing page!
            try {
                appState = State.tokenize(event.getValue());
            } catch (Exception e){
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
    public void onPeptidesRetrieved(Collection<Peptide> peptides) {
        processStateQueue();
    }

    @Override
    public void onRetrievalError(String message) {
        ErrorOnUpdateEvent.fire(this, "There was an error when contacting " +
                                      "the server\n" + message);
    }

    private void requestData(State state) {
        // Check what information is already cached or already requested to
        // notify the application that some data may take some time to be
        // retrieved, we don't want the users to think that the web app is
        // unresponsive.

        boolean areGroupsNotCached = true;
        boolean areProteinsNotCached = true;
        boolean arePeptidesNotCached = true;

        if(!State.getToken(state.getSelectedGroups()).equals(
                State.getToken(appState.getSelectedGroups()))) {
            for(String id : state.getSelectedGroups()) {
                if(!server.isGroupCached(id)) {
                    areGroupsNotCached = false;
                    break;
                }
            }
        }

        if(!State.getToken(state.getSelectedProteins()).equals(
                State.getToken(appState.getSelectedProteins()))) {
            for(String accession : state.getSelectedProteins()) {
                if(!server.isProteinCached(accession)) {
                    areProteinsNotCached = false;
                    break;
                }
            }
        }

        if(!State.getToken(state.getSelectedPeptides()).equals(
                State.getToken(appState.getSelectedPeptides()))) {
            for(String sequence : state.getSelectedPeptides()) {
                if(!server.isPeptideCached(sequence)) {
                    arePeptidesNotCached = false;
                    break;
                }
            }
        }

        //update the queue with the new state that has to be processed
        stateQueue.add(state);

        if(areGroupsNotCached) {
            GroupRequestEvent.fire(this);
        }
        if(areProteinsNotCached) {
            ProteinRequestEvent.fire(this);
        }
        if(arePeptidesNotCached) {
            PeptideRequestEvent.fire(this);
        }

        // If everything is cached we go straight to update the views
        if(areGroupsNotCached && areProteinsNotCached && arePeptidesNotCached) {
            processStateQueue();
        }
        else {
            // It's time to retrieve data and wait for the callback
            if(areGroupsNotCached) {
                server.requestGroups(state.getSelectedGroups());
            }
            if(areProteinsNotCached) {
                server.requestProteins(state.getSelectedProteins());
            }
            if(arePeptidesNotCached) {
                server.requestPeptides(state.getSelectedPeptides());
            }
        }
    }

    private void processStateQueue() {
        // Check if the other data to represent the state arrived already
        if(!isDataReady(stateQueue.peek())) {
            return;
        }

        if(!isStateValid(stateQueue.peek())) {
            InvalidStateEvent.fire(this, "The address cannot be " +
                    "displayed. Please check that is is correct and " +
                    "change it, or go back. If you didn't type the " +
                    "address contact the PRIDE team about the error and " +
                    "explained them what you were doing before this " +
                    "message");
            return;
        }

        goTo(stateQueue.remove());

        //the first state in the queue right now might be ready, who knows?
        processStateQueue();
    }

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

    private boolean isStateValid(State state) {
        if(state == null) {
            return false;
        }

        boolean isCorrect = true;

        for(String accession : state.getSelectedPeptides()) {
            for(String id : state.getSelectedGroups()) {
                if(!server.getGroup(id).getMemberProteins().contains
                        (accession)) {
                    isCorrect = false;
                    break;
                }
            }

            if(!isCorrect) {
                break;
            }

            for(String sequence : state.getSelectedPeptides()) {
                if(!server.getProtein(accession).getPeptides().contains
                        (server.getPeptide(sequence))) {
                    isCorrect = false;
                    break;
                }
            }

            if(!isCorrect) {
                break;
            }
        }

        for(String regionId : state.getSelectedRegions()) {
            try {
                Region.tokenize(regionId);
            }
            catch(Exception e) {
                isCorrect = false;
            }

            if(!isCorrect) {
                break;
            }
        }
        return isCorrect;
    }

    private void goTo(State newState) {
        // tread carefully, when should we update the appState,
        // before or after signaling the change? Should we use some kind of
        // flag to prevent confuzzling?
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
            }
        }
        if(!Arrays.equals(newState.getSelectedPeptides(),
                          appState.getSelectedPeptides())) {
            PeptideUpdateEvent.fire(this, server.getPeptides(newState.getSelectedPeptides()));
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