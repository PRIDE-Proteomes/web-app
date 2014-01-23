package uk.ac.ebi.pride.proteomes.web.client.events.requests;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 30/10/13
 *         Time: 11:49
 */
public class GroupRequestEvent extends GwtEvent<GroupRequestEvent.Handler> {
    public interface Handler extends EventHandler {
        public void onGroupRequestEvent(GroupRequestEvent event);
    }

    private static final Type<Handler> TYPE = new Type<>();

    private GroupRequestEvent(HasHandlers source) {
        super();
        setSource(source);
    }

    public static void fire(HasHandlers source) {
        GroupRequestEvent eventInstance = new GroupRequestEvent(source);
        source.fireEvent(eventInstance);
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
        handler.onGroupRequestEvent(this);
    }
}