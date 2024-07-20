package com.moutamid.daiptv.retrofit;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "tv")
public class EpgResponse {
    private List<Programme> programmes;

    @XmlElement(name = "programme")
    public List<Programme> getProgrammes() {
        return programmes;
    }

    public void setProgrammes(List<Programme> programmes) {
        this.programmes = programmes;
    }

    @XmlRootElement(name = "programme")
    public static class Programme {
        private String start;
        private String stop;
        private String channel;
        private String title;

        @XmlAttribute(name = "start")
        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        @XmlAttribute(name = "stop")
        public String getStop() {
            return stop;
        }

        public void setStop(String stop) {
            this.stop = stop;
        }

        @XmlAttribute(name = "channel")
        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        @XmlElement(name = "title")
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
