package bugsegg.special;

import bananaLib.BMisc.*;
import bananaLib.special.shipInBox;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class eggItem extends shipInBox{
    public static String k(){return "bugsegg";}
    @Override
    public void addExtraDescription(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource, boolean useGray, FleetMemberAPI member) {
        if(member!=null){

            Color c = Misc.getTextColor();
            if (useGray) c = Misc.getGrayColor();
            tooltip.addPara(m.s(k(),"egg_desc"), c,3f).italicize();
        }
    }

    @Override
    public String getActionLabel() {
        return m.s(k(), "egg_action");
    }

    @Override
    public String getName() {
        FleetMemberAPI F = getFleetMember(stack);
        if(F!=null){
            String name = F.getHullSpec().getHullName();
            String cl = "-class";
            if(name.contains(cl)){
                name=name.substring(0, name.length()-cl.length());
            }
            return "Fertilized "+name+" egg";
        }
        return spec.getName();
    }
}
