package uk.ac.ebi.pride.proteomes.web.client.events;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.SimpleEventBus;
import uk.ac.ebi.pride.proteomes.web.client.utils.Console;
import uk.ac.ebi.pride.proteomes.web.client.utils.StringUtils;


/**
 * This event Bus is used for developing process, to get a better idea of the
 * hierarchy of the events that are fired into it.
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 23/10/13
 *         Time: 16:12
 */
public class SnoopingEventBus extends SimpleEventBus {
    private int depth = 0;

    public SnoopingEventBus() {
        super();
    }

    public String getIndentation() {
        return new String(new char[depth]).replace("\0", "  ");
    }

    @Override
    public void fireEvent(Event<?> event) {
        if(Console.VERBOSE){
            String evName = StringUtils.getShortName(event.getClass());

            Console.info("(Event Bus):  " + getIndentation() + evName);
        }

        depth++;
        super.fireEvent(event);
        depth--;
    }

    @Override
    public void fireEventFromSource(Event<?> event, Object source) {
        if(Console.VERBOSE) {
            String clName = StringUtils.getShortName(event.getSource().getClass());
            String evName = StringUtils.getShortName(event.getClass());

            Console.info("(Event Bus):  " + getIndentation() + evName + " <- " + clName);
        }

        depth++;
        super.fireEventFromSource(event, source);
        depth--;
    }
}