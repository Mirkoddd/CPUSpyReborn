package com.mirko.csr;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class CPUSpyRebornTool {

    static final String TAG = "Tools";
    static final boolean DEBUG = true;

    private static CPUSpyRebornTool instance;

    HashMap<String, String> props;

    public CPUSpyRebornTool() {
        readBuildProp();
    }

    public static CPUSpyRebornTool getInstance() {
        if (instance == null)
            return new CPUSpyRebornTool();
        else
            return instance;
    }

    public static String getROMVersion() {
        // get stock build number
    	String stockVersion = CPUSpyRebornTool.getInstance().getProp("ro.build.display.id");
    	// get AOKP build number
        String aokpVersion = CPUSpyRebornTool.getInstance().getProp("ro.aokp.version");
    	// get PA (or CM too maybe) build number
        String modVersion = CPUSpyRebornTool.getInstance().getProp("ro.modversion");
        // get PA version
        String specificPAVersion = CPUSpyRebornTool.getInstance().getProp("ro.pa.version");
        if (aokpVersion != null) //if rom is is not aokp try to get PA
            return aokpVersion;
        else if (modVersion + specificPAVersion!= null) //if not PA get stock build number (i guess every phone has a build number, so no more "if"
            return  "ParanoidAndroid " + specificPAVersion; // just return normal PA name :D
        else if (modVersion != null) //this just in case some rom use modversion (CM?)
            return modVersion;
        else
            return stockVersion;
        //mmh, and MIUI? i wait for feedbacks
    }


    public static String getDevice() {
    	// get model i.e GT-I9100
        String deviceModel = CPUSpyRebornTool.getInstance().getProp( "ro.product.model");
        // get brand i.e Samsung
        String deviceBrand = CPUSpyRebornTool.getInstance().getProp( "ro.product.brand");
        if (deviceModel + deviceBrand != null) //if i cannot retrieve model and brand i tell the user i didn t got :( 
            return deviceBrand + " " + deviceModel; //if all is ok i tell brand and model i.e. Samsung GT-I9100 :)
        else
            return "info not retrieved";
    }

    public String getProp(String key) {
        if (props != null && props.containsKey(key))
            return props.get(key);

        return null;
    }

    private void readBuildProp() {
        props = new HashMap<String, String>();
        try {
            FileInputStream fstream = new FileInputStream("/system/build.prop");
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] line = strLine.split("=");
                if (line.length > 1)
                    props.put(line[0], line[1]);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}