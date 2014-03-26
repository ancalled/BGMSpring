package kz.bgm.platform.model.domain;


public class UpdateResult {
    private String status;
    private String error;
    private Long updateId;

    public UpdateResult(String status, String error, Long updateId) {
        this.status = status;
        this.error = error;
        this.updateId = updateId;
    }

    public String getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public Long getUpdateId() {
        return updateId;
    }
}
