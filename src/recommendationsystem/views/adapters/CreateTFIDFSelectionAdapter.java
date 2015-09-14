package recommendationsystem.views.adapters;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import recommendationsystem.preload.jobs.TFIDFCreateJob;

public class CreateTFIDFSelectionAdapter extends SelectionAdapter
{
    @Override
    public void widgetSelected(SelectionEvent e)
    {
        new TFIDFCreateJob("Create TFIDF Matrix").schedule();
    }
}