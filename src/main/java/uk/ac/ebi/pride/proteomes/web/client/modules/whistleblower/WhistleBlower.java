package uk.ac.ebi.pride.proteomes.web.client.modules.whistleblower;

import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.events.SnoopingEventBus;
import uk.ac.ebi.pride.proteomes.web.client.events.state.EmptyViewEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.InvalidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.StateChangingActionEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.ValidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.ErrorOnUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.utils.Console;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 22/10/13
 *         Time: 15:27
 */
public class WhistleBlower implements
                        StateChangingActionEvent.StateChangingActionHandler,
                        ErrorOnUpdateEvent.ErrorOnUpdateHandler,
                        ValidStateEvent.ValidStateHandler,
                        InvalidStateEvent.InvalidStateHandler
{

    private SnoopingEventBus eventBus = null;

    public WhistleBlower(EventBus eventBus) {
        if(eventBus instanceof SnoopingEventBus) {
            this.eventBus = (SnoopingEventBus) eventBus;
        }
        eventBus.addHandler(StateChangingActionEvent.getType(), this);
        eventBus.addHandler(ErrorOnUpdateEvent.getType(), this);
        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(InvalidStateEvent.getType(), this);
    }

    @Override
    public void onStateChangingActionEvent(StateChangingActionEvent event) {
        if(Console.VERBOSE) {
            String clName = event.getSource().getClass().toString();
            clName = clName.substring(clName.lastIndexOf(".") + 1);

            String evName = event.getClass().toString();
            evName = evName.substring(evName.lastIndexOf(".") + 1);

            String vType = event.getChanger().getChanges();

            Console.info("(Whistler): " + getIndentation() + evName +
                    "(\"" + vType + "\")" + " <- " + clName);
        }
    }

    @Override
    public void onUpdateErrorEvent(ErrorOnUpdateEvent event) {
        if(Console.VERBOSE) {
            String clName = event.getSource().getClass().toString();
            clName = clName.substring(clName.lastIndexOf(".") + 1);

            String evName = event.getClass().toString();
            evName = evName.substring(evName.lastIndexOf(".") + 1);

            Console.info("(Whistler): " + getIndentation() + evName +
                    "(\"" + event.getMessage() + "\")" + " <- " + clName);
        }
    }

    @Override
    public void onValidStateEvent(ValidStateEvent event) {
        if(Console.VERBOSE) {
            String clName = event.getSource().getClass().toString();
            clName = clName.substring(clName.lastIndexOf(".") + 1);

            String evName = event.getClass().toString();
            evName = evName.substring(evName.lastIndexOf(".") + 1);

            String vType = "";
            switch(event.getViewType()){
                case Group:
                    vType = "Group View";
                    break;
                case Protein:
                    vType = "Protein View";
                    break;
            }

            Console.info("(Whistler): " + getIndentation() + evName +
                         "(\"" + vType + "\")" + " <- " + clName);
        }
    }

    @Override
    public void onInvalidStateEvent(InvalidStateEvent event) {
        if(Console.VERBOSE) {
            String clName = event.getSource().getClass().toString();
            clName = clName.substring(clName.lastIndexOf(".") + 1);

            String evName = event.getClass().toString();
            evName = evName.substring(evName.lastIndexOf(".") + 1);

            String vType = event.getState();

            Console.info("(Whistler): " + getIndentation() + evName +
                    "(\"" + vType + "\")" + " <- " + clName);
        }
    }

    private String getIndentation() {
        return eventBus.getIndentation().substring(0,
                                    eventBus.getIndentation().length() - 2);
    }
}
