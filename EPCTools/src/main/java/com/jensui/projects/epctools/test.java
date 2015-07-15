package com.jensui.projects.epctools;

/**
 * Created by christian on 23.06.15.
 */
public class test {

    static EPCTools tools = new EPCTools();

    public static void main(String[] args) throws  Exception {

        tools.parseHexString("CF02032533139342DFDC1C35");
        System.out.println(tools.getGTIN("CF02032533139342DFDC1C35"));

    }

}
