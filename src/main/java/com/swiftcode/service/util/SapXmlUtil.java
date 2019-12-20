package com.swiftcode.service.util;

import com.google.common.base.Splitter;
import com.swiftcode.service.dto.BottleneckDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * SapXmlUtil Class
 *
 * @author Ray
 * @date 2019/12/11 14:41
 */
@Slf4j
public class SapXmlUtil {

    public static String buildImportDeviceXml(BottleneckDTO dto) {
        String replaceStartDate = dto.getStartDate().replace("-", "");
        String replaceEndDate = dto.getEndDate().replace("-", "");
        String replaceStartTime = dto.getStartTime().replace(":", "");
        String replaceEndTime = dto.getEndTime().replace(":", "");
        List<String> deviceCodes = Splitter.on(",").splitToList(dto.getDeviceCode());
        StringBuilder xmlStarter = new StringBuilder("<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "    <Body>\n" +
            "        <ZpmImportEqunr xmlns=\"urn:sap-com:document:sap:soap:functions:mc-style\">\n" +
            " <ItData xmlns=\"\">\n" +
            "     <!-- Optional -->\n");

        for (String device : deviceCodes) {
            xmlStarter.append("<item>\n" + "<Pernr>").append(dto.getUserCode()).append("</Pernr>\n").append("<Equnr>").append(device).append("</Equnr>\n").append("<Sdate>").append(replaceStartDate).append("</Sdate>\n").append("<Stime>").append(replaceStartTime).append("</Stime>\n").append("<Edate>").append(replaceEndDate).append("</Edate>\n").append("<Etime>").append(replaceEndTime).append("</Etime>\n").append("<Message></Message>\n").append("     </item>\n");
        }
        xmlStarter.append(" </ItData>\n" + "</ZpmImportEqunr>\n" + "</Body>\n" + "</Envelope>");
        log.info("xmlStarter: {}", xmlStarter.toString());
        return xmlStarter.toString();
    }

    public static String buildUserDevicesXml(String userCode) {
        return "<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "    <Body>\n" +
            "        <ZpmSearchEqunr xmlns=\"urn:sap-com:document:sap:soap:functions:mc-style\">\n" +
            " <EtData xmlns=\"\">\n" +
            " </EtData>\n" +
            " <Pernr xmlns=\"\">" + userCode + "</Pernr>\n" +
            "        </ZpmSearchEqunr>\n" +
            "    </Body>\n" +
            "</Envelope>";
    }

    /**
     * 释放人员
     *
     * @param userCode 员工工号
     * @return xml
     */
    public static String buildReleaseUserXml(String userCode) {
        return "<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "    <Body>\n" +
            "        <ZpmReleasePernr xmlns=\"urn:sap-com:document:sap:soap:functions:mc-style\">\n" +
            "            <ItData xmlns=\"\">\n" +
            "                <!-- Optional -->\n" +
            "                <item>\n" +
            "                    <Pernr></Pernr>\n" +
            "                    <Pernr1>" + userCode + "</Pernr1>\n" +
            "                    <Aufnr></Aufnr>\n" +
            "                </item>\n" +
            "            </ItData>\n" +
            "            <Pernr xmlns=\"\"></Pernr>\n" +
            "        </ZpmReleasePernr>\n" +
            "    </Body>\n" +
            "</Envelope>";
    }

    /**
     * 同步功能位置和设备数据
     *
     * @return xml
     */
    public static String buildGetPositionAndDeviceXml() {
        return "<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "    <Body>\n" +
            "        <ZpmSearchTplnrNew xmlns=\"urn:sap-com:document:sap:soap:functions:mc-style\">\n" +
            "            <!-- Optional -->\n" +
            "            <EtData xmlns=\"\">\n" +
            "            </EtData>\n" +
            "            <!-- Optional -->\n" +
            "            <EtData1 xmlns=\"\">\n" +
            "            </EtData1>\n" +
            "        </ZpmSearchTplnrNew>\n" +
            "    </Body>\n" +
            "</Envelope>";
    }

    /**
     * 同步物料数据
     *
     * @return xml
     */
    public static String buildMaterialsXml() {
        return "<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "    <Body>\n" +
            "        <ZpmSearchMatnr xmlns=\"urn:sap-com:document:sap:soap:functions:mc-style\">\n" +
            "            <EtData xmlns=\"\">\n" +
            "            </EtData>\n" +
            "        </ZpmSearchMatnr>\n" +
            "    </Body>\n" +
            "</Envelope>";
    }
}
