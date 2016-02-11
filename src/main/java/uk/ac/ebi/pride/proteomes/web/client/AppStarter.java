package uk.ac.ebi.pride.proteomes.web.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.ModificationWithPosition;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.PeptideMatch;
import uk.ac.ebi.pride.proteomes.web.client.modules.Presenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.View;
import uk.ac.ebi.pride.proteomes.web.client.modules.coverage.CoveragePresenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.coverage.CoverageView;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.DataProvider;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.DataServer;
import uk.ac.ebi.pride.proteomes.web.client.modules.googleanalytics.Reporter;
import uk.ac.ebi.pride.proteomes.web.client.modules.grouppeptides.GroupPeptidePresenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.header.HeaderPresenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.header.HeaderView;
import uk.ac.ebi.pride.proteomes.web.client.modules.history.AppController;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.GridView;
import uk.ac.ebi.pride.proteomes.web.client.modules.lists.ListView;
import uk.ac.ebi.pride.proteomes.web.client.modules.main.MainPresenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.main.MainView;
import uk.ac.ebi.pride.proteomes.web.client.modules.modifications.ModificationsPresenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.peptides.PeptidesPresenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.peptiforms.PeptiformsPresenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.sequence.SequencePresenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.sequence.SequenceView;
import uk.ac.ebi.pride.proteomes.web.client.modules.tissues.TissuesPresenter;
import uk.ac.ebi.pride.proteomes.web.client.modules.whistleblower.WhistleBlower;
import uk.ac.ebi.pride.proteomes.web.client.utils.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * The appController is in charge of creating the graphical structure of the
 * application, by binding presenters and panels they can attach to and
 * then control.
 * It also instantiates all the objects that listen to the event bus.
 * ?? Also manages history and presenter view coordination ??
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 15/10/13
 *         Time: 16:54
 */
class AppStarter implements RunAsyncCallback {

    private static final String webServiceRoot = "/pride/ws/proteomes";
    private final EventBus eventBus;
    private HasWidgets container;

    public AppStarter(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void bindToContainer(HasWidgets container) {
        this.container = container;

        // lazy initialization
        GWT.runAsync(this);
    }

    @Override
    public void onFailure(Throwable reason) { }

    @Override
    public void onSuccess() {
        // Stick together the whole app first by creating the listeners to
        // the event bus, then creating the structure of the graphical elements

        // The two lists should have the same size
        List<Presenter> presenterList = new ArrayList<>();
        List<AcceptsOneWidget> placeHolderList = new
                ArrayList<>();

        // Startup loggers & reporters
        new Reporter(eventBus);
        new WhistleBlower(eventBus);

        // Startup the History and Data Retrieval modules
        DataServer provider = new DataProvider(webServiceRoot);
        AppController appController = new AppController(eventBus, provider);
        provider.bind(appController);

        // Startup the widget-containing modules
        View headerView = new HeaderView();
        Presenter headerPresenter = new HeaderPresenter(eventBus,
                                            (HeaderPresenter.ThisView) headerView);

        ListView<String> tissueView = new GridView<>("Tissues", "tissue");
        Presenter tissuePresenter = new TissuesPresenter(eventBus, tissueView);

//        ListView<Multiset.Entry<String>> modView = new GridView<>("Modifications", "modification");
//        Presenter modPresenter = new ModificationsPresenter(eventBus, modView);

        ListView<ModificationWithPosition> modView = new GridView<>("Modifications", "modification");
        Presenter modPresenter = new ModificationsPresenter(eventBus, modView);

        View coverageView = new CoverageView();
        Presenter coveragePresenter = new CoveragePresenter(eventBus,
                                          (CoveragePresenter.ThisView) coverageView);

        View sequenceView = new SequenceView();
        Presenter sequencePresenter = new SequencePresenter(eventBus,
                                          (SequencePresenter.ThisView) sequenceView);

        ListView<PeptideMatch> peptideView = new GridView<>("Peptides", "peptide");
        Presenter peptidePresenter = new PeptidesPresenter(eventBus,
                peptideView);

        ListView<Pair<String, List<String>>> groupPeptideView = new GridView<>("Peptides unique to the group", "peptide", true);
        Presenter groupPeptidePresenter = new GroupPeptidePresenter(eventBus, groupPeptideView);

        ListView<Peptide> peptiformView = new GridView<>("Peptiforms", "peptiform");
        Presenter peptiformPresenter = new PeptiformsPresenter(eventBus, peptiformView);

        // Startup the main presenter, it's used as a container to hold the
        // rest of the widget-showing modules.
        presenterList.add(headerPresenter);
        presenterList.add(groupPeptidePresenter);
        presenterList.add(coveragePresenter);
        presenterList.add(sequencePresenter);
        presenterList.add(tissuePresenter);
        presenterList.add(modPresenter);
        presenterList.add(peptidePresenter);
        presenterList.add(peptiformPresenter);

        for(Presenter p : presenterList) {
            placeHolderList.add(new SimplePanel());
        }

        MainPresenter.ThisView mainView = new MainView(placeHolderList);
        Presenter mainPresenter = new MainPresenter(eventBus,
                                                    mainView,
                                                    presenterList);

        // Link the main presenter to the root container.
        SimplePanel mainPanel = new SimplePanel();
        container.add(mainPanel);

        mainPresenter.bindToContainer(mainPanel);

        // fire first event to reach initial state
        History.fireCurrentHistoryState();
    }
}
