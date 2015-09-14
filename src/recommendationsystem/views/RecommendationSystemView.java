package recommendationsystem.views;

import java.sql.SQLException;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.*;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;

import recommendationsystem.controllers.DbManager;
import recommendationsystem.controllers.ModelManager;
import recommendationsystem.dnd.MethodDeclarationDragAdapter;
import recommendationsystem.dnd.MethodDeclarationDropAdapter;
import recommendationsystem.listeners.SelectionChangedListener;
import recommendationsystem.logging.ErrorLogger;
import recommendationsystem.logging.MyLogger;
import recommendationsystem.model.CurrentRecommendations;
import recommendationsystem.model.MethodHistory;
import recommendationsystem.model.PinnedMethodList;
import recommendationsystem.preferences.MyPreferences;
import recommendationsystem.providers.CurrentMethodContentProvider;
import recommendationsystem.providers.GeneralLabelProvider;
import recommendationsystem.providers.PinnedContentProvider;
import recommendationsystem.providers.RecommendationsContentProvider;
import recommendationsystem.providers.RecommendationsLabelProvider;
import recommendationsystem.views.PluginStyles.ColorList;
import recommendationsystem.views.adapters.CreatePFISSelectionAdapter;
import recommendationsystem.views.adapters.CreateTFIDFSelectionAdapter;
import recommendationsystem.views.adapters.DatabaseLoaderSelectionAdapter;
import recommendationsystem.views.adapters.LogRunnerSelectionAdapter;
import recommendationsystem.views.adapters.SavePFISSelectionAdapter;
import recommendationsystem.views.adapters.SaveTFIDFSelectionAdapter;
import recommendationsystem.views.listeners.RevealDoubleClickListener;
import recommendationsystem.views.listeners.TableItemPaintListener;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class RecommendationSystemView extends ViewPart
{

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "recommendationsystem.views.RecommendationSystemView";

    private Action deleteAction;
    private Composite parent;
    private MyPreferences prefs;
    private PluginStyles colors;
    private ErrorLogger eLog;

    private TableViewer currentTv, recommendedTv, pinnedTv;

    /**
     * The constructor.
     */
    public RecommendationSystemView()
    {
        prefs = MyPreferences.getInstance();
        colors = PluginStyles.getInstance();
        eLog = ErrorLogger.getInstance();
    }

    @Override
    public void dispose()
    {
        try
        {
            DbManager.getInstance().closeConnection();
            colors.dispose();
        }
        catch (SQLException e)
        {
            eLog.logException(e);
        }
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    public void createPartControl(Composite parent)
    {
        ModelManager.getInstance().setView(this);

        parent.setLayout(new GridLayout());
        this.parent = parent;

        drawMainGui();
        drawPreferencedComponents();
        parent.pack();

        makeActions();
        hookContextMenu();
        hookDoubleClickAction();
    }

    private void drawMainGui()
    {
        Transfer[] types = new Transfer[]
            { TextTransfer.getInstance() };
        // TODO: Make COPY, not MOVE
        int ops = DND.DROP_MOVE;
        GridData gd;

        Label label_currentMethod = new Label(parent, SWT.NONE);
        label_currentMethod.setText("You are here:");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        label_currentMethod.setLayoutData(gd);

        currentTv = new TableViewer(parent);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 24;
        currentTv.getTable().setLayoutData(gd);
        currentTv.setContentProvider(new CurrentMethodContentProvider());
        currentTv.setLabelProvider(new GeneralLabelProvider());
        currentTv.setInput(MethodHistory.getInstance());
        currentTv.addDragSupport(ops, types, new MethodDeclarationDragAdapter(
                currentTv));
        // currentTv.getTable().pack();

        Label label = new Label(parent, SWT.NONE);
        label.setText("You might want to go here:");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        label.setLayoutData(gd);

        recommendedTv = new TableViewer(parent);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 430;
        recommendedTv.getTable().setLayoutData(gd);
        recommendedTv.setContentProvider(new RecommendationsContentProvider());
        recommendedTv.setLabelProvider(new RecommendationsLabelProvider());
        recommendedTv.setInput(CurrentRecommendations.getInstance());
        recommendedTv
                .addSelectionChangedListener(new SelectionChangedListener());

        recommendedTv.addDragSupport(ops, types,
                new MethodDeclarationDragAdapter(recommendedTv));
        ColumnViewerToolTipSupport.enableFor(recommendedTv);
        recommendedTv.getTable().addListener(SWT.PaintItem,
                new TableItemPaintListener());
        // recommendedTv.getTable().pack();

        label = new Label(parent, SWT.NONE);
        label.setText("You saved these:");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        label.setLayoutData(gd);

        pinnedTv = new TableViewer(parent);
        gd = new GridData(GridData.FILL_BOTH);
        pinnedTv.getTable().setLayoutData(gd);
        pinnedTv.setContentProvider(new PinnedContentProvider());
        pinnedTv.setLabelProvider(new RecommendationsLabelProvider());
        pinnedTv.setInput(PinnedMethodList.getInstance());
        pinnedTv.addDropSupport(ops, types, new MethodDeclarationDropAdapter(
                pinnedTv));
        ColumnViewerToolTipSupport.enableFor(pinnedTv);
        // pinnedTv.getTable().pack();
        parent.pack();
    }

    private void drawPreferencedComponents()
    {
        GridData gd;

        if (prefs.showDatabasePreLoadControls())
        {
            Button button_DbLoader = new Button(parent, SWT.PUSH);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            button_DbLoader.setText("Fill Database");
            button_DbLoader.setLayoutData(gd);
            button_DbLoader
                    .addSelectionListener(new DatabaseLoaderSelectionAdapter());
        }

        if (prefs.showModelSaveControls())
        {
            Button button_CreatePFIS = new Button(parent, SWT.PUSH);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            button_CreatePFIS.setText("Create PFIS from database");
            button_CreatePFIS.setLayoutData(gd);
            button_CreatePFIS
                    .addSelectionListener(new CreatePFISSelectionAdapter());

            Button button_CreateTFIDF = new Button(parent, SWT.PUSH);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            button_CreateTFIDF.setText("Create TF-IDF from database");
            button_CreateTFIDF.setLayoutData(gd);
            button_CreateTFIDF
                    .addSelectionListener(new CreateTFIDFSelectionAdapter());

            Button button_SavePFIS = new Button(parent, SWT.PUSH);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            button_SavePFIS.setText("Save PFIS to file");
            button_SavePFIS.setLayoutData(gd);
            button_SavePFIS
                    .addSelectionListener(new SavePFISSelectionAdapter());

            Button button_SaveTFIDF = new Button(parent, SWT.PUSH);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            button_SaveTFIDF.setText("Save TF-IDF to file");
            button_SaveTFIDF.setLayoutData(gd);
            button_SaveTFIDF
                    .addSelectionListener(new SaveTFIDFSelectionAdapter());
        }

        if (prefs.showLogPlaybackControls())
        {
            Text text_LoggerPath = new Text(parent, SWT.SINGLE);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            text_LoggerPath.setLayoutData(gd);

            Button button_RunLogger = new Button(parent, SWT.PUSH);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            button_RunLogger.setText("Playback Log");
            button_RunLogger.setLayoutData(gd);
            button_RunLogger
                    .addSelectionListener(new LogRunnerSelectionAdapter(this,
                            text_LoggerPath));
        }
    }

    public void reveal(ASTNode n)
    {
        CompilationUnit unit = (CompilationUnit) n.getRoot();
        IJavaElement javaElem = unit.getJavaElement();
        IResource rsrc = javaElem.getResource();
        IFile file = (IFile) rsrc;
        System.out.println("Revealing: " + file.getName());
        IFileEditorInput fei = new FileEditorInput(file);

        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchPage page = workbench.getActiveWorkbenchWindow()
                .getActivePage();

        IEditorPart editor = page.findEditor(fei);

        if (editor == null)
        {
            IEditorDescriptor ed = workbench.getEditorRegistry()
                    .getDefaultEditor(file.getLocation().lastSegment());

            try
            {
                editor = page.openEditor(fei, ed.getId());
            }
            catch (PartInitException e)
            {
                eLog.logException(e);
                return;
            }
        }
        if (editor instanceof ITextEditor)
        {
            ITextEditor textEditor = (ITextEditor) editor;
            textEditor.selectAndReveal(n.getStartPosition(), 0);
            page.activate(editor);
        }
    }

    private void hookContextMenu()
    {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener()
        {
            public void menuAboutToShow(IMenuManager manager)
            {
                RecommendationSystemView.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(pinnedTv.getControl());
        pinnedTv.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, pinnedTv);
    }

    private void fillContextMenu(IMenuManager manager)
    {
        manager.add(deleteAction);
    }

    private void makeActions()
    {
        deleteAction = new Action()
        {
            public void run()
            {
                IStructuredSelection sel = (IStructuredSelection) pinnedTv
                        .getSelection();
                if (sel.getFirstElement() != null)
                {
                    MethodDeclaration method;
                    if (sel.getFirstElement() instanceof String[])
                    {
                        Table t = pinnedTv.getTable();
                        method = (MethodDeclaration) t.getItem(
                                t.getSelectionIndex() - 1).getData();
                    }
                    else
                    {
                        method = (MethodDeclaration) sel.getFirstElement();
                    }
                    MyLogger.getInstance().logMethodDeleted(
                            method.resolveBinding().getKey());
                    PinnedMethodList.getInstance().removePinnedMethod(method);
                    pinnedTv.refresh();
                }
            }
        };
        deleteAction.setText("Delete");
        deleteAction.setImageDescriptor(PlatformUI.getWorkbench()
                .getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_ETOOL_DELETE));
    }

    private void hookDoubleClickAction()
    {
        recommendedTv
                .addDoubleClickListener(new RevealDoubleClickListener(this));
        pinnedTv.addDoubleClickListener(new RevealDoubleClickListener(this));
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus()
    {
        parent.setFocus();
    }

    public void setHistoryEnabled(boolean enabled)
    {
        if (!recommendedTv.getTable().isDisposed())
        {
            if (!enabled)
                recommendedTv.getTable().clearAll();
            recommendedTv.getTable().setEnabled(enabled);
        }
    }

    public void clearHistoryTable()
    {
        if (!recommendedTv.getTable().isDisposed())
            recommendedTv.getTable().clearAll();
    }

    public synchronized void updateHistory()
    {
        if (!recommendedTv.getTable().isDisposed())
        {
            boolean visitedMethod = false;
            recommendedTv.refresh();
            for (TableItem i : recommendedTv.getTable().getItems())
            {
                if (i.getData() instanceof MethodDeclaration)
                {
                    if (MethodHistory.getInstance().contains(
                            ((MethodDeclaration) i.getData())))
                    {
                        i.setBackground(colors
                                .getColor(ColorList.VISITED_METHOD_BG));
                        visitedMethod = true;
                    }
                    else
                    {
                        i.setBackground(colors
                                .getColor(ColorList.UNVISITED_METHOD_BG));
                        visitedMethod = false;
                    }
                }
                else
                {
                    if (visitedMethod)
                        i.setBackground(colors
                                .getColor(ColorList.VISITED_METHOD_BG));
                    else
                        i.setBackground(colors
                                .getColor(ColorList.UNVISITED_METHOD_BG));
                }
            }
            // setHistoryEnabled(true);
        }
    }

    public synchronized void updateCurrentMethod()
    {
        if (!currentTv.getTable().isDisposed())
            currentTv.refresh();
    }
}