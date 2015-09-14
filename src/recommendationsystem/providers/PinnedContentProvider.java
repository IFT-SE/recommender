package recommendationsystem.providers;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import recommendationsystem.controllers.ModelManager;
import recommendationsystem.model.PinnedMethodList;

/**
 * PinnedContentProvider populates the data fields of the pinned method viewer
 * located at the bottom of the plug-in. It calls on {@link PinnedMethodList} to
 * determine the current pinned methods and their cues before sending it out to
 * the table.
 * <p>
 * The pinned method list is not stored here, but in PinnedMethodList. The
 * expectation is that inputChanged will be called once, when setInput() is
 * called on the viewer, and from then on any change of state is stored in the
 * classes under the model package.
 * 
 * @author David Piorkowski
 * 
 */
public class PinnedContentProvider implements IStructuredContentProvider
{
    private PinnedMethodList pinnedMethodList;
    private ModelManager modelManager;

    /**
     * Does nothing.
     */
    @Override
    public void dispose()
    {
    }

    /**
     * Initializes the {@link ModelManager} and sets PinnedMethodList as the
     * input source.
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
        pinnedMethodList = (PinnedMethodList) newInput;
        modelManager = ModelManager.getInstance();
    }

    /**
     * Depending on whether words are enabled or not, the Object array contains
     * two different possibilities. If words are enabled, the array contains a
     * {@link MethodDeclaration} node followed by a String[] of cues for that
     * node and repeats. If words are disabled, the Object array contains only
     * MethodDeclaration nodes.
     * <p>
     * All data is gathered from the {@link PinnedMethodList} which is used to
     * store the state of the list of pinned methods.
     * <p>
     * Returns:
     * <ul>
     * <li>Object [ MethodDeclaration, String[], MethodDeclaration, String[] ...
     * ] if words are enabled
     * <li>Object [ MethodDeclaration, MethodDeclaration, ... ] if words are not
     * enabled
     * </ul>
     */
    @Override
    public Object[] getElements(Object inputElement)
    {
        MethodDeclaration[] currentPinned = pinnedMethodList.getPinnedMethods();
        String[][] allCues = pinnedMethodList.getCues();

        Object[] rv;

        if (modelManager.getWordsEnabled())
        {

            rv = new Object[2 * currentPinned.length];

            for (int i = 0; i < currentPinned.length; i++)
            {
                MethodDeclaration method = currentPinned[i];
                String[] cues = allCues[i];
                rv[2 * i] = method;
                rv[2 * i + 1] = cues;
            }
        }
        else
        {
            rv = new Object[currentPinned.length];
            for (int i = 0; i < currentPinned.length; i++)
            {
                MethodDeclaration method = currentPinned[i];
                rv[i] = method;
            }
        }

        return rv;
    }
}
