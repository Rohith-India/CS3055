package com.iith.json;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Co
{
    private String c;

    private Na[] na;

    private String n;

    @XmlAttribute
    public String getC ()
    {
        return c;
    }

    public void setC (String c)
    {
        this.c = c;
    }

    @XmlElement
    public Na[] getNa ()
    {
        return na;
    }

    public void setNa (Na[] na)
    {
        this.na = na;
    }

    @XmlAttribute
    public String getN ()
    {
        return n;
    }

    public void setN (String n)
    {
        this.n = n;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [c = "+c+", na = "+na+", n = "+n+"]";
    }
}
