package uk.ac.ebi.pride.proteomes.web.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import uk.ac.ebi.pride.proteomes.web.client.modules.history.StateChanger;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 18/10/13
 *         Time: 11:37
 */
public class StateChangingActionEvent extends
        GwtEvent<StateChangingActionEvent.StateChangingActionHandler> {

    public interface StateChangingActionHandler extends EventHandler {
        public void onStateChangingActionEvent(StateChangingActionEvent event);
    }

    private static final Type<StateChangingActionHandler> TYPE = new Type<StateChangingActionHandler>();

    private final StateChanger changer;

    protected StateChangingActionEvent(StateChanger changer, HasHandlers source) {
        super();
        this.changer = changer;
        setSource(source);
    }

    public static void fire(HasHandlers source, StateChanger stateChanger) {
        StateChangingActionEvent eventInstance =
                new StateChangingActionEvent(stateChanger, source);
        source.fireEvent(eventInstance);
    }

    public StateChanger getChanger() {
        return changer;
    }

    public static Type<StateChangingActionHandler> getType() {
        return TYPE;
    }
    @Override
    public Type<StateChangingActionHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(StateChangingActionHandler handler) {
        handler.onStateChangingActionEvent(this);
    }
}
