package bugsegg.recipes;

import bananaLib.BMisc;
import bananaLib.misc.BState;
import bananaLib.misc.CustomDataRequest;
import bananaLib.misc.FleetMemberRequestAsTextList;
import bananaLib.recipes.baseSpecialRecipe;
import bugsegg.special.eggItem;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class produceEgg extends baseSpecialRecipe {

    @Override
    public float getBasePrice(String spec, BState status) {
        try{
            float pr = getPackageBasePrice();
            float p2 = 0;
            p2+=getParent(getParent1Key(),status).getHullSpec().getBaseValue();
            p2+=getParent(getParent2Key(),status).getHullSpec().getBaseValue();
            p2*=0.5f;
            p2*=0.001f;
            return pr+p2;
        }catch (Exception ex){}
        return super.getBasePrice(spec, status);
    }

    public static String getParent1Key(){
        return "10_parent1";
    }
    public static String getParent2Key(){
        return "11_parent2";
    }

    private static String csvPath(){
        return  Global.getSettings().getString("bugsegg_csvPath");
    }
    public static String getOtherParentKey(String key){

        String other = getParent1Key();
        if(other.equals(key)){
            other = getParent2Key();
        }
        return other;
    }

    private static Map<String, String> getCsvData(){
        String id = "parents";
        Map<String, String> out = new HashMap<>();
        try{
            JSONArray array = Global.getSettings().getMergedSpreadsheetData("parents", csvPath());
            for (int i = 0; i < array.length(); i++) {
                try{
                    JSONObject row = array.getJSONObject(i);

                    String cell = row.optString("child");
                    //JSONObject cell = row.optJSONObject("child");
                    out.put(row.optString(id), cell);
                }catch (Exception ignore){}
            }
        }catch (Exception ex){}
        return out;
    }

    private List<String> getParentHulls(String key, BState status){
        String k2 = getOtherParentKey(key);
        FleetMemberAPI other = null;

        boolean hasOther = hasParent(k2, status);
        if(hasOther){
            other = getParent(k2, status);
        }

        List<String> out = new ArrayList<>();
        List<String> pairs = new ArrayList<>(getCsvData().keySet());
        for(String pp: pairs){
            List<String> pp0 = Arrays.asList(pp.split("\\|"));
            String p1 = pp0.get(0);
            if(!hasOther){
                if(!out.contains(p1)){
                    out.add(p1);
                }
                continue;
            }else {
                if(other.getHullSpec().getBaseHullId().equals(p1)){
                    String p2 = pp0.get(1);

                    if(!out.contains(p2)){
                        out.add(p2);
                    }
                }
            }
        }
        return out;
    }


    public String getChild(BState status){
        String k1 = getParent(getParent1Key(), status).getHullSpec().getBaseHullId();
        String k2 = getParent(getParent2Key(), status).getHullSpec().getBaseHullId();
        Map<String, String> data = getCsvData();
        for(String pair: data.keySet()){
            if(pair.contains(k1)&&pair.contains(k2)){

                WeightedRandomPicker<String> rp1 = new WeightedRandomPicker<>();
                String s1 = data.get(pair);
                for(String s2: s1.split("\\|")){
                    List<String> pp0 = Arrays.asList(s2.split(":"));
                    String var = pp0.get(0);
                    int weight = Integer.parseInt(pp0.get(1));
                    rp1.add(var,weight);
                }

                /*
                JSONObject j1 = data.get(pair);
                Iterator<String> keys = j1.keys();
                while (keys.hasNext()){
                    String c1 = keys.next();
                    rp1.add(c1, j1.optInt(c1, 1));
                }

                 */
                return rp1.pick();
            }
        }
        return null;
    }

    public FleetMemberAPI getParent(String key, BState status){
        return (FleetMemberAPI) status.getCustomData().get(key);
    }

    public Boolean hasParent(String key, BState status){
        return getCustomDataRequest("", status).get(key).isValid(
                status.getCustomData().get(key),
                status);
    }

    @Override
    public void complete(String spec, BState status) {
        String id = getChild(status);

        FleetMemberAPI F = Global.getFactory().createFleetMember(FleetMemberType.SHIP, id );

        eggItem.putShipInBox(F, getCargo(status), getProduct(status).get(0));
    }

    public List<FleetMemberAPI> getParentMembers(String key, BState status){
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        List<FleetMemberAPI> out = new ArrayList<>();
        List<String> hulls = getParentHulls(key, status);
        String other = "";
        if(hasParent(getOtherParentKey(key),status)){
            other = getParent(getOtherParentKey(key),status).getId();
        }
        for(FleetMemberAPI F: fleet.getMembersWithFightersCopy()){
            if(F.isFighterWing())continue;
            if(F.isMothballed())continue;
            if(F.getId().equals(other))continue;
            String id = F.getHullSpec().getBaseHullId();
            if(hulls.contains(id)){
                out.add(F);
            }
        }
        return  out;
    }
    @Override
    public Map<String, CustomDataRequest> getCustomDataRequest(String spec, BState status) {
        Map<String, CustomDataRequest> out = new HashMap<>();
        for(String kk: Arrays.asList(getParent1Key(), getParent2Key())){
            final String kk1 = kk;
            out.put(kk1, new FleetMemberRequestAsTextList(){

                @Override
                public Object getDefaultValue(BState status) {
                    return null;
                    //return super.getDefaultValue(status);
                }

                @Override
                public List<FleetMemberAPI> getMembers(BState status) {
                    List<FleetMemberAPI> out = getParentMembers(kk1, status);
                    out.add(BMisc.getBlankMember());
                    return out;
                }

                @Override
                public String getDataRequestLabel(BState status) {
                    return "select parent";
                }
            });
        }
        return out;
    }
}
