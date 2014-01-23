package uk.ac.ebi.pride.proteomes.web.client.modules;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;

/**
 * All the presenters in the application should extend this class
 * this allows them to put their views in an assigned container. This also
 * allows them to manage all the region inside the container,
 * as well as the container properties, such as size.
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 17/10/13
 *         Time: 10:28
 */
public abstract class Presenter<V extends View> extends EventSender {
    private final V view;

    protected Presenter(EventBus eventBus, V view) {
        super(eventBus);
        this.view = view;
    }

    public void bindToContainer(final AcceptsOneWidget container) {
        view.bindToContainer(container);
    }

    protected V getView() {
        return view;
    }
}
