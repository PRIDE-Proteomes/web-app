package uk.ac.ebi.pride.proteomes.web.client.events.updates;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Group;

import java.util.List;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 14:15
 */
public class GroupUpdateEvent extends GwtEvent<GroupUpdateEvent.Handler> {
    public interface Handler extends EventHandler {
        void onGroupUpdateEvent(GroupUpdateEvent event);
    }

    private static final GwtEvent.Type<Handler> TYPE = new GwtEvent.Type<>();

    private final List<Group> GroupList;

    private GroupUpdateEvent(List<Group> Groups, HasHandlers source) {
        super();
        GroupList = Groups;
        setSource(source);
    }

    public static void fire(HasHandlers source, List<Group> Groups) {
        GroupUpdateEvent eventInstance = new GroupUpdateEvent(Groups, source);
        source.fireEvent(eventInstance);
    }

    public List<Group> getGroups() {
        return GroupList;
    }

    public static GwtEvent.Type<Handler> getType() {
        return TYPE;
    }

    @Override
    public GwtEvent.Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onGroupUpdateEvent(this);
    }
}