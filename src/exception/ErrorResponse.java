package exception;

public class ErrorResponse {
    private String errorMessage;
    private String url;
    private Integer code;

    public ErrorResponse(String errorMessage, String url, Integer code) {
        this.errorMessage = errorMessage;
        this.url = url;
        this.code = code;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
