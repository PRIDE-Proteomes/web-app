package uk.ac.ebi.pride.proteomes.web.client.modules.history;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.History;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Group;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Protein;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.GroupRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.PeptideRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.requests.ProteinRequestEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.StateChangingActionEvent;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.InconsistentStateException;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.DataServer;

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
    private Queue<State> desiredStates;

    public AppController(EventBus eventBus, DataServer server) {
        this.eventBus = eventBus;
        this.server = server;

        desiredStates = new LinkedList<State>();

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

        desiredStates.add(freshState);
        requestData(freshState);
    }

    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        // We're in the empty landing page, we have to initialize the view to
        // show something relevant
        if(event.getValue().isEmpty()) {
            try {
                appState = State.tokenize(event.getValue());
            } catch (Exception e){ /* Application error, todo */ }

            //UpdateViewEvent.fire(this, "Search for a group or a protein
            //                            first!");
        }
        else {
            State freshState;
            try {
                freshState = State.tokenize(event.getValue());
            }
            catch(InconsistentStateException e) {
                // we tried to create an inconsistent state, that's bad,
                // we should act upon it. (show a popup with an error?)
                return;
            }

            desiredStates.add(freshState);
            requestData(freshState);
        }
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEventFromSource(event, this);
    }

    @Override
    public void onGroupsRetrieved(Collection<Group> groups) {
        // Check if we're done retrieving the data needed to change the state

        // Check if the state is semantically correct


        // For the test app, we send the notification to update the views here
        //goTo(desiredState);
    }

    @Override
    public void onProteinsRetrieved(Collection<Protein> proteins) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onPeptidesRetrieved(Collection<Peptide> peptides) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onRetrievalError(String message) {
        //To change body of implemented methods use File | Settings | File Templates.
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

        if(areGroupsNotCached) {
            GroupRequestEvent.fire(this);
        }
        if(areProteinsNotCached) {
            ProteinRequestEvent.fire(this);
        }
        if(arePeptidesNotCached) {
            PeptideRequestEvent.fire(this);
        }

        // It's time to retrieve data and wait for the callback

        server.requestGroups(state.getSelectedGroups());
        server.requestProteins(state.getSelectedProteins());
        server.requestPeptides(state.getSelectedPeptides());
    }

    private void goTo(State newState) {
        History.newItem(newState.getHistoryToken(), false);
        appState = newState;
        //TextUpdateEvent.fire(this, newState.getText());
    }
}