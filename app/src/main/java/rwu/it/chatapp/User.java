package rwu.it.chatapp;

public class User {
    private String userId;
    private String name;
    private String email;
    private String profileImage;
    private String status;   // "online" ya "offline"
    private Long lastSeen;   // millis (System.currentTimeMillis())

    public User() {}

    public User(String userId, String name, String email,
                String profileImage, String status, Long lastSeen) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
        this.status = status;
        this.lastSeen = lastSeen;
    }

    // Getters + Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getLastSeen() { return lastSeen; }
    public void setLastSeen(Long lastSeen) { this.lastSeen = lastSeen; }
}
