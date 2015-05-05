package com.jensui.epctools;

import java.io.Serializable;
import java.text.ParseException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author chpressler
 */
public class EPCTools implements Serializable {

    private static HashMap<Long, ENCODING> headerEncodings;
    private static HashMap<Long, Long> giaiPartitionTableCompPrefix;
    private static HashMap<Long, Long> giaiPartitionTableIndividualAssetRef;
    private static HashMap<Long, Long> giaiPartitionTableSerialReference;

    public EPCTools() {
        init();
    }

    private void init() {
        /**
         * The header determines the EPC tag standard. The following are a few
         * of the defined header standards bound to the representing HEX Value
         * in the Header
         */
        headerEncodings = new HashMap<>();
        headerEncodings.put(206L, ENCODING.DOD_64);
        headerEncodings.put(207L, ENCODING.DOD_96);
        headerEncodings.put(54L, ENCODING.SGTIN_198);
        headerEncodings.put(48L, ENCODING.SGTIN_96);
        headerEncodings.put(49L, ENCODING.SSCC);
        headerEncodings.put(50L, ENCODING.GLN_96);
        headerEncodings.put(51L, ENCODING.GRAI_96);
        headerEncodings.put(52L, ENCODING.GIAI_96);
        headerEncodings.put(53L, ENCODING.GID_96);

        giaiPartitionTableCompPrefix = new HashMap<>();
        giaiPartitionTableCompPrefix.put(0L, 40L);
        giaiPartitionTableCompPrefix.put(1L, 37L);
        giaiPartitionTableCompPrefix.put(2L, 34L);
        giaiPartitionTableCompPrefix.put(3L, 30L);
        giaiPartitionTableCompPrefix.put(4L, 27L);
        giaiPartitionTableCompPrefix.put(5L, 24L);
        giaiPartitionTableCompPrefix.put(6L, 20L);

        giaiPartitionTableSerialReference = new HashMap<>();
        giaiPartitionTableSerialReference.put(0L, 18L);
        giaiPartitionTableSerialReference.put(1L, 21L);
        giaiPartitionTableSerialReference.put(2L, 24L);
        giaiPartitionTableSerialReference.put(3L, 28L);
        giaiPartitionTableSerialReference.put(4L, 31L);
        giaiPartitionTableSerialReference.put(5L, 34L);
        giaiPartitionTableSerialReference.put(6L, 38L);

        giaiPartitionTableIndividualAssetRef = new HashMap<>();
        giaiPartitionTableIndividualAssetRef.put(0L, 42L);
        giaiPartitionTableIndividualAssetRef.put(1L, 45L);
        giaiPartitionTableIndividualAssetRef.put(2L, 48L);
        giaiPartitionTableIndividualAssetRef.put(3L, 52L);
        giaiPartitionTableIndividualAssetRef.put(4L, 55L);
        giaiPartitionTableIndividualAssetRef.put(5L, 58L);
        giaiPartitionTableIndividualAssetRef.put(6L, 62L);
    }

    /**
     * Depending on the EPC tag standard these are the tag data constructs
     *
     */
    public enum TAG_DATA {

        HEADER, FILTER, PARTITION, COMPANY_PREFIX, ITEM_REFERENCE, SERIAL_REFERENCE, SERIAL_NUMBER, UNALLOCATED, INDIVIDUAL_ASSET_REFERENCE, CAGE_CODE
    }

    public enum ENCODING {

        DOD_64, DOD_96, SGTIN_96, SSCC, GLN_96, GRAI_96, GIAI_96, GID_96, SGTIN_198
    }

    private String fillLeftWithZeros(String s, int digits) {
        while (s.length() < digits) {
            s = "0" + s;
        }
        return s;
    }

    private String fillRightWithZeros(String s, int digits) {
        while (s.length() < digits) {
            s += "0";
        }
        return s;
    }

