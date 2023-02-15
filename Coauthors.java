package com.iith.json;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Coauthors
{
    private String nc;

    private Co[] co;

    private String n;

    @XmlElement
    public String getNc ()
    {
        return nc;
    }

    public void setNc (String nc)
    {
        this.nc = nc;
    }

    @XmlElement
    public Co[] getCo ()
    {
        return co;
    }

    public void setCo (Co[] co)
    {
        this.co = co;
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
        return "ClassPojo [nc = "+nc+", co = "+co+", n = "+n+"]";
    }
}
