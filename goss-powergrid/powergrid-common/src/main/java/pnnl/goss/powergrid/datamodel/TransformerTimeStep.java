//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.03.12 at 11:59:10 AM PDT 
//


package pnnl.goss.powergrid.datamodel;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for TransformerTimeStep complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TransformerTimeStep">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PowergridId">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="TimeStep">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}dateTime">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="TransformerId">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="TapPosition">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Ratio">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Status">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="P">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Q">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double">
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TransformerTimeStep", propOrder = {
    "powergridId",
    "timeStep",
    "transformerId",
    "tapPosition",
    "ratio",
    "status",
    "p",
    "q"
})
public class TransformerTimeStep
    implements Serializable
{

    private final static long serialVersionUID = 12343L;
    @XmlElement(name = "PowergridId")
    protected int powergridId;
    @XmlElement(name = "TimeStep", required = true)
    protected XMLGregorianCalendar timeStep;
    @XmlElement(name = "TransformerId")
    protected int transformerId;
    @XmlElement(name = "TapPosition")
    protected double tapPosition;
    @XmlElement(name = "Ratio")
    protected double ratio;
    @XmlElement(name = "Status")
    protected int status;
    @XmlElement(name = "P")
    protected double p;
    @XmlElement(name = "Q")
    protected double q;

    /**
     * Gets the value of the powergridId property.
     * 
     */
    public int getPowergridId() {
        return powergridId;
    }

    /**
     * Sets the value of the powergridId property.
     * 
     */
    public void setPowergridId(int value) {
        this.powergridId = value;
    }

    public boolean isSetPowergridId() {
        return true;
    }

    /**
     * Gets the value of the timeStep property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTimeStep() {
        return timeStep;
    }

    /**
     * Sets the value of the timeStep property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTimeStep(XMLGregorianCalendar value) {
        this.timeStep = value;
    }

    public boolean isSetTimeStep() {
        return (this.timeStep!= null);
    }

    /**
     * Gets the value of the transformerId property.
     * 
     */
    public int getTransformerId() {
        return transformerId;
    }

    /**
     * Sets the value of the transformerId property.
     * 
     */
    public void setTransformerId(int value) {
        this.transformerId = value;
    }

    public boolean isSetTransformerId() {
        return true;
    }

    /**
     * Gets the value of the tapPosition property.
     * 
     */
    public double getTapPosition() {
        return tapPosition;
    }

    /**
     * Sets the value of the tapPosition property.
     * 
     */
    public void setTapPosition(double value) {
        this.tapPosition = value;
    }

    public boolean isSetTapPosition() {
        return true;
    }

    /**
     * Gets the value of the ratio property.
     * 
     */
    public double getRatio() {
        return ratio;
    }

    /**
     * Sets the value of the ratio property.
     * 
     */
    public void setRatio(double value) {
        this.ratio = value;
    }

    public boolean isSetRatio() {
        return true;
    }

    /**
     * Gets the value of the status property.
     * 
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     */
    public void setStatus(int value) {
        this.status = value;
    }

    public boolean isSetStatus() {
        return true;
    }

    /**
     * Gets the value of the p property.
     * 
     */
    public double getP() {
        return p;
    }

    /**
     * Sets the value of the p property.
     * 
     */
    public void setP(double value) {
        this.p = value;
    }

    public boolean isSetP() {
        return true;
    }

    /**
     * Gets the value of the q property.
     * 
     */
    public double getQ() {
        return q;
    }

    /**
     * Sets the value of the q property.
     * 
     */
    public void setQ(double value) {
        this.q = value;
    }

    public boolean isSetQ() {
        return true;
    }

}