    private String hexToBinaryString(String hex) {
        String bin = "";
        for (byte b : hex.getBytes()) {
            bin += fillLeftWithZeros(Integer.toBinaryString(Integer.parseInt(new String(new byte[]{b}), 16)), 4);
        }
        return bin;
    }

    private String binaryStringToHex(String bin) {
        return Long.toHexString(binStringToLong(bin));
    }

    public String getSerialNumber(HashMap<TAG_DATA, String> data) {
        if (data.get(TAG_DATA.HEADER).equals(ENCODING.SGTIN_96.toString()) || data.get(TAG_DATA.HEADER).equals(ENCODING.SGTIN_198.toString())) {
            return data.get(TAG_DATA.SERIAL_NUMBER);
        } else if (data.get(TAG_DATA.HEADER).equals(ENCODING.SSCC.toString())) {
            return data.get(TAG_DATA.SERIAL_REFERENCE);
        } else {
            return "";
        }
    }

    public String getCompanyPrefix(HashMap<TAG_DATA, String> data) {
        return data.get(TAG_DATA.COMPANY_PREFIX);
    }

    public String getGTIN(HashMap<TAG_DATA, String> data) throws Exception {
        if (data.get(TAG_DATA.HEADER).equals(ENCODING.SGTIN_96.toString()) || data.get(TAG_DATA.HEADER).equals(ENCODING.SGTIN_198.toString())) {
            String gtinNoChksum = data.get(TAG_DATA.ITEM_REFERENCE).substring(0, 1) + data.get(TAG_DATA.COMPANY_PREFIX) + data.get(TAG_DATA.ITEM_REFERENCE).substring(1);
            return gtinNoChksum + getChecksum(gtinNoChksum);
        } else {
            return null;
        }
    }

    public String getGTIN(String epcHex) throws Exception {
        HashMap<TAG_DATA, String> data = parseHexString(epcHex);
        if (data.get(TAG_DATA.HEADER).equals(ENCODING.SGTIN_96.toString()) || data.get(TAG_DATA.HEADER).equals(ENCODING.SGTIN_198.toString())) {
            String gtinNoChksum = data.get(TAG_DATA.ITEM_REFERENCE).substring(0, 1) + data.get(TAG_DATA.COMPANY_PREFIX) + data.get(TAG_DATA.ITEM_REFERENCE).substring(1);
            return gtinNoChksum + getChecksum(gtinNoChksum);
        } else {
            return null;
        }
    }

    public String getCompanyPrefix(String epcHex) throws Exception {
        HashMap<TAG_DATA, String> map = parseHexString(epcHex);
        return map.get(TAG_DATA.COMPANY_PREFIX);
    }

    public String getItemReference(String epcHex) throws Exception {
        HashMap<TAG_DATA, String> map = parseHexString(epcHex);
        return map.get(TAG_DATA.ITEM_REFERENCE);
    }

    public String getSerialNumber(String epcHex) throws Exception {
        HashMap<TAG_DATA, String> map = parseHexString(epcHex);
        return getSerialNumber(map);
    }

    public String getItemReference(HashMap<TAG_DATA, String> data) throws Exception {
        if (data.get(TAG_DATA.HEADER).equals(ENCODING.SGTIN_96.toString())) {
            return data.get(TAG_DATA.ITEM_REFERENCE);
        } else {
            return null;
        }
    }

    public String getSSCC(String epcHex) throws Exception {
        return getSSCC(parseHexString(epcHex));
    }

    public String getSSCC(HashMap<TAG_DATA, String> data) throws Exception {
        if (data.get(TAG_DATA.HEADER).equals(ENCODING.SSCC.toString())) {
            String compPrefix = data.get(TAG_DATA.COMPANY_PREFIX);
            String serialRef = data.get(TAG_DATA.SERIAL_REFERENCE);
            String sscc = (serialRef.substring(0, 1) + compPrefix + serialRef.substring(1));
            return sscc + getChecksum(sscc);
        } else {
            return null;
        }
    }

