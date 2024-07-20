package searchengine.components;

import lombok.Data;

@Data
public class DataPropertiesSearch
{
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private float relevance;
}
