package com.scrapper.services;

//@Data
//@AllArgsConstructor
//@NoArgsConstructor
public class DataElement {

    private String category;
    private int rainLevel;
    private int snowLevel;

    public DataElement() {
    }

    public DataElement(String category, int rainLevel, int snowLevel) {
        this.category = category;
        this.rainLevel = rainLevel;
        this.snowLevel = snowLevel;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getRainLevel() {
        return rainLevel;
    }

    public void setRainLevel(int rainLevel) {
        this.rainLevel = rainLevel;
    }

    public int getSnowLevel() {
        return snowLevel;
    }

    public void setSnowLevel(int snowLevel) {
        this.snowLevel = snowLevel;
    }
}