    public String createSGTIN_96HexEPC(int filter, int partition, String compPrefix, String itemRef, String serialNumber) throws Exception {
        try {
            if (Long.parseLong(serialNumber) > 274877906944L) { //SGTIN-96 reserves 38 bit for sn. 2 ^ 38 = 274877906944 max value for sn in SGTIN-96
                throw new Exception("serialnumber (" + serialNumber + ") out of range for SGTIN-96. Maximum Serialnumber value is 274877906944.");
            }
        } catch (NumberFormatException e) {
            throw new Exception("returned serialnumber (" + serialNumber + ") could not be parsed. Serialnumber must be numeric positive value. Max supported value is: 9223372036854775807.", e);
        }
        if (filter > 7) {
            throw new Exception("filter value can not be bigger than 8");
        }
        String b_header = fillLeftWithZeros(Integer.toBinaryString(48), 8); //8 bit header
        String b_filter = fillLeftWithZeros(Integer.toBinaryString(filter), 3); //3 bit filter
        String b_partition = fillLeftWithZeros(Integer.toBinaryString(partition), 3); //3 bit partition 
        long compPrefixLength = giaiPartitionTableCompPrefix.get((long) partition);
        if (Integer.toBinaryString(Integer.parseInt(compPrefix)).length() > compPrefixLength) {
            throw new Exception("comp prefix length for partition: " + partition + " is too big. Max Length: " + compPrefixLength + ".");
        }
        String b_compPrefix = fillLeftWithZeros(Integer.toBinaryString(Integer.parseInt(compPrefix)), (int) compPrefixLength);
        long itemRefLength = giaiPartitionTableIndividualAssetRef.get((long) partition) - 38;
        if (Integer.toBinaryString(Integer.parseInt(itemRef)).length() > itemRefLength) {
            throw new Exception("item reference length for partition: " + partition + " is too big. Max Length: " + itemRefLength + ".");
        }
        String b_itemRef = fillLeftWithZeros(Integer.toBinaryString(Integer.parseInt(itemRef)), (int) itemRefLength);
        String b_serialNumber = fillLeftWithZeros(Long.toBinaryString(Long.parseLong(serialNumber)), 38); //38 bit sn
        String bin = b_header + b_filter + b_partition + b_compPrefix + b_itemRef + b_serialNumber;
        String hex = "";
        int offs = 0;
        while (offs < bin.length()) {
            hex += binaryStringToHex(bin.substring(offs, offs += 4));
        }
        return hex.toUpperCase();
    }

    public String createSGTIN_198HexEPC(int filter, int partition, String compPrefix, String itemRef, String serialNumber) throws Exception {
        if (serialNumber.length() > 20) { //up to 20 alphanumeric digits
            throw new Exception("serialnumber length can not be higher than 20");
        }
        if (filter > 7) {
            throw new Exception("filter value can not be bigger than 8");
        }
        String b_header = fillLeftWithZeros(Integer.toBinaryString(54), 8); //8 bit header
        String b_filter = fillLeftWithZeros(Integer.toBinaryString(filter), 3); //3 bit filter
        String b_partition = fillLeftWithZeros(Integer.toBinaryString(partition), 3); //3 bit partition 
        long compPrefixLength = giaiPartitionTableCompPrefix.get((long) partition);
        if (Integer.toBinaryString(Integer.parseInt(compPrefix)).length() > compPrefixLength) {
            throw new Exception("comp prefix length for partition: " + partition + " is too big. Max Length: " + compPrefixLength + ".");
        }
        String b_compPrefix = fillLeftWithZeros(Integer.toBinaryString(Integer.parseInt(compPrefix)), (int) compPrefixLength);
        long itemRefLength = giaiPartitionTableIndividualAssetRef.get((long) partition) - 38;
        if (Integer.toBinaryString(Integer.parseInt(itemRef)).length() > itemRefLength) {
            throw new Exception("item reference length for partition: " + partition + " is too big. Max Length: " + itemRefLength + ".");
        }
        String b_itemRef = fillLeftWithZeros(Integer.toBinaryString(Integer.parseInt(itemRef)), (int) itemRefLength);
        String b_serialNumber = fillRightWithZeros(toBinaryString(serialNumber), 140);//140 bit sn - alphanumeric (140 bit / 20 digits = 7bit - ASCI 128 per digit)

        String bin = b_header + b_filter + b_partition + b_compPrefix + b_itemRef + b_serialNumber;
        String hex = "";
        int offs = 0;
        while (offs < bin.length()) {
            hex += binaryStringToHex(bin.substring(offs, ((offs += 4) > bin.length()) ? bin.length() : offs));
        }
        return hex.toUpperCase();
    }

