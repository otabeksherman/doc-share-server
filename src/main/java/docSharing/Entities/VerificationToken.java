package docSharing.Entities;


import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "verification_token")
public class VerificationToken {
    private static final int EXPIRATION = 60 * 24;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "token")
    private String token;
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "created_date")
    private Date createdDate;
    @Column(name = "expiry_date")
    private Date expiryDate;
    @Column
    private Boolean isActivated;

    public VerificationToken() {
        super();
    }

    public VerificationToken(final String token, final User user) {
        super();
        Calendar calendar = Calendar.getInstance();
        this.token = token;
        this.user = user;
        this.createdDate = new Date(calendar.getTime().getTime());
        this.expiryDate = calculateExpiryDate(7*EXPIRATION);
    }

    public VerificationToken(String token, User user, Date expiryDate) {
        this.token = token;
        this.user = user;
        this.createdDate = new Date(Calendar.getInstance().getTime().getTime());
        this.expiryDate = expiryDate;
    }

    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Timestamp(calendar.getTime().getTime()));
        calendar.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(calendar.getTime().getTime());
    }
    public void setId(Long id) {
        this.id = id;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public boolean isActivated() {
        if(expiryDate!=null && createdDate!=null) {
            Calendar calendar = Calendar.getInstance();
            if (new Date(calendar.getTime().getTime()).after(expiryDate))
                isActivated = false;
            else
                isActivated = true;
        } else {isActivated =true;}
        return isActivated;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
    }
}
