package Instagram;
public class Person {
    private long id;
    private String idString;
    private String name;
    private String picture;

    public Person(long id, String name, String picture) {
        this.id = id;
        this.name = name;
        this.picture = picture;
    }

    public Person(String idString, String name, String picture) {
        this.idString = idString;
        this.name = name;
        this.picture = picture;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIdString() {
        return this.idString;
    }

    public void setIdString(String idString) {
        this.idString = idString;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return this.picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

}