    private String toBinaryString(String s) {
        byte[] ba;
        String ret = "";
        ba = s.getBytes();
        for (byte b : ba) {
            ret += fillLeftWithZeros(Integer.toBinaryString((int) b), 7);
        }
        return ret;
    }

    public String createSSCCHexEPC(int filter, int partition, String compPrefix, String extensionCode, String serialRef) throws Exception {
        if (filter > 7) {
            throw new Exception("filter value can not be bigger than 8");
        }
        String b_header = fillLeftWithZeros(Integer.toBinaryString(49), 8); //8 bit header
        String b_filter = fillLeftWithZeros(Integer.toBinaryString(filter), 3); //3 bit filter
        String b_partition = fillLeftWithZeros(Integer.toBinaryString(partition), 3); //3 bit partition 
        long compPrefixLength = giaiPartitionTableCompPrefix.get((long) partition);

        String b_compPrefix = fillLeftWithZeros(Integer.toBinaryString(Integer.parseInt(compPrefix)), (int) compPrefixLength);
        long serialRefBitLength = giaiPartitionTableSerialReference.get((long) partition);
        int serialRefDigits = partition + 5;

        String b_serialRef = Long.toBinaryString(Long.parseLong(extensionCode + fillLeftWithZeros(serialRef, serialRefDigits - 1)));

        b_serialRef = fillLeftWithZeros(b_serialRef, (int) serialRefBitLength);
        String b_unallocated = fillLeftWithZeros("0", 24); //24 bit unallocated
        String bin = b_header + b_filter + b_partition + b_compPrefix + b_serialRef + b_unallocated;

        String hex = "";
        int offs = 0;
        while (offs < 96) {
            hex += binaryStringToHex(bin.substring(offs, offs += 4));
        }
        return hex.toUpperCase();
    }

    public String createSGTIN_96HexEPC(String epc, String serialNumber) throws Exception {
        HashMap<TAG_DATA, String> map = parseHexString(epc);
        int filter = Integer.parseInt(map.get(TAG_DATA.FILTER));
        int partition = Integer.parseInt(map.get(TAG_DATA.PARTITION));
        String companyPrefix = getCompanyPrefix(map);
        String itemReference = getItemReference(map);
        return createSGTIN_96HexEPC(filter, partition, companyPrefix, itemReference, serialNumber);
    }

    public String createEPCPureIdentityURI_96(String epcHex) throws Exception {
        HashMap<TAG_DATA, String> map = parseHexString(epcHex);
        if (map.get(TAG_DATA.HEADER).equals(ENCODING.SGTIN_96.toString())) {
            return "urn:epc:id:sgtin:" + getCompanyPrefix(map) + "." + getItemReference(map) + "." + map.get(TAG_DATA.SERIAL_NUMBER);
        } else if (map.get(TAG_DATA.HEADER).equals(ENCODING.SSCC.toString())) {
            return "urn:epc:id:sscc:" + getCompanyPrefix(map) + "." + map.get(TAG_DATA.SERIAL_REFERENCE);
        } else {
            throw new Exception(map.get(TAG_DATA.HEADER) + " not supported yet.");
        }
    }

