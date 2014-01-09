package uk.ac.ebi.pride.proteomes.web.client.modules.whistleblower;

import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.events.SnoopingEventBus;
import uk.ac.ebi.pride.proteomes.web.client.events.state.InvalidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.StateChangingActionEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.state.ValidStateEvent;
import uk.ac.ebi.pride.proteomes.web.client.events.updates.ErrorOnUpdateEvent;
import uk.ac.ebi.pride.proteomes.web.client.utils.Console;
import uk.ac.ebi.pride.proteomes.web.client.utils.StringUtils;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 22/10/13
 *         Time: 15:27
 */
public class WhistleBlower implements StateChangingActionEvent.Handler,
                                      ErrorOnUpdateEvent.Handler,
                                      ValidStateEvent.Handler,
                                      InvalidStateEvent.Handler
{
    private EventBus eventBus = null;

    public WhistleBlower(EventBus eventBus) {
        this.eventBus = eventBus;

        eventBus.addHandler(StateChangingActionEvent.getType(), this);
        eventBus.addHandler(ErrorOnUpdateEvent.getType(), this);
        eventBus.addHandler(ValidStateEvent.getType(), this);
        eventBus.addHandler(InvalidStateEvent.getType(), this);
    }

    @Override
    public void onStateChangingActionEvent(StateChangingActionEvent event) {
        if(Console.VERBOSE) {
            String clName = StringUtils.getShortName(event.getSource().getClass());
            String evName = StringUtils.getShortName(event.getClass());
            String vType = event.getChanger().toString();

            Console.info("(Whistler):   " + getIndentation() + evName +
                    "(\"" + vType + "\")" + " <- " + clName);
        }
    }

    @Override
    public void onUpdateErrorEvent(ErrorOnUpdateEvent event) {
        if(Console.VERBOSE) {
            String clName = StringUtils.getShortName(event.getSource().getClass());
            String evName = StringUtils.getShortName(event.getClass());

            Console.info("(Whistler):   " + getIndentation() + evName +
                    "(\"" + event.getMessage() + "\")" + " <- " + clName);
        }
    }

    @Override
    public void onValidStateEvent(ValidStateEvent event) {
        if(Console.VERBOSE) {
            String clName = StringUtils.getShortName(event.getSource().getClass());
            String evName = StringUtils.getShortName(event.getClass());
            String vType = "";
            switch(event.getViewType()){
                case Group:
                    vType = "Group View";
                    break;
                case Protein:
                    vType = "Protein View";
                    break;
            }

            Console.info("(Whistler):   " + getIndentation() + evName +
                         "(\"" + vType + "\")" + " <- " + clName);
        }
    }

    @Override
    public void onInvalidStateEvent(InvalidStateEvent event) {
        if(Console.VERBOSE) {
            String clName = StringUtils.getShortName(event.getSource().getClass());
            String evName = StringUtils.getShortName(event.getClass());
            String vType = event.getState();

            Console.info("(Whistler): " + getIndentation() + evName +
                    "(\"" + vType + "\")" + " <- " + clName);
        }
    }

    private String getIndentation() {
        if(eventBus instanceof SnoopingEventBus) {
            String indent = ((SnoopingEventBus) eventBus).getIndentation();
            return indent.substring(0, indent.length() - 2);
        }
        else {
            return "";
        }
    }
}
