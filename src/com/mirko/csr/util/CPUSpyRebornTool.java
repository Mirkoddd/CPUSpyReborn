package com.mirko.csr.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class CPUSpyRebornTool {

    static final String TAG = "Tools";
    static final boolean DEBUG = true;
    private static final String LOG_TAG = "DeviceInfoSettings";

    private static final String FILENAME_PROC_VERSION = "/proc/version";
    private static final String FILENAME_PROC_CPUINFO = "/proc/cpuinfo";
    private static final String FILENAME_PROC_MEMINFO = "/proc/meminfo";

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
        if (aokpVersion != null) //if rom is is not aokp try to get PA
            return aokpVersion;
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
    private static String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }

    public static String getFormattedKernelVersion() {
        try {
            return formatKernelVersion(readLine(FILENAME_PROC_VERSION));

        } catch (IOException e) {
            Log.e(LOG_TAG,
                "IO Exception when getting kernel version for Device Info screen",
                e);

            return "Unavailable";
        }
    }

    public static String formatKernelVersion(String rawKernelVersion) {
        // Example (see tests for more):
        // Linux version 3.0.31-g6fb96c9 (android-build@xxx.xxx.xxx.xxx.com) \
        //     (gcc version 4.6.x-xxx 20120106 (prerelease) (GCC) ) #1 SMP PREEMPT \
        //     Thu Jun 28 11:02:39 PDT 2012

        final String PROC_VERSION_REGEX =
            "Linux version (\\S+) " + /* group 1: "3.0.31-g6fb96c9" */
            "\\((\\S+?)\\) " +        /* group 2: "x@y.com" (kernel builder) */
            "(?:\\(gcc.+? \\)) " +    /* ignore: GCC version information */
            "(#\\d+) " +              /* group 3: "#1" */
            "(?:.*?)?" +              /* ignore: optional SMP, PREEMPT, and any CONFIG_FLAGS */
            "((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)"; /* group 4: "Thu Jun 28 11:02:39 PDT 2012" */

        Matcher m = Pattern.compile(PROC_VERSION_REGEX).matcher(rawKernelVersion);
        if (!m.matches()) {
            Log.e(LOG_TAG, "Regex did not match on /proc/version: " + rawKernelVersion);
            return "Unavailable";
        } else if (m.groupCount() < 4) {
            Log.e(LOG_TAG, "Regex match on /proc/version only returned " + m.groupCount()
                    + " groups");
            return "Unavailable";
        }
        return m.group(1) + "\n" +                 // 3.0.31-g6fb96c9
            m.group(2) + " " + m.group(3) + "\n" + // x@y.com #1
            m.group(4);                            // Thu Jun 28 11:02:39 PDT 2012
    }
    
    public static String getMemInfo() {
        String result = null;
        BufferedReader reader = null;

        try {
            /* /proc/meminfo entries follow this format:
             * MemTotal:         362096 kB
             * MemFree:           29144 kB
             * Buffers:            5236 kB
             * Cached:            81652 kB
             */
            String firstLine = readLine(FILENAME_PROC_MEMINFO);
            if (firstLine != null) {
                String parts[] = firstLine.split("\\s+");
                if (parts.length == 3) {
                    result = Long.parseLong(parts[1])/1024 + " MB";
                }
            }
        } catch (IOException e) {}

        return result;
    }

    public static String getCPUInfo() {
        String result = null;

        try {
            /* The expected /proc/cpuinfo output is as follows:
             * Processor	: ARMv7 Processor rev 2 (v7l)
             * BogoMIPS	: 272.62
             */
            String firstLine = readLine(FILENAME_PROC_CPUINFO);
            if (firstLine != null) {
                result = firstLine.split(":")[1].trim();
            }
        } catch (IOException e) {}

        return result;
    }

}