package searchengine.message;

public enum MessageType {

    SITE_IS_NOT_AVAILABLE_ERROR("Главная страница сайта не доступна"),
    INDEXATION_IS_ALREADY_RUNNING_ERROR("Индексация уже запущена"),
    EMPTY_QUERY_ERROR("Задан пустой поисковый запрос"),
    SITE_IS_NOT_INDEXED_ERROR("Сайт/сайты ещё не проиндексированы"),
    INVALID_URL_ERROR("Данная страница находится за пределами сайтов, указанных в конфигурационном файле"),
    URL_EMPTY_ERROR("Страница не указана"),
    INDEXATION_IS_STOPPED_BY_USER("Индексация остановлена пользователем"),
    INDEXATION_IS_NOT_RUNNING("Индексация завершена!");

    private final String message;

    MessageType(String message) {
        this.message = message;
    }
    public String getText() {
        return message;
    }

}