    public String createEPCPureIdentityURI_198(String epcHex) throws Exception {
//        HashMap<TAG_DATA, Long> map = parseHexString(epcHex);
//        if (headerEncodings.get(map.get(TAG_DATA.HEADER)).equals(ENCODING_STANDARD.SGTIN_96)) {
//            return "urn:epc:id:sgtin:"+map.get(TAG_DATA.COMPANY_PREFIX)+"."+map.get(TAG_DATA.ITEM_REFERENCE)+"."+map.get(TAG_DATA.SERIAL_NUMBER);
//        } else if(headerEncodings.get(map.get(TAG_DATA.HEADER)).equals(ENCODING_STANDARD.SSCC_96)) {
//            return "urn:epc:id:sgtin:"+map.get(TAG_DATA.COMPANY_PREFIX)+"."+map.get(TAG_DATA.SERIAL_REFERENCE);
//        } else {
//            throw new Exception(headerEncodings.get(map.get(TAG_DATA.HEADER))+" not supported yet.");
//        }
        return "Not yet Implemented";
    }

    public String createEPCTagURI_96(String epcHex) throws Exception {
        HashMap<TAG_DATA, String> map = parseHexString(epcHex);
        if (map.get(TAG_DATA.HEADER).equals(ENCODING.SGTIN_96.toString())) {
            return "urn:epc:tag:sgtin-96:" + map.get(TAG_DATA.FILTER) + "." + map.get(TAG_DATA.COMPANY_PREFIX) + "." + map.get(TAG_DATA.ITEM_REFERENCE) + "." + map.get(TAG_DATA.SERIAL_NUMBER);
        } else if (map.get(TAG_DATA.HEADER).equals(ENCODING.SSCC.toString())) {
            return "urn:epc:tag:sscc-96:" + map.get(TAG_DATA.FILTER) + "." + map.get(TAG_DATA.COMPANY_PREFIX) + "." + map.get(TAG_DATA.SERIAL_REFERENCE);
        } else {
            throw new Exception(map.get(TAG_DATA.HEADER) + " not supported yet.");
        }
    }

    public String createEPCTagURI_198(String epcHex) throws Exception {
        return "Not yet Implemented";
    }

//    public String create198EPCTagIdentityURI(String epcHex) throws Exception {
//        HashMap<TAG_DATA, Long> map = parseHexString(epcHex);
//        if (headerEncodings.get(map.get(TAG_DATA.HEADER)).equals(ENCODING_STANDARD.SGTIN_96)) {
//            return "urn:epc:tag:sgtin-198:"+map.get(TAG_DATA.FILTER)+"."+map.get(TAG_DATA.COMPANY_PREFIX)+"."+map.get(TAG_DATA.ITEM_REFERENCE)+"."+"1"+fillLeftWithZeros(map.get(TAG_DATA.SERIAL_NUMBER), 19);
//        } else if(headerEncodings.get(map.get(TAG_DATA.HEADER)).equals(ENCODING_STANDARD.SSCC_96)) {
//            return "urn:epc:tag:sscc-198:"+map.get(TAG_DATA.FILTER)+"."+map.get(TAG_DATA.COMPANY_PREFIX)+"."+map.get(TAG_DATA.SERIAL_REFERENCE);
//        } else {
//            throw new Exception(headerEncodings.get(map.get(TAG_DATA.HEADER))+" not supported yet.");
//        }
//    }
    public String createSGTIN_96HexEPC(int rank, String gtin, String sn) throws Exception {
        return createSGTIN_96HexEPC(rank, 6, gtin.substring(1, 7), gtin.substring(0, 1) + gtin.substring(7, 13), sn);
    }

    public String createSGTIN_198HexEPC(int rank, String gtin, String sn) throws Exception {
        return createSGTIN_198HexEPC(rank, 6, gtin.substring(1, 7), gtin.substring(0, 1) + gtin.substring(7, 13), sn);
    }

