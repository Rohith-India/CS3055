package com.iith.json;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Na
{
	
    private String f;

    private String pid;

    private String content;

    @XmlAttribute
    public String getF ()
    {
        return f;
    }

    public void setF (String f)
    {
        this.f = f;
    }

    @XmlAttribute
    public String getPid ()
    {
        return pid;
    }

    public void setPid (String pid)
    {
        this.pid = pid;
    }

    public String getContent ()
    {
        return content;
    }

    public void setContent (String content)
    {
        this.content = content;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [f = "+f+", pid = "+pid+", content = "+content+"]";
    }
}
