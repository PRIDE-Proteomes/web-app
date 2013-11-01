package uk.ac.ebi.pride.proteomes.web.client.events.updates;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 22/10/13
 *         Time: 11:17
 */
public class TextUpdateEvent extends
        GwtEvent<TextUpdateEvent.TextUpdateHandler> {

    public interface TextUpdateHandler extends EventHandler {
        public void onTextUpdateEvent(TextUpdateEvent event);
    }

    private static final Type<TextUpdateHandler> TYPE = new Type<TextUpdateHandler>();

    private String text;

    public TextUpdateEvent(String text, HasHandlers source) {
        super();
        this.text = text;
        setSource(source);
    }

    public static void fire(HasHandlers source, String text) {
        TextUpdateEvent eventInstance = new TextUpdateEvent(text, source);
        source.fireEvent(eventInstance);
    }

    public String getText() {
        return text;
    }

    public static Type<TextUpdateHandler> getType() {
        return TYPE;
    }

    @Override
    public Type<TextUpdateHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(TextUpdateHandler handler) {
        handler.onTextUpdateEvent(this);
    }
}
