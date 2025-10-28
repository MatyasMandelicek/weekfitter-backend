import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "calendar_events")
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType category;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_type")
    private SportType sportType;

    @Column(name = "all_day")
    private boolean allDay;

    private Double duration;
    private Double distance;

    private String sportDescription;

    @Column(name = "file_path")
    private String filePath;

    // === Notifikace k události ===
    @JsonIgnore // zabrání cyklické serializaci (Event -> Notifications -> Event -> ...)
    @OneToMany(mappedBy = "event", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default // pokud používáš Lombok @Builder, jinak by inicializace zmizela
    private List<Notification> notifications = new ArrayList<>();

    // Volitelné helpery – IDE pak netvrdí, že je pole „nepoužité“
    public void addNotification(Notification n) {
        notifications.add(n);
        n.setEvent(this);
    }

    public void removeNotification(Notification n) {
        notifications.remove(n);
        n.setEvent(null);
    }
}
