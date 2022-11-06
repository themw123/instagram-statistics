package Instagram;
import java.util.Comparator;

public class Post {
    private String shortcode;
    private int likes;
    private int comments;
    private String display_url;

    public Post(String shortcode, int likes, int comments, String display_url) {
        this.shortcode = shortcode;
        this.likes = likes;
        this.comments = comments;
        this.display_url = display_url;
    }

    public String getShortcode() {
        return this.shortcode;
    }

    public void setShortcode(String shortcode) {
        this.shortcode = shortcode;
    }

    public int getLikes() {
        return this.likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getComments() {
        return this.comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public String getDisplay_url() {
        return this.display_url;
    }

    public void setDisplay_url(String display_url) {
        this.display_url = display_url;
    }

}
