package org.ekstep.ep.samza.domain;

import com.google.gson.Gson;
import org.ekstep.ep.samza.core.Logger;

import java.util.*;

public class Telemetry {

    static Logger LOGGER = new Logger(Telemetry.class);

    private final String ver = "3.0";
    private String eid;
    private long ets;
    private String mid;
    private Actor actor;
    private Context context;
    private TObject object;
    private HashMap<String, Object> edata;
    private List<String> tags = new ArrayList<>();
    private HashMap<String, String> metadata;
    private long syncts;

    public Telemetry() {

    }

    public Telemetry(Map<String, Object> eventSpec) {

        try {
            List<Map<String, Object>> events = (List<Map<String, Object>>) eventSpec.get("events");
            Map<String, Object> event = events.get(0);
            long syncts = ((Number)eventSpec.get("syncts")).longValue();
            long ets = ((Number)eventSpec.get("ets")).longValue();

            List<String> tags = (List<String>) event.get("tags");
            String mid = (String) eventSpec.get("mid");

            int eventCount = events.size();
            String status = (String) eventSpec.get("sync_status");
            String ver = (String) eventSpec.get("ver");
            if(null==status){
                status = "SUCCESS";
            }
            String consumerId = (String) eventSpec.get("consumer_id");
            if(null==consumerId){
                consumerId = "";
            }

            HashMap<String, Object> edata = new HashMap<String, Object>();
            edata.put("type", "telemetry_audit");
            edata.put("level", "INFO");
            edata.put("message", "telemetry sync");
            edata.put("pageid", "data-pipeline");

            List<Map<String, Object>> params = new ArrayList<Map<String, Object>>();
            Map<String, Object> param = new HashMap<>();
            param.put("sync_status", status);
            param.put("consumer_id", consumerId);
            param.put("events_count", eventCount);
            param.put("ver", ver);
            params.add(param);
            edata.put("params", params);

            this.eid = "LOG";
            this.ets = ets;
            this.syncts = syncts;
            this.tags = tags;
            this.edata = edata;
            this.mid = computeMid(this.eid, mid);
            this.metadata = new HashMap<>();
            this.actor = new Actor(eventSpec);
            this.context = new Context(eventSpec);
            this.object = new TObject(eventSpec);
        }catch(Exception e){

            LOGGER.info("","Failed to initialize telemetry spec data: "+ e.getMessage());
        }

    }

    public String computeMid(String eid, String mid) {
        // Because v2->v3 is one to many, mids have to be changed
        // We just prefix the LOG EID with telemetry spec mid
        return String.format("%s:%s", eid, mid);
    }


    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    public long getEts() {
        return ets;
    }

    public void setEts(long ets) {
        this.ets = ets;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public String getVer() {
        return ver;
    }

    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public TObject getObject() {
        return object;
    }

    public void setObject(TObject object) {
        this.object = object;
    }

    public HashMap<String, Object> getEdata() {
        return edata;
    }

    public void setEdata(HashMap<String, Object> edata) {
        this.edata = edata;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private long getSyncts() {
        return this.syncts;
    }

    private void setSyncts(long syncts) {
        this.syncts = syncts;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> v3map = new HashMap<>();
        v3map.put("eid", eid);
        v3map.put("ets", ets);
        v3map.put("ver", ver);
        v3map.put("mid", mid);
        v3map.put("actor", actor);
        v3map.put("context", context);
        v3map.put("object", object);
        v3map.put("metadata", metadata);
        v3map.put("edata", edata);
        v3map.put("tags", tags);
        v3map.put("syncts", syncts);
        return v3map;
    }

    public String toJson() {
        Map<String, Object> map = toMap();
        return new Gson().toJson(map);
    }
}