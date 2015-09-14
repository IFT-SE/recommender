package recommendationsystem.views.adapters;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import recommendationsystem.preload.jobs.DatabaseLoadJob;

public class DatabaseLoaderSelectionAdapter extends SelectionAdapter
{
    @Override
    public void widgetSelected(SelectionEvent e)
    {
        new DatabaseLoadJob("Fill database").schedule();
    }
}
