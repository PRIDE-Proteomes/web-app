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
import uk.ac.ebi.pride.proteomes.web.client.events.StateChangingActionEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.TextUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.InconsistentStateException;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.DataServer;

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
    private DataServer server = null;
    private State appState;

    public AppController(EventBus eventBus) {
        this.eventBus = eventBus;

        eventBus.addHandler(StateChangingActionEvent.getType(), this);
        History.addValueChangeHandler(this);
    }

    @Override
    public void onStateChangingActionEvent(StateChangingActionEvent event) {
        State desiredState = null;
        try {
            desiredState = event.getChanger().change(appState);
        }
        catch(InconsistentStateException e) {
            // we tried to create an inconsistent state, that's bad,
            // we should act upon it. (show a popup with an error?)
        }

        // If we cannot request for any data don't do anything
        // (This should never happen)
        if(server == null) {
            return;
        }
        // Check what's cached or not and notify the application that some
        // data may take some time to be retrieved, we don't want the users
        // to think that the web app is unresponsive.

        // It's time to retrieve data

        // Check if the state is semantically correct

        // For the test app, we send the notification to update the views here
        goTo(desiredState);
    }

    private void goTo(State newState) {
        History.newItem(newState.getHistoryToken(), false);
        appState = newState;
        //TextUpdateEvent.fire(this, newState.getText());
    }

    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        State desiredState = null;

        // We're in the empty landing page, we have to initialize the view to
        // show something relevant
        if(event.getValue().isEmpty()) {
            try {
                appState = State.tokenize(event.getValue());
            } catch (Exception e){ /* TODO */ }

            TextUpdateEvent.fire(this, "No button pressed yet!");
        }
        else {
            try {
                desiredState = State.tokenize(event.getValue());
            }
            catch(InconsistentStateException e) { /* TODO */ }

            // we've "checked" the desired state is valid and have all the data
            // to represent this
            goTo(desiredState);
        }
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEventFromSource(event, this);
    }

    @Override
    public void onGroupRetrieved(Group group) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onProteinRetrieved(Protein protein) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onPeptideRetrieved(Peptide peptide) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onRetrievalError(String message) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void bindServer(DataServer server) {
        if(this.server != null)
            this.server = server;
    }
}