package recommendationsystem;

import org.eclipse.ui.IStartup;

public class Startup implements IStartup
{
    public void earlyStartup()
    {
        RecommendationSystem.getInstance();
    }

}