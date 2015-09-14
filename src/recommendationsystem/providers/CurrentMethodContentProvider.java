package recommendationsystem.providers;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import recommendationsystem.controllers.MethodManager;
import recommendationsystem.model.MethodHistory;

/**
 * CurrentMethodContentProvider populates the data fields of the Current method
 * viewer located at the top of the plug-in. It calls on {@link MethodHistory}
 * to determine the current location before sending it out to the table for
 * display.
 * <p>
 * The current method is not stored here, but in MethodHistory. The expectation
 * is that inputChanged will be called once, when setInput() is called on the
 * viewer, and from then on any change of state is stored in the classes under
 * the model package.
 * 
 * @author David Piorkowski
 * 
 */
public class CurrentMethodContentProvider implements IStructuredContentProvider
{
    private MethodHistory methodHistory;
    private MethodManager methodManager;

    /**
     * Does nothing.
     */
    @Override
    public void dispose()
    {
    }

    /**
     * Initializes the {@link MethodManager} and sets MethodHistory as the input
     * source.
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
        methodHistory = (MethodHistory) newInput;
        methodManager = MethodManager.getInstance();
    }

    /**
     * Returns the current method in a single element array containing a
     * {@link MethodDeclaration} node determined from the most current key in
     * MethodHistory.
     * <p>
     * Returns: Object [ MethodDeclaration ]
     */
    @Override
    public Object[] getElements(Object inputElement)
    {
        if (methodHistory.getMostRecentMethodKey() == null)
            return new MethodDeclaration[0];

        return new MethodDeclaration[]
            { methodManager.getMethodDeclarationFromMethodKey(methodHistory
                    .getMostRecentMethodKey()) };
    }
}
