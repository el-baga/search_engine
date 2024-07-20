package searchengine.dto.response;

import lombok.Data;
import searchengine.components.DataPropertiesSearch;
import searchengine.components.StatisticsData;

import java.util.List;

@Data
public class ApiRs {
    private boolean result;
    private String error;
    private int count;
    private List<DataPropertiesSearch> data;
    private StatisticsData statistics;

    public ApiRs() {
    }

    public ApiRs(boolean result) {
        this.result = result;
    }

    public ApiRs(boolean result, String error) {
        this.result = result;
        this.error = error;
    }

    public ApiRs(boolean result, int count, List<DataPropertiesSearch> data) {
        this.result = result;
        this.count = count;
        this.data = data;
    }
}
