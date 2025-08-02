package bugsegg;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

public class modPlugin extends BaseModPlugin {


    @Override
    public void onGameLoad(boolean newGame) {
        if(newGame && Global.getSettings().isDevMode()){
            Global.getSector().getPlayerFleet().getCargo().addCommodity(Commodities.FOOD, 200f);
            Global.getSector().getPlayerFleet().getCargo().addCommodity(Commodities.DRUGS, 200f);
            for(int i=0; i<4; i++){
                Global.getSector().getPlayerFleet().getFleetData().addFleetMember("hound_Hull");
            }
        }
    }
}
