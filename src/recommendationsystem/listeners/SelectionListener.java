package recommendationsystem.listeners;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import recommendationsystem.controllers.ModelManager;

/**
 * SelectionListener reacts to selection events that occur in Eclipse's
 * workbench's pages. Although it reacts to all selection events, we limit it to
 * only react to text selection events in editors. This class is what drives the
 * models, each selection is evaluated and acted of if applicable by the
 * {@link ModelManager}.
 * <p>
 * SelectionListener is a singleton class. Call <code>getInstance()</code> to
 * use it.
 * 
 * @author David Piorkowski
 * 
 */
public class SelectionListener implements ISelectionListener
{
    private static SelectionListener instance;
    private ModelManager modelManager;

    /**
     * The singleton constructor.
     */
    private SelectionListener()
    {
        // SelectionListener is a singleton class. Do not put anything here.
    }

    /**
     * Get the instance of SelectionListener.
     * 
     * @return A singleton instance of SelectionListener
     */
    public static synchronized SelectionListener getInstance()
    {
        if (instance == null)
        {
            instance = new SelectionListener();
            instance.init();
        }
        return instance;
    }

    /**
     * SelectionListener is singleton. Don't allow clones.
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    /**
     * Initializes the private class variables used. This method should not be
     * called externally.
     */
    public void init()
    {
        modelManager = ModelManager.getInstance();
    }

    /**
     * Fires whenever a selection event occurs. The events are then filtered to
     * only include those that are text selection in editor parts. These
     * selections are then passed along to the ModelManager for processing.
     */
    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection)
    {
        if (selection instanceof TextSelection && part instanceof IEditorPart)
        {
            TextSelection textSelection = (TextSelection) selection;
            System.out
                    .println("TextSelection at: " + textSelection.getOffset());

            IEditorInput input = ((IEditorPart) part).getEditorInput();
            IFile sourceFile = ((IFileEditorInput) input).getFile();

            modelManager.updateModels(textSelection, sourceFile);
        }
    }

    /**
     * Iterates over all the parts and pages of Eclipse's workbench registering
     * this selection listener to them. This should be called only once.
     */
    public void register()
    {
        for (IWorkbenchWindow window : PlatformUI.getWorkbench()
                .getWorkbenchWindows())
        {
            for (IWorkbenchPage page : window.getPages())
            {
                // System.out.println("Registering for " + page.getClass());
                page.addPostSelectionListener(this);
            }
        }
    }
}
