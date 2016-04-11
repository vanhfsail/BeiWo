package bean;

/**
 * Created by huang.fan on 2016-3-23.
 */
public class Like {
    private String uId;
    private String postId;

    public Like() {
    }

    public Like(String uId, String postId) {
        this.uId = uId;
        this.postId = postId;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}

