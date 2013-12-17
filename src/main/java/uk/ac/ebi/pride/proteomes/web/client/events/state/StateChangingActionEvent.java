package uk.ac.ebi.pride.proteomes.web.client.events.state;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import uk.ac.ebi.pride.proteomes.web.client.UserAction;
import uk.ac.ebi.pride.proteomes.web.client.modules.history.StateChanger;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 18/10/13
 *         Time: 11:37
 */
public class StateChangingActionEvent extends
        GwtEvent<StateChangingActionEvent.Handler> {

    public interface Handler extends EventHandler {
        public void onStateChangingActionEvent(StateChangingActionEvent event);
    }

    private static final Type<Handler> TYPE = new Type<Handler>();

    private final StateChanger changer;
    private final UserAction action;

    protected StateChangingActionEvent(HasHandlers source,
                                       StateChanger changer,
                                       UserAction action) {
        super();
        this.changer = changer;
        this.action = action;
        setSource(source);
    }

    public static void fire(HasHandlers source, StateChanger stateChanger,
                            UserAction action) {
        StateChangingActionEvent eventInstance =
                new StateChangingActionEvent(source, stateChanger, action);
        source.fireEvent(eventInstance);
    }

    public StateChanger getChanger() {
        return changer;
    }

    public UserAction getAction() {
        return action;
    }

    public static Type<Handler> getType() {
        return TYPE;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onStateChangingActionEvent(this);
    }
}
