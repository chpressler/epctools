/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.jensui.epctools;

import java.util.HashMap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.*;

/**
 *
 * @author chpressler
 */
public class EPCToolsTest {
    
    EPCTools epcTools;
    
    public EPCToolsTest() {
        epcTools = new EPCTools();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
   
    @Test
    public void checkGTIN() {
        assertTrue(epcTools.isValidGTIN("00614141453245"));
        assertFalse(epcTools.isValidGTIN("00614141453246"));
    }
    
    @Test
    public void checkSGTIN() throws Exception {
        assertTrue(epcTools.isSGTIN96("30381D5D419C238000000001"));
    }
    
    @Test
    public void testIsGTINSSCC() throws Exception {
       Assert.assertTrue(epcTools.isSSCC("3154F618B8B2D05E00000000"));
       Assert.assertTrue(epcTools.isSGTIN96("30740242204031C0000003E7"));
       Assert.assertTrue(epcTools.isSGTIN198("36381DB78038AF5A3060D183060C583062C00000000000000000"));
    }
    
    @Test
    public void testCreateSGTIN96HEX() throws Exception {
        String hex = epcTools.createSGTIN_96HexEPC(0, 6, "0000000", "00000", "1");
        Assert.assertEquals("301800000000000000000001", hex);
        
        hex = epcTools.createSGTIN_96HexEPC(2, 6, "30069", "422030", "123456789");
        Assert.assertEquals("30581D5D419C2380075BCD15", hex);
        
        hex = epcTools.createSGTIN_96HexEPC(1, 6, "30069", "422030", "1");
        Assert.assertEquals("30381D5D419C238000000001", hex);
        
        hex = epcTools.createSGTIN_96HexEPC(3, 5, "0037000", "65735", "999");
        Assert.assertEquals("30740242204031C0000003E7", hex);
    }
    
    @Test
    public void testCreateSGTIN198HEX() throws Exception {
        String hex = epcTools.createSGTIN_198HexEPC(1, 6, "030430", "0058045", "400400010010");
        Assert.assertEquals("36381DB78038AF5A3060D183060C583062C000000000000000", hex);
        
        hex = epcTools.createSGTIN_198HexEPC(1, 6, "030430", "0058045", "12345abcABC012345678");
        Assert.assertEquals("36381DB78038AF58B266D1AE1C58E0C286C18B266D1AB66EE0", hex);
    }
    
    @Test
    public void testCreateSSCCHEX() throws Exception {
        String sscc = "340320460000000000";
        String hex = epcTools.createSSCCHexEPC(2, "4032046", "3", "0");
        
        Assert.assertEquals(hex, "3154F618B8B2D05E00000000");
        
        Assert.assertEquals(sscc, epcTools.getSSCC(epcTools.parseHexString(hex)));
    }
    
    @Test
    public void testEPCPureIdentityURI() throws Exception {
//       String hex = epcTools.createSSCC_96HexEPC(3, "40320460", "1", "1010120710");
//       Assert.assertEquals(epcTools.createEPCTagIdentityURI(hex), "urn:epc:tag:sscc-96:0.4032046.1010120710");
    }
    
    @Test
    public void testEPCTagIdentityURI() {
        
    }
    
    @Test 
    public void testParseSGTIN96() throws Exception {
        HashMap<EPCTools.TAG_DATA, String> map = epcTools.parseHexString("30740242204031C0000003E7");
        Assert.assertEquals("00037000657354", epcTools.getGTIN(map));
        Assert.assertEquals("000000000999", epcTools.getSerialNumber(map));
        Assert.assertEquals("065735", map.get(EPCTools.TAG_DATA.ITEM_REFERENCE));
        Assert.assertEquals("000000000999", map.get(EPCTools.TAG_DATA.SERIAL_NUMBER));
        
        HashMap<EPCTools.TAG_DATA, String> map_ = epcTools.parseHexString("30740242204031C0000003E7");
        Assert.assertEquals("0037000", map_.get(EPCTools.TAG_DATA.COMPANY_PREFIX));
        Assert.assertEquals("065735", map_.get(EPCTools.TAG_DATA.ITEM_REFERENCE));
        Assert.assertEquals("000000000999", map_.get(EPCTools.TAG_DATA.SERIAL_NUMBER));
        
        map = epcTools.parseHexString("30381D5D419C238000000001");
        Assert.assertEquals("1", map.get(EPCTools.TAG_DATA.FILTER));
        Assert.assertEquals("6", map.get(EPCTools.TAG_DATA.PARTITION));
        Assert.assertEquals("0422030", map.get(EPCTools.TAG_DATA.ITEM_REFERENCE));
        Assert.assertEquals("030069", map.get(EPCTools.TAG_DATA.COMPANY_PREFIX));
        Assert.assertEquals("SGTIN_96", map.get(EPCTools.TAG_DATA.HEADER));
        Assert.assertEquals("000000000001", map.get(EPCTools.TAG_DATA.SERIAL_NUMBER));
        
        System.out.println(" CompanyPrefix: " + epcTools.getCompanyPrefix(map));
        System.out.println(" ItemReference: " + epcTools.getItemReference(map));
    }
    
    @Test(expected=Exception.class)
    public void testExceptionWhenParsingOutOfRange() throws Exception {
        epcTools.parseHexString("36381DB78038AF58B266D1AE1C58E0C286C18B266D1AB66EE0_invalid");
    }
    
    @Test 
    public void testParseSGTIN198() throws Exception {
        HashMap<EPCTools.TAG_DATA, String> map = epcTools.parseHexString("36381DB78038AF58B266D1AE1C58E0C286C18B266D1AB66EE0");
        Assert.assertEquals("1", map.get(EPCTools.TAG_DATA.FILTER));
        Assert.assertEquals("6", map.get(EPCTools.TAG_DATA.PARTITION));
        Assert.assertEquals("0058045", map.get(EPCTools.TAG_DATA.ITEM_REFERENCE));
        Assert.assertEquals("030430", map.get(EPCTools.TAG_DATA.COMPANY_PREFIX));
        Assert.assertEquals(EPCTools.ENCODING.SGTIN_198.toString(), map.get(EPCTools.TAG_DATA.HEADER));
        Assert.assertEquals("12345abcABC012345678", map.get(EPCTools.TAG_DATA.SERIAL_NUMBER));
    }
    
    @Test 
    public void testParseSSCC() throws Exception {
        String epcHex = "315402422000000045000000";
        HashMap<EPCTools.TAG_DATA, String> map = epcTools.parseHexString(epcHex);
        Assert.assertEquals("0037000", epcTools.getCompanyPrefix(map));
        Assert.assertEquals("000370000000000693", epcTools.getSSCC(map));
    }
    
    /**
     * Test of getGTIN method, of class EPCTools.
     */
    @Test
    public void testGetGTIN() throws Exception {
        Assert.assertEquals("00037000657354", epcTools.getGTIN("30740242204031C0000003E7"));
        Assert.assertEquals("00304300580454", epcTools.getGTIN("36381DB78038AF58B266D1AE1C58E0C286C18B266D1AB66EE0"));
    }

    /**
     * Test of getCompanyPrefix method, of class EPCTools.
     */
    @Test
    public void testGetCompanyPrefix() throws Exception {
        Assert.assertEquals("0037000", epcTools.getCompanyPrefix("30740242204031C0000003E7"));
        Assert.assertEquals("0718908", epcTools.getCompanyPrefix("31542BE0F0218A7975000000"));
        Assert.assertEquals("030430", epcTools.getCompanyPrefix("36381DB78038AF58B266D1AE1C58E0C286C18B266D1AB66EE0"));
    }

    /**
     * Test of getItemReference method, of class EPCTools.
     */
    @Test
    public void testGetItemReference() throws Exception {
        Assert.assertEquals("065735", epcTools.getItemReference("30740242204031C0000003E7"));
        Assert.assertEquals("0058045", epcTools.getItemReference("36381DB78038AF58B266D1AE1C58E0C286C18B266D1AB66EE0"));
    }

    /**
     * Test of getSerialnumber method, of class EPCTools.
     */
    @Test
    public void testGetSerialNumber() throws Exception {
        Assert.assertEquals("000000000999", epcTools.getSerialNumber("30740242204031C0000003E7"));
        Assert.assertEquals("0562723189", epcTools.getSerialNumber("31542BE0F0218A7975000000"));
        
        Assert.assertEquals("0562723189", epcTools.getSerialNumber(epcTools.parseHexString("31542BE0F0218A7975000000")));
        
        Assert.assertEquals("1562723189", epcTools.getSerialNumber("31542BE0F05D254375000000"));
        Assert.assertEquals("12345abcABC012345678", epcTools.getSerialNumber("36381DB78038AF58B266D1AE1C58E0C286C18B266D1AB66EE0"));
    }

}