package recommendationsystem.views.adapters;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import recommendationsystem.preload.jobs.PFISCreateJob;

public class CreatePFISSelectionAdapter extends SelectionAdapter
{
    @Override
    public void widgetSelected(SelectionEvent e)
    {
        new PFISCreateJob("Create PFIS Topology").schedule();
    }
}