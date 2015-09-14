package recommendationsystem.views.adapters;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Text;

import recommendationsystem.playback.LogPlaybackThreadDispatcher;
import recommendationsystem.views.RecommendationSystemView;

public class LogRunnerSelectionAdapter extends SelectionAdapter
{
    private RecommendationSystemView view;
    private Text text_LoggerPath;

    public LogRunnerSelectionAdapter(RecommendationSystemView view, Text textbox)
    {
        this.view = view;
        text_LoggerPath = textbox;
    }

    @Override
    public void widgetSelected(SelectionEvent e)
    {
        LogPlaybackThreadDispatcher lrtm = new LogPlaybackThreadDispatcher(
                text_LoggerPath.getText(), view);
        lrtm.start();
    }
}