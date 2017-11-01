
package ru.javaops.masterjava.xml.schemanew;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GroupType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="GroupType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="FINISHED"/>
 *     &lt;enumeration value="CURRENT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "GroupType", namespace = "http://javaops.ru")
@XmlEnum
public enum GroupType {

    FINISHED,
    CURRENT;

    public String value() {
        return name();
    }

    public static GroupType fromValue(String v) {
        return valueOf(v);
    }

}
