package uk.ac.ebi.pride.proteomes.web.client.utils.factories;

import com.google.gwt.user.client.ui.Widget;
import uk.ac.ebi.pride.widgets.client.disclosure.client.ModuleContainer;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public abstract class ModuleContainerFactory {

    public static ModuleContainer getModuleContainer(String text){
        Widget collapseIcon = FrontierIcons.getIcon(FrontierIcons.Type.COLLAPSE);
        Widget expandIcon = FrontierIcons.getIcon(FrontierIcons.Type.EXPAND);
        return ModuleContainer.getAdvancedDisclosurePanel(text, collapseIcon, expandIcon);
    }

}
