package recommendationsystem.listeners;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;

/**
 * SelectionChangedListener is the listener used to capture selection events in
 * StructuredViewers. It implements {@link ISelectionChangedListener} but is
 * mainly used for cosmetic reasons.
 * 
 * @author David Piorkowski
 * 
 */
public class SelectionChangedListener implements ISelectionChangedListener
{
    /**
     * Deselects cues (list of words) when they get selected in the
     * TableViewers. This is mostly for cosmetic reasons, since we restrict
     * interactions between the viewers to only include MethodDeclaration data.
     */
    @Override
    public void selectionChanged(SelectionChangedEvent event)
    {
        IStructuredSelection selection = (IStructuredSelection) event
                .getSelection();
        if (selection.getFirstElement() instanceof String[])
        {
            TableViewer tv = (TableViewer) event.getSelectionProvider();
            Table t = tv.getTable();
            t.deselect(t.getSelectionIndex());
        }

    }

}