    public String createSSCCHexEPC(int rank, String companyPrefix, String extensionCode, String serialRef) throws Exception {
        int partition = (companyPrefix.length() < 6) ? 6 : (12 - companyPrefix.length());
//        if (serialRef.length() > giaiPartitionTableSerialReference.get(partition)) {
//           throw new Exception("Can not create SSCC-96. serialRef too long");
//        }
//        if (serialRef.length() == giaiPartitionTableSerialReference.get(partition)) {
//            if(serialRef.substring(0, 1).equals(extensionCode)) {
//                serialRef = serialRef.substring(1);
//            }
//        }
        return createSSCCHexEPC(rank, partition, companyPrefix, extensionCode, serialRef);
    }

    public ENCODING getEncoding(String epcHex) {
        String binaryData = hexToBinaryString(epcHex);
        return headerEncodings.get(binStringToLong(binaryData.substring(0, 8)));
    }

    public boolean isSGTIN96(String epcHex) {
        String binaryData = hexToBinaryString(epcHex);
        return headerEncodings.get(binStringToLong(binaryData.substring(0, 8))) == ENCODING.SGTIN_96;
    }

    public boolean isSGTIN198(String epcHex) {
        String binaryData = hexToBinaryString(epcHex);
        return headerEncodings.get(binStringToLong(binaryData.substring(0, 8))) == ENCODING.SGTIN_198;
    }

    public boolean isSSCC(String epcHex) {
        String binaryData = hexToBinaryString(epcHex);
        return headerEncodings.get(binStringToLong(binaryData.substring(0, 8))) == ENCODING.SSCC;
    }

    /**
     * @param hexData the Data as Hex String
     *
     * @return a HashMap with all included TagData and values
     * @throws Exception
     */
    public HashMap<TAG_DATA, String> parseHexString(String hexData) throws Exception {
//		if(hexData.length() != 24) {
//			throw new Exception("invalid input hex string");
//		}
        String binaryData = hexToBinaryString(hexData);
        HashMap<TAG_DATA, String> data = new HashMap<>();
        int offs = 0;
        data.put(TAG_DATA.HEADER, headerEncodings.get(binStringToLong(binaryData.substring(0, offs += 8))).toString());

        long filter = binStringToLong(binaryData.substring(offs, offs += 3));
        data.put(TAG_DATA.FILTER, Long.toString(filter));
        //data.get(TAG_DATA.PARTITION);
        long partitionValue = binStringToLong(binaryData.substring(offs, offs += 3));
        data.put(TAG_DATA.PARTITION, Long.toString(partitionValue));
        long compPrefixBinaryLength = giaiPartitionTableCompPrefix.get(partitionValue);
        int compPrefixDecimalLength = (int) (12 - partitionValue);
        long compPrefix = binStringToLong(binaryData.substring(offs, offs += compPrefixBinaryLength));
        data.put(TAG_DATA.COMPANY_PREFIX, fillLeftWithZeros(Long.toString(compPrefix), compPrefixDecimalLength));
        if (!data.get(TAG_DATA.HEADER).equals(ENCODING.SSCC.toString())) {
            long itemreference = binStringToLong(binaryData.substring(offs, offs += (giaiPartitionTableIndividualAssetRef.get(partitionValue) - 38)));
            String itemRef = fillLeftWithZeros(Long.toString(itemreference), (int) (13 - (compPrefixDecimalLength)));
            data.put(TAG_DATA.ITEM_REFERENCE, itemRef);
        }
        if (data.get(TAG_DATA.HEADER).equals(ENCODING.SGTIN_96.toString())) {
            long serial = binStringToLong(binaryData.substring(offs));
            data.put(TAG_DATA.SERIAL_NUMBER, fillLeftWithZeros(Long.toString(serial), 12));
        } else if (data.get(TAG_DATA.HEADER).equals(ENCODING.SSCC.toString())) {
            long serialreference = binStringToLong(binaryData.substring(offs, offs += (giaiPartitionTableSerialReference.get(partitionValue))));
            int snLength = (int) (17 - compPrefixDecimalLength); // 18 (total sscc length) - checkDigit - companyPrefixLength
            data.put(TAG_DATA.SERIAL_REFERENCE, fillLeftWithZeros(Long.toString(serialreference), snLength));
            long unallocated = binStringToLong(binaryData.substring(offs));
            data.put(TAG_DATA.UNALLOCATED, Long.toString(unallocated));
        } else if (data.get(TAG_DATA.HEADER).equals(ENCODING.DOD_64.toString())) {
            //TODO
        } else if (data.get(TAG_DATA.HEADER).equals(ENCODING.DOD_96.toString())) {
            //TODO
        } else if (data.get(TAG_DATA.HEADER).equals(ENCODING.GIAI_96.toString())) {
            //TODO
        } else if (data.get(TAG_DATA.HEADER).equals(ENCODING.GID_96.toString())) {
            //TODO
        } else if (data.get(TAG_DATA.HEADER).equals(ENCODING.GLN_96.toString())) {
            //TODO
        } else if (data.get(TAG_DATA.HEADER).equals(ENCODING.GRAI_96.toString())) {
            //TODO
        } else if (data.get(TAG_DATA.HEADER).equals(ENCODING.SGTIN_198.toString())) {
            data.put(TAG_DATA.SERIAL_NUMBER, fillLeftWithZeros(binStringTo7bitASCII(binaryData.substring(offs, binaryData.length())), 20));
        } else {
            throw new Exception("invalid input hex string");
        }

        return data;
    }

