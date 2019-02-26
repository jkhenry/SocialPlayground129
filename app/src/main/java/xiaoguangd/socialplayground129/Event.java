package xiaoguangd.socialplayground129;

public class Event {
    /**
     * All data for a event.
     */
    private String title;
    //private String address;
    private String description;
    private String location;

    private String username;
    private String id;
    private long time;

    private String imgUri = "";

    private int good;
    private int bad;
    private int commentNumber;
    private int repost;

//    /**
//     * Constructor
//     */
//    public Event(String title, String address, String description) {
//        this.title = title;
//        this.address = address;
//        this.description = description;
//    }
//
//    /**
//     * Getters for private attributes of Event class.
//     */
//    //public String getAddress() { return this.address; }
//    //public String getDescription() { return this.description; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getBad() {
        return this.bad;
    }

    public void setBad(int bad) {
        this.bad = bad;
    }

    public int getGood() {
        return this.good;
    }

    public void setGood(int good) {
        this.good = good;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUser() {
        return this.username;
    }

    public void setUser(String username) {
        this.username = username;
    }

    public long getTime() { return this.time; }

    public void setTime(long time) {
        this.time = time;
    }

    public String getLocation() { return this.location; }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCommentNumber() {
        return this.commentNumber;
    }

    public void setCommentNumber(int commentNumber) {
        this.commentNumber = commentNumber;
    }

    public int getRepost() {
        return this.repost;
    }

    public void setRepost(int repost) {
        this.repost = repost;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImgUri() {
        return this.imgUri;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }
}
