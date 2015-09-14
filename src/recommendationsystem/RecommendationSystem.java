package recommendationsystem;

import recommendationsystem.controllers.ModelManager;
import recommendationsystem.listeners.SelectionListener;

public class RecommendationSystem
{
    // TODO: mark and reconsider synchronized methods, explain why synchronized
    private static RecommendationSystem instance;

    private RecommendationSystem()
    {
    }

    public static synchronized RecommendationSystem getInstance()
    {
        if (instance == null)
        {
            instance = new RecommendationSystem();
            instance.init();
        }
        return instance;
    }

    private void init()
    {
        System.out.println("Initializing plug-in...");
        SelectionListener.getInstance().register();
        ModelManager.getInstance().reset();
    }
}