    private String binStringTo7bitASCII(String s) {
        int offset = 0, length;
        String ret = "";
        while (offset < s.length()) {
            length = (offset + 7 > s.length()) ? (s.length() - offset) : 7;
            long l = binStringToLong(s.substring(offset, offset + length));
            if (l > 9) { //skip control chars
                ret += (char) binStringToLong(s.substring(offset, offset + length));
            }
            offset += 7;
        }
        return ret;
    }

    private long binStringToLong(String s) {
        char[] c = s.toCharArray();
        long z = 0;
        long erg = 0;
        for (int i = c.length - 1; i >= 0; i--) {
            if (z == 0 && c[i] == '0') {
                z++;
                continue;
            }
            erg += Math.pow(c[i] != '0' ? 2 : 0, z);
            z++;
        }
        return erg;
    }

    private int getChecksum(String sscc) throws ParseException {
        int realCC = -1;
        try {

            int checksum = 0;
            int factor = 3;

            for (int i = sscc.length(); i > 0; --i) {
                checksum += (Integer.parseInt(String.valueOf(sscc.charAt(i - 1))) * factor);
                factor = 4 - factor;
            }
            realCC = ((1000 - checksum) % 10);

        } catch (Exception e) {
            Logger.getLogger(EPCTools.class.getName()).log(Level.SEVERE, null, e);
            throw new ParseException("sscc checksum error!", 1);
        }
        return realCC;
    }

    public boolean isValidGTIN(String gtin) {
        if (gtin == null || gtin.isEmpty()) {
            return false;
        }
        int factor = 3;
        int checksum = 0;
        try {
            int currentCC = Integer.parseInt(String.valueOf(gtin.charAt(gtin.length() - 1)));
            for (int i = gtin.length() - 1; i > 0; --i) {
                checksum += (Integer.parseInt(String.valueOf(gtin.charAt(i - 1))) * factor);
                factor = 4 - factor;
            }
            int realCC = ((1000 - checksum) % 10);
            if (realCC == currentCC) {
                return true;
            }
        } catch (Exception e) {
            Logger.getLogger(EPCTools.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
        return false;
    }
}
